/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Iterator;
import java.util.ArrayList;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;

/**
 * JDBCValueClassMetaData holds a list of the properties for a dependent value
 * class.
 *     
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCValueClassMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	
	private Class javaType;
	private ArrayList properties;

	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
   
	// Public --------------------------------------------------------
	public JDBCValueClassMetaData(Element classElement, JDBCApplicationMetaData jdbcApplication) throws DeploymentException {
		ClassLoader classLoader = jdbcApplication.getClassLoader();
		
		String className = getElementContent(getUniqueChild(classElement, "class"));
		try {
			javaType = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("dependent-value-class not found: " + className);
		}
		
		properties = new ArrayList();
		Iterator iterator = getChildrenByTagName(classElement, "property");
		while(iterator.hasNext()) {
			Element propertyElement = (Element)iterator.next();
		
			properties.add(new JDBCValuePropertyMetaData(propertyElement, this));
		}
	}

	public Class getJavaType() {
		return javaType;
	}

	public Iterator getProperties() {
		return properties.iterator();
	}
	
	// XmlLoadable implementation ------------------------------------
		
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
}
