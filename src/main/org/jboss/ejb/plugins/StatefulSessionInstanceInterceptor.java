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
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;


/**
*   This container acquires the given instance. 
*
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.3 $
*/
public class StatefulSessionInstanceInterceptor
	extends AbstractInterceptor
{
	// Constants ----------------------------------------------------
	
	// Attributes ---------------------------------------------------
	protected StatefulSessionContainer container;
	
	// Static -------------------------------------------------------
	
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
		mi.setEnterpriseContext(container.getInstancePool().get());
		
		try
		{
			// Invoke through interceptors
			return getNext().invokeHome(mi);
		} finally
		{
			// Still free? Not free if create() was called successfully
			if (mi.getEnterpriseContext().getId() == null)
			{
				// Create did not associate an ID with the ctx
				// There is nothing to do just let the garbage collector do its work
			
			} else
			{
				// Create was called succesfully we go to the cache
				container.getInstanceCache().release(mi.getEnterpriseContext());
			}
		}
	}
	
	public Object invoke(MethodInvocation mi)
		throws Exception
	{
		// Get context
		EnterpriseContext ctx = container.getInstanceCache().get(mi.getId());
		mi.setEnterpriseContext(ctx);
		
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
				
				if (ctx.getId() == null)
				{
					// Remove from cache
					container.getInstanceCache().remove(mi.getId());
					
					// It has been removed -> send to free pool
					container.getInstancePool().free(ctx);
				}
				{
					// Return context
					container.getInstanceCache().release(ctx);
				}
			}
		}
	}
}

