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
import org.jboss.tm.TxManager;
import org.jboss.logging.Logger;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.MethodMetaData;

/**
*   <description>
*
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.14 $
*/
public class TxInterceptor
extends AbstractInterceptor
{
    
    // Attributes ----------------------------------------------------
    private TxManager tm;
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
       tm = (TxManager) getContainer().getTransactionManager();
       
       // Find out method->tx-type mappings from meta-info
       //		EnterpriseBean eb = getContainer.getMetaData();
       //		eb.getBeanContext()
    }
    
    public Object invokeHome(MethodInvocation mi)
    throws Exception
    {
       return runWithTransactions(false, mi);
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
       return runWithTransactions(true, mi);
    }
    
    private void printMethod(Method m, byte type) {
       String name;
       switch(type) {
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
       Logger.debug(name+" for "+m.getName());
    }
    
    private Object invokeNext(boolean remoteInvocation, MethodInvocation mi) throws Exception {
       if (remoteInvocation) {
         return getNext().invoke(mi);
       } else {
         return getNext().invokeHome(mi);
       }
    }
    
    
    private Object runWithTransactions(boolean remoteInvocation, MethodInvocation mi) throws Exception {
       
       // Old transaction is the transaction that comes with the MI
       Transaction oldTransaction = mi.getTransaction();
        // New transaction is the new transaction this might start
       Transaction newTransaction = null;
       
//DEBUG       Logger.log("Current transaction in MI is "+mi.getTransaction()); 
//DEBUG       Logger.log("Current method "+mi.getMethod().getName());           
       byte transType = getTransactionMethod(mi.getMethod(), remoteInvocation);

   	printMethod(mi.getMethod(), transType);
       
       switch (transType) {
         
         case MetaData.TX_NOT_SUPPORTED: {
          
          
          // Thread arriving must be clean (jboss doesn't set the thread previously)
          // But for robustness purposes we disassociate the thread 
          tm.disassociateThread();
          
          // Do not set a transaction on the thread even if in MI, just run
          return invokeNext(remoteInvocation,mi );
          
          // We don't have to do anything since we don't deal with transactions
         }
       
          
         case MetaData.TX_REQUIRED:      {
              
          if (oldTransaction == null) { // No tx running
                 
              Logger.debug("Begin tx");
                 
              // Create tx 
              tm.begin();
                 
              // get the tx
              newTransaction = tm.getTransaction();
                 
              // Let the method invocation know
              mi.setTransaction(newTransaction);            
          }
              
          else { // We have a tx propagated
                 
              // Associate it with the thread
              tm.associateThread(oldTransaction);
          }
              
          // Continue invocation
          try	{
          
              return invokeNext(remoteInvocation,mi );
          } 
          catch (RemoteException e) {
              
              if (newTransaction != null) {
                   
                 //We started it, 
                 newTransaction.rollback();
              }
                 
              throw e;
          } 
          catch (RuntimeException e) {
                 
              if (newTransaction != null) {
                   
                 // We started it
                 newTransaction.rollback();
              }
              
              throw new ServerException("Exception occurred", e);
          } 
          catch (Error e) {
          
              if (newTransaction != null) {
                   
                 // we started it
                 newTransaction.rollback();
              }
              throw new ServerException("Exception occurred:"+e.getMessage());
          } 
          
          finally {
            
			 //DEBUG Logger.log("In finally");
			  
              // Only do something if we started the transaction
              if (newTransaction != null) {
                 
                 // Marked rollback
                 if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
              
			  		Logger.log("rolling back");	
                   // actually roll it back 
                   newTransaction.rollback();
                 }
              
                 //Still running
                 else if(newTransaction.getStatus() == Status.STATUS_ACTIVE) {
                   
				   Logger.log("commiting");
                   // Commit tx
                   // This will happen if
                   // a) everything goes well
                   // b) app. exception was thrown
                   newTransaction.commit();
                 }
                 
                 // reassociate the oldTransaction with the methodInvocation (even null)
                 mi.setTransaction(oldTransaction);
              }
          	}
         }
          
         case MetaData.TX_SUPPORTS: {
              
          if (oldTransaction != null) { // We have a tx propagated
                 
              // Associate it with the thread
              tm.associateThread(oldTransaction);
          }
          
          // If we don't have a tx propagated we don't do anything
              
          // Continue invocation
          try	{
          
              return invokeNext(remoteInvocation,mi );
          } 
          catch (RuntimeException e) {
                 
              throw new ServerException("Exception occurred", e);
          } 
          catch (Error e) {
          
              throw new ServerException("Exception occurred:"+e.getMessage());
          } 				
          
          // Even on error we don't do anything with the tx, we didn't start it
          
         }
          
         case MetaData.TX_REQUIRES_NEW: {
              
          // Always begin a transaction 
          tm.begin();
          
          // get it
          newTransaction = tm.getTransaction();
              
          // Set it on the method invocation
          mi.setTransaction(newTransaction);
          
          // Continue invocation
          try {
              
              return invokeNext(remoteInvocation,mi );
          } 
          catch (RemoteException e) {
              
              // We started it for sure
              newTransaction.rollback();
              
              throw e;
           } 
          catch (RuntimeException e) {
              
              // We started it for sure
              newTransaction.rollback();
              
              throw new ServerException("Exception occurred", e);
          } 
          catch (Error e) {
              
              // We started it for sure
              newTransaction.rollback();
              
              throw new ServerException("Exception occurred:"+e.getMessage());
          } 
          finally {
              
              // We started the transaction for sure so we commit or roll back
       
              if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                 
                 newTransaction.rollback();
              }
              else {
                 
                 // Commit tx
                 // This will happen if
                 // a) everything goes well
                 // b) app. exception was thrown
                 newTransaction.commit();
              }
              
              // set the old transaction back on the method invocation
              mi.setTransaction(oldTransaction);
          }	
         }
          
         case MetaData.TX_MANDATORY: {
          
          if (oldTransaction == null) { // no transaction = bad! 
              
              throw new TransactionRequiredException("read the spec!");
          } 
          else {
                 
              // Associate it with the thread
              tm.associateThread(oldTransaction);
                 
              // That's it
              return invokeNext(remoteInvocation,mi );
          }
         }
          
         case MetaData.TX_NEVER: {
                                             
          if (oldTransaction != null) { // Transaction = bad!
                 
              throw new RemoteException("Transaction not allowed");
          } 
          else {
                 
              return invokeNext(remoteInvocation,mi );
          }
         }
       }
       
       return null;
    }
    
    // Protected  ----------------------------------------------------
    
    // This should be cached, since this method is called very often
    protected byte getTransactionMethod(Method m, boolean remoteInvocation) {
       Byte b = (Byte)methodTx.get(m);
       if(b != null) return b.byteValue();
         
       BeanMetaData bmd = container.getBeanMetaData();
       //DEBUG Logger.log("Found metadata for bean '"+bmd.getEjbName()+"'"+" method is "+m.getName());
       byte result = bmd.getMethodTransactionType(m.getName(), m.getParameterTypes(), remoteInvocation);
       
       // provide default if method is not found in descriptor 
       if (result == MetaData.TX_UNKNOWN) result = MetaData.TX_REQUIRED;
       
       methodTx.put(m, new Byte(result));
       return result;
    }
    
    // Inner classes -------------------------------------------------
    
}
