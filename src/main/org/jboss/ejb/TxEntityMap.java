/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.HashMap;
import javax.transaction.Transaction;

/**
 * This class provides a way to find out what entities are contained in
 * what transaction.  It is used, to find which entities to call ejbStore()
 * on when a ejbFind() method is called within a transaction. EJB 2.0- 9.6.4
 * Used in EntitySynchronizationInterceptor.
 * 
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.2 $
 */
public class TxEntityMap
{
   protected HashMap m_map = new HashMap();
    
   /**
    * associate entity with transaction
    */
   public synchronized void associate(Transaction tx,
                                      EntityEnterpriseContext entity)
   {
      HashMap entityMap = (HashMap)m_map.get(tx);
      if (entityMap == null)
      {
         entityMap = new HashMap();
         m_map.put(tx, entityMap);
      }
      entityMap.put(entity.getCacheKey(), entity);
   }

   /**
    * Disassociate entity with transaction.  When the transaction has no
    * more entities.  it is removed from this class's internal HashMap.
    */
   public synchronized void disassociate(Transaction tx,
                                         EntityEnterpriseContext ctx)
   {
      HashMap entityMap = (HashMap)m_map.get(tx);
      if (entityMap == null)
      {
         return;
      }
      entityMap.remove(ctx.getCacheKey());
      
      // When all entities are gone, cleanup!
      // cleanup involves removing the transaction
      // from the map
      if (entityMap.size() <= 0)
      {
         m_map.remove(tx);
      }
   }
	
   /**
    * get all EntityEnterpriseContext that are involved with a transaction.
    */
   public synchronized Object[] getEntities(Transaction tx)
   {
      HashMap entityMap = (HashMap)m_map.get(tx);
      if (entityMap == null) // there are no entities associated
      {
         return new Object[0];
      }
      return entityMap.values().toArray();
   }
}
