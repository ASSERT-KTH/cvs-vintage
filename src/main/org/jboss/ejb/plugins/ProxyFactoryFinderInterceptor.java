/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.ServerError;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.log4j.NDC;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.Interceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.metadata.BeanMetaData;
import org.jboss.system.Registry;
import org.jboss.naming.ENCThreadLocalKey;

/** 
 * This interceptor injects the ProxyFactory into the ThreadLocal container 
 * variable
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @version $Revision: 1.10 $
 */
public class ProxyFactoryFinderInterceptor extends AbstractInterceptor
{
   protected void setProxyFactory(
         String invokerBinding, 
         Invocation invocation) throws Exception
   {
      // if(BeanMetaData.LOCAL_INVOKER_PROXY_BINDING.equals(invokerBinding)) return;
      if (invokerBinding == null)
      {
         log.trace("invokerBinding is null in ProxyFactoryFinder");
         return;
      }


      Object proxyFactory = getContainer().lookupProxyFactory(invokerBinding);
      if (proxyFactory == null)
      {
         String methodName;
         if(invocation.getMethod() != null) {
            methodName = invocation.getMethod().getName();
         }
         else 
         {
            methodName ="<no method>";
         }

         log.error("***************** proxyFactory is null ********");
         log.error("Method name: " + methodName);
         log.error("jmx name: " + getContainer().getJmxName().toString());
         log.error("invokerBinding: " + invokerBinding);
         log.error("Stack trace", new Throwable());
         log.error("*************************");
         throw new EJBException("Couldn't find proxy factory");
      }
      getContainer().setProxyFactory(proxyFactory);
   }

   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      String invokerBinding = 
            (String)invocation.getValue(InvocationKey.INVOKER_PROXY_BINDING);
      setProxyFactory(invokerBinding, invocation);

      String oldInvokerBinding = ENCThreadLocalKey.getKey();

      // Only override current ENC binding if we're not local or there has 
      // not been a previous call
      //      if ((!BeanMetaData.LOCAL_INVOKER_PROXY_BINDING.equals(invokerBinding)) || oldInvokerBinding == null)
      if (invokerBinding != null || oldInvokerBinding == null)
      {
         ENCThreadLocalKey.setKey(invokerBinding);
      }

      try
      {
         return getNext().invoke(invocation);
      }
      finally
      {
         ENCThreadLocalKey.setKey(oldInvokerBinding);
      }
   }
}
