/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation.local;

import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.proxy.TransactionInterceptor;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;

/**
 * The Invoker is a local gate in the JMX system.
 *
 * @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @version $Revision: 1.13 $
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class LocalInvoker
   extends ServiceMBeanSupport
   implements Invoker, LocalInvokerMBean
{
   protected void createService() throws Exception
   {
      // note on design: We need to call it ourselves as opposed to 
      // letting the client InvokerInterceptor look it 
      // up through the use of Registry, the reason being including
      // the classes in the client. 
      // If we move to a JNDI format (with local calls) for the
      // registry we could remove the call below
      InvokerInterceptor.setLocal(this);

      Registry.bind(serviceName, this);
   }

   protected void startService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {

         /**
          * FIXME marcf: what is this doing here?
          */
         TransactionManager tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
         TransactionInterceptor.setTransactionManager(tm);
      }
      finally
      {
         ctx.close();
      }

      log.debug("Local invoker for JMX node started");
   }

   protected void destroyService()
   {
      Registry.unbind(serviceName);
   }
   
   // Invoker implementation --------------------------------
   
   public String getServerHostName()
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (Exception ignored)
      {
         return null;
      }
   }

   /**
    * Invoke a method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      ClassLoader oldCl = GetTCLAction.getContextClassLoader();

      ObjectName mbean = (ObjectName) Registry.lookup((Integer) invocation.getObjectName());
      try
      {

         return server.invoke(mbean,
            "invoke",
            new Object[]{invocation},
            Invocation.INVOKE_SIGNATURE);
      }
      catch (Exception e)
      {
         e = (Exception) JMXExceptionDecoder.decode(e);
         if (log.isTraceEnabled())
            log.trace("Failed to invoke on mbean: " + mbean, e);
         throw e;
      }
      finally
      {
         SetTCLAction.setContextClassLoader(oldCl);
      }
   }

   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();

      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }

      static ClassLoader getContextClassLoader()
      {
         ClassLoader loader = (ClassLoader) AccessController.doPrivileged(ACTION);
         return loader;
      }
   }

   private static class SetTCLAction implements PrivilegedAction
   {
      ClassLoader loader;

      SetTCLAction(ClassLoader loader)
      {
         this.loader = loader;
      }

      public Object run()
      {
         Thread.currentThread().setContextClassLoader(loader);
         loader = null;
         return null;
      }

      static void setContextClassLoader(ClassLoader loader)
      {
         PrivilegedAction action = new SetTCLAction(loader);
         AccessController.doPrivileged(action);
      }
   }
}

