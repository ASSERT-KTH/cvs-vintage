/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.tm;

import java.lang.ref.SoftReference;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;


import org.jboss.logging.Logger;

/**
 *  Our TransactionManager implementation.
 *
 *  @see <related>
 *  @author Rickard �berg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.27 $
 */
public class TxManager
   implements TransactionManager,
              TransactionPropagationContextImporter,
              TransactionPropagationContextFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   /**
    *  Default timeout in milliseconds.
    *  Must be >= 1000!
    */
   long timeOut = 5*60*1000; 
    
   // Static --------------------------------------------------------
 
   /**
    *  The singleton instance.
    */
   private static TxManager singleton = new TxManager();

   /**
    *  Get a reference to the singleton instance.
    */
   public static TxManager getInstance()
   {
      return singleton;
   }

   // Constructors --------------------------------------------------
 
   /**
    *  Private constructor for singleton. Use getInstance() to obtain
    *  a reference to the singleton.
    */
   private TxManager()
   {
   }

   // Public --------------------------------------------------------

   /**
    *  Begin a new transaction.
    *  The new transaction will be associated with the calling thread.
    */
   public void begin()
      throws NotSupportedException,
             SystemException
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();

      if (current != null && !current.isDone())
         throw new NotSupportedException("Transaction already active, " +
                                         "cannot nest transactions.");

      TxCapsule txCapsule = TxCapsule.getInstance(timeOut);
      TransactionImpl tx = txCapsule.getTransactionImpl();
      threadTx.set(tx);
      globalIdTx.put(tx.getGlobalId(), tx);
   }

   /**
    *  Commit the transaction associated with the currently running thread.
    */
   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             java.lang.SecurityException,
             IllegalStateException,
             SystemException
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();

      if (current != null) {
         current.commit();
         threadTx.set(null);
      } else
         throw new IllegalStateException("No transaction.");
   }
 
   /**
    *  Return the status of the transaction associated with the currently
    *  running thread, or <code>Status.STATUS_NO_TRANSACTION</code> if no
    *  active transaction is currently associated.
    */
   public int getStatus()
      throws SystemException
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();

      if (current != null)
         return current.getStatus();
      else
         return Status.STATUS_NO_TRANSACTION;
   }

   /**
    *  Return the transaction currently associated with the invoking thread,
    *  or <code>null</code> if no active transaction is currently associated.
    */
   public Transaction getTransaction()
      throws SystemException
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();

      if (current != null && current.isDone()) {
         threadTx.set(null);
         return null;
      }
      return current;
   }

   /**
    *  Resume a transaction.
    *
    *  Note: This will not enlist any resources involved in this
    *  transaction. According to JTA1.0.1 specification section 3.2.3,
    *  that is the responsibility of the application server.
    */
   public void resume(Transaction transaction)
      throws InvalidTransactionException,
             IllegalStateException,
             SystemException
   {
      if (transaction != null && !(transaction instanceof TransactionImpl))
         throw new RuntimeException("Not a TransactionImpl, but a " +
                                    transaction.getClass().getName() + ".");

      TransactionImpl current = (TransactionImpl)threadTx.get();
        
      if (current != null)
         throw new IllegalStateException("Already associated with a tx");

      if (current != transaction)
         threadTx.set(transaction);
   }

   /**
    *  Suspend the transaction currently associated with the current
    *  thread, and return it.
    *
    *  Note: This will not delist any resources involved in this
    *  transaction. According to JTA1.0.1 specification section 3.2.3,
    *  that is the responsibility of the application server.
    */
   public Transaction suspend()
      throws SystemException
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();
        
      if (current != null)
         threadTx.set(null);
        
      return current;
   }

   /**
    *  Roll back the transaction associated with the currently running thread.
    */
   public void rollback()
      throws IllegalStateException,
             java.lang.SecurityException,
             SystemException
   { 
      TransactionImpl current = (TransactionImpl)threadTx.get();

      if (current != null) {
         current.rollback();
         threadTx.set(null);
      } else
         throw new IllegalStateException("No transaction.");
   }

   /**
    *  Mark the transaction associated with the currently running thread
    *  so that the only possible outcome is a rollback.
    */
   public void setRollbackOnly()
      throws IllegalStateException,
             SystemException
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();

      if (current != null)
         current.setRollbackOnly();
      else
         throw new IllegalStateException("No transaction.");
   }

   /**
    *  Set the transaction timeout for new transactions started here.
    */
   public void setTransactionTimeout(int seconds)
      throws SystemException
   {
      timeOut = 1000 * seconds;
   }
    
   /**
    *  Get the transaction timeout for new transactions started here.
    *
    *  @return Transaction timeout in seconds.
    */
   public int getTransactionTimeout()
   {
      return (int)(timeOut / 1000);
   }

   /*
    *  The following 2 methods are here to provide association and
    *  disassociation of the thread.
    */
   public Transaction disassociateThread()
   {
      TransactionImpl current = (TransactionImpl)threadTx.get();
        
      threadTx.set(null);
        
      return current;
   }
    
   public void associateThread(Transaction transaction)
   {
      if (transaction != null && !(transaction instanceof TransactionImpl))
         throw new RuntimeException("Not a TransactionImpl, but a " +
                                    transaction.getClass().getName() + ".");

      // Associate with the thread
      threadTx.set(transaction);
   }


   // Implements TransactionPropagationContextImporter ---------------

   /**
    *  Import a transaction propagation context into this TM.
    *  The TPC is loosely typed, as we may (at a later time) want to
    *  import TPCs that come from other transaction domains without
    *  offloading the conversion to the client.
    *
    *  @param tpc The transaction propagation context that we want to
    *             import into this TM. Currently this is an instance
    *             of GlobalId. At some later time this may be an instance
    *             of a transaction propagation context from another
    *             transaction domain like 
    *             org.omg.CosTransactions.PropagationContext. 
    *
    *  @return A transaction representing this transaction propagation
    *          context, or null if this TPC cannot be imported.
    */
   public Transaction importTransactionPropagationContext(Object tpc)
   {
      if (tpc instanceof GlobalId)
         return (Transaction)globalIdTx.get((GlobalId)tpc);
      else {
         Logger.warning("Cannot import transaction propagation context: " +
                        tpc.toString());
         return null;
      }
   }


   // Implements TransactionPropagationContextFactory ---------------

   /**
    *  Return a TPC for the current transaction.
    */
   public Object getTransactionPropagationContext()
   {
      return getTransactionPropagationContext((Transaction)threadTx.get());
   }
 
   /**
    *  Return a TPC for the argument transaction.
    */
   public Object getTransactionPropagationContext(Transaction tx)
   {
      // If no transaction or unknown transaction class, return null.
      if (tx == null || !(tx instanceof TransactionImpl))
         return null;

      return ((TransactionImpl)tx).getGlobalId();
   }


   // Package protected ---------------------------------------------
    
   /**
    *  Release the given TransactionImpl.
    */
   void releaseTransactionImpl(TransactionImpl tx)
   {
      globalIdTx.remove(tx.getGlobalId());
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
 
   /**
    *  This keeps track of the transaction association with threads.
    *  In some cases terminated transactions may not be cleared here.
    */
   private ThreadLocal threadTx = new ThreadLocal();

   /**
    *  This map contains the active transactions as values.
    *  The keys are the <code>GlobalId</code>s of the transactions.
    */
   private Map globalIdTx = Collections.synchronizedMap(new HashMap());

   // Inner classes -------------------------------------------------
}
