/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.SessionMetaData;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;


/**
*   This container acquires the given instance. 
*
*   @see <related>
*   @author Rickard �berg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.12 $
*/
public class StatefulSessionInstanceInterceptor
extends AbstractInterceptor
{
	// Constants ----------------------------------------------------
	
	// Attributes ---------------------------------------------------
	protected StatefulSessionContainer container;
	
	// Static -------------------------------------------------------
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
    
	// Constructors -------------------------------------------------
	
	// Public -------------------------------------------------------
	
	public void setContainer(Container container) 
	{ 
		this.container = (StatefulSessionContainer)container; 
	}
	
	public  Container getContainer()
	{
		return container;
	}
	// Interceptor implementation -----------------------------------
	public Object invokeHome(MethodInvocation mi)
	throws Exception
	{
		// Get context
		
		// get a new context from the pool (this is a home method call)
		EnterpriseContext ctx = container.getInstancePool().get();
		
		
		// set the context on the methodInvocation
		mi.setEnterpriseContext(ctx);
		
		// It is a new context for sure so we can lock it
		ctx.lock();
		
		try
		{
			// Invoke through interceptors
			return getNext().invokeHome(mi);
		} finally
		{
			// Release the lock
			ctx.unlock();
			
			// Still free? Not free if create() was called successfully
			if (ctx.getId() == null)
			{
				container.getInstancePool().free(mi.getEnterpriseContext()); 
			} 
		}
	}
	
	
	private void register(EnterpriseContext ctx, Transaction tx)
	{
		// Create a new synchronization
		InstanceSynchronization synch = new InstanceSynchronization(tx, ctx);
		
		try {
			// OSH: An extra check to avoid warning.
			// Can go when we are sure that we no longer get
			// the JTA violation warning.
			if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
				
				return;
			}
			
			// We want to be notified when the transaction commits
			tx.registerSynchronization(synch);
		
		} catch (RollbackException e) {
		
		} catch (Exception e) {
			
			throw new EJBException(e);
		
		}
	}
	
	public Object invoke(MethodInvocation mi)
	throws Exception
	{
		EnterpriseInstanceCache cache = (EnterpriseInstanceCache)container.getInstanceCache();
		Object id = mi.getId();
		EnterpriseContext ctx = null;
		Object mutex = cache.getLock(id);
		
		// We synchronize the locking logic (so we can be reentrant)
		synchronized (mutex) 
		{
			// Get context
			ctx = container.getInstanceCache().get(mi.getId());
		
			// Associate it with the method invocation
			mi.setEnterpriseContext(ctx);
		
		
			// BMT beans will lock and replace tx no matter what, CMT do work on transaction
			if (!((SessionMetaData)container.getBeanMetaData()).isBeanManagedTx()) {
				
				// Do we have a running transaction with the context
				if (ctx.getTransaction() != null &&
					// And are we trying to enter with another transaction
					!ctx.getTransaction().equals(mi.getTransaction()))
				{
					// Calls must be in the same transaction
					throw new RemoteException("Application Error: tried to enter Stateful bean with different transaction context");
				}
				
				//If the instance will participate in a new transaction we register a sync for it
				if (ctx.getTransaction() == null && mi.getTransaction() != null) {
					
					register(ctx, mi.getTransaction());
				
				}
			}
			
			if (!ctx.isLocked()){
				
				//take it!
				ctx.lock();  
			} else 
			{
				if (!isCallAllowed(mi))
				{
					// Calls must be in the same transaction
					throw new RemoteException("Application Error: no concurrent calls on stateful beans");
				}
				else 
				{
					ctx.lock();
				}
			}
		} 
		
		try
		{
			// Invoke through interceptors
			return getNext().invoke(mi);
		} catch (RemoteException e)
		{
			// Discard instance
			container.getInstanceCache().remove(mi.getId());
			ctx = null;
			
			throw e;
		} catch (RuntimeException e)
		{
			// Discard instance
			container.getInstanceCache().remove(mi.getId());
			ctx = null;
			
			throw e;
		} catch (Error e)
		{
			// Discard instance
			container.getInstanceCache().remove(mi.getId());
			ctx = null;
			
			throw e;
		} finally 
		{
			if (ctx != null)
			{
				// Still a valid instance
				synchronized (mutex) 
				{
					// release it
					ctx.unlock();
				
					// if removed, remove from cache
					if (ctx.getId() == null)
					{
						// Remove from cache
						container.getInstanceCache().remove(mi.getId());
					}
				}
			}
		}
	}
	
    private boolean isCallAllowed(MethodInvocation mi) 
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
		return false;
    }
	
	// Inner classes -------------------------------------------------

	private class InstanceSynchronization
	implements Synchronization
	{
		/**
		*  The transaction we follow.
		*/
		private Transaction tx;
		
		/**
		*  The context we manage.
		*/
		private EnterpriseContext ctx;
		
		// a utility boolean for session sync
		boolean notifySession = false;
		
		// Utility methods for the notifications
		Method beforeCompletion, afterCompletion;
		
		
		/**
		*  Create a new instance synchronization instance.
		*/
		InstanceSynchronization(Transaction tx, EnterpriseContext ctx)
		{
			this.tx = tx;
			this.ctx = ctx;
			
			// Let's compute it now
			notifySession = (ctx.getInstance() instanceof javax.ejb.SessionSynchronization);
			
			if (notifySession) {
				try {
					
					// Get the class we are working on
					Class sync = Class.forName("javax.ejb.SessionSynchronization");
					
					// Lookup the methods on it
					beforeCompletion = sync.getMethod("beforeCompletion", new Class[0]);
					
					afterCompletion =  sync.getMethod("afterCompletion", new Class[] {boolean.class});
				}
				catch (Exception e) { Logger.exception(e);}
			}
		}
		
		// Synchronization implementation -----------------------------
		
		public void beforeCompletion()
		{
			// DEBUG Logger.debug("beforeCompletion called");
			
			// lock the context the transaction is being commited (no need for sync)
			ctx.lock();
			
			if (notifySession) {
				try {
					
					beforeCompletion.invoke(ctx.getInstance(), new Object[0]);
				}
				catch (Exception e) { Logger.exception(e);}
			}
		}
		
		public void afterCompletion(int status)
		{
			// DEBUG Logger.debug("afterCompletion called");
			
			// finish the transaction association
			ctx.setTransaction(null);
			
			// unlock this context
			ctx.unlock();
			
			if (notifySession) {
				
				try {
					 
					if (status == Status.STATUS_COMMITTED) 
						afterCompletion.invoke(ctx.getInstance(), new Object[] {new Boolean(true)});
					else
						afterCompletion.invoke(ctx.getInstance(), new Object[] {new Boolean(false)});
				}
				catch (Exception e) {Logger.exception(e);}
			}
		
		
		}
	}
}

