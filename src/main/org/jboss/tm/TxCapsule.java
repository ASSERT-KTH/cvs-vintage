/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ConcurrentModificationException;

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

import org.jboss.logging.Logger;
import org.jboss.util.timeout.Timeout;
import org.jboss.util.timeout.TimeoutTarget;
import org.jboss.util.timeout.TimeoutFactory;

/**
 *  TxCapsule holds all the information relevant to a transaction.
 *  Callbacks and synchronizations are held here.
 *
 *  TODO: Implement persistent storage and recovery.
 *
 *  @see TxManager
 *  @see TransactionImpl
 *
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *
 *  @version $Revision: 1.15 $
 */
class TxCapsule implements TimeoutTarget
{
   // Constants -----------------------------------------------------

   // Trace enabled flag
   static private final boolean trace = true;

   // Code meaning "no heuristics seen", must not be XAException.XA_HEURxxx
   static private final int HEUR_NONE     = XAException.XA_RETRY;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    *  Create a new TxCapsule.
    *
    *  @param tm The transaction manager for this transaction.
    *  @param timeout The timeout for this transaction in milliseconds
    *                 (timeouts are not yet implemented).
    */
   TxCapsule(TxManager tm, int timeout)
   {
      this(tm);

      status = Status.STATUS_ACTIVE;

      start = System.currentTimeMillis();
      this.timeout = TimeoutFactory.createTimeout(start+timeout, this);
   }

   /**
    *  Create a new TxCapsule.
    *
    *  @param tm The transaction manager for this transaction.
    */
   private TxCapsule(TxManager tm)
   {
      xid = new XidImpl();
      this.tm = tm;

      if (trace)
         Logger.debug("TxCapsule: Created new instance for tx=" + toString());
   }

   /**
    *  Create a new front for this transaction.
    */
   TransactionImpl createTransactionImpl()
   {
      TransactionImpl tx = new TransactionImpl(this, xid);
      addTransaction(tx);

      return tx;
   }

   /**
    *  Prepare this instance for reuse.
    */
   void reUse(int timeout)
   {
      if (!done)
        throw new IllegalStateException();

      done = false;
      resourcesEnded = false;

      xid = new XidImpl();

      status = Status.STATUS_ACTIVE;
      heuristicCode = HEUR_NONE;

      start = System.currentTimeMillis();
      this.timeout = TimeoutFactory.createTimeout(start+timeout, this);

      if (trace)
         Logger.debug("TxCapsule: Reused instance for tx=" + toString());
   }

   // Public --------------------------------------------------------

   /**
    *  Called when our timeout expires.
    */
   public void timedOut(Timeout timeout)
   {
      try {
         lock();

         Logger.warning("Transaction " + toString() + " timed out." +
                        " status=" + getStringStatus(status));

         if (this.timeout == null)
            return; // Don't race with timeout cancellation.
         this.timeout = null;

         switch (status) {
         case Status.STATUS_ROLLEDBACK:
         case Status.STATUS_COMMITTED:
         case Status.STATUS_NO_TRANSACTION:
            return; // Transaction done.

         case Status.STATUS_ROLLING_BACK:
            return; // Will be done shortly.

         case Status.STATUS_COMMITTING:
            // This is _very_ bad:
            // We are in the second commit phase, and have decided
            // to commit, but now we get a timeout and should rollback.
            // So we end up with a mixed decision.
            gotHeuristic(null, XAException.XA_HEURMIX);
            status = Status.STATUS_MARKED_ROLLBACK;
            return; // commit will fail

         case Status.STATUS_PREPARED:
            // This is bad:
            // We are done with the first phase, and are persistifying
            // our decision. Fortunately this case is currently never
            // hit, as we do not release the lock between the two phases.
         case Status.STATUS_ACTIVE:
            status = Status.STATUS_MARKED_ROLLBACK;
            // fall through..
         case Status.STATUS_MARKED_ROLLBACK:
            endResources();
            rollbackResources();
            doAfterCompletion();
            gotHeuristic(null, XAException.XA_HEURRB);
            instanceDone();
            return;

         case Status.STATUS_PREPARING:
            status = Status.STATUS_MARKED_ROLLBACK;
            return; // commit will fail

         default:
            Logger.warning("TxCapsule: Unknown status at timeout, tx=" +
                           toString());
            return;
         }
      } finally {
        unlock();
      }
   }

   public String toString()
   {
      return xid.toString();
   }

   // Package protected ---------------------------------------------

   XidImpl getXid()
   {
	  return xid;
   }

   /**
    *  Import a transaction encapsulated here.
    */
   void importTransaction(TransactionImpl tx) {
      try {
         lock();

         tx.setTxCapsule(this);
         addTransaction(tx);
      } finally {
        unlock();
      }
   }

   /**
    *  Commit the transaction encapsulated here.
    *  Should not be called directly, use <code>TxManager.commit()</code>
    *  instead.
    */
   void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             java.lang.SecurityException,
             java.lang.IllegalStateException,
             SystemException
   {
      try {
         lock();

         if (trace)
            Logger.debug("TxCapsule.commit(): Entered, tx=" + toString() +
                         " status=" + getStringStatus(status));

         switch (status) {
         case Status.STATUS_PREPARING:
            throw new IllegalStateException("Already started preparing.");
         case Status.STATUS_PREPARED:
            throw new IllegalStateException("Already prepared.");
         case Status.STATUS_ROLLING_BACK:
            throw new IllegalStateException("Already started rolling back.");
         case Status.STATUS_ROLLEDBACK:
            instanceDone();
            checkHeuristics();
            throw new IllegalStateException("Already rolled back.");
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing.");
         case Status.STATUS_COMMITTED:
            instanceDone();
            checkHeuristics();
            throw new IllegalStateException("Already committed.");
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction.");
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state");
         case Status.STATUS_MARKED_ROLLBACK:
            doBeforeCompletion();
            endResources();
            rollbackResources();
            doAfterCompletion();
            cancelTimeout();
            instanceDone();
            checkHeuristics();
            throw new RollbackException("Already marked for rollback");
         case Status.STATUS_ACTIVE:
            break;
         default:
            throw new IllegalStateException("Illegal status: " + status);
         }

         doBeforeCompletion();

         if (trace)
            Logger.debug("TxCapsule.commit(): Before completion done, " +
                         "tx=" + toString() +
                         " status=" + getStringStatus(status));

         endResources();

         if (status == Status.STATUS_ACTIVE) {
            if (resourceCount == 0) {
               // Zero phase commit is really fast ;-)
               if (trace)
                  Logger.debug("TxCapsule.commit(): No resources.");
               status = Status.STATUS_COMMITTED;
            } else if (resourceCount == 1) {
               // One phase commit
               if (trace)
                  Logger.debug("TxCapsule.commit(): One resource.");
               commitResources(true);
            } else {
               // Two phase commit
               if (trace)
                  Logger.debug("TxCapsule.commit(): Many resources.");

               if (!prepareResources()) {
                  boolean commitDecision =
                          status == Status.STATUS_PREPARED &&
                          (heuristicCode == HEUR_NONE ||
                           heuristicCode == XAException.XA_HEURCOM);

                  // TODO: Save decision to stable storage for recovery
                  // after system crash.

                  if (commitDecision)
                     commitResources(false);
               } else
                 status = Status.STATUS_COMMITTED; // all was read-only
            }
         }

         if (status != Status.STATUS_COMMITTED) {
            rollbackResources();
            doAfterCompletion();
            cancelTimeout();
            instanceDone();
            throw new RollbackException("Unable to commit, tx=" + toString() +
                                        " status=" + getStringStatus(status));
         }

         cancelTimeout();
         doAfterCompletion();
         instanceDone();
         checkHeuristics();

         if (trace)
            Logger.debug("TxCapsule.commit(): Transaction " + toString() +
                         " committed OK.");

      } finally {
        unlock();
      }
   }

   /**
    *  Rollback the transaction encapsulated here.
    *  Should not be called directly, use <code>TxManager.rollback()</code>
    *  instead.
    */
   void rollback()
      throws java.lang.IllegalStateException,
             java.lang.SecurityException,
             SystemException
   {
      try {
         lock();

         if (trace)
            Logger.debug("TxCapsule.rollback(): Entered, tx=" + toString() +
                         " status=" + getStringStatus(status));

         switch (status) {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_MARKED_ROLLBACK:
            doBeforeCompletion();
            endResources();
            rollbackResources();
            cancelTimeout();
            doAfterCompletion();
            instanceDone();
            // Cannot throw heuristic exception, so we just have to
            // clear the heuristics without reporting.
            heuristicCode = HEUR_NONE;
            return;
         case Status.STATUS_PREPARING:
            // Set status to avoid race with prepareResources().
            status = Status.STATUS_MARKED_ROLLBACK;
            return; // commit() will do rollback.
         default:
            throw new IllegalStateException("Cannot rollback(), " +
                                            "tx=" + toString() +
                                            " status=" +
                                            getStringStatus(status));
         }
      } finally {
         unlock();
      }
   }

   /**
    *  Mark the transaction encapsulated here so that the only possible
    *  outcome is a rollback.
    *  Should not be called directly, use <code>TxManager.rollback()</code>
    *  instead.
    */
   void setRollbackOnly()
      throws java.lang.IllegalStateException,
             SystemException
   {
      try {
         lock();

         if (trace)
            Logger.debug("TxCapsule.setRollbackOnly(): Entered, tx=" +
                         toString() + " status=" + getStringStatus(status));

         switch (status) {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_PREPARING:
         case Status.STATUS_PREPARED:
            status = Status.STATUS_MARKED_ROLLBACK;
            // fall through..
         case Status.STATUS_MARKED_ROLLBACK:
         case Status.STATUS_ROLLING_BACK:
            return;
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing.");
         case Status.STATUS_COMMITTED:
            throw new IllegalStateException("Already committed.");
         case Status.STATUS_ROLLEDBACK:
            throw new IllegalStateException("Already rolled back.");
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction.");
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state");
         default:
            throw new IllegalStateException("Illegal status: " + status);
         }
      } finally {
         unlock();
      }
   }

   /**
    *  Delist a resource from the transaction encapsulated here.
    *
    *  @param xaRes The resource to delist.
    *  @param flag One of <code>XAResource.TMSUCCESS</code>,
    *             <code>XAResource.TMSUSPEND</code>
    *             or <code>XAResource.TMFAIL</code>.
    *
    *  @returns True iff the resource was successfully delisted.
    */
   boolean delistResource(XAResource xaRes, int flag)
      throws java.lang.IllegalStateException,
             SystemException
   {
      if (xaRes == null)
         throw new IllegalArgumentException("null xaRes");
      if (flag != XAResource.TMSUCCESS &&
          flag != XAResource.TMSUSPEND &&
          flag != XAResource.TMFAIL)
         throw new IllegalArgumentException("Bad flag: " + flag);

      try {
         lock();

         if (trace)
            Logger.debug("TxCapsule.delistResource(): Entered, tx=" +
                         toString() + " status=" + getStringStatus(status));

         int idx = findResource(xaRes);

         if (idx == -1)
           throw new IllegalArgumentException("xaRes not enlisted");

         switch (status) {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_MARKED_ROLLBACK:
            break;
         case Status.STATUS_PREPARING:
            throw new IllegalStateException("Already started preparing.");
         case Status.STATUS_ROLLING_BACK:
            throw new IllegalStateException("Already started rolling back.");
         case Status.STATUS_PREPARED:
            throw new IllegalStateException("Already prepared.");
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing.");
         case Status.STATUS_COMMITTED:
            throw new IllegalStateException("Already committed.");
         case Status.STATUS_ROLLEDBACK:
            throw new IllegalStateException("Already rolled back.");
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction.");
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state");
         default:
            throw new IllegalStateException("Illegal status: " + status);
         }

         try {
            endResource(xaRes, flag);
            if (flag == XAResource.TMSUSPEND)
               resourceState[idx] = RS_SUSPENDED;
            else {
               if (flag == XAResource.TMFAIL)
                  status = Status.STATUS_MARKED_ROLLBACK;
               resourceState[idx] = RS_ENDED;
            }
            return true;
         } catch(XAException e) {
            Logger.warning("XAException: tx=" + toString() + " errorCode=" +
                           getStringXAErrorCode(e.errorCode));
            Logger.exception(e);
            status = Status.STATUS_MARKED_ROLLBACK;
            return false;
         }
      } finally {
         unlock();
      }
   }

   /**
    *  Enlist a resource with the transaction encapsulated here.
    *
    *  @param xaRes The resource to enlist.
    *
    *  @returns True iff the resource was successfully enlisted.
    */
   boolean enlistResource(XAResource xaRes)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      if (xaRes == null)
         throw new IllegalArgumentException("null xaRes");

      try {
         lock();

         if (trace)
            Logger.debug("TxCapsule.enlistResource(): Entered, tx=" +
                         toString() + " status=" + getStringStatus(status));

         switch (status) {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_PREPARING:
            break;
         case Status.STATUS_PREPARED:
            throw new IllegalStateException("Already prepared.");
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing.");
         case Status.STATUS_COMMITTED:
            throw new IllegalStateException("Already committed.");
         case Status.STATUS_MARKED_ROLLBACK:
            throw new RollbackException("Already marked for rollback");
         case Status.STATUS_ROLLING_BACK:
            throw new RollbackException("Already started rolling back.");
         case Status.STATUS_ROLLEDBACK:
            throw new RollbackException("Already rolled back.");
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction.");
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state");
         default:
            throw new IllegalStateException("Illegal status: " + status);
         }

         if (resourcesEnded)
            throw new IllegalStateException("Too late to enlist resources");

         // Add resource
         try {
            int idx = findResource(xaRes);

            if (idx != -1) {
               if (resourceState[idx] == RS_SUSPENDED) {
                  startResource(xaRes, XAResource.TMRESUME);
                  resourceState[idx] = RS_ENLISTED;
                  return true;
               } else if (resourceState[idx] == RS_ENDED) {
                  startResource(xaRes, XAResource.TMJOIN);
                  resourceState[idx] = RS_ENLISTED;
                  return true;
               } else
                  return false; // already enlisted
            }
            for (int i = 0; i < resourceCount; ++i) {
               if (xaRes.isSameRM(resources[i])) {
                  startResource(xaRes, XAResource.TMJOIN);
                  addResource(xaRes);
                  return true;
               }
            }
            // New resource
            // According to the JTA spec we should create a new
            // transaction branch here.
            startResource(xaRes, XAResource.TMNOFLAGS);
            addResource(xaRes);
            return true;
         } catch(XAException e) {
            Logger.warning("XAException: tx=" + toString() + " errorCode=" +
                           getStringXAErrorCode(e.errorCode));
            Logger.exception(e);
            return false;
         }
      } finally {
         unlock();
      }
   }

   /**
    *  Return the status of the transaction encapsulated here.
    */
   int getStatus()
      throws SystemException
   {
      return status;
   }

   /**
    *  Register a new transaction synchronization for the transaction
    *  encapsulated here.
    */
   void registerSynchronization(Synchronization s)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      if (s == null)
         throw new IllegalArgumentException("Null synchronization");

      try {
         lock();

         if (trace)
            Logger.debug("TxCapsule.registerSynchronization(): Entered, " +
                         "tx=" + toString() +
                         " status=" + getStringStatus(status));

         switch (status) {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_PREPARING:
            break;
         case Status.STATUS_PREPARED:
            throw new IllegalStateException("Already prepared.");
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing.");
         case Status.STATUS_COMMITTED:
            throw new IllegalStateException("Already committed.");
         case Status.STATUS_MARKED_ROLLBACK:
            //throw new RollbackException("Already marked for rollback");
            // Workaround for EntitySynchronizationInterceptor bug.
            Logger.warning("TxCapsule: Violating JTA by adding synchronization to a transaction marked for rollback.");
            break;
         case Status.STATUS_ROLLING_BACK:
            throw new RollbackException("Already started rolling back.");
         case Status.STATUS_ROLLEDBACK:
            throw new RollbackException("Already rolled back.");
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction.");
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state");
         default:
            throw new IllegalStateException("Illegal status: " + status);
         }

         if (syncCount == syncAllocSize) {
            // expand table
            syncAllocSize = 2 * syncAllocSize;

            Synchronization[] sy = new Synchronization[syncAllocSize];
            System.arraycopy(sync, 0, sy, 0, syncCount);
            sync = sy;
         }
         sync[syncCount++] = s;
      } finally {
         unlock();
      }
   }

   // Protected -----------------------------------------------------


   // Private -------------------------------------------------------

   /**
    *  The public faces of this capsule are JTA Transaction implementations.
    */
   private TransactionImpl[] transactions = new TransactionImpl[1];

   /**
    *  Size of allocated transaction frontend array.
    */
   private int transactionAllocSize = 1;

   /**
    *  Count of transaction frontends for this transaction.
    */
   private int transactionCount = 0;


   /**
    *  The synchronizations to call back.
    */
   private Synchronization[] sync = new Synchronization[1];

   /**
    *  Size of allocated synchronization array.
    */
   private int syncAllocSize = 1;

   /**
    *  Count of ynchronizations for this transaction.
    */
   private int syncCount = 0;


   /**
    *  A list of the XARessources that have participated in this transaction.
    */
   private XAResource[] resources = new XAResource[1];

   /**
    *  The state of the resources.
    */
   private int[] resourceState = new int[1];

   private final static int RS_ENLISTED      = 1; // enlisted
   private final static int RS_SUSPENDED     = 2; // suspended
   private final static int RS_ENDED         = 3; // not associated
   private final static int RS_VOTE_READONLY = 4; // voted read-only
   private final static int RS_VOTE_OK       = 5; // voted ok

   /**
    *  Size of allocated resource arrays.
    */
   private int resourceAllocSize = 1;

   /**
    *  Count of resources that have participated in this transaction.
    */
   private int resourceCount = 0;


   /**
    *  Flags that it is too late to enlist new resources.
    */
   private boolean resourcesEnded = false;

   /**
    *  The ID of this transaction.
    */
   private XidImpl xid; // Transaction id

   /**
    *  Status of this transaction.
    */
   private int status;

   /**
    *  The heuristics status of this transaction.
    */
   private int heuristicCode = HEUR_NONE;

   /**
    *  The time when this transaction was started.
    */
   private long start;

   /**
    *  The timeout handle for this transaction.
    */
   private Timeout timeout;

   /**
    *  The incarnation count of this transaction. This is incremented
    *  whenever this instance is done so that nobody is waiting for the
    *  lock across incarnations.
    */
   private long incarnationCount = 1;

   /**
    *  The transaction manager for this transaction.
    */
   private TxManager tm;

   /**
    *  Mutex for thread-safety. This should only be changed in the
    *  <code>lock()</code> and <code>unlock()</code> methods.
    */
   private boolean locked = false;

   /**
    *  Flags that we are done with this transaction and that it can be reused.
    */
   private boolean done = false;


   /**
    *  Return a string representation of the given status code.
    */
   private String getStringStatus(int status) {
      switch (status) {
         case Status.STATUS_PREPARING:
            return "STATUS_PREPARING";
         case Status.STATUS_PREPARED:
            return "STATUS_PREPARED";
         case Status.STATUS_ROLLING_BACK:
            return "STATUS_ROLLING_BACK";
         case Status.STATUS_ROLLEDBACK:
            return "STATUS_ROLLEDBACK";
         case Status.STATUS_COMMITTING:
            return "STATUS_COMMITING";
         case Status.STATUS_COMMITTED:
            return "STATUS_COMMITED";
         case Status.STATUS_NO_TRANSACTION:
            return "STATUS_NO_TRANSACTION";
         case Status.STATUS_UNKNOWN:
            return "STATUS_UNKNOWN";
         case Status.STATUS_MARKED_ROLLBACK:
            return "STATUS_MARKED_ROLLBACK";
         case Status.STATUS_ACTIVE:
            return "STATUS_ACTIVE";
 
         default:
            return "STATUS_UNKNOWN(" + status + ")";
      }
   }

   /**
    *  Return a string representation of the given XA error code.
    */
   private String getStringXAErrorCode(int errorCode) {
      switch (errorCode) {
         case XAException.XA_HEURCOM:
            return "XA_HEURCOM";
         case XAException.XA_HEURHAZ:
            return "XA_HEURHAZ";
         case XAException.XA_HEURMIX:
            return "XA_HEURMIX";
         case XAException.XA_HEURRB:
            return "XA_HEURRB";

         case XAException.XA_NOMIGRATE:
            return "XA_NOMIGRATE";

         case XAException.XA_RBCOMMFAIL:
            return "XA_RBCOMMFAIL";
         case XAException.XA_RBDEADLOCK:
            return "XA_RBDEADLOCK";
         case XAException.XA_RBINTEGRITY:
            return "XA_RBINTEGRITY";
         case XAException.XA_RBOTHER:
            return "XA_RBOTHER";
         case XAException.XA_RBPROTO:
            return "XA_RBPROTO";
         case XAException.XA_RBROLLBACK:
            return "XA_RBROLLBACK";
         case XAException.XA_RBTIMEOUT:
            return "XA_RBTIMEOUT";
         case XAException.XA_RBTRANSIENT:
            return "XA_RBTRANSIENT";

         case XAException.XA_RDONLY:
            return "XA_RDONLY";
         case XAException.XA_RETRY:
            return "XA_RETRY";

         case XAException.XAER_ASYNC:
            return "XAER_ASYNC";
         case XAException.XAER_DUPID:
            return "XAER_DUPID";
         case XAException.XAER_INVAL:
            return "XAER_INVAL";
         case XAException.XAER_NOTA:
            return "XAER_NOTA";
         case XAException.XAER_OUTSIDE:
            return "XAER_OUTSIDE";
         case XAException.XAER_PROTO:
            return "XAER_PROTO";
         case XAException.XAER_RMERR:
            return "XAER_RMERR";
         case XAException.XAER_RMFAIL:
            return "XAER_RMFAIL";
 
         default:
            return "XA_UNKNOWN(" + errorCode + ")";
      }
   }

   /**
    *  Lock this instance.
    */
   private synchronized void lock()
   {
      if (done)
         throw new IllegalStateException("Transaction has terminated");

      if (locked) {
         Logger.warning("TxCapsule: Lock contention, tx=" + toString());
         Thread.currentThread().dumpStack();

         long myIncarnation = incarnationCount;

         while (locked) {
            try {
               wait();
            } catch (InterruptedException ex) {}

            // MF FIXME: don't we need a notify() in this case?
            // we need to release all the thread waiting on this lock 

            // OSH: notifyAll() is done in instanceDone()
            // and notify() is done in unlock().

            if (done || myIncarnation != incarnationCount)
              throw new IllegalStateException("Transaction has now terminated");
         }
      }

      locked = true;
   }

   /**
    *  Unlock this instance.
    */
   private synchronized void unlock()
   {
      if (!locked) {
         Logger.warning("TxCapsule: Unlocking, but not locked, tx=" +
                        toString());
         Logger.exception(new Exception("[Stack trace]"));
      }

      locked = false;

      notify();
   }

   /**
    *  Cancel the timeout.
    *  This will release the lock while calling out.
    */
   private void cancelTimeout()
   {
      if (timeout != null) {
         unlock();
         try {
            timeout.cancel();
         } catch (Exception e)
            {
                Logger.debug(e);
            } finally {
            lock();
         }
         timeout = null;
      }
   }

   /**
    *  Return index of XAResource, or <code>-1</code> if not found.
    */
   private int findResource(XAResource xaRes)
   {
      for (int i = 0; i < resourceCount; ++i)
         if (resources[i] == xaRes)
            return i;

      return -1;
   }

   /**
    *  Add a resource, expanding tables if needed.
    */
   private void addResource(XAResource xaRes)
   {
      if (resourceCount == resourceAllocSize) {
         // expand tables
         resourceAllocSize = 2 * resourceAllocSize;

         XAResource[] res = new XAResource[resourceAllocSize];
         System.arraycopy(resources, 0, res, 0, resourceCount);
         resources = res;

         int[] stat = new int[resourceAllocSize];
         System.arraycopy(resourceState, 0, stat, 0, resourceCount);
         resourceState = stat;
      }
      resources[resourceCount] = xaRes;
      resourceState[resourceCount] = RS_ENLISTED;
      ++resourceCount;
   }

   /**
    *  Add a transaction frontend, expanding the table if needed.
    */
   private void addTransaction(TransactionImpl tx)
   {
      if (transactionCount == transactionAllocSize) {
         // expand table
         transactionAllocSize = 2 * transactionAllocSize;

         TransactionImpl[] tr = new TransactionImpl[transactionAllocSize];
         System.arraycopy(transactions, 0, tr, 0, transactionCount);
         transactions = tr;
      }
      transactions[transactionCount++] = tx;
   }

   /**
    *  Call <code>start()</code> on the XAResource.
    *  This will release the lock while calling out.
    */
   private void startResource(XAResource xaRes, int flags)
      throws XAException
   {
        Logger.debug("TxCapsule.startResource(" + xid.toString() +
                   ") entered: " + xaRes.toString() +
                   " flags=" + flags);
      unlock();
      // OSH FIXME: resourceState could be incorrect during this callout.
      try {
         xaRes.start(xid, flags);
      } finally {
         lock();
        Logger.debug("TxCapsule.startResource(" + xid.toString() +
                   ") leaving: " + xaRes.toString() +
                   " flags=" + flags);
      }
   }

   /**
    *  Call <code>end()</code> on the XAResource.
    *  This will release the lock while calling out.
    */
   private void endResource(XAResource xaRes, int flag)
      throws XAException
   {
      Logger.debug("TxCapsule.endResource(" + xid.toString() +
                   ") entered: " + xaRes.toString() +
                   " flag=" + flag);
      unlock();
      // OSH FIXME: resourceState could be incorrect during this callout.
      try {
         xaRes.end(xid, flag);
      } finally {
         lock();
         Logger.debug("TxCapsule.endResource(" + xid.toString() +
                      ") leaving: " + xaRes.toString() +
                      " flag=" + flag);
      }
   }

   /**
    *  End Tx association for all resources.
    */
   private void endResources()
   {
      for (int i = 0; i < resourceCount; i++) {
         try {
            if (resourceState[i] == RS_SUSPENDED) {
               // This is mad, but JTA 1.0.1 spec says on page 41:
               // "If TMSUSPEND is specified in flags, the transaction
               // branch is temporarily suspended in incomplete state.
               // The transaction context is in suspened state and must
               // be resumed via start with TMRESUME specified."
               // Note the _must_ above: It does not say _may_.
               // The above citation also seem to contradict the XA resource
               // state table on pages 17-18 where it is legal to do both
               // end(TMSUCCESS) and end(TMFAIL) when the resource is in
               // a suspended state.
               // But the Minerva XA pool does not like that we call end()
               // two times in a row, so we resume before ending.
               startResource(resources[i], XAResource.TMRESUME);
               resourceState[i] = RS_ENLISTED;
            }
            if (resourceState[i] == RS_ENLISTED) {
              Logger.debug("endresources("+i+"): state="+resourceState[i]);
              endResource(resources[i], XAResource.TMSUCCESS);
              resourceState[i] = RS_ENDED;
            }
         } catch(XAException e) {
            Logger.warning("XAException: tx=" + toString() + " errorCode=" +
                           getStringXAErrorCode(e.errorCode));
            Logger.exception(e);
            status = Status.STATUS_MARKED_ROLLBACK;
         }
      }
      resourcesEnded = true; // Too late to enlist new resources.
   }


   /**
    *  Call synchronization <code>beforeCompletion()</code>.
    *  This will release the lock while calling out.
    */
   private void doBeforeCompletion()
   {
      unlock();
      try {
         for (int i = 0; i < syncCount; i++)
            sync[i].beforeCompletion();
      } finally {
         lock();
      }
   }

   /**
    *  Call synchronization <code>afterCompletion()</code>.
    *  This will release the lock while calling out.
    */
   private void doAfterCompletion()
   {
      // Assert: Status indicates: Too late to add new synchronizations.
      unlock();
      try {
         for (int i = 0; i < syncCount; i++)
            sync[i].afterCompletion(status);
      } finally {
         lock();
      }
   }

   /**
    *  We got another heuristic.
    *  Promote <code>heuristicCode</code> if needed and tell
    *  the resource to forget the heuristic.
    *  This will release the lock while calling out.
    */
   private void gotHeuristic(XAResource resource, int code)
   {
      switch (code) {
      case XAException.XA_HEURMIX:
         heuristicCode = XAException.XA_HEURMIX;
         break;
      case XAException.XA_HEURRB:
         if (heuristicCode == HEUR_NONE)
            heuristicCode = XAException.XA_HEURRB;
         else if (heuristicCode == XAException.XA_HEURCOM ||
                  heuristicCode == XAException.XA_HEURHAZ)
            heuristicCode = XAException.XA_HEURMIX;
         break;
      case XAException.XA_HEURCOM:
         if (heuristicCode == HEUR_NONE)
            heuristicCode = XAException.XA_HEURCOM;
         else if (heuristicCode == XAException.XA_HEURRB ||
                  heuristicCode == XAException.XA_HEURHAZ)
            heuristicCode = XAException.XA_HEURMIX;
         break;
      case XAException.XA_HEURHAZ:
         if (heuristicCode == HEUR_NONE)
            heuristicCode = XAException.XA_HEURHAZ;
         else if (heuristicCode == XAException.XA_HEURCOM ||
                  heuristicCode == XAException.XA_HEURRB)
            heuristicCode = XAException.XA_HEURMIX;
         break;
      default:
         throw new IllegalArgumentException();
      }

      if (resource != null) {
         try {
            unlock();
            resource.forget(xid);
         } catch (XAException e) {
            Logger.warning("XAException at forget(): errorCode=" +
                           getStringXAErrorCode(e.errorCode));
            Logger.exception(e);
         } finally {
            lock();
         }
      }
   }

   /**
    *  Check for heuristics, clear and throw exception if any found.
    */
   private void checkHeuristics()
     throws HeuristicMixedException,
            HeuristicRollbackException
   {
      switch (heuristicCode) {
      case XAException.XA_HEURHAZ:
      case XAException.XA_HEURMIX:
         heuristicCode = HEUR_NONE;
         if (trace)
            Logger.debug("TxCapsule: Throwing HeuristicMixedException, " +
                         "status=" + getStringStatus(status));
         throw new HeuristicMixedException();
      case XAException.XA_HEURRB:
         heuristicCode = HEUR_NONE;
         if (trace)
            Logger.debug("TxCapsule: Throwing HeuristicRollbackException, " +
                         "status=" + getStringStatus(status));
         throw new HeuristicRollbackException();
      case XAException.XA_HEURCOM:
         heuristicCode = HEUR_NONE;
         // Why isn't HeuristicCommitException used in JTA ?
         // And why define something that is not used ?
         // For now we just have to ignore this failure, even if it happened
         // on rollback.
         if (trace)
            Logger.debug("TxCapsule: NOT Throwing HeuristicCommitException, " +
                         "status=" + getStringStatus(status));
         return;
      }
   }

   /**
    *  Prepare this instance for reuse.
    */
   private void instanceDone()
   {
      // Notify transaction fronts that we are done.
      for (int i = 0; i < transactionCount; ++i)
        transactions[i].setDone();

      synchronized (this) {
         // Done with this incarnation.
         ++incarnationCount;

         // Set done flag so we get no more frontends waiting for
         // the lock.
         done = true;

         // Wake up anybody waiting for the lock.
         notifyAll();
      }

      // Clear content of collections.
      syncCount = 0;
      transactionCount = 0;
      resourceCount = 0;

      // This instance is now ready for reuse (when we release the lock).
      tm.releaseTxCapsule(this);
   }

   /**
    *  Prepare all enlisted resources.
    *  If the first phase of the commit process results in a decision
    *  to commit the <code>status</code> will be
    *  <code>Status.STATUS_PREPARED</code> on return.
    *  Otherwise the <code>status</code> will be
    *  <code>Status.STATUS_MARKED_ROLLBACK</code> on return.
    *  This will release the lock while calling out.
    *
    *  @returns True iff all resources voted read-only.
    */
   private boolean prepareResources()
   {
      boolean readOnly = true;

      status = Status.STATUS_PREPARING;

      for (int i = 0; i < resourceCount; i++) {
         // Abort prepare on state change.
         if (status != Status.STATUS_PREPARING)
            return false;

         XAResource resource = resources[i];

         try {
            int vote;

            unlock();
            try {
               vote = resources[i].prepare(xid);
            } finally {
               lock();
            }

            if (vote == XAResource.XA_OK) {
               readOnly = false;
               resourceState[i] = RS_VOTE_OK;
            } else if (vote == XAResource.XA_RDONLY)
               resourceState[i] = RS_VOTE_READONLY;
            else {
               // Illegal vote: rollback.
               status = Status.STATUS_MARKED_ROLLBACK;
               return false;
            }
         } catch (XAException e) {
            readOnly = false;

            switch (e.errorCode) {
            case XAException.XA_HEURCOM:
               // Heuristic commit is not that bad when preparing.
               // But it means trouble if we have to rollback.
               gotHeuristic(resources[i], e.errorCode);
               break;
            case XAException.XA_HEURRB:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resources[i], e.errorCode);
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            default:
               Logger.warning("XAException: tx=" + toString() + " errorCode=" +
                              getStringXAErrorCode(e.errorCode));
               Logger.exception(e);
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            }
         }
      }

      if (status == Status.STATUS_PREPARING)
         status = Status.STATUS_PREPARED;

      return readOnly;
   }

   /**
    *  Commit all enlisted resources.
    *  This will release the lock while calling out.
    */
   private void commitResources(boolean onePhase)
   {
      status = Status.STATUS_COMMITTING;

      for (int i = 0; i < resourceCount; i++) {
        Logger.debug("TxCapsule.commitResources(): resourceStates["+i+"]="+resourceState[i]);
         if (!onePhase && resourceState[i] != RS_VOTE_OK)
           continue;

         // Abort commit on state change.
         if (status != Status.STATUS_COMMITTING)
            return;

         try {
            unlock();
            try {
               resources[i].commit(xid, onePhase);
            } finally {
               lock();
            }
         } catch (XAException e) {
            switch (e.errorCode) {
            case XAException.XA_HEURRB:
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resources[i], e.errorCode);
               break;
            default:
               Logger.warning("XAException: tx=" + toString() + " errorCode=" +
                              getStringXAErrorCode(e.errorCode));
               Logger.exception(e);
               break;
            }
         }
      }

      if (status == Status.STATUS_COMMITTING)
         status = Status.STATUS_COMMITTED;
   }

   /**
    *  Rollback all enlisted resources.
    *  This will release the lock while calling out.
    */
   private void rollbackResources()
   {
      status = Status.STATUS_ROLLING_BACK;

      for (int i = 0; i < resourceCount; i++) {
         if (resourceState[i] == RS_VOTE_READONLY)
           continue;

         try {
            unlock();
            try {
               resources[i].rollback(xid);
            } finally {
               lock();
            }
         } catch (XAException e) {
            switch (e.errorCode) {
            case XAException.XA_HEURRB:
               // Heuristic rollback is not that bad when rolling back.
               gotHeuristic(resources[i], e.errorCode);
               break;
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resources[i], e.errorCode);
               break;
            default:
               Logger.warning("XAException: tx=" + toString() + " errorCode=" +
                              getStringXAErrorCode(e.errorCode));
               Logger.exception(e);
               break;
            }
         }
      }

      status = Status.STATUS_ROLLEDBACK;
   }

   // Inner classes -------------------------------------------------
}
