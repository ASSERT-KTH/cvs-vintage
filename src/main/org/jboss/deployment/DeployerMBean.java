/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.io.IOException;
import java.net.MalformedURLException;

import org.jboss.util.ServiceMBean;

/**
 *   The common interface for services that deploy application
 *   components.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public interface DeployerMBean
   extends ServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
   
   void deploy (String url)
      throws MalformedURLException, IOException, DeploymentException;

   void undeploy (String url)
      throws MalformedURLException, IOException, DeploymentException;

   boolean isDeployed (String url)
      throws MalformedURLException, DeploymentException;
}
