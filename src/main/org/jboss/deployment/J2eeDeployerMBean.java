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
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.4 $
 */
public interface J2eeDeployerMBean
   extends DeployerMBean
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
}
