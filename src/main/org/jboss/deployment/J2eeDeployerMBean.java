/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.MalformedURLException;
import java.io.IOException;

import org.jboss.util.ServiceMBean;

/**
 *   @see 
 *   @author Daniel Schulze (daniel.schulze@telkel.com)
 *   @version $Revision: 1.3 $
 */
public interface J2eeDeployerMBean
	extends ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "J2EE:service=J2eeDeployer";
    
   // Public --------------------------------------------------------
   public void setDeployerName(String name);
   public String getDeployerName();
   
   public void setJarDeployerName(String jarDeployerName);
   public String getJarDeployerName();
   
   public void setWarDeployerName(String warDeployerName);
   public String getWarDeployerName();
   
   public void deploy (String url) throws MalformedURLException, IOException, J2eeDeploymentException;

   public void undeploy (String url) throws MalformedURLException, IOException, J2eeDeploymentException;

   public boolean isDeployed (String url) throws MalformedURLException, J2eeDeploymentException;
}
