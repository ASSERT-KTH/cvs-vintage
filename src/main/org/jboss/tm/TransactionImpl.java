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
 *	A light weight transaction.
 *
 * It is the public face of the TxCapsule.  Many of these "transactions" can coexist representing the TxCap
 * Access to the underlying txCap is done through the TransactionManager
 *
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.6 $
 */
public class TransactionImpl
   implements Transaction, Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   int hash;
   Xid xid; // XA legacy
   
   // Static --------------------------------------------------------
   public transient TxManager tm;

   static String hostName;

   // Constructors --------------------------------------------------
   
   public TransactionImpl(TxManager tm,int hash, Xid xid)
   {
      this.tm = tm;
	  this.hash = hash;
	  this.xid = xid; 
   }
   
   /*
   * setTxManager()
   *
   * used for propagated Tx
   */
   public void setTxManager(TxManager tm) {
	   
	   this.tm= tm;
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
      tm.commit(this);
   }

   public boolean delistResource(XAResource xaRes, int flag)
   {
       return tm.delistResource(this, xaRes, flag);
   }

   public boolean enlistResource(XAResource xaRes)
      throws RollbackException
   {
	   return tm.enlistResource(this, xaRes);
   }

   public int getStatus()
              throws SystemException
   {
      return tm.getStatus(this);
   }

   public void registerSynchronization(Synchronization s)
   {
	   tm.registerSynchronization(this, s);
   }

   public void rollback()
              throws java.lang.IllegalStateException,
                     java.lang.SecurityException,
                     SystemException
   {
	   tm.rollback(this);
   }

   public void setRollbackOnly()
                     throws java.lang.IllegalStateException,
                            SystemException
   {
     
		tm.setRollbackOnly(this); 
	}

   public boolean equals(Object obj)
   {
	  return ((TransactionImpl)obj).hash == hash;
   }

   public int hashCode()
   {
      return hash;
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
        return "tx:Xid:"+hash;
    }

   // Private -------------------------------------------------------
   // Inner classes -------------------------------------------------
}
