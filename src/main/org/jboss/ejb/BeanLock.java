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
 * BeanLock interface
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 *
 * @version $Revision: 1.7 $
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
   public Object getId();
	public void setId(Object id);
	public void setReentrant(boolean reentrant);
	public void setTimeout(int timeout);
	 
   public void sync();
   public void releaseSync();
	
	/*
	* schedule(MethodInvocation)
	* 
	* implements the actual logic of the lock.  In the case of an EJB lock must at least implement
	* the serialization of calls 
	*/
   public boolean schedule(MethodInvocation mi) 
      throws Exception;
		
   /**
    * setTransaction(Transaction tx)
    * 
    * The setTransaction associates a transaction with the lock.  
	 */
   public void setTransaction(Transaction tx);
   public Transaction getTransaction();
	
	// Signifies to the lock of the transaction boundary (Tx demarcation seen)
	public void endTransaction(Transaction tx);
	
	//Signifies to the lock that the transaction will not Synchronize (Tx demarcation not seen) 
	public void wontSynchronize(Transaction tx);
	
	/*encapsulates notify()*/
	public void notifyOne();
	/*encapsulates notifyAll()*/
	public void notifyEveryone();
	
	public Object getLock();
   
   
   public boolean isMethodLocked(); 
   public int getNumMethodLocks(); 
   public void addMethodLock(); 
   /**
    * releaseMethodLock
    *
    * if we reach the count of zero it means the instance is free from threads (and reentrency)
    * we wake up the next thread in the currentLock
    */
   public void releaseMethodLock();
   
   public void addRef() ;
   public void removeRef() ;
   public int getRefs() ;
}
