/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation.jrmp.interfaces;






import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.SystemException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.local.LocalInvoker;
import org.jboss.security.SecurityAssociation;
import org.jboss.tm.TransactionPropagationContextFactory;

/**
 * JRMPInvokerProxy, local to the proxy and is capable of delegating to local and JRMP implementations
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.8 $
 *
 * <p><b>2001/11/19: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public class JRMPInvokerProxy
   implements Invoker, Externalizable
{
   /** Serial Version Identifier. */
   // private static final long serialVersionUID = 1870461898442160570L;
   
   // Attributes ----------------------------------------------------
   
   // Invoker to the remote JMX node
   protected Invoker remoteInvoker;
   
   /**
    * Factory for transaction propagation contexts.
    *
    * @todo: marcf remove all transaction spill from here
    * 
    * When set to a non-null value, it is used to get transaction
    * propagation contexts for remote method invocations.
    * If <code>null</code>, transactions are not propagated on
    * remote method invocations.
    */
   protected static TransactionPropagationContextFactory tpcFactory = null;
   
   //  @todo: MOVE TO TRANSACTION
   // 
   // TPC factory
   public static void setTPCFactory(TransactionPropagationContextFactory tpcf) {
      tpcFactory = tpcf;
   }
   
   /**
    * Exposed for externalization.
    */
   public JRMPInvokerProxy() {
      super();
   }
   
   /**
    * Create a new Proxy.
    *
    * @param container    The remote interface of the container invoker of the
    *                     container we proxy for.
    */
   public JRMPInvokerProxy(final Invoker remoteInvoker) 
   {
      this.remoteInvoker = remoteInvoker;
   }
   
   /**
    * The name of of the server.
    */
   public String getServerHostName() throws Exception {
      return remoteInvoker.getServerHostName();
   }
   
   /**
    * ???
    *
    * @todo: MOVE TO TRANSACTION
    *  
    * @return the transaction propagation context of the transaction
    *         associated with the current thread.
    *         Returns <code>null</code> if the transaction manager was never
    *         set, or if no transaction is associated with the current thread.
    */
   public Object getTransactionPropagationContext()
      throws SystemException
   {
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }
   
   /**
    * The invocation on the delegate, calls the right invoker.  Remote if we are remote, 
    * local if we are local. 
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // We are going to go through a Remote invocation, switch to a Marshalled Invocation
      MarshalledInvocation mi = new MarshalledInvocation(invocation);
         
      // Set the transaction propagation context
      //  @todo: MOVE TO TRANSACTION
      mi.setTransactionPropagationContext(getTransactionPropagationContext());
         
      try { 
         return ((MarshalledObject) remoteInvoker.invoke(mi)).get();
      }
      catch (ServerException ex) {
         // Suns RMI implementation wraps NoSuchObjectException in
         // a ServerException. We cannot have that if we want
         // to comply with the spec, so we unwrap here.
         if (ex.detail instanceof NoSuchObjectException)
         {
            throw (NoSuchObjectException) ex.detail;
         }
         //likewise
         if (ex.detail instanceof TransactionRolledbackException)
         {
            throw (TransactionRolledbackException) ex.detail;
         }
         /* Shouldn't we unwrap _all_ remote exceptions with this code? 
         if (ex.detail instanceof RemoteException)
         {
            throw (RemoteException) ex.detail;
         }
         */
         throw ex;
      }  
   }
   
   /**
    * Externalize this instance.
    *
    * If this instance lives in a different VM than its container
    * invoker, the remote interface of the container invoker is
    * not externalized.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   { 
      out.writeObject(remoteInvoker);
   }
   
   /**
    * Un-externalize this instance.
    *
    * We check timestamps of the interfaces to see if the instance is in the original VM of creation
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      remoteInvoker = (Invoker) in.readObject();
   }
}
