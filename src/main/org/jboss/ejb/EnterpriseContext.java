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

import org.jboss.logging.Logger;

/**
 *	The EnterpriseContext is used to associate EJB instances with metadata about it.
 *	
 *	@see StatefulSessionEnterpriseContext
 *	@see StatelessSessionEnterpriseContext
 *	@see EntityEnterpriseContext
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.6 $
 */
public abstract class EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	// The EJB instanfce
   Object instance;
	
	// The container using this context
   Container con;
	
	// Set to the synchronization currently associated with this context. May be null
   Synchronization synch;
   
   // The transaction associated with the instance
   Transaction transaction;
   
   // The principal associated with the call
   Principal principal;
	
	// Only StatelessSession beans have no Id, stateful and entity do
   Object id; 
   
   // Static --------------------------------------------------------
   
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
   
   public abstract void discard()
      throws RemoteException;
      
   public void setId(Object id) { 
		this.id = id; 
	}
	
   public Object getId() { 
		return id; 
	}

   public void setTransaction(Transaction transaction) { 
		this.transaction = transaction; 
	}
	
   public Transaction getTransaction() { 
		return transaction; 
	}
	
	public void setPrincipal(Principal principal) {
		
		this.principal = principal;
	}
	
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   protected class EJBContextImpl
      implements EJBContext
   {
      /**
       *
       *
       * @deprecated
       */
      public Identity getCallerIdentity() 
		{ 
			throw new EJBException("Deprecated"); 
		}
      
      public Principal getCallerPrincipal() 
		{ 
			return principal;
		}
      
      public EJBHome getEJBHome() 
      { 
			if (con instanceof EntityContainer)
			{
				return ((EntityContainer)con).getContainerInvoker().getEJBHome(); 
			} 
			else if (con instanceof StatelessSessionContainer)
			{
				return ((StatelessSessionContainer)con).getContainerInvoker().getEJBHome(); 
			} 
			else if (con instanceof StatefulSessionContainer) 
			{
			 	return ((StatefulSessionContainer)con).getContainerInvoker().getEJBHome();
			}
			{
				// Should never get here
				throw new EJBException("No EJBHome available (BUG!)");
			}
      }
      
      /**
       *
       *
       * @deprecated
       */
      public Properties getEnvironment() 
		{ 
			throw new EJBException("Deprecated"); 
		}
      
      public boolean getRollbackOnly() 
      { 
         try
         {
            return con.getTransactionManager().getStatus() == Status.STATUS_MARKED_ROLLBACK; 
         } catch (SystemException e)
         {
            Logger.debug(e);
            return true;
         }
      }
       
      public void setRollbackOnly() 
		{ 
			try
			{
				con.getTransactionManager().setRollbackOnly();
			} catch (SystemException e)
			{
				Logger.debug(e);
			}
		}
   
      /**
       *
       *
       * @deprecated
       */
      public boolean isCallerInRole(Identity id) 
		{ 
			throw new EJBException("Deprecated"); 
		}
   
      // TODO - how to handle this best?
      public boolean isCallerInRole(String id) 
		{ 
			return false; 
		}
   
      // TODO - how to handle this best?
      public UserTransaction getUserTransaction() 
		{ 
			return new UserTransactionImpl(); 
		}
   }
   
	// Inner classes -------------------------------------------------
   class UserTransactionImpl
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

