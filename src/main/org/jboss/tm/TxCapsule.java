/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
 *  TODO: Implement timeouts.
 *  TODO: Implement persistent storage and recovery.
 *
 *  @see TxManager
 *  @see TransactionImpl
 *
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *
 *  @version $Revision: 1.7 $
 */
class TxCapsule implements TimeoutTarget
{
   // Constants -----------------------------------------------------

   // Code meaning "no heuristics seen", must not be XAException.XA_HEURxxx
   static private final int HEUR_NONE     = XAException.XA_RETRY;

   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------

   static private int nextId = 0;
   static private synchronized int getNextId() { return nextId++; }

   private static String hostName;

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
      int hashCode = getNextId();
      xid = new XidImpl((getHostName()+"/"+hashCode).getBytes(), null);
      transaction = new TransactionImpl(tm, hashCode, xid);
      this.tm = tm;
   }

   // Public --------------------------------------------------------

   /**
    *  Called when our timeout expires.
    */
   public void timedOut(Timeout timeout)
   {
      try {
         lock();

         Logger.warning("Transaction " + toString() + " timed out.");

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
            suspendedResourcesDone();
            rollbackResources();
            doAfterCompletion();
            gotHeuristic(null, XAException.XA_HEURRB);
            return;

         case Status.STATUS_PREPARING:
            status = Status.STATUS_MARKED_ROLLBACK;
            return; // commit will fail

         default:
            Logger.warning("TxCapsule: Unknown status at timeout.");
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

   /**
    *  Return the transaction encapsulated here.
    */
   Transaction getTransaction() {
      return transaction;
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
         //DEBUG Logger.log("TxCapsule before lock");
		 lock();
         //DEBUG Logger.log("TxCapsule after lock status is "+getStringStatus(status));
		 
         switch (status) {
         case Status.STATUS_PREPARING:
            throw new IllegalStateException("Already started preparing.");
         case Status.STATUS_PREPARED:
            throw new IllegalStateException("Already prepared.");
         case Status.STATUS_ROLLING_BACK:
            throw new IllegalStateException("Already started rolling back.");
         case Status.STATUS_ROLLEDBACK:
            checkHeuristics();
            throw new IllegalStateException("Already rolled back.");
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing.");
         case Status.STATUS_COMMITTED:
            checkHeuristics();
            throw new IllegalStateException("Already committed.");
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction.");
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state");
         case Status.STATUS_MARKED_ROLLBACK:
            suspendedResourcesDone();
            rollbackResources();
            doAfterCompletion();
            throw new RollbackException("Already marked for rollback");
         case Status.STATUS_ACTIVE:
			 //DEBUG Logger.log("Commiting tx with status Active");
            break;
         default:
            throw new IllegalStateException("Illegal status: " + status);
         }

         suspendedResourcesDone();

         doBeforeCompletion();

		 Logger.log("Before completion is done status is "+getStringStatus(status));
		 
         if (status == Status.STATUS_ACTIVE) {
            if (resources.size() == 0) {
				//DEBUG Logger.log("no resources 0 phi commit");
               // Zero phase commit is really fast ;-)
               status = Status.STATUS_COMMITTED;
            } else if (resources.size() == 1) {
               // DEBUG Logger.log("1 resource 1 phi commit");
			   // One phase commit
			   
               commitResources(true);
            } else {
				
				// DEBUG Logger.log("many resources 2 phi commit");
               // Two phase commit

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
            throw new RollbackException("Unable to commit transaction has status. "+getStringStatus(status));
         }

         cancelTimeout();

         doAfterCompletion();

         checkHeuristics();
      } finally {
        unlock();
      }
   }

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
		 	return "STATUS_UNKNOWN";
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

         switch (status) {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_MARKED_ROLLBACK:
            suspendedResourcesDone();
            rollbackResources();
            cancelTimeout();
            doAfterCompletion();
            // Cannot throw heuristic exception, so we just have to
            // clear the heuristics without reporting.
            heuristicCode = HEUR_NONE;
            return;
         case Status.STATUS_PREPARING:
            // Set status to avoid race with prepareResources().
            status = Status.STATUS_MARKED_ROLLBACK;
            return; // commit() will do rollback.
         default:
            throw new IllegalStateException("Cannot rollback()");
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

         if (!resources.contains(xaRes))
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
               suspendedResources.add(xaRes);
            else if (flag == XAResource.TMFAIL)
               status = Status.STATUS_MARKED_ROLLBACK;
            return true;
         } catch(XAException e) {
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

         // Add resource
         try {
            if (suspendedResources.contains(xaRes)) {
               startResource(xaRes, XAResource.TMRESUME);
               suspendedResources.remove(xaRes);
               return true;
            }
            for (int i = 0; i < resources.size(); ++i) {
               if (xaRes.isSameRM((XAResource)resources.get(i))) {
                  startResource(xaRes, XAResource.TMJOIN);
                  resources.add(xaRes);
                  return true;
               }
            }
            // New resource
            // According to the JTA spec we should create a new
            // transaction branch here.
            startResource(xaRes, XAResource.TMNOFLAGS);
            resources.add(xaRes);
            return true;
         } catch(XAException e) {
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

         sync.add(s);
      } finally {
         unlock();
      }
   }

   // Protected -----------------------------------------------------

   /**
    *  Return the host name of this host.
    *  This is used for building globally unique transaction identifiers.
    *  It would be safer to use the IP address, but a host name is better
    *  for humans to read and will do for now.
    */
   protected String getHostName()
   {
      if (hostName == null) {
         try {
            hostName = InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException e) {
            hostName = "localhost";
         }
      }

      return hostName;
   }

   // Private -------------------------------------------------------

   // A list of synchronizations to call back on commit (before and after)
   private ArrayList sync = new ArrayList();

   // A list of the XARessources to 2phi commit (prepare and commit)
   private ArrayList resources = new ArrayList();

   // Suspended XAResources are in this set
   private Set suspendedResources = new HashSet();

   private Xid xid; // XA legacy
   private int status; // status code
   private int heuristicCode = HEUR_NONE; // heuristics status
   private long start;
   private Timeout timeout;

   // The public face of the capsule a JTA implementation
   private Transaction transaction;

   // My manager
   private TxManager tm;

   // Mutex for thread-safety
   private boolean locked = false;

   /**
    *  Lock this instance.
    */
   private synchronized void lock()
   {
      if (locked) {
         Logger.warning("TxCapsule: Lock contention."); // Good for debugging.
         Thread.currentThread().dumpStack();
      }

      while (locked) {
         try {
            wait();
         } catch (InterruptedException ex) {}
      }

      locked = true;
   }

   /**
    *  Unlock this instance.
    */
   private synchronized void unlock()
   {
      if (!locked)
         Logger.warning("TxCapsule: Unlocking, but not locked.");

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
         } finally {
            lock();
         }
         timeout = null;
      }
   }

   /**
    *  Call <code>start()</code> on the XAResource.
    *  This will release the lock while calling out.
    */
   private void startResource(XAResource xaRes, int flags)
      throws XAException
   {
      unlock();
      try {
         xaRes.start(xid, flags);
      } finally {
         lock();
      }
   }

   /**
    *  Call <code>end()</code> on the XAResource.
    *  This will release the lock while calling out.
    */
   private void endResource(XAResource xaRes, int flag)
      throws XAException
   {
      unlock();
      try {
         xaRes.end(xid, flag);
      } finally {
         lock();
      }
   }

   /**
    *  End Tx association for all suspended resources.
    */
   private void suspendedResourcesDone()
   {
      try {
         while (!suspendedResources.isEmpty()) {
            Iterator iter = suspendedResources.iterator();

            try {
               while (iter.hasNext()) {
                  XAResource xaRes = (XAResource)iter.next();

                  iter.remove();
                  endResource(xaRes, XAResource.TMSUCCESS);
               }
            } catch (ConcurrentModificationException e) { }
         }
      } catch(XAException e) {
         Logger.exception(e);
         status = Status.STATUS_MARKED_ROLLBACK;
      }
   }

   /**
    *  Call synchronization <code>beforeCompletion()</code>.
    *  This will release the lock while calling out.
    */
   private void doBeforeCompletion()
   {
      unlock();
      try {
         for (int i = 0; i < sync.size(); i++) {
			//DEBUG Logger.log("calling beforeCompletion on synch status is "+getStringStatus(status));
            ((Synchronization)sync.get(i)).beforeCompletion();
			//DEBUG Logger.log("Done calling beforeCompletion on synch status is "+getStringStatus(status));
            
		}
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
         for (int i = 0; i < sync.size(); i++)
            ((Synchronization)sync.get(i)).afterCompletion(status);
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
         throw new HeuristicMixedException();
      case XAException.XA_HEURRB:
         heuristicCode = HEUR_NONE;
         throw new HeuristicRollbackException();
      case XAException.XA_HEURCOM:
         heuristicCode = HEUR_NONE;
         // Why isn't HeuristicCommitException used in JTA ?
         // And why define something that is not used ?
         // For now we just have to ignore this failure, even if it happened
         // on rollback.
         return;
      }
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
      Logger.log("Status Preparing: "+status);

      for (int i = 0; i < resources.size(); i++) {
         // Abort prepare on state change.
         if (status != Status.STATUS_PREPARING)
            return false;

         XAResource resource = (XAResource)resources.get(i);

         try {
            int vote;

            unlock();
            try {
			   vote = resource.prepare(xid);
			   
               Logger.log("resource vote is "+vote);
			   
            } finally {
               lock();
            }

            if (vote != XAResource.XA_RDONLY)
               readOnly = false;
            else {
               // Resource voted read-only: Can forget about resource.
               resources.remove(i);
               --i; // undo future increment
            }
         } catch (XAException e) {
            switch (e.errorCode) {
            case XAException.XA_HEURCOM:
               // Heuristic commit is not that bad when preparing.
               // But it means trouble if we have to rollback.
               gotHeuristic(resource, e.errorCode);
               break;
            case XAException.XA_HEURRB:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resource, e.errorCode);
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            default:
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

      for (int i = 0; i < resources.size(); i++) {
         // Abort commit on state change.
         if (status != Status.STATUS_COMMITTING)
            return;

         XAResource resource = (XAResource)resources.get(i);

         try {
            unlock();
            try {
               resource.commit(xid, onePhase);
            } finally {
               lock();
            }
         } catch (XAException e) {
            switch (e.errorCode) {
            case XAException.XA_HEURRB:
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resource, e.errorCode);
               break;
            default:
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

      for (int i = 0; i < resources.size(); i++) {
         XAResource resource = (XAResource)resources.get(i);

         try {
            unlock();
            try {
               resource.rollback(xid);
            } finally {
               lock();
            }
         } catch (XAException e) {
            switch (e.errorCode) {
            case XAException.XA_HEURRB:
               // Heuristic rollback is not that bad when rolling back.
               gotHeuristic(resource, e.errorCode);
               break;
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resource, e.errorCode);
               break;
            default:
               Logger.exception(e);
               break;
            }
         }
      }

      status = Status.STATUS_ROLLEDBACK;
   }

   // Inner classes -------------------------------------------------
}
