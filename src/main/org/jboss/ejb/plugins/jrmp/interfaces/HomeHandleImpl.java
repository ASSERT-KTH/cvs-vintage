/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.ejb.HomeHandle;
import javax.ejb.EJBHome;
import javax.naming.NamingException;

/**
 * An EJB home handle implementation.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.9 $
 */
public class HomeHandleImpl
    extends AbstractHandle
    implements HomeHandle
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = -6105191783910395296L;
    
    // Attributes ----------------------------------------------------
   
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Construct a <tt>HomeHandleImpl</tt>.
     *
     * @param handle    The initial context handle that will be used
     *                  to restore the naming context or null to use
     *                  a fresh InitialContext object.
     * @param name      JNDI name.
     */
    public HomeHandleImpl(final InitialContextHandle handle,
                          final String name)
    {
        super(handle, name);
    }

    /**
     * Construct a <tt>HomeHandleImpl</tt>.  This is used for
     * constructing a handle reference outside of a proxy.
     *
     * @param name   JNDI name.
     */
    public HomeHandleImpl(final String name) {
        super(name);
    }
    
    // Public --------------------------------------------------------

    // Handle implementation -----------------------------------------

    /**
     * HomeHandle implementation.
     *
     * @return  <tt>EJBHome</tt> reference.
     *
     * @throws ServerException    Could not get EJBObject.
     * @throws RemoteException
     */
    public EJBHome getEJBHome() throws RemoteException {
        try {
            return lookupEJBHome();
        }
        catch (NamingException e) {
            throw new ServerException("Could not get EJBHome", e);
        } 
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

