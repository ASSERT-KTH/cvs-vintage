/*
* JBoss, the OpenSource J2EE server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;

import javax.management.ObjectName;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.jboss.system.Service;
/** 
* This is the main Service Deployer API.
*   
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @see org.jboss.system.Service
*
* @version $Revision: 1.1 $
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
   // Public --------------------------------------------------------
   
   /** The default object name. */
   public static final String OBJECT_NAME = "JBOSS-SYSTEM:service=ServiceDeployer";
   
}
