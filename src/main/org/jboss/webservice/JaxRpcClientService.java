/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: JaxRpcClientService.java,v 1.2 2004/04/27 18:44:51 tdiesler Exp $
package org.jboss.webservice;

// $Id: JaxRpcClientService.java,v 1.2 2004/04/27 18:44:51 tdiesler Exp $

import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.FileProvider;
import org.jboss.logging.Logger;

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
   public static final String AXIS_CLIENT_CONFIG = "META-INF/axis-client-config.xml";

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
      // load axis client config from resource
      ClassLoader cl = getClass().getClassLoader();
      InputStream configStream = cl.getResourceAsStream(AXIS_CLIENT_CONFIG);
      if (configStream != null)
         return new AxisClient(new FileProvider(configStream));

      // fall back to Axis discovery of the client config
      log.warn("Cannot load '" + AXIS_CLIENT_CONFIG + "', using default");
      return super.getAxisClient();
   }
}
