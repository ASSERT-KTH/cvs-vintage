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
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.xml.XMLManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * 
 *
 * @see
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.3 $
 */
public class CachePolicySupportConfiguration
	extends BeanContextSupport
	implements BeanContextChildComponentProxy, XmlExternalizable
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private Component m_component;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	public CachePolicySupportConfiguration() 
	{
	}

	// Public --------------------------------------------------------

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
		Element policyConfig = doc.createElement("cache-policy-conf");
		return policyConfig;
	}
		
	public void importXml(Element elt)
		throws Exception
	{
	}

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
