/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.lock;

import java.util.LinkedList;
import java.util.HashMap;

import javax.transaction.Transaction;
import javax.transaction.Status;

import org.jboss.invocation.Invocation;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;

/**
 * This class is holds threads awaiting the transactional lock to be free
 * in a fair FIFO transactional queue.  Non-transactional threads
 * are also put in this wait queue as well. Unlike SimplePessimisticEJBLock which notifies all
 * threads on transaction completion, this class pops the next waiting transaction from the queue
 * and notifies only those threads waiting associated with that transaction.  This
 * class should perform better than Simple on high contention loads.
 * 
 * Holds all locks for entity beans, not used for stateful. <p>
 *
 * All BeanLocks have a reference count.
 * When the reference count goes to 0, the lock is released from the
 * id -> lock mapping.
 *
 * As of 04/10/2002, you can now specify in jboss.xml method attributes that define
 * methods as read-only.  read-only methods(and read-only beans) will release transactional
 * locks at the end of the invocation.  This decreases likelyhood of deadlock and increases
 * performance.
 *
 * FIXME marcf: we should get solid numbers on this locking, bench in multi-thread environments
 * We need someone with serious SUN hardware to run this lock into the ground
 *
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="pete@subx.com">Peter Murray</a>
 *
 * @version $Revision: 1.10 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/03: billb</b>
 *  <ol>
 *  <li>Initial revision
 *  </ol>
 * <p><b>2002/04/10: billb </b>
 *  <ol>
 *  <li>Applied Peter Murray's read-only locking mechanisms.
 *  </ol>
 */
public class QueuedPessimisticEJBLock extends BeanLockSupport
{
   private Object methodLock = new Object();

   private HashMap txLocks = new HashMap();
   private LinkedList txWaitQueue = new LinkedList();
   private boolean isReadOnlyTxLock = true;

   private int txIdGen = 0;
   private class TxLock
   {

      public Transaction tx = null;
      public int id = 0;
      public String threadName;
      public boolean isQueued;
      public TxLock(Transaction tx)
      {
         this.threadName = Thread.currentThread().toString();
         this.tx = tx;
         if (tx == null)
         {
            if (txIdGen < 0) txIdGen = 0;
            this.id = txIdGen++;
         }
         this.isQueued = true;
      }

      public boolean equals(Object obj)
      {
         if (obj == this) return true;

         TxLock lock = (TxLock)obj;

         if (lock.tx == null && this.tx == null)
         {
            return lock.id == this.id;
         }
         else if (lock.tx != null && this.tx != null)
         {
            return lock.tx.equals(this.tx);
         }
         return false;
      }

      public int hashCode()
      {
         return this.id;
      }

   }

   protected TxLock getTxLock(Transaction miTx)
   {
      TxLock lock = null;
      if (miTx == null)
      {
         // There is no transaction
         lock = new TxLock(null);
         txWaitQueue.addLast(lock);
      }
      else
      {
         TxLock key = new TxLock(miTx);
         lock = (TxLock)txLocks.get(key);
         if (lock == null)
         {
            txLocks.put(key, key);
            txWaitQueue.addLast(key);
            lock = key;
         }
      }
      return lock;
   }

   protected boolean isTxExpired(Transaction miTx) throws Exception
   {
      if (miTx != null && miTx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
      {
         return true;
      }
      return false;
   }


   public void schedule(Invocation mi) throws Exception
   {
      boolean threadScheduled = false;
      while (!threadScheduled)
      {
         /* loop on lock wakeup and restart trying to schedule */
         threadScheduled = doSchedule(mi);
      }
      // Only set isReadOnlyTxLock if there was a transactional lock
      if (mi.getTransaction() != null)
      {
         // Promote the txlock into a writeLock if we're not a readonly method
         // isReadOnlyTxLock will be reset in nextTransaction()
         isReadOnlyTxLock = isReadOnlyTxLock && (((EntityContainer)container).isReadOnly() || container.getBeanMetaData().isMethodReadOnly(mi.getMethod().getName()));
      }
   }
   /**
    * doSchedule(Invocation)
    * 
    * doSchedule implements a particular policy for scheduling the threads coming in. 
    * There is always the spec required "serialization" but we can add custom scheduling in here
    *
    * Synchronizing on lock: a failure to get scheduled must result in a wait() call and a 
    * release of the lock.  Schedulation must return with lock.
    * 
    */
   protected boolean doSchedule(Invocation mi) 
      throws Exception
   {
      boolean wasThreadScheduled = false;
      Transaction miTx = mi.getTransaction();
      boolean trace = log.isTraceEnabled();
      this.sync();
      try
      {
         if( trace ) log.trace("Begin schedule, key="+mi.getId());
  
         if (isTxExpired(miTx))
         {
            log.error("Saw rolled back tx="+miTx);
            throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
         }

         //Next test is independent of whether the context is locked or not, it is purely transactional
         // Is the instance involved with another transaction? if so we implement pessimistic locking
         wasThreadScheduled = waitForTx(miTx, trace);

         // Here, we are trying to get the methodLock on the bean
         try
         {
            boolean acquiredMethodLock = false;
            while (!acquiredMethodLock)
            {
               acquiredMethodLock = attemptMethodLock(mi, trace);
               if (!acquiredMethodLock)
               {
                  if (miTx != null)
                  {
                     // This thread is involved with a transaction
                     // We need to check whether the transaction has timed
                     // out because we may have waited awhile in attemptMethodLock.
                     if (isTxExpired(miTx))
                     {
                        log.error("Saw rolled back tx="+miTx+" waiting for methodLock."
                                  // +" On method: " + mi.getMethod().getName()
                                  // +" txWaitQueue size: " + txWaitQueue.size()
                                  );
                        throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
                     }
                  }
                  else // non-transactional
                  {
                     // we're non-transactional so we must return false
                     // and re-do lock acquisition logic
                     return false;
                  }
               }
            } // end while(acquiredMethodLock)

            // We successfully acquired method lock!
         }
         catch (Exception ex)
         {
            if (miTx != null)
            {
               // we have tx mutex so we must 
               // wakeup next transaction
               nextTransaction();
            }
            throw ex;
         }
      }
      finally
      {
         if (miTx == null // non-transactional
             && wasThreadScheduled) 
         {
            // if this non-transctional thread was
            // scheduled in txWaitQueue, we need to call nextTransaction
            // Otherwise, threads in txWaitQueue will never wake up.
            nextTransaction();
         }
         this.releaseSync();
      }
      
      //If we reach here we are properly scheduled to go through so return true
      return true;
   } 

   /**
    * Wait until no other transaction is running with this lock.
    *
    * @return    Returns true if this thread was scheduled in txWaitQueue
    */
   protected boolean waitForTx(Transaction miTx, boolean trace) throws Exception
   {
      boolean wasScheduled = false;
      boolean addedWaiting = false;
      try
      {
	 // Do we have a running transaction with the context?
	 // We loop here until either until success or until transaction timeout
	 // If we get out of the loop successfully, we can successfully
	 // set the transaction on this puppy.
	 while (this.tx != null &&
		// And are we trying to enter with another transaction?
		!this.tx.equals(miTx))
	 {
	    // For deadlock detection.
	    // miTx is waiting for this.tx to finish so put it
	    // in the waiting table and do deadlock detection.
	    if (miTx != null && !addedWaiting)
	    {
	       synchronized (waiting)
	       {
		  waiting.put(miTx, this.tx);
		  addedWaiting = true;
	       }
	       deadlockDetection(miTx);
	    }
	    wasScheduled = true;
	    // That's no good, only one transaction per context
	    // Let's put the thread to sleep the transaction demarcation will wake them up
	    if( trace ) log.trace("Transactional contention on context"+id);
	    
	    TxLock txLock = getTxLock(miTx);
	    
	    if( trace ) log.trace("Begin wait on Tx="+this.tx);
	    
	    // And lock the threads on the lock corresponding to the Tx in MI
	    synchronized(txLock)
	    {
	       releaseSync(); 
	       try
	       {
		  txLock.wait(txTimeout);
	       } catch (InterruptedException ignored) {}
	    } // end synchronized(txLock)
	    
	    this.sync();
	    
	    if( trace ) log.trace("End wait on TxLock="+this.tx);
	    if (isTxExpired(miTx))
	    {
	       log.error(Thread.currentThread() + "Saw rolled back tx="+miTx+" waiting for txLock"
			 // +" On method: " + mi.getMethod().getName()
			 // +" txWaitQueue size: " + txWaitQueue.size()
			 );
	       if (txLock.isQueued)
	       {
		  // Remove the TxLock from the queue because this thread is exiting.
		  // Don't worry about notifying other threads that share the same transaction.
		  // They will timeout and throw the below RuntimeException
		  txLocks.remove(txLock);
		  txWaitQueue.remove(txLock);
	       }
	       else if (this.tx != null && tx.equals(miTx))
	       {
		  // We're not qu
		  nextTransaction();
	       }
	       throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
	    }
	 } // end while(tx!=miTx)
      }
      finally
      {
	 // miTx is now long waiting for a tx to finish so remove it from the waiting table.
	 if (miTx != null && addedWaiting)
	 {
	    synchronized (waiting)
	    {
	       waiting.remove(miTx);
	    }
	 }
      }

      // If we get here, this means that we have the txlock
      this.tx = miTx;
      return wasScheduled;
   }

   /**
    * Attempt to acquire a method lock.
    */
   protected boolean attemptMethodLock(Invocation mi, boolean trace) throws Exception
   {
      if (isMethodLocked()) 
      {
         // It is locked but re-entrant calls permitted (reentrant home ones are ok as well)
         if (!isCallAllowed(mi)) 
         {
            // This instance is in use and you are not permitted to reenter
            // Go to sleep and wait for the lock to be released
            // Threads finishing invocation will notify() on the lock
            if( trace ) log.trace("Thread contention on methodLock, Begin lock.wait(), id="+mi.getId());
            synchronized(methodLock)
            {
               releaseSync();
               try
               {
                  methodLock.wait(txTimeout);
               }
               catch (InterruptedException ignored) {}
            }
            this.sync();
            if( trace ) log.trace("End lock.wait(), id="+mi.getId()+", isLocked="+isMethodLocked());
            return false;
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
      // if we got here addMethodLock was called
      return true;
   }

   /*
    * nextTransaction()
    *
    * nextTransaction will 
    * - set the current tx to null
    * - schedule the next transaction by notifying all threads waiting on the transaction
    * - setting the thread with the new transaction so there is no race with incoming calls
    */
   protected void nextTransaction() 
   {
      if (!synched)
      {
         throw new IllegalStateException("do not call nextTransaction while not synched!");
      }

      this.tx = null;
      this.isReadOnlyTxLock = true;
      // is there a waiting list?
      if (!txWaitQueue.isEmpty())
      {
         TxLock thelock = (TxLock) txWaitQueue.removeFirst();
         txLocks.remove(thelock);
         thelock.isQueued = false;
         // The new transaction is the next one, important to set it up to avoid race with 
         // new incoming calls
         this.tx = thelock.tx;
         //         log.debug(Thread.currentThread()+" handing off to "+lock.threadName);
         synchronized(thelock) 
         { 
            // notify All threads waiting on this transaction.
            // They will enter the methodLock wait loop.
            thelock.notifyAll();
         }
      }
      else
      {
         //         log.debug(Thread.currentThread()+" handing off to empty queue");
      }
   }
   
   public void endTransaction(Transaction transaction)
   {
      nextTransaction();
   }

   public void wontSynchronize(Transaction trasaction)
   {
      nextTransaction();
   }
   
   /**
    * releaseMethodLock
    *
    * if we reach the count of zero it means the instance is free from threads (and reentrency)
    * we wake up the next thread in the currentLock
    */
   public void endInvocation(Invocation mi)
   { 
      numMethodLocks--;
      if (numMethodLocks == 0)
      {
         synchronized(methodLock) {methodLock.notify();}
         if (isReadOnlyTxLock && mi.getTransaction() != null)
         {
            if (isReadOnlyTxLock)
            {
               endTransaction(mi.getTransaction());
            }
         }
      }
   }
   
   public void removeRef() 
   { 
      refs--;
      if (refs == 0 && txWaitQueue.size() > 0) 
      {
         throw new IllegalStateException("removing bean lock and it has tx's in QUEUE!");
      }
      else if (refs == 0 && this.tx != null) 
      {
         throw new IllegalStateException("removing bean lock and it has tx set!");
      }
      /*      else if (refs == 0)
         log.debug(Thread.currentThread() + " removing lock!");
      */
   }

}

