/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;

/**
 *  A light weight transaction.
 *
 *  It is the public face of the TxCapsule.
 *  Many of these "transactions" can coexist representing the TxCap.
 *  Access to the underlying txCap is done through the TransactionManager.
 *
 *  @see TxCapsule
 *  @author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.12 $
 */
public class TransactionImpl
   implements Transaction, Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   Xid xid; // Transaction ID.

   // Constructors --------------------------------------------------

   TransactionImpl(TxCapsule txCapsule, Xid xid)
   {
      this.txCapsule = txCapsule;
      this.xid = xid;
      travelled = false;
   }

   // Public --------------------------------------------------------

   // In the following methods we synchronize to avoid races with transaction
   // termination. The travelled flag is not checked, as we assume that the
   // transaction has already been imported.

   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             java.lang.SecurityException,
             java.lang.IllegalStateException,
             SystemException
   {
      synchronized (this) {
        if (done)
           throw new IllegalStateException("No transaction.");

        txCapsule.commit();
      }
   }

   public void rollback()
      throws java.lang.IllegalStateException,
             java.lang.SecurityException,
             SystemException
   {
      synchronized (this) {
         if (done)
            throw new IllegalStateException("No transaction.");

         txCapsule.rollback();
      }
   }

   public boolean delistResource(XAResource xaRes, int flag)
      throws java.lang.IllegalStateException,
             SystemException
   {
      synchronized (this) {
         if (done)
            throw new IllegalStateException("No transaction.");

         return txCapsule.delistResource(xaRes, flag);
      }
   }

   public boolean enlistResource(XAResource xaRes)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      synchronized (this) {
         if (done)
            throw new IllegalStateException("No transaction.");

         return txCapsule.enlistResource(xaRes);
      }
   }

   public int getStatus()
      throws SystemException
   {
      synchronized (this) {
         if (done)
            return Status.STATUS_NO_TRANSACTION;

         return txCapsule.getStatus();
      }
   }

   public void registerSynchronization(Synchronization s)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      synchronized (this) {
         if (done)
            throw new IllegalStateException("No transaction.");

         txCapsule.registerSynchronization(s);
      }
   }

   public void setRollbackOnly()
      throws java.lang.IllegalStateException,
             SystemException
   {
      synchronized (this) {
         if (done)
            throw new IllegalStateException("No transaction.");

         txCapsule.setRollbackOnly();
      }
   }

   public int hashCode()
   {
      return xid.hashCode();
   }

   public String toString()
   {
      return "TransactionImpl:" + xid.toString();
   }

   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof TransactionImpl)
         return xid.equals(((TransactionImpl)obj).xid);
      return false;
   }

   // Package protected ---------------------------------------------

   /**
    *  Setter for property txCapsule.
    *
    *  This is needed when a propagated transaction is imported into the
    *  current transaction manager.
    */
   synchronized void setTxCapsule(TxCapsule txCapsule)
   {
      if (done)
         // Shouldn't happen.
         throw new IllegalStateException("Transaction " + toString() +
                                         " is done.");
      this.txCapsule = txCapsule;
      travelled = false;
   }

   /**
    *  Setter for property done.
    *  No argument for this mutator; we can only set to true.
    *  This will also clear the txCapsule reference.
    */
   synchronized void setDone()
   {
      done = true;
      txCapsule = null;
   }

   /**
    *  Getter for property done.
    */
   boolean isDone()
   {
      return done;
   }

   /**
    *  Returns true iff this transaction needs to be imported into the
    *  local transaction manager.
    */
   boolean importNeeded()
   {
      return !done && travelled;
   }

   // Private -------------------------------------------------------

   private transient TxCapsule txCapsule; // The real implementation.
   private boolean done; // Flags that the transaction has terminated.
   transient boolean travelled; // Flags that the transaction has travelled.

   private void writeObject(java.io.ObjectOutputStream stream)
      throws java.io.IOException
   {
      stream.defaultWriteObject();
   }

   private void readObject(java.io.ObjectInputStream stream)
      throws java.io.IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      travelled = true;
   }

   // Inner classes -------------------------------------------------
}
