/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.rmi.MarshalledObject;
import java.rmi.ServerException;
import java.rmi.NoSuchObjectException;

import java.util.HashMap;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.tm.TransactionPropagationContextFactory;

import org.jboss.security.SecurityAssociation;

/**
 * Abstract superclass of JRMP client-side proxies.
 *      
 * @see ContainerRemote
 * 
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a> *  
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.16 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/10/16: billb</b>
 * <ol>
 *   <li>if proxy serialized more than once, container would be null and proxy wouldn't work
 * </ol>
 */
public abstract class GenericProxy
   implements Externalizable
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
   private static final long serialVersionUID = 1870461898442160570L;
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};
    
   /** {@link Object#toString} method reference. */
   protected static final Method TO_STRING;

   /** {@link Object#hashCode} method reference. */
   protected static final Method HASH_CODE;

   /** {@link Object#equals} method reference. */
   protected static final Method EQUALS;

   /**
    * Initialize {@link Object} method references.
    */
   static {
       try {
           final Class[] empty = {};
           final Class type = Object.class;

           TO_STRING = type.getMethod("toString", empty);
           HASH_CODE = type.getMethod("hashCode", empty);
           EQUALS = type.getMethod("equals", new Class[] { type });
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new ExceptionInInitializerError(e);
       }
   }
    
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
   private static HashMap invokers = new HashMap(); // Prevent DGC

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

      // get a context handle
      this.initialContextHandle = InitialContextHandle.create();
   }
   
   // Public --------------------------------------------------------

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
        out.writeUTF(name);
        out.writeObject(container);
        out.writeLong(containerStartup);
        out.writeBoolean(optimize);
        out.writeObject(initialContextHandle);
   }

   /**
    *  Un-externalize this instance.
    *
    *  If this instance is deserialized in the same VM as its container
    *  invoker, the remote interface of the container invoker is
    *  restored by looking up the name in the invokers map.
    */
   public void readExternal(final ObjectInput in)
       throws IOException, ClassNotFoundException
   {
      name = in.readUTF();
      container = (ContainerRemote)in.readObject();
      containerStartup = in.readLong();
      optimize = in.readBoolean();
      initialContextHandle = (InitialContextHandle)in.readObject();
      
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
   protected boolean optimize; // = false;

   /**
    *  Provides access to the correct naming context for handle objects.
    */ 
   protected InitialContextHandle initialContextHandle;
    
   /**
    *  Returns <code>true</code> iff this instance lives in the same
    *  VM as its container.
    */
   protected boolean isLocal()
   {
      return containerStartup == ContainerRemote.STARTUP;
   }

    /**
     * Create an <tt>InitialContext</tt> using the saved environment or 
     * create a vanilla <tt>InitialContext</tt> when the enviroment
     * is <i>null</i>.
     *
     * @return  <tt>InitialContext</tt> suitable for the bean that this
     *          is a proxy for.
     *
     * @throws NamingException    Failed to create <tt>InitialContext</tt>.
     */
    protected InitialContext createInitialContext() 
        throws NamingException
    {
        return initialContextHandle.getInitialContext();
    }
    
    /**
     * Invoke the container to handle this method invocation.
     *
     * <p>If optimization is enabled and this is a local proxy, then the
     *    container is invoked directly, else a remote call is made.
     *
     * @param id        ???
     * @param method    The method to invoke.
     * @param args      The arguments passed to the method.
     *
     * @throws Throwable    Failed to invoke container.
     */
    protected Object invokeContainer(final Object id,
                                     final Method method,
                                     final Object[] args)
        throws Throwable
    {
        Object result;

        // optimize if calling another bean in same EJB-application
        if (optimize && isLocal()) {
            result = container.invoke(id,
                                      method,
                                      args,
                                      getTransaction(), 
                                      getPrincipal(),
                                      getCredential());
        }
        else {
            MarshalledObject mo = createMarshalledObject(id, method, args);

            // Invoke on the remote server, enforce marshaling
            if (isLocal()) {
                // ensure marshaling of exceptions is done properly
                try {
                    result = container.invoke(mo).get();
                }
                catch (Throwable e) {
                    throw (Throwable)new MarshalledObject(e).get();
                }
            }
            else {
                // Marshaling is done by RMI
                try {
                    result = container.invoke(mo).get();
                } catch (ServerException ex) {
                    // Suns RMI implementation wraps NoSuchObjectException in
                    // a ServerException. We cannot have that if we want
                    // to comply with the spec, so we unwrap here.
                    if (ex.detail instanceof NoSuchObjectException)
                        throw ex.detail;
                    throw ex;
                }
            }
        }
        
        return result;
    }

    /**
     * Create a <tt>MarshalledObject</tt> suitable for
     * invoking a remote container with.
     *
     * @param id        ???
     * @param method    The method to invoke.
     * @param args      The arguments passed to the method.
     * @return          <tt>MarshalledObject</tt> suitable for invoking 
     *                  a remote container with.
     *
     * @throws SystemException    Failed to get transaction.
     * @throws IOException        Failed to create <tt>MarshalledObject</tt>.
     */
    protected MarshalledObject createMarshalledObject(final Object id,
                                                      final Method method, 
                                                      final Object[] args)
        throws SystemException, IOException
    {
        RemoteMethodInvocation rmi =
            new RemoteMethodInvocation(id, method, args);

        // Set the transaction propagation context
        rmi.setTransactionPropagationContext(getTransactionPropagationContext());

        // Set the security stuff
        // MF fixme this will need to use "thread local" and therefore same construct as above
        // rmi.setPrincipal(sm != null? sm.getPrincipal() : null);
        // rmi.setCredential(sm != null? sm.getCredential() : null);
        // is the credential thread local? (don't think so... but...)
        rmi.setPrincipal(getPrincipal());
        rmi.setCredential(getCredential());

        return new MarshalledObject(rmi);
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
   private long containerStartup = ContainerRemote.STARTUP;

   // Inner classes -------------------------------------------------
}

