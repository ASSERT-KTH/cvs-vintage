/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
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
 * TxCapsule holds all the information relevant to a transaction.
 * Callbacks and synchronizations are held here.
 *
 * <p>TODO: Implement persistent storage and recovery.
 *
 * @see TxManager
 * @see TransactionImpl
 *
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.33 $
 */
class TxCapsule
implements TimeoutTarget
{
   // Constants -----------------------------------------------------
   
   /**
    * Code meaning "no heuristics seen",
    * must not be XAException.XA_HEURxxx
    */
   private static final int HEUR_NONE = XAException.XA_RETRY;
   
   // Attributes ----------------------------------------------------
   
   /** Instance logger. */
   private Logger log = Logger.getLogger(this.getClass());
   
   /** True if trace messages should be logged. */
   private boolean trace = log.isDebugEnabled();
   
   // Static --------------------------------------------------------
   
   /**
    *  Constructor for Xid instances of specified class, or null.
    *  This is set from the <code>TransactionManagerService</code>
    *  MBean.
    */
   static Constructor xidConstructor = null;
   
   /**
    *  This collection contains the inactive txCapsules.
    *  We keep these for reuse.
    *  They are referenced through soft references so that they will
    *  be garbage collected if the VM runs low on memory.
    */
   private static LinkedList inactiveCapsules = new LinkedList();
   
   /**
    *  Get a new instance.
    *
    *  @param timeout The timeout for this transaction in milliseconds.
    */
   static TxCapsule getInstance(long timeOut)
   {
      TxCapsule txCapsule = null;
      synchronized (inactiveCapsules)
      {
         while (inactiveCapsules.size() > 0)
         {
            SoftReference ref = (SoftReference)inactiveCapsules.removeFirst();
            txCapsule = (TxCapsule)ref.get();
            if (txCapsule != null)
               break;
         }
      }
      if (txCapsule == null)
         txCapsule = new TxCapsule(timeOut);
      else
         txCapsule.reUse(timeOut);
      
      return txCapsule;
   }
   
   /**
    *  Release an instance for reuse.
    */
   static private void releaseInstance(TxCapsule txCapsule)
   {
      synchronized (inactiveCapsules)
      {
         inactiveCapsules.add(new SoftReference(txCapsule));
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    *  Create a new TxCapsule.
    *
    *  @param timeout The timeout for this transaction in milliseconds.
    */
   private TxCapsule(long timeout)
   {
      xid = new XidImpl();
      
      if (xidConstructor != null)
      {
         // Allocate constructor argument array.
         xidConstructorArgs = new Object[3];
         // First constructor argument is always the same: Pre-init it.
         xidConstructorArgs[0] = new Integer(xid.getFormatId());
      }
      
      transaction = new TransactionImpl(this, xid);
      
      status = Status.STATUS_ACTIVE;
      
      start = System.currentTimeMillis();
      this.timeout = TimeoutFactory.createTimeout(start+timeout, this);
      
      if (trace)
      {
         log.trace("Created new instance for tx=" + toString());
      }
   }
   
   /**
    *  Prepare this instance for reuse.
    */
   private void reUse(long timeout)
   {
      if (!done)
         throw new IllegalStateException();
      
      done = false;
      resourcesEnded = false;
      
      xid = new XidImpl();
      lastBranchId = 0; // BQIDs start over again in scope of the new GID.
      
      transaction = new TransactionImpl(this, xid);
      
      status = Status.STATUS_ACTIVE;
      heuristicCode = HEUR_NONE;
      
      start = System.currentTimeMillis();
      this.timeout = TimeoutFactory.createTimeout(start+timeout, this);
      
      if (trace)
      {
         log.trace("Reused instance for tx=" + toString());
      }
   }
   
   // Public --------------------------------------------------------
   
   /**
    *  Called when our timeout expires.
    */
   public void timedOut(Timeout timeout)
   {
      try
      {
         lock();
         
         log.warn("Transaction " + toString() + " timed out." +
         " status=" + getStringStatus(status));
         
         if (this.timeout == null)
            return; // Don't race with timeout cancellation.
         this.timeout = null;
         
         switch (status)
         {
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
               gotHeuristic(-1, XAException.XA_HEURMIX);
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
               // don't rollback for now, this messes up with the TxInterceptor.
               return;
               
            case Status.STATUS_PREPARING:
               status = Status.STATUS_MARKED_ROLLBACK;
               return; // commit will fail
               
            default:
               log.warn("Unknown status at timeout, tx=" + toString());
               return;
         }
      } finally
      {
         unlock();
      }
   }
   
   public String toString()
   {
      return XidImpl.toString(xid);
   }
   
   /**
    *  Return the front for this transaction.
    */
   TransactionImpl getTransactionImpl()
   {
      return transaction;
   }
   
   // Package protected ---------------------------------------------
   
   /**
    *  Commit the transaction encapsulated here.
    *  Should not be called directly, use <code>TxManager.commit()</code>
    *  instead.
    */
   void commit()
   throws RollbackException,
   HeuristicMixedException,
   HeuristicRollbackException,
   SecurityException,
   IllegalStateException,
   SystemException
   {
      try
      {
         lock();
         
         if (trace)
         {
            log.trace("Committing, tx=" + this +
            ", status=" + getStringStatus(status));
         }
         
         switch (status)
         {
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
         {
            log.trace("Before completion done, tx=" + this +
            ", status=" + getStringStatus(status));
         }
         
         endResources();
         
         if (status == Status.STATUS_ACTIVE)
         {
            if (resourceCount == 0)
            {
               // Zero phase commit is really fast ;-)
               if (trace)
               {
                  log.trace("Zero phase commit: No resources.");
               }
               status = Status.STATUS_COMMITTED;
            } else if (resourceCount == 1)
            {
               // One phase commit
               if (trace)
               {
                  log.trace("One phase commit: One resource.");
               }
               commitResources(true);
            } else
            {
               // Two phase commit
               if (trace)
               {
                  log.trace("Two phase commit: Many resources.");
               }
               
               if (!prepareResources())
               {
                  boolean commitDecision =
                  status == Status.STATUS_PREPARED &&
                  (heuristicCode == HEUR_NONE ||
                  heuristicCode == XAException.XA_HEURCOM);
                  
                  // TODO: Save decision to stable storage for recovery
                  //       after system crash.
                  
                  if (commitDecision)
                     commitResources(false);
               } else
                  status = Status.STATUS_COMMITTED; // all was read-only
            }
         }
         
         if (status != Status.STATUS_COMMITTED)
         {
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
         {
            log.trace("Committed OK, tx=" + this);
         }
         
      } finally
      {
         unlock();
         
         // This instance is now ready for reuse (when we release the lock).
         if (done)
            releaseInstance(this);
      }
   }
   
   /**
    *  Rollback the transaction encapsulated here.
    *  Should not be called directly, use <code>TxManager.rollback()</code>
    *  instead.
    */
   void rollback()
   throws IllegalStateException, SecurityException, SystemException
   {
      try
      {
         lock();
         
         if (trace)
         {
            log.trace("rollback(): Entered, tx=" + toString() +
            " status=" + getStringStatus(status));
         }
         
         switch (status)
         {
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
      } finally
      {
         unlock();
         
         // This instance is now ready for reuse (when we release the lock).
         if (done)
            releaseInstance(this);
      }
   }
   
   /**
    *  Mark the transaction encapsulated here so that the only possible
    *  outcome is a rollback.
    *  Should not be called directly, use <code>TxManager.rollback()</code>
    *  instead.
    */
   void setRollbackOnly()
   throws IllegalStateException, SystemException
   {
      try
      {
         lock();
         
         if (trace)
         {
            log.trace("setRollbackOnly(): Entered, tx=" +
            toString() + " status=" + getStringStatus(status));
         }
         
         switch (status)
         {
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
      } finally
      {
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
   throws IllegalStateException, SystemException
   {
      if (xaRes == null)
         throw new IllegalArgumentException("null xaRes");
      if (flag != XAResource.TMSUCCESS &&
      flag != XAResource.TMSUSPEND &&
      flag != XAResource.TMFAIL)
         throw new IllegalArgumentException("Bad flag: " + flag);
      
      try
      {
         lock();
         
         if (trace)
         {
            log.trace("delistResource(): Entered, tx=" +
            toString() + " status=" + getStringStatus(status));
         }
         
         int idx = findResource(xaRes);
         
         if (idx == -1)
            throw new IllegalArgumentException("xaRes not enlisted");
         
         switch (status)
         {
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
         
         try
         {
            endResource(idx, flag);
            return true;
         } catch (XAException e)
         {
            log.warn("XAException: tx=" + toString() + " errorCode=" +
            getStringXAErrorCode(e.errorCode), e);
            status = Status.STATUS_MARKED_ROLLBACK;
            return false;
         }
      } finally
      {
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
   throws RollbackException, IllegalStateException, SystemException
   {
      if (xaRes == null)
         throw new IllegalArgumentException("null xaRes");
      
      try
      {
         lock();
         
         if (trace)
         {
            log.trace("enlistResource(): Entered, tx=" +
            toString() + " status=" + getStringStatus(status));
         }
         
         switch (status)
         {
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
         try
         {
            int idx = findResource(xaRes);
            
            if (idx != -1)
            {
               if (resourceState[idx] == RS_ENLISTED)
                  return false; // already enlisted
               
               startResource(idx);
               return true;
            }
            
            // This optimization hangs the Oracle XAResource
            // when you perform xaCon.start(Xid, TMJOIN)
            if (xidConstructor == null)
            {
               for (int i = 0; i < resourceCount; ++i)
               {
                  if (resourceSameRM[i] == -1 && xaRes.isSameRM(resources[i]))
                  {
                     // The xaRes is new. We register the xaRes with the Xid
                     // that the RM has previously seen from this transaction,
                     // and note that it has the same RM.
                     startResource(addResource(xaRes, resourceXids[i], i));
                     
                     return true;
                  }
               }
            }
            
            // New resource and new RM: Create a new transaction branch.
            startResource(addResource(xaRes, createXidBranch(), -1));
            return true;
         } catch (XAException e)
         {
            log.warn("XAException: tx=" + toString() + " errorCode=" +
            getStringXAErrorCode(e.errorCode), e);
            return false;
         }
      } finally
      {
         unlock();
      }
   }
   
   /**
    *  Return the status of the transaction encapsulated here.
    */
   int getStatus() throws SystemException
   {
      return status;
   }
   
   /**
    *  Register a new transaction synchronization for the transaction
    *  encapsulated here.
    */
   void registerSynchronization(Synchronization s)
   throws RollbackException, IllegalStateException, SystemException
   {
      if (s == null)
         throw new IllegalArgumentException("Null synchronization");
      
      try
      {
         lock();
         
         if (trace)
         {
            log.trace("registerSynchronization(): Entered, " +
            "tx=" + toString() +
            " status=" + getStringStatus(status));
         }
         
         switch (status)
         {
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
               // OSH: EntitySynchronizationInterceptor bug is fixed long ago,
               // and since nobody seems to get the warning anymore it should
               // be safe to be JTA-conformant.
               // In case of trouble, try changing "true" below to "false".
               if (true)
                  throw new RollbackException("Already marked for rollback");
               else
               {
                  // Workaround for EntitySynchronizationInterceptor bug.
                  log.warn("Violating JTA by adding synchronization to a " +
                  "transaction marked for rollback.");
                  break;
               }
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
         
         if (syncCount == syncAllocSize)
         {
            // expand table
            syncAllocSize = 2 * syncAllocSize;
            
            Synchronization[] sy = new Synchronization[syncAllocSize];
            System.arraycopy(sync, 0, sy, 0, syncCount);
            sync = sy;
         }
         sync[syncCount++] = s;
      } finally
      {
         unlock();
      }
   }
   
   // Protected -----------------------------------------------------
   
   
   // Private -------------------------------------------------------
   
   /**
    *  The public face of this capsule is a JTA Transaction implementation.
    */
   private TransactionImpl transaction;
   
   /**
    *  The synchronizations to call back.
    */
   private Synchronization[] sync = new Synchronization[1];
   
   /**
    *  Size of allocated synchronization array.
    */
   private int syncAllocSize = 1;
   
   /**
    *  Count of synchronizations for this transaction.
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
   
   private final static int RS_NEW           = 0; // not yet enlisted
   private final static int RS_ENLISTED      = 1; // enlisted
   private final static int RS_SUSPENDED     = 2; // suspended
   private final static int RS_ENDED         = 3; // not associated
   private final static int RS_VOTE_READONLY = 4; // voted read-only
   private final static int RS_VOTE_OK       = 5; // voted ok
   
   /**
    *  Index of the first XAResource representing the same resource manager,
    *  or <code>-1</code> if this XAResource is the first XAResource in this
    *  transaction that represents its resource manager.
    */
   private int[] resourceSameRM = new int[1];
   
   /**
    *  A list of the XARessources that have participated in this transaction.
    */
   private Xid[] resourceXids = new Xid[1];
   
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
    *  The ID of this transaction. This Xid corresponds to the root branch
    *  of the transaction branch tree, and is never passed outside this
    *  package.
    */
   private XidImpl xid;
   
   /**
    *  Xid constructor arguments. Never used unless <code>xidConstructor</code>
    *  is <code>null</code>.
    */
   private Object[] xidConstructorArgs = null;
   
   /**
    *  Last branch id used.
    */
   private int lastBranchId = 0;
   
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
    * private static TxManager tm = TxManager.getInstance();
    */
   
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
   private String getStringStatus(int status)
   {
      switch (status)
      {
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
   private String getStringXAErrorCode(int errorCode)
   {
      switch (errorCode)
      {
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
      
      if (locked)
      {
         log.warn("Lock contention, tx=" + toString());
         //DEBUG Thread.currentThread().dumpStack();
         
         long myIncarnation = incarnationCount;
         
         while (locked)
         {
            try
            {
               // Wakeup happens when:
               // - notify() is called from unlock()
               // - notifyAll is called from instanceDone()
               wait();
            } catch (InterruptedException ex)
            {}
            
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
      if (!locked)
      {
         log.warn("Unlocking, but not locked, tx=" + toString(),
         new Throwable("[Stack trace]"));
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
      if (timeout != null)
      {
         unlock();
         try
         {
            timeout.cancel();
         } catch (Exception e)
         {
            if (trace)
               log.trace("failed to cancel timeout", e);
         } finally
         {
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
      // A linear search may seem slow, but please note that
      // the number of XA resources registered with a transaction
      // are usually low.
      for (int idx = 0; idx < resourceCount; ++idx)
         if (xaRes == resources[idx])
            return idx;
      
      return -1;
   }
   
   /**
    *  Add a resource, expanding tables if needed.
    *
    *  @param xaRes The new XA resource to add. It is assumed that the
    *         resource is not already in the table of XA resources.
    *  @param branchXid The Xid for the transaction branch that is to
    *         be used for associating with this resource.
    *  @param idxSameRM The index in our XA resource tables of the first
    *         XA resource having the same resource manager as
    *         <code>xaRes</code>, or <code>-1</code> if <code>xaRes</code>
    *         is the first resource seen with this resource manager.
    *
    *  @return The index of the new resource in our internal tables.
    */
   private int addResource(XAResource xaRes, Xid branchXid, int idxSameRM)
   {
      if (resourceCount == resourceAllocSize)
      {
         // expand tables
         resourceAllocSize = 2 * resourceAllocSize;
         
         XAResource[] res = new XAResource[resourceAllocSize];
         System.arraycopy(resources, 0, res, 0, resourceCount);
         resources = res;
         
         int[] stat = new int[resourceAllocSize];
         System.arraycopy(resourceState, 0, stat, 0, resourceCount);
         resourceState = stat;
         
         Xid[] xids = new Xid[resourceAllocSize];
         System.arraycopy(resourceXids, 0, xids, 0, resourceCount);
         resourceXids = xids;
         
         int[] sameRM = new int[resourceAllocSize];
         System.arraycopy(resourceSameRM, 0, sameRM, 0, resourceCount);
         resourceSameRM = sameRM;
      }
      resources[resourceCount] = xaRes;
      resourceState[resourceCount] = RS_NEW;
      resourceXids[resourceCount] = branchXid;
      resourceSameRM[resourceCount] = idxSameRM;
      
      return resourceCount++;
   }
   
   /**
    *  Call <code>start()</code> on a XAResource and update
    *  internal state information.
    *  This will release the lock while calling out.
    *
    *  @param idx The index of the resource in our internal tables.
    */
   private void startResource(int idx)
   throws XAException
   {
      int flags = XAResource.TMJOIN;
      
      if (resourceSameRM[idx] == -1)
      {
         switch (resourceState[idx])
         {
            case RS_NEW:
               flags = XAResource.TMNOFLAGS;
               break;
            case RS_SUSPENDED:
               flags = XAResource.TMRESUME;
               break;
         }
      }
      
      if (trace)
      {
         log.trace("startResource(" +
         XidImpl.toString(resourceXids[idx]) +
         ") entered: " + resources[idx].toString() +
         " flags=" + flags);
      }
      
      unlock();
      // OSH FIXME: resourceState could be incorrect during this callout.
      try
      {
         try
         {
            resources[idx].start(resourceXids[idx], flags);
         } catch(XAException e)
         {
            throw e;
         } catch (Throwable t)
         {
            if (trace)
            {
               log.trace("unhandled throwable", t);
            }
            status = Status.STATUS_MARKED_ROLLBACK;
            return;
         }
         
         // Now the XA resource is associated with a transaction.
         resourceState[idx] = RS_ENLISTED;
      } finally
      {
         lock();
         if (trace)
         {
            log.trace("startResource(" +
            XidImpl.toString(resourceXids[idx]) +
            ") leaving: " + resources[idx].toString() +
            " flags=" + flags);
         }
      }
   }
   
   /**
    *  Call <code>end()</code> on the XAResource and update
    *  internal state information.
    *  This will release the lock while calling out.
    *
    *  @param idx The index of the resource in our internal tables.
    *  @param flag The flag argument for the end() call.
    */
   private void endResource(int idx, int flag)
   throws XAException
   {
      if (trace)
      {
         log.trace("endResource(" +
         XidImpl.toString(resourceXids[idx]) +
         ") entered: " + resources[idx].toString() +
         " flag=" + flag);
      }
      
      unlock();
      // OSH FIXME: resourceState could be incorrect during this callout.
      try
      {
         try
         {
            resources[idx].end(resourceXids[idx], flag);
         } catch(XAException e)
         {
            throw e;
         } catch (Throwable t)
         {
            if (trace)
            {
               log.trace("unhandled throwable", t);
            }
            status = Status.STATUS_MARKED_ROLLBACK;
            // Resource may or may not be ended after illegal exception.
            // We just assume it ended.
            resourceState[idx] = RS_ENDED;
            return;
         }
         
         
         // Update our internal state information
         if (flag == XAResource.TMSUSPEND)
            resourceState[idx] = RS_SUSPENDED;
         else
         {
            if (flag == XAResource.TMFAIL)
               status = Status.STATUS_MARKED_ROLLBACK;
            resourceState[idx] = RS_ENDED;
         }
      } finally
      {
         lock();
         if (trace)
         {
            log.trace("endResource(" +
            XidImpl.toString(resourceXids[idx]) +
            ") leaving: " + resources[idx].toString() +
            " flag=" + flag);
         }
      }
   }
   
   /**
    *  End Tx association for all resources.
    */
   private void endResources()
   {
      for (int idx = 0; idx < resourceCount; idx++)
      {
         try
         {
            if (resourceState[idx] == RS_SUSPENDED)
            {
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
               startResource(idx);
            }
            if (resourceState[idx] == RS_ENLISTED)
            {
               if (trace)
               {
                  log.trace("endresources(" + idx + "): state=" +
                  resourceState[idx]);
               }
               endResource(idx, XAResource.TMSUCCESS);
            }
         } catch(XAException e)
         {
            log.warn("XAException: tx=" + toString() + " errorCode=" +
            getStringXAErrorCode(e.errorCode), e);
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
      try
      {
         for (int i = 0; i < syncCount; i++)
         {
            try
            {
               sync[i].beforeCompletion();
            } catch (Throwable t)
            {
               if (trace)
               {
                  log.trace("failed before completion", t);
               }
               status = Status.STATUS_MARKED_ROLLBACK;
               break;
            }
         }
      } finally
      {
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
      try
      {
         for (int i = 0; i < syncCount; i++)
         {
            try
            {
               sync[i].afterCompletion(status);
            } catch (Throwable t)
            {
               if (trace)
               {
                  log.trace("failed after completion", t);
               }
            }
         }
      } finally
      {
         lock();
      }
   }
   
   /**
    *  We got another heuristic.
    *
    *  Promote <code>heuristicCode</code> if needed and tell
    *  the resource to forget the heuristic.
    *  This will release the lock while calling out.
    *
    *  @param resIdx The index of the XA resource that got a
    *         heurictic in our internal tables, or <code>-1</code>
    *         if the heuristic came from here.
    *  @param code The heuristic code, one of
    *         <code>XAException.XA_HEURxxx</code>.
    */
   private void gotHeuristic(int resIdx, int code)
   {
      switch (code)
      {
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
      
      if (resIdx != -1)
      {
         try
         {
            unlock();
            resources[resIdx].forget(resourceXids[resIdx]);
         } catch (XAException e)
         {
            log.warn("XAException at forget(): errorCode=" +
            getStringXAErrorCode(e.errorCode), e);
         } finally
         {
            lock();
         }
      }
   }
   
   /**
    *  Check for heuristics, clear and throw exception if any found.
    */
   private void checkHeuristics()
   throws HeuristicMixedException, HeuristicRollbackException
   {
      switch (heuristicCode)
      {
         case XAException.XA_HEURHAZ:
         case XAException.XA_HEURMIX:
            heuristicCode = HEUR_NONE;
            if (trace)
            {
               log.trace("Throwing HeuristicMixedException, " +
               "status=" + getStringStatus(status));
            }
            throw new HeuristicMixedException();
         case XAException.XA_HEURRB:
            heuristicCode = HEUR_NONE;
            if (trace)
            {
               log.trace("Throwing HeuristicRollbackException, " +
               "status=" + getStringStatus(status));
            }
            throw new HeuristicRollbackException();
         case XAException.XA_HEURCOM:
            heuristicCode = HEUR_NONE;
            // Why isn't HeuristicCommitException used in JTA ?
            // And why define something that is not used ?
            // For now we just have to ignore this failure, even if it happened
            // on rollback.
            if (trace)
            {
               log.trace("NOT Throwing HeuristicCommitException, " +
               "status=" + getStringStatus(status));
            }
            return;
      }
   }
   
   /**
    *  Prepare this instance for reuse.
    */
   private void instanceDone()
   {
      // Notify transaction frontend that we are done.
      transaction.setDone();
      
      synchronized (this)
      {
         // Done with this incarnation.
         ++incarnationCount;
         
         // Set done flag so we get no more frontends waiting for
         // the lock.
         done = true;
         
         // Wake up anybody waiting for the lock.
         notifyAll();
      }
      
      // Clear content of collections.
      for (int i = 0; i < syncCount; ++i)
         sync[i] = null; // release for GC
      syncCount = 0;
      
      //for (int i = 0; i < transactionCount; ++i)
      //   transactions[i] = null; // release for GC
      //transactionCount = 0;
      transaction = null; // release for GC
      
      for (int i = 0; i < resourceCount; ++i)
      {
         resources[i] = null; // release for GC
         resourceXids[i] = null; // release for GC
      }
      resourceCount = 0;
      
      // If using a special class, second constructor argument is now useless.
      if (xidConstructor != null)
         xidConstructorArgs[1] = null; // This now needs initializing
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
      
      for (int i = 0; i < resourceCount; i++)
      {
         // Abort prepare on state change.
         if (status != Status.STATUS_PREPARING)
            return false;
         
         if (resourceSameRM[i] != -1)
            continue; // This RM already prepared.
         
         XAResource resource = resources[i];
         
         try
         {
            int vote;
            
            unlock();
            try
            {
               vote = resources[i].prepare(resourceXids[i]);
            } finally
            {
               lock();
            }
            
            if (vote == XAResource.XA_OK)
            {
               readOnly = false;
               resourceState[i] = RS_VOTE_OK;
            } else if (vote == XAResource.XA_RDONLY)
               resourceState[i] = RS_VOTE_READONLY;
            else
            {
               // Illegal vote: rollback.
               status = Status.STATUS_MARKED_ROLLBACK;
               return false;
            }
         } catch (XAException e)
         {
            readOnly = false;
            
            switch (e.errorCode)
            {
               case XAException.XA_HEURCOM:
                  // Heuristic commit is not that bad when preparing.
                  // But it means trouble if we have to rollback.
                  gotHeuristic(i, e.errorCode);
                  break;
               case XAException.XA_HEURRB:
               case XAException.XA_HEURMIX:
               case XAException.XA_HEURHAZ:
                  gotHeuristic(i, e.errorCode);
                  if (status == Status.STATUS_PREPARING)
                     status = Status.STATUS_MARKED_ROLLBACK;
                  break;
               default:
                  log.warn("XAException: tx=" + toString() + " errorCode=" +
                  getStringXAErrorCode(e.errorCode), e);
                  if (status == Status.STATUS_PREPARING)
                     status = Status.STATUS_MARKED_ROLLBACK;
                  break;
            }
         } catch (Throwable t)
         {
            if (trace)
            {
               log.trace("unhandled throwable", t);
            }
            if (status == Status.STATUS_PREPARING)
               status = Status.STATUS_MARKED_ROLLBACK;
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
      
      for (int i = 0; i < resourceCount; i++)
      {
         if (trace)
         {
            log.trace("Committing resources, resourceStates["+i+"]=" +
            resourceState[i]);
         }
         
         if (!onePhase && resourceState[i] != RS_VOTE_OK)
            continue; // Voted read-only at prepare phase.
         
         if (resourceSameRM[i] != -1)
            continue; // This RM already committed.
         
         // Abort commit on state change.
         if (status != Status.STATUS_COMMITTING)
            return;
         
         try
         {
            unlock();
            try
            {
               resources[i].commit(resourceXids[i], onePhase);
            } finally
            {
               lock();
            }
         } catch (XAException e)
         {
            switch (e.errorCode)
            {
               case XAException.XA_HEURRB:
               case XAException.XA_HEURCOM:
               case XAException.XA_HEURMIX:
               case XAException.XA_HEURHAZ:
                  gotHeuristic(i, e.errorCode);
                  break;
               default:
                  log.warn("XAException: tx=" + toString() + " errorCode=" +
                  getStringXAErrorCode(e.errorCode), e);
                  break;
            }
            try
            {
               // OSH: Why this?
               // Heuristics only exist if we go one of the heuristic
               // error codes, and for these forget() is called from
               // gotHeuristic().
               resources[i].forget(resourceXids[i]);
            } catch (XAException forgetEx)
            {}
         } catch (Throwable t)
         {
            if (trace)
            {
               log.trace("unhandled throwable", t);
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
      
      for (int i = 0; i < resourceCount; i++)
      {
         if (resourceState[i] == RS_VOTE_READONLY)
            continue;
         
         try
         {
            unlock();
            try
            {
               resources[i].rollback(resourceXids[i]);
            } finally
            {
               lock();
            }
         } catch (XAException e)
         {
            switch (e.errorCode)
            {
               case XAException.XA_HEURRB:
                  // Heuristic rollback is not that bad when rolling back.
                  gotHeuristic(i, e.errorCode);
                  break;
               case XAException.XA_HEURCOM:
               case XAException.XA_HEURMIX:
               case XAException.XA_HEURHAZ:
                  gotHeuristic(i, e.errorCode);
                  break;
               default:
                  log.warn("XAException: tx=" + toString() + " errorCode=" +
                  getStringXAErrorCode(e.errorCode), e);
                  break;
            }
            try
            {
               // OSH: Why this?
               // Heuristics only exist if we go one of the heuristic
               // error codes, and for these forget() is called from
               // gotHeuristic().
               resources[i].forget(resourceXids[i]);
            } catch (XAException forgetEx)
            {}
         } catch (Throwable t)
         {
            if (trace)
            {
               log.trace("unhandled throwable", t);
            }
         }
      }
      
      status = Status.STATUS_ROLLEDBACK;
   }
   
   /**
    *  Create an Xid representing a new branch of this transaction.
    */
   private Xid createXidBranch()
   {
      int branchId = ++lastBranchId;
      
      if (xidConstructor == null)
         return new XidImpl(xid, branchId);
      else
      {
         try
         {
            if (xidConstructorArgs[1] == null)
            {
               // First branching of this global transaction id.
               // Oracle XA driver needs the full length GID.
               byte[] gidShort = xid.getGlobalTransactionId();
               byte[] gid = new byte[Xid.MAXGTRIDSIZE];
               System.arraycopy(gidShort, 0, gid, 0, gidShort.length);
               xidConstructorArgs[1] = gid;
            }
            
            // Oracle XA driver needs the full length BQID.
            byte[] bqidShort = Integer.toString(branchId).getBytes();
            byte[] bqid = new byte[Xid.MAXBQUALSIZE];
            System.arraycopy(bqidShort, 0, bqid, 0, bqidShort.length);
            xidConstructorArgs[2] = bqid;
            
            return (Xid)xidConstructor.newInstance(xidConstructorArgs);
         } catch (Exception e)
         {
            log.warn("Unable to create an Xid (using default impl)", e);
            return new XidImpl(xid, branchId);
         }
      }
   }
   
   // Inner classes -------------------------------------------------
}
