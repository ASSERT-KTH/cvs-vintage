/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;

import java.net.URL;
import java.util.Collection;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBean;
import org.jboss.util.SafeObjectNameFactory;

/**
* This is the interface of the AutoDeployer that is exposed for
* administration
*      
* @see AutoDeployer
* 
* @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
* @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.4 $
*      <p><b>20011223 marc fleury:</b>
*      <ul> 
*      <li>add/RemoveURL, added the capacity to dynamically add a URL to watch
*      <li>add/Removedeployer, dynamically add a new deployer
*      <li>Changed ObjectName to JBOSs-SYSTEM realm (from EJB realm)
*      <li>Rewrite
*      </ul>
*/
public interface MainDeployerMBean
   extends ServiceMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME = SafeObjectNameFactory.create("jboss.system",
                                                     "service", 
                                                     "MainDeployer");
   
   
   /** individual URLs for dynamically deploying **/
   void deploy(String URL);
   void undeploy(String URL);
   
   void deploy(DeploymentInfo sdi)
   throws DeploymentException;
   
   public void undeploy(DeploymentInfo sdi)
   throws DeploymentException;
   
   
   /** Dynamically add directories to scan **/
   void addDirectory(String url);
   void removeDirectory(String url);
   
   /** Add the capacity to dynamically add a deployer to the list **/
   void addDeployer(DeployerMBean deployer);
   void removeDeployer(DeployerMBean deployer);
   
   /** get all the watched URLs in this deployment **/
   Collection listDeployed();
   
   /** Get set scan-period**/
   void setScan( boolean scan);
   boolean getScan();
   void setPeriod(int period);
   int getPeriod();   
}

