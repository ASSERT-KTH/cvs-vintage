/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;
import javax.ejb.Handle;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

import org.jboss.proxy.Interceptor;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.invocation.Invocation;

/*
import javax.naming.Name;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
*/
/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.5 $
*
* <p><b>2001/11/21: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class TransactionInterceptor
extends Interceptor
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //   private static final long serialVersionUID = 432426690456622923L;
   
      
   public static TransactionManager tm;

   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public TransactionInterceptor() {}
   
   
   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation) 
   throws Throwable
   {
      if (tm != null)
      {
         Transaction tx = tm.getTransaction();
         if (tx != null) invocation.setTransaction(tx);
      }
      return getNext().invoke(invocation);
   }
   
   
      /** Transaction manager. */
   public static void setTransactionManager(TransactionManager tmx) { tm = tmx;}
   
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
