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
import java.rmi.NoSuchObjectException;
import java.util.HashMap;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.SystemException;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.MethodMetaData;

/**
 *  This interceptor handles transactions for CMT beans.
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *  @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.12 $
 */
public class TxInterceptorCMT
extends AbstractInterceptor
{

    // Attributes ----------------------------------------------------

    /** Local reference to the container's TransactionManager. */
    private TransactionManager tm;
 
    /** A cache mapping methods to transaction attributes. */
    private HashMap methodTx = new HashMap();

    /** The container that we manage transactions for. */
    protected Container container;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /**
     *  Set the container that we manage transactions for.
     */
    public void setContainer(Container container)
    {
        this.container = container;
    }

    /**
     *  Set the container that we manage transactions for.
     */
    public Container getContainer()
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
     *  This method does invocation interpositioning of tx management
     */
    public Object invoke(MethodInvocation mi)
        throws Exception
    {
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

    /*
     *  This method calls the next interceptor in the chain.
     *
     *  Throwables <code>Error</code>, <code>RemoteException</code> and
     *  <code>RuntimeException</code> are caught and result in the transaction
     *  being marked for rollback only. If such a non-application exception
     *  happens with a transaction that was not started locally, it is wrapped
     *  in a <code>TransactionRolledbackException</code>.
     *
     *  @param remoteInvocation If <code>true</code> this is an invocation
     *                          of a method in the remote interface, otherwise
     *                          it is an invocation of a method in the home
     *                          interface.
     *  @param mi The <code>MethodInvocation</code> of this call.
     *  @param newTx If <code>true</code> the transaction has just been
     *               started in this interceptor.
     */
    private Object invokeNext(boolean remoteInvocation, MethodInvocation mi, boolean newTx)
        throws Exception
    {
        try {
            if (remoteInvocation)
                return getNext().invoke(mi);
            else
                return getNext().invokeHome(mi);
        } catch (RuntimeException e) {
            try {
                mi.getTransaction().setRollbackOnly();
            } catch (SystemException ex) {
                Logger.exception(ex);
            } catch (IllegalStateException ex) {
                Logger.exception(ex);
            }
            RemoteException ex;
            if (newTx) {
                if (e instanceof NoSuchEntityException) {
                    // Convert NoSuchEntityException to a NoSuchObjectException
                    // with the same detail.
                    ex = new NoSuchObjectException(e.getMessage());
                    ex.detail = ((NoSuchEntityException)e).getCausedByException();
                    throw ex;
                }
                // OSH: Should this be wrapped?
                ex = new ServerException(e.getMessage());
            } else
                // We inherited tx: Tell caller that we marked for rollback only.
                ex = new TransactionRolledbackException(e.getMessage());
            ex.detail = e;
            throw ex;
        } catch (RemoteException e) {
            try {
                mi.getTransaction().setRollbackOnly();
            } catch (SystemException ex) {
                Logger.exception(ex);
            } catch (IllegalStateException ex) {
                Logger.exception(ex);
            }
            RemoteException ex;
            if (newTx) {
                if (e instanceof NoSuchObjectException)
                    throw e; // Do not wrap this.
                // OSH: Should this be wrapped?
                ex = new ServerException(e.getMessage());
            } else
                ex = new TransactionRolledbackException(e.getMessage());
            ex.detail = e;
            throw ex;
        } catch (Error e) {
            if (mi.getTransaction() != null) {
                try {
                    mi.getTransaction().setRollbackOnly();
                } catch (IllegalStateException ex) {
                }
                RemoteException tre = new TransactionRolledbackException(e.getMessage());
                tre.detail = e;
                throw tre;
            } else {
                // This exception will be transformed into a RemoteException
                // by the LogInterceptor
                throw e;
            }
        }
    }

    /*
     *  This method does invocation interpositioning of tx management.
     *
     *  This is where the meat is.  We define what to do with the Tx based
     *  on the declaration.
     *  The MethodInvocation is always the final authority on what the Tx
     *  looks like leaving this interceptor.  In other words, interceptors
     *  down the chain should not rely on the thread association with Tx but
     *  on the Tx present in the MethodInvocation.
     *
     *  @param remoteInvocation If <code>true</code> this is an invocation
     *                          of a method in the remote interface, otherwise
     *                          it is an invocation of a method in the home
     *                          interface.
     *  @param mi The <code>MethodInvocation</code> of this call.
     */
    private Object runWithTransactions(boolean remoteInvocation,
                                       MethodInvocation mi)
        throws Exception
    {
        // Old transaction is the transaction that comes with the MI
        Transaction oldTransaction = mi.getTransaction();
        // New transaction is the new transaction this might start
        Transaction newTransaction = null;
 
        //DEBUG       Logger.debug("Current transaction in MI is "+mi.getTransaction());
        //DEBUG Logger.debug("Current method "+mi.getMethod());
        byte transType = getTransactionMethod(mi.getMethod(), remoteInvocation); 
        printMethod(mi.getMethod(), transType);
 
        // Thread arriving must be clean (jboss doesn't set the thread
        // previously). However optimized calls come with associated
        // thread for example. We suspend the thread association here, and
        // resume in the finally block of the following try.
        Transaction threadTx = tm.suspend();
        try { // OSH FIXME: Indentation

        switch (transType) {
            case MetaData.TX_NOT_SUPPORTED:
                // Do not set a transaction on the thread even if in MI, just run
                return invokeNext(remoteInvocation, mi, false);
 
            case MetaData.TX_REQUIRED:
                if (oldTransaction == null) { // No tx running
                    // Create tx
                    tm.begin();
 
                    // get the tx
                    newTransaction = tm.getTransaction();
 
                    // Let the method invocation know
                    mi.setTransaction(newTransaction);
                } else { // We have a tx propagated
                    // Associate it with the thread
                    tm.resume(oldTransaction);
                }
 
                // Continue invocation
                try {
                    return invokeNext(remoteInvocation, mi, newTransaction != null);
                } finally {
//DEBUG                Logger.debug("TxInterceptorCMT: In finally");
 
                    // Only do something if we started the transaction
                    if (newTransaction != null) {
                        // Marked rollback
                        if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                            newTransaction.rollback();
                        } else {
                            // Commit tx
                            // This will happen if
                            // a) everything goes well
                            // b) app. exception was thrown
//DEBUG                        Logger.debug("TxInterceptorCMT:before commit");
                            newTransaction.commit();
//DEBUG                        Logger.debug("TxInterceptorCMT:after commit");
                        }
 
                        // reassociate the oldTransaction with the methodInvocation (even null)
                        mi.setTransaction(oldTransaction);
                    } else {
                        // Drop thread association
                        tm.suspend();
                    }
                }

            case MetaData.TX_SUPPORTS:
                {
                    // Associate old transaction (may be null) with the thread
                    tm.resume(oldTransaction);
 
                    try {
                        return invokeNext(remoteInvocation, mi, false);
                    } finally {
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
                    try {
                        return invokeNext(remoteInvocation, mi, true);
                    } finally {
                        // We started the transaction for sure so we commit or roll back
 
                        if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                            newTransaction.rollback();
                        } else {
                            // Commit tx
                            // This will happen if
                            // a) everything goes well
                            // b) app. exception was thrown
                            newTransaction.commit();
                        }
 
                        // set the old transaction back on the method invocation                        mi.setTransaction(oldTransaction);
                    }
                }
            case MetaData.TX_MANDATORY:
                if (oldTransaction == null) // no transaction = bad!
                    throw new TransactionRequiredException("Transaction Required, read the spec!");
                // Associate it with the thread
                tm.resume(oldTransaction);
                try {
                    return invokeNext(remoteInvocation, mi, false);
                } finally {
                    tm.suspend();
                }
 
            case MetaData.TX_NEVER:
                if (oldTransaction != null) // Transaction = bad!
                    throw new RemoteException("Transaction not allowed");
                return invokeNext(remoteInvocation, mi, false);
        }
 
        } finally { // OSH FIXME: Indentation
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

//DEBUG        Logger.debug("Found metadata for bean '"+bmd.getEjbName()+"'"+" method is "+m.getName());

        byte result = bmd.getMethodTransactionType(m.getName(), m.getParameterTypes(), remoteInvocation);

        // provide default if method is not found in descriptor
        if (result == MetaData.TX_UNKNOWN) result = MetaData.TX_REQUIRED;

        methodTx.put(m, new Byte(result));
        return result;
    }

    // Inner classes -------------------------------------------------

}
