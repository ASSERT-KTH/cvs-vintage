/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionRequiredException;

import org.jboss.invocation.Invocation;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.BeanMetaData;

/**
 *  This interceptor handles transactions for CMT beans.
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *  @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.23 $
 */
public class TxInterceptorCMT
extends AbstractTxInterceptor
{
   
   // Attributes ----------------------------------------------------
   
   /** A cache mapping methods to transaction attributes. */
   private HashMap methodTx = new HashMap();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(Invocation mi)
   throws Exception
   {
      return runWithTransactions(false, mi);
   }
   
   /**
    *  This method does invocation interpositioning of tx management
    */
   public Object invoke(Invocation mi)
   throws Exception
   {
      return runWithTransactions(true, mi);
   }
   
   // Private  ------------------------------------------------------
   
   private void printMethod(Method m, byte type)
   {
      String name;
      switch(type)
      {
         case MetaData.TX_MANDATORY:
            name = "TX_MANDATORY";
            break;
         case MetaData.TX_NEVER:
            name = "TX_NEVER";
            break;
         case MetaData.TX_NOT_SUPPORTED:
            name = "TX_NOT_SUPPORTED";
            break;
         case MetaData.TX_REQUIRED:
            name = "TX_REQUIRED";
            break;
         case MetaData.TX_REQUIRES_NEW:
            name = "TX_REQUIRES_NEW";
            break;
         case MetaData.TX_SUPPORTS:
            name = "TX_SUPPORTS";
            break;
         default:
            name = "TX_UNKNOWN";
      }
      if (log.isTraceEnabled())
         log.trace(name+" for "+m.getName());
   }
   
    /*
     *  This method does invocation interpositioning of tx management.
     *
     *  This is where the meat is.  We define what to do with the Tx based
     *  on the declaration.
     *  The Invocation is always the final authority on what the Tx
     *  looks like leaving this interceptor.  In other words, interceptors
     *  down the chain should not rely on the thread association with Tx but
     *  on the Tx present in the Invocation.
     *
     *  @param remoteInvocation If <code>true</code> this is an invocation
     *                          of a method in the remote interface, otherwise
     *                          it is an invocation of a method in the home
     *                          interface.
     *  @param mi The <code>Invocation</code> of this call.
     */
   private Object runWithTransactions(boolean remoteInvocation,
      Invocation mi)
      throws Exception
   {
      // Old transaction is the transaction that comes with the MI
      Transaction oldTransaction = mi.getTransaction();
      // New transaction is the new transaction this might start
      Transaction newTransaction = null;
      
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Current transaction in MI is " + oldTransaction);
      
      byte transType = getTransactionMethod(mi.getMethod(), remoteInvocation);
      if( trace )
         printMethod(mi.getMethod(), transType);

      // Thread arriving must be clean (jboss doesn't set the thread
      // previously). However optimized calls come with associated
      // thread for example. We suspend the thread association here, and
      // resume in the finally block of the following try.
      Transaction threadTx = tm.suspend();
      if( trace )
         log.trace("Thread came in with tx " + threadTx);
      try
      {
         switch (transType)
         {
            case MetaData.TX_NOT_SUPPORTED:
               // Do not set a transaction on the thread even if in MI, just run
               return invokeNext(remoteInvocation, mi, false);
               
            case MetaData.TX_REQUIRED:
               if (oldTransaction == null)
               { // No tx running
                  // Create tx
                  tm.begin();
                  
                  // get the tx
                  newTransaction = tm.getTransaction();
                  if( trace )
                     log.trace("Starting new tx " + newTransaction);
                  
                  // Let the method invocation know
                  mi.setTransaction(newTransaction);
               }
               else
               { // We have a tx propagated
                  // Associate it with the thread
                  tm.resume(oldTransaction);
               }

               // Continue invocation
               try
               {
                  return invokeNext(remoteInvocation, mi, newTransaction != null);
               }
               finally
               {
                  if( trace )
                     log.trace("TxInterceptorCMT: In finally");

                  // Only do something if we started the transaction
                  if (newTransaction != null)
                  {
                     // Marked rollback
                     if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                     {
                        newTransaction.rollback();
                     }
                     else
                     {
                        // Commit tx
                        // This will happen if
                        // a) everything goes well
                        // b) app. exception was thrown
                        if( trace )
                           log.trace("TxInterceptorCMT:before commit of " + newTransaction);
                        newTransaction.commit();
                        if( trace )
                           log.trace("TxInterceptorCMT:after commit of " + newTransaction);
                     }
                     
                     // reassociate the oldTransaction with the Invocation (even null)
                     mi.setTransaction(oldTransaction);
                  }
                  // Always drop thread association even if committing or
                  // rolling back the newTransaction because not all TMs
                  // will drop thread associations when commit() or rollback()
                  // are called through tx itself (see JTA spec that seems to
                  // indicate that thread assoc is required to be dropped only
                  // when commit() and rollback() are called through TransactionManager
                  // interface)
                  tm.suspend();
                  
               }
               
            case MetaData.TX_SUPPORTS:
            {
               // Associate old transaction with the thread
               // Some TMs cannot resume a null transaction and will throw
               // an exception (e.g. Tyrex), so make sure it is not null
               if (oldTransaction != null)
                  tm.resume(oldTransaction);
               
               try
               {
                  return invokeNext(remoteInvocation, mi, false);
               }
               finally
               {
                  tm.suspend();
               }
               
               // Even on error we don't do anything with the tx, we didn't start it
            }
            
            case MetaData.TX_REQUIRES_NEW:
            {
               // Always begin a transaction
               tm.begin();
               
               // get it
               newTransaction = tm.getTransaction();
               
               // Set it on the method invocation
               mi.setTransaction(newTransaction);
               // Continue invocation
               try
               {
                  return invokeNext(remoteInvocation, mi, true);
               }
               finally
               {
                  // We started the transaction for sure so we commit or roll back
                  
                  if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                  {
                     newTransaction.rollback();
                  }
                  else
                  {
                     // Commit tx
                     // This will happen if
                     // a) everything goes well
                     // b) app. exception was thrown
                     newTransaction.commit();
                  }
                  
                  // set the old transaction back on the method invocation
                  mi.setTransaction(oldTransaction);
                  tm.suspend();
               }
            }
            case MetaData.TX_MANDATORY:
               if (oldTransaction == null) // no transaction = bad!
                  throw new TransactionRequiredException("Transaction Required, read the spec!");
               // Associate it with the thread
               tm.resume(oldTransaction);
               try
               {
                  return invokeNext(remoteInvocation, mi, false);
               } finally
               {
                  tm.suspend();
               }
               
            case MetaData.TX_NEVER:
               if (oldTransaction != null) // Transaction = bad!
                  throw new RemoteException("Transaction not allowed");
               return invokeNext(remoteInvocation, mi, false);
         }
         
      }
      finally
      {
         // IN case we had a Tx associated with the thread reassociate
         if (threadTx != null)
            tm.resume(threadTx);
      }

      return null;
   }
   
   // Protected  ----------------------------------------------------
   
   // This should be cached, since this method is called very often
   protected byte getTransactionMethod(Method m, boolean remoteInvocation)
   {
      Byte b = (Byte)methodTx.get(m);
      if (b != null) return b.byteValue();
      
      BeanMetaData bmd = container.getBeanMetaData();
      
      //DEBUG        log.debug("Found metadata for bean '"+bmd.getEjbName()+"'"+" method is "+m.getName());
      
      byte result = bmd.getMethodTransactionType(m.getName(), m.getParameterTypes(), remoteInvocation);
      
      // provide default if method is not found in descriptor
      if (result == MetaData.TX_UNKNOWN) result = MetaData.TX_REQUIRED;
      
      methodTx.put(m, new Byte(result));
      return result;
   }
   
   // Inner classes -------------------------------------------------
   
   // Monitorable implementation ------------------------------------
   public void sample(Object s)
   {
      // Just here to because Monitorable request it but will be removed soon
   }
   public Map retrieveStatistic()
   {
      return null;
   }
   public void resetStatistic()
   {
   }
}
