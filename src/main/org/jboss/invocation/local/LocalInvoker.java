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
*  The Invoker is a local gate in the JMX system.
*  
*  @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
*
*  @version $Revision: 1.3 $
*
*/

public class LocalInvoker
extends ServiceMBeanSupport
implements Invoker, LocalInvokerMBean
{
   // Constants -----------------------------------------------------
   protected static Logger log = Logger.getLogger(LocalInvoker.class);
   
   // Attributes ----------------------------------------------------
   
   // The MBean Server
   protected MBeanServer server;
   
   // The ObjectName for the local invoker
   protected ObjectName name;
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // Service implementation -------------------------------
   
   public String getName() { return "LocalInvoker";}
   
   public void create()
   throws Exception
   {
      //note on design: We need to call it ourselves as opposed to letting the client JRMPDelegate look it 
      // up through the use of Registry, the reason being including the classes in the client. 
      // If we move to a JNDI format (with local calls) for the registry we could remove the call below
      JRMPInvokerProxy.setLocal(this);
      
      Registry.bind(name, this);
   }
   
   
   public void start()
   throws Exception
   {
      
      GenericProxy.setTransactionManager((TransactionManager)new InitialContext().lookup("java:/TransactionManager"));
      
      log.debug("Local invoker for JMX node started");
   }
   
   public void stop()
   {
   }
   
   public void destroy()
   {
      Registry.unbind(name);
   }
   
   // Invoker implementation --------------------------------
   
   public String getServerHostName() 
   { 
      try {return InetAddress.getLocalHost().getHostName();}
         
      catch (Exception ignored) {return null;}
   }
   
   
   /**
   *  Invoke a  method.
   */
   public Object invoke(Invocation invocation)
   throws Exception
   {     
      
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      
      try
      {         
         
         
         ObjectName mbean = (ObjectName) Registry.lookup((Integer) invocation.getContainer());
        
         return server.invoke(mbean, null,  new Object[] {invocation}, new String[] {"java.lang.Object"}); 
      }
      
      catch (MBeanException mbe) { throw mbe.getTargetException(); }
      
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }      
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // MBeanRegistration implementation -------------------------------------------------------
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      this.server = server;
      
      this.name = name;
      
      log.info("Local Invoker MBean online");
      //return name == null ? new ObjectName(OBJECT_NAME) : name;
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
      {
         log.info( "Registration of JRMP Invoker MBean failed" );
      }
   }
   
   public void preDeregister()
   throws Exception
   {
   }
   
   public void postDeregister()
   {
   }
   // Inner classes -------------------------------------------------
}

