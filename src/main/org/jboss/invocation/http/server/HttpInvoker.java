/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.http.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;

import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.MalformedObjectNameException;

import org.jboss.invocation.http.interfaces.HttpInvokerProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Strings;

/**
 * The HttpInvoker ... into the JMX base.
 *
 * @author <a href="mailto:scott.stark@jboss.org>Scott Stark</a>
 * @version $Revision: 1.4 $
 */
public class HttpInvoker extends ServiceMBeanSupport
   implements HttpInvokerMBean
{
   private String invokerURL;

   // Public --------------------------------------------------------

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

   protected void startService()
      throws Exception
   {
      InitialContext context = new InitialContext();
      Invoker delegateInvoker = new HttpInvokerProxy(invokerURL);

      // Export the Invoker interface
      ObjectName name = super.getServiceName();
      Registry.bind(name, delegateInvoker);

      // Bind the invoker in the JNDI invoker naming space
      Util.rebind(
         // The context
         context,
         // It should look like so "invokers/<name>/jrmp"
         "invokers/"+InetAddress.getLocalHost().getHostName()+"/http",
         // The bare invoker
         delegateInvoker);

      log.debug("Bound Http invoker for JMX node");         
   }

   protected void stopService()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind("invokers/"+InetAddress.getLocalHost().getHostName()+"/http");
      }
      catch (NamingException ignore)
      {
      }
      catch (Throwable e)
      {
         log.error("Failed", e);
         return;
      }
   }

   protected void destroyService()
   {
      // Export references to the bean
      Registry.unbind(serviceName);
   }
  
   /**
    * Invoke a Remote interface method.
    */
   public InvocationResponse invoke(Invocation invocation)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         // Deserialize the transaction if it is there
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         Object tpc = mi.getTransactionPropagationContext();
         Transaction tx = importTPC(tpc);
         invocation.setTransaction(tx);

         Integer nameHash = (Integer) invocation.getObjectName();
         ObjectName mbean = (ObjectName) Registry.lookup(nameHash);

         // The cl on the thread should be set in another interceptor
         Object[] args = {invocation};
         String[] sig = {"org.jboss.invocation.Invocation"};
         Object obj = super.getServer().invoke(mbean, 
            "invoke", args, sig);

         // Return the raw object and let the http layer marshall it
         return (InvocationResponse)obj;
      }
      catch (Exception e)
      {
         if (e instanceof MBeanException)
            e = ((MBeanException)e).getTargetException();
         
         if (e instanceof RuntimeMBeanException)
            e = ((RuntimeMBeanException)e).getTargetException();
         
         if (e instanceof RuntimeOperationsException)
            e = ((RuntimeOperationsException)e).getTargetException();
         
         // Only log errors if trace is enabled
         if( log.isTraceEnabled() )
            log.trace("operation failed", e);
         throw e;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /** Not implemented, and should not be
    */
   protected Transaction importTPC(Object tpc)
   {
      return null;
   }
}
