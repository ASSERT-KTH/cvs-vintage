/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerError;
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
*   @author Peter Antman (peter.antman@tim.se)
*   @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
*   @version $Revision: 1.16 $
*/
public class TxInterceptorBMT
extends AbstractInterceptor
{

    // Attributes ----------------------------------------------------
    // Protected to be able to inherit, pra
    protected TxManager tm;

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

    public void stop()
    {
       try
       {
          ((Context)new InitialContext().lookup("java:comp/")).unbind("UserTransaction");
       }
       catch (Exception e)
       {
          //ignore
       }
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
            Transaction t1 = tm.getTransaction();

            // t2 refers to the instance transaction (spec ejb1.1, 11.6.1, p174)
            Transaction t2 = mi.getEnterpriseContext().getTransaction();

            try {

                if (t2 == null) {
                    tm.suspend();
                }
                else if (! t2.equals(t1) ){
                    tm.suspend();
                    // associate the transaction to the thread
                    tm.resume(t2);

                } // else we are in the proper tx context

                return getNext().invokeHome(mi);

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

                Transaction currentTx = tm.getTransaction();
                if (t1 == null) {
                    tm.suspend();
                }
                else if (! t1.equals(currentTx)) {

                    tm.suspend();
                    // reassociate the previous transaction before returning
                    tm.resume(t1);
                } // else we are in the right tx context, do nothing
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
        Transaction t1 = tm.getTransaction();

//DEBUG     Logger.debug("TxInterceptorBMT disassociate" + ((t1==null) ? "null": Integer.toString(t1.hashCode())));

        // t2 refers to the instance transaction (spec ejb1.1, 11.6.1, p174)
        Transaction t2 = mi.getEnterpriseContext().getTransaction();

        // This is BMT so the transaction is dictated by the Bean, the MethodInvocation follows
        mi.setTransaction(t2);

//DEBUG Logger.debug("TxInterceptorBMT t2 in context" + ((t2==null) ? "null": Integer.toString(t2.hashCode())));

        try {

            if (t2 == null) {

                tm.suspend();
            }
            else if ( ! t2.equals(t1) ) {

                tm.suspend();
                // associate the transaction to the thread
                tm.resume(t2);

            }
            // else we are in the right tx context

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

            throw new ServerError("Transaction rolled back", e);
        } finally {

            // Reset user Tx
            userTransaction.set(oldUserTx);

//DEBUG         Logger.debug("TxInterceptorBMT reassociating client tx " +
//DEBUG                      (t1==null?"null":String.valueOf(t1.hashCode())));


            Transaction currentTx = tm.getTransaction();
            if (t1 == null) {

                tm.suspend();
            }
            else if (! t1.equals(currentTx)) {

                tm.suspend();
                // reassociate the previous transaction before returning
                tm.resume(t1);
            } // else we are in the right tx context, do nothing

            if (((SessionMetaData)container.getBeanMetaData()).isStateless()) {

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

