/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityRoleRefMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;

import javax.ejb.*;
import javax.ejb.Timer;
import javax.transaction.*;
import java.rmi.RemoteException;
import java.security.Identity;
import java.security.Principal;
import java.util.*;
import java.io.Serializable;

/**
 * The EnterpriseContext is used to associate EJB instances with
 * metadata about it.
 *  
 * @see StatefulSessionEnterpriseContext
 * @see StatelessSessionEnterpriseContext
 * @see EntityEnterpriseContext
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 1.74 $
 *
 * Revisions:
 * 2001/06/29: marcf
 *	- Added txLock to permit locking and most of all notifying on tx
 *	  demarcation only
 */
public abstract class EnterpriseContext
        implements AllowedOperationsFlags
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** Instance logger. */
   protected static Logger log = Logger.getLogger(EnterpriseContext.class);

   /** The EJB instance */
   Object instance;
    
   /** The container using this context */
   Container con;
    
   /**
    * Set to the synchronization currently associated with this context.
    * May be null
    */
   Synchronization synch;
   
   /** The transaction associated with the instance */
   Transaction transaction;
   
   /** The principal associated with the call */
   private Principal principal;

   /** The principal for the bean associated with the call */
   private Principal beanPrincipal;
    
   /** Only StatelessSession beans have no Id, stateful and entity do */
   Object id; 
   
   /** The instance is being used.  This locks it's state */
   int locked = 0;
	
   /** The instance is used in a transaction, synchronized methods on the tx */
   Object txLock = new Object();

   /**
    * Holds one of the IN_METHOD constants, to indicate that we are in an ejb method
    * According to the EJB2.1 spec not all context methods can be accessed at all times
    * For example ctx.getPrimaryKey() should throw an IllegalStateException when called from within ejbCreate()
    */
   private Stack inMethodStack = new Stack();

   // Static --------------------------------------------------------
   //Registration for CachedConnectionManager so our UserTx can notify
   //on tx started.
   private static ServerVMClientUserTransaction.UserTransactionStartedListener tsl;

   /**
    * The <code>setUserTransactionStartedListener</code> method is called by 
    * CachedConnectionManager on start and stop.  The tsl is notified on 
    * UserTransaction.begin so it (the CachedConnectionManager) can enroll
    * connections that are already checked out.
    *
    * @param newTsl a <code>ServerVMClientUserTransaction.UserTransactionStartedListener</code> value
    */
   public static void setUserTransactionStartedListener(ServerVMClientUserTransaction.UserTransactionStartedListener newTsl)
   {
      tsl = newTsl;
   }

   // Constructors --------------------------------------------------
   
   public EnterpriseContext(Object instance, Container con)
   {
      this.instance = instance;
      this.con = con;
   }
   
   // Public --------------------------------------------------------

   public Object getInstance() 
   { 
      return instance; 
   }
    
   /**
    * Gets the container that manages the wrapped bean.
    */
   public Container getContainer() {
      return con;
   }
   
   public abstract void discard()
      throws RemoteException;

   /**
    * Get the EJBContext object
    */
   public abstract EJBContext getEJBContext();

   public void setId(Object id) { 
      this.id = id; 
   }
    
   public Object getId() { 
      return id; 
   }

   public Object getTxLock() {
      return txLock;
   }
	
   public void setTransaction(Transaction transaction) {
      // DEBUG log.debug("EnterpriseContext.setTransaction "+((transaction == null) ? "null" : Integer.toString(transaction.hashCode()))); 
      this.transaction = transaction; 
   }
    
   public Transaction getTransaction() { 
      return transaction; 
   }
    
   public void setPrincipal(Principal principal) {
      this.principal = principal;
      this.beanPrincipal = null;
   }
   
   public void lock() 
   {
      locked ++;
      //new Exception().printStackTrace();
      //DEBUG log.debug("EnterpriseContext.lock() "+hashCode()+" "+locked);
   }
    
   public void unlock() {
        
      // release a lock
      locked --;
       
      //new Exception().printStackTrace();
      if (locked <0) {
         // new Exception().printStackTrace();
         log.error("locked < 0", new Throwable());
      }
       
      //DEBUG log.debug("EnterpriseContext.unlock() "+hashCode()+" "+locked);
   }
    
   public boolean isLocked() {
            
      //DEBUG log.debug("EnterpriseContext.isLocked() "+hashCode()+" at "+locked);
      return locked != 0;
   }
   
   public Principal getCallerPrincipal()
   {
      EJBContextImpl ctxImpl = (EJBContextImpl) getEJBContext();
      return ctxImpl.getCallerPrincipalInternal();
   }
   
   /**
    * before reusing this context we clear it of previous state called
    * by pool.free()
    */
   public void clear() {
      this.id = null;
      this.locked = 0;
      this.principal = null;
      this.beanPrincipal = null;
      this.synch = null;
      this.transaction = null;
      this.inMethodStack.clear();
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------

   protected boolean isContainerManagedTx()
   {
      BeanMetaData md = (BeanMetaData)con.getBeanMetaData();
      return md.isContainerManagedTx();
   }

   protected boolean isUserManagedTx()
   {
      BeanMetaData md = (BeanMetaData)con.getBeanMetaData();
      return md.isContainerManagedTx() == false;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   protected class EJBContextImpl
      implements EJBContext
   {
      /**
       *  A per-bean instance UserTransaction instance cached after the
       *  first call to <code>getUserTransaction()</code>.
       */
      private UserTransactionImpl userTransaction = null;

      /**
       * @deprecated
       */
      public Identity getCallerIdentity() 
      { 
         throw new EJBException("Deprecated"); 
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         return getContainer().getTimerService( null );
      }

      /** Get the Principal for the current caller. This method
          cannot return null according to the ejb-spec.
      */
      public Principal getCallerPrincipal() 
      { 
         return getCallerPrincipalInternal();
      }
      
      /**
       * The implementation of getCallerPrincipal()
       */
      Principal getCallerPrincipalInternal()
      {
         if( beanPrincipal == null )
         {
            RealmMapping rm = con.getRealmMapping();           
            if( principal != null )
            {
               if( rm != null )
                  beanPrincipal = rm.getPrincipal(principal);
               else
                  beanPrincipal = principal;      
            }
            else if( rm != null )
            {  // Let the RealmMapping map the null principal
               beanPrincipal = rm.getPrincipal(principal);
            }
            else
            {  // Check for a unauthenticated principal value
               ApplicationMetaData appMetaData = con.getBeanMetaData().getApplicationMetaData();
               String name = appMetaData.getUnauthenticatedPrincipal();
               if( name != null )
                  beanPrincipal = new SimplePrincipal(name);
            }
         }
         if( beanPrincipal == null )
            throw new IllegalStateException("No security context set");
         return beanPrincipal;
      }
      
      public EJBHome getEJBHome()
      {
         EJBProxyFactory proxyFactory = con.getProxyFactory();
         if (proxyFactory == null)
            throw new IllegalStateException("No remote home defined.");

         return (EJBHome) proxyFactory.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         if (con.getLocalHomeClass()==null)
            throw new IllegalStateException( "No local home defined." );

         if (con instanceof EntityContainer)
            return ((EntityContainer)con).getLocalProxyFactory().getEJBLocalHome();
         else if (con instanceof StatelessSessionContainer)
            return ((StatelessSessionContainer)con).getLocalProxyFactory().getEJBLocalHome();
         else if (con instanceof StatefulSessionContainer)
            return ((StatefulSessionContainer)con).getLocalProxyFactory().getEJBLocalHome();

         // Should never get here
         throw new EJBException("No EJBLocalHome available (BUG!)");
      }
      
      /**
       * @deprecated
       */
      public Properties getEnvironment() 
      { 
         throw new EJBException("Deprecated"); 
      }
      
      public boolean getRollbackOnly() 
      { 
         // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
         if (con.getBeanMetaData().isBeanManagedTx())
            throw new IllegalStateException("getRollbackOnly() not allowed for BMT beans.");

         try {
            TransactionManager tm = con.getTransactionManager();

            // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
            // only in the session bean methods that execute in the context of a transaction.
            if (tm.getTransaction() == null)
               throw new IllegalStateException("getRollbackOnly() not allowed without a transaction.");

            return tm.getStatus() == Status.STATUS_MARKED_ROLLBACK;
         } catch (SystemException e) {
            log.warn("failed to get tx manager status; ignoring", e);
            return true;
         }
      }
       
      public void setRollbackOnly() 
      { 
         // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
         if (con.getBeanMetaData().isBeanManagedTx())
            throw new IllegalStateException("setRollbackOnly() not allowed for BMT beans.");

         try {
            TransactionManager tm = con.getTransactionManager();

            // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
            // only in the session bean methods that execute in the context of a transaction.
            if (tm.getTransaction() == null)
               throw new IllegalStateException("setRollbackOnly() not allowed without a transaction.");

            tm.setRollbackOnly();
         } catch (SystemException e) {
            log.warn("failed to set rollback only; ignoring", e);
         }
      }
   
      /**
       * @deprecated
       */
      public boolean isCallerInRole(Identity id) 
      { 
         throw new EJBException("Deprecated"); 
      }

      /**
       * Checks if the current caller has a given role.
       * The current caller is either the principal associated with the method invocation
       * or the current run-as principal.
       */ 
      public boolean isCallerInRole(String roleName)
      {
         RunAsIdentity runAsIdentity = SecurityAssociation.peekRunAsIdentity();
         if (principal == null && runAsIdentity == null)
            return false;

         RealmMapping rm = con.getRealmMapping();
         if (rm == null)
         {
            String msg = "isCallerInRole() called with no security context. "
                    + "Check that a security-domain has been set for the application.";
            throw new IllegalStateException(msg);
         }

         // Map the role name used by Bean Provider to the security role
         // link in the deployment descriptor. The EJB 1.1 spec requires
         // the security role refs in the descriptor but for backward
         // compability we're not enforcing this requirement.
         //
         // TODO (2.3): add a conditional check using jboss.xml <enforce-ejb-restrictions> element
         //             which will throw an exception in case no matching
         //             security ref is found.           
         Iterator it = getContainer().getBeanMetaData().getSecurityRoleReferences();
         boolean matchFound = false;

         while (it.hasNext())
         {
            SecurityRoleRefMetaData meta = (SecurityRoleRefMetaData) it.next();
            if (meta.getName().equals(roleName))
            {
               roleName = meta.getLink();
               matchFound = true;
               break;
            }
         }

         if (!matchFound)
            log.warn("no match found for security role " + roleName +
                    " in the deployment descriptor.");

         HashSet set = new HashSet();
         set.add(new SimplePrincipal(roleName));

         if (runAsIdentity == null)
            return rm.doesUserHaveRole(principal, set);
         else
            return runAsIdentity.doesUserHaveRole(set);
      }
   
      public UserTransaction getUserTransaction() 
      { 
         if (userTransaction == null)
         {
            if (isContainerManagedTx()) {
               throw new IllegalStateException
                  ("CMT beans are not allowed to get a UserTransaction");
            }
            
            userTransaction = new UserTransactionImpl(); 
         }

         return userTransaction;
      }
   }
   
   // Inner classes -------------------------------------------------
 
   protected class UserTransactionImpl
           implements UserTransaction
   {
      /**
       * Timeout value in seconds for new transactions started
       * by this bean instance.
       */
      private int timeout = 0;

      public UserTransactionImpl()
      {
         if (log.isDebugEnabled())
            log.debug("new UserTx: " + this);
      }

      public void begin()
              throws NotSupportedException, SystemException
      {
         TransactionManager tm = con.getTransactionManager();

         // Set the timeout value
         tm.setTransactionTimeout(timeout);

         // Start the transaction
         tm.begin();

         //notify checked out connections
         if (tsl != null)
         {
            tsl.userTransactionStarted();
         } // end of if ()
         
         Transaction tx = tm.getTransaction();
         if (log.isDebugEnabled())
            log.debug("UserTx begin: " + tx);

         // keep track of the transaction in enterprise context for BMT
         setTransaction(tx);
      }

      public void commit()
              throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
              SecurityException, IllegalStateException, SystemException
      {
         try
         {
            TransactionManager tm = con.getTransactionManager();
            Transaction tx = tm.getTransaction();
            if (log.isDebugEnabled())
               log.debug("UserTx commit: " + tx);

            int status = tm.getStatus();
            tm.commit();
         }
         finally
         {
            // According to the spec, after commit and rollback was called on
            // UserTransaction, the thread is associated with no transaction.
            // Since the BMT Tx interceptor will associate and resume the tx
            // from the context with the thread that comes in
            // on a subsequent invocation, we must set the context transaction to null
            setTransaction(null);
         }
      }

      public void rollback()
              throws IllegalStateException, SecurityException, SystemException
      {
         try
         {
            TransactionManager tm = con.getTransactionManager();
            Transaction tx = tm.getTransaction();
            if (log.isDebugEnabled())
               log.debug("UserTx rollback: " + tx);
            tm.rollback();
         }
         finally
         {
            // According to the spec, after commit and rollback was called on
            // UserTransaction, the thread is associated with no transaction.
            // Since the BMT Tx interceptor will associate and resume the tx
            // from the context with the thread that comes in
            // on a subsequent invocation, we must set the context transaction to null
            setTransaction(null);
         }
      }

      public void setRollbackOnly()
              throws IllegalStateException, SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         Transaction tx = tm.getTransaction();
         if (log.isDebugEnabled())
            log.debug("UserTx setRollbackOnly: " + tx);

         tm.setRollbackOnly();
      }

      public int getStatus()
              throws SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         return tm.getStatus();
      }

      /**
       * Set the transaction timeout value for new transactions
       * started by this instance.
       */
      public void setTransactionTimeout(int seconds)
              throws SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         Transaction tx = tm.getTransaction();
         if (log.isDebugEnabled())
            log.debug("UserTx setTransactionTimeout(" + seconds + "): " + tx);

         timeout = seconds;
      }
   }
}
