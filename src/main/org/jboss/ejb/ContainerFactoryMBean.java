/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.MalformedURLException;

/**
 *   This is the interface of the ContainerFactory that is exposed for administration
 *      
 *   @see ContainerFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface ContainerFactoryMBean
{
   // Constants -----------------------------------------------------
   public static String OBJECT_NAME = "EJB:service=ContainerFactory";
    
   // Public --------------------------------------------------------

	/**
	 *	Deploy an application
	 *
	 * @param   url  
	 * @exception   MalformedURLException  
	 * @exception   DeploymentException  
	 */
   public void deploy(String url)
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
}

