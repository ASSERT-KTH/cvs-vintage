/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.ejb;


import javax.transaction.Transaction;

import org.jboss.invocation.Invocation;

/**
 * BeanLock interface
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 *
 * @version $Revision: 1.13 $
 *
 * <p><b>Revisions:</b><br>
*  <p><b>2001/07/29: marcf</b>
*  <ol>
*   <li>Initial revision
*  </ol>
*  <p><b>20010802: marcf</b>
*  <ol>
*   <li>Moved to a pluggable framework for the locking policies
*   <li>you specify in jboss.xml what locking-policy you want, eg. pessimistic/optimistic
*   <li>The BeanLock is now an interface and implementations can be found under ejb/plugins/lock
*  </ol>
*/
public interface BeanLock
{
   /**
    *  Get the bean instance cache id for the bean we are locking for.
    *
    *  @return The cache key for the bean instance we are locking for.
    */
   public Object getId();

   /**
    *  Set the bean instance cache id for the bean we are locking for.
    *
    *  @param id The cache key for the bean instance we are locking for.
    */
   public void setId(Object id);

   /**
    *  Change the reentrant flag.
    */
   public void setReentrant(boolean reentrant);

   /**
    *  Change long we should wait for a lock.
    */
   public void setTimeout(int timeout);

   /**
    *  Obtain exclusive access to this lock instance.
    */
   public void sync();

   /**
    *  Release exclusive access to this lock instance.
    */
   public void releaseSync();
	
   /**
    *  This method implements the actual logic of the lock.
    *  In the case of an EJB lock it must at least implement
    *  the serialization of calls 
    *
    *  @param mi The method invocation that needs a lock.
    */
   public void schedule(Invocation mi) 
      throws Exception;
		
   /**
    *  Set the transaction currently associated with this lock.
    *  The current transaction is associated by the schedule call.
    *
    *  @param tx The transaction to associate with this lock.
    */
   public void setTransaction(Transaction tx);

   /**
    *  Get the transaction currently associated with this lock.
    *
    *  @return The transaction currently associated with this lock,
    *          or <code>null</code> if no transaction is currently
    *          associated with this lock.
    */
   public Transaction getTransaction();

   /**
    *  Informs the lock that the given transaction has ended.
    *
    *  @param tx The transaction that has ended.
    */
   public void endTransaction(Transaction tx);
	
   /**
    *  Signifies to the lock that the transaction will not Synchronize
    *  (Tx demarcation not seen).
    *  <p>
    *  OSH: This method does not seem to be called from anywhere.
    *  What is it meant for? To be called on a timeout before the
    *  transaction has terminated?
    */
   public void wontSynchronize(Transaction tx);

   /**
    *  Check if any threads have been allowed into the bean instance
    *  through this lock.
    *
    *  @return <code>true</code> if any thread have been allowed into
    *          the bean instance code through this lock, otherwise
    *          <code>false</code>.
    */
   public boolean isMethodLocked(); 

   /**
    *  Get the number of threads have been allowed into the bean instance
    *  through this lock.
    *
    *  @return The number of threads that have been allowed into
    *          the bean instance code through this lock.
    */
   public int getNumMethodLocks(); 

   /**
    *  Increment the number of locks that have been allowed into the bean
    *  instance code through this lock,
    *
    *  OSH: Should this method really be part of this interface?
    */
   public void addMethodLock(); 

   /**
    *  Release the lock. A thread that got the lock through a successful
    *  call to {@link #schedule} should call this method to release the
    *  lock again.
    *  <p>
    *  If we reach the count of zero it means the instance is free from
    *  threads (and reentrency) we wake up the next thread in the currentLock.
    */
   public void releaseMethodLock();
 
   /**
    *  Increment the reference count of this lock.
    */
   public void addRef();

   /**
    *  Decrement the reference count of this lock.
    */
   public void removeRef();

   /**
    *  Get the current reference count of this lock.
    *
    *  @return The current reference count.
    */
   public int getRefs();
}
