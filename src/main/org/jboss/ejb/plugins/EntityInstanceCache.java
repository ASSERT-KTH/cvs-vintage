/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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
import org.jboss.util.Sync;

/**
 * Cache subclass for entity beans.
 * 
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.4 $
 */
public class EntityInstanceCache
	extends AbstractInstanceCache 
	implements org.jboss.ejb.EntityCache
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
		throw new IllegalArgumentException("cache.get for entity beans must have a CacheKey object as argument instead of " + id);
	    }
	    EnterpriseContext rtn = null;
	    rtn = super.get(id);
	    //
	    // FIXME: (Bill Burke) We were running into problems where CMP EntityBeans 
	    // were out of sync with the database
	    // The problem went away after a few minutes of inactivity leading us to 
	    // believe that the bean in cache was passivated and the CacheKey was out 
	    // of sync with the Context's key.  So I put this defensive check in to 
	    // flag the problem.
	    if (rtn != null && !rtn.getId().equals(((CacheKey)id).id))
	    {
		throw new IllegalStateException("somehow the cache is out of synch with the context ctx.id != lookup.id");
	    }
	    return rtn;
	}
	public void remove(Object id)
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.remove for entity beans must have a CacheKey object as argument instead of " + id);
		}
		super.remove(id);
	}
	public synchronized Sync getLock(Object id) 
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.getLock for entity beans must have a CacheKey object as argument instead of " + id);
		}
		return super.getLock(id);
	}
	protected synchronized void removeLock(Object id) 
	{
		if (!(id instanceof CacheKey)) 
		{
			throw new IllegalArgumentException("cache.removeLock for entity beans must have a CacheKey object as argument instead of " + id);
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
