/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.HashMap;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;

/**
 * This class provides a way to find out what entities are contained in
 * what transaction.  It is used, to find which entities to call ejbStore()
 * on when a ejbFind() method is called within a transaction. EJB 2.0- 9.6.4
 * Used in EntitySynchronizationInterceptor.
 * 
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.4 $
 *
 * Revisions:
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/06: marcf</b>
 * <ol>
 *   <li>Got rid of disassociate and added a javax.transaction.Synchronization.  The sync will clean up the map now.
 *   <li>This class now interacts with ApplicationTxEntityMap available in Application
 * </ol>
 */
public class TxEntityMap
{
   protected HashMap m_map = new HashMap();
    
   /**
    * associate entity with transaction
    */
   public synchronized void associate(Transaction tx,
                                      EntityEnterpriseContext entity) throws RollbackException, SystemException
   {
      HashMap entityMap = (HashMap)m_map.get(tx);
      if (entityMap == null)
      {
         entityMap = new HashMap();
         m_map.put(tx, entityMap);
         tx.registerSynchronization(new TxEntityMapCleanup(this, tx));
      }
      entity.getContainer().getApplication().getTxEntityMap().associate(tx, entity);
      entityMap.put(entity.getCacheKey(), entity);
   }

   public synchronized EntityEnterpriseContext getCtx(Transaction tx,
                                                      CacheKey key)
   {
      HashMap entityMap = (HashMap)m_map.get(tx);
      return (EntityEnterpriseContext)entityMap.get(key);
   }

   private class TxEntityMapCleanup implements Synchronization
   {
      TxEntityMap map;
      Transaction tx;

      public TxEntityMapCleanup(TxEntityMap map,
                                Transaction tx)
      {
         this.map = map;
         this.tx = tx;
      }

      // Synchronization implementation -----------------------------
  
      public void beforeCompletion()
      {
         /* complete */
      }
  
      public void afterCompletion(int status)
      {
         synchronized(map)
         {
            HashMap entityMap = (HashMap)m_map.remove(tx);
            if (entityMap != null)
            {
               entityMap.clear();
            }
         }
      }
   }
}
