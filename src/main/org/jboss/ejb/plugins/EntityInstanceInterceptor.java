/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.CacheKey;
import org.jboss.metadata.EntityMetaData;
import org.jboss.logging.Logger;
import org.jboss.util.Sync;

/**
*
*	 <p>The instance interceptors role is to acquire a context representing the target object from the 
*   cache.
*
*   <p>This particular container interceptor implements pessimistic locking on the transaction that 
*   is associated with the retrieved instance.  If there is a transaction associated with the 
*   target component and it is different from the transaction associated with the MethodInvocation
*   coming in then the policy is to wait for transactional commit. 
*   
*   <p>We also implement serialization of calls in here (this is a spec requirement).
*   This is a fine grained notify, notifyAll mechanism. We notify on ctx serialization locks and 
*   notifyAll on global transactional locks 
*   
*   <p><b>WARNING: critical code</b>, get approval from senior developers before changing.
*    
*
*   @see <related>
*   @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.32 $
*
*   <p><b>Revisions:</b><br>
*   <p><b>2001/06/28: marcf</b>
*   <ol>
*   <li>Moved to new synchronization
*   <li>Pools are gone simple design
*   <li>two levels of syncrhonization with Tx and ctx
*   <li>remove busy wait from previous mechanisms
*   </ol>
*   
*/
public class EntityInstanceInterceptor
extends AbstractInterceptor
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	protected EntityContainer container;
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	public void setContainer(Container container)
	{
		this.container = (EntityContainer)container;
	}
	
	public  Container getContainer()
	{
		return container;
	}
	
	// Interceptor implementation --------------------------------------
	public Object invokeHome(MethodInvocation mi)
	throws Exception
	{
		// Get context
		EnterpriseContext ctx = ((EntityContainer)getContainer()).getInstancePool().get();
		
		// Pass it to the method invocation
		mi.setEnterpriseContext(ctx);
		
		// Give it the transaction
		ctx.setTransaction(mi.getTransaction());
		
		// This context is brand new. We can lock without more "fuss" 
		// The reason we need to lock it is that it will be put in cache before the end
		// of the call.  So another thread could access it before we are done.
		
		ctx.lock();
		
		try
		{
			// Invoke through interceptors
			return getNext().invokeHome(mi);
		} 
		finally
		{
			//Other threads can be coming for this instance if it is in cache
			synchronized(ctx) {
				// Always unlock, no matter what
				ctx.unlock();
				// Wake everyone up in case of create with Tx contention
				ctx.notifyAll();
			}
			//Do not send back to pools in any case, let the instance be GC'ed
		}
	}
	
	public Object invoke(MethodInvocation mi)
	throws Exception
	{
		// It's all about the context
		EntityEnterpriseContext ctx = null;
		
		// And it must correspond to the key.
		CacheKey key = (CacheKey) mi.getId();
		
		while (ctx == null)
		{
			ctx = (EntityEnterpriseContext) container.getInstanceCache().get(key);
			
			//Next test is independent of whether the context is locked or not, it is purely transactional
			// Is the instance involved with another transaction? if so we implement pessimistic locking
			synchronized(ctx.getTxLock()) 
			{
				Transaction tx = ctx.getTransaction();
				
				// Do we have a running transaction with the context?
				if (tx != null &&
					// And are we trying to enter with another transaction?
					!tx.equals(mi.getTransaction()))
				{
					// That's no good, only one transaction per context
					// Let's put the thread to sleep the transaction demarcation will wake them up
					Logger.debug("Transactional contention on context"+ctx.getId());
					
					// Wait for it to finish, note that we use wait() and not wait(5000), why? 
					// cause we got cojones, if there a problem in this code we want a freeze not illusion
					// Threads finishing the transaction must notifyAll() on the ctx.txLock
					try {ctx.getTxLock().wait();}
					
					// We need to try again
					finally {ctx = null; continue;}
				}
				
				/*
				In future versions we can use copies of the instance per transaction
				*/
			}
			
			// The next test is the pure serialization from the EJB specification.  
			synchronized(ctx) 
			{
				// synchronized is a time gap, when the thread enters here it can be after "sleep"
				// we need to make sure that stuff is still kosher
				
				// Maybe my transaction already expired?
				if (mi.getTransaction() != null && mi.getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK)
					throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
				
				// We do not use pools any longer so the only thing that can happen is that 
				// a ctx has a null id (instance removed) (no more "wrong id" problem)
				if (ctx.getId() == null) 
				{
					// This will happen when the instance is removed from cache 
					// We need to go through the same mechs and get the new ctx
					ctx = null;
					continue;
				}
				// So the ctx is still valid and the transaction is still game and we own the context
				// so no one can change ctx.  Make sure that all access to ctx is synchronized.
				
				// The ctx is still kosher go on with the real serialization
				
				//Is the context used already (with or without a transaction), is it locked?
				if (ctx.isLocked()) 
				{
					// It is locked but re-entrant calls permitted (reentrant home ones are ok as well)
					if (!isCallAllowed(mi)) 
					{
						// This instance is in use and you are not permitted to reenter
						// Go to sleep and wait for the lock to be released
						Logger.debug("Thread contention on context"+key);
						
						// we want to know about freezes so we wait(), let us know if this locks  
						// Threads finishing invocation will come here and notify() on the ctx
						try {ctx.wait();}
						catch (InterruptedException ignored) {}					
						// We need to try again
						finally {ctx = null; continue;}
					}
					else
					{
						//We are in a valid reentrant call so take the lock, take it!
						ctx.lock();
					}
				}
				// No one is using that context
				else 
				{
					
					// We are now using the context
					ctx.lock(); 
				}
				
				// The transaction is associated with the ctx while we own the lock 
				ctx.setTransaction(mi.getTransaction());
			
			}// end sychronized(ctx)
		
		}
		
		// Set context on the method invocation
		mi.setEnterpriseContext(ctx);
		
		boolean exceptionThrown = false;
		
		try {
			
			// Go on, you won
			return getNext().invoke(mi);
		
		}
		catch (RemoteException e)
		{
			exceptionThrown = true;
			throw e;
		} catch (RuntimeException e)
		{
			exceptionThrown = true;
			throw e;
		} catch (Error e)
		{
			exceptionThrown = true;
			throw e;
		} 
		finally
		{
			// ctx can be null if cache.get throws an Exception, for
			// example when activating a bean.
			if (ctx != null)
			{
				
				synchronized(ctx) 
				{
					ctx.unlock();
					
					Transaction tx = ctx.getTransaction();
					
					// If an exception has been thrown, 
					if (exceptionThrown && 					
						// if tx, the ctx has been registered in an InstanceSynchronization. 
						// that will remove the context, so we shouldn't.
					   // if no synchronization then we need to do it by hand
						!ctx.isTxSynchronized()) 
					{
						// Discard instance
						// EJB 1.1 spec 12.3.1
						container.getInstanceCache().remove(key);
						
						// A cache removal wakes everyone up
						ctx.notifyAll();
					}
					
					else if (ctx.getId() == null)
					{
						// The key from the MethodInvocation still identifies the right cachekey
						container.getInstanceCache().remove(key);

						// A cache removal wakes everyone up
						ctx.notifyAll();

						// no more pool return
					}
					
					// We are done using the context so we wake up the next thread waiting for the ctx
					// marcf: I suspect we could use it only if lock = 0 (code it in the context.lock in fact)
					// this doesn't hurt here, meaning that even if we don't wait for 0 to come up 
					// we will wake up a thread that will go back to sleep and the next coming out of 
					// the body of code will wake the next one etc until we reach 0.  Reentrants are a pain
					// a minor though and I really suspect not checking for 0 is quite ok in all cases.
					ctx.notify();
				}
			}// synchronized ctx
		} // finally
	}
	
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
		boolean reentrant = ((EntityMetaData)container.getBeanMetaData()).isReentrant();
		
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
