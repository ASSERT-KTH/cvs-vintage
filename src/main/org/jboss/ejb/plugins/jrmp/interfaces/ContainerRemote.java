/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;
import java.security.Principal;
import java.rmi.Remote;
import java.rmi.MarshalledObject;

import javax.transaction.Transaction;

/**
 * The remote interface of a container.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.9 $
 */
public interface ContainerRemote
   extends Remote
{
    // Constants -----------------------------------------------------

    /**
     * The time when this class was initialized. Used to 
     * determine if this instance lives in the same VM as the container.
     */
    long STARTUP = System.currentTimeMillis();
   
    // Public --------------------------------------------------------

    /**
     * Invoke the remote home instance.
     *
     * @param mi    The marshalled object representing the method to
     *              invoke.
     * @return      Return value of method invocation.
     * 
     * @throws Exception    Failed to invoke method.
     */
    MarshalledObject invokeHome(MarshalledObject mi)
        throws Exception;

    /**
     * Invoke a remote object instance.
     *
     * @param mi    The marshalled object representing the method to
     *              invoke.
     * @return      Return value of method invocation.
     * 
     * @throws Exception    Failed to invoke method.
     */
    MarshalledObject invoke(MarshalledObject mi)
        throws Exception;

    /**
     * Invoke the local home instance.
     *
     * @param m             The method to invoke.
     * @param args          The arguments to the method.
     * @param tx            The transaction to use for the invocation.
     * @param idendity      The principal to use for the invocation.
     * @param credential    The credentials to use for the invocation.
     * @return              Return value of method invocation.
     * 
     * @throws Exception    Failed to invoke method.
     */
    Object invokeHome(Method m, Object[] args, Transaction tx,
                      Principal identity, Object credential)
        throws Exception;

    /**
     * Invoke a local object instance.
     *
     * @param id            The identity of the object to invoke.
     * @param m             The method to invoke.
     * @param args          The arguments to the method.
     * @param tx            The transaction to use for the invocation.
     * @param idendity      The principal to use for the invocation.
     * @param credential    The credentials to use for the invocation.
     * @return              Return value of method invocation.
     * 
     * @throws Exception    Failed to invoke method.
     */
    Object invoke(Object id, Method m, Object[] args, Transaction tx,
                  Principal identity, Object credential)
        throws Exception;

   // Inner classes -------------------------------------------------
}

