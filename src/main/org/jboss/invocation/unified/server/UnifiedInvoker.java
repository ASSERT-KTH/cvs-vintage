/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.unified.server;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.unified.interfaces.UnifiedInvokerProxy;
import org.jboss.invocation.unified.marshall.InvocationMarshaller;
import org.jboss.invocation.unified.marshall.InvocationUnMarshaller;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerCallbackHandler;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.marshal.MarshalFactory;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import java.rmi.MarshalledObject;

/**
 * This is a detached invoker which sits on top of jboss remoting.
 * Since this uses remoting, the transport protocol used is defined within
 * the remoting service and this, the UnifiedInvoker, is declared as the handler.
 *
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class UnifiedInvoker extends ServiceMBeanSupport implements ServerInvocationHandler, UnifiedInvokerMBean
{
   private ServerInvoker serverInvoker;

   private MBeanServer mbServer;

   //TODO: -TME (JBREM-51) This is a hack to get the invocation marshaller registered with the MarshalFactory.
   static
   {
      MarshalFactory.addMarshaller(InvocationMarshaller.DATATYPE, new InvocationMarshaller(), new InvocationUnMarshaller());
   }


   /**
    * Will get the invoker locator from the server invoker, start the server invoker, create the proxy,
    * and bind the proxy.
    *
    * @throws Exception
    */
   protected void startService() throws Exception
   {
      log.debug("Starting unified invoker service.");

      InitialContext ctx = new InitialContext();

      InvokerLocator locator = null;
      if(serverInvoker != null)
      {
         locator = serverInvoker.getLocator();
         if(!serverInvoker.isStarted())
         {
            serverInvoker.start();
         }
      }
      else
      {
         /**
          * TODO: -TME Doing configuration this way is not the best.  The only way this is going to work is
          * for the unified invoker to be decarled before the connector in the service xml (so will be constructed
          * first).  Then make the unified invoker depend on the connector.  This way the connector will be created
          * (and have the unified invoker as the handler) and will be started.  This will cause it to call on the
          * unified invoker and pass its server invoker.  Then, when the unfified invoker is started, it will have
          * the reference to the server invoker needed here (so can get the locator).
          */
         log.error("Error getting locator for the unified invoker proxy because server invoker is null.");
         log.error("This means that a Connector was never started with the unified invoker as a handler.");
         throw new RuntimeException("Error getting locator because server invoker is null.");
      }

      UnifiedInvokerProxy proxy = new UnifiedInvokerProxy(locator);

      Registry.bind(getServiceName(), proxy);

      ctx.close();

   }

   /**
    * Stops the server invoker.
    *
    * @throws Exception
    */
   public void stopService() throws Exception
   {
      if(serverInvoker != null)
      {
         serverInvoker.stop();
      }
   }

   /**
    * Gives this JMX service a name.
    *
    * @return The Name value
    */
   public String getName()
   {
      return "Unified-Invoker";
   }

   /**
    * Gets the invoker locator string for this server
    *
    * @return
    */
   public String getInvokerLocator()
   {
      if(serverInvoker != null)
      {
         return serverInvoker.getLocator().getLocatorURI();
      }
      else
      {
         return null;
      }
   }

   /**
    * Implementation of the server invoker handler interface.  Will take the invocation request
    * and invoke down the interceptor chain.
    *
    * @param invocationReq
    * @return
    * @throws Throwable
    */
   public Object invoke(InvocationRequest invocationReq) throws Throwable
   {
      Invocation invocation = (Invocation) invocationReq.getParameter();
      Thread currentThread = Thread.currentThread();
      ClassLoader oldCl = currentThread.getContextClassLoader();
      ObjectName mbean = null;
      try
      {
         mbean = (ObjectName) Registry.lookup(invocation.getObjectName());

         // The cl on the thread should be set in another interceptor
         Object obj = getServer().invoke(mbean,
                                         "invoke",
                                         new Object[]{invocation},
                                         Invocation.INVOKE_SIGNATURE);
         return new MarshalledObject(obj);
      }
      catch(Exception e)
      {
         Throwable th = JMXExceptionDecoder.decode(e);
         if(log.isTraceEnabled())
         {
            log.trace("Failed to invoke on mbean: " + mbean, th);
         }

         if(th instanceof Exception)
         {
            e = (Exception) th;
         }

         throw e;
      }
      finally
      {
         currentThread.setContextClassLoader(oldCl);
         Thread.interrupted(); // clear interruption because this thread may be pooled.
      }

   }

   /**
    * set the mbean server that the handler can reference
    *
    * @param server
    */
   public void setMBeanServer(MBeanServer server)
   {
      mbServer = server;
   }

   public MBeanServer getServer()
   {
      return mbServer;
   }

   /**
    * set the invoker that owns this handler
    *
    * @param invoker
    */
   public void setInvoker(ServerInvoker invoker)
   {
      /**
       * This is needed in case we need to make calls on the server invoker (for classloading
       * in particular).  Will just leave alone for now and come back to this when have
       * a use to call on it.
       */
      serverInvoker = invoker;
   }

   /**
    * Adds a callback handler that will listen for callbacks from
    * the server invoker handler.
    * This is a no op as don't expect the detached invokers to have callbacks
    *
    * @param callbackHandler
    */
   public void addListener(InvokerCallbackHandler callbackHandler)
   {
      //NO OP - do not expect the detached invoker to have callbacks
   }

   /**
    * Removes the callback handler that was listening for callbacks
    * from the server invoker handler.
    * This is a no op as don't expect the detached invokers to have callbacks
    *
    * @param callbackHandler
    */
   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
      //NO OP - do not expect the detached invoker to have callbacks
   }

}