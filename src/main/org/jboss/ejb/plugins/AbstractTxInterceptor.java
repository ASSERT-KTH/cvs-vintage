/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.NoSuchObjectException;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.SystemException;

import javax.ejb.NoSuchEntityException;

import org.jboss.ejb.Container;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;

/**
 *  A common superclass for the transaction interceptors.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.7 $
 */
abstract class AbstractTxInterceptor
   extends AbstractInterceptor
{

    // Attributes ----------------------------------------------------
    /** Local reference to the container's TransactionManager. */
    protected TransactionManager tm;

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
     *  Get the container that we manage transactions for.
     */
    public Container getContainer()
    {
        return container;
    }

    // Interceptor implementation --------------------------------------

    public void init()
        throws Exception
    {
        tm = (TransactionManager) getContainer().getTransactionManager();
    }

    // Protected  ----------------------------------------------------

    /**
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
    protected Object invokeNext(boolean remoteInvocation, MethodInvocation mi,
                                boolean newTx)
        throws Exception
    {
        try {
            if (remoteInvocation)
                return getNext().invoke(mi);
            else
                return getNext().invokeHome(mi);
        } catch (RuntimeException e) {
            try {
                if(mi.getTransaction() != null) {
                    mi.getTransaction().setRollbackOnly();
                }
            } catch (SystemException ex) {
                log.error("invokeNext", ex);
            } catch (IllegalStateException ex) {
                log.error("invokeNext", ex);
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
                // We inherited tx: Tell caller we marked for rollback only.
                ex = new TransactionRolledbackException(e.getMessage());
            ex.detail = e;
            throw ex;
        } catch (RemoteException e) {
           if (mi.getTransaction() != null)
           {
              try {
                 mi.getTransaction().setRollbackOnly();
              } catch (SystemException ex) {
                log.error("invokeNext", ex);
              } catch (IllegalStateException ex) {
                log.error("invokeNext", ex);
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
           }
           else
           {
              throw e;
           }
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

    // Inner classes -------------------------------------------------

}
