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

/**
 *	<description>
 *
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.4 $
 */
public class TransactionImpl
   implements Transaction, Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   Vector sync;
   Vector resources;

   Xid xid;
   int status;
   long timeout;
   long start;

   // Static --------------------------------------------------------
   static long nextId = 0;
   public long getNextId() { return nextId++; }
   public transient TxManager tm;

   static String hostName;

   // Constructors --------------------------------------------------
   public TransactionImpl(TxManager tm, int timeout)
   {
		this(tm);

      resources = new Vector();
      sync = new Vector();
      status = Status.STATUS_ACTIVE;

      this.timeout = (long)timeout;
      start = System.currentTimeMillis();
   }

   public TransactionImpl(TxManager tm)
   {
      status = Status.STATUS_NO_TRANSACTION;
      xid = new XidImpl((getHostName()+"/"+getNextId()).getBytes(), null);

      this.tm = tm;
   }
   
   private TxManager getTxManager() {
	
	   if (tm != null) {
		   
		   return tm;
	   }
	   
	   // A way to circle the mess
	   // :(
	   tm = TxManager.getTransactionManager();
	   
	   return tm;
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
                   e.printStackTrace();
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
                  e.printStackTrace();
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

      // Remove thread association with this tx
      getTxManager().removeTransaction();
      resources.clear();
   }

   public boolean delistResource(XAResource xaRes, int flag)
   {
        try {
            xaRes.end(xid, Status.STATUS_ACTIVE);
//            resources.removeElement(xaRes);
            return true;
        } catch(XAException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
   }

   public void setRollbackOnly()
                     throws java.lang.IllegalStateException,
                            SystemException
   {
      if (status == Status.STATUS_NO_TRANSACTION)
         throw new IllegalStateException("No transaction started");

      status = Status.STATUS_MARKED_ROLLBACK;
   }

   public boolean equals(Object obj)
   {
      return ((TransactionImpl)obj).xid.getGlobalTransactionId().equals(xid.getGlobalTransactionId());
   }

   public int hashCode()
   {
      return xid.getGlobalTransactionId().hashCode();
   }

   // Package protected ---------------------------------------------

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
