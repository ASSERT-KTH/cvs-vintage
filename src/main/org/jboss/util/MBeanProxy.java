/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;
import java.lang.reflect.Method;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Log;
import org.jboss.proxy.Proxy;
import org.jboss.proxy.InvocationHandler;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @version $Revision: 1.5 $
 */
public class MBeanProxy
   implements InvocationHandler
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   ObjectName name;
   MBeanServer server;
   
   // Static --------------------------------------------------------
   public static Object create(Class intf, String name)
      throws MalformedObjectNameException
   {
      return Proxy.newProxyInstance(intf.getClassLoader(),
                                          new Class[] { intf },
                                          new MBeanProxy(name));
   }

   public static Object create(Class intf, ObjectName name)
   {
      return Proxy.newProxyInstance(intf.getClassLoader(),
                                          new Class[] { intf },
                                          new MBeanProxy(name));
   }
   
   // Constructors --------------------------------------------------
   MBeanProxy(String name)
      throws MalformedObjectNameException
   {
      this(new ObjectName(name));
   }
   
   MBeanProxy(ObjectName name)
   {
      this.name = name;
      server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).iterator().next();
   }
   
   // Public --------------------------------------------------------
   public Object invoke(Object proxy,
                     Method method,
                     Object[] args)
              throws Throwable
   {
      if (args == null) args = new Object[0];
      
      Class[] types = method.getParameterTypes();
      String[] sig = new String[types.length];
      for (int i = 0; i < types.length; i++)
         sig[i] = types[i].getName();
      
      try
      {
         return server.invoke(name, method.getName(), args, sig);
      } catch (MBeanException e)
      {
         throw e.getTargetException();
      } catch (ReflectionException e)
      {
         throw e.getTargetException();
      }
   }
   
   // Protected -----------------------------------------------------
}

