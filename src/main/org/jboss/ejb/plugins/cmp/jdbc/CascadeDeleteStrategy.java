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
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;
import org.jboss.security.SecurityAssociation;

import javax.ejb.RemoveException;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.security.PrivilegedAction;
import java.security.Principal;
import java.security.AccessController;
import java.rmi.RemoteException;

/**
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 1.10 $
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

      public void cascadeDelete(EntityEnterpriseContext ctx, List oldValues) throws RemoveException, RemoteException
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

      public void cascadeDelete(EntityEnterpriseContext ctx, List oldValues) throws RemoveException, RemoteException
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
            .append(cmrField.getRelatedJDBCEntity().getQualifiedTableName())
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

      public void cascadeDelete(EntityEnterpriseContext ctx, List oldValues) throws RemoveException, RemoteException
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

   public CascadeDeleteStrategy(JDBCCMRFieldBridge cmrField) throws DeploymentException
   {
      this.cmrField = cmrField;
      entity = (JDBCEntityBridge)cmrField.getEntity();
      relatedManager = cmrField.getRelatedManager();

      log = Logger.getLogger(getClass().getName() + "." + cmrField.getEntity().getEntityName());
   }

   public abstract void removedIds(EntityEnterpriseContext ctx, Object[] oldRelationRefs, List ids);

   public abstract void cascadeDelete(EntityEnterpriseContext ctx, List oldValues) throws RemoveException,
      RemoteException;

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

   public void invokeRemoveRelated(Object relatedId) throws RemoveException, RemoteException
   {
      EntityContainer container = relatedManager.getContainer();

      /*
      try
      {
         EntityCache instanceCache = (EntityCache) container.getInstanceCache();
         SecurityActions actions = SecurityActions.UTIL.getSecurityActions();

         org.jboss.invocation.Invocation invocation = new org.jboss.invocation.Invocation();
         invocation.setId(instanceCache.createCacheKey(relatedId));
         invocation.setArguments(new Object[]{});
         invocation.setTransaction(container.getTransactionManager().getTransaction());
         invocation.setPrincipal(actions.getPrincipal());
         invocation.setCredential(actions.getCredential());
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
      */

      /**
       * Have to remove through EJB[Local}Object interface since the proxy contains the 'removed' flag
       * to be set on removal.
       */
      if(container.getLocalProxyFactory() != null)
      {
         final EJBLocalObject ejbObject = container.getLocalProxyFactory().getEntityEJBLocalObject(relatedId);
         ejbObject.remove();
      }
      else
      {
         final EJBObject ejbObject = (EJBObject)container.getProxyFactory().getEntityEJBObject(relatedId);
         ejbObject.remove();
      }
   }

   interface SecurityActions
   {
      class UTIL
      {
         static SecurityActions getSecurityActions()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }
      }

      SecurityActions NON_PRIVILEGED = new SecurityActions()
      {
         public Principal getPrincipal()
         {
            return SecurityAssociation.getPrincipal();
         }

         public Object getCredential()
         {
            return SecurityAssociation.getCredential();
         }
      };

      SecurityActions PRIVILEGED = new SecurityActions()
      {
         private final PrivilegedAction getPrincipalAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getPrincipal();
            }
         };

         private final PrivilegedAction getCredentialAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getCredential();
            }
         };

         public Principal getPrincipal()
         {
            return (Principal)AccessController.doPrivileged(getPrincipalAction);
         }

         public Object getCredential()
         {
            return AccessController.doPrivileged(getCredentialAction);
         }
      };

      Principal getPrincipal();

      Object getCredential();
   }
}
