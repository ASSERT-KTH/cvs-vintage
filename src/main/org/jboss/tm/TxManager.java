/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.tm;

import java.lang.ref.SoftReference;

import java.util.Hashtable;
import java.util.LinkedList;
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
 *  @author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.26 $
 */
public class TxManager
implements TransactionManager
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
   static TxManager getInstance()
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

   public void begin()
      throws NotSupportedException,
             SystemException
   {
      Transaction current = (Transaction)threadTx.get();

      if (current != null &&
          (!(current instanceof TransactionImpl) ||
           !((TransactionImpl)current).isDone()))
         throw new NotSupportedException("Transaction already active, " +
                                         "cannot nest transactions.");

      TxCapsule txCapsule = null;
      synchronized (inactiveCapsules) {
         while (inactiveCapsules.size() > 0) {
            SoftReference ref = (SoftReference)inactiveCapsules.removeFirst();
            txCapsule = (TxCapsule)ref.get();
            if (txCapsule != null)
              break;
         }
      }
      if (txCapsule == null)
         txCapsule = new TxCapsule(this, timeOut);
      else
         txCapsule.reUse(timeOut);
      TransactionImpl tx = txCapsule.createTransactionImpl();
      threadTx.set(tx);
      activeCapsules.put(tx.xid, txCapsule);
   }

   /**
    *  Commit the transaction associated with the currently running thread.
    */
   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             java.lang.SecurityException,
             java.lang.IllegalStateException,
             SystemException
   {
      Transaction current = (Transaction)threadTx.get();

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
      Transaction current = (Transaction)threadTx.get();

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
      Transaction current = (Transaction)threadTx.get();

      if (current != null && current instanceof TransactionImpl &&
          ((TransactionImpl)current).isDone()) {
         threadTx.set(null);
         return null;
      }
      return current;
   }

   public void resume(Transaction tobj)
      throws InvalidTransactionException,
             java.lang.IllegalStateException,
             SystemException
   {
        //Useless
        
        //throw new Exception("txMan.resume() NYI");
   }

   public Transaction suspend()
      throws SystemException
   {
        //      Logger.log("suspend tx");
        
        // Useless
        
        return null;
        //throw new Exception("txMan.suspend() NYI");
   }

   /**
    *  Roll back the transaction associated with the currently running thread.
    */
   public void rollback()
      throws java.lang.IllegalStateException,
             java.lang.SecurityException,
             SystemException
   { 
      Transaction current = (Transaction)threadTx.get();

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
      throws java.lang.IllegalStateException,
             SystemException
   {
      Transaction current = (Transaction)threadTx.get();

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
      Transaction current = (Transaction)threadTx.get();
        
      threadTx.set(null);
        
      return current;
   }
    
   public void associateThread(Transaction transaction)
   {
      //
      // If the transaction has travelled, we have to import it.
      //
      // This implicit import will go away at some point in the
      // future and be replaced by an explicit import by calling
      // importTPC().
      // That will make it possible to only propagate XidImpl over the
      // wire so that we no longer have to handle multible Transaction
      // frontends for each transaction.
      //
      if (transaction != null && transaction instanceof TransactionImpl) {
         TransactionImpl tx = (TransactionImpl)transaction;

         if (tx.importNeeded())
            transaction = importTPC(transaction);
      }

      // Associate with the thread
      threadTx.set(transaction);
   }

   /**
    *  Import a transaction propagation context into this TM.
    *  The TPC is loosely typed, as we may (at a later time) want to
    *  import TPCs that come from other transaction monitors without
    *  offloading the conversion to the client.
    *
    *  @param tpc The transaction propagation context that we want to
    *             import into this TM. Currently this is an instance
    *             of TransactionImpl. Later this will be changed to an
    *             instance of XidImpl. And at some later time this may
    *             even be an instance of a transaction propagation context
    *             from another kind of transaction monitor like 
    *             org.omg.CosTransactions.PropagationContext. 
    *
    *  @return A transaction representing this transaction propagation
    *          context, or null if this TPC cannot be imported.
    */
   public Transaction importTPC(Object tpc)
   {
      if (tpc instanceof TransactionImpl) {
         // TODO: Change to just return the transaction without importing.
         // A raw TransactionImpl will only be used for optimized local calls,
         // where the import will not be needed.
         // But that will have to wait until remote calls use XidImpl instead,
         // otherwise we cannot distinguish cases.
         TransactionImpl tx = (TransactionImpl)tpc;

         if (tx.importNeeded()) {
            synchronized(tx) {
               // Recheck with synchronization.
               if (tx.importNeeded()) {
                  TxCapsule txCapsule = (TxCapsule)activeCapsules.get(tx.xid);
                  if (txCapsule != null)
                     txCapsule.importTransaction(tx);
                  else
                     Logger.warning("Cannot import transaction: " +
                                    tx.toString());
               }
            }
         }
         return tx;
      } else if (tpc instanceof XidImpl)
         Logger.warning("XidImpl import not yet implemented.");
      else
         Logger.warning("Cannot import transaction propagation context: " +
                        tpc.toString());
      return null;
   }

   // Package protected ---------------------------------------------
    
   /**
    *  Release the given txCapsule for reuse.
    */
   void releaseTxCapsule(TxCapsule txCapsule, XidImpl xid)
   {
      activeCapsules.remove(xid);

      SoftReference ref = new SoftReference(txCapsule);
      synchronized (inactiveCapsules) {
         inactiveCapsules.add(ref);
      }
   }

   /**
    *  Return an Xid that identifies the transaction associated
    *  with the invoking thread, or <code>null</code> if the invoking
    *  thread is not associated with a transaction.
    *  This is used for JRMP transaction context propagation.
    */
   Xid getXid()
   {
      Transaction current = (Transaction)threadTx.get();

      // If no transaction or unknown transaction class, return null.
      if (current == null || !(current instanceof TransactionImpl))
         return null;

      return ((TransactionImpl)current).xid;
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
 
   /**
    *  This keeps track of the transaction association with threads.
    *  In some cases terminated transactions may not be cleared here.
    */
   private ThreadLocal threadTx = new ThreadLocal();

   /**
    *  This map contains the active txCapsules as values.
    *  The keys are the <code>Xid</code> of the txCapsules.
    */
   private Map activeCapsules = Collections.synchronizedMap(new HashMap());

   /**
    *  This collection contains the inactive txCapsules.
    *  We keep these for reuse.
    */
   private LinkedList inactiveCapsules = new LinkedList();

   // Inner classes -------------------------------------------------
}
