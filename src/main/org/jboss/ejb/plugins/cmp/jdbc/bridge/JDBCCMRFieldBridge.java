/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.transaction.Transaction;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.invocation.Invocation;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.ejb.plugins.EntityInstanceCache;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.plugins.cmp.bridge.CMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCContext;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;

/**
 * JDBCCMRFieldBridge a bean relationship. This class only supports
 * relationships between entities managed by a JDBCStoreManager in the same
 * application.
 *
 * Life-cycle:
 *      Tied to the EntityBridge.
 *
 * Multiplicity:   
 *      One for each role that entity has.       
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.35 $
 */                            
public class JDBCCMRFieldBridge implements JDBCFieldBridge, CMRFieldBridge {
   // ------ Invocation messages ------
   
   /** tells the related continer to retrieve the id of the related entity */
   private static final Method GET_RELATED_ID;
   /** tells the related continer to add an id to its list related entities */
   private static final Method ADD_RELATION;
   /** tells the related continer to remove an id from its list related 
    * entities */
   private static final Method REMOVE_RELATION;
   
   // set the message method objects
   static {
      try {
         final Class empty[] = {};
         final Class type = CMRMessage.class;
         
         GET_RELATED_ID = type.getMethod("getRelatedId", new Class[] { 
            EntityEnterpriseContext.class,
            JDBCCMRFieldBridge.class
         });
         
         ADD_RELATION = type.getMethod("addRelation", new Class[] { 
            EntityEnterpriseContext.class,
            Object.class
         });
         
         REMOVE_RELATION = type.getMethod("removeRelation", new Class[] { 
            EntityEnterpriseContext.class,
            Object.class
         });
         }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);            
      }
   }
   
   /**
    * The entity bridge to which this cmr field belongs.
    */
   private JDBCEntityBridge entity;
   
   /**
    * The manager of this entity.
    */
   private JDBCStoreManager manager;
   
   /**
    * Metadata of the relationship role that this field represents.
    */
   private JDBCRelationshipRoleMetaData metadata;
   
   /**
    * That data source used to acess the relation table if relevent.
    */
   private DataSource dataSource;

   /**
    * That the relation table name if relevent.
    */
   private String tableName;

   /**
    * Does this cmr field have foreign keys.
    */
   private boolean hasForeignKey;
   
   /**
    * The key fields that this entity maintains in the relation table.
    */
   private List tableKeyFields;
   
   /**
    * Foreign key fields of this entity (i.e., related entities pk fields)
    */
   private List foreignKeyFields;

   /**
    * JDBCType for the foreign key fields. Basically, this is an ordered
    * merge of the JDBCType of the foreign key field.
    */
   private JDBCType jdbcType;

   /**
    * The related entity's container.
    */
   private WeakReference relatedContainer;
   
   /**
    * The related entity's jdbc store manager
    */
   private JDBCStoreManager relatedManager;
   
   /**
    * The related entity.
    */
   private JDBCEntityBridge relatedEntity;
   
   /**
    * The related entity's cmr field for this relationship.
    */
   private JDBCCMRFieldBridge relatedCMRField;
   
   /**
    * da log.
    */
   private Logger log;
   
   /**
    * Creates a cmr field for the entity based on the metadata.
    */
   public JDBCCMRFieldBridge(
         JDBCEntityBridge entity,
         JDBCStoreManager manager, 
         JDBCRelationshipRoleMetaData metadata) throws DeploymentException {

      this.entity = entity;
      this.manager = manager;
      this.metadata = metadata;

      //  Creat the log
      String categoryName = this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName() +
            ".";
      if(metadata.getCMRFieldName() != null) {
         categoryName += metadata.getCMRFieldName();
      } else {
         categoryName +=
            metadata.getRelatedRole().getEntity().getName() +
            "-" +
            metadata.getRelatedRole().getCMRFieldName();
      }
      this.log = Logger.getLogger(categoryName);
   }

   public void resolveRelationship() throws DeploymentException {
      // Data Source
      dataSource = metadata.getRelationMetaData().getDataSource();

      // Fix table name
      //
      // This code doesn't work here...  The problem each side will generate
      // the table name and this will only work for simple generation.
      tableName = SQLUtil.fixTableName(
            metadata.getRelationMetaData().getDefaultTableName(),
            dataSource);

      //
      // Set handles to the related entity's container, cache, 
      // manager, and invoker
      //
      
      // Related Entity Name
      String relatedEntityName = 
            metadata.getRelatedRole().getEntity().getName();
      
      // Related Entity
      Catalog catalog = (Catalog)manager.getApplicationData("CATALOG");
      relatedEntity = 
            (JDBCEntityBridge)catalog.getEntityByEJBName(relatedEntityName);
      if(relatedEntity == null) {
         throw new DeploymentException("Related entity not found: " +
               "entity=" + entity.getEntityName() + ", " +
               "cmrField=" + getFieldName() + ", " +
               "relatedEntity=" + relatedEntityName); 
      }

      // Related CMR Field
      List cmrFields = relatedEntity.getCMRFields();
      for(Iterator iter = cmrFields.iterator(); iter.hasNext();) { 
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)iter.next();
         if(metadata.getRelatedRole() == cmrField.getMetaData()) {
            relatedCMRField = cmrField;
            break;
         }
      }

      // if we didn't find the related CMR field throw an exception
      // with a detailed message
      if(relatedCMRField == null) {
         String message = "Related CMR field not found not found in " +
               relatedEntity.getEntityName() + " for relationship from";

         message += entity.getEntityName() + ".";
         if(getFieldName() != null) {
            message += getFieldName();
         } else {
            message += "<no-field>";
         }

         message += " to ";
         message += relatedEntityName + ".";
         if(metadata.getRelatedRole().getCMRFieldName() != null) {
            message += metadata.getRelatedRole().getCMRFieldName();
         } else {
            message += "<no-field>";
         }

         throw new DeploymentException(message);
      }

      // Related Manager
      relatedManager = relatedEntity.getManager();
      
      // Related Container
      EntityContainer theContainer = relatedManager.getContainer();
      relatedContainer = new WeakReference(theContainer);

      // 
      // Initialize the key fields
      //
      if(metadata.getRelationMetaData().isTableMappingStyle()) {
         // initialize relation table key fields
         Collection tableKeys = metadata.getKeyFields();
         tableKeyFields = new ArrayList(tableKeys.size());
         for(Iterator i=tableKeys.iterator(); i.hasNext(); ) {
            JDBCCMPFieldMetaData cmpFieldMetaData = 
                  (JDBCCMPFieldMetaData)i.next();
            tableKeyFields.add(
                  new JDBCCMP2xFieldBridge(manager, cmpFieldMetaData));
         }
         tableKeyFields = Collections.unmodifiableList(tableKeyFields);
      } else {      
         // initialize foreign key fields
         Collection foreignKeys = metadata.getRelatedRole().getKeyFields();
         foreignKeyFields = new ArrayList(foreignKeys.size());
         for(Iterator i=foreignKeys.iterator(); i.hasNext(); ) {
            JDBCCMPFieldMetaData cmpFieldMetaData = 
                  (JDBCCMPFieldMetaData)i.next();
            foreignKeyFields.add(new JDBCCMP2xFieldBridge(
                  manager,
                  cmpFieldMetaData,
                  manager.getJDBCTypeFactory().getJDBCType(
                     cmpFieldMetaData)));
         }
         foreignKeyFields = Collections.unmodifiableList(foreignKeyFields);
         hasForeignKey = !foreignKeyFields.isEmpty();
         if(hasForeignKey) {
            jdbcType = new CMRJDBCType(foreignKeyFields);
         }
      }
   }
      
   /**
    * Gets the manager of this entity.
    */
   public JDBCStoreManager getJDBCStoreManager() {
      return manager;
   }
   
   /**
    * Gets bridge for this entity.
    */
   public JDBCEntityBridge getEntity() {
      return entity;
   }
   
   /**
    * Gets the metadata of the relationship role that this field represents.
    */
   public JDBCRelationshipRoleMetaData getMetaData() {
      return metadata;
   }

   /**
    * Gets the relation metadata.
    */
   public JDBCRelationMetaData getRelationMetaData() {
      return metadata.getRelationMetaData();
   }

   /**
    * Gets the name of this field.
    */
   public String getFieldName() {
      return metadata.getCMRFieldName();
   }
   
   /**
    * Gets the name of the relation table if relevent.
    */
   public String getTableName() {
      return tableName;
   }
   
   /**
    * Gets the datasource of the relation table if relevent.
    */
   public DataSource getDataSource() {
      return dataSource;
   }

   /**
    * Gets the read ahead meta data.
    */
   public JDBCReadAheadMetaData getReadAhead() {
      return metadata.getReadAhead();
   }

   public JDBCType getJDBCType() {
      return jdbcType;
   }

   public boolean isPrimaryKeyMember() {
      return false;
   }

   /**
    * Does this cmr field have foreign keys.
    */
   public boolean hasForeignKey() {
      return hasForeignKey;
   }

   /**
    * Is this a collection valued field.
    */
   public boolean isCollectionValued() {
      return metadata.getRelatedRole().isMultiplicityMany();
   }
   
   /** 
    * Is this a single valued field.
    */
   public boolean isSingleValued() {
      return metadata.getRelatedRole().isMultiplicityOne();
   }
   
   /**
    * Gets the key fields that this entity maintains in the relation table.
    */
   public List getTableKeyFields() {
      return tableKeyFields;
   }
   
   /**
    * Gets the foreign key fields of this entity (i.e., related entities 
    * pk fields)
    */
   public List getForeignKeyFields() {
      return foreignKeyFields;
   }
   
   /**
    * The related entity's cmr field for this relationship.
    */
   public JDBCCMRFieldBridge getRelatedCMRField() {
      return relatedCMRField;
   }

   /**
    * The related manger.
    */
   public JDBCStoreManager getRelatedManager() {
      return relatedManager;
   }
   
   /**
    * The related entity.
    */
   public EntityBridge getRelatedEntity() {
      return relatedEntity;
   }
   
   /**
    * The related entity.
    */
   public JDBCEntityBridge getRelatedJDBCEntity() {
      return relatedEntity;
   }
   
   /**
    * The related container
    */
   private final EntityContainer getRelatedContainer() {
      return (EntityContainer) relatedContainer.get();
   }

   /**
    * The related entity's local home interface.
    */
   public final Class getRelatedLocalInterface() {
      return getRelatedContainer().getLocalClass();
   }
   
   /**
    * The related entity's local container invoker.
    */
   public final LocalProxyFactory getRelatedInvoker() {
      return getRelatedContainer().getLocalProxyFactory();
   }

   /**
    * Gets the EntityCache from the related entity.
    */
   public final EntityCache getRelatedCache() {
      return (EntityCache)getRelatedContainer().getInstanceCache();
   }

   public boolean isLoaded(EntityEnterpriseContext ctx) {
      return getFieldState(ctx).isLoaded;
   }
   
   /**
    * Is this field readonly?
    */
   public boolean isReadOnly() {
      return getRelationMetaData().isReadOnly();
   }

   /**
    * Had the read time expired?
    */
   public boolean isReadTimedOut(EntityEnterpriseContext ctx) {
      // if we are read/write then we are always timed out
      if(!isReadOnly()) {
         return true;
      }

      // if read-time-out is -1 then we never time out.
      if(getRelationMetaData().getReadTimeOut() == -1) {
         return false;
      }

      long readInterval = System.currentTimeMillis() - 
            getFieldState(ctx).getLastRead(); 
      return readInterval > getRelationMetaData().getReadTimeOut();
   }

   public Object getValue(EntityEnterpriseContext ctx) {
      // no user checks yet, but this is where they would go
      return getInstanceValue(ctx);
   }

   public void setValue(EntityEnterpriseContext ctx, Object value) {
      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " +
               "fieldName=" + getFieldName());
      }
      if(!entity.isCreated(ctx)) {
         throw new IllegalStateException("A CMR field cannot be set " +
               "in ejbCreate; this should be done in the ejbPostCreate " +
               "method instead [EJB 2.0 Spec. 10.5.2].");
      }
      if(isCollectionValued() && value == null) {
         throw new IllegalArgumentException("null cannot be assigned to a " +
               "collection-valued cmr-field [EJB 2.0 Spec. 10.3.8].");
      }
      
      setInstanceValue(ctx, value);      
   }

   /**
    * Gets the value of the cmr field for the instance associated with 
    * the context.
    */
   public Object getInstanceValue(EntityEnterpriseContext myCtx) {
      load(myCtx);

      FieldState fieldState = getFieldState(myCtx);
      if(isCollectionValued()) {
         return fieldState.getRelationSet();
      }
      
      // only return one      
      try {
         if(fieldState.getValue().size() > 0) {
            Object fk = fieldState.getValue().iterator().next();
            return getRelatedInvoker().getEntityEJBLocalObject(
                  getRelatedCache().createCacheKey(fk));
         }
         return null;
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException(e);
      }
   }
   
   /**
    * Sets the value of the cmr field for the instance associated with 
    * the context.
    */
   public void setInstanceValue(
         EntityEnterpriseContext myCtx, Object newValue) {

      load(myCtx);
      
      FieldState fieldState = getFieldState(myCtx);
      try{
         // Remove old value(s)
         List valuesCopy = new ArrayList(fieldState.getValue());
         Iterator relatedKeys = valuesCopy.iterator();
         while(relatedKeys.hasNext()) {
            destroyRelationLinks(myCtx, relatedKeys.next());
         }
         
         // Add new value(s)
         Collection c;
         if(newValue instanceof Collection) {
            c = (Collection) newValue;
         } else {
            c = new ArrayList();
            if(newValue != null) {
               c.add(newValue);
            }
         }

         Iterator newBeans = (new ArrayList(c)).iterator();
         while(newBeans.hasNext()) {
            EJBLocalObject newBean = (EJBLocalObject)newBeans.next();
            createRelationLinks(myCtx, newBean.getPrimaryKey());
         }
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException(e);
      }
   }

   /**
    * Creates the relation links between the instance associated with the 
    * context and the related instance (just the id is passed in).
    *
    * This method calls a.addRelation(b) and b.addRelation(a)
    */
   public void createRelationLinks(
         EntityEnterpriseContext myCtx, Object relatedId) {      

      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " + getFieldName());
      }

      // If my multiplicity is one, then we need to free the new related context
      // from its old relationship.
      if(metadata.isMultiplicityOne()) {
         Object oldRelatedId = relatedCMRField.invokeGetRelatedId(
               myCtx.getTransaction(), relatedId);
         if(oldRelatedId != null) {
            invokeRemoveRelation(
                  myCtx.getTransaction(), oldRelatedId, relatedId);
            relatedCMRField.invokeRemoveRelation(
                  myCtx.getTransaction(), relatedId, oldRelatedId);
         }
      }

      addRelation(myCtx, relatedId);
      relatedCMRField.invokeAddRelation(
            myCtx.getTransaction(), relatedId, myCtx.getId());
   }
   
   /**
    * Destroys the relation links between the instance associated with the 
    * context and the related instance (just the id is passed in).
    *
    * This method calls a.removeRelation(b) and b.removeRelation(a)
    */
   public void destroyRelationLinks(
         EntityEnterpriseContext myCtx, Object relatedId) {

      destroyRelationLinks(myCtx, relatedId, true);
   }
   
   /**
    * Destroys the relation links between the instance associated with the
    * context and the related instance (just the id is passed in).
    *
    * This method calls a.removeRelation(b) and b.removeRelation(a)
    *
    * If updateValueCollection is false, the related id collection is not
    * updated. This form is only used by the RelationSet iterator.
    */
   public void destroyRelationLinks(
         EntityEnterpriseContext myCtx,
         Object relatedId,
         boolean updateValueCollection) {

      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " + getFieldName());
      }

      removeRelation(myCtx, relatedId, updateValueCollection);      
      relatedCMRField.invokeRemoveRelation(
            myCtx.getTransaction(), relatedId, myCtx.getId());
   }
   
   /**
    * Invokes the getRelatedId on the related CMR field via the container
    * invocation interceptor chain.
    */
   private Object invokeGetRelatedId(Transaction tx, Object myId) {
      try {
         EntityInstanceCache instanceCache = 
               (EntityInstanceCache)manager.getContainer().getInstanceCache();

         return manager.getContainer().invoke(new Invocation(
               instanceCache.createCacheKey(myId),
               GET_RELATED_ID,
               new Object[] { this },
               tx, 
               SecurityAssociation.getPrincipal(),
               SecurityAssociation.getCredential()));
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Error in getRelatedId", e);
      }
   }      
      
   /**
    * Invokes the addRelation on the related CMR field via the container
    * invocation interceptor chain.
    */
   private void invokeAddRelation(
         Transaction tx, Object myId, Object relatedId) {

      try {
         EntityInstanceCache instanceCache = 
               (EntityInstanceCache)manager.getContainer().getInstanceCache();

         manager.getContainer().invoke(new Invocation(
               instanceCache.createCacheKey(myId),
               ADD_RELATION,
               new Object[] { this, relatedId },
               tx,
               SecurityAssociation.getPrincipal(),
               SecurityAssociation.getCredential()));
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Error in addRelation", e);
      }
   }

   /**
    * Invokes the removeRelation on the related CMR field via the container
    * invocation interceptor chain.
    */
   private void invokeRemoveRelation(
         Transaction tx, Object myId, Object relatedId) {

      try {
         EntityInstanceCache instanceCache = 
               (EntityInstanceCache)manager.getContainer().getInstanceCache();

         manager.getContainer().invoke(new Invocation(
               instanceCache.createCacheKey(myId),
               REMOVE_RELATION,
               new Object[] { this, relatedId },
               tx,
               SecurityAssociation.getPrincipal(),
               SecurityAssociation.getCredential()));
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Error in removeRelation", e);
      }
   }

   /**
    * Get the related entity's id.  This only works on single valued cmr fields.
    */
   public Object getRelatedId(EntityEnterpriseContext myCtx) {
      if(isCollectionValued()) {
         throw new EJBException("getRelatedId may only be called on a " +
               "cmr-field with a multiplicity of one.");
      }
      
      load(myCtx);

      FieldState fieldState = getFieldState(myCtx);
      if(fieldState.getValue().size() > 0) {
         return fieldState.getValue().iterator().next();
      }
      return null;
   }
   
   /**
    * Adds the foreign key to the set of related ids, and updates
    * any foreign key fields.
    */
   public void addRelation(EntityEnterpriseContext myCtx, Object fk) {
      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " + getFieldName());
      }

      if(!entity.isCreated(myCtx)) {
         throw new IllegalStateException("A CMR field cannot be set or added " +
               "to a relationship in ejbCreate; this should be done in the " +
               "ejbPostCreate method instead [EJB 2.0 Spec. 10.5.2].");
      }

      load(myCtx);
      
      FieldState fieldState = getFieldState(myCtx);
      
      // Check that single value relations are current not related to anything.
      if(isSingleValued() && fieldState.getValue().size() > 0) {
         throw new IllegalStateException("This bean may only be related to " +
               "one other bean at a time");
      }

      // must check to avoid dupes in the list
      if(fieldState.getValue().contains(fk)) {
         // we are already related to this entity
         return;
      }
      
      // add to current related set
      fieldState.getValue().add(fk);
      
      // set the foreign key, if we have one.
      if(hasForeignKey()) {
         setForeignKey(myCtx, fk);
      }
   }      
   
   /**
    * Removes the foreign key to the set of related ids, and updates
    * any foreign key fields.
    */
   public void removeRelation(EntityEnterpriseContext myCtx, Object fk) {
      removeRelation(myCtx, fk, true);
   }
   
   /**
    * Removes the foreign key to the set of related ids, and updates
    * any foreign key fields.
    *
    * If updateValueCollection is false, the related id collection is not
    * updated. This form is only used by the RelationSet iterator.
    */
   public void removeRelation(
         EntityEnterpriseContext myCtx,
         Object fk,
         boolean updateValueCollection) {

      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " + getFieldName());
      }

      load(myCtx);
      
      // remove from current related set
      if(updateValueCollection) {
         getFieldState(myCtx).getValue().remove(fk);
      }
      
      // set the foreign key to null, if we have one.
      if(hasForeignKey()) {
         setForeignKey(myCtx, null);
      }
   }      
   
   /**
    * loads the collection of related ids
    */
   private void load(EntityEnterpriseContext myCtx) {
      FieldState fieldState = getFieldState(myCtx);
      if(fieldState.isLoaded()) {
         return;
      }
      
      // check the preload cache
      if(log.isTraceEnabled()) {
         log.trace("Read ahead cahce load:"+
               " cmrField="+getFieldName()+
               " pk="+myCtx.getId());
      }
      manager.getReadAheadCache().load(myCtx);
      if(fieldState.isLoaded()) {
         return;
      }
      
      // load the value from the database
      Collection values = manager.loadRelation(this, myCtx.getId());

      // did we get more then one value for a single valued field
      if(isSingleValued() && values.size() > 1) {
         throw new EJBException("Preload data contains multiple values, but " +
               "this cmr field is single valued");
      }

      // just in the case where there are lingering values
      fieldState.getValue().clear();
      
      // add the new values
      fieldState.getValue().addAll(values);
      
      // set the foreign key, if we have one.
      if(hasForeignKey() && values.size()==1) {
         setForeignKey(myCtx, values.iterator().next());
      }

      // mark the field loaded
      fieldState.setLoaded(true);

      // we just loaded the results we are clean
      setClean(myCtx);
   }

   public void loadPreloadedValue(
         EntityEnterpriseContext myCtx,
         Collection values) {

      if(isSingleValued() && values.size() > 1) {
         throw new EJBException("Preload data contains multiple values, but " +
               "this cmr field is single valued");
      }

      FieldState fieldState = getFieldState(myCtx);

      // check if we are aleready loaded
      if(fieldState.isLoaded()) {
         throw new EJBException("CMR field value is already loaded");
      }

      // just in the case where there are lingering values
      fieldState.getValue().clear();
      
      // add the new values
      fieldState.getValue().addAll(values);
      
      // set the foreign key, if we have one.
      if(hasForeignKey() && values.size()==1) {
         setForeignKey(myCtx, values.iterator().next());
      }

      // mark the field loaded
      fieldState.setLoaded(true);
      
      if(log.isTraceEnabled()) {
         log.trace("Preloaded value: "+
               " cmrField="+getFieldName()+
               " pk="+myCtx.getId()+
               " values="+values);
      }
   }
   
   /**
    * Sets the foreign key field value.
    */
   private void setForeignKey(
         EntityEnterpriseContext myCtx, Object foreignKey) {

      if(!hasForeignKey()) {
         throw new EJBException(getFieldName() + " CMR field does not have " +
               "a foreign key to set.");
      }
      
      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         Object fieldValue = field.getPrimaryKeyValue(foreignKey);
         field.setInstanceValue(myCtx, fieldValue);
      }
   }

   /**
    * Initialized the foreign key fields.
    */
   public void initInstance(EntityEnterpriseContext ctx) {
      // mark this field as loaded
      getFieldState(ctx).setLoaded(true);

      if(!hasForeignKey()) {
         return;
      }

      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         field.initInstance(ctx);
      }
   }
   
   /**
    * resets the persistence context of the foreign key fields
    */
   public void resetPersistenceContext(EntityEnterpriseContext ctx) {
      // only reset if the read has timed out
      if(!isReadTimedOut(ctx)) {
         return;
      }

      // clear the field state
      setFieldState(ctx, null);
      
      if(!hasForeignKey()) {
         return;
      }

      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         field.resetPersistenceContext(ctx);
      }
   }

   public int setInstanceParameters(
         PreparedStatement ps, 
         int parameterIndex,
         EntityEnterpriseContext ctx) {

      if(!hasForeignKey()) {
         return parameterIndex;
      }

      Object fk = null;
      Collection c = getFieldState(ctx).getValue();
      if(c.size() > 0) {
         fk = c.iterator().next();
      }
      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         parameterIndex = field.setPrimaryKeyParameters(ps, parameterIndex, fk);
      }
      return parameterIndex;
   }

   public int loadInstanceResults(
         ResultSet rs,
         int parameterIndex,
         EntityEnterpriseContext ctx) {

      if(!hasForeignKey()) {
         return parameterIndex;
      }

      FieldState fieldState = getFieldState(ctx);

      // load the value from the database
      Object[] ref = new Object[1];
      parameterIndex = loadArgumentResults(rs, parameterIndex, ref);

      // just in the case where there are lingering values
      fieldState.getValue().clear();
      
      // add the new values
      if(ref[0] != null) {
         fieldState.getValue().add(ref[0]);
      }
      
      // set the foreign key, if we have one.
      setForeignKey(ctx, ref[0]);

      // mark the field loaded
      fieldState.setLoaded(true);

      return parameterIndex;
   }

   public int loadArgumentResults(
         ResultSet rs,
         int parameterIndex,
         Object[] fkRef) {

      if(!hasForeignKey()) {
         return parameterIndex;
      }

      // value of this field,  will be filled in below
      Object[] argumentRef = new Object[1];
      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         parameterIndex = field.loadArgumentResults(
               rs,
               parameterIndex,
               argumentRef);
      
         if(field.getPrimaryKeyField() != null) {
            // if we are tring to set a null value 
            // into a null pk, we are already done.
            if(argumentRef[0] != null || fkRef[0] != null) {
            
               // if we don't have a pk object yet create one
               if(fkRef[0] == null) {
                  fkRef[0] = relatedEntity.createPrimaryKeyInstance();
               }
            
               try {
                  // Set this field's value into the primary key object.
                  field.getPrimaryKeyField().set(
                        fkRef[0],
                        argumentRef[0]);
               } catch(Exception e) {
                  // Non recoverable internal exception
                  throw new EJBException("Internal error setting foreign-key " +
                        "field " + getFieldName(), e);
               }
            }
         } else {
            // This field is the primary key, so no extraction is necessary.
            fkRef[0] = argumentRef[0];
         }
      }
      return parameterIndex;
   }

   public boolean isDirty(EntityEnterpriseContext ctx) {
      if(!hasForeignKey()) {
         return false;
      }

      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         if(field.isDirty(ctx)) {
            return true;
         }
      }
      return false;
   }

   public void setClean(EntityEnterpriseContext ctx) {
      if(!hasForeignKey()) {
         return;
      }

      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         field.setClean(ctx);
      }
   }
   
   /**
    * Gets the field state object from the persistence context.
    */
   private FieldState getFieldState(EntityEnterpriseContext ctx) {
      JDBCContext jdbcCtx = (JDBCContext)ctx.getPersistenceContext();
      FieldState fieldState = (FieldState)jdbcCtx.get(this);
      if(fieldState == null) {
         fieldState = new FieldState(ctx);
         jdbcCtx.put(this, fieldState);
      }
      return fieldState;
   }

   /**
    * Sets the field state object in the persistence context.
    */
   private void setFieldState(
         EntityEnterpriseContext ctx, FieldState fieldState) {

      JDBCContext jdbcCtx = (JDBCContext)ctx.getPersistenceContext();
      
      // invalidate current field state
      FieldState currentFieldState = (FieldState)jdbcCtx.get(this);
      if(currentFieldState != null) {
         currentFieldState.invalidate();
      }
      
      if(fieldState == null) {
         jdbcCtx.remove(this);
      } else {
         jdbcCtx.put(this, fieldState);
      }
   }

   private class FieldState {
      private EntityEnterpriseContext ctx;
      private List[] setHandle = new List[1];
      private Set relationSet;
      private boolean isLoaded = false;
      private long lastRead = -1;

      public FieldState(EntityEnterpriseContext ctx) {
         this.ctx = ctx;
         setHandle[0] = new ArrayList();
      }
      public List getValue() {
         return setHandle[0];
      }
      public Set getRelationSet() {
         if(relationSet == null) {
            relationSet = new RelationSet(
                  JDBCCMRFieldBridge.this, ctx, setHandle);
         }
         return relationSet;
      }
      public boolean isLoaded() {
         return isLoaded;
      }
      public void setLoaded(boolean isLoaded) {
         this.isLoaded = isLoaded;
      }
      public long getLastRead() {
         return lastRead;
      }
      public void invalidate() {
         setHandle[0] = null;
         setHandle = null;
         relationSet = null;
      }
   }   
   private class CMRJDBCType implements JDBCType {
      private final String[] columnNames;
      private final Class[] javaTypes;
      private final int[] jdbcTypes;
      private final String[] sqlTypes;
      private final boolean[] notNull;
      
      private CMRJDBCType(List fields) {
         List columnNamesList = new ArrayList();
         List javaTypesList = new ArrayList();
         List jdbcTypesList = new ArrayList();
         List sqlTypesList = new ArrayList();
         List notNullList = new ArrayList(); 

         for(Iterator iter = fields.iterator(); iter.hasNext(); ) {
            JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)iter.next();
            JDBCType type = field.getJDBCType();
            for(int i=0; i<type.getColumnNames().length; i++) {
               columnNamesList.add(type.getColumnNames()[i]);
               javaTypesList.add(type.getJavaTypes()[i]);
               jdbcTypesList.add(new Integer(type.getJDBCTypes()[i]));
               sqlTypesList.add(type.getSQLTypes()[i]);
               notNullList.add(new Boolean(type.getNotNull()[i]));
            }
         }
         columnNames = (String[])columnNamesList.toArray(
               new String[columnNamesList.size()]);
         javaTypes = (Class[])javaTypesList.toArray(
               new Class[javaTypesList.size()]);
         sqlTypes = (String[])sqlTypesList.toArray(
               new String[sqlTypesList.size()]);

         jdbcTypes = new int[jdbcTypesList.size()];
         for(int i=0; i<jdbcTypes.length; i++) {
            jdbcTypes[i] = ((Integer)jdbcTypesList.get(i)).intValue();
         }

         notNull = new boolean[notNullList.size()];
         for(int i=0; i<notNull.length; i++) {
            notNull[i] = ((Boolean)notNullList.get(i)).booleanValue();
         }
      }   
      public String[] getColumnNames() {
         return columnNames;
      }
      public Class[] getJavaTypes() {
         return javaTypes;
      }
      public int[] getJDBCTypes() {
         return jdbcTypes;
      }
      public String[] getSQLTypes() {
         return sqlTypes;
      }
      public boolean[] getNotNull() {
         return notNull;
      }
      public Object getColumnValue(int index, Object value) {
         throw new UnsupportedOperationException();
      }
      public Object setColumnValue(
            int index, Object value, Object columnValue) {
         throw new UnsupportedOperationException();
      }
   }
}
