/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.naming.interceptors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.util.MBeanServerLocator;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;

/** An interceptor that replaces any NamingContext values returned with the
 * proxy found as the Proxy attribute of the mbean given by proxyName.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.2 $
 */
public class ProxyFactoryInterceptor
   extends AbstractInterceptor
{
   private static Logger log = Logger.getLogger(ProxyFactoryInterceptor.class);
   private String proxyName;
   private Naming proxy;

   public void setProxyName(String proxyName)
   {
      this.proxyName = proxyName;
   }

   // Interceptor overrides -----------------------------------------
   public Object invoke(Invocation invocation) throws Throwable
   {
      Object value = invocation.nextInterceptor().invoke(invocation);
      if( value instanceof NamingContext )
      {
         initNamingProxy();
         NamingContext ctx = (NamingContext) value;
         ctx.setNaming(proxy);
      }
      return value;
   }

   /** This is not synchronized as we don't care if the proxy object might
    * get lookedup more than once on initialization
    * @throws Exception
    */ 
   private void initNamingProxy()
      throws Exception
   {
      if( proxy != null )
         return;

      ObjectName proxyFactory = new ObjectName(proxyName);
      MBeanServer server = MBeanServerLocator.locateJBoss();
      proxy = (Naming) server.getAttribute(proxyFactory, "Proxy");
   }
}
