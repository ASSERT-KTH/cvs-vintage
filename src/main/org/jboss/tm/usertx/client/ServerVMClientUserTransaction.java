/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm.usertx.client;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;


/**
 *  The client-side UserTransaction implementation for clients
 *  operating in the same VM as the server.
 *  This will delegate all UserTransaction calls to the
 *  <code>TransactionManager</code> of the server.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.1 $
 */
public class ServerVMClientUserTransaction
   implements UserTransaction
{
   // Static --------------------------------------------------------

   /**
    *  Our singleton instance.
    */
   private static ServerVMClientUserTransaction singleton = null;

   /**
    *  Return a reference to the singleton instance.
    */
   public static ServerVMClientUserTransaction getSingleton()
   {
      if (singleton == null)
         singleton = new ServerVMClientUserTransaction();
      return singleton;
   }


   // Constructors --------------------------------------------------

   /**
    *  Create a new instance.
    */
   private ServerVMClientUserTransaction()
   {
      // Lookup the local TM
      try {
         tm = (TransactionManager)new InitialContext().lookup("java:/TransactionManager");

      } catch (NamingException ex) {
         throw new RuntimeException("TransactionManager not found: " + ex);
      }
   }

   // Public --------------------------------------------------------

   //
   // implements interface UserTransaction
   //

   public void begin()
      throws NotSupportedException, SystemException
   {
      tm.begin();
   }

   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             SecurityException,
             IllegalStateException,
             SystemException
   {
      tm.commit();
   }

   public void rollback()
      throws SecurityException,
             IllegalStateException,
             SystemException
   {
      tm.rollback();
   }

   public void setRollbackOnly()
      throws IllegalStateException,
             SystemException
   {
      tm.setRollbackOnly();
   }

   public int getStatus()
      throws SystemException
   {
      return tm.getStatus();
   }

   public void setTransactionTimeout(int seconds)
      throws SystemException
   {
      tm.setTransactionTimeout(seconds);
   }


   // Private -------------------------------------------------------

   /**
    *  The <code>TransactionManagerz</code> we delegate to.
    */
   private TransactionManager tm;
}
