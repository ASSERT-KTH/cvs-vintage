/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;
import java.security.Principal;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;

import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
import org.jboss.tm.TransactionPropagationContextFactory;

import java.util.HashMap;
import org.jboss.security.SecurityAssociation;


/**
 *  Abstract superclass of JRMP client-side proxies.
 *      
 *  @see ContainerRemote
 *  @author Rickard Öberg (rickard.oberg@telkel.com)
 *  @version $Revision: 1.10 $
 */
public abstract class GenericProxy
   implements java.io.Externalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   /**
    *  Our transaction manager.
    *
    *  When set to a non-null value, this is used for getting the
    *  transaction to use for optimized local method invocations.
    *  If <code>null</code>, transactions are not propagated on
    *  optimized local method invocations.
    */
   private static TransactionManager tm = null;

   /**
    *  Factory for transaction propagation contexts.
    *
    *  When set to a non-null value, it is used to get transaction
    *  propagation contexts for remote method invocations.
    *  If <code>null</code>, transactions are not propagated on
    *  remote method invocations.
    */
   protected static TransactionPropagationContextFactory tpcFactory = null;

   /**
    *  This map maps JNDI names of containers to the remote interfaces
    *  of the container invokers of these containers.
    */
   static HashMap invokers = new HashMap(); // Prevent DGC

   /**
    *  Return the remote interface of the container invoker for the
    *  container with the given JNDI name.
    */
   private static ContainerRemote getLocal(String jndiName)
   {
      return (ContainerRemote)invokers.get(jndiName);
   }

   /**
    *  Add an invoker to the invokers map.
    */
   public static void addLocal(String jndiName, ContainerRemote invoker)
   {
      invokers.put(jndiName, invoker);
   }

   /**
    *  Remove an invoker from the invokers map.
    */
   public static void removeLocal(String jndiName)
   {
      invokers.remove(jndiName);
   }

   /**
    *  Set the transaction manager.
    */
   public static void setTransactionManager(TransactionManager txMan)
   {
      tm = txMan;
   }

   /**
    *  Set the transaction propagation context factory.
    */
   public static void setTPCFactory(TransactionPropagationContextFactory tpcf)
   {
      tpcFactory = tpcf;
   }

   // Constructors --------------------------------------------------

   /**
    *  A public, no-args constructor for externalization to work.
    */
   public GenericProxy()
   {
      // For externalization to work
   }

   /**
    *  Create a new GenericProxy.
    *
    *  @param name
    *         The JNDI name of the container that we proxy for.
    *  @param container
    *         The remote interface of the container invoker of the
    *         container we proxy for.
    *  @param optimize
    *         If <code>true</true>, this proxy will attempt to optimize
    *         VM-local calls.
    */
   protected GenericProxy(String name, ContainerRemote container,
                          boolean optimize)
   {
      this.name = name;
      this.container = container;
      this.optimize = optimize;
   }
   
   // Public --------------------------------------------------------

   /**
    *  Externalize this instance.
    *
    *  If this instance lives in a different VM than its container
    *  invoker, the remote interface of the container invoker is
    *  not externalized.
    */
   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
        out.writeUTF(name);
        out.writeObject(isLocal() ? container : null);
        out.writeLong(containerStartup);
        out.writeBoolean(optimize);
   }

   /**
    *  Un-externalize this instance.
    *
    *  If this instance is deserialized in the same VM as its container
    *  invoker, the remote interface of the container invoker is
    *  restored by looking up the name in the invokers map.
    */
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      name = in.readUTF();
      container = (ContainerRemote)in.readObject();
      containerStartup = in.readLong();
      optimize = in.readBoolean();
      
      if (isLocal())
      {
         // VM-local optimization; still follows RMI-semantics though
         container = getLocal(name);
      }
   }

   // Package protected ---------------------------------------------

   /**
    *  Return the principal to use for invocations with this proxy.
    */
   protected Principal getPrincipal()
   {
      return SecurityAssociation.getPrincipal();
   }

   /**
    *  Return the credentials to use for invocations with this proxy.
    */
   protected Object getCredential()
   {
      return SecurityAssociation.getCredential();
   }

   /**
    *  Return the transaction associated with the current thread.
    *  Returns <code>null</code> if the transaction manager was never
    *  set, or if no transaction is associated with the current thread.
    */
   protected Transaction getTransaction()
      throws SystemException
   {
      return (tm == null) ? null : tm.getTransaction();
   }

   /**
    *  Return the transaction propagation context of the transaction
    *  associated with the current thread.
    *  Returns <code>null</code> if the transaction manager was never
    *  set, or if no transaction is associated with the current thread.
    */
   protected Object getTransactionPropagationContext()
      throws SystemException
   {
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }

   // Protected -----------------------------------------------------

   /**
    *  The JNDI name of the container that we proxy for.
    */
   protected String name;

   /**
    *  The remote interface of the container invoker of the container
    *  we proxy for.
    */
   protected ContainerRemote container;

   /**
    *  If <code>true</true>, this proxy will attempt to optimize
    *  VM-local calls.
    */
   protected boolean optimize = false;


   /**
    *  Returns <code>true</code> iff this instance lives in the same
    *  VM as its container.
    */
   protected boolean isLocal()
   {
      return containerStartup == ContainerRemote.startup;
   }

   // Private -------------------------------------------------------

   /**
    *  Time of <code>ContainerRemote</code> class initialization as
    *  read when this instance was created. This time is used to
    *  determine if this instance lives in the same VM as the container.
    *
    *  This is not completely fail-safe: If the ContainerRemote class
    *  is initialized in the server and at the client within the same
    *  millisecond, the proxy will think it is local, and the client
    *  will fail. This is, however, very unlikely to happen in real
    *  life, and next time the client is started it would run OK.
    */
   private long containerStartup = ContainerRemote.startup;

   // Inner classes -------------------------------------------------
}

