/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.SystemException;

import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.6 $
 */
public class TxInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
   public static final int TX_NOT_SUPPORTED  = 0;
   public static final int TX_REQUIRED       = 1;
   public static final int TX_SUPPORTS       = 2;
   public static final int TX_REQUIRES_NEW   = 3;
   public static final int TX_MANDATORY      = 4;
   public static final int TX_NEVER          = 5;
    
   // Attributes ----------------------------------------------------
   private TransactionManager tm;
	private HashMap methodTx = new HashMap();
	
	protected Container container;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container) 
   { 
   	this.container = container; 
   }

   public  Container getContainer()
   {
   	return container;
   }

   // Interceptor implementation --------------------------------------
	public void init()
		throws Exception
	{
		// Store TM reference locally
		tm = getContainer().getTransactionManager();
	
		// Find out method->tx-type mappings from meta-info
//		EnterpriseBean eb = getContainer.getMetaData();
//		eb.getBeanContext()
	}
	
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      // TODO
      return getNext().invokeHome(mi);
   }

   /**
    *   This method does invocation interpositioning of tx management
    *
    * @param   id  
    * @param   m  
    * @param   args  
    * @return     
    * @exception   Exception  
    */
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      Transaction current = mi.getTransaction();
      
      switch (getTransactionMethod(mi.getMethod()))
      {
         case TX_NOT_SUPPORTED:
         {
//DEBUG            Logger.debug("TX_NOT_SUPPORTED");
            if (current.getStatus() != Status.STATUS_NO_TRANSACTION)
            {
               // Suspend tx
               getContainer().getTransactionManager().suspend();
               
               try
               {
                  return getNext().invoke(mi);
               } finally
               {
                  // Resume tx
                  getContainer().getTransactionManager().resume(current);
               }
            } else
            {
               return getNext().invoke(mi);
            }
         }
         
         case TX_REQUIRED:
         {
//DEBUG            Logger.debug("TX_REQUIRED");
            Transaction tx = current;
            
            if (current.getStatus() == Status.STATUS_NO_TRANSACTION)
            {
               // No tx running
               // Create tx
//DEBUG               Logger.debug("Begin tx");
               getContainer().getTransactionManager().begin();
               mi.setTransaction(getContainer().getTransactionManager().getTransaction());
            } 
            
            // Continue invocation
            try
            {
               return getNext().invoke(mi);
            } catch (RemoteException e)
            {
					if (!tx.equals(current))
					{
						tx.rollback();
					}
               throw e;
            } catch (RuntimeException e)
            {
            	if (!tx.equals(current))
            	{
            		tx.rollback();
            	}
               throw new ServerException("Exception occurred", e);
            } catch (Error e)
            {
            	if (!tx.equals(current))
            	{
            		tx.rollback();
            	}
               throw new ServerException("Exception occurred:"+e.getMessage());
            } finally
            {
               if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
               {
                  tx.rollback();
               }
               else if (current.getStatus() == Status.STATUS_NO_TRANSACTION && 
                        tx.getStatus() == Status.STATUS_ACTIVE)
               {
						// Commit tx
						// This will happen if
						// a) everything goes well
						// b) app. exception was thrown
                  tx.commit();
               }
            }
         }
         
         case TX_SUPPORTS:
         {
//DEBUG	         Logger.debug("TX_SUPPORTS");
	         
				// This mode doesn't really do anything
				// If tx started -> do nothing
				// If tx not started -> do nothing
				
	         // Continue invocation
            return getNext().invoke(mi);
         }
         
         case TX_REQUIRES_NEW:
         {
	         Logger.debug("TX_REQUIRES_NEW");
	         
            // Always begin new tx
            Logger.debug("Begin tx");
            getContainer().getTransactionManager().begin();
				mi.setTransaction(getContainer().getTransactionManager().getTransaction());
	         
	         // Continue invocation
	         try
	         {
	            return getNext().invoke(mi);
	         } catch (RemoteException e)
	         {
	        		getContainer().getTransactionManager().rollback();
	            throw e;
	         } catch (RuntimeException e)
	         {
         		getContainer().getTransactionManager().rollback();
	            throw new ServerException("Exception occurred", e);
	         } catch (Error e)
	         {
         		getContainer().getTransactionManager().rollback();
	            throw new ServerException("Exception occurred:"+e.getMessage());
	         } finally
	         {
	            if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK)
	            {
	               tm.rollback();
	            }
	            else
	            {
	         		// Commit tx
	         		// This will happen if
	         		// a) everything goes well
	         		// b) app. exception was thrown
	               tm.commit();
	            }
	         }
         }
         
         case TX_MANDATORY:
         {
//DEBUG	         Logger.debug("TX_MANDATORY");
	         if (current.getStatus() == Status.STATUS_NO_TRANSACTION)
	         {
					throw new TransactionRequiredException();
	         } else
	         {
	            return getNext().invoke(mi);
	         }
         }
         
         case TX_NEVER:
         {
//DEBUG	         Logger.debug("TX_NEVER");
	         if (current.getStatus() == Status.STATUS_ACTIVE)
	         {
	         	throw new RemoteException("Transaction not allowed");
	         } else
	         {
	            return getNext().invoke(mi);
	         }
         }
      }
      
      return null;
   }
   
   // Protected  ----------------------------------------------------
   
   // This should be cached, since this method is called very often
   protected int getTransactionMethod(Method m)
   {
      return TX_SUPPORTS; // TODO: find out transaction method
   }
   // Inner classes -------------------------------------------------
}
