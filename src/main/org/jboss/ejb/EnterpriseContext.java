/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;
import java.security.Identity;
import java.security.Principal;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;
import javax.transaction.Synchronization;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public abstract class EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Object instance;
   Container con;
   Transaction tx;
   Synchronization synch;
   Principal principal;
    
   Object id; // Not used for sessions
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public EnterpriseContext(Object instance, Container con)
   {
      this.instance = instance;
      this.con = con;
   }
   
   // Public --------------------------------------------------------
   public Object getInstance() { return instance; }
   
   public abstract void discard()
      throws RemoteException;
      
   public void setPrincipal(Principal p) { principal = p; }
   public Principal getPrincipal(Principal p) { return principal; }
   
   public void setTransaction(Transaction tx) { this.tx = tx; }
   public Transaction getTransaction() { return tx; }
   
   public void setId(Object id) { this.id = id; }
   public Object getId() { return id; }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   protected class EJBContextImpl
      implements EJBContext
   {
      public Identity getCallerIdentity() { throw new EJBException("Deprecated"); }
      
      public Principal getCallerPrincipal() { return principal; }
      
      public EJBHome getEJBHome() 
      { 
         return con.getContainerInvoker().getEJBHome(); 
      }
      
      public Properties getEnvironment() { throw new EJBException("Deprecated"); }
      
      public boolean getRollbackOnly() 
      { 
         try
         {
            return con.getTransactionManager().getStatus() == Status.STATUS_MARKED_ROLLBACK; 
         } catch (SystemException e)
         {
            return true;
         }
      }
       
      public void setRollbackOnly() { }
   
      public boolean isCallerInRole(Identity id) { throw new EJBException("Deprecated"); }
   
      // TODO - how to handle this best?
      public boolean isCallerInRole(String id) { return false; }
   
      // TODO - how to handle this best?
      public UserTransaction getUserTransaction() { return new UserTransactionImpl(); }
   }
   
   protected class UserTransactionImpl
      implements UserTransaction
   {
      public void begin()
         throws NotSupportedException,SystemException
      {
         con.getTransactionManager().begin();
      }
      
      public void commit()
            throws RollbackException,
                   HeuristicMixedException,
                   HeuristicRollbackException,
                   java.lang.SecurityException,
                   java.lang.IllegalStateException,
                   SystemException
      {
         con.getTransactionManager().commit();
      }
       
      public void rollback()
              throws java.lang.IllegalStateException,
                     java.lang.SecurityException,
                     SystemException
      {
         con.getTransactionManager().rollback();
      }
      
      public void setRollbackOnly()
         throws java.lang.IllegalStateException, SystemException   
      {
         con.getTransactionManager().setRollbackOnly();
      }
      
      public int getStatus()
              throws SystemException
      {
         return con.getTransactionManager().getStatus();
      }
      
      public void setTransactionTimeout(int seconds)
         throws SystemException
      {
         con.getTransactionManager().setTransactionTimeout(seconds);
      }
   }
}

