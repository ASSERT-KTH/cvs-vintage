/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.jrmp.interfaces;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.rmi.ServerException;
import java.rmi.NoSuchObjectException;
import java.rmi.MarshalledObject;

import javax.transaction.SystemException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.local.LocalInvoker;

import org.jboss.security.SecurityAssociation;

import org.jboss.tm.TransactionPropagationContextFactory;

/**
*
* JRMPInvokerProxy, local to the proxy and is capable of delegating to local and JRMP implementations
* 
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.2 $
*
* <p><b>2001/11/19: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class JRMPInvokerProxy
implements Invoker, Externalizable
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
   
   
   /**
   *  Factory for transaction propagation contexts.
   *
   *  When set to a non-null value, it is used to get transaction
   *  propagation contexts for remote method invocations.
   *  If <code>null</code>, transactions are not propagated on
   *  remote method invocations.
   */
   protected static TransactionPropagationContextFactory tpcFactory = null;
   
   // Get and set methods
   
   //Local invoker reference, useful for optimization
   public static Invoker getLocal() { return localInvoker;}
   public static void setLocal(Invoker invoker) { localInvoker = invoker ;}

   // TPC factory
   public static void setTPCFactory(TransactionPropagationContextFactory tpcf) { tpcFactory = tpcf; }
   
   // Constructors --------------------------------------------------
   
   public JRMPInvokerProxy() {
      // For externalization to work
   }
   
   /**
   *  Create a new Proxy.
   *
   *  @param container
   *         The remote interface of the container invoker of the
   *         container we proxy for.
   */
   public JRMPInvokerProxy(Invoker remoteInvoker) 
   {
      this.remoteInvoker = remoteInvoker;
   }
   
   // Public --------------------------------------------------------
   
   
   /**
   * Returns wether we are local to the originating container or not. 
   */
   public boolean isLocal() { return containerStartup == Invoker.STARTUP; }
   
   
   // The name of of the server
   public String getServerHostName() throws Exception {return remoteInvoker.getServerHostName();}
   
   /**
   *  Return the transaction propagation context of the transaction
   *  associated with the current thread.
   *  Returns <code>null</code> if the transaction manager was never
   *  set, or if no transaction is associated with the current thread.
   */
   public Object getTransactionPropagationContext()
   throws SystemException
   {
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }
   
   
   /**
   * The invocation on the delegate, calls the right invoker.  Remote if we are remote, local if we
   * are local. 
   */
   public Object invoke(Invocation invocation)
   throws Exception
   {

      // Pass the current security information
      invocation.setPrincipal(SecurityAssociation.getPrincipal());
      invocation.setCredential(SecurityAssociation.getCredential());

      // optimize if calling another bean in same EJB-application
      if (isLocal()) {
         
         // The payload as is is good
         // FIXME FIXME FIXME FIXME FIXME ****USE THIS**** WHEN CL ARE INTEGRATED
         // return localInvoker.invoke(invocation);
         
         // FIXME FIXME FIXME FIXME FIXME REMOVE WHEN CL ARE INTEGRATED
         Object value;
         
         // FIXME FIXME FIXME FIXME FIXME REMOVE WHEN CL ARE INTEGRATED
         try {
            
            // FIXME FIXME FIXME FIXME FIXME REMOVE WHEN CL ARE INTEGRATED
            value = localInvoker.invoke(invocation);
            
            // FIXME FIXME FIXME FIXME FIXME REMOVE WHEN CL ARE INTEGRATED
            if (value instanceof MarshalledObject) return ((MarshalledObject) value).get();
               
            // FIXME FIXME FIXME FIXME FIXME REMOVE WHEN CL ARE INTEGRATED
            else return value;
         }
         // FIXME FIXME FIXME FIXME FIXME REMOVE WHEN CL ARE INTEGRATED
         catch (Exception e) {throw (Exception) new MarshalledObject(e).get();}
      
      
      }
      else {
         
         // We are going to go through a Remote invocation, switch to a Marshalled Invocation
         MarshalledInvocation mi = new MarshalledInvocation(invocation.payload);
         
         // Set the transaction propagation context
         mi.setTransactionPropagationContext(getTransactionPropagationContext());
         
         try{ 
            
            return ((MarshalledObject) remoteInvoker.invoke(mi)).get();
         } catch (ServerException ex) {
            // Suns RMI implementation wraps NoSuchObjectException in
            // a ServerException. We cannot have that if we want
            // to comply with the spec, so we unwrap here.
            if (ex.detail instanceof NoSuchObjectException)
               throw (NoSuchObjectException) ex.detail;
            throw ex;
         }
      
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
      out.writeObject(remoteInvoker);
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
      remoteInvoker = (Invoker) in.readObject();
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
