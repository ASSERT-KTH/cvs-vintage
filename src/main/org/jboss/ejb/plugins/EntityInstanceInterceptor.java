/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
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

/**
*   This container acquires the given instance. 
*
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.8 $
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
		mi.setEnterpriseContext(ctx);
		
		// It is a new context for sure so we can lock it (no need for sync (not in cache))
		ctx.lock();
		
		try
		{
			// Invoke through interceptors
			return getNext().invokeHome(mi);
		} finally
		{
			// Still free? Not free if create() was called successfully
			if (mi.getEnterpriseContext().getId() == null)
			{
				// Free that context
				ctx.unlock();
				
				container.getInstancePool().free(mi.getEnterpriseContext());
			} 
			else
			{
				// DEBUG           Logger.log("Entity was created; not returned to pool");
				synchronized (ctx) {
					
					// Release the lock
					ctx.unlock();
					
					//Let the waiters know
					ctx.notifyAll();
				}
			}
		}
	}
	
	public Object invoke(MethodInvocation mi)
	throws Exception
	{
		// The id store is a CacheKey in the case of Entity 
		CacheKey key = (CacheKey) mi.getId();
		
		// Get context
		EnterpriseContext ctx = ((EntityContainer)getContainer()).getInstanceCache().get(key);
		
		// Set it on the method invocation
		mi.setEnterpriseContext(ctx);
		
		// We synchronize the locking logic (so that the invoke is unsynchronized and can be reentrant)
		synchronized (ctx) 
		{
			// Do we have a running transaction with the context
			if (ctx.getTransaction() != null &&
				// And are we trying to enter with another transaction
				!ctx.getTransaction().equals(mi.getTransaction())) 
			{
				// Let's put the thread to sleep a lock release will wake the thread
				try{ctx.wait();}
					catch (InterruptedException ie) {}
				
				// Try your luck again
				return invoke(mi);
			}
			
			if (!ctx.isLocked()){
				
				//take it!
				ctx.lock();  
			}
			
			else 
			{
				if (!isCallAllowed(mi)) {
					
					// Let's put the thread to sleep a lock release will wake the thread
					try{ctx.wait();}
						catch (InterruptedException ie) {}
					
					// Try your luck again
					return invoke(mi);
				}
				
				// The call is allowed, do increment the lock though (ctx already locked)
				ctx.lock();
			}
		
		} 
		
		try {
			
			// Go on, you won
			return getNext().invoke(mi);
		
		} 
		catch (RemoteException e)
		{
			// Discard instance
			// EJB 1.1 spec 12.3.1
			((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
			
			throw e;
		} catch (RuntimeException e)
		{
			// Discard instance
			// EJB 1.1 spec 12.3.1
			((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
			
			throw e;
		} catch (Error e)
		{
			// Discard instance
			// EJB 1.1 spec 12.3.1
			((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
			
			throw e;
		} finally
		{
			//         Logger.log("Release instance for "+id);
			if (ctx != null)
			{
				
				synchronized (ctx) {
					
					// unlock the context
					ctx.unlock();
					
					if (ctx.getId() == null)                             
					{
						// Remove from cache
						((EntityContainer)getContainer()).getInstanceCache().remove(key.id);
						
						// It has been removed -> send to free pool
						container.getInstancePool().free(ctx);
					}
					
					// notify the thread waiting on ctx
					ctx.notifyAll();
				}
			}
		}
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

