/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment.scope;

import org.jboss.deployment.J2eeDeployerMBean;
import org.jboss.deployment.DeploymentException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 *   The JMX interface for scoped deployment services
 *
 *   @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 *   @version $Revision: 1.2 $
 */
public interface J2eeGlobalScopeDeployerMBean extends J2eeDeployerMBean
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
   

   Scope getScope(String name);

   void deploy(String url, String scopeName)    
	throws MalformedURLException, IOException, DeploymentException;

   Scope createScope(String name) throws Exception;

   String[] listScopes();

}
