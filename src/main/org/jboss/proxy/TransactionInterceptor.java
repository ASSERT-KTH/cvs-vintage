/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy;

import javax.transaction.TransactionManager;

import org.jboss.proxy.Interceptor;

import org.jboss.invocation.Invocation;

/**
 * An interceptor whichs sets the transaction on the invocation
 * if the transaction manager has been previously given to us.
 *      
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.4 $
 *
 * <p><b>2001/11/21: marcf</b>
 * <ul>
 *   <li>Initial checkin
 * </ul>
 */
public class TransactionInterceptor
   extends Interceptor
{
   /** Serial Version Identifier. */
   // private static final long serialVersionUID = 432426690456622923L;
      
   private static TransactionManager tm;
   
   /**
    * No-argument constructor for externalization.
    */
   public TransactionInterceptor()
   {
      super();
   }
    
   public Object invoke(Invocation invocation) 
      throws Throwable
   {
      if (tm != null) {
         invocation.setTransaction(tm.getTransaction());
      }
      
      return getNext().invoke(invocation);
   }
   
   /**
    * Set the transaction manaer.
    */
   public static void setTransactionManager(final TransactionManager tm) {
      TransactionInterceptor.tm = tm;
   }
}
