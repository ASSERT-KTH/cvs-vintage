/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import javax.transaction.Status;
import javax.transaction.SystemException;

import javax.jms.Message;
import javax.jms.JMSException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.logging.Logger;

/**
 * Cache for stateful session beans. 
 *      
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.7 $
 */
public class StatefulSessionInstanceCache 
	extends AbstractInstanceCache
{
	// Constants -----------------------------------------------------
    
	// Attributes ----------------------------------------------------
	/* The container */
	private StatefulSessionContainer m_container;
	/* The map that holds passivated beans that will be removed */
	private HashMap m_passivated = new HashMap();
	/* Used for logging */
	private StringBuffer m_buffer = new StringBuffer();
    
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
		m_passivated.put(ctx.getId(), new Long(System.currentTimeMillis()));
	}
	protected void activate(EnterpriseContext ctx) throws RemoteException
	{
		m_container.getPersistenceManager().activateSession((StatefulSessionEnterpriseContext)ctx);
		m_passivated.remove(ctx.getId());
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
				try 
				{
				   return (ctx.getTransaction().getStatus() == Status.STATUS_NO_TRANSACTION);
				}
				catch (SystemException e) 
				{
				   // SA FIXME: not sure what to do here 
				   return false;
				}
			}
		}
		return true;
	}
   
	// Package protected ---------------------------------------------
	void removePassivated(long maxLifeAfterPassivation)
	{
		StatefulSessionPersistenceManager store = (StatefulSessionPersistenceManager)m_container.getPersistenceManager();
		long now = System.currentTimeMillis();
		Iterator entries = m_passivated.entrySet().iterator();
		while (entries.hasNext())
		{
			Map.Entry entry = (Map.Entry)entries.next();
			Object key = entry.getKey();
			long passivationTime = ((Long)entry.getValue()).longValue();
			if (now - passivationTime > maxLifeAfterPassivation) 
			{
				store.removePassivated(key);
				log(key);
				// Must use the iterator to remove, otherwise 
				// ConcurrentModificationException is thrown
				entries.remove();
				removeLock(key);
			}
		}
	}
    
	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------
	private void log(Object key)
	{
		m_buffer.setLength(0);
		m_buffer.append("Removing from storage bean '");
		m_buffer.append(m_container.getBeanMetaData().getEjbName());
		m_buffer.append("' with id = ");
		m_buffer.append(key);
		Logger.debug(m_buffer.toString());

		if (isJMSMonitoringEnabled())
		{
			// Prepare JMS message
			Message message = createMessage(key);
			try
			{
				message.setStringProperty(TYPE, "REMOVER");
			}
			catch (JMSException x)
			{
				Logger.exception(x);
			}

			// Send JMS Message
			sendMessage(message);
		}
	}

	// Inner classes -------------------------------------------------

}
