/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.metadata;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;


/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.2 $
 */
public class FinderMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
    
	// Attributes ----------------------------------------------------
    private String name;
	private String order;
	private String query;
	
	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
   
	// Public --------------------------------------------------------
    public String getName() { return name; }
	
	public String getOrder() { return order; }
	
	public String getQuery() { return query; }
	
	
	// XmlLoadable implementation ------------------------------------
    public void importXml(Element element) throws DeploymentException {
		name = getElementContent(getUniqueChild(element, "name"));
		query = getElementContent(getUniqueChild(element, "query"));
   	order = getElementContent(getUniqueChild(element, "order"));
	}	
	
	// Package protected ---------------------------------------------
    
	// Protected -----------------------------------------------------
    
	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
