/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ServiceRefBuilder.java,v 1.1 2004/04/27 15:55:43 tdiesler Exp $
package org.jboss.webservice;

// $Id: ServiceRefBuilder.java,v 1.1 2004/04/27 15:55:43 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.metadata.ServiceRefMetaData;
import org.jboss.naming.Util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Referenceable;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * A deployer service that manages WS4EE compliant webservice clients,
 *
 * This service receives deployment notifications from the ClientDeployer and registers
 * the services in the client's environment.
 *
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public final class ServiceRefBuilder
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceRefBuilder.class);

   public static void setupEnvironment(Context envCtx, Iterator serviceRefs, DeploymentInfo di) throws DeploymentException
   {
      try
      {
         while (serviceRefs.hasNext())
         {
            ServiceRefMetaData serviceRef = (ServiceRefMetaData) serviceRefs.next();

            Definition wsdl = serviceRef.getWsdlDefinition();
            if (wsdl.getServices().size() < 1)
               throw new NamingException("Cannot find service entry in WSDL");
            if (wsdl.getServices().size() > 1)
               throw new NamingException("Found more than one service entry in WSDL");

            // get the service name from the WSDL definition
            QName serviceName = (QName) wsdl.getServices().keySet().iterator().next();

            Referenceable serviceReferenceable = null;
            String serviceRefName = serviceRef.getServiceRefName();
            String serviceInterface = serviceRef.getServiceInterface();
            if (serviceRef.getWsdlOverride() != null)
               serviceReferenceable = new ServiceReferenceable(serviceName, serviceRef.getWsdlOverride());
            else
               serviceReferenceable = new ServiceReferenceable(serviceName, di.url, serviceRef.getWsdlFile());

            Util.bind(envCtx, serviceRefName, serviceReferenceable);
            log.info("Webservice binding: java:comp/env/" + serviceRefName);
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Cannot bind webservice to client environment", e);
      }
   }
}
