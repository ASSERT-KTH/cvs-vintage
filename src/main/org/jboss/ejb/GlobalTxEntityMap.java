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
import java.util.Iterator;
import java.util.Collection;
import org.jboss.logging.Logger;
import java.util.Map;
import javax.transaction.TransactionRolledbackException;

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
public class GlobalTxEntityMap
{

   private final Logger log = Logger.getLogger(getClass());

   protected final Map m_map = new HashMap();
    
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
      throws TransactionRolledbackException
   {
      Collection entities = null;
      synchronized (m_map)
      {
	 entities = (Collection)m_map.remove(tx);
      }
      if (entities != null) // there are entities associated
      {
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
		  //read-only will never get into this list.
		  ctx = (EntityEnterpriseContext)i.next();
		  EntityContainer container = (EntityContainer)ctx.getContainer();
		  Thread.currentThread().setContextClassLoader(container.getClassLoader());
		  container.storeEntity(ctx);
	       }
	    }
	    catch (Exception e)
	    {
	       /*ejb 1.1 section 12.3.2
		*ejb 2 section 18.3.3
		*exception during store must log exception,
		* mark tx for rollback and throw 
		* a TransactionRolledBack[Local]Exception
		*/

	       //however we may need to ignore a NoSuchEntityException -- TODO
	       log.error("Store failed on entity: " + ctx.getId(), e);
	       try
	       {
		  tx.setRollbackOnly();
	       }
	       catch(SystemException se)
	       {
		  log.warn("SystemException while trying to rollback tx: " + tx, se);
	       }
	       catch (IllegalStateException ise)
	       {
		  log.warn("IllegalStateException while trying to rollback tx: " + tx, ise);
	       }
	       finally
	       {
		  //How do we distinguish local/remote??
		  throw new TransactionRolledbackException("Exception in store of entity :" + ctx.id);
	       }
	    }
	 }
	 finally
         {
	    Thread.currentThread().setContextClassLoader(oldCl);
	 }

      }
   }




   /**
    * Store changed entities to resource manager in this Synchronization
    */
   private class GlobalTxEntityMapCleanup implements Synchronization
   {
      GlobalTxEntityMap map;
      Transaction tx;

      public GlobalTxEntityMapCleanup(GlobalTxEntityMap map,
                                      Transaction tx)
      {
         this.map = map;
         this.tx = tx;
      }

      // Synchronization implementation -----------------------------
  
      public void beforeCompletion()
      {
         // complete
         boolean trace = log.isTraceEnabled();
         if( trace )
            log.trace("beforeCompletion called for tx " + tx);
	 try
	 {
	    syncEntities(tx);
	 }
	 catch (Exception e)
	 {//ignore - we can't throw any exceptions, it is already logged
	 }

      }
  
      public void afterCompletion(int status)
      {
	 //no-op
      }
   }
   
}
