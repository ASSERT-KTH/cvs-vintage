/*
 * JBoss, the OpenSource J2EE server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import org.jboss.system.Service;

/** 
 * This is the main Service Deployer API.
 *   
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @see org.jboss.system.Service
 *
 * @version $Revision: 1.3 $
 *
 *   <p><b>20010830 marc fleury:</b>
 *   <ul>
 *      initial import
 *   <li> 
 *   </ul>
 */
public interface ServiceDeployerMBean
    extends Service, DeployerMBean
{
   /** The default object name. */
   String OBJECT_NAME = "jboss.system:service=ServiceDeployer";
}