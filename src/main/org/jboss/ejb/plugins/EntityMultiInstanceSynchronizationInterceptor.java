/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * The role of this interceptor is to synchronize the state of the cache with
 * the underlying storage.  It does this with the ejbLoad and ejbStore
 * semantics of the EJB specification.  In the presence of a transaction this
 * is triggered by transaction demarcation. It registers a callback with the
 * underlying transaction monitor through the JTA interfaces.  If there is no
 * transaction the policy is to store state upon returning from invocation.
 * The synchronization polices A,B,C of the specification are taken care of
 * here.
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 * before changing.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.8 $
 */
public class EntityMultiInstanceSynchronizationInterceptor
   extends EntitySynchronizationInterceptor
{
   public void create() throws Exception
   {
      super.create();
      InstancePool pool = getContainer().getInstancePool();
      if(pool instanceof EntityInstancePool)
      {
         ((EntityInstancePool)pool).setReclaim(true);
      }
      // DAIN: if not an EntityInstancePool should we throw an exception?
   }

   protected Synchronization createSynchronization(
         Transaction tx, 
         EntityEnterpriseContext ctx)
   {
      return new MultiInstanceSynchronization(tx, ctx);
   } 
 
   private class MultiInstanceSynchronization implements Synchronization
   {
      /**
       *  The transaction we follow.
       */
      private Transaction tx;

      /**
       *  The context we manage.
       */
      private EntityEnterpriseContext ctx;

      /**
       * The context lock
       */
      private BeanLock lock;
 
      /**
       *  Create a new instance synchronization instance.
       */
      MultiInstanceSynchronization(Transaction tx, EntityEnterpriseContext ctx)
      {
         this.tx = tx;
         this.ctx = ctx;
         this.lock = getContainer().getLockManager().getLock(ctx.getCacheKey());
      }
  
      public void beforeCompletion()
      {
         //synchronization is handled by GlobalTxEntityMap.
      }

      public void afterCompletion(int status)
      {
         EntityContainer container = (EntityContainer)getContainer();
   
         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(
               container.getClassLoader());
   
         try
         {
            lock.sync();
            try
            {
               try
               {
                  // If rolled back -> invalidate instance
                  if (status != Status.STATUS_ROLLEDBACK)
                  {
                     switch (getCommitOption())
                     {
                        // Keep instance cached after tx commit
                     case ConfigurationMetaData.A_COMMIT_OPTION:
                        throw new IllegalStateException("Commit option A not allowed with this Interceptor");
                        // Keep instance active, but invalidate state
                     case ConfigurationMetaData.B_COMMIT_OPTION:
                        break;
                        // Invalidate everything AND Passivate instance
                     case ConfigurationMetaData.C_COMMIT_OPTION:
                        break;
                     case ConfigurationMetaData.D_COMMIT_OPTION:
                        throw new IllegalStateException("Commit option A not allowed with this Interceptor");
                     }
                  }
                  try
                  {
                     container.passivateEntity(ctx);
                  }
                  catch (Exception ignored) {}
                  
                  container.getInstancePool().free(ctx);
               }
               finally
               {
                  boolean trace = log.isTraceEnabled();
                  if(trace)
                  {
                     log.trace("afterCompletion, clear tx for ctx="+ctx+", tx="+tx);
                  }

                  lock.endTransaction(tx);
                  
                  if(trace)
                  {
                     log.trace("afterCompletion, sent notify on TxLock for ctx="+ctx);
                  }
               }
            }
            finally
            {
               lock.releaseSync();
               Thread.currentThread().setContextClassLoader(oldCl);               
            }
         } 
         catch (InterruptedException ex)
         {
            log.error("Failed to lock.sync: " + ex);
         }
      }
   }
}
