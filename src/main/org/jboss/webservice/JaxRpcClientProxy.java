/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: JaxRpcClientProxy.java,v 1.1 2004/05/04 08:48:43 tdiesler Exp $
package org.jboss.webservice;

// $Id: JaxRpcClientProxy.java,v 1.1 2004/05/04 08:48:43 tdiesler Exp $

import org.jboss.logging.Logger;

import javax.xml.rpc.Service;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public class JaxRpcClientProxy implements InvocationHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(JaxRpcClientProxy.class);

   private Service jaxrpcService;
   private Class seiClass;

   public JaxRpcClientProxy(Service jaxrpcService, Class seiClass)
   {
      this.jaxrpcService = jaxrpcService;
      this.seiClass = seiClass;

      if (Service.class.isAssignableFrom(seiClass) == false)
         throw new IllegalArgumentException ("Is not a javax.xml.rpc.Service: " + seiClass.getName());
   }

   /**
    * Processes a method invocation on a proxy instance and returns
    * the result.  
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // client invokes a method on the Service interface
      try
      {
         Method serviceMethod = javax.xml.rpc.Service.class.getMethod(method.getName(), method.getParameterTypes());
         log.debug("Invoke on jaxrpcService: " + method);
         return serviceMethod.invoke(jaxrpcService, args);
      }
      catch (NoSuchMethodException ignore)
      {
      }

      // client invokes a method on the service endpoint interface
      Object port = jaxrpcService.getPort(seiClass);
      log.debug("Invoke on port: " + method);
      Method portMethod = port.getClass().getMethod(method.getName(), method.getParameterTypes());
      return portMethod.invoke(port, args);
   }
}
