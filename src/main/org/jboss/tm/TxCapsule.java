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
import org.jboss.logging.Logger;

/**
 *  TxCapsule
 *
 *  TxCapsule holds all the information relevant to a transaction. Callbacks and synchronizations are held here
 *
 *	@see <related>
 *	@author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @version $Revision: 1.2 $
 */
public class TxCapsule
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   // A list of synchronizations to call back on commit (before and after)
   Vector sync;
   
   // A list of the XARessources to 2phi commit (prepare and commit)
   Vector resources;

   Xid xid; // XA legacy 
   int hashCode;
   int status;
   long timeout;
   long start;
   
   // The public face of the capsule a JTA implementation
   Transaction transaction;

   // Static --------------------------------------------------------
   static int nextId = 0;
   public int getNextId() { return nextId++; }
   public TxManager tm;

   static String hostName;

   // Constructors --------------------------------------------------
   public TxCapsule(TxManager tm, int timeout)
   {
		this(tm);

      resources = new Vector();
      sync = new Vector();
      status = Status.STATUS_ACTIVE;

      this.timeout = (long) timeout;
      start = System.currentTimeMillis();
	}

   public TxCapsule(TxManager tm)
   {
      status = Status.STATUS_NO_TRANSACTION;
	  hashCode = getNextId();
      xid = new XidImpl((getHostName()+"/"+hashCode).getBytes(), null);
      transaction = new TransactionImpl(tm, hashCode, xid);
      this.tm = tm;
   }
 
   // Public --------------------------------------------------------
   public void commit()
            throws RollbackException,
                   HeuristicMixedException,
                   HeuristicRollbackException,
                   java.lang.SecurityException,
                   java.lang.IllegalStateException,
                   SystemException
   {
      if (status == Status.STATUS_NO_TRANSACTION)
         throw new IllegalStateException("No transaction started");
      // Call Synchronization
      for (int i = 0; i < sync.size(); i++)
      {
         // Check rollback
         if (status == Status.STATUS_MARKED_ROLLBACK)
         {
            break;
         }

         ((Synchronization)sync.elementAt(i)).beforeCompletion();
      }

      // Check rollback
      if (status != Status.STATUS_MARKED_ROLLBACK)
      {
         // Prepare XAResources
         status = Status.STATUS_PREPARING;
         for (int i = 0; i < resources.size(); i++)
         {
            // Check rollback
            if (status == Status.STATUS_MARKED_ROLLBACK)
            {
               break;
            }

            try
            {
               ((XAResource)resources.elementAt(i)).prepare(xid);
            } catch (XAException e)
            {
               if(e.errorCode != XAException.XA_HEURCOM)
               {
                   Logger.exception(e);
                  // Rollback
                  setRollbackOnly();
                  break;
               }
            }
         }

         // Check rollback
         if (status != Status.STATUS_MARKED_ROLLBACK)
         {
            status = Status.STATUS_PREPARED; // TODO: necessary to set?

            // Commit XAResources
            status = Status.STATUS_COMMITTING;
            for (int i = 0; i < resources.size(); i++)
            {

               try
               {
                  ((XAResource)resources.elementAt(i)).commit(xid, false);
               } catch (XAException e)
               {
                  try {
                     ((XAResource)resources.elementAt(i)).forget(xid);
                  } catch(XAException another) {}
                  Logger.exception(e);
                  // TODO: what to do here?
               }
            }
            status = Status.STATUS_COMMITTED;
         }
      }

      // Check rollback
      if (status == Status.STATUS_MARKED_ROLLBACK)
      {
         // Rollback XAResources
         status = Status.STATUS_ROLLING_BACK;
         for (int i = 0; i < resources.size(); i++)
         {

            try
            {
               ((XAResource)resources.elementAt(i)).rollback(xid);
            } catch (XAException e)
            {
               try {
                  ((XAResource)resources.elementAt(i)).forget(xid);
               } catch(XAException another) {}
               // TODO: what to do here?
            }
         }
         status = Status.STATUS_ROLLEDBACK;
      }

      // Call Synchronization
      for (int i = 0; i < sync.size(); i++)
      {
         ((Synchronization)sync.elementAt(i)).afterCompletion(status);
      }

   }

   public boolean delistResource(XAResource xaRes, int flag)
   {
        try {
            xaRes.end(xid, Status.STATUS_ACTIVE);
//            resources.removeElement(xaRes);
            return true;
        } catch(XAException e) {
            Logger.exception(e);
            return false;
        }
   }

   public boolean enlistResource(XAResource xaRes)
      throws RollbackException
   {
      // Check rollback only
      if (status == Status.STATUS_MARKED_ROLLBACK)
      {
         throw new RollbackException();
      }

      // Add resource
        try {
            xaRes.start(xid, Status.STATUS_ACTIVE);
            resources.addElement(xaRes);
            return true;
        } catch(XAException e) {
            Logger.exception(e);
            return false;
        }
   }

   public int getStatus()
              throws SystemException
   {
      return status;
   }

   public void registerSynchronization(Synchronization s)
   {
      sync.addElement(s);
   }

   public void rollback()
              throws java.lang.IllegalStateException,
                     java.lang.SecurityException,
                     SystemException
   {
      if (status == Status.STATUS_NO_TRANSACTION)
         throw new IllegalStateException("No transaction started");
		 
		 //MF FIXME I don't get it what is the use of this call if the "rollback is done in the commit
   }

   public void setRollbackOnly()
                     throws java.lang.IllegalStateException,
                            SystemException
   {
      if (status == Status.STATUS_NO_TRANSACTION)
         throw new IllegalStateException("No transaction started");

      status = Status.STATUS_MARKED_ROLLBACK;
   }

   // Package protected ---------------------------------------------

	Transaction getTransaction() {
		
		return transaction;
	}
	
   // Protected -----------------------------------------------------
   protected String getHostName()
   {
      if (hostName == null)
      {
         try
         {
            hostName = InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException e)
         {
            hostName = "localhost";
         }
      }

      return hostName;
   }

    public String toString() {
        return xid.toString();
    }

   // Private -------------------------------------------------------
/*   private Object writeReplace(java.io.ObjectOutputStream out)
      throws IOException
   {
      return new TransactionProxy(this);
   }
*/
   // Inner classes -------------------------------------------------
}
