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
import org.jboss.invocation.Invoker;
/*

import java.rmi.ServerException;
import java.rmi.NoSuchObjectException;
import java.rmi.MarshalledObject;

import javax.transaction.SystemException;

import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.local.LocalInvoker;

import org.jboss.security.SecurityAssociation;

import org.jboss.tm.TransactionPropagationContextFactory;
*/

import org.jboss.proxy.Interceptor;

/**
* InvokerInterceptor
*
* a very simple implementation of it that branches to the local stuff
* 
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
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
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //   private static final long serialVersionUID = 1870461898442160570L;
   
   // Attributes ----------------------------------------------------
   
   // We use this startup time to find if we are in the original VM
   private long containerStartup = Invoker.STARTUP;
   
   // Invoker to the remote JMX node
   protected Invoker remoteInvoker;
   
   // Static references to local invokers
   protected static Invoker localInvoker; 
   
   
   // Get and set methods
   
   //Local invoker reference, useful for optimization
   public static Invoker getLocal() { return localInvoker;}
   public static void setLocal(Invoker invoker) { localInvoker = invoker ;}
   
   // Constructors --------------------------------------------------
   
   public InvokerInterceptor() {
      // For externalization to work
   }
   
   
   // Public --------------------------------------------------------
   
   
   /**
   * Returns wether we are local to the originating container or not. 
   */
   public boolean isLocal() { return containerStartup == Invoker.STARTUP; }
   
   /**
   * The invocation on the delegate, calls the right invoker.  Remote if we are remote, local if we
   * are local. 
   */
   public Object invoke(Invocation invocation)
   throws Exception
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
   *  Externalize this instance.
   *
   *  If this instance lives in a different VM than its container
   *  invoker, the remote interface of the container invoker is
   *  not externalized.
   */
   public void writeExternal(final ObjectOutput out)
   throws IOException
   { 
      out.writeLong(containerStartup);
   }
   
   /**
   *  Un-externalize this instance.
   *
   *  We check timestamps of the interfaces to see if the instance is in the original VM of creation
   */
   public void readExternal(final ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      containerStartup = in.readLong();
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
