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
 *   @version $Revision: 1.2 $
 */
public class ResourceRefMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	private String refName;      // the name used in the bean code (from ejb-jar.xml)
	private String name;         // the name of the resource used by jboss
	// the jndi name will be found in the ApplicationMetaData where resources are declared
	
    private String type;
	private boolean containerAuth;
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public ResourceRefMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getRefName() { return refName; }
	
	public String getResourceName() { 
		if (name == null) {
			// default is refName
			name = refName;
		} 
		return name;
	}
	
	public void setResourceName(String resName) {
		name = resName;
	}
	
	public String getType() { return type; }

	public boolean isContainerAuth() { return containerAuth; }


    public void importEjbJarXml(Element element) throws DeploymentException {
		refName = getElementContent(getUniqueChild(element, "res-ref-name"));
		
		type = getElementContent(getUniqueChild(element, "res-type"));
		
		String auth = getElementContent(getUniqueChild(element, "res-auth"));
		if (auth.equals("Container")) {
			containerAuth = true;
		} else if (auth.equals("Application")) {
			containerAuth = false;
		} else {
			throw new DeploymentException("res-auth tag should be 'Container' or 'Application'");
		}
	}
	
	public void importJbossXml(Element element) throws DeploymentException {
		name = getElementContent(getUniqueChild(element, "resource-name"));
	}
    
	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
