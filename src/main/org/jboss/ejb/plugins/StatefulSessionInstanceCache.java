/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

/**
 * Cache for stateful session beans. 
 *      
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.1 $
 */
public class StatefulSessionInstanceCache 
	extends EnterpriseInstanceCache
{
	// Constants -----------------------------------------------------
    
	// Attributes ----------------------------------------------------
	/* The container */
	private StatefulSessionContainer m_container;
    
	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
   
	// Public --------------------------------------------------------
	/* From ContainerPlugin interface */
	public void setContainer(Container c) 
	{
		m_container = (StatefulSessionContainer)c;
	}
	
	// Z implementation ----------------------------------------------

	// Y overrides ---------------------------------------------------
	protected Container getContainer() {return m_container;}
	protected void passivate(EnterpriseContext ctx) throws RemoteException
	{
		m_container.getPersistenceManager().passivateSession((StatefulSessionEnterpriseContext)ctx);
	}
	protected void activate(EnterpriseContext ctx) throws RemoteException
	{
		m_container.getPersistenceManager().activateSession((StatefulSessionEnterpriseContext)ctx);
	}
	protected EnterpriseContext acquireContext() throws Exception
	{
		return m_container.getInstancePool().get();
	}
	protected void freeContext(EnterpriseContext ctx)
	{
		m_container.getInstancePool().free(ctx);
	}
	protected Object getKey(EnterpriseContext ctx) 
	{
		return ctx.getId();
	}
	protected void setKey(Object id, EnterpriseContext ctx) 
	{
		ctx.setId(id);
	}
	protected boolean canPassivate(EnterpriseContext ctx) 
	{
		if (ctx.isLocked()) 
		{
			// The context is in the interceptor chain
			return false;
		}
		else 
		{
			if (ctx.getTransaction() != null) 
			{
				return false;
			}
		}
		return true;
	}
   
	// Package protected ---------------------------------------------
    
	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------

}
