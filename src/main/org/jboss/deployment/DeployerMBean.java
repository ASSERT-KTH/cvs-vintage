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
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.6 $
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
   FilenameFilter getDeployableFilter();

   /**
    * Deploy a given URL.
    *
    * @param url    The URL to deploy.
    *
    * @throws MalformedURLException    Invalid URL
    * @throws IOException              Failed to fetch content
    * @throws DeploymentException      Failed to deploy
    */
   void deploy(String url)
      throws MalformedURLException, IOException, DeploymentException;

   /**
    * Undeploy a given URL.
    *
    * @param url    The URL to undeploy.
    *
    * @throws MalformedURLException    Invalid URL
    * @throws IOException              Failed to fetch content
    * @throws DeploymentException      Failed to undeploy
    */
   void undeploy(String url)
      throws MalformedURLException, IOException, DeploymentException;

   /**
    * Check if a URL corresponds to a deployment.
    *
    * @param url    The URL to check.
    * @return       True if the URL corresponds to a deployment.
    *
    * @throws MalformedURLException    Invalid URL
    * @throws DeploymentException      Failed to undeploy
    */
   boolean isDeployed(String url)
      throws MalformedURLException, DeploymentException;
}
