/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.deployment;

import java.io.IOException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

import org.jboss.system.ServiceMBean;

/**
* The common interface for services that deploy application
* components.
*
* This is to be called by the MainDeployer.  Use the MainDeployer to deploy application
*
* @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
* @author <a href="mailto:marc.fleury@Jboss.org">Marc Fleury</a>
* @version $Revision: 1.8 $
*
*   <p><b>20011225 marc fleury:</b>
*      <ul>
*      <li>Deployer Unification, the main deployer is the point of entry this interface
*       is reserved for "sub-deployers" and the String URL-> DeploymentInfo
*      </ul>
*/
public interface DeployerMBean
extends ServiceMBean
{
   /**
    * The <code>accepts</code> method is called by MainDeployer to 
    * determine which deployer is suitable for a DeploymentInfo.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @return a <code>boolean</code> value
    */
   boolean accepts(DeploymentInfo sdi);
   
   
   
   /**
    * The <code>init</code> method lets the deployer set a few properties
    * of the DeploymentInfo, such as the watch url.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void init(DeploymentInfo sdi)
   throws DeploymentException;
   
   
   /**
   * Set up the components of the deployment that do not 
   * refer to other components
   *
   *
    * @param sdi a <code>DeploymentInfo</code> value
   * @throws DeploymentException      Failed to deploy
   */
   void create(DeploymentInfo sdi)
   throws DeploymentException;
   
   /**
    * The <code>start</code> method sets up relationships with other components.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void start(DeploymentInfo sdi)
   throws DeploymentException;
   
   /**
    * The <code>stop</code> method removes relationships between components.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void stop(DeploymentInfo sdi)
   throws DeploymentException;


   /**
    * The <code>destroy</code> method removes individual components
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void destroy(DeploymentInfo sdi)
   throws DeploymentException;
}
