
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.invocation.remoting;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.Xid;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerXAResource;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerCallbackHandler;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.ident.Identity;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.jmx.JMXExceptionDecoder;
import org.jboss.util.naming.Util;



/**
 * EJBSubsystemInvocationHandler.java
 *
 *
 * Created: Wed Apr 23 19:19:31 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class EJBSubsystemInvocationHandler
   extends ServiceMBeanSupport
   implements ServerInvocationHandler, EJBSubsystemInvocationHandlerMBean
{

   final static int DEFAULT_TIMEOUT = 6;//seconds. Wrong class for this.

   /**
    * The field <code>connectorName</code> links to the remoting
    * Connector we register with as the EJB subsystem.
    *
    */
   private ObjectName connectorName;

   /**
    * The field <code>workManagerName</code> links to the WorkManager
    * we use for transaction and security import.  It is used only
    * synchronously (doWork), not using any threads.
    * @todo Refactor remoting framework so this thread pool is used in
    * the transport endoint.
    *
    */
   private ObjectName workManagerName;

   /**
    * The field <code>workManager</code> is the actual WorkManager object.
    *
    */
   private WorkManager workManager;

   /**
    * The field <code>serverInvoker</code> is the server side
    * transport enpoint obtained from the Connector.  We don't use it
    * at present, since we don't send back asynchronous callbacks.
    *
    */
   private ServerInvoker serverInvoker;

   public EJBSubsystemInvocationHandler()
   {

   } // EJBSubsystemInvocationHandler constructor

   public void startService() throws Exception
   {
      getServer().invoke(connectorName,
                         "addInvocationHandler",
                         new Object[] {"EJB", this},
                         new String[] {String.class.getName(),
                                       ServerInvocationHandler.class.getName()});
      workManager = (WorkManager)getManagedResource(workManagerName);
   }

   protected void destroyService() throws Exception
   {
      workManager = null;
   }

   /**
    * Get the ConnectorName value.
    * @return the ConnectorName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getConnectorName()
   {
      return connectorName;
   }

   /**
    * Set the ConnectorName value.
    * @param connectorName The new ConnectorName value.
    *
    * @jmx.managed-attribute
    */
   public void setConnectorName(ObjectName connectorName)
   {
      this.connectorName = connectorName;
   }



   /**
    * Get the WorkManagerName value.
    * @return the WorkManagerName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getWorkManagerName()
   {
      return workManagerName;
   }

   /**
    * Set the WorkManagerName value.
    * @param workManagerName The new WorkManagerName value.
    *
    * @jmx.managed-attribute
    */
   public void setWorkManagerName(ObjectName workManagerName)
   {
      this.workManagerName = workManagerName;
   }



   // Implementation of org.jboss.remoting.ServerInvocationHandler

   /**
    * The <code>setMBeanServer</code> method
    *
    * @param MBeanServer a <code>MBeanServer</code> value
    */
   public void setMBeanServer(MBeanServer MBeanServer)
   {

   }

   public void addListener(InvokerCallbackHandler handler)
   {
   }

   public void removeListener(InvokerCallbackHandler handler)
   {
   }



   /**
    * The <code>setInvoker</code> method is called by the Connector
    * when we register with it.  We don't currently use the
    * serverInvoker.
    *
    * @param serverInvoker a <code>ServerInvoker</code> value
    */
   public void setInvoker(ServerInvoker serverInvoker)
   {
      this.serverInvoker = serverInvoker;
   }


   /**
    * The <code>invoke</code> method translates the remoting
    * invocation into the appropraite mbean invocation.  It uses the
    * WorkManager to import the transaction context.  This should be
    * refactored so the transport endpoint uses the thread pool.
    *
    * @param invocationRequest an <code>InvocationRequest</code> value
    * @return an <code>Object</code> value
    * @exception Throwable if an error occurs
    */
   public Object invoke(InvocationRequest invocationRequest) throws Throwable
   {
      String methodName = invocationRequest.getMethodName();
      Object args [] = invocationRequest.getArgs();
      String signature [] = invocationRequest.getSignature();
      String sessionId = invocationRequest.getSessionId();

      if (!methodName.equals("invoke"))
      {
         throw new IllegalArgumentException("Unexpected method name: " + methodName);
      } // end of if ()
      Invocation invocation = (Invocation)args[0];
      ExecutionContext ec = new ExecutionContext();
      ec.setXid((Xid)invocation.getValue(InvocationKey.XID));
      Integer transactionTimeout = (Integer)invocation.getValue(InvocationKey.TX_TIMEOUT);
      if (transactionTimeout != null)
      {
         ec.setTransactionTimeout(transactionTimeout.intValue());
      }
      else
      {
         ec.setTransactionTimeout(DEFAULT_TIMEOUT);
      }

      if (log.isTraceEnabled()) {
         log.info("About to schedule work with execution context: " + ec);
      }
      RequestRunner runner = new RequestRunner(invocation);
      workManager.doWork(runner,
                         ec.getTransactionTimeout() * 1000, //convert to milliseconds
                         ec,
                         null);

      if (runner.problem != null)
      {
         throw runner.problem;
      } // end of if ()

      return runner.result;
   }

   /**
    * The class <code>RequestRunner</code> is a Work object that
    * actually invokes on the appropriate mbean and provides the
    * results for the caller.  This should be refactored so the
    * transport endpoint is using the thread pool.
    *
    */
   public class RequestRunner implements Work
   {
      Invocation invocation;
      Object result;
      Throwable problem;

      RequestRunner(Invocation invocation)
      {
         this.invocation = invocation;
      }

      /**
       * The <code>run</code> method actually invokes the target mbean
       * and stores the results for use by the caller.
       *
       */
      public void run()
      {
         log.trace("in requestRunner, about to requestEvent from listener");
         Thread currentThread = Thread.currentThread();
         ClassLoader oldCl = currentThread.getContextClassLoader();
         try
         {
            ObjectName mbean = (ObjectName) Registry.lookup(invocation.getObjectName());

            result =  getServer().invoke(mbean,
                                         "invoke",
                                         new Object[] { invocation },
                                         Invocation.INVOKE_SIGNATURE);

         }
         catch (Exception e)
         {
            problem = JMXExceptionDecoder.decode(e);
         }
         finally
         {
            currentThread.setContextClassLoader(oldCl);
         }

         log.trace("in requestRunner, done with requestEvent from listener");
      }

      public void release()
      {
         //maybe should interrupt??
      }
   }


} // EJBSubsystemInvocationHandler
