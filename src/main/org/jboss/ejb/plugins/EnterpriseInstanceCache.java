/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Element;
import org.jboss.util.CachePolicy;
import org.jboss.util.Executable;
import org.jboss.util.WorkerQueue;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;

/**
 * Base class for caches of entity and stateful beans. <p>
 * It manages the cache entries through a {@link CachePolicy} object; 
 * the implementation of the cache policy object must respect the following
 * requirements:
 * <ul>
 * <li> Have a public constructor that takes a single argument of type 
 * EnterpriseInstanceCache.class or a subclass
 * </ul>
 *
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.3 $
 */
public abstract class EnterpriseInstanceCache 
	implements InstanceCache, XmlLoadable
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	/* The object that is delegated to implement the desired caching policy */
	private CachePolicy m_cache;
	/* The worker queue that passivates beans in another thread */
	private WorkerQueue m_passivator;
	/* The mutex object for the cache */
	private Object m_cacheLock = new Object();
	/* Helper class that handles synchronization for the passivation thread */
	private PassivationHelper m_passivationHelper;
	/* Min cache capaciy */
	private int m_minCapacity;
	/* Max cache capaciy */
	private int m_maxCapacity;
	/* Map that holds mutexes used to sync the passivation with other activities */
	private Map m_lockMap = new HashMap();

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------

	// Public --------------------------------------------------------
	/* From InstanceCache interface */
	public EnterpriseContext get(Object id) 
		throws RemoteException, NoSuchObjectException 
	{
		if (id == null) throw new IllegalArgumentException("Can't get an object with a null key");

		EnterpriseContext ctx = null;
		synchronized (getCacheLock()) 
		{
			ctx = (EnterpriseContext)getCache().get(id);
		}
		if (ctx == null) 
		{
			// Here I block if the bean is passivating now
			ctx = unschedulePassivation(id);
			// Already passivated ?
			if (ctx == null)
			{
				try
				{
					ctx = acquireContext();
					setKey(id, ctx);
					activate(ctx);
				}
				catch (Exception x)
				{
					freeContext(ctx);
					throw new NoSuchObjectException(x.getMessage());
				}
			}
			insert(ctx);
		}
		if (ctx == null) throw new NoSuchObjectException("Can't find bean with id = " + id);
		return ctx;
	}
	/* From InstanceCache interface */
	public void insert(EnterpriseContext ctx)
	{
		if (ctx == null) throw new IllegalArgumentException("Can't insert a null object in the cache");

		Object key = getKey(ctx);
		synchronized (getCacheLock())
		{
			// Paranoid check...
			if (getCache().peek(key) == null)
			{
				getCache().insert(key, ctx);
			}
			else
			{
				throw new IllegalStateException("Can't insert bean with id = " + key + ": it is already in the cache.");
			}
		}
	}
	/* From InstanceCache interface */
	public void release(EnterpriseContext ctx) 
	{
		if (ctx == null) throw new IllegalArgumentException("Can't release a null object");

		remove(getKey(ctx));
		schedulePassivation(ctx);
	}
	/* From InstanceCache interface */
	public void remove(Object id)
	{
		if (id == null) throw new IllegalArgumentException("Can't remove an object using a null key");
		
		synchronized (getCacheLock())
		{
			// Paranoid check...
			if (getCache().peek(id) != null)
			{
				getCache().remove(id);
			}
			else
			{
				throw new IllegalStateException("Can't remove bean with id = " + id + ": it isn't in the cache.");
			}
		}
		removeLock(id);
	}
	/**
	 * Creates (if necessary) and returns an object used as mutex to sync passivation
	 * activity with other activities. <br>
	 * The mutex is automatically removed when the corrispondent id is removed from
	 * the cache.
	 */
	public synchronized Object getLock(Object id) 
	{
		Object mutex = m_lockMap.get(id);
		if (mutex == null) 
		{
			mutex = new Object();
			m_lockMap.put(id, mutex);
		}
		return mutex;
	}
	/**
	 * Removes the mutex associated with the given id.
	 */
	protected synchronized void removeLock(Object id) 
	{
		Object mutex = m_lockMap.get(id);
		if (mutex != null) 
		{
			m_lockMap.remove(id);
		}
	}

	// XmlLoadable implementation ----------------------------------------------
	public void importXml(Element element) throws DeploymentException 
	{
		// This one is mandatory
		String p = MetaData.getElementContent(MetaData.getUniqueChild(element, "cache-policy"));
		try 
		{
			Class cls = Thread.currentThread().getContextClassLoader().loadClass(p);
			Constructor ctor = cls.getConstructor(new Class[] {EnterpriseInstanceCache.class});
			m_cache = (CachePolicy)ctor.newInstance(new Object[] {this});
		}
		catch (Exception x) 
		{
			throw new DeploymentException("Can't create cache policy", x);
		}
		
		Element policyConf = MetaData.getOptionalChild(element, "cache-policy-conf");
		if (policyConf != null)
		{
			if (m_cache instanceof XmlLoadable)
			{
				try 
				{
					((XmlLoadable)m_cache).importXml(policyConf);
				}
				catch (Exception x) 
				{
					throw new DeploymentException("Can't import policy configuration", x);
				}
			}
		}
	}	
	
	/* From Service interface*/
	public void init() throws Exception 
	{
		getCache().init();
		m_passivationHelper = new PassivationHelper();
		String threadName = "Passivator Thread for " + getContainer().getBeanMetaData().getEjbName();
		ClassLoader cl = getContainer().getClassLoader();
		m_passivator = new PassivatorQueue(threadName, cl);
	}
	/* From Service interface*/
	public void start() throws Exception 
	{
		getCache().start();
		m_passivator.start();
	}
	/* From Service interface*/
	public void stop() 
	{
		// Empty the cache
		synchronized (getCacheLock())
		{
			getCache().stop();
		}
		m_passivator.stop();
	}
	/* From Service interface*/
	public void destroy() 
	{
		getCache().destroy();
	}

	// Y overrides ---------------------------------------------------

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------
	/**
	 * Schedules the given EnterpriseContext for passivation
	 * @see PassivationHelper#schedule
	 */
	protected void schedulePassivation(EnterpriseContext ctx) 
	{
		m_passivationHelper.schedule(ctx);
	}
	/**
	 * Tries to unschedule the given EnterpriseContext for passivation; returns
	 * the unscheduled context if it wasn't passivated yet, null if the 
	 * passivation already happened.
	 * @see PassivationHelper#unschedule
	 */
	protected EnterpriseContext unschedulePassivation(Object id) 
	{
		return m_passivationHelper.unschedule(id);
	}
	/**
	 * Returns the container for this cache.
	 */
	protected abstract Container getContainer();
	/**
	 * Returns the cache policy used for this cache.
	 */
	protected CachePolicy getCache() {return m_cache;}
	/**
	 * Returns the mutex used to sync access to the cache policy object
	 */
	protected Object getCacheLock() 
	{
		return m_cacheLock;
	}
	/**
	 * Passivates the given EnterpriseContext
	 */
	protected abstract void passivate(EnterpriseContext ctx) throws RemoteException;
	/**
	 * Activates the given EnterpriseContext
	 */
	protected abstract void activate(EnterpriseContext ctx) throws RemoteException;
	/**
	 * Acquires an EnterpriseContext from the pool
	 */
	protected abstract EnterpriseContext acquireContext() throws Exception;
	/**
	 * Frees the given EnterpriseContext to the pool
	 */
	protected abstract void freeContext(EnterpriseContext ctx);
	/**
	 * Returns the key used by the cache to map the given context
	 */
	protected abstract Object getKey(EnterpriseContext ctx);
	/**
	 * Sets the given id as key for the given context
	 */
	protected abstract void setKey(Object id, EnterpriseContext ctx);
	/**
	 * Returns whether the given context can be passivated or not
	 */
	protected abstract boolean canPassivate(EnterpriseContext ctx);
	
	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
	/**
	 * Helper class that schedules, unschedules, and executes the passivation jobs.
	 */
	protected class PassivationHelper
	{
		/* The map that holds the passivation jobs posted */
		private Map m_passivationJobs = Collections.synchronizedMap(new HashMap());

		/**
		 * Creates and schedules a {@link PassivationJob} for passivation
		 */
		protected void schedule(EnterpriseContext bean) 
		{
			PassivationJob job = new PassivationJob(bean)
			{
				public synchronized void execute() throws Exception
				{
					EnterpriseContext ctx = getEnterpriseContext();
					Object id = getKey(ctx);
					Object mutex = getLock(id);
					
					synchronized (mutex) 
					{
						if (!canPassivate(ctx))
						{
							synchronized (getCacheLock())
							{
								getCache().insert(id, ctx);
							}
							return;
						}
						else 
						{
							if (!isCancelled())
							{
								try
								{
									// If the next call throws RemoteException we can 
									// reinsert the context meaningfully in the cache
									passivate(ctx);
									executed();
									removeLock(id);
									freeContext(ctx);
								} 
								catch (RemoteException x)
								{
									// Can't passivate this bean, keep it in memory
									// Reinsert it in the cache
									synchronized (getCacheLock())
									{
										getCache().insert(id, ctx);
									}
									throw x;
								}
								finally
								{
									m_passivationJobs.remove(id);
								}
							}
						}
					}
				}
			};
			m_passivationJobs.put(getKey(bean), job);
			m_passivator.putJob(job);
		}
		/**
		 * Tries to unschedule a job paired with the given context's id
		 * @return null if the bean has been passivated, the context 
		 * paired with the given id otherwise
		 */
		protected EnterpriseContext unschedule(Object id) 
		{
			PassivationJob job = (PassivationJob)m_passivationJobs.get(id);
			if (job != null) 
			{
				// Still to execute or executing now, cancel the job
				job.cancel();
				// Sync to not allow method execute to be executed after 
				// the if statement below but before the return
				synchronized (job)
				{
					if (!job.isExecuted()) 
					{
						// Still to be executed, return the bean
						return job.getEnterpriseContext();
					}
				}
			}
			// Unscheduling request arrived too late, bean already passivated
			return null;
		}
	}	
}

/**
 * Abstract class for passivation jobs.
 * Subclasses should implement {@link #execute} synchronizing it in some way because
 * the execute method is normally called in the passivation thread,
 * while the cancel method is normally called from another thread.
 * To avoid that subclasses override methods of this class without
 * make them synchronized (except execute of course), they're declared final.
 */
abstract class PassivationJob implements Executable
{
	private EnterpriseContext m_context;
	private boolean m_cancelled;
	private boolean m_executed;
	
	PassivationJob(EnterpriseContext ctx) 
	{
		m_context = ctx;
	}
	public abstract void execute() throws Exception;
	/**
	 * Returns the EnterpriseContext associated with this passivation job,
	 * so the bean that will be passivated.
	 */
	final synchronized EnterpriseContext getEnterpriseContext() 
	{
		return m_context;
	}
	/**
	 * Mark this job for cancellation.
	 * @see #isCancelled
	 */
	final synchronized void cancel() 
	{
		m_cancelled = true;
	}
	/**
	 * Returns whether this job has been marked for cancellation
	 * @see #cancel
	 */
	final synchronized boolean isCancelled() 
	{
		return m_cancelled;
	}
	/**
	 * Mark this job as executed
	 * @see #isExecuted
	 */
	final synchronized void executed() 
	{
		m_executed = true;
	}
	/**
	 * Returns whether this job has been executed
	 * @see #executed
	 */
	final synchronized boolean isExecuted() 
	{
		return m_executed;
	}
}

class PassivatorQueue extends WorkerQueue
{
	/**
	 * Creates a new passivator queue with default thread name of
	 * "Passivator Thread".
	 */
	PassivatorQueue() 
	{
		this("Passivator Thread", null);
	}
	/**
	 * Creates a new passivator queue with the given thread name and given
	 * context class loader. <br>
	 * @param threadName the name of the passivator thread
	 * @param cl the context class loader; if null the context class loader is not set.
	 */
	PassivatorQueue(String threadName, ClassLoader cl) 
	{
		super(threadName);
		if (cl != null) 
		{
			m_queueThread.setContextClassLoader(cl);
		}
	}
}
