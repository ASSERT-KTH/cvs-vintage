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
import javax.transaction.UserTransaction;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;

import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.tm.TxManager;
import org.jboss.logging.Logger;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.MethodMetaData;

/**
*   <description>
*
*   @see <related>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @version $Revision: 1.3 $
*/
public class TxInterceptorBMT
extends AbstractInterceptor
{
    
    // Attributes ----------------------------------------------------
    private TxManager tm;
    
    // lookup on java:comp/UserTransaction should be redirected to
    //   sessionContext.getUserTransaction()
	// The ThreadLocal associates the thread to the UserTransaction	
	ThreadLocal userTransaction = new ThreadLocal(); 
    
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
        
    	// bind java:comp/UserTransaction
		RefAddr refAddr = new RefAddr("userTransaction") {
        	public Object getContent() {
                return userTransaction;
			}
		};
		
		Reference ref = new Reference("javax.transaction.UserTransaction", 
									  refAddr, 
									  new UserTxFactory().getClass().getName(), 
									  null);
		((Context)new InitialContext().lookup("java:comp/")).bind("UserTransaction", ref);
			
	}

    public Object invokeHome(MethodInvocation mi)
    throws Exception
    {
        // set the threadlocal to the userTransaction of the instance
		// (mi has the sessioncontext from the previous interceptor)
		if (((SessionMetaData)container.getBeanMetaData()).isStateful()) {
			
			// Save old userTx
			Object oldUserTx = userTransaction.get();
			
			// retrieve the real userTransaction
			userTransaction.set(((StatefulSessionEnterpriseContext)mi.getEnterpriseContext()).getSessionContext().getUserTransaction());
		
			// t1 refers to the client transaction (spec ejb1.1, 11.6.1, p174)
			// this is necessary for optimized (inVM) calls: threads come associated with the client transaction
			Transaction t1 = tm.disassociateThread();
			
			// t2 refers to the instance transaction (spec ejb1.1, 11.6.1, p174) 
			Transaction t2 = mi.getEnterpriseContext().getTransaction();
			
			try {
				
				if (t2 != null) {
					
					// associate the transaction to the thread
					tm.associateThread(t2);
				
				}
				
				return getNext().invokeHome(mi);
			
			} finally {
				
				// Reset user Tx
				userTransaction.set(oldUserTx);
				
				if (t1 != null) {
					
					// reassociate the previous transaction before returning
					tm.associateThread(t1);
				
				}
			}
		} else {
			
			// stateless: no context, no transaction, no call to the instance
			
			return getNext().invokeHome(mi);
		}
        
        
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

		// Store old UserTX
        Object oldUserTx = userTransaction.get();
		
        // set the threadlocal to the userTransaction of the instance
		// (mi has the sessioncontext from the previous interceptor)
		if (((SessionMetaData)container.getBeanMetaData()).isStateful()) {
			
			// retrieve the real userTransaction
			userTransaction.set(((StatefulSessionEnterpriseContext)mi.getEnterpriseContext()).getSessionContext().getUserTransaction());
		
		} else {
			
			// retrieve the real userTransaction
			userTransaction.set(((StatelessSessionEnterpriseContext)mi.getEnterpriseContext()).getSessionContext().getUserTransaction());
		}
        
		
        // t1 refers to the client transaction (spec ejb1.1, 11.6.1, p174)
		// this is necessary for optimized (inVM) calls: threads come associated with the client transaction
        Transaction t1 = tm.disassociateThread();
		
		// DEBUG Logger.debug("TxInterceptorBMT disassociate" + ((t1==null) ? "null": Integer.toString(t1.hashCode())));
		Logger.debug("TxInterceptorBMT disassociate" + ((t1==null) ? "null": Integer.toString(t1.hashCode())));
		
        // t2 refers to the instance transaction (spec ejb1.1, 11.6.1, p174) 
        Transaction t2 = mi.getEnterpriseContext().getTransaction();
        
		// DEBUG Logger.debug("TxInterceptorBMT t2 in context" + ((t2==null) ? "null": Integer.toString(t2.hashCode())));
		Logger.debug("TxInterceptorBMT t2 in context" + ((t2==null) ? "null": Integer.toString(t2.hashCode())));
		
        try {
			
			if (t2 != null) {
				
				// associate the transaction to the thread
				tm.associateThread(t2);
								
			}
			
			return getNext().invoke(mi);
			
		} finally {
			
			// Reset user Tx
			userTransaction.set(oldUserTx);
			
			if (t1 != null) {
				
				// DEBUG Logger.debug("TxInterceptorBMT reassociating client tx " + t1.hashCode());
				Logger.debug("TxInterceptorBMT reassociating client tx " + t1.hashCode());
				
				// reassociate the previous transaction before returning
				tm.associateThread(t1);
				
			}
			
			if (((SessionMetaData)container.getBeanMetaData()).isStateless()) {

				// t3 is the transaction associated with the context at the end of the call
				Transaction t3 = mi.getEnterpriseContext().getTransaction();
				
				Logger.debug("in TxIntBMT " + t3);
				
				// for a stateless sessionbean the transaction should be completed at the end of the call
			    if (t3 != null) switch (t3.getStatus()) {
					case Status.STATUS_ACTIVE: 
          			case Status.STATUS_COMMITTING: 
          			case Status.STATUS_MARKED_ROLLBACK: 
          			case Status.STATUS_PREPARING: 
          			case Status.STATUS_ROLLING_BACK:
					    
						t3.rollback();
					
					case Status.STATUS_PREPARED: 
          			
						// cf ejb1.1 11.6.1
						Logger.debug("Application error: CMT stateless bean " + container.getBeanMetaData().getEjbName() + " should complete transactions before returning (ejb1.1 spec, 11.6.1)");
					    
						// the instance interceptor will discard the instance
						throw new RemoteException("Application error: CMT stateless bean " + container.getBeanMetaData().getEjbName() + " should complete transactions before returning (ejb1.1 spec, 11.6.1)");
				}
			}
		}
    }
    
    // Protected  ----------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
	public static class UserTxFactory implements ObjectFactory {
        public Object getObjectInstance(Object ref,
                                        Name name,
                                        Context nameCtx,
                                        Hashtable environment)
                                        throws Exception 
		{
			
            // the ref is a list with only one refAddr whose content is the threadlocal
			ThreadLocal threadLocal = (ThreadLocal)((Reference)ref).get(0).getContent();
			
			// the threadlocal holds the UserTransaction
			// we can now return the userTx, calls on it will indirect on the right context
			return threadLocal.get();
   
        }
    }
    
}
