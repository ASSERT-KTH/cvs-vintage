/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.ejb.DeploymentException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author Peter Antman (peter.antman@tim.se)
 *   @author Daniel OConnor (docodan@mvcsoft.com)
 *   @version $Revision: 1.17 $
 */
public abstract class BeanMetaData extends MetaData {
    // Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	private ApplicationMetaData application;
    
	// from ejb-jar.xml
	private String ejbName;
	private String homeClass;
    private String remoteClass;
    private String localHomeClass;
    private String localClass;
    private String ejbClass;
    protected boolean session;
    protected boolean messageDriven = false;
	
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
    private String securityProxy;
	
	// Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public BeanMetaData(ApplicationMetaData app) {
		application = app;
	}
	
    // Public --------------------------------------------------------
    public boolean isSession() { return session; }

    public boolean isMessageDriven() { return messageDriven; }

	public boolean isEntity() { return !session && !messageDriven; }
                                            	
	public String getHome() { return homeClass; }
	
	public String getRemote() { return remoteClass; }

	public String getLocalHome() { return localHomeClass; }
	
	public String getLocal() { return localClass; }    
        
	public String getEjbClass() { return ejbClass; }
	
	public String getEjbName() { return ejbName; }
	
	public Iterator getEjbReferences() { return ejbReferences.values().iterator(); }
	
	public EjbRefMetaData getEjbRefByName(String name) {
		return (EjbRefMetaData)ejbReferences.get(name);
	}
	
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

    public String getSecurityProxy() { return securityProxy; }

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
		
		// default value
		byte result = TX_UNKNOWN;

		Iterator iterator = getTransactionMethods();
		while (iterator.hasNext()) {
			MethodMetaData m = (MethodMetaData)iterator.next();
			if (m.patternMatches(methodName, params, remote)) {
				result = m.getTransactionType();
				
				// if it is an exact match, break, if it is the wildcard continue to look for a finer match
				if (!"*".equals(m.getMethodName())) break;
			}
		}

		return result;
	}

   // d.s.> PERFORMANCE !!! 
	public Set getMethodPermissions(String methodName, Class[] params, boolean remote) {
		Set result = new HashSet ();
      Iterator iterator = getPermissionMethods();
		while (iterator.hasNext()) {
			MethodMetaData m = (MethodMetaData)iterator.next();
			if (m.patternMatches(methodName, params, remote))
         {
            Iterator i = m.getRoles().iterator ();
            while (i.hasNext ())
               result.add (i.next ());
         }
		}
		if (result.isEmpty ()) // no method-permission specified
         return null;
      else
         return result;
	}

	public void importEjbJarXml(Element element) throws DeploymentException {
    
	    // set the ejb-name
		ejbName = getElementContent(getUniqueChild(element, "ejb-name"));

		// set the classes
		// Not for MessageDriven
		if (!messageDriven) {
		    homeClass = getElementContent(getUniqueChild(element, "home"));
		    remoteClass = getElementContent(getUniqueChild(element, "remote"));
                    localHomeClass = getElementContent(getUniqueChild(element, "local-home"));
                    localClass = getElementContent(getUniqueChild(element, "local"));
		}
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

        // Get the security proxy
        securityProxy = getElementContent(getOptionalChild(element, "security-proxy"), securityProxy);

		// update the resource references (optional)
		Iterator iterator = getChildrenByTagName(element, "resource-ref");
		while (iterator.hasNext()) {
			Element resourceRef = (Element)iterator.next();
			String resRefName = getElementContent(getUniqueChild(resourceRef, "res-ref-name"));
			ResourceRefMetaData resourceRefMetaData = (ResourceRefMetaData)resourceReferences.get(resRefName);
            
            if (resourceRefMetaData == null) {
                throw new DeploymentException("resource-ref " + resRefName + " found in jboss.xml but not in ejb-jar.xml");
            }
            resourceRefMetaData.importJbossXml(resourceRef);
		}
		
		// set the external ejb-references (optional)
		iterator = getChildrenByTagName(element, "ejb-ref");
		while (iterator.hasNext()) {
			Element ejbRef = (Element)iterator.next();
			String ejbRefName = getElementContent(getUniqueChild(ejbRef, "ejb-ref-name"));
			EjbRefMetaData ejbRefMetaData = getEjbRefByName(ejbRefName);
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
