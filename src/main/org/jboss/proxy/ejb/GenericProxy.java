/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy.ejb;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import javax.transaction.TransactionManager;
import java.security.Principal;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

import org.jboss.proxy.ejb.ReadAheadBuffer;
import org.jboss.proxy.ejb.ListEntityProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.security.SecurityAssociation;

/**
 * Generic Proxy 
 *
 * These proxies are independent of the transportation protocol.  Their role is to take
 * care of some of the local calls on the client (done in extension like EJB) and to 
 * delegate the calls to a "delegate invoker". 
 *      
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.2 $
 *
 * <p><b>2001/11/19: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public abstract class GenericProxy
   implements Externalizable, InvocationHandler
{
   /** Invoker to the delegate */
   protected Invoker invoker;
   
   /**
    * A proxy is associated with a given container, we identify the container by 
    * any object in RH 3.0 this is for example a simple MBean identifying the 
    * container in the future we should give just an abstract key into a metadata 
    * container repository in the server.
    */
   protected transient String objectName;
   
   protected String jndiName;
   
   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};
   
   // Static method references 
   protected static final Method TO_STRING, HASH_CODE, EQUALS;
   
   // Static method references to EJB
   protected static final Method GET_PRIMARY_KEY, GET_HANDLE, GET_EJB_HOME, IS_IDENTICAL;
   
   /** Initialize the static variables. */
   static {
      try {
         
         // Get the methods from Object
         Class[] empty = {};
         Class type = Object.class;
         
         TO_STRING = type.getMethod("toString", empty);
         HASH_CODE = type.getMethod("hashCode", empty);
         EQUALS = type.getMethod("equals", new Class[] { type });
         
         // Get the methods from EJBObject
         type = EJBObject.class;
         
         GET_PRIMARY_KEY = type.getMethod("getPrimaryKey", empty);
         GET_HANDLE = type.getMethod("getHandle", empty);
         GET_EJB_HOME = type.getMethod("getEJBHome", empty);
         IS_IDENTICAL = type.getMethod("isIdentical", new Class[] { type });
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
   protected static TransactionManager tm = null;
   
   /** Transaction manager. */
   public static void setTransactionManager(TransactionManager txm) { tm = txm; }
   
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
    *  @param container
    *  @param invoker
    */
   protected GenericProxy(final String jndiName, final Invoker invoker)
   {
      this.jndiName = jndiName;
      this.objectName = "jboss.j2ee:service=EJB,jndiName="+jndiName;
      this.invoker = invoker;
   }
   
   /**
    * Test the identitiy of an <tt>EJBObject</tt>.
    *
    * @param a    <tt>EJBObject</tt>.
    * @param b    Object to test identity with.
    * @return     True if objects are identical.
    *
    * @throws RemoteException      Failed to get primary key.
    * @throws ClassCastException   Not an EJBObject instance.
    */
   protected Boolean isIdentical(final Object a, final Object b)
      throws RemoteException
   {
      if (a == null) return new Boolean(a==b);
         final EJBObject ejb = (EJBObject)a;
      final Object pk = ejb.getPrimaryKey();
      return new Boolean(pk.equals(b));
   }
   
   // Get Principal and credentials 
   protected Principal getPrincipal() { return SecurityAssociation.getPrincipal(); }
   protected Object getCredential() { return SecurityAssociation.getCredential(); }
   
   /**
    * Return the transaction associated with the current thread.
    * Returns <code>null</code> if the transaction manager was never
    * set, or if no transaction is associated with the current thread.
    */
   protected Transaction getTransaction() throws SystemException
   {
      return (tm == null) ? null : tm.getTransaction();
   }
   
   protected EJBHome getEJBHome() throws NamingException {
      return (EJBHome) new InitialContext().lookup(jndiName);
   }

   /**
    * Invoke a method.
    *
    * The actual optimization happens in the delegate, the responsibility here it 
    * set the variables on the Invocation
    *
    */
   protected Object invoke(Invocation invocation) throws Exception
   {
      // just delegate
      return invoker.invoke(invocation);
   }
   
   /**
    * Externalize this instance.
    *    
    * If this instance lives in a different VM than its container
    * invoker, the remote interface of the container invoker is
    * not externalized.
    */
   public void writeExternal(final ObjectOutput out) throws IOException
   {
      out.writeUTF(jndiName);
      out.writeObject(invoker);
   }
   
   /**
    * Un-externalize this instance.
    *
    * If this instance is deserialized in the same VM as its container
    * invoker, the remote interface of the container invoker is
    * restored by looking up the name in the invokers map.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      jndiName = in.readUTF();
      objectName = "jboss.j2ee:service=EJB,jndiName="+jndiName;
      invoker = (Invoker) in.readObject();
   }
}
