package org.jboss.web;

import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBean;

/** A template pattern for web container integration into JBoss.

@author  Scott.Stark@jboss.org
@version $Revision: 1.5 $
*/
public interface AbstractWebContainerMBean extends ServiceMBean
{
    public void deploy(String ctxPath, String warUrl) throws DeploymentException;
    public void undeploy(String warUrl) throws DeploymentException;
    public boolean isDeployed(String warUrl);
   /** Returns the applications deployed by the container factory
    @return An Iterator of WebApplication objects for the deployed wars.
    */
   public Iterator getDeployedApplications();
   /** Allow the import of an arbitrary XML configuration tree
    */
   public void importXml(Element config);
}
