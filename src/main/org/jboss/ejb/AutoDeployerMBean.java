/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.util.ServiceMBean;

/**
 *   This is the interface of the AutoDeployer that is exposed for administration
 *      
 *   @see AutoDeployer
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface AutoDeployerMBean
	extends ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "EJB:service=AutoDeployer";
    
   // Public --------------------------------------------------------
   public void addURLs(String urlList);
}

