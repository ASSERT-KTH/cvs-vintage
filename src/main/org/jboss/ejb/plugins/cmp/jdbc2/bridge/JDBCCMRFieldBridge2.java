/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.bridge;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.plugins.cmp.bridge.FieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc2.JDBCStoreManager2;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.EntityTable;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.RelationTable;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Cache;
import org.jboss.ejb.plugins.cmp.jdbc2.PersistentContext;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRInvocation;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRMessage;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.invocation.InvocationType;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.ConcurrentModificationException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Array;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Principal;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class JDBCCMRFieldBridge2
   extends JDBCAbstractCMRFieldBridge
{
   private final JDBCRelationshipRoleMetaData metadata;
   private final JDBCStoreManager2 manager;
   private final JDBCEntityBridge2 entity;
   private final int cmrIndex;
   private final Logger log;

   private JDBCEntityBridge2 relatedEntity;
   private JDBCCMRFieldBridge2 relatedCMRField;
   private EntityContainer relatedContainer;

   private JDBCCMPFieldBridge2[] tableKeyFields;
   private JDBCCMPFieldBridge2[] foreignKeyFields;
   private JDBCCMPFieldBridge2[] relatedPKFields;

   private CMRFieldLoader loader;
   private RelationTable relationTable;

   private final TransactionManager tm;

   public JDBCCMRFieldBridge2(JDBCEntityBridge2 entityBridge,
                              JDBCStoreManager2 manager,
                              JDBCRelationshipRoleMetaData metadata)
   {
      this.manager = manager;
      this.entity = entityBridge;
      this.metadata = metadata;
      cmrIndex = entity.getNextCMRIndex();
      tm = manager.getContainer().getTransactionManager();

      log = Logger.getLogger(getClass().getName() + "." + entity.getEntityName() + "#" + getFieldName());
   }

   // Public

   public void resolveRelationship() throws DeploymentException
   {
      //
      // Set handles to the related entity's container, cache, manager, and invoker
      //

      // Related Entity Name
      String relatedEntityName = metadata.getRelatedRole().getEntity().getName();

      // Related Entity
      Catalog catalog = (Catalog) manager.getApplicationData("CATALOG");
      relatedEntity = (JDBCEntityBridge2) catalog.getEntityByEJBName(relatedEntityName);
      if(relatedEntity == null)
      {
         throw new DeploymentException("Related entity not found: "
            +
            "entity="
            +
            entity.getEntityName()
            +
            ", "
            +
            "cmrField="
            +
            getFieldName()
            +
            ", " +
            "relatedEntity=" + relatedEntityName);
      }

      // Related CMR Field
      JDBCCMRFieldBridge2[] cmrFields = (JDBCCMRFieldBridge2[]) relatedEntity.getCMRFields();
      for(int i = 0; i < cmrFields.length; ++i)
      {
         JDBCCMRFieldBridge2 cmrField = cmrFields[i];
         if(metadata.getRelatedRole() == cmrField.getMetaData())
         {
            relatedCMRField = cmrField;
            break;
         }
      }

      // if we didn't find the related CMR field throw an exception with a detailed message
      if(relatedCMRField == null)
      {
         String message = "Related CMR field not found in " +
            relatedEntity.getEntityName() + " for relationship from ";

         message += entity.getEntityName() + ".";
         if(getFieldName() != null)
         {
            message += getFieldName();
         }
         else
         {
            message += "<no-field>";
         }

         message += " to ";
         message += relatedEntityName + ".";
         if(metadata.getRelatedRole().getCMRFieldName() != null)
         {
            message += metadata.getRelatedRole().getCMRFieldName();
         }
         else
         {
            message += "<no-field>";
         }

         throw new DeploymentException(message);
      }

      // Related Container
      relatedContainer = relatedEntity.getContainer();

      //
      // Initialize the key fields
      //
      if(metadata.getRelationMetaData().isTableMappingStyle())
      {
         // initialize relation table key fields
         Collection tableKeys = metadata.getKeyFields();
         List keyFieldsList = new ArrayList(tableKeys.size());

         // first phase is to create fk fields
         Map pkFieldsToFKFields = new HashMap(tableKeys.size());
         for(Iterator i = tableKeys.iterator(); i.hasNext();)
         {
            JDBCCMPFieldMetaData cmpFieldMetaData = (JDBCCMPFieldMetaData) i.next();
            FieldBridge pkField = entity.getFieldByName(cmpFieldMetaData.getFieldName());
            if(pkField == null)
            {
               throw new DeploymentException("Primary key not found for key-field " + cmpFieldMetaData.getFieldName());
            }
            pkFieldsToFKFields.put(pkField, new JDBCCMPFieldBridge2(manager, entity, cmpFieldMetaData, -1));
         }

         // second step is to order fk fields to match the order of pk fields
         JDBCFieldBridge[] pkFields = entity.getPrimaryKeyFields();
         for(int i = 0; i < pkFields.length; ++i)
         {
            Object fkField = pkFieldsToFKFields.get(pkFields[i]);
            if(fkField == null)
            {
               throw new DeploymentException("Primary key " + pkFields[i].getFieldName() + " is not mapped.");
            }
            keyFieldsList.add(fkField);
         }
         tableKeyFields = (JDBCCMPFieldBridge2[]) keyFieldsList.toArray(new JDBCCMPFieldBridge2[keyFieldsList.size()]);
      }
      else
      {
         initializeForeignKeyFields();
      }
   }

   public void initLoader() throws DeploymentException
   {
      if(metadata.getRelationMetaData().isTableMappingStyle())
      {
         relationTable = relatedCMRField.getRelationTable();
         loader = new RelationTableLoader();
      }
      else
      {
         if(foreignKeyFields != null)
         {
            loader = new ContextForeignKeyLoader();
         }
         else
         {
            loader = new ForeignKeyLoader();
         }
      }
   }

   public JDBCRelationshipRoleMetaData getMetaData()
   {
      return metadata;
   }

   public boolean removeRelatedId(EntityEnterpriseContext ctx, Object relatedId)
   {
      FieldState state = getFieldState(ctx);
      return state.removeRelatedId(ctx, relatedId);
   }

   public boolean addRelatedId(EntityEnterpriseContext ctx, Object relatedId)
   {
      FieldState state = getFieldState(ctx);
      return state.addRelatedId(ctx, relatedId);
   }

   public void remove(EntityEnterpriseContext ctx) throws RemoveException
   {
      if(metadata.getRelatedRole().isCascadeDelete())
      {
         FieldState state = getFieldState(ctx);
         state.cascadeDelete(ctx);
      }
      else
      {
         destroyExistingRelationships(ctx);
      }
   }

   public void destroyExistingRelationships(EntityEnterpriseContext ctx)
   {
      FieldState state = getFieldState(ctx);
      state.destroyExistingRelationships(ctx);
   }

   public JDBCFieldBridge[] getTableKeyFields()
   {
      return tableKeyFields;
   }

   public JDBCEntityPersistenceStore getManager()
   {
      return manager;
   }

   public boolean hasForeignKey()
   {
      return foreignKeyFields != null;
   }

   public JDBCAbstractCMRFieldBridge getRelatedCMRField()
   {
      return this.relatedCMRField;
   }

   public JDBCFieldBridge[] getForeignKeyFields()
   {
      return foreignKeyFields;
   }

   public JDBCCMRFieldBridge2 getRelatedField()
   {
      return relatedCMRField;
   }

   public JDBCAbstractEntityBridge getEntity()
   {
      return entity;
   }

   public String getTableName()
   {
      return relationTable.getTableName();
   }

   // JDBCFieldBridge implementation

   public JDBCType getJDBCType()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isPrimaryKeyMember()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isReadOnly()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isReadTimedOut(EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public boolean isLoaded(EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public void initInstance(EntityEnterpriseContext ctx)
   {
      getFieldState(ctx).init();
   }

   public void resetPersistenceContext(EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public int setInstanceParameters(PreparedStatement ps, int parameterIndex, EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public Object getInstanceValue(EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public void setInstanceValue(EntityEnterpriseContext ctx, Object value)
   {
      throw new UnsupportedOperationException();
   }

   public int loadInstanceResults(ResultSet rs, int parameterIndex, EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public int loadArgumentResults(ResultSet rs, int parameterIndex, Object[] argumentRef)
   {
      throw new UnsupportedOperationException();
   }

   public boolean isDirty(EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public void setClean(EntityEnterpriseContext ctx)
   {
      throw new UnsupportedOperationException();
   }

   public boolean isCMPField()
   {
      return false;
   }

   // CMRFieldBridge implementation

   public String getFieldName()
   {
      return metadata.getCMRFieldName();
   }

   public Object getValue(EntityEnterpriseContext ctx)
   {
      FieldState state = getFieldState(ctx);
      return state.getValue(ctx);
   }

   public void setValue(EntityEnterpriseContext ctx, Object value)
   {
      FieldState state = getFieldState(ctx);
      state.setValue(ctx, value);
      state.cacheValue(ctx);
   }

   public boolean isSingleValued()
   {
      return metadata.getRelatedRole().isMultiplicityOne();
   }

   public EntityBridge getRelatedEntity()
   {
      return relatedEntity;
   }

   // Private

   private void initializeForeignKeyFields() throws DeploymentException
   {
      Collection foreignKeys = metadata.getRelatedRole().getKeyFields();

      // temporary map used later to write fk fields in special order
      Map fkFieldsByRelatedPKFields = new HashMap();
      for(Iterator i = foreignKeys.iterator(); i.hasNext();)
      {
         JDBCCMPFieldMetaData fkFieldMetaData = (JDBCCMPFieldMetaData) i.next();
         JDBCCMPFieldBridge2 relatedPKField =
            (JDBCCMPFieldBridge2) relatedEntity.getFieldByName(fkFieldMetaData.getFieldName());

         // now determine whether the fk is mapped to a pk column
         String fkColumnName = fkFieldMetaData.getColumnName();
         JDBCCMPFieldBridge2 fkField = null;

         // look among the CMP fields for the field with the same column name
         JDBCCMPFieldBridge2[] tableFields = (JDBCCMPFieldBridge2[]) entity.getTableFields();
         for(int tableInd = 0; tableInd < tableFields.length && fkField == null; ++tableInd)
         {
            JDBCCMPFieldBridge2 cmpField = tableFields[tableInd];
            if(fkColumnName.equals(cmpField.getColumnName()))
            {
               // construct the foreign key field
               fkField = new JDBCCMPFieldBridge2(cmpField, relatedPKField);
               /*
                  cmpField.getManager(), // this cmpField's manager
                  relatedPKField.getFieldName(),
                  relatedPKField.getFieldType(),
                  cmpField.getJDBCType(), // this cmpField's jdbc type
                  relatedPKField.isReadOnly(),
                  relatedPKField.getReadTimeOut(),
                  relatedPKField.getPrimaryKeyClass(),
                  relatedPKField.getPrimaryKeyField(),
                  cmpField, // CMP field I am mapped to
                  this,
                  fkColumnName
               );
               */
            }
         }

         // if the fk is not a part of pk then create a new field
         if(fkField == null)
         {
            fkField = entity.addTableField(fkFieldMetaData);
         }
         fkFieldsByRelatedPKFields.put(relatedPKField, fkField); // temporary map
      }

      // Note: this important to order the foreign key fields so that their order matches
      // the order of related entity's pk fields in case of complex primary keys.
      // The order is important in fk-constraint generation and in SELECT when loading
      if(fkFieldsByRelatedPKFields.size() > 0)
      {
         JDBCFieldBridge[] pkFields = relatedEntity.getPrimaryKeyFields();
         List fkList = new ArrayList(pkFields.length);
         List relatedPKList = new ArrayList(pkFields.length);
         for(int i = 0; i < pkFields.length; ++i)
         {
            JDBCFieldBridge relatedPKField = pkFields[i];
            JDBCFieldBridge fkField = (JDBCCMPFieldBridge2) fkFieldsByRelatedPKFields.remove(relatedPKField);
            fkList.add(fkField);
            relatedPKList.add(relatedPKField);
         }
         foreignKeyFields = (JDBCCMPFieldBridge2[]) fkList.toArray(new JDBCCMPFieldBridge2[fkList.size()]);
         relatedPKFields =
            (JDBCCMPFieldBridge2[]) relatedPKList.toArray(new JDBCCMPFieldBridge2[relatedPKList.size()]);
      }
      else
      {
         foreignKeyFields = null;
         relatedPKFields = null;
      }
   }

   private FieldState getFieldState(EntityEnterpriseContext ctx)
   {
      PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
      FieldState state = pctx.getCMRState(cmrIndex);
      if(state == null)
      {
         if(isSingleValued())
         {
            state = new SingleValuedFieldState();
         }
         else
         {
            state = new CollectionValuedFieldState();
         }
         pctx.setCMRState(cmrIndex, state);
      }
      return state;
   }

   private void invokeRemoveRelatedId(Object myId, Object relatedId)
   {
      ClassLoader oldCL = GetTCLAction.getContextClassLoader();
      SetTCLAction.setContextClassLoader(manager.getContainer().getClassLoader());

      try
      {
         Transaction tx = getTransaction();
         EntityCache instanceCache = (EntityCache) manager.getContainer().getInstanceCache();

         /*
         RelationInterceptor.RelationInvocation invocation =
            new RelationInterceptor.RelationInvocation(RelationInterceptor.CMRMessage.REMOVE_RELATED_ID);
         invocation.setId(instanceCache.createCacheKey(myId));
         invocation.setArguments(new Object[]{this, relatedId});
         invocation.setTransaction(tx);
         invocation.setPrincipal(SecurityAssociation.getPrincipal());
         invocation.setCredential(SecurityAssociation.getCredential());
         invocation.setType(InvocationType.LOCAL);
         */

         CMRInvocation invocation = new CMRInvocation();
         invocation.setCmrMessage(CMRMessage.REMOVE_RELATION);
         invocation.setEntrancy(Entrancy.NON_ENTRANT);
         invocation.setId(instanceCache.createCacheKey(myId));
         invocation.setArguments(new Object[]{this, relatedId});
         invocation.setTransaction(tx);
         invocation.setPrincipal(GetPrincipalAction.getPrincipal());
         invocation.setCredential(GetCredentialAction.getCredential());
         invocation.setType(InvocationType.LOCAL);

         manager.getContainer().invoke(invocation);
      }
      catch(EJBException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Error in invokeRemoveRelatedId()", e);
      }
      finally
      {
         SetTCLAction.setContextClassLoader(oldCL);
      }
   }

   private void invokeAddRelatedId(Object myId, Object relatedId)
   {
      ClassLoader oldCL = GetTCLAction.getContextClassLoader();
      SetTCLAction.setContextClassLoader(manager.getContainer().getClassLoader());

      try
      {
         Transaction tx = getTransaction();
         EntityCache instanceCache = (EntityCache) manager.getContainer().getInstanceCache();
         /*
         RelationInterceptor.RelationInvocation invocation =
            new RelationInterceptor.RelationInvocation(RelationInterceptor.CMRMessage.ADD_RELATED_ID);
         invocation.setId(instanceCache.createCacheKey(myId));
         invocation.setArguments(new Object[]{this, relatedId});
         invocation.setTransaction(tx);
         invocation.setPrincipal(SecurityAssociation.getPrincipal());
         invocation.setCredential(SecurityAssociation.getCredential());
         invocation.setType(InvocationType.LOCAL);
         */
         CMRInvocation invocation = new CMRInvocation();
         invocation.setCmrMessage(CMRMessage.ADD_RELATION);
         invocation.setEntrancy(Entrancy.NON_ENTRANT);
         invocation.setId(instanceCache.createCacheKey(myId));
         invocation.setArguments(new Object[]{this, relatedId});
         invocation.setTransaction(tx);
         invocation.setPrincipal(GetPrincipalAction.getPrincipal());
         invocation.setCredential(GetCredentialAction.getCredential());
         invocation.setType(InvocationType.LOCAL);

         manager.getContainer().invoke(invocation);
      }
      catch(EJBException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Error in invokeAddRelatedId()", e);
      }
      finally
      {
         SetTCLAction.setContextClassLoader(oldCL);
      }
   }

   private Transaction getTransaction() throws SystemException
   {
      return tm.getTransaction();
   }

   private RelationTable getRelationTable() throws DeploymentException
   {
      if(relationTable == null)
      {
         relationTable = manager.getSchema().createRelationTable(this, relatedCMRField);
      }
      return relationTable;
   }

   // Inner

   public class SingleValuedFieldState
      implements FieldState
   {
      private boolean loaded;
      private Object value;

      public void init()
      {
         loaded = true;
      }

      public Object getValue(EntityEnterpriseContext ctx)
      {
         // todo: cache this value?
         Object value = getLoadedValue(ctx);
         return value == null ? null : relatedContainer.getLocalProxyFactory().getEntityEJBLocalObject(value);
      }

      public void setValue(EntityEnterpriseContext ctx, Object value)
      {
         if(value != null)
         {
            EJBLocalObject localObject = (EJBLocalObject) value;
            Object relatedId = localObject.getPrimaryKey();

            addRelatedId(ctx, relatedId);
            relatedCMRField.invokeAddRelatedId(relatedId, ctx.getId());
         }
         else
         {
            destroyExistingRelationships(ctx);
         }

         cacheValue(ctx);
      }

      public void cascadeDelete(EntityEnterpriseContext ctx) throws RemoveException
      {
         EJBLocalObject value = (EJBLocalObject) getValue(ctx);
         if(value != null)
         {
            value.remove();
         }
      }

      public void destroyExistingRelationships(EntityEnterpriseContext ctx)
      {
         Object value = getLoadedValue(ctx);
         if(value != null)
         {
            removeRelatedId(ctx, value);
            relatedCMRField.invokeRemoveRelatedId(value, ctx.getId());
         }
      }

      public boolean removeRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         if(hasForeignKey())
         {
            getLoadedValue(ctx);
         }
         else
         {
            loaded = true;
         }

         this.value = null;
         loader.removeRelatedId(ctx, relatedId);

         return true;
      }

      public boolean addRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         Object value = getLoadedValue(ctx);
         if(value != null)
         {
            relatedCMRField.invokeRemoveRelatedId(value, ctx.getId());
         }

         this.value = relatedId;
         loader.addRelatedId(ctx, relatedId);

         return true;
      }

      public void addLoadedPk(Object pk)
      {
         if(loaded)
         {
            throw new IllegalStateException(entity.getEntityName()
               +
               "."
               +
               getFieldName()
               +
               " single-valued CMR field is already loaded. Check the database for consistancy. "
               + " current value=" + value + ", loaded value=" + pk);
         }

         value = pk;
         loaded = true;
      }

      public Object loadFromCache(Object value)
      {
         if(value != null)
         {
            this.value = (NULL_VALUE == value ? null : value);
            loaded = true;
         }
         return value;
      }

      public Object getCachedValue()
      {
         return value == null ? NULL_VALUE : value;
      }

      public void cacheValue(EntityEnterpriseContext ctx)
      {
         PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
         pctx.cacheRelations(cmrIndex, this);
      }

      // Private

      private Object getLoadedValue(EntityEnterpriseContext ctx)
      {
         if(!loaded)
         {
            PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
            pctx.loadCachedRelations(cmrIndex, this);
            if(!loaded)
            {
               loader.load(ctx, this);
               loaded = true;
               cacheValue(ctx);
            }
         }
         return value;
      }
   }

   public class CollectionValuedFieldState
      implements FieldState
   {
      private boolean loaded;
      private Set value;

      private Set removedWhileNotLoaded;
      private Set addedWhileNotLoaded;

      public void init()
      {
         loaded = true;
         value = new HashSet();
      }

      public Object getValue(EntityEnterpriseContext ctx)
      {
         // todo: cache this value?
         return new CMRSet(ctx, this);
      }

      public void setValue(EntityEnterpriseContext ctx, Object value)
      {
         if(value == null)
         {
            throw new IllegalStateException("Can't set collection-valued CMR field to null: " +
               entity.getEntityName() + "." + getFieldName());
         }

         Set newValue = (Set) value;
         if(!newValue.isEmpty())
         {
            Set copy = new HashSet(newValue);
            for(Iterator iter = copy.iterator(); iter.hasNext();)
            {
               EJBLocalObject localObject = (EJBLocalObject) iter.next();
               Object relatedId = localObject.getPrimaryKey();

               addRelatedId(ctx, relatedId);
               relatedCMRField.invokeAddRelatedId(relatedId, ctx.getId());
            }
         }
      }

      public void cascadeDelete(EntityEnterpriseContext ctx) throws RemoveException
      {
         Collection value = (Collection) getValue(ctx);
         if(!value.isEmpty())
         {
            EJBLocalObject[] locals = (EJBLocalObject[]) value.toArray();
            for(int i = 0; i < locals.length; ++i)
            {
               locals[i].remove();
            }
         }
      }

      public void destroyExistingRelationships(EntityEnterpriseContext ctx)
      {
         Set value = getLoadedValue(ctx);
         if(!value.isEmpty())
         {
            Object[] copy = value.toArray();
            for(int i = 0; i < copy.length; ++i)
            {
               Object relatedId = copy[i];
               removeRelatedId(ctx, relatedId);
               relatedCMRField.invokeRemoveRelatedId(relatedId, ctx.getId());
               loader.removeRelatedId(ctx, relatedId);
            }
         }
      }

      public boolean removeRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         boolean modified = false;
         if(loaded)
         {
            Set value = getLoadedValue(ctx);
            if(!value.isEmpty())
            {
               modified = value.remove(relatedId);
            }
         }
         else
         {
            modified = removeWhileNotLoaded(relatedId);
         }
         return modified;
      }

      public boolean addRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         boolean modified;
         if(loaded)
         {
            Set value = getLoadedValue(ctx);
            modified = value.add(relatedId);
         }
         else
         {
            modified = addWhileNotLoaded(relatedId);
         }
         return modified;
      }

      public void addLoadedPk(Object pk)
      {
         if(loaded)
         {
            throw new IllegalStateException(entity.getEntityName()
               +
               "."
               +
               getFieldName()
               +
               " collection-valued CMR field is already loaded. Check the database for consistancy. "
               + " current value=" + value + ", loaded value=" + pk);
         }

         if(pk != null)
         {
            value.add(pk);
         }
      }

      public Object loadFromCache(Object value)
      {
         if(value != null)
         {
            value = this.value = new HashSet((Set) value);
            loaded = true;
         }
         return value;
      }

      public Object getCachedValue()
      {
         return value;
      }

      public void cacheValue(EntityEnterpriseContext ctx)
      {
         PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
         pctx.cacheRelations(cmrIndex, this);
      }

      // Private

      private Set getLoadedValue(EntityEnterpriseContext ctx)
      {
         if(!loaded)
         {
            PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
            pctx.loadCachedRelations(cmrIndex, this);

            if(!loaded)
            {
               if(value == null || value == Collections.EMPTY_SET)
               {
                  value = new HashSet();
               }

               loader.load(ctx, this);

               if(addedWhileNotLoaded != null)
               {
                  value.addAll(addedWhileNotLoaded);
                  addedWhileNotLoaded = null;
               }

               if(removedWhileNotLoaded != null)
               {
                  value.removeAll(removedWhileNotLoaded);
                  removedWhileNotLoaded = null;
               }

               loaded = true;

               cacheValue(ctx);
            }
         }
         return value;
      }

      private boolean removeWhileNotLoaded(Object relatedId)
      {
         boolean removed = false;
         if(addedWhileNotLoaded != null)
         {
            removed = addedWhileNotLoaded.remove(relatedId);
         }

         if(!removed)
         {
            if(removedWhileNotLoaded == null)
            {
               removedWhileNotLoaded = new HashSet();
            }
            removed = removedWhileNotLoaded.add(relatedId);
         }

         if(log.isTraceEnabled() && removed)
         {
            log.trace("removed while not loaded: relatedId=" + relatedId);
         }

         return removed;
      }

      private boolean addWhileNotLoaded(Object relatedId)
      {
         boolean added = false;
         if(removedWhileNotLoaded != null)
         {
            added = removedWhileNotLoaded.remove(relatedId);
         }

         if(!added)
         {
            if(addedWhileNotLoaded == null)
            {
               addedWhileNotLoaded = new HashSet();
            }
            added = addedWhileNotLoaded.add(relatedId);
         }

         if(log.isTraceEnabled() && added)
         {
            log.trace("added while not loaded: relatedId=" + relatedId);
         }

         return added;
      }
   }

   public interface FieldState
      extends Cache.CacheLoader
   {
      Object NULL_VALUE = new Object();

      void init();

      Object getValue(EntityEnterpriseContext ctx);

      void cascadeDelete(EntityEnterpriseContext ctx) throws RemoveException;

      void destroyExistingRelationships(EntityEnterpriseContext ctx);

      void setValue(EntityEnterpriseContext ctx, Object value);

      boolean removeRelatedId(EntityEnterpriseContext ctx, Object relatedId);

      boolean addRelatedId(EntityEnterpriseContext ctx, Object value);

      void addLoadedPk(Object pk);

      void cacheValue(EntityEnterpriseContext ctx);
   }

   private class RelationTableLoader
      implements CMRFieldLoader
   {
      private final String loadSql;

      public RelationTableLoader()
      {
         StringBuffer sql = new StringBuffer();
         sql.append("select ");

         String relatedTable = relatedEntity.getTableName();
         String relationTable = metadata.getRelationMetaData().getDefaultTableName();

         relatedEntity.getTable().appendColumnNames((JDBCCMPFieldBridge2[]) relatedEntity.getTableFields(),
            relatedTable,
            sql);
         sql.append(" from ")
            .append(relatedTable)
            .append(" inner join ")
            .append(relationTable)
            .append(" on ");

         JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) relatedEntity.getPrimaryKeyFields();
         for(int i = 0; i < pkFields.length; ++i)
         {
            if(i > 0)
            {
               sql.append(" and ");
            }

            sql.append(relatedTable).append('.').append(pkFields[i].getColumnName())
               .append('=')
               .append(relationTable).append('.').append(relatedCMRField.tableKeyFields[i].getColumnName());
         }

         /*
         sql.append(" inner join ")
            .append(myTable)
            .append(" on ");

         String myTable = entity.getTableName();
         pkFields = entity.getPrimaryKeyFields();
         for(int i = 0; i < pkFields.length; ++i)
         {
            if(i > 0)
            {
               sql.append(", ");
            }

            sql.append(myTable).append('.').append(pkFields[i].getColumnName())
               .append('=')
               .append(relationTable).append('.').append(tableKeyFields[i].getColumnName());
         }
         */

         sql.append(" where ");
         for(int i = 0; i < tableKeyFields.length; ++i)
         {
            if(i > 0)
            {
               sql.append(" and ");
            }

            sql.append(relationTable).append('.').append(tableKeyFields[i].getColumnName()).append("=?");
         }

         loadSql = sql.toString();

         if(log.isTraceEnabled())
         {
            log.trace("load sql: " + loadSql);
         }
      }

      public void load(EntityEnterpriseContext ctx, FieldState state)
      {
         Object value;
         EntityTable relatedTable = relatedEntity.getTable();

         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try
         {
            if(log.isDebugEnabled())
            {
               log.debug("executing: " + loadSql);
            }

            con = relatedTable.getDataSource().getConnection();
            ps = con.prepareStatement(loadSql);

            JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();

            Object myPk = ctx.getId();
            int paramInd = 1;
            for(int i = 0; i < pkFields.length; ++i)
            {
               JDBCCMPFieldBridge2 pkField = pkFields[i];
               Object fieldValue = pkField.getPrimaryKeyValue(myPk);

               JDBCCMPFieldBridge2 relatedFkField = tableKeyFields[i];
               relatedFkField.setArgumentParameters(ps, paramInd++, fieldValue);
            }

            rs = ps.executeQuery();

            while(rs.next())
            {
               value = relatedTable.loadRow(rs);
               state.addLoadedPk(value);
            }
         }
         catch(SQLException e)
         {
            log.error("Failed to load related role: ejb-name="
               +
               entity.getEntityName() +
               ", cmr-field=" + getFieldName() + ": " + e.getMessage(), e);
            throw new EJBException("Failed to load related role: ejb-name="
               +
               entity.getEntityName() +
               ", cmr-field=" + getFieldName() + ": " + e.getMessage(), e);
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(ps);
            JDBCUtil.safeClose(con);
         }
      }

      public void removeRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         relationTable.removeRelation(JDBCCMRFieldBridge2.this, ctx.getId(), relatedCMRField, relatedId);
      }

      public void addRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         relationTable.addRelation(JDBCCMRFieldBridge2.this, ctx.getId(), relatedCMRField, relatedId);
      }
   }

   private class ForeignKeyLoader
      implements CMRFieldLoader
   {
      private final String loadSql;

      public ForeignKeyLoader()
      {
         StringBuffer sql = new StringBuffer();
         sql.append("select ");
         relatedEntity.getTable().appendColumnNames((JDBCCMPFieldBridge2[]) relatedEntity.getTableFields(), null, sql);
         sql.append(" from ").append(relatedEntity.getTableName()).append(" where ");

         JDBCCMPFieldBridge2[] relatedFkFields = relatedCMRField.foreignKeyFields;
         sql.append(relatedFkFields[0].getColumnName()).append("=?");
         for(int i = 1; i < relatedFkFields.length; ++i)
         {
            JDBCCMPFieldBridge2 relatedFkField = relatedFkFields[i];
            sql.append(" and ").append(relatedFkField.getColumnName()).append("=?");
         }

         loadSql = sql.toString();

         if(log.isTraceEnabled())
         {
            log.trace("load sql: " + loadSql);
         }
      }

      public void load(EntityEnterpriseContext ctx, FieldState state)
      {
         Object value;
         EntityTable relatedTable = relatedEntity.getTable();

         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try
         {
            if(log.isDebugEnabled())
            {
               log.debug("executing: " + loadSql);
            }

            con = relatedTable.getDataSource().getConnection();
            ps = con.prepareStatement(loadSql);

            JDBCCMPFieldBridge2[] relatedFkFields = relatedCMRField.foreignKeyFields;
            JDBCCMPFieldBridge2[] myPkFields = relatedCMRField.relatedPKFields;

            Object myPk = ctx.getId();
            int paramInd = 1;
            for(int i = 0; i < relatedFkFields.length; ++i)
            {
               JDBCCMPFieldBridge2 myPkField = myPkFields[i];
               Object fieldValue = myPkField.getPrimaryKeyValue(myPk);

               JDBCCMPFieldBridge2 relatedFkField = relatedFkFields[i];
               relatedFkField.setArgumentParameters(ps, paramInd++, fieldValue);
            }

            rs = ps.executeQuery();

            while(rs.next())
            {
               value = relatedTable.loadRow(rs);
               state.addLoadedPk(value);
            }
         }
         catch(SQLException e)
         {
            log.error("Failed to load related role: ejb-name="
               +
               entity.getEntityName() +
               ", cmr-field=" + getFieldName() + ": " + e.getMessage(), e);
            throw new EJBException("Failed to load related role: ejb-name="
               +
               entity.getEntityName() +
               ", cmr-field=" + getFieldName() + ": " + e.getMessage(), e);
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(ps);
            JDBCUtil.safeClose(con);
         }
      }

      public void removeRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
      }

      public void addRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
      }
   }

   private class ContextForeignKeyLoader
      implements CMRFieldLoader
   {
      public void load(EntityEnterpriseContext ctx, FieldState state)
      {
         Object relatedId = null;
         for(int i = 0; i < foreignKeyFields.length; ++i)
         {
            JDBCCMPFieldBridge2 fkField = foreignKeyFields[i];
            Object fkFieldValue = fkField.getValue(ctx);
            if(fkFieldValue == null)
            {
               break;
            }

            JDBCCMPFieldBridge2 relatedPKField = relatedPKFields[i];
            relatedId = relatedPKField.setPrimaryKeyValue(relatedId, fkFieldValue);
         }

         state.addLoadedPk(relatedId);
      }

      public void removeRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         for(int i = 0; i < foreignKeyFields.length; ++i)
         {
            foreignKeyFields[i].setValueInternal(ctx, null, true);
         }
      }

      public void addRelatedId(EntityEnterpriseContext ctx, Object relatedId)
      {
         for(int i = 0; i < foreignKeyFields.length; ++i)
         {
            JDBCCMPFieldBridge2 relatedPKField = relatedPKFields[i];
            Object fieldValue = relatedPKField.getPrimaryKeyValue(relatedId);
            foreignKeyFields[i].setValueInternal(ctx, fieldValue, true);
         }
      }
   }

   private interface CMRFieldLoader
   {
      void load(EntityEnterpriseContext ctx, FieldState state);

      void removeRelatedId(EntityEnterpriseContext ctx, Object relatedId);

      void addRelatedId(EntityEnterpriseContext ctx, Object relatedId);
   }

   private class CMRSet implements Set
   {
      private final EntityEnterpriseContext ctx;
      private final CollectionValuedFieldState state;

      public CMRSet(EntityEnterpriseContext ctx, CollectionValuedFieldState state)
      {
         this.ctx = ctx;
         this.state = state;
      }

      public int size()
      {
         return state.getLoadedValue(ctx).size();
      }

      public void clear()
      {
         destroyExistingRelationships(ctx);
      }

      public boolean isEmpty()
      {
         return size() == 0;
      }

      public boolean add(Object o)
      {
         EJBLocalObject local = memberArgumentValidation(o);
         Object relatedId = local.getPrimaryKey();
         boolean modified = addRelatedId(ctx, relatedId);

         if(modified)
         {
            relatedCMRField.invokeAddRelatedId(relatedId, ctx.getId());
            loader.addRelatedId(ctx, relatedId);
         }

         return modified;
      }

      public boolean contains(Object o)
      {
         EJBLocalObject local = memberArgumentValidation(o);
         return state.getLoadedValue(ctx).contains(local.getPrimaryKey());
      }

      public boolean remove(Object o)
      {
         EJBLocalObject local = memberArgumentValidation(o);
         Object relatedId = local.getPrimaryKey();
         return removeById(relatedId);
      }

      public boolean addAll(Collection c)
      {
         if(c == null || c.isEmpty())
         {
            return false;
         }

         boolean modified = false;
         Object[] copy = c.toArray();
         for(int i = 0; i < copy.length; ++i)
         {
            // not modified || add()
            modified = add(copy[i]) || modified;
         }

         return modified;
      }

      public boolean containsAll(Collection c)
      {
         if(c == null || c.isEmpty())
         {
            return true;
         }

         Set ids = argumentToIdSet(c);

         return state.getLoadedValue(ctx).contains(ids);
      }

      public boolean removeAll(Collection c)
      {
         if(c == null || c.isEmpty())
         {
            return false;
         }

         boolean modified = false;
         Object[] copy = c.toArray();
         for(int i = 0; i < copy.length; ++i)
         {
            modified = remove(copy[i]) || modified;
         }

         return modified;
      }

      public boolean retainAll(Collection c)
      {
         Set value = state.getLoadedValue(ctx);
         if(c == null || c.isEmpty())
         {
            if(value.isEmpty())
            {
               return false;
            }
            else
            {
               clear();
            }
         }

         boolean modified = false;
         Set idSet = argumentToIdSet(c);
         Object[] valueCopy = value.toArray();
         for(int i = 0; i < valueCopy.length; ++i)
         {
            Object id = valueCopy[i];
            if(!idSet.contains(id))
            {
               removeById(id);
               modified = true;
            }
         }

         return modified;
      }

      public Iterator iterator()
      {
         return new Iterator()
         {
            // todo get rid of copying
            private final Iterator idIter = new HashSet(state.getLoadedValue(ctx)).iterator();
            private Object curId;

            public void remove()
            {
               try
               {
                  idIter.remove();
               }
               catch(ConcurrentModificationException e)
               {
                  throw new IllegalStateException(e.getMessage());
               }

               removeById(curId);
            }

            public boolean hasNext()
            {
               try
               {
                  return idIter.hasNext();
               }
               catch(ConcurrentModificationException e)
               {
                  throw new IllegalStateException(e.getMessage());
               }
            }

            public Object next()
            {
               try
               {
                  curId = idIter.next();
               }
               catch(ConcurrentModificationException e)
               {
                  throw new IllegalStateException(e.getMessage());
               }

               return relatedContainer.getLocalProxyFactory().getEntityEJBLocalObject(curId);
            }
         };
      }

      public Object[] toArray()
      {
         Set value = state.getLoadedValue(ctx);

         Object[] result = (Object[]) Array.newInstance(relatedEntity.getLocalInterface(), value.size());

         LocalProxyFactory relatedPF = relatedContainer.getLocalProxyFactory();
         int i = 0;
         for(Iterator iter = value.iterator(); iter.hasNext();)
         {
            Object id = iter.next();
            result[i++] = relatedPF.getEntityEJBLocalObject(id);
         }

         return result;
      }

      public Object[] toArray(Object a[])
      {
         Set value = state.getLoadedValue(ctx);
         if(a == null || a.length < value.size())
         {
            a = (Object[]) Array.newInstance(entity.getLocalInterface(), value.size());
         }

         LocalProxyFactory relatedPF = relatedContainer.getLocalProxyFactory();
         int i = 0;
         for(Iterator iter = value.iterator(); iter.hasNext();)
         {
            Object id = iter.next();
            a[i++] = relatedPF.getEntityEJBLocalObject(id);
         }

         return a;
      }

      public String toString()
      {
         return state.getLoadedValue(ctx).toString();
      }

      // Private

      private boolean removeById(Object relatedId)
      {
         boolean modified = removeRelatedId(ctx, relatedId);
         if(modified)
         {
            relatedCMRField.invokeRemoveRelatedId(relatedId, ctx.getId());
            loader.removeRelatedId(ctx, relatedId);
         }
         return modified;
      }

      private Set argumentToIdSet(Collection c)
      {
         Set ids = new HashSet();
         for(Iterator iter = c.iterator(); iter.hasNext();)
         {
            EJBLocalObject local = memberArgumentValidation(iter.next());
            ids.add(local.getPrimaryKey());
         }
         return ids;
      }

      private EJBLocalObject memberArgumentValidation(Object o)
      {
         if(o == null)
         {
            throw new IllegalStateException("This implementation does not support null members.");
         }

         if(!relatedEntity.getLocalInterface().isInstance(o))
         {
            throw new IllegalStateException("Memebers must be of type " + entity.getLocalInterface().getName());
         }

         return (EJBLocalObject) o;
      }
   }

   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();

      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }

      static ClassLoader getContextClassLoader()
      {
         ClassLoader loader = (ClassLoader) AccessController.doPrivileged(ACTION);
         return loader;
      }
   }

   private static class SetTCLAction implements PrivilegedAction
   {
      ClassLoader loader;

      SetTCLAction(ClassLoader loader)
      {
         this.loader = loader;
      }

      public Object run()
      {
         Thread.currentThread().setContextClassLoader(loader);
         loader = null;
         return null;
      }

      static void setContextClassLoader(ClassLoader loader)
      {
         PrivilegedAction action = new SetTCLAction(loader);
         AccessController.doPrivileged(action);
      }
   }

   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();

      public Object run()
      {
         Principal principal = SecurityAssociation.getPrincipal();
         return principal;
      }

      static Principal getPrincipal()
      {
         Principal principal = (Principal) AccessController.doPrivileged(ACTION);
         return principal;
      }
   }

   private static class GetCredentialAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetCredentialAction();

      public Object run()
      {
         Object credential = SecurityAssociation.getCredential();
         return credential;
      }

      static Object getCredential()
      {
         Object credential = AccessController.doPrivileged(ACTION);
         return credential;
      }
   }

}
