/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityCache;
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;
import org.jboss.invocation.InvocationType;
import org.jboss.security.SecurityAssociation;

import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Principal;

/**
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 1.7 $
 */
public abstract class CascadeDeleteStrategy
{
   /**
    * No cascade-delete strategy.
    */
   public static final class NoneCascadeDeleteStrategy
      extends CascadeDeleteStrategy
   {
      public NoneCascadeDeleteStrategy(JDBCCMRFieldBridge cmrField) throws DeploymentException
      {
         super(cmrField);
      }

      public void removedIds(EntityEnterpriseContext ctx, Object[] oldRelationRefs, List ids)
      {
         cmrField.setInstanceValue(ctx, null);
      }

      public void cascadeDelete(EntityEnterpriseContext ctx, List oldValues)
      {
         boolean trace = log.isTraceEnabled();
         for(int i = 0; i < oldValues.size(); ++i)
         {
            Object oldValue = oldValues.get(i);
            if(relatedManager.unscheduledCascadeDelete(oldValue))
            {
               if(trace)
               {
                  log.trace("Removing " + oldValue);
               }

               invokeRemoveRelated(oldValue);
            }
            else if(trace)
            {
               log.trace(oldValue + " already removed");
            }
         }
      }
   }

   /**
    * Specification compliant cascade-delete strategy, i.e. one DELETE per child
    */
   public static final class DefaultCascadeDeleteStrategy
      extends CascadeDeleteStrategy
   {
      public DefaultCascadeDeleteStrategy(JDBCCMRFieldBridge cmrField) throws DeploymentException
      {
         super(cmrField);
      }

      public void removedIds(EntityEnterpriseContext ctx, Object[] oldRelationRef, List ids)
      {
         cmrField.scheduleChildrenForCascadeDelete(ctx);
         scheduleCascadeDelete(oldRelationRef, new ArrayList(ids));
         cmrField.setInstanceValue(ctx, null);
      }

      public void cascadeDelete(EntityEnterpriseContext ctx, List oldValues)
      {
         boolean trace = log.isTraceEnabled();
         for(int i = 0; i < oldValues.size(); ++i)
         {
            Object oldValue = oldValues.get(i);
            if(relatedManager.unscheduledCascadeDelete(oldValue))
            {
               if(trace)
               {
                  log.trace("Removing " + oldValue);
               }
               invokeRemoveRelated(oldValue);
            }
            else if(trace)
            {
               log.trace(oldValue + " already removed");
            }
         }
      }
   }

   /**
    * Batch cascade-delete strategy. Deletes children with one statement of the form
    * DELETE FROM RELATED_TABLE WHERE FOREIGN_KEY = ?
    */
   public static final class BatchCascadeDeleteStrategy
      extends CascadeDeleteStrategy
   {
      private final String batchCascadeDeleteSql;

      public BatchCascadeDeleteStrategy(JDBCCMRFieldBridge cmrField)
         throws DeploymentException
      {
         super(cmrField);

         if(cmrField.hasForeignKey())
         {
            throw new DeploymentException(
               "Batch cascade-delete was setup for the role with a foreign key: relationship "
               + cmrField.getMetaData().getRelationMetaData().getRelationName()
               + ", role " + cmrField.getMetaData().getRelationshipRoleName()
               + ". Batch cascade-delete supported only for roles with no foreign keys."
            );
         }

         StringBuffer buf = new StringBuffer(100);
         buf.append("DELETE FROM ")
            .append(cmrField.getRelatedJDBCEntity().getTableName())
            .append(" WHERE ");
         SQLUtil.getWhereClause(cmrField.getRelatedCMRField().getForeignKeyFields(), buf);
         batchCascadeDeleteSql = buf.toString();

         log.debug(
            cmrField.getMetaData().getRelationMetaData().getRelationName() + " batch cascade delete SQL: "
            + batchCascadeDeleteSql
         );
      }

      public void removedIds(EntityEnterpriseContext ctx, Object[] oldRelationRefs, List ids)
      {
         cmrField.scheduleChildrenForBatchCascadeDelete(ctx);
         scheduleCascadeDelete(oldRelationRefs, new ArrayList(ids));
      }

      public void cascadeDelete(EntityEnterpriseContext ctx, List oldValues) throws RemoveException
      {
         boolean didDelete = false;
         boolean trace = log.isTraceEnabled();
         for(int i = 0; i < oldValues.size(); ++i)
         {
            Object oldValue = oldValues.get(i);
            if(relatedManager.unscheduledCascadeDelete(oldValue))
            {
               if(trace)
               {
                  log.trace("Removing " + oldValue);
               }
               invokeRemoveRelated(oldValue);
               didDelete = true;
            }
            else if(trace)
            {
               log.trace(oldValue + " already removed");
            }
         }

         if(didDelete)
         {
            executeDeleteSQL(batchCascadeDeleteSql, ctx.getId());
         }
      }
   }

   public static CascadeDeleteStrategy getCascadeDeleteStrategy(JDBCCMRFieldBridge cmrField)
      throws DeploymentException
   {
      CascadeDeleteStrategy result;
      JDBCRelationshipRoleMetaData relatedRole = cmrField.getMetaData().getRelatedRole();
      if(relatedRole.isBatchCascadeDelete())
      {
         result = new BatchCascadeDeleteStrategy(cmrField);
      }
      else if(relatedRole.isCascadeDelete())
      {
         result = new DefaultCascadeDeleteStrategy(cmrField);
      }
      else
      {
         result = new NoneCascadeDeleteStrategy(cmrField);
      }
      return result;
   }

   protected final JDBCCMRFieldBridge cmrField;
   protected final JDBCEntityBridge entity;
   protected final JDBCStoreManager relatedManager;
   protected final Logger log;

   private Method removeMethod;
   private InvocationType invocationType;

   public CascadeDeleteStrategy(JDBCCMRFieldBridge cmrField) throws DeploymentException
   {
      this.cmrField = cmrField;
      entity = (JDBCEntityBridge)cmrField.getEntity();
      relatedManager = cmrField.getRelatedManager();

      Class localClass = relatedManager.getMetaData().getLocalClass();
      if(localClass != null)
      {
         try
         {
            removeMethod = localClass.getMethod("remove", new Class[]{});
         }
         catch(NoSuchMethodException e)
         {
            throw new DeploymentException("Failed to obtain the remove method from " + localClass.getName(), e);
         }
         invocationType = InvocationType.LOCAL;
      }
      else
      {
         Class remoteClass = relatedManager.getMetaData().getRemoteClass();
         try
         {
            removeMethod = remoteClass.getMethod("remove", new Class[]{});
         }
         catch(NoSuchMethodException e)
         {
            throw new DeploymentException("Failed to obtain the remove method from " + localClass.getName(), e);
         }
         invocationType = InvocationType.REMOTE;
      }

      log = Logger.getLogger(getClass().getName() + "." + cmrField.getEntity().getEntityName());
   }

   public abstract void removedIds(EntityEnterpriseContext ctx, Object[] oldRelationRefs, List ids);

   public abstract void cascadeDelete(EntityEnterpriseContext ctx, List oldValues) throws RemoveException;

   protected void scheduleCascadeDelete(Object[] oldRelationsRef, List values)
   {
      Map oldRelations = (Map)oldRelationsRef[0];
      if(oldRelations == null)
      {
         oldRelations = new HashMap();
         oldRelationsRef[0] = oldRelations;
      }
      oldRelations.put(cmrField, values);
      relatedManager.scheduleCascadeDelete(values);
   }

   protected void executeDeleteSQL(String sql, Object key) throws RemoveException
   {
      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected = 0;
      try
      {
         if(log.isDebugEnabled())
            log.debug("Executing SQL: " + sql);

         // get the connection
         con = entity.getDataSource().getConnection();
         ps = con.prepareStatement(sql);

         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, key);

         // execute statement
         rowsAffected = ps.executeUpdate();
      }
      catch(Exception e)
      {
         log.error("Could not remove " + key, e);
         throw new RemoveException("Could not remove " + key);
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected == 0)
      {
         log.error("Could not remove entity " + key);
         throw new RemoveException("Could not remove entity");
      }

      if(log.isDebugEnabled())
         log.debug("Remove: Rows affected = " + rowsAffected);
   }

   public void invokeRemoveRelated(Object relatedId)
   {
      EntityContainer container = relatedManager.getContainer();

      try
      {
         EntityCache instanceCache = (EntityCache) container.getInstanceCache();

         org.jboss.invocation.Invocation invocation = new org.jboss.invocation.Invocation();
         invocation.setId(instanceCache.createCacheKey(relatedId));
         invocation.setArguments(new Object[]{});
         invocation.setTransaction(container.getTransactionManager().getTransaction());
         invocation.setPrincipal(GetPrincipalAction.getPrincipal());
         invocation.setCredential(GetCredentialAction.getCredential());
         invocation.setType(invocationType);
         invocation.setMethod(removeMethod);

         container.invoke(invocation);
      }
      catch(EJBException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Error in remove instance", e);
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
