/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;


import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBean;

/**
 *   @see 
 *   @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 *   @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *   @version $Revision: 1.9 $
 */
public interface J2eeDeployerMBean
   extends DeployerMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "J2EE:service=J2eeDeployer";
    
   // Public --------------------------------------------------------
   public void setDeployerName(String name);
   public String getDeployerName();
   
   public void setJarDeployer(ObjectName jarDeployer);
   public ObjectName getJarDeployer();
   
   public void setWarDeployer(ObjectName warDeployer);
   public ObjectName getWarDeployer();
   
   public void setRarDeployer(ObjectName rarDeployer);
   public ObjectName getRarDeployer();
   
   public void setServiceDeployer(ObjectName javaDeployer);
   public ObjectName getServiceDeployer();

}
