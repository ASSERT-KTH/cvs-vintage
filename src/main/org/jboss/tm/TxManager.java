/*
* JBoss, the OpenSource J2EE webOS
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
 * Our TransactionManager implementation.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.34 $
 */
public class TxManager
   implements TransactionManager,
              TransactionPropagationContextImporter,
              TransactionPropagationContextFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   /** Instance logger. */
   private Logger log = Logger.getLogger(this.getClass());

   /** True if debug messages should be logged. */
   private boolean debug = log.isDebugEnabled();
   
   /**
    *  Default timeout in milliseconds.
    *  Must be >= 1000!
    */
   private long timeOut = 5*60*1000; 
    
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
   public void begin() throws NotSupportedException, SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null) {
         if (current.isDone()) {
            ti.tx = null;
         }
         else {
            throw new NotSupportedException
               ("Transaction already active, cannot nest transactions.");
         }
      }

      long timeout = (ti.timeout == 0) ? timeOut : ti.timeout;
      TxCapsule txCapsule = TxCapsule.getInstance(timeout);
      TransactionImpl tx = txCapsule.getTransactionImpl();
      ti.tx = tx;
      globalIdTx.put(tx.getGlobalId(), tx);

      if (debug) {
         log.debug("began tx: " + tx);
      }
   }

   /**
    *  Commit the transaction associated with the currently running thread.
    */
   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             SecurityException,
             IllegalStateException,
             SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null) {
         current.commit();
         ti.tx = null;
         if (debug) {
            log.debug("commited tx: " + current);
         }
      }
      else {
         throw new IllegalStateException("No transaction.");
      }
   }
 
   /**
    *  Return the status of the transaction associated with the currently
    *  running thread, or <code>Status.STATUS_NO_TRANSACTION</code> if no
    *  active transaction is currently associated.
    */
   public int getStatus() throws SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null) {
         if (current.isDone()) {
            ti.tx = null;
         }
         else {
            return current.getStatus();
         }
      }
      return Status.STATUS_NO_TRANSACTION;
   }

   /**
    *  Return the transaction currently associated with the invoking thread,
    *  or <code>null</code> if no active transaction is currently associated.
    */
   public Transaction getTransaction() throws SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null && current.isDone())
         current = ti.tx = null;

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
      if (transaction != null && !(transaction instanceof TransactionImpl)) {
         throw new RuntimeException("Not a TransactionImpl, but a " +
                                    transaction.getClass().getName());
      }

      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;
        
      if (current != null) {
         if (current.isDone()) {
            current = ti.tx = null;
         }
         else {
            throw new IllegalStateException("Already associated with a tx");
         }
      }

      if (current != transaction) {
         ti.tx = (TransactionImpl)transaction;
      }

      if (debug) {
         log.debug("resumed tx: " + ti.tx);
      }
   }

   /**
    *  Suspend the transaction currently associated with the current
    *  thread, and return it.
    *
    *  Note: This will not delist any resources involved in this
    *  transaction. According to JTA1.0.1 specification section 3.2.3,
    *  that is the responsibility of the application server.
    */
   public Transaction suspend() throws SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;
        
      if (current != null) {
         ti.tx = null;

         if (debug) {
            log.debug("suspended tx: " + current);
         }
         
         if (current.isDone()) {
            current = null;
         }
      }

      return current;
   }

   /**
    *  Roll back the transaction associated with the currently running thread.
    */
   public void rollback()
      throws IllegalStateException, SecurityException, SystemException
   { 
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null) {
         if (!current.isDone()) {
            current.rollback();

            if (debug) {
               log.debug("rolled back tx: " + current);
            }
            return;
         }
         ti.tx = null;
      }
      throw new IllegalStateException("No transaction.");
   }

   /**
    *  Mark the transaction associated with the currently running thread
    *  so that the only possible outcome is a rollback.
    */
   public void setRollbackOnly()
      throws IllegalStateException, SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null) {
         if (!current.isDone()) {
            current.setRollbackOnly();
            
            if (debug) {
               log.debug("tx marked for rollback only: " + current);
            }
            return;
         }
         ti.tx = null;
      }
      throw new IllegalStateException("No transaction.");
   }

   /**
    *  Set the transaction timeout for new transactions started by the
    *  calling thread.
    */
   public void setTransactionTimeout(int seconds)
      throws SystemException
   {
      getThreadInfo().timeout = 1000 * seconds;

      if (debug) {
         log.debug("tx timeout is now: " + seconds + "s");
      }
   }
    
   /**
    *  Set the default transaction timeout for new transactions.
    *  This default value is used if <code>setTransactionTimeout()</code>
    *  was never called, or if it was called with a value of <code>0</code>.
    */
   public void setDefaultTransactionTimeout(int seconds)
   {
      timeOut = 1000L * seconds;

      if (debug) {
         log.debug("default tx timeout is now: " + seconds + "s");
      }
   }
    
   /**
    *  Get the default transaction timeout.
    *
    *  @return Default transaction timeout in seconds.
    */
   public int getDefaultTransactionTimeout()
   {
      return (int)(timeOut / 1000);
   }

   /**
    *  The following 2 methods are here to provide association and
    *  disassociation of the thread.
    */
   public Transaction disassociateThread()
   {
      TransactionImpl current = getTxImpl();
        
      setTxImpl(null);
        
      return current;
   }
    
   public void associateThread(Transaction transaction)
   {
      if (transaction != null && !(transaction instanceof TransactionImpl)) {
         throw new RuntimeException("Not a TransactionImpl, but a " +
                                    transaction.getClass().getName());
      }

      // Associate with the thread
      setTxImpl((TransactionImpl)transaction);
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
      if (tpc instanceof GlobalId) {
         return (Transaction)globalIdTx.get((GlobalId)tpc);
      }
      else {
         log.warn("Cannot import transaction propagation context: " + tpc);
         return null;
      }
   }


   // Implements TransactionPropagationContextFactory ---------------

   /**
    *  Return a TPC for the current transaction.
    */
   public Object getTransactionPropagationContext()
   {
      return getTransactionPropagationContext(getTxImpl());
   }
 
   /**
    *  Return a TPC for the argument transaction.
    */
   public Object getTransactionPropagationContext(Transaction tx)
   {
      // If no transaction or unknown transaction class, return null.
      if (tx == null || !(tx instanceof TransactionImpl)) {
         return null;
      }

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
    *  This keeps track of the thread association with transactions
    *  and timeout values.
    *  In some cases terminated transactions may not be cleared here.
    */
   private ThreadLocal threadTx = new ThreadLocal();

   /**
    *  This map contains the active transactions as values.
    *  The keys are the <code>GlobalId</code>s of the transactions.
    */
   private Map globalIdTx = Collections.synchronizedMap(new HashMap());


   /**
    *  Return the ThreadInfo for the calling thread, and create if not
    *  found.
    */
   private ThreadInfo getThreadInfo()
   {
      ThreadInfo ret = (ThreadInfo)threadTx.get();

      if (ret == null) {
         ret = new ThreadInfo();
         ret.timeout = timeOut;
         threadTx.set(ret);
      }

      return ret;
   }

   /**
    *  Return the TransactionImpl associated with the calling thread.
    */
   private TransactionImpl getTxImpl()
   {
      return getThreadInfo().tx;
   }

   /**
    *  Set the TransactionImpl associated with the calling thread.
    */
   private void setTxImpl(TransactionImpl tx)
   {
      getThreadInfo().tx = tx;
   }


   // Inner classes -------------------------------------------------

   /**
    *  A simple aggregate of a thread-associated timeout value
    *  and a thread-associated transaction.
    */
   static class ThreadInfo
   {
      long timeout = 0;
      TransactionImpl tx = null;
   }
}
