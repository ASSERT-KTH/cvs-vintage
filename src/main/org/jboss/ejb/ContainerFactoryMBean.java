/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.MalformedURLException;

/**
 *   This is the interface of the ContainerFactory that is exposed for administration
 *
 *   @see ContainerFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author Juha Lindfors (jplindfo@helsinki.fi)
 *
 *   @version $Revision: 1.11 $
 */
public interface ContainerFactoryMBean
	extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static String OBJECT_NAME = ":service=ContainerFactory";

   // Public --------------------------------------------------------

   /**
    * Returns the applications deployed by the container factory
    */
   public java.util.Iterator getDeployedApplications();
   /**
	 *	Deploy an application
	 *
	 * @param   url URL to the directory with the given EJBs to be deployed
    * @param   appId Id of the application this EJBs belongs to
    *                used for management
	 * @exception   MalformedURLException
	 * @exception   DeploymentException
	 */
   public void deploy(String url, String appId )
      throws MalformedURLException, DeploymentException;

   /**
	 *	Deploy an application
	 *
    * @param   appUrl Url to the application itself
    * @param   jarUrls Array of URLs to the JAR files containing the EJBs
    * @param   appId Id of the application this EJBs belongs to
    *                used for management
	 * @exception   MalformedURLException
	 * @exception   DeploymentException
	 */
   public void deploy( String appUurl, String[] jarUrls, String appId )
      throws MalformedURLException, DeploymentException;

	/**
	 *	Undeploy an application
	 *
	 * @param   url
	 * @exception   MalformedURLException
	 * @exception   DeploymentException
	 */
   public void undeploy(String url)
      throws MalformedURLException, DeploymentException;

   /**
    * Enable/disable bean verification upon deployment.
    *
    * @param    verify  true to enable the verifier; false to disable
    */
   public void setVerifyDeployments(boolean verify);

   /**
    * Returns the state of the verifier (enabled/disabled)
    *
    * @return   true if verifier is enabled; false otherwise
    */
   public boolean getVerifyDeployments();

   /**
    * Enable/disable bean verifier verbose mode.
    *
    * @param    verbose true to enable verbose mode; false to disable
    */
   public void setVerifierVerbose(boolean verbose);

   /**
    * Returns the state of the verifier (verbose/non-verbose mode).
    *
    * @return  true if the verbose mode is enabled; false otherwise
    */
   public boolean getVerifierVerbose();

   /**
   * Enables/disables the metrics interceptor for containers.
   *
   * @param enable  true to enable; false to disable
   */
   public void setMetricsEnabled(boolean enable);

   /**
    * Checks if this container factory initializes the metrics interceptor.
    *
    * @return   true if metrics are enabled; false otherwise
    */
   public boolean isMetricsEnabled();

	/**
	 *	is the aplication with this url deployed
	 *
	 * @param   url
	 * @exception   MalformedURLException
	 */
   public boolean isDeployed(String url)
      throws MalformedURLException;

   /**
    * Set the JMS monitoring of the bean cache.
    */
   public void setBeanCacheJMSMonitoringEnabled(boolean enable);
}

