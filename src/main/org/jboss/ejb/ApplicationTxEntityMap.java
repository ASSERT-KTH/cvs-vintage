/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;


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
 * @version $Revision: 1.2 $
 */
public class ApplicationTxEntityMap
{
   protected HashMap m_map = new HashMap();
    
   /**
    * associate entity with transaction
    */
   public synchronized void associate(Transaction tx,
                                      EntityEnterpriseContext entity)
      throws RollbackException, SystemException
   {
      ArrayList entityList = (ArrayList)m_map.get(tx);
      if (entityList == null)
      {
         entityList = new ArrayList();
         m_map.put(tx, entityList);
         tx.registerSynchronization(new ApplicationTxEntityMapCleanup(this, tx));
      }
      entityList.add(entity);
   }

   /**
    * get all EntityEnterpriseContext that are involved with a transaction.
    */
   public synchronized EntityEnterpriseContext[] getEntities(Transaction tx)
   {
      ArrayList entityList = (ArrayList)m_map.get(tx);
      if (entityList == null) // there are no entities associated
      {
         return new EntityEnterpriseContext[0];
      }
      return (EntityEnterpriseContext[])
         entityList.toArray(new EntityEnterpriseContext[entityList.size()]);
   }

   private class ApplicationTxEntityMapCleanup implements Synchronization
   {
      ApplicationTxEntityMap map;
      Transaction tx;

      public ApplicationTxEntityMapCleanup(ApplicationTxEntityMap map,
                                           Transaction tx)
      {
         this.map = map;
         this.tx = tx;
      }

      // Synchronization implementation -----------------------------
  
      public void beforeCompletion()
      {
         // complete
      }
  
      public void afterCompletion(int status)
      {
         synchronized(map)
         {
            ArrayList entityList = (ArrayList)m_map.remove(tx);
            if (entityList != null)
            {
               entityList.clear();
            }
         }
      }
   }
   
}
