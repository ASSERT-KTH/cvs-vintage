/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy.ejb;

import org.jboss.proxy.Interceptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.lang.reflect.Method;

import org.jboss.invocation.Invocation;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.naming.NamingException;

import javax.ejb.EJBObject;
import javax.naming.InitialContext;

/*

import java.lang.reflect.InvocationHandler;
import javax.transaction.TransactionManager;
import java.security.Principal;
import javax.transaction.Transaction;
import javax.transaction.SystemException;


import org.jboss.proxy.ejb.ReadAheadBuffer;
import org.jboss.proxy.ejb.ListEntityProxy;
import org.jboss.invocation.Invoker;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.security.SecurityAssociation;
*/

/**
 * Generic Proxy 
 *
 * These proxies are independent of the transportation protocol.  Their role is to take
 * care of some of the local calls on the client (done in extension like EJB) 
 *      
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1 $
 *
 * <p><b>2001/11/19: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public abstract class GenericEJBInterceptor
extends Interceptor
implements Externalizable
{
   
   public static final Integer JNDI_NAME = new Integer(new String("JNDI_NAME").hashCode());
   
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
    *  A public, no-args constructor for externalization to work.
    */
   public GenericEJBInterceptor()
   {
      // For externalization to work
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
/*
   protected Boolean isIdentical(final Object a, final Object b)
      throws RemoteException
   {
      if (a == null) return new Boolean(a==b);
         final EJBObject ejb = (EJBObject)a;
      final Object pk = ejb.getPrimaryKey();
      return new Boolean(pk.equals(b));
   }
*/   
   
   protected EJBHome getEJBHome(Invocation invocation) throws NamingException {
      return (EJBHome) new InitialContext().lookup((String) invocation.getInvocationContext().getValue(JNDI_NAME));
   }
}
  