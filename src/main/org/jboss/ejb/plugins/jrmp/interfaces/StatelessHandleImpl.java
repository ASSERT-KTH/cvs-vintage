/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;

import javax.ejb.Handle;
import javax.ejb.EJBObject;

/**
 * An EJB stateless session bean handle.
 *
 * @author  Rickard �berg (rickard.oberg@telkel.com)
 * @author  Jason Dillon <a href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</a>
 * @version $Revision: 1.5 $
 */
public class StatelessHandleImpl
    extends AbstractHandle
    implements Handle
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = 4651553991845772180L;
    
    // Attributes ----------------------------------------------------
   
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Construct a <tt>StatelessHandleImpl</tt>.
     *
     * @param handle    The initial context handle that will be used
     *                  to restore the naming context or null to use
     *                  a fresh InitialContext object.
     * @param name      JNDI name.
     */
    public StatelessHandleImpl(final InitialContextHandle handle,
                               final String name)
    {
        super(handle, name);
    }
   
    // Public --------------------------------------------------------

    /**
     * Handle implementation.
     *
     * @return  <tt>EJBObject</tt> reference.
     *
     * @throws ServerException    Could not get EJBObject.
     * @throws RemoteException
     */
    public EJBObject getEJBObject() throws RemoteException {
        return getEJBObject("create", new Class[0], new Object[0]);
    }

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

