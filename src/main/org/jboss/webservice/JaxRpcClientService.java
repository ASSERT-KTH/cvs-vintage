/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: JaxRpcClientService.java,v 1.5 2004/05/04 08:48:43 tdiesler Exp $
package org.jboss.webservice;

// $Id: JaxRpcClientService.java,v 1.5 2004/05/04 08:48:43 tdiesler Exp $

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.FileProvider;
import org.jboss.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Lookup EngineConfiguration configure the the jaxrpc service with it.
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
   public JaxRpcClientService()
   {
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
    * Get the AxisClient engine.
    *
    * Use the {@link EngineConfigurationFinder#getClientEngineConfiguration()} to discover the client
    * engine configuration. If it cannot be found, fall back to the Axis default engine.
    */
   protected AxisClient getAxisClient()
   {
      EngineConfiguration config = EngineConfigurationFinder.getClientEngineConfiguration();
      if (config != null)
         return new AxisClient(config);
      else
         return super.getAxisClient();
   }
}
