
/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;
import java.security.Identity;
import java.security.Principal;
import java.util.Properties;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.EJBLocalHome;
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
import org.jboss.metadata.SecurityRoleRefMetaData;

/**
 *  The EnterpriseContext is used to associate EJB instances with metadata about it.
 *  
 *  @see StatefulSessionEnterpriseContext
 *  @see StatelessSessionEnterpriseContext
 *  @see EntityEnterpriseContext
 *  @author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *  @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 *  @version $Revision: 1.29 $
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

   // The principal for the bean associated with the call
   Principal beanPrincipal;
    
    // Only StatelessSession beans have no Id, stateful and entity do
   Object id; 
   
   // The instance is being used.  This locks it's state
   int locked = 0;  
                  
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
    
   /**
    * Gets the container that manages the wrapped bean.
    */
   public Container getContainer() {
   		return con;
   	}
   
   public abstract void discard()
      throws RemoteException;

   /** Get the EJBContext object
   */
   public abstract EJBContext getEJBContext();

   public void setId(Object id) { 
       this.id = id; 
    }
    
   public Object getId() { 
       return id; 
    }

   public void setTransaction(Transaction transaction) {
       
//DEBUG       Logger.debug("EnterpriseContext.setTransaction "+((transaction == null) ? "null" : Integer.toString(transaction.hashCode()))); 
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
       
//DEBUG     Logger.debug("EnterpriseContext.lock() "+hashCode()+" "+locked);
    }
    
    public void unlock() {
        
        // release a lock
        locked --;
       
       //new Exception().printStackTrace();
       if (locked <0) new Exception().printStackTrace();
       
//DEBUG     Logger.debug("EnterpriseContext.unlock() "+hashCode()+" "+locked);
    }
    
    public boolean isLocked() {
            
//DEBUG       Logger.debug("EnterpriseContext.isLocked() "+hashCode()+" at "+locked);
       return locked != 0;
   }
   
   /*
   * clear()
   *
   * before reusing this context we clear it of previous state called by pool.free()
   */
   public void clear() {
   
    this.id = null;
    this.locked = 0;
    this.principal = null;
    this.beanPrincipal = null;
    this.synch = null;
    this.transaction = null;
   
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
         if (beanPrincipal == null && principal != null)
         {
             if( con.getRealmMapping() == null ) {
                 beanPrincipal = principal;
             } else {
                 beanPrincipal = con.getRealmMapping().getPrincipal(principal);
             }
         }
         else if( beanPrincipal == null && con.getRealmMapping() == null )
             throw new IllegalStateException("No security context set");
         return beanPrincipal;
       }
      
      public EJBHome getEJBHome()
      { 
         if (con instanceof EntityContainer)
         {
          if (((EntityContainer)con).getContainerInvoker()==null)
             throw new IllegalStateException( "No remote home defined." );
          return ((EntityContainer)con).getContainerInvoker().getEJBHome(); 
         } 
         else if (con instanceof StatelessSessionContainer)
         {
          if (((StatelessSessionContainer)con).getContainerInvoker()==null)
             throw new IllegalStateException( "No remote home defined." );
          return ((StatelessSessionContainer)con).getContainerInvoker().getEJBHome(); 
         } 
         else if (con instanceof StatefulSessionContainer) 
         {
          if (((StatefulSessionContainer)con).getContainerInvoker()==null)
             throw new IllegalStateException( "No remote home defined." );
              return ((StatefulSessionContainer)con).getContainerInvoker().getEJBHome();
         }
         {
          // Should never get here
          throw new EJBException("No EJBHome available (BUG!)");
         }
      }

      public EJBLocalHome getEJBLocalHome()
      { 
         if (con instanceof EntityContainer)
         {
          if (((EntityContainer)con).getLocalHomeClass()==null)
             throw new IllegalStateException( "No local home defined." );
          return ((EntityContainer)con).getLocalContainerInvoker().getEJBLocalHome(); 
         } 
         else if (con instanceof StatelessSessionContainer)
         {
          if (((StatelessSessionContainer)con).getLocalHomeClass()==null)
             throw new IllegalStateException( "No local home defined." );
          return ((StatelessSessionContainer)con).getLocalContainerInvoker().getEJBLocalHome(); 
         } 
         else if (con instanceof StatefulSessionContainer) 
         {
          if (((StatefulSessionContainer)con).getLocalHomeClass()==null)
             throw new IllegalStateException( "No local home defined." );
              return ((StatefulSessionContainer)con).getLocalContainerInvoker().getEJBLocalHome();
         }
         {
          // Should never get here
          throw new EJBException("No EJBLocalHome available (BUG!)");
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
//DEBUG            Logger.debug(e);
            return true;
         }
      }
       
      public void setRollbackOnly() 
      { 
         try {
            con.getTransactionManager().setRollbackOnly();
         } catch (IllegalStateException e) {
         } catch (SystemException e) {
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
         if (principal == null)
            return false;

         // Map the role name used by Bean Provider to the security role
         // link in the deployment descriptor. The EJB 1.1 spec requires
         // the security role refs in the descriptor but for backward
         // compability we're not enforcing this requirement.
         //
         // TODO (2.3): add a conditional check using jboss.xml <secure> element
         //             which will throw an exception in case no matching
         //             security ref is found.           
         Iterator it = getContainer().getBeanMetaData().getSecurityRoleReferences();
         boolean matchFound = false;
         
         while (it.hasNext()) {
             SecurityRoleRefMetaData meta = (SecurityRoleRefMetaData)it.next();
             if (meta.getName().equals(id)) {
                 id = meta.getLink();                 
                 matchFound = true;
                 
                 break;
             }
         }

         if (!matchFound)
             Logger.warning("WARNING: no match found for security role " + id + " in the deployment descriptor.");
             
         HashSet set = new HashSet();
         set.add( id );
         
         return con.getRealmMapping().doesUserHaveRole( principal, set );
       }
   
      // TODO - how to handle this best?
      public UserTransaction getUserTransaction() 
       { 
         return new UserTransactionImpl(); 
       }
   }
   
    // Inner classes -------------------------------------------------
   
   
   // SA MF FIXME: the usertransaction is only used for session beans with BMT.
   // This does not belong here (getUserTransaction is properly implemented in subclasses)
   
   class UserTransactionImpl
      implements UserTransaction
   {
      public void begin()
         throws NotSupportedException,SystemException
      {
         con.getTransactionManager().begin();
        
        // keep track of the transaction in enterprise context for BMT
        setTransaction(con.getTransactionManager().getTransaction());        
        
        
        // DEBUG Logger.debug("UserTransactionImpl.begin " + transaction.hashCode() + " in UserTransactionImpl " + this.hashCode());
//DEBUG        Logger.debug("UserTransactionImpl.begin " + transaction.hashCode() + " in UserTransactionImpl " + this.hashCode());
        
      }
      
      public void commit()
            throws RollbackException,
                   HeuristicMixedException,
                   HeuristicRollbackException,
                   java.lang.SecurityException,
                   java.lang.IllegalStateException,
                   SystemException
      {
//DEBUG        Logger.debug("UserTransactionImpl.commit " + transaction.hashCode() + " in UserTransactionImpl " + this.hashCode());
        
        con.getTransactionManager().commit();
      }
       
      public void rollback()
              throws java.lang.IllegalStateException,
                     java.lang.SecurityException,
                     SystemException
      {
//DEBUG        Logger.debug("UserTransactionImpl.rollback " + transaction.hashCode() + " in UserTransactionImpl " + this.hashCode());
        
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
