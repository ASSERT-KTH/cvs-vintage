/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;

import javax.ejb.Handle;
import javax.ejb.EJBObject;

/**
 * An EJB entity bean handle implementation.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.9 $
 */
public class EntityHandleImpl
    extends AbstractHandle
    implements Handle
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = 1636103643167246469L;
    
    // Attributes ----------------------------------------------------

    /** The primary key of the entity bean. */
    protected final Object id;
   
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Construct a <tt>EntityHandleImpl</tt>.
     *
     * @param state     The initial context state that will be used
     *                  to restore the naming context or null to use
     *                  a fresh InitialContext object.
     * @param name      JNDI name.
     * @param id        Primary key of the entity.
     */
    public EntityHandleImpl(final InitialContextHandle state,
                            final String name,
                            final Object id)
    {
        super(state, name);
        this.id = id;
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
        return getEJBObject("findByPrimaryKey",
                            new Class[] { id.getClass() },
                            new Object[] { id });
    }

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

