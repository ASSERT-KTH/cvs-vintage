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
public class EnvEntryMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	private String name;
	private String type;
    private String value;
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public EnvEntryMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getName() { return name; }
	
	public String getType() { return type; }
	
	public String getValue() { return value; }
    
    public void importEjbJarXml(Element element) throws DeploymentException {
		name = getElementContent(getUniqueChild(element, "env-entry-name"));
		type = getElementContent(getUniqueChild(element, "env-entry-type"));
		value = getElementContent(getUniqueChild(element, "env-entry-value"));
	}		
    
	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
