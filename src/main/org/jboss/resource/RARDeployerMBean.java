/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import org.jboss.deployment.DeployerMBean;

/**
 *   <description> 
 *
 *   @see <related>
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public interface RARDeployerMBean
   extends DeployerMBean
{
   // Constants -----------------------------------------------------

   String OBJECT_NAME = ":service=RARDeployer";
    
   // Public --------------------------------------------------------

//     public RARMetaData getMetaData(String resourceAdapterName);
}
