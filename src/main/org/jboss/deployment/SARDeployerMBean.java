/*
 * JBoss, the OpenSource J2EE server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;

import org.jboss.system.Service;

/** 
 * This is the main Service Deployer API.
 *   
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @see org.jboss.system.Service
 *
 * @version $Revision: 1.3 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *    <li>initial import
 * </ul>
 */
public interface SARDeployerMBean
   extends Service, DeployerMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system", "service", "ServiceDeployer");
}
