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
 * ???
 *
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.10 $
 */
public interface J2eeDeployerMBean
   extends DeployerMBean
{
   String OBJECT_NAME = "jboss.j2ee:service=J2eeDeployer";
    
   void setDeployerName(String name);
   String getDeployerName();
   
   void setJarDeployer(ObjectName jarDeployer);
   ObjectName getJarDeployer();
   
   void setWarDeployer(ObjectName warDeployer);
   ObjectName getWarDeployer();
   
   void setRarDeployer(ObjectName rarDeployer);
   ObjectName getRarDeployer();
   
   void setServiceDeployer(ObjectName javaDeployer);
   ObjectName getServiceDeployer();
}
