/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.ejb;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Collections;
import java.lang.reflect.Method;

import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.ejb.EJBObject;

import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.log4j.JBossCategory;

/**
 * Holds all locks for entity beans, not used for stateful. <p>
 *
 * All BeanLocks have a reference count.
 * When the reference count goes to 0, the lock is released from the
 * id -> lock mapping.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 *
 * @version $Revision: 1.5 $
 *
 * <p><b>Revisions:</b><br>
*  <p><b>2001/07/29: billb</b>
*  <ol>
*   <li>Initial revision
* </ol>
*  <p><b>2001/08/01: marcf</b>
*  <ol>
*   <li>Added the schedule method 
*   <li>The bean lock is now responsible for implementing the locking policies, it was before in the 
*   interceptor code it is now factored out allowing for pluggable lock policies (optimistic for ex) 
*   <li>Implemented pessimistic locking and straight spec requirement in the schedule method
*   would need to factor this in an abstract class for the BeanLock that extending policies can use
* </ol>
 */
public class BeanLock
{
   /** The actual lock object **/
   private Object lock = new Object();
 
   /** number of threads invoking methods on this bean (1 normally >1 if reentrant) **/
   private int numMethodLocks = 0;
   /** number of threads that retrieved this lock from the manager (0 means removing) **/ 
   private int refs = 0;
 
   /**The Cachekey corresponding to this Bean */
   private Object id = null;
 
   /**Are reentrant calls allowed? */
   private boolean reentrant;
 
   /** Use a JBoss custom log4j category for trace level logging */
   static JBossCategory log = (JBossCategory) JBossCategory.getInstance(BeanLock.class);
 
   private Transaction tx = null;
 
   private boolean synched = false;

   private int txTimeout;
 
   public BeanLock(Object id, boolean reentrant, int txTimeout)
   {
      this.id = id;
      this.reentrant = reentrant;
      this.txTimeout = txTimeout;
   }
 
   public Object getId()
   {
      return id;
   }
 
   public void sync()
   {
      synchronized(this)
      {
         while(synched)
         {
            try
            {
               this.wait();
            }
            catch (InterruptedException ex) { /* ignore */ }
         }
         synched = true;
      }
   }
 
   public void releaseSync()
   {
      synchronized(this)
      {
         synched = false;
         this.notify();
      }
   }
 
   /**
    * Schedule(MethodInvocation)
    * 
    * Schedule implements a particular policy for scheduling the threads coming in. 
    * There is always the spec required "serialization" but we can add custom scheduling in here
    *
    * Synchronizing on lock: a failure to get scheduled must result in a wait() call and a 
    * release of the lock.  Schedulation must return with lock.
    * 
    * @return boolean returns true if the thread is scheduled to go through the rest of the 
    *  interceptors.  Returns false if the interceptor must try the scheduling again. 
    */
   public boolean schedule(MethodInvocation mi) 
      throws Exception
   {
      this.sync();
      boolean syncAlreadyReleased = false;
      try
      {
         
         Transaction miTx = mi.getTransaction();
         boolean trace = true;//log.isTraceEnabled();
  
         if( trace ) log.trace("Begin schedule, key="+mi.getId());
  
         // Maybe my transaction already expired? 
         if (miTx != null && miTx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         {
            log.error("Saw rolled back tx="+miTx);
	         throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
         }
  
         //Next test is independent of whether the context is locked or not, it is purely transactional
         // Is the instance involved with another transaction? if so we implement pessimistic locking
  
         // Do we have a running transaction with the context?
         if (tx != null &&
             // And are we trying to enter with another transaction?
             !tx.equals(miTx))
         {
            // That's no good, only one transaction per context
            // Let's put the thread to sleep the transaction demarcation will wake them up
            if( trace ) log.trace("Transactional contention on context"+id);
    
            try
            {
               if( trace ) log.trace("Begin wait on Tx="+tx);
     
               // And lock the threads on the lock corresponding to the Tx in MI
               synchronized(lock)
               {
                  // We are going to wait on one of the implementation locks therefore
                  // we need to release the sync on the BeanLock object itself
                  syncAlreadyReleased = true;
                  
						//In case the thread was the only one alive
						if (numMethodLocks <= 0) lock.notify();
						
						releaseSync(); 
						
						//Wait a thread coming out will wake you up
						//lock.wait(txTimeout);
                  lock.wait(5000);
              
				   }
               if( trace ) log.trace("End wait on TxLock="+tx);
            }
   
            // We need to try again
            finally {return false;}
         }
  
         // The following code should really be done in a superclass of the lock
         // it implements the default serialization from the specification
  
         // The next test is the pure serialization from the EJB specification.
         // If we are here we either did not have a tx(tx == null) or this is a
         // recursive call and the current ctx.tx == mi.tx
  
         // The transaction is good and current.
         
         //Is the context used already (with or without a transaction), is it locked?
         if (isMethodLocked()) 
         {
            // It is locked but re-entrant calls permitted (reentrant home ones are ok as well)
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
                     // we're about to wait on a different object, so release synch on beanlock
                     // Also, this notify should always be within the synch(lock) block
                     syncAlreadyReleased = true;
                     releaseSync();
      					if (numMethodLocks <= 0) lock.notify();
                     //lock.wait(txTimeout);
                     lock.wait(5000);
						}
               }
    
               catch (InterruptedException ignored) {}     
               // We need to try again
               finally
               {
                  if( trace ) log.trace("End lock.wait(), id="+mi.getId()+", isLocked="+isMethodLocked());
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
      
      //If we reach here we are properly scheduled to go through so return true
      return true;
   } 
 
   /**
    * setTransaction(Transaction tx)
    * 
    * The setTransaction associates a transaction with the lock.  The current transaction is associated
    * by the schedule call.  
    */
   public void setTransaction(Transaction tx){this.tx = tx;}
   public Transaction getTransaction(){return tx;}
   
   
   public boolean isMethodLocked() { return numMethodLocks > 0;}
   public int getNumMethodLocks() { return numMethodLocks;}
   public void addMethodLock() { numMethodLocks++; }
   /**
    * releaseMethodLock
    *
    * if we reach the count of zero it means the instance is free from threads (and reentrency)
    * we wake up the next thread in the currentLock
    */
   public void releaseMethodLock() 
   { 
      numMethodLocks--;
      
		//Wake up a thread to do work on the instance within the current transaction
      synchronized(lock) {lock.notify();}
      
   }
   
   public void addRef() { refs++;}
   public void removeRef() { refs--;}
   public int getRefs() { return refs;}
   
   // Private --------------------------------------------------------
   
   private static Method getEJBHome;
   private static Method getHandle;
   private static Method getPrimaryKey;
   private static Method isIdentical;
   private static Method remove;
   
   static
   {
      try
      {
         Class[] noArg = new Class[0];
         getEJBHome = EJBObject.class.getMethod("getEJBHome", noArg);
         getHandle = EJBObject.class.getMethod("getHandle", noArg);
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", noArg);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] {EJBObject.class});
         remove = EJBObject.class.getMethod("remove", noArg);
      }
      catch (Exception x) {x.printStackTrace();}
   }
   
   private boolean isCallAllowed(MethodInvocation mi)
   {
      if (reentrant)
      {
         return true;
      }
      else
      {
         Method m = mi.getMethod();
         if (m.equals(getEJBHome) ||
             m.equals(getHandle) ||
             m.equals(getPrimaryKey) ||
             m.equals(isIdentical) ||
             m.equals(remove))
         {
            return true;
         }
      }
      
      return false;
   }
}

