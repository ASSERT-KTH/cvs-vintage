/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.ejb.DeploymentException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.8 $
 */
public abstract class BeanMetaData extends MetaData {
    // Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	private ApplicationMetaData application;
    
	// from ejb-jar.xml
	private String ejbName;
	private String homeClass;
	private String remoteClass;
    private String ejbClass;
    protected boolean session;
	
	private HashMap ejbReferences = new HashMap();
	private ArrayList environmentEntries = new ArrayList();
    private ArrayList securityRoleReferences = new ArrayList();
	private HashMap resourceReferences = new HashMap();
	
	private ArrayList permissionMethods = new ArrayList();
	private ArrayList transactionMethods = new ArrayList();
	
	// from jboss.xml
	private String jndiName;
	protected String configurationName;
	private ConfigurationMetaData configuration;

	
	// Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public BeanMetaData(ApplicationMetaData app) {
		application = app;
	}
	
    // Public --------------------------------------------------------
    public boolean isSession() { return session; }

	public boolean isEntity() { return !session; }
                                            	
	public String getHome() { return homeClass; }
	
	public String getRemote() { return remoteClass; }
	
	public String getEjbClass() { return ejbClass; }
	
	public String getEjbName() { return ejbName; }
	
	public Iterator getEjbReferences() { return ejbReferences.values().iterator(); }
	
	public Iterator getEnvironmentEntries() { return environmentEntries.iterator(); }
	
	public Iterator getSecurityRoleReferences() { return securityRoleReferences.iterator(); }
	
	public Iterator getResourceReferences() { return resourceReferences.values().iterator(); }
	
	public String getJndiName() { 
		// jndiName may be set in jboss.xml
		if (jndiName == null) {
			jndiName = ejbName;
		}
		return jndiName;
	}
	
	public String getConfigurationName() {
		if (configurationName == null) {
			configurationName = getDefaultConfigurationName();
		}
		return configurationName;
	}
			
	
	public ConfigurationMetaData getContainerConfiguration() {
		if (configuration == null) {
			configuration = application.getConfigurationMetaDataByName(getConfigurationName());
		}
		return configuration;
	}
	
	public ApplicationMetaData getApplicationMetaData() { return application; }
	
	public abstract String getDefaultConfigurationName();
	
	public Iterator getTransactionMethods() { return transactionMethods.iterator(); }
	
	public Iterator getPermissionMethods() { return permissionMethods.iterator(); }
	
	
	public void addTransactionMethod(MethodMetaData method) { 
		transactionMethods.add(method);
	}
	
	public void addPermissionMethod(MethodMetaData method) { 
		permissionMethods.add(method);
	}
	
	public byte getMethodTransactionType(String methodName, Class[] params, boolean remote) {
		Iterator iterator = getTransactionMethods();
		while (iterator.hasNext()) {
			MethodMetaData m = (MethodMetaData)iterator.next();
			if (m.patternMatches(methodName, params, remote)) return m.getTransactionType();
		}
		// not found
		return TX_UNKNOWN;
	}

	public Set getMethodPermissions(String methodName, Class[] params, boolean remote) {
		Iterator iterator = getPermissionMethods();
		while (iterator.hasNext()) {
			MethodMetaData m = (MethodMetaData)iterator.next();
			if (m.patternMatches(methodName, params, remote)) return m.getRoles();
		}
		// not found
		return null;
	}

	public void importEjbJarXml(Element element) throws DeploymentException {
    
	    // set the ejb-name
		ejbName = getElementContent(getUniqueChild(element, "ejb-name"));

		// set the classes
		homeClass = getElementContent(getUniqueChild(element, "home"));
		remoteClass = getElementContent(getUniqueChild(element, "remote"));
		ejbClass = getElementContent(getUniqueChild(element, "ejb-class"));
		
		// set the environment entries
		Iterator iterator = getChildrenByTagName(element, "env-entry");
		
		while (iterator.hasNext()) {
			Element envEntry = (Element)iterator.next();
 			
			EnvEntryMetaData envEntryMetaData = new EnvEntryMetaData();
			envEntryMetaData.importEjbJarXml(envEntry);
			
			environmentEntries.add(envEntryMetaData);
		}
		
		// set the ejb references
		iterator = getChildrenByTagName(element, "ejb-ref");
		
		while (iterator.hasNext()) {
			Element ejbRef = (Element) iterator.next();
		    
			EjbRefMetaData ejbRefMetaData = new EjbRefMetaData();
			ejbRefMetaData.importEjbJarXml(ejbRef);
			
			ejbReferences.put(ejbRefMetaData.getName(), ejbRefMetaData);
		}
		
		// set the security roles references
		iterator = getChildrenByTagName(element, "security-role-ref");
		
		while (iterator.hasNext()) {
			Element secRoleRef = (Element) iterator.next();
			
			SecurityRoleRefMetaData securityRoleRefMetaData = new SecurityRoleRefMetaData();
			securityRoleRefMetaData.importEjbJarXml(secRoleRef);
			
			securityRoleReferences.add(securityRoleRefMetaData);
		}
			
		// set the resource references
        iterator = getChildrenByTagName(element, "resource-ref");
		
		while (iterator.hasNext()) {
			Element resourceRef = (Element) iterator.next();
			
			ResourceRefMetaData resourceRefMetaData = new ResourceRefMetaData();
			resourceRefMetaData.importEjbJarXml(resourceRef);
			
			resourceReferences.put(resourceRefMetaData.getRefName(), resourceRefMetaData);
		}
	}

	public void importJbossXml(Element element) throws DeploymentException {
		// we must not set defaults here, this might never be called
		
		// set the jndi name, (optional)		
		jndiName = getElementContent(getOptionalChild(element, "jndi-name"));
		
		// set the configuration (optional)
		configurationName = getElementContent(getOptionalChild(element, "configuration-name"));
		if (configurationName != null && getApplicationMetaData().getConfigurationMetaDataByName(configurationName) == null) {
			throw new DeploymentException("configuration '" + configurationName + "' not found in standardjboss.xml or jboss.xml");
		}
		
		// update the resource references (optional)
		Iterator iterator = getChildrenByTagName(element, "resource-ref");
		while (iterator.hasNext()) {
			Element resourceRef = (Element)iterator.next();
			String resRefName = getElementContent(getUniqueChild(resourceRef, "res-ref-name"));
			String resourceName = getElementContent(getUniqueChild(resourceRef, "resource-name"));
			ResourceRefMetaData resourceRefMetaData = (ResourceRefMetaData)resourceReferences.get(resRefName);
            
            if (resourceRefMetaData == null) {
                throw new DeploymentException("resource-ref " + resRefName + " found in jboss.xml but not in ejb-jar.xml");
            }
            
		    resourceRefMetaData.setResourceName(resourceName);
		}
		
		// set the external ejb-references (optional)
		iterator = getChildrenByTagName(element, "ejb-ref");
		while (iterator.hasNext()) {
			Element ejbRef = (Element)iterator.next();
			String ejbRefName = getElementContent(getUniqueChild(ejbRef, "ejb-ref-name"));
			EjbRefMetaData ejbRefMetaData = (EjbRefMetaData)ejbReferences.get(ejbRefName);
			if (ejbRefMetaData == null) {
				throw new DeploymentException("ejb-ref " + ejbRefName + " found in jboss.xml but not in ejb-jar.xml");
			}
			ejbRefMetaData.importJbossXml(ejbRef);
		}
	}
	
	
	
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
