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
import java.lang.reflect.UndeclaredThrowableException;

import javax.transaction.Transaction;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;

import org.jboss.proxy.Interceptor;

import org.jboss.system.Registry;
import org.jboss.util.id.GUID;

/**
 * A very simple implementation of it that branches to the local stuff.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.6 $
 */
public class InvokerInterceptor
   extends Interceptor
   implements Externalizable
{
   /** Serial Version Identifier. @since 1.2 */
   private static final long serialVersionUID = 2548120545997920357L;

   /** The value of our local Invoker.ID to detect when we are local. */
   private GUID invokerID = Invoker.ID;

   /** Invoker to the remote JMX node. */
   protected Invoker remoteInvoker;

   /** Static references to local invokers. */
   protected static Invoker localInvoker; 

   /** The InvokerProxyHA class */
   protected static Class invokerProxyHA;
   
   static
   {
      try
      {
         // Using Class.forName() to avoid security problems in the client
         invokerProxyHA = Class.forName("org.jboss.invocation.InvokerProxyHA");
      }
      catch (Throwable ignored)
      {
      }
   }
   
   /**
    * Get the local invoker reference, useful for optimization.
    */
   public static Invoker getLocal()
   {
      return localInvoker;
   }

   /**
    * Set the local invoker reference, useful for optimization.
    */
   public static void setLocal(Invoker invoker)
   {
      localInvoker = invoker;
   }

   /**
    * Exposed for externalization.
    */
   public InvokerInterceptor()
   {
      super();
   }

   /**
    * Returns wether we are local to the originating container or not.
    * 
    * @return true when we have the same GUID 
    */
   public boolean isLocal()
   {
      return invokerID.equals(Invoker.ID);
   }

   /**
    * Whether the target is local
    * 
    * @param invocation the invocation
    * @return true when the target is local
    */
   public boolean isLocal(Invocation invocation)
   {
      // No local invoker, it must be remote
      if (localInvoker == null)
         return false;

      // The proxy was downloaded from a remote location
      if (isLocal() == false)
      {
         // It is not clustered so we go remote
         if (isClustered(invocation) == false)
            return false;
      }
      
      // See whether we have a local target
      return hasLocalTarget(invocation);
   }
   
   /**
    * Whether we are in a clustered environment<p>
    * 
    * NOTE: This should be future compatible under any
    * new design where a prior target chooser interceptor
    * picks a non HA target than that code being
    * inside a ha invoker.
    * 
    * @param invocation the invocation
    * @return true when a clustered invoker
    */
   public boolean isClustered(Invocation invocation)
   {
      // No clustering classes
      if (invokerProxyHA == null)
         return false;
      
      // Is the invoker a HA invoker?
      InvocationContext ctx = invocation.getInvocationContext();
      Invoker invoker = ctx.getInvoker();
      return invoker != null && invokerProxyHA.isAssignableFrom(invoker.getClass());
   }
   
   /**
    * Whether there is a local target
    * 
    * @param invocation
    * @return true when in the registry
    */
   public boolean hasLocalTarget(Invocation invocation)
   {
      return Registry.lookup(invocation.getObjectName()) != null;
   }
   
   /**
    * The invocation on the delegate, calls the right invoker.  
    * Remote if we are remote, local if we are local. 
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // optimize if calling another bean in same server VM
      if (isLocal(invocation))
         return invokeLocal(invocation);
      else
         return invokeInvoker(invocation);
   }

   /**
    * Invoke using local invoker
    * 
    * @param invocation the invocation
    * @return the result
    * @throws Exception for any error
    */
   protected Object invokeLocal(Invocation invocation) throws Exception
   {
      return localInvoker.invoke(invocation);
   }

   /**
    * Invoke using local invoker and marshalled
    * 
    * @param invocation the invocation
    * @return the result
    * @throws Exception for any error
    */
   protected Object invokeMarshalled(Invocation invocation) throws Exception
   {
      MarshalledInvocation mi = new MarshalledInvocation(invocation);
      MarshalledValue copy = new MarshalledValue(mi);
      Invocation invocationCopy = (Invocation) copy.get();

      // copy the Tx
      Transaction tx = invocation.getTransaction();
      invocationCopy.setTransaction(tx);

      try
      {
         Object rtnValue = localInvoker.invoke(invocationCopy);
         MarshalledValue mv = new MarshalledValue(rtnValue);
         return mv.get();
      }
      catch(Throwable t)
      {
         MarshalledValue mv = new MarshalledValue(t);
         Throwable t2 = (Throwable) mv.get();
         if( t2 instanceof Exception )
            throw (Exception) t2;
         else
            throw new UndeclaredThrowableException(t2);
      }
   }

   /**
    * Invoke using invoker
    * 
    * @param invocation the invocation
    * @return the result
    * @throws Exception for any error
    */
   protected Object invokeInvoker(Invocation invocation) throws Exception
   {
      InvocationContext ctx = invocation.getInvocationContext();
      Invoker invoker = ctx.getInvoker();
      return invoker.invoke(invocation);
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
