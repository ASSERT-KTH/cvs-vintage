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
 *   @version $Revision: 1.4 $
 */
public class ResourceRefMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    /** The ejb-jar.xml/../resource-ref/res-ref-name element used by the bean code */
	private String refName;
    /** The jboss.xml/../resource-ref/resource-name value that maps to a resource-manager */
	private String name;
    /** The jndi name of the deployed resource, or the URL in the case of
     a java.net.URL resource type. This comes from either the:
     jboss.xml/../resource-ref/jndi-name element value or the
     jboss.xml/../resource-ref/res-url element value or the
     jboss.xml/../resource-manager/res-jndi-name element value
     jboss.xml/../resource-manager/res-url element value
     */
     private String jndiName;
     /** The ejb-jar.xml/../resource-ref/res-type java classname of the resource */
    private String type;
    /** The ejb-jar.xml/../resource-ref/res-auth value */
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
    public String getJndiName() { return jndiName; }
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
        // Look for the resource-ref/resource-name element
        Element child = getOptionalChild(element, "resource-name");
        if( child == null )
        {
            // There must be a resource-ref/res-url value if this is a URL resource
            if( type.equals("java.net.URL") )
                jndiName = getElementContent(getUniqueChild(element, "res-url"));
            // There must be a resource-ref/jndi-name value otherwise
            else
                jndiName = getElementContent(getUniqueChild(element, "jndi-name"));
        }
        else
        {
            name = getElementContent(child);
        }
	}

	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
