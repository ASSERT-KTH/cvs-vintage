/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.ref.WeakReference;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.LocalProxyFactory;
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
import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.tm.TxUtils;

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
 * @version $Revision: 1.64 $
 */
public class JDBCCMRFieldBridge implements JDBCFieldBridge, CMRFieldBridge {
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
    *  Indicates whether all FK fields are mapped to PK fields
    */
   private boolean allFkFieldsMappedToPkFields;

   /**
    *  This map contains related PK fields that are mapped through FK fields to this entity's PK fields
    */
   private Map relatedPkFieldsByPkFields = new HashMap();

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

      //
      // Set handles to the related entity's container,
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
         String message = "Related CMR field not found in " +
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

      // Data Source
      if(metadata.getRelationMetaData().isTableMappingStyle())
         dataSource = metadata.getRelationMetaData().getDataSource();
      else
         dataSource = hasForeignKey() ? entity.getDataSource() : relatedEntity.getDataSource();

      // Fix table name
      //
      // This code doesn't work here...  The problem each side will generate
      // the table name and this will only work for simple generation.
      tableName = SQLUtil.fixTableName(
         metadata.getRelationMetaData().getDefaultTableName(), dataSource
      );

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

         // FKs mapped to PKs
         Map fkFieldsByPkFields = new HashMap();
         for(Iterator i=foreignKeys.iterator(); i.hasNext(); ) {

            JDBCCMPFieldMetaData fkFieldMetaData =
                  (JDBCCMPFieldMetaData)i.next();

            // now determine whether the fk is a part of the pk.
            // fk is a part of pk if its fields are mapped to
            // the primary key columns
            String fkColumnName = fkFieldMetaData.getColumnName();

            JDBCCMP2xFieldBridge fkField = null;
            // look among the pk fields for the field with matching column name
            for(Iterator pkIter = entity.getPrimaryKeyFields().iterator();
               pkIter.hasNext() && fkField == null;) {

               JDBCCMP2xFieldBridge pkField = (JDBCCMP2xFieldBridge)pkIter.next();
               JDBCCMPFieldMetaData pkFieldMetaData =
                  entity.getMetaData().getCMPFieldByName(pkField.getFieldName());

               if(fkColumnName.equals(pkFieldMetaData.getColumnName())) {
                  JDBCCMP2xFieldBridge relatedPkField =
                     (JDBCCMP2xFieldBridge)relatedEntity.
                        getFieldByName(fkFieldMetaData.getFieldName());

                  // construct the foreign key field mapped to a primary key field
                  fkField = new JDBCCMP2xFieldBridge(
                     pkField.getManager(),               // this pk's manager
                     relatedPkField.getFieldName(),
                     relatedPkField.getFieldType(),
                     pkField.getJDBCType(),              // this pk's jdbc type
                     relatedPkField.isReadOnly(),
                     relatedPkField.getReadTimeOut(),
                     false,                              // not a primary key
                     relatedPkField.getPrimaryKeyClass(),
                     relatedPkField.getPrimaryKeyField(),
                     false                               // is not an unknown key
                  );

                  fkFieldsByPkFields.put(pkField, fkField);
                  relatedPkFieldsByPkFields.put(pkField, relatedPkField);
               }
            }

            // if the fk is not a part of the pk then create a new field
            if(fkField == null) {
               fkField = new JDBCCMP2xFieldBridge(
                  manager,
                  fkFieldMetaData,
                  manager.getJDBCTypeFactory().getJDBCType(fkFieldMetaData)
               );
               foreignKeyFields.add(0, fkField);
            }
         }

         // add FK fields mapped to PK fields in the same order as PK fields
         // this does matter when using joining
         if(fkFieldsByPkFields.size() > 0) {
            for(Iterator iter = entity.getPrimaryKeyFields().iterator(); iter.hasNext();) {
               Object fkField = fkFieldsByPkFields.get(iter.next());
               if(fkField != null)
                  foreignKeyFields.add(fkField);
            }
         }

         // are all FK fields mapped to PK fields?
         allFkFieldsMappedToPkFields = relatedPkFieldsByPkFields.size() > 0
            && relatedPkFieldsByPkFields.size() == foreignKeyFields.size();

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
    * Returns true if all FK fields are mapped to PK fields
    */
   public boolean allFkFieldsMappedToPkFields() {
      return allFkFieldsMappedToPkFields;
   }

   public boolean hasFkFieldsMappedToPkFields() {
      return relatedPkFieldsByPkFields.size() > 0;
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
      if(allFkFieldsMappedToPkFields) {
         throw new IllegalStateException(
            "Can't modify relationship: CMR field "
            + entity.getEntityName() + "." + getFieldName()
            + " has foreign key fields mapped to the primary key columns."
            + " Primary key may only be set once in ejbCreate [EJB 2.0 Spec. 10.3.5].");
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
         Collection c = fieldState.getValue();
         if(!c.isEmpty()) {
            Object fk = c.iterator().next();
            return getRelatedInvoker().getEntityEJBLocalObject(fk);
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
   public void setInstanceValue(EntityEnterpriseContext myCtx, Object newValue) {
      load(myCtx);
      FieldState fieldState = getFieldState(myCtx);

      // is this just setting our own relation set back
      if(newValue == fieldState.getRelationSet()) {
         return;
      }

      Collection valueCopy;
      if(newValue instanceof Collection) {
         valueCopy = new ArrayList((Collection) newValue);
      } else {
         valueCopy = new ArrayList(1);
         if(newValue != null) {
            valueCopy.add(newValue);
         }
      }
      Iterator newBeans = valueCopy.iterator();
      // list of new pk values. just not to fetch them twice
      // if there there are FK fields mapped to PK fields
      List newPkValues = null;

      // check whether new value modifies the primary key if there are FK fields
      // mapped to PK fields
      if(relatedPkFieldsByPkFields.size() > 0) {
         newPkValues = new ArrayList();
         while(newBeans.hasNext()) {
            EJBLocalObject ejbObject = (EJBLocalObject)newBeans.next();
            if(ejbObject == null)
               continue;
            Object pkObject = ejbObject.getPrimaryKey();
            checkSetForeignKey(myCtx, pkObject);
            newPkValues.add(pkObject);
         }
      }

      try{
         // Remove old value(s)
         List valuesCopy = new ArrayList(fieldState.getValue());
         Iterator relatedKeys = valuesCopy.iterator();
         while(relatedKeys.hasNext()) {
            destroyRelationLinks(myCtx, relatedKeys.next());
         }

         // Add new value(s)
         if(newPkValues != null) {
            for(Iterator iter = newPkValues.iterator(); iter.hasNext();) {
               createRelationLinks(myCtx, iter.next());
            }
         } else {
            while(newBeans.hasNext()) {
               EJBLocalObject newBean = (EJBLocalObject)newBeans.next();
               createRelationLinks(myCtx, newBean.getPrimaryKey());
            }
         }
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException(e);
      }
   }

   /**
    * Throws IllegalStateException if new foreign key value will change
    * the primary key value, otherwise returns silently.
    */
   private void checkSetForeignKey(EntityEnterpriseContext myCtx, Object newValue)
      throws IllegalStateException {
      for(Iterator pkFields=entity.getPrimaryKeyFields().iterator(); pkFields.hasNext();) {
         JDBCCMP2xFieldBridge pkField = (JDBCCMP2xFieldBridge)pkFields.next();
         JDBCCMP2xFieldBridge relatedPkField = (JDBCCMP2xFieldBridge)relatedPkFieldsByPkFields.get(pkField);
         if(relatedPkField != null) {
            Object comingValue = relatedPkField.getPrimaryKeyValue(newValue);
            Object currentValue = pkField.getInstanceValue(myCtx);

            // they shouldn't be null, should they?
            if(!comingValue.equals(currentValue)) {
               throw new IllegalStateException(
                  "Can't create relationship: CMR field "
                  + entity.getEntityName() + "." + getFieldName()
                  + " has foreign key fields mapped to the primary key columns."
                  + " Primary key may only be set once in ejbCreate [EJB 2.0 Spec. 10.3.5].");
            }
         }
      }
   }

   /**
    * Creates the relation links between the instance associated with the
    * context and the related instance (just the id is passed in).
    *
    * This method calls a.addRelation(b) and b.addRelation(a)
    */
   public void createRelationLinks(EntityEnterpriseContext myCtx, Object relatedId) {
      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " + getFieldName());
      }

      // If my multiplicity is one, then we need to free the new related context
      // from its old relationship.
      if(metadata.isMultiplicityOne()) {
         Object oldRelatedId = relatedCMRField.invokeGetRelatedId(
               getTransaction(), relatedId);
         if(oldRelatedId != null) {
            invokeRemoveRelation(
                  getTransaction(), oldRelatedId, relatedId);
            relatedCMRField.invokeRemoveRelation(
                  getTransaction(), relatedId, oldRelatedId);
         }
      }

      addRelation(myCtx, relatedId);
      relatedCMRField.invokeAddRelation(
            getTransaction(), relatedId, myCtx.getId());
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
            getTransaction(), relatedId, myCtx.getId());
   }

   /**
    * Invokes the getRelatedId on the related CMR field via the container
    * invocation interceptor chain.
    */
   private Object invokeGetRelatedId(Transaction tx, Object myId) {
      Thread thread = Thread.currentThread();
      ClassLoader oldCL = thread.getContextClassLoader();
      thread.setContextClassLoader(manager.getContainer().getClassLoader());

      try {
         Invocation invocation = new Invocation();
         invocation.setValue(CMRMessage.CMR_MESSAGE_KEY,
               CMRMessage.GET_RELATED_ID, PayloadKey.AS_IS);
         invocation.setValue(Entrancy.ENTRANCY_KEY,
               Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
         invocation.setId(myId);
         invocation.setArguments(new Object[] { this });
         invocation.setTransaction(tx);
         invocation.setPrincipal(SecurityAssociation.getPrincipal());
         invocation.setCredential(SecurityAssociation.getCredential());
         invocation.setType(InvocationType.LOCAL);
         return manager.getContainer().invoke(invocation).getResponse();
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Error in getRelatedId", e);
      }
      finally
      {
         thread.setContextClassLoader(oldCL);
      }
   }

   /**
    * Invokes the addRelation on the related CMR field via the container
    * invocation interceptor chain.
    */
   private void invokeAddRelation(Transaction tx, Object myId, Object relatedId) {
      Thread thread = Thread.currentThread();
      ClassLoader oldCL = thread.getContextClassLoader();
      thread.setContextClassLoader(manager.getContainer().getClassLoader());

      try {
         Invocation invocation = new Invocation();
         invocation.setValue(CMRMessage.CMR_MESSAGE_KEY,
               CMRMessage.ADD_RELATION, PayloadKey.AS_IS);
         invocation.setValue(Entrancy.ENTRANCY_KEY,
               Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
         invocation.setId(myId);
         invocation.setArguments(new Object[] { this, relatedId });
         invocation.setTransaction(tx);
         invocation.setPrincipal(SecurityAssociation.getPrincipal());
         invocation.setCredential(SecurityAssociation.getCredential());
         invocation.setType(InvocationType.LOCAL);
         manager.getContainer().invoke(invocation);
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
   private void invokeRemoveRelation(Transaction tx, Object myId, Object relatedId) {
      Thread thread = Thread.currentThread();
      ClassLoader oldCL = thread.getContextClassLoader();
      thread.setContextClassLoader(manager.getContainer().getClassLoader());

      try {
         Invocation invocation = new Invocation();
         invocation.setValue(CMRMessage.CMR_MESSAGE_KEY,
               CMRMessage.REMOVE_RELATION, PayloadKey.AS_IS);
         invocation.setValue(Entrancy.ENTRANCY_KEY,
               Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
         invocation.setId(myId);
         invocation.setArguments(new Object[] { this, relatedId });
         invocation.setTransaction(tx);
         invocation.setPrincipal(SecurityAssociation.getPrincipal());
         invocation.setCredential(SecurityAssociation.getCredential());
         invocation.setType(InvocationType.LOCAL);
         manager.getContainer().invoke(invocation);
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Error in removeRelation", e);
      }
      finally
      {
         thread.setContextClassLoader(oldCL);
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

      Collection c = getFieldState(myCtx).getValue();
      if(!c.isEmpty()) {
         return c.iterator().next();
      }
      return null;
   }

   /**
    * Adds the foreign key to the set of related ids, and updates
    * any foreign key fields.
    */
   public void addRelation(EntityEnterpriseContext myCtx, Object fk) {
      checkSetForeignKey(myCtx, fk);

      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " + getFieldName());
      }

      if(!entity.isCreated(myCtx)) {

         // substitute with primary key change check
         //if(relatedCMRField.isFkPartOfPk())
         //   return;

         throw new IllegalStateException("A CMR field cannot be set or added " +
               "to a relationship in ejbCreate; this should be done in the " +
               "ejbPostCreate method instead [EJB 2.0 Spec. 10.5.2].");
      }

      // add to current related set
      FieldState myState = getFieldState(myCtx);
      myState.addRelation(fk);

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

      // remove from current related set
      if(updateValueCollection) {
         FieldState myState = getFieldState(myCtx);
         myState.removeRelation(fk);
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
      // if we are already loaded we're done
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
      manager.getPrefetchCache().loadPrefetchData(myCtx);
      if(fieldState.isLoaded()) {
         return;
      }

      // load the value from the database
      load(myCtx, manager.loadRelation(this, myCtx));

      // we just loaded the results so we are clean
      setClean(myCtx);
   }

   public void load(
         EntityEnterpriseContext myCtx,
         Collection values) {

      // did we get more then one value for a single valued field
      if(isSingleValued() && values.size() > 1) {
         throw new EJBException("Data contains multiple values, but " +
               "this cmr field is single valued");
      }

      // add the new values
      getFieldState(myCtx).loadRelations(values);

      // set the foreign key, if we have one.
      if(hasForeignKey()) {
         if(values.isEmpty()) {
            setForeignKey(myCtx, null);
         } else {
            setForeignKey(myCtx, values.iterator().next());
         }
      }
   }

   /**
    * Sets the foreign key field value.
    */
   private void setForeignKey(EntityEnterpriseContext myCtx, Object foreignKey) {
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
      getFieldState(ctx).loadRelations(Collections.EMPTY_SET);

      if(!hasForeignKey()) {
         return;
      }

      for(Iterator fields = foreignKeyFields.iterator(); fields.hasNext();) {
         JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)fields.next();
         field.initInstance(ctx);
      }
   }

   /**
    * resets the persistence context
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
      if(!c.isEmpty()) {
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

      // load the value from the database
      Object[] ref = new Object[1];
      parameterIndex = loadArgumentResults(rs, parameterIndex, ref);

      // only actually set the value if the state is not already loaded
      FieldState fieldState = getFieldState(ctx);
      if(!fieldState.isLoaded()) {
         if(ref[0] != null) {
            load(ctx, Collections.singleton(ref[0]));
         } else {
            load(ctx, Collections.EMPTY_SET);
         }
      }
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

            // if there is a null field among FK fields,
            // the whole FK field is considered null.
            // NOTE: don't throw exception in this case,
            // it's ok if FK is partly mapped to a PK
            if(argumentRef[0] == null) {
               fkRef[0] = null;
               break;
            }

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
      return getFieldState(ctx).isDirty();
   }

   public void setClean(EntityEnterpriseContext ctx) {
      if(!hasForeignKey()) {
         return;
      }

      for(Iterator iter = foreignKeyFields.iterator(); iter.hasNext();) {
         ((JDBCCMP2xFieldBridge)iter.next()).setClean(ctx);
      }
      getFieldState(ctx).setClean();
   }

   /**
    * Returns dirty foreign key fields
    */
   public List getDirtyForeignKeyFields(EntityEnterpriseContext ctx) {
      if(!hasForeignKey)
         return Collections.EMPTY_LIST;
      List dirtyFields = new ArrayList();
      for(Iterator iter = this.foreignKeyFields.iterator(); iter.hasNext();) {
         JDBCCMP2xFieldBridge fkField = (JDBCCMP2xFieldBridge)iter.next();
         if(fkField.isDirty(ctx))
            dirtyFields.add(fkField);
      }
      return dirtyFields;
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

   private Transaction getTransaction()
   {
      try
      {
         EntityContainer container = getJDBCStoreManager().getContainer();
         TransactionManager tm = container.getTransactionManager();
         return tm.getTransaction();
      }
      catch(SystemException e)
      {
         throw new EJBException(
               "Error getting transaction from the transaction manager", e);
      }
   }

   private final class FieldState {
      private EntityEnterpriseContext ctx;
      private List[] setHandle = new List[1];
      private Set addedRelations = new HashSet();
      private Set removedRelations = new HashSet();
      private Set relationSet;
      private boolean isLoaded = false;
      private long lastRead = -1;
      private boolean dirty = false;

      public FieldState(EntityEnterpriseContext ctx) {
         this.ctx = ctx;
         setHandle[0] = new ArrayList();
      }

      /**
       * Returns the dirty state
       */
      public boolean isDirty() {
         return dirty;
      }

      /**
       * Marks the CMR field as dirty
       */
      public void setDirty() {
         this.dirty = true;
      }

      /**
       * Makrs the CMR field as clean
       */
      public void setClean() {
         this.dirty = false;
      }

      /**
       * Get the current value (list of primary keys).
       */
      public List getValue() {
         if(!isLoaded) {
            throw new EJBException("CMR field value not loaded yet");
         }
         return Collections.unmodifiableList(setHandle[0]);
      }

      /**
       * Has this relation been loaded.
       */
      public boolean isLoaded() {
         return isLoaded;
      }

      /**
       * When was this value last read from the datastore.
       */
      public long getLastRead() {
         return lastRead;
      }

      /**
       * Add this foreign to the relationship.
       */
      public void addRelation(Object fk) {
         boolean modified = false;
         if(isLoaded) {
            modified = setHandle[0].add(fk);
         } else {
            modified = removedRelations.remove(fk)
               || addedRelations.add(fk);
         }

         if(modified) setDirty();
      }

      /**
       * Remove this foreign to the relationship.
       */
      public void removeRelation(Object fk) {

         boolean modified = false;
         if(isLoaded) {
            modified = setHandle[0].remove(fk);
         } else {
            modified = addedRelations.remove(fk)
               || removedRelations.add(fk);
         }

         if(modified) setDirty();
      }

      /**
       * loads the collection of related ids
       */
      public void loadRelations(Collection values) {

         // check if we are aleready loaded
         if(isLoaded) {
            throw new EJBException("CMR field value is already loaded");
         }

         // just in the case where there are lingering values
         setHandle[0].clear();

         // add the new values
         setHandle[0].addAll(values);

         // remove the already removed values
         setHandle[0].removeAll(removedRelations);

         // add the already added values
         // but remove FKs we are going to add to avoid duplication
         setHandle[0].removeAll(addedRelations);
         setHandle[0].addAll(addedRelations);

         // mark the field loaded
         isLoaded = true;
      }

      /**
       * Get the current relation set or create a new one.
       */
      public Set getRelationSet() {
         if(!isLoaded) {
            throw new EJBException("CMR field value not loaded yet");
         }

         if(relationSet == null) {
            try {
               // get the curent transaction
               EntityContainer container = getJDBCStoreManager().getContainer();
               TransactionManager tm = container.getTransactionManager();
               Transaction tx = tm.getTransaction();

               // if whe have a valid transaction...
               if(TxUtils.isActive(tx))
               {

                  // create the relation set and register for a tx callback
                  relationSet = new RelationSet(
                     JDBCCMRFieldBridge.this, ctx, setHandle);
                  TxSynchronization sync =
                        new TxSynchronization(FieldState.this);
                  tx.registerSynchronization(sync);

               } else {

                  // if there is no transaction create a pre-failed list
                  relationSet = new RelationSet(
                     JDBCCMRFieldBridge.this, ctx, new List[1]);
               }
            } catch(SystemException e) {
               throw new EJBException("Error while creating RelationSet", e);
            } catch(RollbackException e) {
               throw new EJBException("Error while creating RelationSet", e);
            }
         }

         return relationSet;
      }

      /**
       * Invalidate the current relationship set.
       */
      public void invalidate() {
         // make a new set handle and copy the currentList to the new handle
         // this will cause old references to the relationSet to throw an
         // IllegalStateException if accesses, but will not cause a reload
         // in Commit Option A
         List currentList = null;
         if(setHandle != null && setHandle.length > 0)
         {
            currentList = setHandle[0];
            setHandle[0] = null;
         }
         setHandle = new List[1];
         setHandle[0] = currentList;

         relationSet = null;
      }
   }
   private final static class CMRJDBCType implements JDBCType {
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
      public boolean[] getAutoIncrement() {
         return new boolean[] {false};
      }
      public Object getColumnValue(int index, Object value) {
         throw new UnsupportedOperationException();
      }
      public Object setColumnValue(
            int index, Object value, Object columnValue) {
         throw new UnsupportedOperationException();
      }
   }
   private final static class TxSynchronization implements Synchronization
   {
      private final WeakReference fieldStateRef;

      private TxSynchronization(FieldState fieldState)
      {
         if(fieldState == null)
         {
            throw new IllegalArgumentException("fieldState is null");
         }
         this.fieldStateRef = new WeakReference(fieldState);
      }

      public void beforeCompletion()
      {
         // Be Careful where you put this invalidate
         // If you put it in afterCompletion, the beanlock will probably
         // be released before the invalidate and you will have a race
         FieldState fieldState = (FieldState)fieldStateRef.get();
         if(fieldState != null) {
            fieldState.invalidate();
         }
      }

      public void afterCompletion(int status)
      {
      }
   }
}
