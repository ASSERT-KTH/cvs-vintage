/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.dreambean.ejx.xml.XMLManager;

/**
 *
 *
 * @see
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.3 $
 */
public class LRUEnterpriseContextCachePolicyConfiguration 
	extends CachePolicySupportConfiguration 
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private int m_minCapacity;
	private int m_maxCapacity;
	private int m_resizerPeriod;
	private int m_overagerPeriod;
	private int m_maxBeanAge;
	private int m_minCacheMissPeriod;
	private int m_maxCacheMissPeriod;
	private double m_cacheLoadFactor;

	// Static --------------------------------------------------------
	private static int DEFAULT_MAX_CAPACITY;
	private static int DEFAULT_MIN_CAPACITY;
	private static int DEFAULT_RESIZER_PERIOD;
	private static int DEFAULT_OVERAGER_PERIOD;
	private static int DEFAULT_MAX_BEAN_AGE;
	private static int DEFAULT_MAX_CACHE_MISS_PERIOD;
	private static int DEFAULT_MIN_CACHE_MISS_PERIOD;
	private static double DEFAULT_CACHE_LOAD_FACTOR;

	// Constructors --------------------------------------------------
	public LRUEnterpriseContextCachePolicyConfiguration()
	{
		m_minCapacity = DEFAULT_MIN_CAPACITY;
		m_maxCapacity = DEFAULT_MAX_CAPACITY;
		m_resizerPeriod = DEFAULT_RESIZER_PERIOD;
		m_overagerPeriod = DEFAULT_OVERAGER_PERIOD;
		m_maxBeanAge = DEFAULT_MAX_BEAN_AGE;
		m_minCacheMissPeriod = DEFAULT_MIN_CACHE_MISS_PERIOD;
		m_maxCacheMissPeriod = DEFAULT_MAX_CACHE_MISS_PERIOD;
		m_cacheLoadFactor = DEFAULT_CACHE_LOAD_FACTOR;
	}

	// Public --------------------------------------------------------
	public int getMinimumCapacity() {return m_minCapacity;}
	public void setMinimumCapacity(int size) 
	{
		if (size < 1) throw new IllegalArgumentException("Minimum cache size less than 1");
		m_minCapacity = size;
	}
	public int getMaximumCapacity() {return m_maxCapacity;}
	public void setMaximumCapacity(int size) 
	{
		if (size < 1) throw new IllegalArgumentException("Maximum cache size less than 1");
		m_maxCapacity = size;
	}
	public int getResizerPeriod() {return m_resizerPeriod;}
	public void setResizerPeriod(int period) 
	{
		if (period < 0) {throw new IllegalArgumentException("Resizer period can't be < 0");}
		m_resizerPeriod = period;
	}
	public int getOveragerPeriod() {return m_overagerPeriod;}
	public void setOveragerPeriod(int period) 
	{
		if (period < 0) {throw new IllegalArgumentException("Overager period can't be < 0");}
		m_overagerPeriod = period;
	}
	public int getMaxBeanAge() {return m_maxBeanAge;}
	public void setMaxBeanAge(int age) 
	{
		if (age <= 0) {throw new IllegalArgumentException("Max bean age can't be <= 0");}
		m_maxBeanAge = age;
	}
	public int getMinCacheMissPeriod() {return m_minCacheMissPeriod;}
	public void setMinCacheMissPeriod(int period) 
	{
		if (period <= 0) {throw new IllegalArgumentException("Min cache miss period can't be <= 0");}
		m_minCacheMissPeriod = period;
	}
	public int getMaxCacheMissPeriod() {return m_maxCacheMissPeriod;}
	public void setMaxCacheMissPeriod(int period) 
	{
		if (period <= 0) {throw new IllegalArgumentException("Max cache miss period can't be <= 0");}
		m_maxCacheMissPeriod = period;
	}
	public double getCacheLoadFactor() {return m_cacheLoadFactor;}
	public void setCacheLoadFactor(double factor) 
	{
		if (factor <= 0.0 || factor >= 1.0) {throw new IllegalArgumentException("Cache load factor can't be <= 0 or >= 1");}
		m_cacheLoadFactor = factor;
	}
	
	// Z implementation ----------------------------------------------

	// Y overrides ---------------------------------------------------
	public Element exportXml(Document doc)
		throws Exception
	{
		
		Element policyConfig = super.exportXml(doc);
		XMLManager.addElement(policyConfig, "min-capacity" , Integer.toString(getMinimumCapacity()));
		XMLManager.addElement(policyConfig, "max-capacity" , Integer.toString(getMaximumCapacity()));
		XMLManager.addElement(policyConfig, "overager-period" , Integer.toString(getOveragerPeriod()));
		XMLManager.addElement(policyConfig, "resizer-period" , Integer.toString(getResizerPeriod()));
		XMLManager.addElement(policyConfig, "max-bean-age", Integer.toString(getMaxBeanAge()));
		XMLManager.addElement(policyConfig, "max-cache-miss-period" , Integer.toString(getMaxCacheMissPeriod()));
		XMLManager.addElement(policyConfig, "min-cache-miss-period" , Integer.toString(getMinCacheMissPeriod()));
		XMLManager.addElement(policyConfig, "cache-load-factor" , Double.toString(getCacheLoadFactor()));
		return policyConfig;
	}
		
	public void importXml(Element elt)
		throws Exception
	{
		super.importXml(elt);
		if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(jBossEjbJar.JBOSS_DOCUMENT))
		{
			NodeList nl = elt.getChildNodes();
			int l = nl.getLength();
			for (int i = 0; i < l; ++i)
			{
				Node n = nl.item(i);
				String name = n.getNodeName();
				if (name.equals("min-capacity"))
				{
					int s = 0;
					String period = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!period.trim().equals(""))
					{
						try {s = Integer.parseInt(period);}
						catch (NumberFormatException ignored) {}
					}
					setMinimumCapacity(s);
					if (DEFAULT_MIN_CAPACITY == 0)
					{
						DEFAULT_MIN_CAPACITY = s;
					}
				}
				else if (name.equals("max-capacity"))
				{
					int s = 0;
					String period = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!period.trim().equals(""))
					{
						try {s = Integer.parseInt(period);}
						catch (NumberFormatException ignored) {}
					}
					setMaximumCapacity(s);
					if (DEFAULT_MAX_CAPACITY == 0)
					{
						DEFAULT_MAX_CAPACITY = s;
					}
				}
				else if (name.equals("overager-period"))
				{
					int p = 0;
					String period = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!period.trim().equals(""))
					{
						try {p = Integer.parseInt(period);}
						catch (NumberFormatException ignored) {}
					}
					setOveragerPeriod(p);
					if (DEFAULT_OVERAGER_PERIOD == 0)
					{
						DEFAULT_OVERAGER_PERIOD = p;
					}
				} 
				else if (name.equals("resizer-period")) 
				{
					int p = 0;
					String period = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!period.trim().equals(""))
					{
						try {p = Integer.parseInt(period);}
						catch (NumberFormatException ignored) {}
					}
					setResizerPeriod(p);
					if (DEFAULT_RESIZER_PERIOD == 0)
					{
						DEFAULT_RESIZER_PERIOD = p;
					}
				}
				else if (name.equals("max-bean-age"))
				{
					int a = 0;
					String age = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!age.trim().equals(""))
					{
						try {a = Integer.parseInt(age);}
						catch (NumberFormatException ignored) {}
					}
					setMaxBeanAge(a);
					if (DEFAULT_MAX_BEAN_AGE == 0)
					{
						DEFAULT_MAX_BEAN_AGE = a;
					}
				} 
				else if (name.equals("max-cache-miss-period")) 
				{
					int p = 0;
					String period = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!period.trim().equals(""))
					{
						try {p = Integer.parseInt(period);}
						catch (NumberFormatException ignored) {}
					}
					setMaxCacheMissPeriod(p);
					if (DEFAULT_MAX_CACHE_MISS_PERIOD == 0)
					{
						DEFAULT_MAX_CACHE_MISS_PERIOD = p;
					}
				}
				else if (name.equals("min-cache-miss-period")) 
				{
					int p = 0;
					String period = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!period.trim().equals(""))
					{
						try {p = Integer.parseInt(period);}
						catch (NumberFormatException ignored) {}
					}
					setMinCacheMissPeriod(p);
					if (DEFAULT_MIN_CACHE_MISS_PERIOD == 0)
					{
						DEFAULT_MIN_CACHE_MISS_PERIOD = p;
					}
				}
				else if (name.equals("cache-load-factor")) 
				{
					double f = 0.0;
					String factor = n.hasChildNodes() ? XMLManager.getString(n) : "";
					if (!factor.trim().equals(""))
					{
						try {f = Double.parseDouble(factor);}
						catch (NumberFormatException ignored) {}
					}
					setCacheLoadFactor(f);
					if (DEFAULT_CACHE_LOAD_FACTOR == 0.0)
					{
						DEFAULT_CACHE_LOAD_FACTOR = f;
					}
				}
			}
		}
	}
	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
