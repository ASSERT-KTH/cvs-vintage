/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.EnterpriseContext;


/**
*   This container acquires the given instance. 
*
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.2 $
*/
public class StatefulSessionInstanceInterceptor
extends AbstractInterceptor
{
	// Constants ----------------------------------------------------
	
	// Attributes ---------------------------------------------------
	
	// Static -------------------------------------------------------
	
	// Constructors -------------------------------------------------
	
	// Public -------------------------------------------------------
	
	// Interceptor implementation -----------------------------------
	public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
	throws Exception
	{
		// Get context
		ctx = ((StatefulSessionContainer)getContainer()).getInstancePool().get();
		
		try
		{
			// Invoke through interceptors
			return getNext().invokeHome(method, args, ctx);
		
		} finally
		{
			// Still free? Not free if create() was called successfully
			// MF Praise: hey for once the comment is good rickard ;-0
			if (ctx.getId() == null)
			{
				// Create did not associate an ID with the ctx
				// There is nothing to do just let the garbage collector do its work
			
			} else
			{
				// Create was called succesfully we go to the cache
				((StatefulSessionContainer)getContainer()).getInstanceCache().release(ctx);
			}
		}
	}
	
	public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
	throws Exception
	{
		// Get context
		ctx = ((StatefulSessionContainer)getContainer()).getInstanceCache().get(id);
		
		try
		{
			// Invoke through interceptors
			return getNext().invoke(id, method, args, ctx);
		} catch (RemoteException e)
		{
			// Discard instance
			((StatefulSessionContainer)getContainer()).getInstanceCache().remove(id);
			ctx = null;
			
			throw e;
		} catch (RuntimeException e)
		{
			// Discard instance
			((StatefulSessionContainer)getContainer()).getInstanceCache().remove(id);
			ctx = null;
			
			throw e;
		} catch (Error e)
		{
			// Discard instance
			((StatefulSessionContainer)getContainer()).getInstanceCache().remove(id);
			ctx = null;
			
			throw e;
		} 
		finally {
			
			if (ctx != null)
			{
				if (ctx.getId() == null)
				{
					// Remove from cache
					((StatefulSessionContainer)getContainer()).getInstanceCache().remove(id);
					
					// It has been removed -> send to free pool
					getContainer().getInstancePool().free(ctx);
				}
				{
					// Return context
					((StatefulSessionContainer)getContainer()).getInstanceCache().release(ctx);
				}
			}
		}
	}
}

