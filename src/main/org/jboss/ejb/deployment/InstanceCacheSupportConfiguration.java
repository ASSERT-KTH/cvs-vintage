/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.Component;
import java.beans.beancontext.BeanContextSupport;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.beans.beancontext.BeanContextContainerProxy;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.xml.XMLManager;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @see
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.3 $
 */
public class InstanceCacheSupportConfiguration 
	extends BeanContextSupport
	implements BeanContextChildComponentProxy, XmlExternalizable
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private String m_cachePolicyClassName;
	private Component m_component;
	private Object m_policyConfiguration;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	public InstanceCacheSupportConfiguration()
	{
		// Initializing this attribute is fundamental for EJX to work correctly,
		// otherwise when selecting in EJX for the first time an object of this 
		// (sub)class that was removed and re-added, the children of this object 
		// are not created because setCachePolicy is not called (due to the fact 
		// that getCachePolicy returns null); setting it by default to the empty 
		// string ensure that setCachePolicy is called the first time this object 
		// is selected and thus its children created.  
		m_cachePolicyClassName = "";
	}

	// Public --------------------------------------------------------
	public void setCachePolicy(String className) 
	{
		if (className == null || className.trim().equals("")) throw new IllegalArgumentException("Cache policy class name not defined");
		m_cachePolicyClassName = className;

		if (m_policyConfiguration != null)
		{
			remove(m_policyConfiguration);
			m_policyConfiguration = null;
		}

		try
		{
			String confName = getConfigurationClassName(className);
			Class clazz = Thread.currentThread().getContextClassLoader().loadClass(confName);
			Object obj = clazz.newInstance();
			if (obj instanceof BeanContextChildComponentProxy || obj instanceof BeanContextContainerProxy)
			{
				m_policyConfiguration = obj;
				add(m_policyConfiguration);
			}
		} 
		catch (Throwable ignored) {}
	}
	public String getCachePolicy() 
	{
		return m_cachePolicyClassName;
	}
	public Object getPolicyConfiguration() 
	{
		return m_policyConfiguration;
	}

	// BeanContextChildComponentProxy implementation -----------------
	public Component getComponent()
	{
		if (m_component == null) m_component = new com.dreambean.awt.GenericCustomizer(this);
		return m_component;
	}
	// XmlExternalizable implementation ------------------------------
	public Element exportXml(Document doc)
		throws Exception
	{
		Element cacheConfig = doc.createElement("container-cache-conf");
		XMLManager.addElement(cacheConfig, "cache-policy" , getCachePolicy());
		if (m_policyConfiguration != null)
		{
			cacheConfig.appendChild(((XmlExternalizable)m_policyConfiguration).exportXml(doc));
		}
		return cacheConfig;
	}
		
	public void importXml(Element elt)
		throws Exception
	{
		if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(jBossEjbJar.JBOSS_DOCUMENT))
		{
			NodeList nl = elt.getChildNodes();
			int l = nl.getLength();
			for (int i = 0; i < l; ++i)
			{
				Node n = nl.item(i);
				String name = n.getNodeName();
				
				if (name.equals("cache-policy"))
				{
					setCachePolicy(n.hasChildNodes() ? XMLManager.getString(n) : "");
				}
				else if (name.equals("cache-policy-conf")) 
				{
					((XmlExternalizable)m_policyConfiguration).importXml((Element)n);
				}
			}
 		}
	}
	
	
	// Y overrides ---------------------------------------------------

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------
	protected String getConfigurationClassName(String c) 
	{
		String name = c.substring(c.lastIndexOf(".") + 1);
		return "org.jboss.ejb.deployment." + name + "Configuration";
	}
	
	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
