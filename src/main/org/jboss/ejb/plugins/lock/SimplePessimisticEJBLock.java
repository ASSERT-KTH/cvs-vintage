/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.lock;


import javax.transaction.Transaction;
import javax.transaction.Status;

import org.jboss.invocation.Invocation;

/**
 * Holds all locks for entity beans, not used for stateful.
 *
 * <p>All BeanLocks have a reference count.
 *    When the reference count goes to 0, the lock is released from the
 *    id -> lock mapping.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.8 $
 *
 * <p><b>Revisions:</b><br>
 *  <p><b>2001/07/29: billb</b>
 *  <ol>
 *   <li>Initial revision
 * </ol>
 *  <p><b>2001/08/01: marcf</b>
 *  <ol>
 *   <li>Added the schedule method 
 *   <li>The bean lock is now responsible for implementing the locking
 *   policies, it was before in the interceptor code it is now factored
 *   out allowing for pluggable lock policies (optimistic for ex) 
 *   <li>Implemented pessimistic locking and straight spec requirement in the
 *   schedule method would need to factor this in an abstract class for the
 *   BeanLock that extending policies can use
 * </ol>
 *  <p><b>2001/08/02: marcf</b>
 *  <ol>
 *   <li>Did what was said above, moved to an extension based mech with an
 *   abstract base class.
 *   <li>This is the simple lock, won't scale well (imho) but is robust in
 *   normal operation
 *   <li>The class must now implement schedule and the various notification
 *   calls.  EndTransaction wontSynchronize can be radically different (N-Lock)
 * </ol>
 */
public class SimplePessimisticEJBLock
   extends BeanLockSupport  
{
   /** The actual lock object **/
   public Object lock = new Object();
 
   public Object getLock() {return lock;}
	
   public void schedule(Invocation mi) throws Exception
   {
      boolean threadScheduled = false;
      while (!threadScheduled)
      {
         /* loop on lock wakeup and restart trying to schedule */
         threadScheduled = doSchedule(mi);
      }
   }
   /**
    * doSchedule implements a particular policy for scheduling the threads
    * coming in. There is always the spec required "serialization" but we can
    * add custom scheduling in here
    *
    * Synchronizing on lock: a failure to get scheduled must result in a
    * wait() call and a release of the lock.  Schedulation must return
    * with lock.
    * 
    * @return    Returns true if the thread is scheduled to go through the 
    *            rest of the interceptors.  Returns false if the interceptor
    *            must try the scheduling again. 
    */
   protected boolean doSchedule(Invocation mi) 
      throws Exception
   {
      this.sync();
      boolean syncAlreadyReleased = false;
      try
      {
         
         Transaction miTx = mi.getTransaction();
         boolean trace = log.isTraceEnabled();
  
         if( trace ) log.trace("Begin schedule, key="+mi.getId());
  
         // Maybe my transaction already expired? 
         if (miTx != null && miTx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         {
            log.error("Saw rolled back tx="+miTx);
				
            // Wake up the next ones, this won't scale, under stress we will
            // wake everyone... 
            // synchronized(lock) {lock.notifyAll();}
				
            //and get out of here
            throw new RuntimeException
               ("Transaction marked for rollback, possibly a timeout");
         }
  
         // Next test is independent of whether the context is locked or not,
         // it is purely transactional.  Is the instance involved with another
         // transaction? if so we implement pessimistic locking
  
         // Do we have a running transaction with the context?
         if (tx != null &&
             // And are we trying to enter with another transaction?
             !tx.equals(miTx))
         {
            // That's no good, only one transaction per context
            // Let's put the thread to sleep the transaction demarcation will
            // wake them up
            if( trace ) log.trace("Transactional contention on context"+id);
    
            try
            {
               if( trace ) log.trace("Begin wait on Tx="+tx);
                 
               // And lock the threads on the lock corresponding to the Tx
               // in MI
               synchronized(lock)
               {
                  // We are going to wait on one of the implementation locks
                  // therefore we need to release the sync on the BeanLock
                  // object itself
                  syncAlreadyReleased = true;
                  
                  releaseSync(); 
						
                  //Wait a thread coming out will wake you up
                  lock.wait(txTimeout);
                  //lock.wait(5000);
              
               }
               if( trace ) log.trace("End wait on TxLock="+tx);
            }
   
            // We need to try again
            finally {return false;}
         }
  
         // The following code should really be done in a superclass of the 
         // lock it implements the default serialization from the specification
  
         // The next test is the pure serialization from the EJB specification.
         // If we are here we either did not have a tx(tx == null) or this is a
         // recursive call and the current ctx.tx == mi.tx
  
         // The transaction is good and current.
         
         // Is the context used already (with or without a transaction),
         // is it locked?
         if (isMethodLocked()) 
         {
            // It is locked but re-entrant calls permitted (reentrant
            // home ones are ok as well)
            if (!isCallAllowed(mi)) 
            {
               // This instance is in use and you are not permitted to reenter
               // Go to sleep and wait for the lock to be released
               if( trace ) log.trace("Thread contention on lock"+id);
     
               // Threads finishing invocation will notify() on the lock
               try
               {
     
                  if( trace ) log.trace("Begin lock.wait(), id="+mi.getId());
                 
                  synchronized(lock)
                  {
                     // we're about to wait on a different object, so release
                     // synch on beanlock.  Also, this notify should always be
                     // within the synch(lock) block
                     syncAlreadyReleased = true;
                     releaseSync();
							
                     lock.wait(txTimeout);
                     //lock.wait(5000);
                  }
               }
    
               catch (InterruptedException ignored) {}     
               // We need to try again
               finally
               {
                  if( trace ) {
                     log.trace("End lock.wait(), id="+mi.getId()+
                               ", isLocked="+isMethodLocked());
                  }
                  return false;
               }
            }
            else
            { 
               //We are in a valid reentrant call so add a method lock
               addMethodLock();
            }
         }
         // No one is using that instance
         else 
         {
            // We are now using the instance
            addMethodLock();
         }
  
         // keep track of the transaction in the lock
         tx = miTx;
      }
      finally
      {
         if (!syncAlreadyReleased) this.releaseSync();
      }
      
      // If we reach here we are properly scheduled to go
      // through so return true
      return true;
   } 
   
   /**
    * This is called if the synchronization missed registration
    * (Sync interceptor).
    */
   public void wontSynchronize(Transaction transaction) 
   {
      tx = null;
		
      synchronized(lock) {lock.notifyAll();}
   }
	
   /**
    * This is called up synchronization to notify the end of the transaction.
    */
   public void endTransaction(Transaction transaction) 
   {
      //The tx is done
      tx = null;
		
      synchronized(lock) {lock.notifyAll();}
   }
	
	
   /**
    * if we reach the count of zero it means the instance is free from threads
    * (and reentrency) we wake up the next thread in the currentLock
    */
   public void releaseMethodLock() 
   { 
      numMethodLocks--;
	   
      // Wake up a thread to do work on the instance within the current
      // transaction
      if (numMethodLocks ==0) synchronized(lock) {lock.notifyAll();}
   }

}
