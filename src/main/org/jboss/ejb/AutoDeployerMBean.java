/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.util.ServiceMBean;

/**
 *   This is the interface of the AutoDeployer that is exposed for administration
 *      
 *   @see AutoDeployer
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public interface AutoDeployerMBean
	extends ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "EJB:service=AutoDeployer";
    
   // Public --------------------------------------------------------
   public void setURLs(String urlList);
   public String getURLs();
   public void setDeployer(String deployer);
   public String getDeployer();
}

