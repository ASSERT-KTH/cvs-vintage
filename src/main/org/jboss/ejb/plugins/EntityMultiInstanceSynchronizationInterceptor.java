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
import org.jboss.ejb.EntityPersistenceManager;
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
 *    before changing.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.6 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/08: billb</b>
 * <ol>
 *   <li>Initial revision
 * </ol>
 */
public class EntityMultiInstanceSynchronizationInterceptor
   extends EntitySynchronizationInterceptor
{
   public void create()
      throws Exception
   {
      super.create();
      if (container.getInstancePool() instanceof EntityInstancePool)
      {
         ((EntityInstancePool)container.getInstancePool()).setReclaim(true);
      }
   }

   protected Synchronization createSynchronization(Transaction tx, EntityEnterpriseContext ctx)
   {
      return new MultiInstanceSynchronization(tx, ctx);
   } 
   // Protected  ----------------------------------------------------
 
   // Inner classes -------------------------------------------------
 
   protected class MultiInstanceSynchronization extends EntitySynchronizationInterceptor.InstanceSynchronization
   {
      /**
       *  Create a new instance synchronization instance.
       */
      MultiInstanceSynchronization(Transaction tx, EntityEnterpriseContext ctx)
      {
         super(tx, ctx);
      }
  
      // Synchronization implementation -----------------------------
  
      public void afterCompletion(int status)
      {
         boolean trace = log.isTraceEnabled();
   
         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(container.getClassLoader());
   
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
                     switch (commitOption)
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
                     container.getPersistenceManager().passivateEntity(ctx);
                  }
                  catch (Exception ignored) {}
                  
                  container.getInstancePool().free(ctx);
               }
               finally
               {
                  if( trace )
                     log.trace("afterCompletion, clear tx for ctx="+ctx+", tx="+tx);
                  
                  lock.endTransaction(tx);
                  
                  if( trace )
                     log.trace("afterCompletion, sent notify on TxLock for ctx="+ctx);
               }
            } // synchronized(lock)
            finally
            {
               lock.releaseSync();
               container.getLockManager().removeLockRef(lock.getId());
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
