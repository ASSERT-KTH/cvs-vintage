/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.inflow;

import java.lang.reflect.Method;

import javax.resource.ResourceException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import org.jboss.proxy.Interceptor;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * Implements the application server message endpoint requirements.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.5 $
 */
public class MessageEndpointInterceptor extends Interceptor
{
   // Constants -----------------------------------------------------
   
   /** The log */
   private static final Logger log = Logger.getLogger(MessageEndpointInterceptor.class);
   
   /** The key for the factory */
   public static final String MESSAGE_ENDPOINT_FACTORY = "MessageEndpoint.Factory";

   /** The key for the xa resource */
   public static final String MESSAGE_ENDPOINT_XARESOURCE = "MessageEndpoint.XAResource";
   
   // Attributes ----------------------------------------------------
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled(); 
   
   /** Cached version of our proxy string */
   private String cachedProxyString = null;
   
   /** Whether this proxy has been released */
   protected SynchronizedBoolean released = new SynchronizedBoolean(false);
   
   /** Whether we have delivered a message */
   protected boolean delivered = false;
   
   /** The in use thread */
   protected Thread inUseThread = null;
   
   /** The old classloader of the thread */
   protected ClassLoader oldClassLoader = null;
   
   /** Any transaction we started */
   protected Transaction transaction = null;
   
   /** Any suspended transaction */
   protected Transaction suspended = null;

   /** The message endpoint factory */
   private JBossMessageEndpointFactory endpointFactory;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public MessageEndpointInterceptor()
   {
   }
   
   // Public --------------------------------------------------------
   
   // Interceptor implementation ------------------------------------

   public Object invoke(Invocation mi) throws Throwable
   {
      // Are we still useable?
      if (released.get())
         throw new IllegalStateException("This message endpoint + " + getProxyString(mi) + " has been released");

      // Concurrent invocation?
      Thread currentThread = Thread.currentThread();
      if (inUseThread != null && inUseThread.equals(currentThread) == false)
         throw new IllegalStateException("This message endpoint + " + getProxyString(mi) + " is already in use by another thread " + inUseThread);
      inUseThread = currentThread;
      
      String method = mi.getMethod().getName();
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " in use by " + method + " " + inUseThread);
      
      // Which operation?
      if (method.equals("release"))
      {
         release(mi);
         return null;
      }
      else if (method.equals("beforeDelivery"))
      {
         before(mi);
         return null;
      }
      else if (method.equals("afterDelivery"))
      {
         after(mi);
         return null;
      }
      else
         return delivery(mi);
   }
   
   // Package Protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * Release this message endpoint.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void release(Invocation mi) throws Throwable
   {
      // We are now released
      released.set(true);

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " released");
      
      // Tidyup any outstanding delivery
      if (oldClassLoader != null)
      {
         try
         {
            finish("release", mi, false);
         }
         catch (Throwable t)
         {
            log.warn("Error in release ", t);
         }
      }
   }
   
   /**
    * Before delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void before(Invocation mi) throws Throwable
   {
      // Called out of sequence
      if (oldClassLoader != null)
         throw new IllegalStateException("Missing afterDelivery from the previous beforeDelivery for message endpoint " + getProxyString(mi));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " released");

      // Set the classloader
      MessageDrivenContainer container = getContainer(mi);
      oldClassLoader = GetTCLAction.getContextClassLoader(inUseThread);
      SetTCLAction.setContextClassLoader(inUseThread, container.getClassLoader());
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " set context classloader to " + container.getClassLoader());

      // start any transaction
      try
      {
         startTransaction("beforeDelivery", mi, container);
      }
      catch (Throwable t)
      {
         resetContextClassLoader(mi);
         throw new ResourceException(t);
      }
   }
   
   /**
    * After delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void after(Invocation mi) throws Throwable
   {
      // Called out of sequence
      if (oldClassLoader == null)
         throw new IllegalStateException("afterDelivery without a previous beforeDelivery for message endpoint " + getProxyString(mi));

      // Finish this delivery committing if we can
      try
      {
         finish("afterDelivery", mi, true);
      }
      catch (Throwable t)
      {
         throw new ResourceException(t);
      }
   }
   
   /**
    * Delivery.
    * 
    * @param mi the invocation
    * @return the result of the delivery
    * @throws Throwable for any error
    */
   protected Object delivery(Invocation mi) throws Throwable
   {
      // Have we already delivered a message?
      if (delivered)
         throw new IllegalStateException("Multiple message delivery between before and after delivery is not allowed for message endpoint " + getProxyString(mi));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " delivering");
      
      // Mark delivery if beforeDelivery was invoked
      if (oldClassLoader != null)
         delivered = true;

     
      MessageDrivenContainer container = getContainer(mi);
      boolean commit = true;
      try
      {
         // Check for starting a transaction
         if (oldClassLoader == null)
            startTransaction("delivery", mi, container);
         return getNext().invoke(mi);
      }
      catch (Throwable t)
      {
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(mi) + " delivery error", t);
         if (t instanceof Error || t instanceof RuntimeException)
         {
            if (transaction != null)
               transaction.setRollbackOnly();
            commit = false;
         }
         throw t;
      }
      finally
      {
         // No before/after delivery, end any transaction and release the lock
         if (oldClassLoader == null)
         {
            try
            {
               // Finish any transaction we started
               endTransaction(mi, commit);
            }
            finally
            {
               releaseThreadLock(mi);
            }
         }
      }
   }
   
   /**
    * Finish the current delivery
    * 
    * @param context the lifecycle method
    * @param mi the invocation
    * @param commit whether to commit
    * @throws Throwable for any error
    */
   protected void finish(String context, Invocation mi, boolean commit) throws Throwable
   {
      try
      {
         endTransaction(mi, commit);
      }
      finally
      {
         // Reset delivered flag
         delivered = false;
         // Change back to the original context classloader
         resetContextClassLoader(mi);
         // We no longer hold the lock
         releaseThreadLock(mi);
      }
   }

   /**
    * Start a transaction
    *  
    * @param context the lifecycle method
    * @param mi the invocation
    * @param container the container
    * @throws Throwable for any error
    */
   protected void startTransaction(String context, Invocation mi, MessageDrivenContainer container) throws Throwable
   {
      // Get any passed resource
      XAResource resource = (XAResource) mi.getInvocationContext().getValue(MESSAGE_ENDPOINT_XARESOURCE);

      Method method = null;

      // Normal delivery      
      if ("delivery".equals(context))
         method = mi.getMethod();
      // Before delivery
      else
         method = (Method) mi.getArguments()[0];

      // Is the delivery transacted?
      boolean isTransacted = getMessageEndpointFactory(mi).isDeliveryTransacted(method);

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " " + context + " method=" + method + " xaResource=" + resource + " transacted=" + isTransacted);

      // Get the transaction status
      TransactionManager tm = container.getTransactionManager();
      suspended = tm.suspend();

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " " + context + " currentTx=" + suspended);

      // Delivery is transacted
      if (isTransacted)
      {
         // No transaction means we start a new transaction and enlist the resource
         if (suspended == null)
         {
            tm.begin();
            transaction = tm.getTransaction();
            if (trace)
               log.trace("MessageEndpoint " + getProxyString(mi) + " started transaction=" + transaction);
      
            // Enlist the XAResource in the transaction
            if (resource != null)
            {
               transaction.enlistResource(resource);
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " enlisted=" + resource);
            }
         }
         else
         {
            // If there is already a transaction we ignore the XAResource (by spec 12.5.9)
            try
            {
               tm.resume(suspended);
            }
            finally
            {
               suspended = null;
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " transaction=" + suspended + " already active, IGNORED=" + resource);
            }
         }
      }
   }
   
   /**
    * End the transaction
    * 
    * @param mi the invocation
    * @param commit whether to try to commit
    * @throws Throwable for any error
    */
   protected void endTransaction(Invocation mi, boolean commit) throws Throwable
   {
      TransactionManager tm = null;
      Transaction currentTx = null;
      try
      {
         // If we started the transaction, commit it
         if (transaction != null)
         {
            tm = getContainer(mi).getTransactionManager();
            currentTx = tm.getTransaction();
            
            // Suspend any bad transaction - there is bug somewhere, but we will try to tidy things up
            if (currentTx != null && currentTx.equals(transaction) == false)
            {
               log.warn("Current transaction " + currentTx + " is not the expected transaction.");
               tm.suspend();
               tm.resume(transaction);
            }
            else
            {
               // We have the correct transaction
               currentTx = null;
            }
            
            // Commit or rollback depending on the status
            if (commit == false || transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " rollback");
               tm.rollback();
            }
            else
            {
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " commit");
               tm.commit();
            }
         }

         // If we suspended the incoming transaction, resume it
         if (suspended != null)
         {
            try
            {
               tm = getContainer(mi).getTransactionManager();
               tm.resume(suspended);
            }
            finally
            {
               suspended = null;
            }
         }
      }
      finally
      {
         // Resume any suspended transaction
         if (currentTx != null)
         {
            try
            {
               tm.resume(currentTx);
            }
            catch (Throwable t)
            {
               log.warn("MessageEndpoint " + getProxyString(mi) + " failed to resume old transaction " + currentTx);
               
            }
         }
      }
   }
   
   /**
    * Reset the context classloader
    * 
    * @param mi the invocation
    */
   protected void resetContextClassLoader(Invocation mi)
   {
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " reset classloader " + oldClassLoader);
      SetTCLAction.setContextClassLoader(inUseThread, oldClassLoader);
      oldClassLoader = null;
   }

   /**
    * Release the thread lock
    * 
    * @param mi the invocation
    */
   protected void releaseThreadLock(Invocation mi)
   {
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " no longer in use by " + inUseThread);
      inUseThread = null;
   }
   
   /**
    * Get our proxy's string value.
    * 
    * @param mi the invocation
    * @return the string
    */
   protected String getProxyString(Invocation mi)
   {
      if (cachedProxyString == null)
         cachedProxyString = mi.getInvocationContext().getCacheId().toString();
      return cachedProxyString;
   }

   /**
    * Get the message endpoint factory
    *
    * @return the message endpoint factory
    */
   protected JBossMessageEndpointFactory getMessageEndpointFactory(Invocation mi)
   {
      if (endpointFactory == null)
         endpointFactory = (JBossMessageEndpointFactory) mi.getInvocationContext().getValue(MESSAGE_ENDPOINT_FACTORY);
      return endpointFactory;
   }
   
   /**
    * Get the container
    *
    * @return the container
    */
   protected MessageDrivenContainer getContainer(Invocation mi)
   {
      return getMessageEndpointFactory(mi).getContainer();
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
