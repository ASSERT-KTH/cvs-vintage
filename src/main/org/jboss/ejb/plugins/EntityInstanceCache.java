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
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.CacheKey;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * Cache subclass for entity beans.
 * 
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.1 $
 */
public class EntityInstanceCache
	extends EnterpriseInstanceCache implements org.jboss.ejb.EntityInstanceCache
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	/* The container */
	private EntityContainer m_container;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------

	// Public --------------------------------------------------------
	/* From ContainerPlugin interface */
	public void setContainer(Container c) 
	{
		m_container = (EntityContainer)c;
	}

	// Z implementation ----------------------------------------------
	public Object createCacheKey(Object id) {return new CacheKey(id);}
	

	// Y overrides ---------------------------------------------------
	public EnterpriseContext get(Object id) 
		throws RemoteException, NoSuchObjectException 
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.get for entity beans must have a CacheKey object as argument instead of " + id.getClass());
		}
		return super.get(id);
	}
	public void remove(Object id)
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.remove for entity beans must have a CacheKey object as argument instead of " + id.getClass());
		}
		super.remove(id);
	}
	public synchronized Object getLock(Object id) 
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.getLock for entity beans must have a CacheKey object as argument instead of " + id.getClass());
		}
		return super.getLock(id);
	}
	protected synchronized void removeLock(Object id) 
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.removeLock for entity beans must have a CacheKey object as argument instead of " + id.getClass());
		}
		super.removeLock(id);
	}
	
	protected Object getKey(EnterpriseContext ctx) 
	{
		return ((EntityEnterpriseContext)ctx).getCacheKey();
	}
	protected void setKey(Object id, EnterpriseContext ctx) 
	{
		((EntityEnterpriseContext)ctx).setCacheKey(id);
		ctx.setId(((CacheKey)id).id);
	}

	protected Container getContainer() {return m_container;}

	protected void passivate(EnterpriseContext ctx) throws RemoteException
	{
		m_container.getPersistenceManager().passivateEntity((EntityEnterpriseContext)ctx);
	}
	protected void activate(EnterpriseContext ctx) throws RemoteException
	{
		m_container.getPersistenceManager().activateEntity((EntityEnterpriseContext)ctx);
	}
	protected EnterpriseContext acquireContext() throws Exception
	{
		return m_container.getInstancePool().get();
	}
	protected void freeContext(EnterpriseContext ctx)
	{
		m_container.getInstancePool().free(ctx);
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
