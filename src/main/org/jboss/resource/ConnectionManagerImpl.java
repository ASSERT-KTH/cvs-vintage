/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;

import javax.security.auth.Subject;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Log;
import org.jboss.minerva.pools.ObjectPool;
import org.jboss.minerva.pools.PoolObjectFactory;
import org.jboss.resource.pool.PoolStrategy;
import org.jboss.resource.pool.PoolStrategyFactory;
import org.jboss.resource.security.PrincipalMapping;
import org.jboss.security.SecurityAssociation;

/**
 *   Provides connection factories with a hook into the app server's
 *   "quality of services". There is one instance of this class for
 *   each connection factory and therefore for each managed connection
 *   factory instance.
 *
 *   @see <related>
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class ConnectionManagerImpl
   implements ConnectionManager, ConnectionEventListener
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private Log log;

   private TransactionManager tm;
   private ManagedConnectionFactory mcf;
   private RARMetaData metadata;

   private PrincipalMapping principalMapping;
   private PoolStrategy poolStrategy;

   /** Maps Transaction to ManagedConnection */
   private Map tx2mc = new HashMap();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   ConnectionManagerImpl(RARMetaData metadata, ConnectionFactoryConfig cfConfig,
                         ManagedConnectionFactory mcf, Log log,
                         TransactionManager tm,
                         PrincipalMapping principalMapping)
      throws SystemException
   {
      this.log = log;
      this.metadata = metadata;
      this.mcf = mcf;
      poolStrategy = PoolStrategyFactory.getStrategy(metadata, cfConfig,
                                                     mcf, log);
      //FIXME communicate pool settings to pool strategy, possibly
      //through metadata
      poolStrategy.setConnectionEventListener(this);
      this.tm = tm;
      this.principalMapping = principalMapping;
   }

   // Public --------------------------------------------------------

   // ConnectionManager implementation ------------------------------

   public Object allocateConnection(ManagedConnectionFactory mcf,
                                    ConnectionRequestInfo cxRequestInfo)
      throws ResourceException
   {
      if (!mcf.equals(this.mcf))
      {
         throw new ApplicationServerInternalException(
            "allocateConnection called for a managed connection factory that" +
            "is not the one this connection manager handles. this = '" + this +
            "', this.mcf = '" + this.mcf + "', mcf = '" + mcf + "', " +
            "cxRequestInfo = '" + cxRequestInfo + "'");
      }

      // Obtain a subject that identifies the resource principal and
      // its credentials. There are two possibilities for passing the
      // security information to the resource adapter:

      // 1) Component-managed sign-on - in this case cxRequestInfo
      // will contain the information that the resource adapter needs
      // to sign onto the EIS and we will leave subject null

      // 2) Container-managed sign-on - in this case cxRequestInfo is
      // null and we will create a Subject identifying the resource
      // principal and its credentials

      Subject subject;

      if (cxRequestInfo != null)
         // component-managed sign-on
         subject = null;
      else
      {
         // container-managed sign-on
         Principal callerPrincipal = SecurityAssociation.getPrincipal();
         subject = principalMapping.createSubject(callerPrincipal);
      }


      // Find an appropriate managed connection
      ManagedConnection mc;

      // Figure out if we need to associate this managed connection
      // with a transaction
      try
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            mc = (ManagedConnection) tx2mc.get(tx);
            if (mc != null)
            {
               log.debug("Using connection '" + mc + "', which is already " +
                         "associated with transaction '" + tx + "'");
            }
            else
            {
               log.debug("Finding an unused managed connection to handle " +
                         "transaction '" + tx + "'");
               mc = poolStrategy.getManagedConnection(subject, cxRequestInfo);
               log.debug("Enlisting connection '" + mc + "' with transaction " +
                         "'" + tx + "'");
               try
               {
                  tx.enlistResource(mc.getXAResource());
                  tx2mc.put(tx, mc);
                  TransactionSynchronization synch =
                     new TransactionSynchronization(tx, mc);
                  tx.registerSynchronization(synch);
               }
               catch (RollbackException rbe)
               {
                  // Oops! The transaction has been marked for
                  // rollback. We can't give out a connection not
                  // enlisted in any transaction, so we must throw an
                  // exception.
                  log.debug(rbe);
                  
                  // Don't forget to put the connection back in the
                  // pool! The connection must have just come from the
                  // pool because otherwise we wouldn't be trying to
                  // enlist it.
                  log.debug("The current transaction is marked for rollback, " +
                            "returning connection '" + mc + "' to its pool");
                  try
                  {
                     poolStrategy.releaseManagedConnection(mc);
                  }
                  catch (ResourceException re)
                  {
                     log.exception(re);
                  }
                  
                  ResourceException re =
                     new ResourceException("Transaction marked for rollback");
                  re.setLinkedException(rbe);
                  throw re;
               }
            }
         }
         else
         {
            mc = poolStrategy.getManagedConnection(subject, cxRequestInfo);
            log.debug("Not enlisting connection '" + mc + "' with a " +
                      "transaction");
         }
      }
      catch (SystemException se)
      {
         log.debug(se);
         ApplicationServerInternalException asie =
            new ApplicationServerInternalException("Transaction manager FUBAR");
         asie.setLinkedException(se);
         throw asie;
      }

      // Return an application-level connection handle
      return mc.getConnection(subject, cxRequestInfo);
   }

   // ConnectionEventListener implementation ------------------------

   public void connectionClosed(ConnectionEvent event)
   {
      ManagedConnection mc = (ManagedConnection) event.getSource();
      log.debug("connectionClosed for connection '" + mc + "'");

      try
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            // Sanity checks
            ManagedConnection txmc = (ManagedConnection) tx2mc.get(tx);
            if (txmc == null)
            {
               log.error("The connection '" + mc + "' has been closed in the " +
                         "context of transaction '" + tx + "' but I have no " +
                         "record of a connection being enlisted with that " +
                         "transaction. Bad");
               //FIXME what action to take?
            }
            else if (!txmc.equals(mc))
            {
               log.error("Connection '" + mc + "' has been closed in the " +
                         "context of transaction '" + tx + "' but " +
                         "connection '" + txmc + "' was enlisted in that " +
                         "transaction. Bad!");
               //FIXME what action to take?
            }

            // The connection will be returned to the pool when the
            // transaction completes. See TransactionSynchronization.

            /* delisting will only work right with a full-on
               XA-compliant resource adapter. I have none to test
               with.

            // We delist the connection's resource from the transaction (if
            // there is one) and return it to the pool so that it can be
            // used for other transactions or further work on this
            // transaction
            log.debug("delisting connection '" + mc + "' from transaction '" +
                      tx + "'");
            try
            {
               XAResource res = mc.getXAResource();
               tx.delistResource(res, XAResource.TMSUCCESS);
            }
            catch (ResourceException re)
            {
               log.warning("Unable to get XAResource from managed connection");
               log.exception(re);
            }
            */
         }
         else
         {
            log.debug("Connection '" + mc + "' not participating in a " +
                      "transaction, returning it to its pool");
            try
            {
               poolStrategy.releaseManagedConnection(mc);
            }
            catch (ResourceException re)
            {
               log.exception(re);
            }
         }
      }
      catch (SystemException se)
      {
         log.error("Transaction manager FUBAR");
         log.exception(se);
      }
   }

   public void connectionErrorOccurred(ConnectionEvent event)
   {
      ManagedConnection mc = (ManagedConnection) event.getSource();
      log.debug("connectionErrorOccurred for connection '" + mc + "', " +
                "exception is '" + event.getException() + "'");

      try
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            // Sanity checks
            ManagedConnection txmc = (ManagedConnection) tx2mc.get(tx);
            if (txmc == null)
            {
               log.error("An error has occurred for the connection '" + mc +
                         "' in the context of transaction '" + tx + "' but " +
                         "I have no record of a connection being enlisted " +
                         "with that transaction. Bad!");
               //FIXME what action to take?
            }
            else if (!txmc.equals(mc))
            {
               log.error("An error has occurred for the connection '" + mc +
                         "' in the context of transaction '" + tx + "' but " +
                         "connection '" + txmc + "' was enlisted in that " +
                         "transaction. Bad!");
               //FIXME what action to take?
            }

            try
            {
               // The managed connection will be destroyed by the pool
               // when it is returned at the completion of the
               // transaction.
               poolStrategy.condemnManagedConnection(mc);
            }
            catch (ResourceException re)
            {
               log.exception(re);
            }

            /* delisting will only work right with a full-on
               XA-compliant resource adapter. I have none to test
               with.

            // We delist the connection's resource from the transaction (if
            // there is one) and tell the transaction manager that the unit
            // of work has failed, which should mean that the transaction is
            // marked for rollback
            log.debug("Delisting connection '" + mc + "' from transaction " +
                      tx + "'");
            try
            {
               XAResource res = mc.getXAResource();
               tx.delistResource(res, XAResource.TMFAIL);
            }
            catch (ResourceException re)
            {
               log.warning("Unable to get XAResource from managed connection");
               log.exception(re);
            }

            log.debug("Destroying connection '" + mc + "'");
            try
            {
               poolStrategy.destroyManagedConnection(mc);
            }
            catch (ResourceException re)
            {
               log.exception(re);
            }
            */
         }
         else
         {
            log.debug("Connection '" + mc + "' not participating in a " +
                      "transaction, destroying immediately");
            try
            {
               poolStrategy.condemnManagedConnection(mc);
               poolStrategy.releaseManagedConnection(mc);
            }
            catch (ResourceException re)
            {
               log.exception(re);
            }
         }
      }
      catch (SystemException se)
      {
         log.error("Transaction manager FUBAR");
         log.exception(se);
      }
   }

   // We don't handle local transactions at the moment.
   public void localTransactionStarted(ConnectionEvent event)
   {
      log.error("Local transaction optimisation not implemented");
      log.exception(new Exception());
   } 
   public void localTransactionCommitted(ConnectionEvent event)
   {
      log.error("Local transaction optimisation not implemented");
      log.exception(new Exception());
   } 
   public void localTransactionRolledback(ConnectionEvent event)
   {
      log.error("Local transaction optimisation not implemented");
      log.exception(new Exception());
   } 

   // Package protected ---------------------------------------------

   void shutdown()
   {
      // Empty the pool(s)
      poolStrategy.shutdown();
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   /**
    * An instance of this class is registered as a synchronisation
    * whenever a connection is given out as part of a transaction.
    */
   private class TransactionSynchronization
      implements Synchronization
   {
      // Attributes -------------------------------------------------

      private Transaction tx;
      private ManagedConnection mc;

      // Constructors -----------------------------------------------

      private TransactionSynchronization(Transaction tx, ManagedConnection mc)
      {
         this.tx = tx;
         this.mc = mc;
      }

      // Synchronization implementation -----------------------------
      
      public void afterCompletion(int status)
      {
         log.debug("afterCompletion for connection '" + mc + "', transaction " +
                   "'" + tx + ", status = " + statusName(status));
         
         // Remove the association between this transaction and
         // managed connection
         ManagedConnection txmc = (ManagedConnection) tx2mc.remove(tx);

         // Sanity check
         if (txmc == null || !txmc.equals(mc))
         {
            log.error("Inconsistent transaction assocation: this.tx = '" + tx +
                      "', this.mc = '" + mc + "', tx2mc(tx) = '" + txmc + "'");
            //FIXME what action to take?
         }

         log.debug("Returning connection '" + mc + "' to its pool");
         try
         {
            poolStrategy.releaseManagedConnection(mc);
         }
         catch (ResourceException re)
         {
            log.exception(re);
         }
      }
      
      public void beforeCompletion() { }

      // Private ----------------------------------------------------

      private final String statusName(int s)
      {
         switch (s) {
         case Status.STATUS_ACTIVE: return "STATUS_ACTIVE";
         case Status.STATUS_COMMITTED: return "STATUS_COMMITED"; 
         case Status.STATUS_COMMITTING: return "STATUS_COMMITTING"; 
         case Status.STATUS_MARKED_ROLLBACK: return "STATUS_MARKED_ROLLBACK"; 
         case Status.STATUS_NO_TRANSACTION: return "STATUS_NO_TRANSACTION"; 
         case Status.STATUS_PREPARED: return "STATUS_PREPARED"; 
         case Status.STATUS_PREPARING: return "STATUS_PREPARING"; 
         case Status.STATUS_ROLLEDBACK: return "STATUS_ROLLEDBACK"; 
         case Status.STATUS_ROLLING_BACK: return "STATUS_ROLLING_BACK"; 
         case Status.STATUS_UNKNOWN: return "STATUS_UNKNOWN"; 
         }
         return "REALLY_UNKNOWN";
      }   
   }
}
