/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: JaxRpcClientService.java,v 1.1 2004/04/27 15:55:43 tdiesler Exp $
package org.jboss.webservice;

// $Id: JaxRpcClientService.java,v 1.1 2004/04/27 15:55:43 tdiesler Exp $

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Service;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.ObjectNameFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.io.InputStream;
import java.net.URL;

/**
 * Lookup EngineConfiguration from JMX and
 * configure the the jaxrpc service with it.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public class JaxRpcClientService extends Service
{
   // provide logging
   private static final Logger log = Logger.getLogger(JaxRpcClientService.class);

   /**
    * Constructs a new Service object - this assumes the caller will set
    * the appropriate fields by hand rather than getting them from the
    * WSDL.
    */
   public JaxRpcClientService(URL wsdlDoc, QName serviceName)
           throws ServiceException
   {
      super(wsdlDoc, serviceName);
   }

   /**
    * Constructs a new Service object for the service in the WSDL document
    * in the wsdlInputStream and serviceName parameters.  This is
    * just like the previous constructor but instead of reading the WSDL
    * from a file (or from a URL) it is in the passed in InputStream.
    *
    * @param  wsdlInputStream InputStream containing the WSDL
    * @param  serviceName     Qualified name of the desired service
    * @throws ServiceException If there's an error finding or parsing the WSDL
    */
   public JaxRpcClientService(InputStream wsdlInputStream, QName serviceName)
           throws ServiceException
   {
      super(wsdlInputStream, serviceName);
   }

   /**
    * Get the AxisClient from JMX
    * @return
    */
   protected AxisClient getAxisClient()
   {
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName axisService = ObjectNameFactory.create("jboss.webservice:service=AxisService");
         EngineConfiguration config = (EngineConfiguration) server.getAttribute(axisService, "ClientConfig");
         return new AxisClient(config);
      }
      catch (Exception e)
      {
         log.warn("Cannot access AxisService, using default client config");
      }

      // fall back to Axis discovery of the client config
      return super.getAxisClient();
   }
}
