/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.ejb;

import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Properties;
import javax.naming.InitialContext;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.logging.Logger;
import org.jboss.proxy.Interceptor;

/** An interceptor that will retry failed invocations by restoring the 
 * InvocationContext invoker. This is triggered by a ServiceUnavailableException
 * which causes the interceptor to fall into a while loop that retries the
 * lookup of the transport invoker using the jndi name obtained from the
 * invocation context under the key InvocationKey.JNDI_NAME, with the additional
 * extension of "-RemoteInvoker" if the invocation type is InvocationType.REMOTE
 * and "-HomeInvoker" if the invocation type is InvocationType.HOME.
 * 
 * The JNDI environment used for the lookup must be set via the setRetryEnv.
 * Typically this is an HA-JNDI configuration with one or more bootstrap
 * urls.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class RetryInterceptor extends Interceptor
{
   /** Serial Version Identifier. @since 1.0 */
   private static final long serialVersionUID = 1;
   /** The current externalized data version */
   private static final int EXTERNAL_VERSION = 1;
   private static Logger log = Logger.getLogger(RetryInterceptor.class);
   /** The HA-JNDI environment used to restore the invoker proxy */
   private static Properties retryEnv;

   /** A flag that can be set to abort the retry loop */
   private transient boolean retry;
   /** The logging trace flag */
   private transient boolean trace;

   /**
    * Set the HA-JNDI InitialContext env used to lookup the invoker proxy
    * @param env the InitialContext env used to lookup the invoker proxy
    */ 
   public static void setRetryEnv(Properties env)
   {
      retryEnv = env;
   }

   /**
    * No-argument constructor for externalization.
    */
   public RetryInterceptor()
   {}
   // Public --------------------------------------------------------

   public void setRetry(boolean flag)
   {
      this.retry = flag;
   }
   public boolean getRetry()
   {
      return this.retry;
   }

   /**
    * InvocationHandler implementation.
    *
    * @throws Throwable    Any exception or error thrown while processing.
    */
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      Object result = null;
      InvocationContext ctx = invocation.getInvocationContext();
      retry = true;
      while( retry == true )
      {
         Interceptor next = getNext();
         try
         {
            if( trace )
               log.trace("invoke, method="+invocation.getMethod());
            result = next.invoke(invocation);
            break;
         }
         catch(ServiceUnavailableException e)
         {
            if( trace )
               log.trace("Invocation failed", e);
            /* RemoteException is thrown by the JRMPInvokerProxyHA when all
            targets are exhausted. It probably should be a custom subclass since
            RemoteException can be thrown by the application.
            */
            InvocationType type = (InvocationType) invocation.getType();
            waitOnInvokerProxy(ctx, type);
         }
      }
      return result;
   }

   /**
    * Loop trying to lookup the proxy invoker from jndi. This sleeps 1
    * second between lookup operations.
    * @param ctx - the invocation context to populate with the new invoker
    * @param type - the type of the invocation, InvocationType.REMOTE or
    *    InvocationType.HOME
    */
   private void waitOnInvokerProxy(InvocationContext ctx, InvocationType type)
   {
      if( trace )
         log.trace("Begin waitOnInvokerProxy");
      boolean isRemote = type == InvocationType.REMOTE;
      String jndiName = (String) ctx.getValue(InvocationKey.JNDI_NAME);
      if( isRemote == true )
         jndiName += "-RemoteInvoker";
      else
         jndiName += "-HomeInvoker";
      while( retry == true )
      {
         try
         {
            Thread.sleep(1000);
            InitialContext namingCtx = new InitialContext(retryEnv);
            if( trace )
               log.trace("Looking for invoker: "+jndiName);
            Invoker invoker = (Invoker) namingCtx.lookup(jndiName);
            if( trace )
               log.trace("Found invoker: "+invoker);
            ctx.setInvoker(invoker);
            break;
         }
         catch(Throwable t)
         {
            if( trace )
               log.trace("Failed to lookup proxy", t);
         }
      }
      if( trace )
         log.trace("End waitOnInvokerProxy");
   }

   /**
    * Writes the next interceptor.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      super.writeExternal(out);
      // Write out a version identifier for future extensibility
      out.writeInt(EXTERNAL_VERSION);
      // There is no additional data currently
   }

   /**
    * Reads the next interceptor.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      // Read the version identifier
      int version = in.readInt();
      if( version == EXTERNAL_VERSION )
      {
         // This version has no additional data
      }
      // Set the logging trace level
      trace = log.isTraceEnabled();
   }
}
