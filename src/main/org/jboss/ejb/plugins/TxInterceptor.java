/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import javax.ejb.EJBException;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.logging.Logger;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
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
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation --------------------------------------
   public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      // TODO
      return getNext().invokeHome(method, args, ctx);
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
   public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      Transaction current = getContainer().getTransactionManager().getTransaction();
      
      switch (getTransactionMethod(method))
      {
         case TX_NOT_SUPPORTED:
         {
//            System.out.println("TX_NOT_SUPPORTED");
            if (current.getStatus() != Status.STATUS_NO_TRANSACTION)
            {
               // Suspend tx
               getContainer().getTransactionManager().suspend();
               
               try
               {
                  return getNext().invoke(id, method, args, ctx);
               } finally
               {
                  // Resume tx
                  getContainer().getTransactionManager().resume(current);
               }
            } else
            {
               return getNext().invoke(id, method, args, ctx);
            }
         }
         
         case TX_REQUIRED:
         {
            Logger.debug("TX_REQUIRED");
            Transaction tx = current;
            
            if (tx.getStatus() == Status.STATUS_NO_TRANSACTION)
            {
               // No tx running
               // Create tx
               Logger.debug("Begin tx");
               getContainer().getTransactionManager().begin();
               tx = getContainer().getTransactionManager().getTransaction();
            } 
            
            // Continue invocation
            try
            {
               return getNext().invoke(id, method, args, ctx);
            } catch (RemoteException e)
            {
					if (!tx.equals(current))
					{
						getContainer().getTransactionManager().rollback();
					}
               throw e;
            } catch (RuntimeException e)
            {
            	if (!tx.equals(current))
            	{
            		getContainer().getTransactionManager().rollback();
            	}
               throw new ServerException("Exception occurred", e);
            } catch (Error e)
            {
            	if (!tx.equals(current))
            	{
            		getContainer().getTransactionManager().rollback();
            	}
               throw new ServerException("Exception occurred", e);
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
         }
         
         case TX_REQUIRES_NEW:
         {
         }
         
         case TX_MANDATORY:
         {
         }
         
         case TX_NEVER:
         {
         }
      }
      
      return null;
   }
   
   // Protected  ----------------------------------------------------
   
   // This should be cached, since this method is called very often
   protected int getTransactionMethod(Method m)
   {
      return TX_REQUIRED; // TODO: find out transaction method
   }
   // Inner classes -------------------------------------------------
}
