/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.ejb.DeploymentException;


/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.5 $
 */
public class ApplicationMetaData extends MetaData {
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	private URL url;

	private ArrayList beans = new ArrayList();
	private ArrayList securityRoles = new ArrayList();
	private ArrayList configurations = new ArrayList();
	private HashMap resources = new HashMap();

	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	public ApplicationMetaData (URL u) {
		url = u;
	}
	
	public ApplicationMetaData () {
	}
	                                 	
	// Public --------------------------------------------------------
	public URL getUrl() { return url; }

	public void setUrl(URL u) { url = u; } 
	
	public Iterator getEnterpriseBeans() {
		return beans.iterator(); 
	}
	
	public BeanMetaData getBeanByEjbName(String ejbName) {
		Iterator iterator = getEnterpriseBeans();
		while (iterator.hasNext()) {
			BeanMetaData current = (BeanMetaData) iterator.next();
			if (current.getEjbName().equals(ejbName)) return current;
		}
		// not found
		return null;
	}
	
	public Iterator getConfigurations() {
		return configurations.iterator();
	}
	
	public ConfigurationMetaData getConfigurationMetaDataByName(String name) {
		Iterator iterator = getConfigurations();
		while (iterator.hasNext()) {
			ConfigurationMetaData current = (ConfigurationMetaData) iterator.next();
			if (current.getName().equals(name)) return current;
		}
		// not found
		return null;
	}
	
	public String getResourceByName(String name) {
		// if not found, the container will use default
		return (String)resources.get(name);
	}
	
	
	public void importEjbJarXml (Element element) throws DeploymentException {
		
		// find the beans		
		Element enterpriseBeans = getUniqueChild(element, "enterprise-beans");
		
		// entities
		Iterator iterator = getChildrenByTagName(enterpriseBeans, "entity");
		while (iterator.hasNext()) {
			Element currentEntity = (Element)iterator.next();
			EntityMetaData entityMetaData = new EntityMetaData(this);
			try {
				entityMetaData.importEjbJarXml(currentEntity);
			} catch (DeploymentException e) {
				throw new DeploymentException("Error in ejb-jar.xml for Entity Bean " + entityMetaData.getEjbName() + ": " + e.getMessage());
			}
			beans.add(entityMetaData);
		}
		
		// sessions
		iterator = getChildrenByTagName(enterpriseBeans, "session");
		while (iterator.hasNext()) {
			Element currentSession = (Element)iterator.next();
			SessionMetaData sessionMetaData = new SessionMetaData(this);
			try {
				sessionMetaData.importEjbJarXml(currentSession);
			} catch (DeploymentException e) {
				throw new DeploymentException("Error in ejb-jar.xml for Session Bean " + sessionMetaData.getEjbName() + ": " + e.getMessage());
			}
			beans.add(sessionMetaData);
		}
		
		// read the assembly descriptor (optional)
		Element assemblyDescriptor = getOptionalChild(element, "assembly-descriptor");
		
		if (assemblyDescriptor != null) {
			
			// set the security roles (optional)
			iterator = getChildrenByTagName(assemblyDescriptor, "security-role");
			while (iterator.hasNext()) {
				Element securityRole = (Element)iterator.next(); 
				try {
					String role = getElementContent(getUniqueChild(securityRole, "role-name"));
					securityRoles.add(role);
				} catch (DeploymentException e) {
					throw new DeploymentException("Error in ejb-jar.xml for security-role: " + e.getMessage());
				}
			}
				
			// set the method permissions (optional)
			iterator = getChildrenByTagName(assemblyDescriptor, "method-permission");
			try {
				while (iterator.hasNext()) {
					Element methodPermission = (Element)iterator.next();
				
					// find the list of roles
					Set roles = new HashSet();
					Iterator rolesIterator = getChildrenByTagName(methodPermission, "role-name");
					while (rolesIterator.hasNext()) {
						roles.add(getElementContent((Element)rolesIterator.next()));
					}
				
					// find the methods
					Iterator methods = getChildrenByTagName(methodPermission, "method");
					while (methods.hasNext()) {
					
						// load the method
						MethodMetaData method = new MethodMetaData();
						method.importEjbJarXml((Element)methods.next());
	            	    method.setRoles(roles);
						
						// give the method to the right bean
						BeanMetaData bean = getBeanByEjbName(method.getEjbName());
						if (bean == null) {
							throw new DeploymentException(method.getEjbName() + " doesn't exist");
						}
						bean.addPermissionMethod(method);
					}
				}
			} catch (DeploymentException e) {
				throw new DeploymentException("Error in ejb-jar.xml, in method-permission: " + e.getMessage());
			}
				
			// set the container transactions (optional)
	    	iterator = getChildrenByTagName(assemblyDescriptor, "container-transaction");
			try {
				while (iterator.hasNext()) {
					Element containerTransaction = (Element)iterator.next();
					
					// find the type of the transaction
					byte transactionType;
					String type = getElementContent(getUniqueChild(containerTransaction, "trans-attribute"));
					if (type.equals("NotSupported")) {
						transactionType = TX_NOT_SUPPORTED;
					} else if (type.equals("Supports")) {
						transactionType = TX_SUPPORTS;
					} else if (type.equals("Required")) {
						transactionType = TX_REQUIRED;
					} else if (type.equals("RequiresNew")) {
						transactionType = TX_REQUIRES_NEW;
					} else if (type.equals("Mandatory")) {
						transactionType = TX_MANDATORY;
					} else if (type.equals("Never")) {
						transactionType = TX_NEVER;
					} else {
						throw new DeploymentException("invalid transaction-attribute : " + type);
					}
        			// find the methods
					Iterator methods = getChildrenByTagName(containerTransaction, "method");
					while (methods.hasNext()) {
						
						// load the method
						MethodMetaData method = new MethodMetaData();
						method.importEjbJarXml((Element)methods.next());
		                method.setTransactionType(transactionType);
						
						// give the method to the right bean
						BeanMetaData bean = getBeanByEjbName(method.getEjbName());
						if (bean == null) {
							throw new DeploymentException("bean " + method.getEjbName() + " doesn't exist");
						}
						bean.addTransactionMethod(method);
					}
				}
			} catch (DeploymentException e) {
				throw new DeploymentException("Error in ejb-jar.xml, in container-transaction: " + e.getMessage());
			}
		}
	}

	
	
	public void importJbossXml(Element element) throws DeploymentException {
		Iterator iterator;
		
		// all the tags are optional
		
		// find the container configurations (we need them first to use them in the beans)
		Element confs = getOptionalChild(element, "container-configurations");
		if (confs != null) {
			iterator = getChildrenByTagName(confs, "container-configuration");
			while (iterator.hasNext()) {
				Element conf = (Element)iterator.next();
				ConfigurationMetaData configurationMetaData = new ConfigurationMetaData();
				try {
					configurationMetaData.importJbossXml(conf);
				} catch (DeploymentException e) {
					throw new DeploymentException("Error in jboss.xml for container-configuration " + configurationMetaData.getName() + ": " + e.getMessage());
				}
				configurations.add(configurationMetaData);
			}
		}
		
		// update the enterprise beans
		Element entBeans = getOptionalChild(element, "enterprise-beans");
		if (entBeans != null) {
			String ejbName = null;
			try {
				iterator = getChildrenByTagName(entBeans, "entity");
				while (iterator.hasNext()) {
					Element bean = (Element) iterator.next();
					ejbName = getElementContent(getUniqueChild(bean, "ejb-name"));
					BeanMetaData beanMetaData = getBeanByEjbName(ejbName);
					if (beanMetaData == null) {
						throw new DeploymentException("found in jboss.xml but not in ejb-jar.xml");
					}
					beanMetaData.importJbossXml(bean);
				}
					
				iterator = getChildrenByTagName(entBeans, "session");
				while (iterator.hasNext()) {
					Element bean = (Element) iterator.next();
					ejbName = getElementContent(getUniqueChild(bean, "ejb-name"));
					BeanMetaData beanMetaData = getBeanByEjbName(ejbName);
					if (beanMetaData == null) {
						throw new DeploymentException("found in jboss.xml but not in ejb-jar.xml");
					}
					beanMetaData.importJbossXml(bean);
				}
				
			} catch (DeploymentException e) {
				throw new DeploymentException("Error in jboss.xml for Bean " + ejbName + ": " + e.getMessage());
			}
		}
		
		// set the resource managers
	    Element resmans = getOptionalChild(element, "resource-managers");
		if (resmans != null) {
			iterator = getChildrenByTagName(resmans, "resource-manager");
			try {
				while (iterator.hasNext()) {
					Element resourceManager = (Element)iterator.next();
					String resName = getElementContent(getUniqueChild(resourceManager, "res-name"));
				
					String jndi = getElementContent(getOptionalChild(resourceManager, "res-jndi-name"));
					String url = getElementContent(getOptionalChild(resourceManager, "res-url"));
				
					if (jndi != null && url == null) {
						resources.put(resName, jndi);
					} else if (jndi == null && url != null) {
						resources.put(resName, url);
					} else {
						throw new DeploymentException(resName + " : expected res-url or res-jndi-name tag");
					}
				}
			} catch (DeploymentException e) {
				throw new DeploymentException("Error in jboss.xml, in resource-manager: " + e.getMessage());
			}
		}
	}
	
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
}
