/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation.local;

import java.net.InetAddress;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.transaction.TransactionManager;
import javax.naming.InitialContext;

import org.jboss.proxy.ejb.GenericProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

/**
 * The Invoker is a local gate in the JMX system.
 *  
 * @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @version $Revision: 1.5 $
 */
public class LocalInvoker
   extends ServiceMBeanSupport
   implements Invoker, LocalInvokerMBean
{
   protected void createService() throws Exception
   {
      // note on design: We need to call it ourselves as opposed to 
      // letting the client JRMPDelegate look it 
      // up through the use of Registry, the reason being including
      // the classes in the client. 
      // If we move to a JNDI format (with local calls) for the
      // registry we could remove the call below
      JRMPInvokerProxy.setLocal(this);
      
      Registry.bind(serviceName, this);
   }
   
   protected void startService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try {
         TransactionManager tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
         GenericProxy.setTransactionManager(tm);
      }
      finally {
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
      try {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (Exception ignored) {
         return null;
      }
   }
   
   /**
    * Invoke a method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {     
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      
      try
      {         
         ObjectName mbean = (ObjectName)
            Registry.lookup((Integer)invocation.getContainer());
         
         return server.invoke(mbean,
                              "",
                              new Object[] { invocation },
                              Invocation.INVOKE_SIGNATURE);
      }
      catch (MBeanException e)
      {
         throw e.getTargetException();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }      
   }
}

