package org.jboss.web;

import java.util.Iterator;

import org.jboss.ejb.DeploymentException;

/** A template pattern for web container integration into JBoss.

@author  <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.2 $
*/
public interface AbstractWebContainerMBean extends org.jboss.util.ServiceMBean
{
    public abstract void deploy(String ctxPath, String warUrl) throws DeploymentException;
    public abstract void undeploy(String warUrl) throws DeploymentException;
    public abstract boolean isDeployed(String warUrl);
   /** Returns the applications deployed by the container factory
    @return An Iterator of WebApplication objects for the deployed wars.
    */
   public Iterator getDeployedApplications();
}
