/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.lock;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.HashMap;

import javax.transaction.Transaction;
import javax.transaction.Status;

import org.jboss.invocation.Invocation;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.monitor.LockMonitor;

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
 * @version $Revision: 1.18 $
 */
public class QueuedPessimisticEJBLock extends BeanLockSupport
{
   private HashMap txLocks = new HashMap();
   private LinkedList txWaitQueue = new LinkedList();
   private boolean isReadOnlyTxLock = true;

   private int txIdGen = 0;
   protected LockMonitor lockMonitor = null;

   public void setContainer(Container container) 
   { 
      this.container = container; 
      lockMonitor = container.getLockManager().getLockMonitor();
   }
   private class TxLock
   {

      public Transaction waitingTx = null;
      public int id = 0;
      public String threadName;
      public boolean isQueued;
      public TxLock(Transaction trans)
      {
         this.threadName = Thread.currentThread().toString();
         this.waitingTx = trans;
         if (trans == null)
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

         if (lock.waitingTx == null && this.waitingTx == null)
         {
            return lock.id == this.id;
         }
         else if (lock.waitingTx != null && this.waitingTx != null)
         {
            return lock.waitingTx.equals(this.waitingTx);
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
         Method method = mi.getMethod();
         isReadOnlyTxLock = 
            isReadOnlyTxLock && 
            (
               ((EntityContainer)container).isReadOnly() || 
               (
                  method != null &&
                  container.getBeanMetaData().isMethodReadOnly(method.getName())
                  )
               );

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
         long startWait = System.currentTimeMillis();
         try
         {
            wasThreadScheduled = waitForTx(miTx, trace);
            if (wasThreadScheduled && lockMonitor != null)
            {
               long endWait = System.currentTimeMillis() - startWait;
               lockMonitor.finishedContending(endWait);
            }
         }
         catch (Exception throwable)
         {
            if (lockMonitor != null && isTxExpired(miTx))
            {
               synchronized(lockMonitor)
               {
                  lockMonitor.timeouts++;
               }
            }
            if (lockMonitor != null)
            {
               long endWait = System.currentTimeMillis() - startWait;
               lockMonitor.finishedContending(endWait);
            }
            throw throwable;
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
      // Do we have a running transaction with the context?
      // We loop here until either until success or until transaction timeout
      // If we get out of the loop successfully, we can successfully
      // set the transaction on this puppy.
      while (getTransaction() != null &&
             // And are we trying to enter with another transaction?
             !getTransaction().equals(miTx))
      {
         // For deadlock detection.
         // miTx is waiting for this.tx to finish so put it
         // in the waiting table and do deadlock detection.
         if (miTx != null)
         {
            synchronized (waiting)
            {
               waiting.put(miTx, getTransaction());
            }
         }
         deadlockDetection(miTx);
         wasScheduled = true;
         // That's no good, only one transaction per context
         // Let's put the thread to sleep the transaction demarcation will wake them up
         if( trace ) log.trace("Transactional contention on context"+id);
         
         TxLock txLock = getTxLock(miTx);
         
         if( trace ) log.trace("Begin wait on Tx="+getTransaction());
         
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
         
         if( trace ) log.trace("End wait on TxLock="+getTransaction());
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
            else if (getTransaction() != null && getTransaction().equals(miTx))
            {
               // We're not qu
               nextTransaction();
            }
            if (miTx != null)
            {
               synchronized (waiting)
               {
                  waiting.remove(miTx);
               }
            }
            throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
         }
      } // end while(tx!=miTx)
   
      // If we get here, this means that we have the txlock
      if (!wasScheduled) setTransaction(miTx);
      this.holdingThread = Thread.currentThread();
      return wasScheduled;
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

      setTransaction(null);
      this.holdingThread = null;
      this.isReadOnlyTxLock = true;
      // is there a waiting list?
      if (!txWaitQueue.isEmpty())
      {
         TxLock thelock = (TxLock) txWaitQueue.removeFirst();
         txLocks.remove(thelock);
         thelock.isQueued = false;
         // The new transaction is the next one, important to set it up to avoid race with 
         // new incoming calls
         if (thelock.waitingTx != null)
         {
            synchronized (waiting)
            {
               waiting.remove(thelock.waitingTx);
            }
         }
         setTransaction(thelock.waitingTx);
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
      if (isReadOnlyTxLock && mi.getTransaction() != null)
      {
         if (isReadOnlyTxLock)
         {
            endTransaction(mi.getTransaction());
         }
      }
   }
}

