/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.MalformedURLException;

public interface ContainerFactoryMBean
{
   // Constants -----------------------------------------------------
   public static String OBJECT_NAME = "EJB:service=ContainerFactory";
    
   // Public --------------------------------------------------------
   public void deploy(String url)
      throws MalformedURLException, DeploymentException;
      
   public void undeploy(String url)
      throws MalformedURLException, DeploymentException;
}

