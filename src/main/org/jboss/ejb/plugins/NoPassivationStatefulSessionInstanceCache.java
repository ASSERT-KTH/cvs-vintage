/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Collections;

import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolContainer;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.6 $
*/
public class NoPassivationStatefulSessionInstanceCache
implements InstanceCache
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	Container con;
	
	Map active = new HashMap();
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	
	/**
	*   Set the callback to the container. This is for initialization.
	*   The IM may extract the configuration from the container.
	*
	* @param   c  
	*/
	public void setContainer(Container c)
	{
		this.con = c;
	}
	
	public void init()
	throws Exception
	{
	}
	
	public void start()
	throws Exception
	{
	}
	
	public void stop()
	{
	}
	
	public void destroy()
	{
	}
	
	public synchronized EnterpriseContext get(Object id)
	throws RemoteException
	{
		InstanceInfo info = null;
		
		// Do we have the context in cache?
		StatefulSessionEnterpriseContext ctx = 
			(StatefulSessionEnterpriseContext)active.get(id);
		
		// We have it in cache
		if (ctx != null) {
			
			info = (InstanceInfo)ctx.getCacheContext();
			
			if (info.isLocked()) {
			
				//MF DESIGN: talk about this one... I know it is spec compliant but it sucks
				throw new RemoteException("Concurrent call to stateful session is not allowed");
			}
			
			else {
				
				info.lock();
			}
		}
			
	    // We don't have it in cache
		if (ctx == null) {
			
			// Get new instance from pool (bogus in our case)
			ctx = (StatefulSessionEnterpriseContext)((InstancePoolContainer)con).getInstancePool().get();
			
			// Activate
			ctx.setId(id);
			
			((StatefulSessionContainer)con).getPersistenceManager().activateSession(ctx);
			
			insert(ctx);
		}
		// The context has the instance as well and the right id
		return ctx;
	}
	
	public synchronized void insert(EnterpriseContext ctx)
	{
		InstanceInfo info = createInstanceInfo((StatefulSessionEnterpriseContext)ctx);
		((StatefulSessionEnterpriseContext)ctx).setCacheContext(info);
		info.lock();
		active.put(ctx.getId(), ctx) ;
	}
	
	public void release(EnterpriseContext ctx)
	{
		// This context is now available for other threads
		((InstanceInfo)((StatefulSessionEnterpriseContext)ctx).getCacheContext()).unlock();
		
		// The following code makes sense if we put threads to sleep (not the case anymore)
		//if (!((InstanceInfo)((StatefulSessionEnterpriseContext)ctx).getCacheContext()).isLocked())
		//		ctx.notify();
		
	}
	
	public synchronized void remove(Object id)
	{
		Object ctx = active.remove(id);
		synchronized(ctx)
		{
			ctx.notifyAll();
		}
	}
	
	// Z implementation ----------------------------------------------
	
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	protected InstanceInfo createInstanceInfo(StatefulSessionEnterpriseContext ctx)
	{
		return new InstanceInfo(ctx);
	}
	
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
	class InstanceInfo
	{
		int locked = 0; // 0 == unlocked, >0 == locked
		
		StatefulSessionEnterpriseContext ctx;
		
		InstanceInfo(StatefulSessionEnterpriseContext ctx)
		{
			this.ctx = ctx;
		}
		
		public void lock()
		{
			locked++;
		}
		
		public void unlock()
		{
			locked--;
		}
		
		public boolean isLocked()
		{
			return locked > 0;
		}
		
		public StatefulSessionEnterpriseContext getContext()
		{
			return ctx;
		}
	}
}
