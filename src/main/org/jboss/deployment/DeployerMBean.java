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
* @version $Revision: 1.7 $
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
   * Provides a filter that decides whether a file can be deployed by
   * this deployer based on the filename.  This is for the benefit of
   * the {@link org.jboss.deployer.AutoDeployer} service.
   *
   * @return a <tt>FilenameFilter</tt> that only
   *         <tt>accept</tt>s files with names that can be
   *         deployed by this deployer
   */
   public boolean accepts(DeploymentInfo sdi);
   
   
   
   /*
   * Init a deployment 
   *
   * parse the XML MetaData.  Init and deploy are separate steps allowing for subDeployment
   * in between.  
   *
   * @param url    The URL to deploy.
   *
   * @throws MalformedURLException    Invalid URL
   * @throws IOException              Failed to fetch content
   * @throws DeploymentException      Failed to deploy
   */
   public void init(DeploymentInfo sdi)
   throws DeploymentException;
   
   
   /**
   * Deploy a given URL.
   *
   * @param url    The URL to deploy.
   *
   * @throws MalformedURLException    Invalid URL
   * @throws IOException              Failed to fetch content
   * @throws DeploymentException      Failed to deploy
   */
   public void deploy(DeploymentInfo sdi)
   throws DeploymentException;
   
   /**
   * Undeploy a given URL.
   *
   * @param url    The URL to undeploy.
   *
   * @throws MalformedURLException    Invalid URL
   * @throws IOException              Failed to fetch content
   * @throws DeploymentException      Failed to undeploy
   */
   public void undeploy(DeploymentInfo sdi)
   throws DeploymentException;
}
