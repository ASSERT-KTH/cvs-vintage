/*
* jBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import javax.ejb.EJBException;

import org.jboss.ejb.MessageDrivenEnterpriseContext;
import org.jboss.ejb.MethodInvocation;

import org.jboss.logging.Logger;
/**
*   <description>
*
*   Stolen from TxInterceptorBMP
*   @see <related>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
*   @version $Revision: 1.5 $
*/
public class MessageDrivenTxInterceptorBMT
extends TxInterceptorBMT
{


/**
    *   This method does invocation interpositioning of tx management
    *
    * MessageDriven specialication
    * @param   id
    * @param   m
    * @param   args
    * @return
    * @exception   Exception
    */
    public Object invoke(MethodInvocation mi) throws Exception {

        // Store old UserTX
        Object oldUserTx = userTransaction.get();
        
	
	
	// retrieve the real userTransaction
	userTransaction.set(((MessageDrivenEnterpriseContext)mi.getEnterpriseContext()).getMessageDrivenContext().getUserTransaction());
	
        
        
        // t1 refers to the client transaction (spec ejb1.1, 11.6.1, p174)
        // this is necessary for optimized (inVM) calls: threads come associated with the client transaction
        Transaction t1 = tm.disassociateThread();
        
	//DEBUG     Logger.debug("TxInterceptorBMT disassociate" + ((t1==null) ? "null": Integer.toString(t1.hashCode())));
	
        // t2 refers to the instance transaction (spec ejb1.1, 11.6.1, p174) 
        Transaction t2 = mi.getEnterpriseContext().getTransaction();
	
	// This is BMT so the transaction is dictated by the Bean, the MethodInvocation follows
	mi.setTransaction(t2);
        
	//DEBUG Logger.debug("TxInterceptorBMT t2 in context" + ((t2==null) ? "null": Integer.toString(t2.hashCode())));
        
        try {
        
        if (t2 != null) {
        
                // associate the transaction to the thread
	    tm.associateThread(t2);
        
        }
        
        return getNext().invoke(mi);
        
    } catch (RuntimeException e)
        {
            // EJB 2.0 17.3, table 16
            if (mi.getEnterpriseContext().getTransaction() != null) {
                try {
                        mi.getEnterpriseContext().getTransaction().setRollbackOnly();
                } catch (IllegalStateException ex) {
                }
            }
        
            if (e instanceof EJBException) {
                throw new ServerException("Transaction rolled back",
                                          ((EJBException) e).getCausedByException());
            } else {
                throw new ServerException("Transaction rolled back", e);
            }
        } catch (RemoteException e)
        {
            // EJB 2.0 17.3, table 16
            if (mi.getEnterpriseContext().getTransaction() != null) {
                try {
                    mi.getEnterpriseContext().getTransaction().setRollbackOnly();
                } catch (IllegalStateException ex) {
                }
            }
            
            throw new ServerException("Transaction rolled back", e);    
        } catch (Error e)
            {
            // EJB 2.0 17.3, table 16
            if (mi.getEnterpriseContext().getTransaction() != null) {
                try {
                mi.getEnterpriseContext().getTransaction().setRollbackOnly();
                } catch (IllegalStateException ex) {
                }
            }
            
            throw new ServerException("Transaction rolled back:"+e.getMessage());   
            } finally {
            
            // Reset user Tx
            userTransaction.set(oldUserTx);
            
            if (t1 != null) {
                
                // DEBUG Logger.debug("TxInterceptorBMT reassociating client tx " + t1.hashCode());
                //DEBUG             Logger.debug("TxInterceptorBMT reassociating client tx " + t1.hashCode());
                
                // reassociate the previous transaction before returning
                tm.associateThread(t1);
                
            }
            
            
            
                // t3 is the transaction associated with the context at the end of the call
            Transaction t3 = mi.getEnterpriseContext().getTransaction();
            
            //DEBUG             Logger.debug("in TxIntBMT " + t3);
            
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
                Logger.error("Application error: BMT stateless bean " + container.getBeanMetaData().getEjbName() + " should complete transactions before returning (ejb1.1 spec, 11.6.1)");
                
                // the instance interceptor will discard the instance
                throw new RemoteException("Application error: BMT stateless bean " + container.getBeanMetaData().getEjbName() + " should complete transactions before returning (ejb1.1 spec, 11.6.1)");
            }
            
            }
    }
}




