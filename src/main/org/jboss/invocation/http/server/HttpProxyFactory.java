/*
* JBoss, the OpenSource J2EE WebOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.server;

import java.util.ArrayList;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.http.interfaces.HttpInvokerProxy;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Strings;

/** Create an interface proxy that uses HTTP to communicate with the server
 * side object that exposes the corresponding JMX invoke operation. Any request
 * to this servlet receives a serialized object stream containing a
 * MarshalledValue with the Naming proxy as its content.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.2 $
 */
public class HttpProxyFactory extends ServiceMBeanSupport
   implements HttpProxyFactoryMBean
{
   /** The server side mbean that exposes the invoke operation for the
    exported interface */
   private ObjectName jmxInvokerName;
   /** The Proxy object which uses the HttpInvokerProxy as its handler */
   private Object theProxy;
   /** The http URL to the InvokerServlet */
   private String invokerURL;
   /** The JNDI name under which the HttpInvokerProxy will be bound */
   private String jndiName;
   /** The interface that the HttpInvokerProxy implements */
   private Class exportedInterface;

   public ObjectName getInvokerName()
   {
      return jmxInvokerName;
   }
   public void setInvokerName(ObjectName jmxInvokerName)
   {
      this.jmxInvokerName = jmxInvokerName;
   }

   public String getJndiName()
   {
      return jndiName;
   }
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public String getInvokerURL()
   {
      return invokerURL;
   }
   public void setInvokerURL(String invokerURL)
   {
      // Replace any system properties in the URL
      String tmp = Strings.replaceProperties(invokerURL);
      this.invokerURL = tmp;
      log.debug("Set invokerURL to "+this.invokerURL);
   }

   public Class getExportedInterface()
   {
      return exportedInterface;
   }
   public void setExportedInterface(Class exportedInterface)
   {
      this.exportedInterface = exportedInterface;
   }

   /** Initializes the servlet.
    */
   protected void startService() throws Exception
   {
      /** Create an HttpInvokerProxy that posts invocations to the
       externalURL. This proxy will be associated with a naming JMX invoker
       given by the jmxInvokerName.
       */
      if( invokerURL == null )
      {
         invokerURL = "http://localhost:8080/invoker/JMXInvokerServlet";
         log.warn("No invokerURL attribute specified, defaulting to localhost:8080");
      }
      HttpInvokerProxy delegateInvoker = new HttpInvokerProxy(invokerURL);
      Integer nameHash = new Integer(jmxInvokerName.hashCode());
      Registry.bind(jmxInvokerName, delegateInvoker);
      Registry.bind(nameHash, jmxInvokerName);

      Object cacheID = null;
      Class[] ifaces = {exportedInterface};
      ArrayList interceptorClasses = new ArrayList();
      interceptorClasses.add(InvokerInterceptor.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      theProxy = proxyFactory.createProxy(cacheID, jmxInvokerName,
         jndiName, null, interceptorClasses, loader, ifaces);
      log.debug("Created HttpInvokerProxy for invoker="+jmxInvokerName);

      InitialContext iniCtx = new InitialContext();
      Util.bind(iniCtx, jndiName, theProxy);
   }

   protected void stopService() throws Exception
   {
      Integer nameHash = new Integer(jmxInvokerName.hashCode());
      Registry.unbind(jmxInvokerName);
      Registry.unbind(nameHash);
      InitialContext iniCtx = new InitialContext();
      Util.unbind(iniCtx, jndiName);
   }

}
