/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;

import javax.ejb.EJBException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a way to find out what entities are contained in
 * what transaction.  It is used, to find which entities to call ejbStore()
 * on when a ejbFind() method is called within a transaction. EJB 2.0- 9.6.4
 * also, it is used to synchronize on a remove.
 * Used in EntitySynchronizationInterceptor, EntityContainer
 *
 * Entities are stored in an ArrayList to ensure specific ordering.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 1.14 $
 */
public class GlobalTxEntityMap
{
   private static final Logger log = Logger.getLogger(GlobalTxEntityMap.class);

   private final TransactionLocal txSynch = new TransactionLocal();

   /**
    * An instance can be in one of the three states:
    * <ul>
    * <li>not associated with the tx and, hence, does not need to be synchronized</li>
    * <li>associated with the tx and needs to be synchronized</li>
    * <li>associated with the tx but does not need to be synchronized</li>
    * </ul>
    * Implementations of TxAssociation implement these states.
    */
   public static interface TxAssociation
   {
      /**
       * Schedules the instance for synchronization. The instance might or might not be associated with the tx.
       *
       * @param tx       the transaction the instance should be associated with if not yet associated
       * @param instance the instance to be scheduled for synchronization
       * @throws SystemException
       * @throws RollbackException
       */
      void scheduleSync(Transaction tx, EntityEnterpriseContext instance)
         throws SystemException, RollbackException;

      /**
       * Synchronizes the instance if it is needed.
       * @param thread  current thread
       * @param tx  current transaction
       * @param instance  the instance to be synchronized
       * @throws Exception  thrown if synchronization failed
       */
      void synchronize(Thread thread, Transaction tx, EntityEnterpriseContext instance)
         throws Exception;

      /**
       * Invokes ejbStore if needed
       * @param thread  current thread
       * @param instance  the instance to be synchronized
       * @throws Exception  thrown if synchronization failed
       */
      void invokeEjbStore(Thread thread, EntityEnterpriseContext instance)
         throws Exception;
   }

   public static final TxAssociation NONE = new TxAssociation()
   {
      public void scheduleSync(Transaction tx, EntityEnterpriseContext instance)
         throws SystemException, RollbackException
      {
         EntityContainer.getGlobalTxEntityMap().associate(tx, instance);
         instance.setTxAssociation(SYNC_SCHEDULED);
      }

      public void synchronize(Thread thread, Transaction tx, EntityEnterpriseContext instance)
      {
         throw new UnsupportedOperationException();
      }

      public void invokeEjbStore(Thread thread, EntityEnterpriseContext instance)
      {
         throw new UnsupportedOperationException();
      }
   };

   public static final TxAssociation SYNC_SCHEDULED = new TxAssociation()
   {
      public void scheduleSync(Transaction tx, EntityEnterpriseContext instance)
      {
      }

      public void invokeEjbStore(Thread thread, EntityEnterpriseContext instance) throws Exception
      {
         if(instance.getId() != null)
         {
            EntityContainer container = (EntityContainer) instance.getContainer();
            // set the context class loader before calling the store method
            SecurityActions.setContextClassLoader(thread, container.getClassLoader());

            // store it
            container.invokeEjbStore(instance);
         }
      }

      public void synchronize(Thread thread, Transaction tx, EntityEnterpriseContext instance)
         throws Exception
      {
         // only synchronize if the id is not null.  A null id means
         // that the entity has been removed.
         if(instance.getId() != null)
         {
            EntityContainer container = (EntityContainer) instance.getContainer();

            // set the context class loader before calling the store method
            SecurityActions.setContextClassLoader(thread, container.getClassLoader());

            // store it
            container.storeEntity(instance);

            instance.setTxAssociation(SYNCHRONIZED);
         }
      }
   };

   public static final TxAssociation SYNCHRONIZED = new TxAssociation()
   {
      public void scheduleSync(Transaction tx, EntityEnterpriseContext instance)
      {
         instance.setTxAssociation(SYNC_SCHEDULED);
      }

      public void invokeEjbStore(Thread thread, EntityEnterpriseContext instance)
      {
      }

      public void synchronize(Thread thread, Transaction tx, EntityEnterpriseContext instance)
      {
      }
   };

   /**
    * sync all EntityEnterpriseContext that are involved (and changed)
    * within a transaction.
    */
   public void synchronizeEntities(Transaction tx)
   {
      GlobalTxSynchronization globalSync = (GlobalTxSynchronization) txSynch.get(tx);
      if(globalSync != null)
      {
         globalSync.synchronize();
      }
   }

   /**
    * associate instance with transaction
    */
   private void associate(Transaction tx, EntityEnterpriseContext entity)
      throws RollbackException, SystemException
   {
      GlobalTxSynchronization globalSync = (GlobalTxSynchronization) txSynch.get(tx);
      if(globalSync == null)
      {
         globalSync = new GlobalTxSynchronization(tx);
         txSynch.set(tx, globalSync);
         tx.registerSynchronization(globalSync);
      }

      //There should be only one thread associated with this tx at a time.
      //Therefore we should not need to synchronize on entityFifoList to ensure exclusive
      //access.  entityFifoList is correct since it was obtained in a synch block.

      globalSync.associate(entity);
   }

   // Inner

   /**
    * A list of instances associated with the transaction.
    */
   private class GlobalTxSynchronization implements Synchronization
   {
      private Transaction tx;
      private List instances = new ArrayList();
      private boolean synchronizing;

      public GlobalTxSynchronization(Transaction tx)
      {
         this.tx = tx;
      }

      public void associate(EntityEnterpriseContext ctx)
      {
         instances.add(ctx);
      }

      public void synchronize()
      {
         if(synchronizing || instances.isEmpty())
         {
            return;
         }

         synchronizing = true;

         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         Thread currentThread = Thread.currentThread();
         ClassLoader oldCl = SecurityActions.getContextClassLoader();

         EntityEnterpriseContext instance = null;
         try
         {
            for(int i = 0; i < instances.size(); i++)
            {
               // any one can mark the tx rollback at any time so check
               // before continuing to the next store
               if(tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
               {
                  return;
               }

               instance = (EntityEnterpriseContext) instances.get(i);
               instance.getTxAssociation().invokeEjbStore(currentThread, instance);
            }

            for(int i = 0; i < instances.size(); i++)
            {
               // any one can mark the tx rollback at any time so check
               // before continuing to the next store
               if(tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
               {
                  return;
               }

               // read-only instances will never get into this list.
               instance = (EntityEnterpriseContext) instances.get(i);
               instance.getTxAssociation().synchronize(currentThread, tx, instance);
            }
         }
         catch(Exception causeByException)
         {
            // EJB 1.1 section 12.3.2 and EJB 2 section 18.3.3
            // exception during store must log exception, mark tx for
            // rollback and throw a TransactionRolledback[Local]Exception
            // if using caller's transaction.  All of this is handled by
            // the AbstractTxInterceptor and LogInterceptor.
            //
            // All we need to do here is mark the transacction for rollback
            // and rethrow the causeByException.  The caller will handle logging
            // and wraping with TransactionRolledback[Local]Exception.
            try
            {
               tx.setRollbackOnly();
            }
            catch(Exception e)
            {
               log.warn("Exception while trying to rollback tx: " + tx, e);
            }

            // Rethrow cause by exception
            if(causeByException instanceof EJBException)
            {
               throw (EJBException) causeByException;
            }
            throw new EJBException("Exception in store of entity:" +
               ((instance == null) ? "<null>" : instance.getId().toString()), causeByException);
         }
         finally
         {
            SecurityActions.setContextClassLoader(oldCl);
            synchronizing = false;
         }
      }

      // Synchronization implementation -----------------------------

      public void beforeCompletion()
      {
         if(log.isTraceEnabled())
         {
            log.trace("beforeCompletion called for tx " + tx);
         }

         // let the runtime exceptions fall out, so the committer can determine
         // the root cause of a rollback
         synchronize();
      }

      public void afterCompletion(int status)
      {
         //no-op
      }
   }
}
