/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ServiceObjectFactory.java,v 1.3 2004/05/04 08:48:43 tdiesler Exp $
package org.jboss.webservice;

// $Id: ServiceObjectFactory.java,v 1.3 2004/05/04 08:48:43 tdiesler Exp $

import org.jboss.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Service;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

/**
 * This ServiceObjectFactory reconstructs a javax.xml.rpc.Service
 * for a given WSDL when the webservice client does a JNDI lookup
 * <p>
 * It uses the information provided by the service-ref element in application-client.xml
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class ServiceObjectFactory implements ObjectFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceObjectFactory.class);

   /**
    * Creates an object using the location or reference information specified.
    * <p>
    *
    * @param obj The possibly null object containing location or reference
    * 		information that can be used in creating an object.
    * @param name The name of this object relative to <code>nameCtx</code>,
    *		or null if no name is specified.
    * @param nameCtx The context relative to which the <code>name</code>
    *		parameter is specified, or null if <code>name</code> is
    *		relative to the default initial context.
    * @param environment The possibly null environment that is used in
    * 		creating the object.
    * @return The object created; null if an object cannot be created.
    * @exception Exception if this object factory encountered an exception
    * while attempting to create an object, and no other object factories are
    * to be tried.
    *
    * @see NamingManager#getObjectInstance
    * @see NamingManager#getURLContext
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
           throws Exception
   {
      Reference ref = (Reference) obj;

      // get the service QName
      RefAddr namespaceRefAddr = ref.get(ServiceReferenceable.SERVICE_NAMESPACE);
      RefAddr localpartRefAddr = ref.get(ServiceReferenceable.SERVICE_LOCALPART);

      Service jaxrpcService = null;
      if (localpartRefAddr == null)
      {
         log.debug("javax.rpc.xml.Service with no wsdl");
         jaxrpcService = new JaxRpcClientService();
      }
      else
      {
         String namespace = (String) namespaceRefAddr.getContent();
         String localpart = (String) localpartRefAddr.getContent();
         QName serviceName = new QName(namespace, localpart);

         // construct the service from a URL to a WSDL document
         URL wsdlUrl = null;
         if (ref.get(ServiceReferenceable.WSDL_OVERRIDE_URL) != null)
         {
            wsdlUrl = new URL((String) ref.get(ServiceReferenceable.WSDL_OVERRIDE_URL).getContent());
         }
         else
         {
            // construct the service from an InputStream to a WSDL contained in the client deployment
            URL deployment = new URL((String) ref.get(ServiceReferenceable.DEPLOYMENT_URL).getContent());
            String wsdlFile = (String) ref.get(ServiceReferenceable.WSDL_FILE).getContent();

            // create the service
            ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
            URLClassLoader localCl = new URLClassLoader(new URL[]{deployment}, contextCL);
            wsdlUrl = localCl.getResource(wsdlFile);
         }

         log.debug("javax.rpc.xml.Service [serviceName=" + serviceName + ",url=" + wsdlUrl + "]");
         InputStream wsdlInputStream = wsdlUrl.openStream();
         if (wsdlInputStream == null)
            throw new IOException("Cannot access wsdl document at: " + wsdlUrl);

         jaxrpcService = new JaxRpcClientService(wsdlInputStream, serviceName);
      }

      // The client wants a proxy
      String serviceInterface = (String) ref.get(ServiceReferenceable.SERVICE_ENDPOINT_INTERFACE).getContent();
      if (serviceInterface.equals("javax.xml.rpc.Service") == false)
      {
         ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
         Class seiClass = contextCL.loadClass(serviceInterface);
         InvocationHandler handler = new JaxRpcClientProxy(jaxrpcService, seiClass);
         Class[] interfaces = new Class[]{seiClass, Service.class};
         Service proxyService = (Service)Proxy.newProxyInstance(contextCL, interfaces, handler);
         return proxyService;
      }

      return jaxrpcService;
   }
}
