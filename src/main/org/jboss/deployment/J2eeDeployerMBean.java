/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.MalformedURLException;
import java.io.IOException;

import org.jboss.system.ServiceMBean;

/**
 *   @see 
 *   @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 *   @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *   @version $Revision: 1.8 $
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
   
   public void setRarDeployerName(String rarDeployerName);
   public String getRarDeployerName();
   
   public void setJavaDeployerName(String javaDeployerName);
   public String getJavaDeployerName();

}
