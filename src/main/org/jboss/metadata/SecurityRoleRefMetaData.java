/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.2 $
 */
public class SecurityRoleRefMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	private String name;
    private String link;
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public SecurityRoleRefMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getName() { return name; }
	
	public String getLink() { return link; }

    public void importEjbJarXml(Element element) throws DeploymentException {
		name = getElementContent(getUniqueChild(element, "role-name"));
		link = getElementContent(getOptionalChild(element, "role-link"));
	}		
    
	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
