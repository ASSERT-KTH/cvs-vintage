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
import java.util.*;

import javax.swing.tree.*;

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
import org.jboss.metadata.*;
import org.jboss.metadata.ejbjar.EJBMethod;

/**
 *   <description>
 *
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.7 $
 */
public class TxInterceptor
   extends AbstractInterceptor
{

   // Attributes ----------------------------------------------------
   private TransactionManager tm;
	private HashMap methodTx = new HashMap();

	protected Container container;
    private RunInvoke invoker = new RunInvoke();
    private RunInvokeHome invokeHomer = new RunInvokeHome();

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
//      return getNext().invokeHome(mi);
        invokeHomer.mi = mi;
        return runWithTransactions(invokeHomer, mi);
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
    public Object invoke(MethodInvocation mi) throws Exception {
        invoker.mi = mi;
        return runWithTransactions(invoker, mi);
    }

    private void printMethod(Method m, byte type) {
        String name;
        switch(type) {
            case EJBMethod.TX_MANDATORY:
                name = "TX_MANDATORY";
                break;
            case EJBMethod.TX_NEVER:
                name = "TX_NEVER";
                break;
            case EJBMethod.TX_NOT_SUPPORTED:
                name = "TX_NOT_SUPPORTED";
                break;
            case EJBMethod.TX_REQUIRED:
                name = "TX_REQUIRED";
                break;
            case EJBMethod.TX_REQUIRES_NEW:
                name = "TX_REQUIRES_NEW";
                break;
            case EJBMethod.TX_SUPPORTS:
                name = "TX_SUPPORTS";
                break;
            default:
                name = "TX_UNKNOWN";
        }
        Logger.debug(name+" for "+m.getName());
    }

    private Object runWithTransactions(Doable runner, MethodInvocation mi) throws Exception {
      Transaction current = mi.getTransaction();

      byte transType = getTransactionMethod(mi.getMethod(), runner);
      printMethod(mi.getMethod(), transType);
      switch (transType)
      {
         case EJBMethod.TX_NOT_SUPPORTED:
         {
            if (current.getStatus() != Status.STATUS_NO_TRANSACTION)
            {
               // Suspend tx
               getContainer().getTransactionManager().suspend();

               try
               {
                  return runner.run();
               } finally
               {
                  // Resume tx
                  getContainer().getTransactionManager().resume(current);
               }
            } else
            {
               return runner.run();
            }
         }

         case EJBMethod.TX_REQUIRED:
         {
            Transaction tx = current;

            if (current.getStatus() == Status.STATUS_NO_TRANSACTION)
            {
               // No tx running
               // Create tx
//DEBUG               Logger.debug("Begin tx");
               getContainer().getTransactionManager().begin();
               tx = getContainer().getTransactionManager().getTransaction();
               mi.setTransaction(tx);
            }

            // Continue invocation
            try
            {
               return runner.run();
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
               if(!tx.equals(current))
                mi.setTransaction(current);
            }
         }

         case EJBMethod.TX_SUPPORTS:
         {

				// This mode doesn't really do anything
				// If tx started -> do nothing
				// If tx not started -> do nothing

	         // Continue invocation
            return runner.run();
         }

         case EJBMethod.TX_REQUIRES_NEW:
         {

            // Always begin new tx
//            Logger.debug("Begin tx");
            getContainer().getTransactionManager().begin();
				mi.setTransaction(getContainer().getTransactionManager().getTransaction());

	         // Continue invocation
	         try
	         {
	            return runner.run();
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

         case EJBMethod.TX_MANDATORY:
         {
	         if (current.getStatus() == Status.STATUS_NO_TRANSACTION)
	         {
					throw new TransactionRequiredException();
	         } else
	         {
	            return runner.run();
	         }
         }

         case EJBMethod.TX_NEVER:
         {
	         if (current.getStatus() == Status.STATUS_ACTIVE)
	         {
	         	throw new RemoteException("Transaction not allowed");
	         } else
	         {
	            return runner.run();
	         }
         }
      }

      return null;
   }

   // Protected  ----------------------------------------------------

   // This should be cached, since this method is called very often
    protected byte getTransactionMethod(Method m, Doable d) {
        Byte b = (Byte)methodTx.get(m);
        if(b != null) return b.byteValue();

        try {
            BeanMetaData bmd = container.getBeanMetaData();
//System.out.println("Found metadata for bean '"+bmd.getName()+"'");
            try {
                MethodMetaData mmd;
                if(d == invoker)
                    mmd = bmd.getMethod(m.getName(), m.getParameterTypes());
                else
                    mmd = bmd.getHomeMethod(m.getName(), m.getParameterTypes());
//System.out.println("Found metadata for method '"+mmd.getName()+"'");
                byte result = ((Byte)mmd.getProperty("transactionAttribute")).byteValue();
                methodTx.put(m, new Byte(result));
                return result;
            } catch(IllegalArgumentException e2) {
                try {
                    MethodMetaData mmd;
                    if(d == invoker)
                        mmd = bmd.getMethod("*", new Class[0]);
                    else
                        mmd = bmd.getHomeMethod("*", new Class[0]);
//System.out.println("Found metadata for '*'");
                    byte result = ((Byte)mmd.getProperty("transactionAttribute")).byteValue();
                    methodTx.put(m, new Byte(result));
                    return result;
                } catch(IllegalArgumentException e3) {
//System.out.println("Couldn't find method metadata for "+m+" in "+bmd.getName());
                }
            }
        } catch(IllegalArgumentException e) {
            System.out.println("Couldn't find bean '"+container.getMetaData().getEjbName()+"'");
        }
        methodTx.put(m, new Byte(EJBMethod.TX_SUPPORTS));
        return EJBMethod.TX_SUPPORTS;
    }
    // Inner classes -------------------------------------------------
    interface Doable {
        public Object run() throws Exception;
    }

    class RunInvoke implements Doable {
        MethodInvocation mi;
        public Object run() throws Exception {
            return getNext().invoke(mi);
        }
    }

    class RunInvokeHome implements Doable {
        MethodInvocation mi;
        public Object  run() throws Exception {
            return getNext().invokeHome(mi);
        }
    }
}
