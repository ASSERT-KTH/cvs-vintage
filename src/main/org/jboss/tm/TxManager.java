/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
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
 *  @version $Revision: 1.20 $
 */
public class TxManager
implements TransactionManager
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   /**
    *  Default timeout in milliseconds.
    */
   int timeOut = 60*1000; // Default timeout in milliseconds
    
   // Static --------------------------------------------------------
    
   // Constructors --------------------------------------------------
    
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
      // If the transaction has travelled, we have to import it.
      if (transaction != null && transaction instanceof TransactionImpl) {
         TransactionImpl tx = (TransactionImpl)transaction;

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
      }

      // Associate with the thread
      threadTx.set(transaction);
   }
    
    
   // Package protected ---------------------------------------------
    
   // There has got to be something better :)
   static TxManager getTransactionManager()
   {
      try {
         javax.naming.InitialContext context = new javax.naming.InitialContext();
            
         //One tx in naming
         Logger.log("Calling get manager from JNDI");
         TxManager manager = (TxManager) context.lookup("TransactionManager");
         Logger.log("Returning TM " + manager.hashCode());
            
         return manager;
      } catch (Exception e ) {
         return null;
      }
   }
    
   /**
    *  Release the given txCapsule for reuse.
    */
   void releaseTxCapsule(TxCapsule txCapsule)
   {
      activeCapsules.remove(txCapsule);

      SoftReference ref = new SoftReference(txCapsule);
      synchronized (inactiveCapsules) {
         inactiveCapsules.add(ref);
      }
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
