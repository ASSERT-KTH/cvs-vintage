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
public class ConfigurationMetaData extends MetaData {
    
	// Constants -----------------------------------------------------
    public static final String DEFAULT_CMP = "Default CMP EntityBean";
	public static final String DEFAULT_BMP = "Default BMP EntityBean";
	public static final String DEFAULT_STATELESS = "Default Stateless SessionBean";
	public static final String DEFAULT_STATEFUL = "Default Stateful SessionBean";
	
	public static final byte A_COMMIT_OPTION = 0;
	public static final byte B_COMMIT_OPTION = 1;
	public static final byte C_COMMIT_OPTION = 2;
	
	
    // Attributes ----------------------------------------------------
	private String name;
	private String containerInvoker;
	private String instancePool;
	private String instanceCache;
	private String persistenceManager;
	private String transactionManager;
    private byte commitOption;
	private boolean callLogging;
	
	// TODO security manager
	// TODO realm mapping
	
	private Element containerInvokerConf;
	private Element containerPoolConf;
	private Element containerCacheConf;
	
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public ConfigurationMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getName() { return name; }
	
	public String getContainerInvoker() { return containerInvoker; }
	
	public String getInstancePool() { return instancePool; }
	
	public String getInstanceCache() { return instanceCache; }
	
	public String getPersistenceManager() { return persistenceManager; }
	
	public String getTransactionManager() { return transactionManager; }
	
    public Element getContainerInvokerConf() { return containerInvokerConf; }
	public Element getContainerPoolConf() { return containerPoolConf; }
	public Element getContainerCacheConf() { return containerCacheConf; }
	
	public boolean getCallLogging() { return callLogging; }
	
	public byte getCommitOption() { return commitOption; }
	
	
	public void importJbossXml(Element element) throws DeploymentException {
		
		// set the configuration name
		name = getElementContent(getUniqueChild(element, "container-name"));
		
		// set call logging
		callLogging = Boolean.valueOf(getElementContent(getUniqueChild(element, "call-logging"))).booleanValue();
		
		// set the container invoker
		containerInvoker = getElementContent(getUniqueChild(element, "container-invoker"));
		
		// set the instance pool
		instancePool = getElementContent(getOptionalChild(element, "instance-pool"));
		
		// set the instance cache
		instanceCache = getElementContent(getOptionalChild(element, "instance-cache"));
		
		// set the persistence manager
		persistenceManager = getElementContent(getOptionalChild(element, "persistence-manager"));
		
		// set the transaction manager
		transactionManager = getElementContent(getOptionalChild(element, "transaction-manager"));

        // set the commit option
		String commit = getElementContent(getOptionalChild(element, "commit-option"));
		if (commit != null) {
			if (commit.equals("A")) {
				commitOption = A_COMMIT_OPTION;
			} else if (commit.equals("B")) {
				commitOption = B_COMMIT_OPTION;
			} else if (commit.equals("C")) {
				commitOption = C_COMMIT_OPTION;
			} else throw new DeploymentException("Invalid commit option");
		}
		
		// the classes which can understand the following are dynamically loaded during deployment : 
		// We save the Elements for them to use later
		
		// configuration for container invoker
	    containerInvokerConf = getOptionalChild(element, "container-invoker-conf");
		
		// configuration for instance pool
		containerPoolConf = getOptionalChild(element, "container-pool-conf");
		
		// configuration for instance cache
		containerCacheConf = getOptionalChild(element, "container-cache-conf");
		
	}		
    
	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
