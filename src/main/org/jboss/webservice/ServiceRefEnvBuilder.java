/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ServiceRefEnvBuilder.java,v 1.3 2004/04/30 16:24:46 tdiesler Exp $
package org.jboss.webservice;

// $Id: ServiceRefEnvBuilder.java,v 1.3 2004/04/30 16:24:46 tdiesler Exp $

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
 * Binds a JAXRPC Service object in the client's ENC for every service-ref element in the
 * deployment descriptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public final class ServiceRefEnvBuilder
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceRefEnvBuilder.class);

   /**
    * This scans the WSDL for the one and only service element
    *
    * @param envCtx ENC to bind the javax.rpc.xml.Service object to
    * @param serviceRefs An iterator of the service-ref elements in the client deployment descriptor
    * @param di The client's deployment info
    * @throws DeploymentException if it goes wrong
    */
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
            if (serviceRef.getWsdlOverride() != null)
            {
               log.debug("Using wsdl override: " + serviceRef.getWsdlOverride());
               serviceReferenceable = new ServiceReferenceable(serviceName, serviceRef.getWsdlOverride());
            }
            else
            {
               log.debug("Using wsdl file: " + serviceRef.getWsdlFile());
               serviceReferenceable = new ServiceReferenceable(serviceName, di.url, serviceRef.getWsdlFile());
            }

            Util.bind(envCtx, serviceRefName, serviceReferenceable);
            log.debug("Webservice binding: java:comp/env/" + serviceRefName);
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Cannot bind webservice to client environment", e);
      }
   }
}
