/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.util.HashMap;
import org.jboss.util.LRUCachePolicy;
import org.jboss.util.TimerTask;
import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Least Recently Used cache policy for EnterpriseContexts.
 *
 * @see EnterpriseInstanceCache
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.4 $
 */
public class LRUEnterpriseContextCachePolicy extends LRUCachePolicy
	implements EnterpriseContextCachePolicy, XmlLoadable
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	/* The EnterpriseInstanceCache that uses this cache policy */
	private EnterpriseInstanceCache m_cache;
	/* The period of the resizer's runs */
	private long m_resizerPeriod;
	/* The period of the overager's runs */
	private long m_overagerPeriod;
	/* The age after which a bean is automatically passivated */
	private long m_maxBeanAge;
	/* Enlarge cache capacity if there is a cache miss every or less this member's value */
	private long m_minPeriod;
	/* Shrink cache capacity if there is a cache miss every or more this member's value */
	private long m_maxPeriod;
	/* The resizer will always try to keep the cache capacity so 
	 * that the cache is this member's value loaded of cached objects */
	private double m_factor;
	/* The overager timer task */
	private TimerTask m_overager;
	/* The resizer timer task */
	private TimerTask m_resizer;


	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	/**
	 * Creates a LRU cache policy object given the instance cache that use
	 * this policy object. 
	 */
	public LRUEnterpriseContextCachePolicy(EnterpriseInstanceCache eic) 
	{	
		if (eic == null) throw new IllegalArgumentException("Instance cache argument cannot be null");
		m_cache = eic;
	}

	// Public --------------------------------------------------------

	// Z implementation ----------------------------------------------
	public void start() throws Exception 
	{
		if (m_resizerPeriod > 0)
		{
			m_resizer = new ResizerTask(m_resizerPeriod);
			scheduler.schedule(m_resizer, (long)(Math.random() * m_resizerPeriod));
		}
				
		if (m_overagerPeriod > 0) 
		{
			m_overager = new OveragerTask(m_overagerPeriod);
			scheduler.schedule(m_overager, (long)(Math.random() * m_overagerPeriod));
		}
	}
	
	public void stop() 
	{
		if (m_resizer != null) {m_resizer.cancel();}
		if (m_overager != null) {m_overager.cancel();}
		super.stop();
	}
	/**
	 * Reads from the configuration the parameters for this cache policy, that are
	 * all optionals.
	 */
	public void importXml(Element element) throws DeploymentException
	{
		String min = MetaData.getElementContent(MetaData.getOptionalChild(element, "min-capacity"));
		String max = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-capacity"));
		String op = MetaData.getElementContent(MetaData.getOptionalChild(element, "overager-period"));
		String rp = MetaData.getElementContent(MetaData.getOptionalChild(element, "resizer-period"));
		String ma = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-bean-age"));
		String map = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-cache-miss-period"));
		String mip = MetaData.getElementContent(MetaData.getOptionalChild(element, "min-cache-miss-period"));
		String fa = MetaData.getElementContent(MetaData.getOptionalChild(element, "cache-load-factor"));
		try 
		{
			if (min != null)
			{
				int s = Integer.parseInt(min);
				if (s <= 0) 
				{
					throw new DeploymentException("Min cache capacity can't be <= 0");
				}
				m_minCapacity = s;
			}
			if (max != null)
			{
				int s = Integer.parseInt(max);
				if (s <= 0)
				{
					throw new DeploymentException("Max cache capacity can't be <= 0");
				}
				m_maxCapacity = s;
			}				
			if (op != null)
			{
				int p = Integer.parseInt(op);
				if (p <= 0) {throw new DeploymentException("Overager period can't be <= 0");}
				m_overagerPeriod = p * 1000;
			}
			if (rp != null)				
			{
				int p = Integer.parseInt(rp);
				if (p <= 0) {throw new DeploymentException("Resizer period can't be <= 0");}
				m_resizerPeriod = p * 1000;
			}
			if (ma != null)
			{
				int a = Integer.parseInt(ma);
				if (a <= 0) {throw new DeploymentException("Max bean age can't be <= 0");}
				m_maxBeanAge = a * 1000;
			}
			if (map != null)
			{
				int p = Integer.parseInt(map);
				if (p <= 0) {throw new DeploymentException("Max cache miss period can't be <= 0");}
				m_maxPeriod = p * 1000;
			}
			if (mip != null)
			{
				int p = Integer.parseInt(mip);
				if (p <= 0) {throw new DeploymentException("Min cache miss period can't be <= 0");}
				m_minPeriod = p * 1000;
			}
			if (fa != null)
			{
				double f = Double.parseDouble(fa);
				if (f <= 0.0) {throw new DeploymentException("Cache load factor can't be <= 0");}
				m_factor = f;
			}
		}
		catch (NumberFormatException x) 
		{
			throw new DeploymentException("Can't parse policy configuration", x);
		}
	}

	// Y overrides ---------------------------------------------------

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------
	protected LRUList createList() {return new ContextLRUList();}
	protected LRUCacheEntry createCacheEntry(Object key, Object value) 
	{
		return new ContextLRUEntry(key, value);
	}
	protected void ageOut(LRUCacheEntry entry) 
	{
		if (entry == null) {throw new IllegalArgumentException("Cannot remove a null cache entry");}
		m_cache.release((EnterpriseContext)entry.m_object);
	}
	protected void cacheMiss() 
	{
		ContextLRUList list = getList();
		synchronized (list) 
		{
			++list.m_cacheMiss;
		}
	}

	// Private -------------------------------------------------------
	private ContextLRUList getList() {return (ContextLRUList)m_list;}

	// Inner classes -------------------------------------------------
	/**
	 * This TimerTask resizes the cache capacity using the cache miss frequency
	 * algorithm, that is the more cache misses we have, the more the cache size
	 * is enlarged, and viceversa. <p>
	 * Of course, the maximum and minimum capacity are the bounds that this
	 * resizer never passes.
	 */
	protected class ResizerTask extends TimerTask
	{
		protected ResizerTask(long resizerPeriod) 
		{
			super(resizerPeriod);
		}
		public void execute() 
		{
			// For now implemented as a Cache Miss Frequency algorithm
			// Sync the list, since it is accessed also by another thread
			ContextLRUList list = getList();
			synchronized (list) 
			{
				int period = list.m_cacheMiss == 0 ? Integer.MAX_VALUE : (int)(getPeriod() / list.m_cacheMiss);
				int cap = list.m_capacity;
				if (period <= m_minPeriod && cap < list.m_maxCapacity) 
				{
					// Enlarge cache capacity: if period == m_minPeriod then
					// the capacity is increased of the (1-m_factor)*100 %.
					double factor = 1.0 + ((double)m_minPeriod / period) * (1.0 - m_factor);
					int newCap = (int)(cap * factor);
					list.m_capacity = newCap < list.m_maxCapacity ? newCap : list.m_maxCapacity;
					log.log("Resized cache for bean " + m_cache.getContainer().getBeanMetaData().getEjbName() + ": old size = " + cap + ", new size = " + list.m_capacity);
				}
				else if (period >= m_maxPeriod && 
						 cap > list.m_minCapacity && 
						 list.m_count < (cap * m_factor))
				{
					// Shrink cache capacity
					int newCap = (int)(list.m_count / m_factor);
					list.m_capacity = newCap > list.m_minCapacity ? newCap : list.m_minCapacity;
					log.log("Resized cache for bean " + m_cache.getContainer().getBeanMetaData().getEjbName() + ": old size = " + cap + ", new size = " + list.m_capacity);
				}
				list.m_cacheMiss = 0;
			}
		}
	}
	/**
	 * This TimerTask passivates cached beans that have not been called for a while.
	 */
	protected class OveragerTask extends TimerTask
	{
		protected OveragerTask(long period) 
		{
			super(period);
		}
		public void execute() 
		{
			ContextLRUEntry entry = null;
			long now = System.currentTimeMillis();
			ContextLRUList list = getList();
			synchronized (list) 
			{
				while ((entry = (ContextLRUEntry)list.m_tail) != null)
				{
					if (now - entry.m_time >= m_maxBeanAge)
					{
						log.log("Passivated overaged bean " + m_cache.getContainer().getBeanMetaData().getEjbName() + " with id = " + entry.m_key);
						ageOut(entry);
					}
					else {break;}
				}
			}
		}
	}
	/* Dummy subclass to give visibility to the inner classes */
	protected class ContextLRUList extends LRUList {}
	/* Dummy subclass to give visibility to the inner classes */
	protected class ContextLRUEntry extends LRUCacheEntry 
	{
		protected ContextLRUEntry(Object key, Object value) {super(key, value);}
	}
}
