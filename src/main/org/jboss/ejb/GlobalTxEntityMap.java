/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import javax.ejb.EJBException;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import java.util.Iterator;
import java.util.Collection;
import org.jboss.logging.Logger;
import java.util.Map;

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
 * @version $Revision: 1.4 $
 */
public class GlobalTxEntityMap
{

   private final Logger log = Logger.getLogger(getClass());

   protected final Map m_map = new HashMap();
    
   /**
    * associate entity with transaction
    */
   public synchronized void associate(
         Transaction tx,
         EntityEnterpriseContext entity)
      throws RollbackException, SystemException
   {
      ArrayList entityList = (ArrayList)m_map.get(tx);
      if (entityList == null)
      {
         entityList = new ArrayList();
         m_map.put(tx, entityList);
         tx.registerSynchronization(new GlobalTxEntityMapCleanup(this, tx));
      }
      if (!entityList.contains(entity))
      {
         entityList.add(entity);
      }
   }

   /**
    * sync all EntityEnterpriseContext that are involved (and changed)
    * within a transaction.
    */
   public void syncEntities(Transaction tx) 
   {
      Collection entities = null;
      synchronized (m_map)
      {
         entities = (Collection)m_map.remove(tx);
      }

      // if there are there no entities associated with this tx we are done
      if (entities == null) 
      {
         return;
      }

      // This is an independent point of entry. We need to make sure the
      // thread is associated with the right context class loader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
 
      try
      {
         EntityEnterpriseContext ctx = null;
         try
         {
            for (Iterator i = entities.iterator(); i.hasNext(); )
            {
               // any one can mark the tx rollback at any time so check
               // before continuing to the next store
               if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) 
               {
                  // nothing else to do here
                  return;
               } 
                  
               // read-only entities will never get into this list.
               ctx = (EntityEnterpriseContext)i.next();
               EntityContainer container = (EntityContainer)ctx.getContainer();

               // set the context class loader before calling the store method
               Thread.currentThread().setContextClassLoader(
                     container.getClassLoader());

               // store it
               container.storeEntity(ctx);
            }
         }
         catch (Exception e)
         {
            // EJB 1.1 section 12.3.2 and EJB 2 section 18.3.3
            // exception during store must log exception, mark tx for 
            // rollback and throw a TransactionRolledback[Local]Exception
            // if using caller's transaction.  All of this is handled by 
            // the AbstractTxInterceptor and LogInterceptor.
            //
            // however we may need to ignore a NoSuchEntityException -- TODO
            //
            throw new EJBException("Exception in store of entity:" + ctx.id, e);
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }




   /**
    * Store changed entities to resource manager in this Synchronization
    */
   private class GlobalTxEntityMapCleanup implements Synchronization
   {
      GlobalTxEntityMap map;
      Transaction tx;

      public GlobalTxEntityMapCleanup(
            GlobalTxEntityMap map,
            Transaction tx)
      {
         this.map = map;
         this.tx = tx;
      }

      // Synchronization implementation -----------------------------
  
      public void beforeCompletion()
      {
         if (log.isTraceEnabled()) 
         {
            log.trace("beforeCompletion called for tx " + tx);
         }

         try
         {
            syncEntities(tx);
         }
         catch (Exception e)
         {
            log.error("Entity synchronization failed", e);
            try
            {
               // rolback the tx
               tx.setRollbackOnly();
            }
            catch (SystemException ex)
            {
               log.warn("SystemException while trying to rollback tx: " 
                     + tx, ex);
            }
            catch (IllegalStateException ex)
            {
               log.warn("IllegalStateException while trying to rollback tx: " +
                     tx, ex);
            }
         }
      }
  
      public void afterCompletion(int status)
      {
         //no-op
      }
   }
}
