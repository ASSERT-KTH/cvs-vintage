/*
 * JBoss, the OpenSource J2EE webOS
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
 *  A light weight transaction frontend to a TxCapsule.
 *
 *  @see TxCapsule
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.18 $
 */
class TransactionImpl
   implements Transaction
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   TransactionImpl(TxCapsule txCapsule, XidImpl xid)
   {
      this.txCapsule = txCapsule;
      this.xid = xid;
      globalId = new GlobalId(xid.hashCode(),
                              xid.getInternalGlobalTransactionId());
   }

   // Public --------------------------------------------------------

   // In the following methods we synchronize to avoid races with transaction
   // termination. The travelled flag is not checked, as we assume that the
   // transaction has already been imported.

   // When the transaction is done, instance variable txCapsule is set
   // to null, and this is used as an indicator that the methods here
   // should throw an exception.
   // To avoid too much optimization, txCapsule is declared volatile.

   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             java.lang.SecurityException,
             java.lang.IllegalStateException,
             SystemException
   {
      if( txCapsule == null )
         throw new IllegalStateException("No transaction.");
      txCapsule.commit();
   }

   public void rollback()
      throws java.lang.IllegalStateException,
             java.lang.SecurityException,
             SystemException
   {
      if( txCapsule == null )
         throw new IllegalStateException("No transaction.");
      txCapsule.rollback();
   }

   public boolean delistResource(XAResource xaRes, int flag)
      throws java.lang.IllegalStateException,
             SystemException
   {
      if( txCapsule == null )
         throw new IllegalStateException("No transaction.");
      return txCapsule.delistResource(xaRes, flag);
   }

   public boolean enlistResource(XAResource xaRes)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      if( txCapsule == null )
         throw new IllegalStateException("No transaction.");
      return txCapsule.enlistResource(xaRes);
   }

   public int getStatus()
      throws SystemException
   {
      int status = Status.STATUS_NO_TRANSACTION;
      if( txCapsule != null )
         status = txCapsule.getStatus();
      return status;
   }

   public void registerSynchronization(Synchronization s)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      if( txCapsule == null )
         throw new IllegalStateException("No transaction.");
      txCapsule.registerSynchronization(s);
   }

   public void setRollbackOnly()
      throws java.lang.IllegalStateException,
             SystemException
   {
      if( txCapsule == null )
         throw new IllegalStateException("No transaction.");
      txCapsule.setRollbackOnly();
   }

   public int hashCode()
   {
      return globalId.hashCode();
   }

   public String toString()
   {
      return "TransactionImpl:" + xid.toString();
   }

   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof TransactionImpl)
         return globalId.equals(((TransactionImpl)obj).globalId);
      return false;
   }

   // Package protected ---------------------------------------------

   /** The ID of this transaction. */
   XidImpl xid;

   /**
    *  Setter for property done.
    *  No argument for this mutator; we can only set to true.
    *  This will also clear the txCapsule reference.
    */
   synchronized void setDone()
   {
      txCapsule = null;
      TxManager.getInstance().releaseTransactionImpl(this);
   }

   /**
    *  Getter for property done.
    */
   boolean isDone()
   {
      return txCapsule == null;
   }

   /**
    *  Return the global id of this transaction.
    */
   GlobalId getGlobalId()
   {
      return globalId;
   }

   // Private -------------------------------------------------------

   /**
    *  The backend of this transaction.
    *  Null iff this transaction is done.
    */
   private volatile TxCapsule txCapsule;

   /**
    *  The global ID of this transaction.
    *  This is used as a transaction propagation context, and in the
    *  TxManager for mapping transaction IDs to transactions.
    */
   private GlobalId globalId;

   // Inner classes -------------------------------------------------
}
