/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tomcat;

import org.jboss.ejb.DeploymentException;


/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.1 $
 */
public interface EmbededTomcatServiceMBean extends org.jboss.util.ServiceMBean {
	
	// Constants -----------------------------------------------------
	public static final String OBJECT_NAME = ":service=EmbededTomcat";
	
	// Public --------------------------------------------------------
	public void deploy(String ctxPath, String warUrl) throws DeploymentException;
	
	public void undeploy(String warUrl) throws DeploymentException;
	
	public boolean isDeployed(String warUrl);

}
