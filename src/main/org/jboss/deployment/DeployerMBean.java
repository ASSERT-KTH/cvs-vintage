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

import org.jboss.util.ServiceMBean;

/**
 *   The common interface for services that deploy application
 *   components.
 *
 *   @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *   @version $Revision: 1.4 $
 */
public interface DeployerMBean
   extends ServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
   
   /**
    * Provides a filter that decides whether a file can be deployed by
    * this deployer based on the filename.  This is for the benefit of
    * the {@link org.jboss.ejb.AutoDeployer} service.
    *
    * @return a <code>FilenameFilter</code> that only
    *         <code>accept</code>s files with names that can be
    *         deployed by this deployer
    */
   FilenameFilter getDeployableFilter();
   
   void deploy (String url)
      throws MalformedURLException, IOException, DeploymentException;

   void undeploy (String url)
      throws MalformedURLException, IOException, DeploymentException;

   boolean isDeployed (String url)
      throws MalformedURLException, DeploymentException;
}
