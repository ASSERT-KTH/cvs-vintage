/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.ejb.plugins.lock;


import javax.transaction.Transaction;

import org.jboss.ejb.MethodInvocation;

/**
 * This class does not perform any pessimistic transactional locking. Only locking
 * on single-threaded non-reentrant beans.
 * 
 * Holds all locks for entity beans, not used for stateful. <p>
 *
 * All BeanLocks have a reference count.
 * When the reference count goes to 0, the lock is released from the
 * id -> lock mapping.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 *
 * @version $Revision: 1.3 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/08: billb</b>
 *  <ol>
 *  <li>Initial revision
 *  </ol>
 */
public class MethodOnlyEJBLock extends QueuedPessimisticEJBLock
{
   /**
    * Schedule(MethodInvocation)
    * 
    * Schedule implements a particular policy for scheduling the threads coming in. 
    * There is always the spec required "serialization" but we can add custom scheduling in here
    *
    * Synchronizing on lock: a failure to get scheduled must result in a wait() call and a 
    * release of the lock.  Schedulation must return with lock.
    * 
    */
   public void schedule(MethodInvocation mi) 
      throws Exception
   {
      Transaction miTx = mi.getTransaction();
      boolean trace = log.isTraceEnabled();
      this.sync();
      try
      {
         if( trace ) log.trace("Begin schedule, key="+mi.getId());
  
         boolean acquiredMethodLock = false;
         while (!acquiredMethodLock)
         {
            if (isTxExpired(miTx))
            {
               log.error("Saw rolled back tx="+miTx);
               throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
            }
            acquiredMethodLock = attemptMethodLock(mi, trace);
         }
      }
      finally
      {
         this.releaseSync();
      }
      
      //If we reach here we are properly scheduled to go through
   } 

   public void endTransaction(Transaction transaction)
   {
      // complete
   }

   public void wontSynchronize(Transaction trasaction)
   {
      // complete
   }
   
}

