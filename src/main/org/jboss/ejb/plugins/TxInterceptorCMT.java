/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
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
import javax.transaction.TransactionRolledbackException;
import javax.transaction.SystemException;

import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.MethodMetaData;

/**
*   <description>
*
*   @see <related>
*   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
*   @version $Revision: 1.11 $
*/
public class TxInterceptorCMT
extends AbstractInterceptor
{

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
        tm = (TransactionManager) getContainer().getTransactionManager();

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
//DEBUG        Logger.debug(name+" for "+m.getName());
    }

    private Object invokeNext(boolean remoteInvocation, MethodInvocation mi) throws Exception {
		try
		{
	        if (remoteInvocation) {
	            return getNext().invoke(mi);
	        } else {
	            return getNext().invokeHome(mi);
	        }
		} catch (RuntimeException e)
		{
			// EJB 2.0 17.3, table 15
			if (mi.getTransaction() != null)
			{
				try {
					mi.getTransaction().setRollbackOnly();
				} catch (IllegalStateException ex) {
				}
				RemoteException tre = new TransactionRolledbackException(e.getMessage());
				tre.detail = e;
				throw tre;
			} else
			{
				// This exception will be transformed into a RemoteException by the LogInterceptor
				throw e;
			}
		} catch (RemoteException e)
		{
			// EJB 2.0 17.3, table 15
			if (mi.getTransaction() != null)
			{
				try {
					mi.getTransaction().setRollbackOnly();
				} catch (IllegalStateException ex) {
				}
				RemoteException tre = new TransactionRolledbackException(e.getMessage());
				tre.detail = e;
				throw tre;
			} else
			{
				// This exception will be transformed into a RemoteException by the LogInterceptor
				throw e;
			}
		} catch (Error e)
		{
			// EJB 2.0 17.3, table 15
			if (mi.getTransaction() != null)
			{
				try {
					mi.getTransaction().setRollbackOnly();
				} catch (IllegalStateException ex) {
				}
				RemoteException tre = new TransactionRolledbackException(e.getMessage());
				tre.detail = e;
				throw tre;
			} else
			{
				// This exception will be transformed into a RemoteException by the LogInterceptor
				throw e;
			}
		}
    }

    /*
    * runWithTransactions
    *
    * This is where the meat is.  We define what to do with the Tx based on the declaration.
    * The MethodInvocation is always the final authority on what the Tx looks like leaving this
    * interceptor.  In other words, interceptors down the chain should not rely on the thread
    * association with Tx but on the Tx present in the MethodInvocation
    */

    private Object runWithTransactions(boolean remoteInvocation, MethodInvocation mi) throws Exception {

        // Thread transaction is the transaction that the
        // current thread came in with
        Transaction threadTransaction = tm.getTransaction();
        // Old transaction is the transaction that comes with the MI
        Transaction oldTransaction = mi.getTransaction();
        // New transaction is the new transaction this might start
        Transaction newTransaction = null;

        // Indicates if the call was made already
        // in the context of the right transaction
        boolean optimized = ( (threadTransaction != null) &&
                              (oldTransaction != null) &&
                              (threadTransaction.equals(oldTransaction)) );

        //DEBUG        Logger.debug("Current transaction in MI is  "+mi.getTransaction());
        //DEBUG        Logger.debug("Current thread transaction is "+threadTransaction);
        //DEBUG        Logger.debug("Current method "+mi.getMethod());
        byte transType = getTransactionMethod(mi.getMethod(), remoteInvocation);

        printMethod(mi.getMethod(), transType);

        switch (transType) {

            case MetaData.TX_NOT_SUPPORTED:
                {
                    //DEBUG                    Logger.debug("TX_NOT_SUPPORTED begin");
                    // Thread arriving must be clean (jboss doesn't set the thread previously)
                    // However optimized calls come with associated thread for example
                    if (threadTransaction != null) {
                        //DEBUG                        Logger.debug("Suspending current thread transaction");
                        tm.suspend();
                    }

                    try {

                        // Do not set a transaction on the thread even if in MI, just run
                        return invokeNext(remoteInvocation,mi );
                    }
                    finally {

                        // IN case we had a Tx associated with the thread reassociate
                        if (threadTransaction != null) {
                            //DEBUG                            Logger.debug("Resuming original thread transaction");
                            tm.resume(threadTransaction);
                        }
                        //DEBUG                        Logger.debug("TX_NOT_SUPPORTED end");
                    }
                }


            case MetaData.TX_REQUIRED:
                {
                    //DEBUG                    Logger.debug("TX_REQUIRED begin");

                    if (oldTransaction == null) { // No tx running

                        if (threadTransaction != null) {
                            //DEBUG                            Logger.debug("Suspending current thread transaction");
                            tm.suspend();
                        }

                        //DEBUG                        Logger.debug("Starting new transaction");
                        // Create tx
                        tm.begin();

                        // get the tx
                        newTransaction = tm.getTransaction();

                        // Let the method invocation know
                        mi.setTransaction(newTransaction);
                    }

                    else { // We have a tx propagated

                        // are we in optimized tx context?
                        if ( ! optimized ) {
                            // the incoming thread is not optimized, so
                            // suspend its transaction
                            // DEBUG                            Logger.debug("Suspending current thread transaction");
                            tm.suspend();
                            // Associate the propagated tx with the thread
                            // DEBUG                            Logger.debug("Resuming propagated transaction");
                            tm.resume(oldTransaction);
                        }
                        /*
                        else {
                            //DEBUG
                            Logger.debug("Optimized call -- current transaction same as propagated");
                        }
                        */
                    }

                    // Continue invocation
                    try	{
                        //DEBUG                        Logger.debug("Current transaction is  "+tm.getTransaction());

                        return invokeNext(remoteInvocation,mi );
                    }
                    catch (RemoteException e) {

                        if (newTransaction != null) {

                            //We started it, it will be rolled back in the finally
                            newTransaction.setRollbackOnly();
                        }

                        throw e;
                    }
                    catch (RuntimeException e) {

                        if (newTransaction != null) {

                            // We started it, it will be rolled back in the finally
                            newTransaction.setRollbackOnly();
                        }

                        throw new ServerException("Exception occurred", e);
                    }
                    catch (Error e) {

                        if (newTransaction != null) {

                            // we started it, it will be rolled back in the finally
                            newTransaction.setRollbackOnly();
                        }
                        throw new ServerException("Exception occurred:"+e.getMessage());
                    }

                    finally {

                        //DEBUG                        Logger.debug("TxInterceptorCMT: in finally");

                        // Do something wuth the transaction we've started
                        if (newTransaction != null) {

                            //DEBUG                            Logger.debug("TxInterceptorCMT: newTransaction is not null");

                            // Marked rollback
                            if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {

                                // actually roll it back
                                newTransaction.rollback();
                                //DEBUG                                Logger.debug("TxInterceptorCMT: rolling back newTransaction");
                            }

                            //Still running
                            else if(newTransaction.getStatus() == Status.STATUS_ACTIVE) {

                                // Commit tx
                                // This will happen if
                                // a) everything goes well
                                // b) app. exception was thrown
//DEBUG                                Logger.debug("TxInterceptorCMT:before commit");
                                newTransaction.commit();
//DEBUG                                Logger.debug("TxInterceptorCMT:after commit");

                            }

                            // reassociate the oldTransaction with the methodInvocation (even null)
                            mi.setTransaction(oldTransaction);
                        }

                        // Either the newTransaction was committed/rolled back
                        // or it was null. Just disassociate ourselves from
                        // whatever the current transaction is (the only optimization
                        // here could be to check if the current transaction is the
                        // same as the threadTransaction but this, AFAIK, is not
                        // very likely)
                        // In any case, if the original invocation was without a
                        // transactional context, we need to make sure we come out
                        // without one, especially not in the context of already
                        // committed transaction, for this may cause big trouble later on

                        Transaction currentTx = tm.getTransaction();

                        if (threadTransaction == null) {

                          if (currentTx != null)
                            tm.suspend();
                        }
                        else if (! threadTransaction.equals(currentTx)) {

                          tm.suspend();
                          //DEBUG                          Logger.debug("TxInterceptorCMT: resuming threadTransaction");
                          tm.resume(threadTransaction);
                        }
                        /* else we are still in the right context, don't do anything */

                    }
                }

            case MetaData.TX_SUPPORTS:
                {

                    // DEBUG                    Logger.debug("TX_SUPPORTS begin");

                    if (oldTransaction != null) { // We have a tx propagated
                        // are we in the optimized tx context?
                        if ( ! optimized ) {
                            // the incoming thread is not optimized, so
                            // suspend its transaction
                            // DEBUG                            Logger.debug("Suspending current thread transaction");
                            tm.suspend();
                            // Associate the propagated tx with the thread
                            // DEBUG                            Logger.debug("Resuming propagated transaction");
                            tm.resume(oldTransaction);
                        }
                        /*
                        else {
                            //DEBUG
                            Logger.debug("Optimized call -- current transaction same as propagated");
                        }
                        */
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
                    finally {

                      Transaction currentTx = tm.getTransaction();

                      if (threadTransaction == null) {

                        if (currentTx != null)
                          tm.suspend();
                      }
                      else if ( ! threadTransaction.equals(currentTx) ){
                        tm.suspend();
                        // resume the original transaction
                        //DEBUG                        Logger.debug("Resuming original thread transaction");
                        tm.resume(threadTransaction);
                      }

                      //DEBUG                        Logger.debug("TX_SUPPORTS end");
                    }
                }

            case MetaData.TX_REQUIRES_NEW:
                {

                    // Thread arriving must be clean (jboss doesn't set the thread previously)
                    // However optimized calls come with associated thread for example
                    tm.suspend();


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
                        // will be rolled back in the finally
                        newTransaction.setRollbackOnly();

                        throw e;
                    }
                    catch (RuntimeException e) {

                        // We started it for sure
                        // will be rolled back in the finally
                        newTransaction.setRollbackOnly();

                        throw new ServerException("Exception occurred", e);
                    }
                    catch (Error e) {

                        // We started it for sure
                        // will be rolled back in the finally
                        newTransaction.setRollbackOnly();

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

                        // make sure if we came w/o a transactional
                        // context into this call, we leave w/o one
                        tm.suspend();

                        // or in case we had a Tx associated
                        // with this thread reassociate
                        if (threadTransaction != null) {
                            tm.resume(threadTransaction);
                        }
                    }
                }

            case MetaData.TX_MANDATORY:
                {

                    if (oldTransaction == null) { // no transaction = bad!

                        throw new TransactionRequiredException("Transaction Required, read the spec!");
                    }
                    else {
                        // are we in the optimized tx context?
                        if ( ! optimized ) {
                          // clean up this thread
                          tm.suspend();

                          // Associate it with the thread
                          tm.resume(oldTransaction);
                        }

                        try {
                          return invokeNext(remoteInvocation,mi );
                        }
                        finally {

                          Transaction currentTx = tm.getTransaction();

                          if (threadTransaction == null) {

                            if (currentTx != null)
                              tm.suspend();
                          }
                          else if ( ! threadTransaction.equals(currentTx) ) {
                            tm.suspend();
                            tm.resume(threadTransaction);
                          }
                          // else we are still in the right context
                        }
                    }
                }

            case MetaData.TX_NEVER:
                {

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

//DEBUG        Logger.debug("Found metadata for bean '"+bmd.getEjbName()+"'"+" method is "+m.getName());

        byte result = bmd.getMethodTransactionType(m.getName(), m.getParameterTypes(), remoteInvocation);

        // provide default if method is not found in descriptor
        if (result == MetaData.TX_UNKNOWN) result = MetaData.TX_REQUIRED;

        methodTx.put(m, new Byte(result));
        return result;
    }

    // Inner classes -------------------------------------------------

}
