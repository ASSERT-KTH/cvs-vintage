/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.Invoker;

import org.jboss.proxy.Interceptor;

import org.jboss.util.id.GUID;

/**
 * A very simple implementation of it that branches to the local stuff.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.4 $
 *
 * <p><b>2001/02/28: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public class InvokerInterceptor
   extends Interceptor
   implements Externalizable
{
   /** Serial Version Identifier. */
   // private static final long serialVersionUID = 1870461898442160570L;

   /** The value of our local Invoker.ID to detect when we are local. */
   private GUID invokerID = Invoker.ID;

   /** Invoker to the remote JMX node. */
   protected Invoker remoteInvoker;

   /** Static references to local invokers. */
   protected static Invoker localInvoker;

   /**
    * Get the local invoker reference, useful for optimization.
    */
   public static Invoker getLocal() {
      return localInvoker;
   }

   /**
    * Set the local invoker reference, useful for optimization.
    */
   public static void setLocal(Invoker invoker) {
      localInvoker = invoker;
   }

   /**
    * Exposed for externalization.
    */
   public InvokerInterceptor() {
      super();
   }

   /**
    * Returns wether we are local to the originating container or not.
    */
   public boolean isLocal() {
      return invokerID.equals(Invoker.ID);
      // return containerStartup == Invoker.STARTUP;
   }

   /**
    * The invocation on the delegate, calls the right invoker.
    * Remote if we are remote, local if we are local.
    */
   public InvocationResponse invoke(Invocation invocation)
      throws Throwable
   {
      // optimize if calling another bean in same EJB-application
      if (isLocal()) {
         // The payload as is is good
         return localInvoker.invoke(invocation);
      }
      else {
         // this payload will go through marshalling
         return invocation.getInvocationContext().getInvoker().invoke(invocation);
      }
   }

   /**
    * Externalize this instance.
    *
    * <p>
    * If this instance lives in a different VM than its container
    * invoker, the remote interface of the container invoker is
    * not externalized.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(invokerID);
   }

   /**
    * Un-externalize this instance.
    *
    * <p>
    * We check timestamps of the interfaces to see if the instance is in the original
    * VM of creation
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      invokerID = (GUID)in.readObject();
   }
}
