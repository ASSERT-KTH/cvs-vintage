/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.jrmp.server;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.ServerException;
import java.rmi.MarshalledObject;
import java.util.Date;

import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.AttributeChangeNotification;

import org.jboss.metadata.MetaData;
import org.jboss.deployment.DeploymentException;
import org.jboss.proxy.ejb.GenericProxy;

import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.logging.Logger;
import org.jboss.system.Registry;


/**
*  The JRMPInvoker is an RMI implementation that can generate Invocations from RMI/JRMP into the JMX base
*
*  @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
*
*  @version $Revision: 1.3 $
*/

public class JRMPInvoker
extends RemoteServer
implements Invoker, JRMPInvokerMBean,  MBeanRegistration

{
   // Constants -----------------------------------------------------
   protected final static int ANONYMOUS_PORT = 0;
   
   // Attributes ----------------------------------------------------
   protected Logger log = Logger.getLogger(JRMPInvoker.class);
   
   /** The port the container will be exported on */
   protected int rmiPort = ANONYMOUS_PORT;
   /** An optional custom client socket factory */
   protected RMIClientSocketFactory clientSocketFactory;
   /** An optional custom server socket factory */
   protected RMIServerSocketFactory serverSocketFactory;
   /** The class name of the optional custom client socket factory */
   protected String clientSocketFactoryName;
   /** The class name of the optional custom server socket factory */
   protected String serverSocketFactoryName;
   /** The address to bind the rmi port on */
   protected String serverAddress;
   
   // The MBean Server
   protected MBeanServer server;
   protected ObjectName serviceName;
   
   protected int state;
   protected int id = 0;
   
   // Static --------------------------------------------------------
   
   private static TransactionPropagationContextFactory tpcFactory;
   private static TransactionPropagationContextImporter tpcImporter;
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // MBean parameters

   public String getServerHostName() 
   { 
      try {return InetAddress.getLocalHost().getHostName();}
      
      catch (Exception ignored) {return null;}
   }
   
   public void setRMIObjectPort(int rmiPort) {this.rmiPort = rmiPort;}
   public int getRMIObjectPort()  { return rmiPort;}
   
   public void setRMIClientSocketFactory(String name) {clientSocketFactoryName = name;}
   public String getRMIClientSocketFactory() { return clientSocketFactoryName;}
   
   public void setRMIServerSocketFactory(String name) {serverSocketFactoryName = name;}
   public String getRMIServerSocketFactory() { return serverSocketFactoryName;}
   
   public void setServerAddress(String address) { serverAddress = address;}
   public String getServerAddress() { return serverAddress;}
   
   public String getName() {return "JRMPInvoker";}
   
   // Service implementation -------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public JRMPInvoker()
   {
      log = Logger.getLogger(getClass());
   }
   
   // Public --------------------------------------------------------
   
   
   public MBeanServer getServer() { return server; }
   
   public int getState() { return state; }
   
   public String getStateString() { return states[state]; }
   
   public void create()
   throws Exception
   {
      log.info("creating");
      loadCustomSocketFactories();
      
      log.debug("Container Invoker RMI Port='"+(rmiPort == ANONYMOUS_PORT ? "Anonymous" : Integer.toString(rmiPort))+"'");
      log.debug("Container Invoker Client SocketFactory='"+(clientSocketFactory == null ? "Default" : clientSocketFactory.toString())+"'");
      log.debug("Container Invoker Server SocketFactory='"+(serverSocketFactory == null ? "Default" : serverSocketFactory.toString())+"'");
      log.debug("Container Invoker Server SocketAddr='"+(serverAddress == null ? "Default" : serverAddress)+"'");
       
      log.info("created");
   }
   
   public void start()
   throws Exception
   {
      if (getState() != STOPPED && getState() != FAILED)
         return;
       
      state = STARTING;
      log.info("Starting");

      Context ctx = new InitialContext();
      
      // Get the transaction propagation context factory
      // and the transaction propagation context importer
      tpcFactory = (TransactionPropagationContextFactory)ctx.lookup("java:/TransactionPropagationContextExporter");
      tpcImporter = (TransactionPropagationContextImporter)ctx.lookup("java:/TransactionPropagationContextImporter");
      
      // Set the transaction manager and transaction propagation
      // context factory of the GenericProxy class
      GenericProxy.setTransactionManager((TransactionManager)ctx.lookup("java:/TransactionManager"));
      JRMPInvokerProxy.setTPCFactory(tpcFactory);
      
      Invoker delegateInvoker = createDelegateInvoker();
      
      // Export references to the bean
      Registry.bind(serviceName, delegateInvoker);   
  
      try
      {
         
         // Export CI
         exportCI();
         
         InitialContext context = new InitialContext();
         
         // Bind the invoker in the JNDI invoker naming space
         rebind(
            // The context
            context,
            // It should look like so "invokers/<name>/jrmp" 
            "invokers/"+InetAddress.getLocalHost().getHostName()+"/jrmp", 
            // The bare invoker            
            delegateInvoker);
         
         log.debug("Bound JRMP invoker for JMX node");
      
      }
      catch (Exception e)
      {
         state = FAILED;
         log.error("Failed", e);
         throw new ServerException("Could not bind JRMP invoker", e);
      }

      state = STARTED;
      log.info("Started");
   }
   
   public void stop()
   {
      if (getState() != STARTED)
         return;
      
      state = STOPPING;
      log.info("Stopping");
      
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind("invokers/"+InetAddress.getLocalHost().getHostName()+"/jrmp");
         
         unexportCI();
      }
      catch (Throwable e)
      {
         state = FAILED;
         log.error("Failed", e);
         return;
      }
      state = STOPPED;
      log.info("Stopped");
   }
   
   public void destroy()
   {
      // Export references to the bean
      Registry.unbind(serviceName);
   
   }
   
   // MBeanRegistration implementation -------------------------------------------------------
   
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws MalformedObjectNameException
   {
      return serviceName;
   }
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      this.server = server;
      
      serviceName = name;
      
      log.info("JRMP Invoker MBean online");
      
      return serviceName;
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
   
   // ContainerRemote implementation --------------------------------
   
   /**
   *  Invoke a Remote interface method.
   */
   public Object invoke(Invocation invocation)
   throws Exception
   {     
      
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      
      try
      {
         // Deserialize the transaction if it is there  
         invocation.setTransaction(importTPC(((MarshalledInvocation) invocation).getTransactionPropagationContext()));
               
         // Extract the ObjectName, the rest is still marshalled
         ObjectName mbean = new ObjectName((String) invocation.getContainer());
         
         // The cl on the thread should be set in another interceptor
         return new MarshalledObject(server.invoke(mbean, invocation.getType(),  new Object[] {invocation}, new String[] {"java.lang.Object"})); 
      }
      
      catch (MBeanException mbe) { throw mbe.getTargetException(); }
      catch (Exception e) {e.printStackTrace(); throw e;}
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }      
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   protected Invoker createDelegateInvoker()
   {
      return new JRMPInvokerProxy(this);
   }
   
   protected void exportCI() throws Exception
   {
      UnicastRemoteObject.exportObject(this, rmiPort,
         clientSocketFactory, serverSocketFactory);
   }
   
   protected void unexportCI() throws Exception
   {
      UnicastRemoteObject.unexportObject(this, true);
   }
   
   
   protected void rebind(Context ctx, String name, Object val)
   throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate contexts exist
      
      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context)ctx.lookup(ctxName);
         } catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }
      
      ctx.rebind(n.get(0), val);
   }
   
   // Private -------------------------------------------------------
   private void loadCustomSocketFactories()
   {
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      
      try
      {
         if( clientSocketFactoryName != null )
         {
            Class csfClass = loader.loadClass(clientSocketFactoryName);
            clientSocketFactory = (RMIClientSocketFactory) csfClass.newInstance();
         }
      }
      catch(Exception e)
      {
         log.error(e);
         clientSocketFactory = null;
      }
      try
      {
         if( serverSocketFactoryName != null )
         {
            Class ssfClass = loader.loadClass(serverSocketFactoryName);
            serverSocketFactory = (RMIServerSocketFactory) ssfClass.newInstance();
            if( serverAddress != null )
            {
               // See if the server socket supports setBindAddress(String)
               try
               {
                  Class[] parameterTypes =
                  {String.class};
                  Method m = ssfClass.getMethod("setBindAddress", parameterTypes);
                  Object[] args =
                  {serverAddress};
                  m.invoke(serverSocketFactory, args);
               }
               catch(NoSuchMethodException e)
               {
                  log.error("Socket factory does not support setBindAddress(String)");
                  // Go with default address
               }
               catch(Exception e)
               {
                  log.error("Failed to setBindAddress="+serverAddress+" on socket factory");
                  // Go with default address
               }
            }
         }
         // If a bind address was specified create a DefaultSocketFactory
         else if( serverAddress != null )
         {
            DefaultSocketFactory defaultFactory = new DefaultSocketFactory();
            serverSocketFactory = defaultFactory;
            try
            {
               defaultFactory.setBindAddress(serverAddress);
            }
            catch(UnknownHostException e)
            {
               log.error("Failed to setBindAddress="+serverAddress+" on socket factory, "+e.getMessage());
            }
         }
      }
      catch(Exception e)
      {
         log.error(e);
         serverSocketFactory = null;
      }
   }
   
   /**
   *  Import a transaction propagation context into the local VM, and
   *  return the corresponding <code>Transaction</code>.
   */
   private Transaction importTPC(Object tpc)
   {
      if (tpc != null)
         return tpcImporter.importTransactionPropagationContext(tpc);
      return null;
   }
   // Inner classes -------------------------------------------------
}

