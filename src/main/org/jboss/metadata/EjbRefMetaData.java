/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *   @version $Revision: 1.1 $
 */
public class EjbRefMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	
	// the name used in the bean code
	private String name;
	
	// entity or session
	private String type;
	
	// the 2 interfaces
    private String home;
    private String remote;
	
	// the jndi name: we must map "name" to "link"
    private String link;
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public EjbRefMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getName() { return name; }
	
	public String getType() { return type; }
	
	public String getHome() { return home; }
	
	public String getRemote() { return remote; }
	
	public String getLink() { return link; }

    public void importEjbJarXml(Element element) throws DeploymentException {
		name = getElementContent(getUniqueChild(element, "ejb-ref-name"));
		type = getElementContent(getUniqueChild(element, "ejb-ref-type"));
		home = getElementContent(getUniqueChild(element, "home"));
		remote = getElementContent(getUniqueChild(element, "remote"));
		link = getElementContent(getOptionalChild(element, "ejb-link"));
	}		
    
	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
