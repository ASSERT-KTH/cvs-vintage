/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WebServiceClientHandler.java,v 1.1 2004/05/05 16:38:37 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.mx.util.MBeanServerLocator;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ReflectionException;
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
      if (server.isRegistered(WebServiceClientDeployer.OBJECT_NAME) == false)
         throw new DeploymentException("Web service client deployer not registered: " + WebServiceClientDeployer.OBJECT_NAME);

      try
      {
         server.invoke(WebServiceClientDeployer.OBJECT_NAME,
                 "setupServiceRefEnvironment",
                 new Object[]{envCtx, serviceRefs, di},
                 new String[]{Context.class.getName(), Iterator.class.getName(), DeploymentInfo.class.getName()});
      }
      catch (Exception ex)
      {
         Exception targetException = ex;
         if (ex instanceof MBeanException)
            targetException = ((MBeanException)ex).getTargetException();
         if (ex instanceof ReflectionException)
            targetException = ((ReflectionException)ex).getTargetException();
         throw new DeploymentException("Cannot setup web service client ENC", targetException);
      }
   }
}
