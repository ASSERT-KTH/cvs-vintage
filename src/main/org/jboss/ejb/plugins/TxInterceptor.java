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
import org.jboss.metadata.*;
import org.jboss.metadata.ejbjar.EJBMethod;

/**
*   <description>
*
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.9 $
*/
public class TxInterceptor
extends AbstractInterceptor
{
	
	// Attributes ----------------------------------------------------
	private TxManager tm;
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
		tm = (TxManager) getContainer().getTransactionManager();
		
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
		
		// Old transaction is the transaction that comes with the MI
		Transaction oldTransaction = mi.getTransaction();
	    // New transaction is the new transaction this might start
		Transaction newTransaction = null;
		
//DEBUG	System.out.println("Current transaction in MI is "+mi.getTransaction()); 
		           
		byte transType = getTransactionMethod(mi.getMethod(), runner);

// DEBUG  	printMethod(mi.getMethod(), transType);
		
		switch (transType) {
			
			case EJBMethod.TX_NOT_SUPPORTED: {
				
				
				// Thread arriving must be clean (jboss doesn't set the thread previously)
				// But for robustness purposes we disassociate the thread 
				tm.disassociateThread();
				
				// Do not set a transaction on the thread even if in MI, just run
				return runner.run();
				
				// We don't have to do anything since we don't deal with transactions
			}
		
				
			case EJBMethod.TX_REQUIRED:      {
					
				if (oldTransaction == null) { // No tx running
						
					//DEBUG               Logger.debug("Begin tx");
						
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
				
					return runner.run();
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
					
					// Only do something if we started the transaction
					if (newTransaction != null) {
						
						// Marked rollback
						if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
					
							// actually roll it back 
						 	newTransaction.rollback();
						}
					
						//Still running
						else if(newTransaction.getStatus() == Status.STATUS_ACTIVE) {
							
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
				
			case EJBMethod.TX_SUPPORTS: {
					
				if (oldTransaction != null) { // We have a tx propagated
						
					// Associate it with the thread
					tm.associateThread(oldTransaction);
				}
				
				// If we don't have a tx propagated we don't do anything
					
				// Continue invocation
				try	{
				
					return runner.run();
				} 
				catch (RuntimeException e) {
						
					throw new ServerException("Exception occurred", e);
				} 
				catch (Error e) {
				
					throw new ServerException("Exception occurred:"+e.getMessage());
				} 				
				
				// Even on error we don't do anything with the tx, we didn't start it
				
			}
				
			case EJBMethod.TX_REQUIRES_NEW: {
					
				// Always begin a transaction 
				tm.begin();
				
				// get it
				newTransaction = tm.getTransaction();
					
				// Set it on the method invocation
				mi.setTransaction(newTransaction);
				
				// Continue invocation
				try {
					
					return runner.run();
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
				
			case EJBMethod.TX_MANDATORY: {
				
				if (oldTransaction == null) { // no transaction = bad! 
					
					throw new TransactionRequiredException("read the spec!");
				} 
				else {
						
					// Associate it with the thread
					tm.associateThread(oldTransaction);
						
					// That's it
					return runner.run();
				}
			}
				
			case EJBMethod.TX_NEVER: {
				
				if (oldTransaction != null) { // Transaction = bad!
						
					throw new RemoteException("Transaction not allowed");
				} 
				else {
						
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
			System.out.println("Found metadata for bean '"+bmd.getName()+"'"+" method is "+m);
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
//DEBUG			System.out.println("Calling the next invoker in runInvoke");
			return getNext().invoke(mi);
		}
	}
	
	class RunInvokeHome implements Doable {
		MethodInvocation mi;
		public Object  run() throws Exception {
			
//DEBUG		System.out.println("Calling the next invoker in runInvokeHome");
			return getNext().invokeHome(mi);
		}
	}
}
