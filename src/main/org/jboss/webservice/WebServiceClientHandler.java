/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WebServiceClientHandler.java,v 1.2 2005/01/27 18:26:21 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.ObjectNameFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2004
 */
public class WebServiceClientHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(WebServiceClientHandler.class);

   /**
    * This binds a jaxrpc Service into the callers ENC for every service-ref element
    *
    * @param envCtx      ENC to bind the javax.rpc.xml.Service object to
    * @param serviceRefs An iterator of the service-ref elements in the client deployment descriptor
    * @param di          The client's deployment info
    * @throws org.jboss.deployment.DeploymentException if it goes wrong
    */
   public static void setupServiceRefEnvironment(Context envCtx, Iterator serviceRefs, DeploymentInfo di) throws DeploymentException
   {
      // nothing to do
      if (serviceRefs.hasNext() == false)
         return;

      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName ws4eeObjectName = ObjectNameFactory.create("jboss.ws4ee:service=ServiceClientDeployer");
      ObjectName jbosswsObjectName = ObjectNameFactory.create("jboss.ws:service=WebServiceClientDeployer");

      ObjectName objectName = null;
      WebServiceClientDeployment wsClientDeployment;
      try
      {
         if (server.isRegistered(ws4eeObjectName))
         {
            objectName = ws4eeObjectName;
            wsClientDeployment = (WebServiceClientDeployment)MBeanProxy.get(WebServiceClientDeployment.class, ws4eeObjectName, server);
         }
         else if (server.isRegistered(jbosswsObjectName))
         {
            objectName = jbosswsObjectName;
            wsClientDeployment = (WebServiceClientDeployment)MBeanProxy.get(WebServiceClientDeployment.class, jbosswsObjectName, server);
         }
         else
         {
            log.warn("No web service client deployer registered");
            return;
         }
      }
      catch (MBeanProxyCreationException e)
      {
         throw new DeploymentException("Cannot create proxy to the web service client deployer: " + objectName);
      }

      // Delegate to the web service client deloyer
      wsClientDeployment.setupServiceRefEnvironment(envCtx, serviceRefs, di);
   }
}
