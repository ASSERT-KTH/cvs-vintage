/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm.usertx.client;


import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.tm.usertx.interfaces.UserTransactionStartedListener;


/**
 *  The client-side UserTransaction implementation for clients
 *  operating in the same VM as the server.
 *  This will delegate all UserTransaction calls to the
 *  <code>TransactionManager</code> of the server.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.3 $
 */
public class ServerVMClientUserTransaction
   implements UserTransaction
{
   // Static --------------------------------------------------------

   /**
    *  Our singleton instance.
    */
   private static ServerVMClientUserTransaction singleton;


   /**
    *  The <code>TransactionManager</code> we delegate to.
    */
   private final TransactionManager tm;


   private final UserTransactionStartedListener listener;

   /**
    *  Return a reference to the singleton instance.
    */
   public static ServerVMClientUserTransaction getSingleton()
   {
      if (singleton == null) 
      {
         throw new IllegalStateException("ServerVMClientUserTransaction not yet available, ClientUserTransactionService is not started");
      } // end of if ()
      
      return singleton;
   }


   // Constructors --------------------------------------------------

   /**
    *  Create a new instance.
    */
   public ServerVMClientUserTransaction(final TransactionManager tm, UserTransactionStartedListener listener)
   {
      if (singleton != null) 
      {
         throw new IllegalStateException("You can only create one ServerVMClientUserTransaction!");
      } // end of if ()
      
      this.tm = tm;
      this.listener = listener;
      singleton = this;
   }

   public void clearSingleton()
   {
      singleton = null;
   }

   // Public --------------------------------------------------------


   //
   // implements interface UserTransaction
   //

   public void begin()
      throws NotSupportedException, SystemException
   {
      tm.begin();
      listener.userTransactionStarted();
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


}
