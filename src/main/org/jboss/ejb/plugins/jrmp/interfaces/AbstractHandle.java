/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.Serializable;
import java.rmi.ServerException;

import java.lang.reflect.*;
import javax.ejb.*;
import javax.naming.*;

/**
 * An abstract base handle class from which all handles extend from.
 *      
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.4 $
 */
public abstract class AbstractHandle
   implements Serializable
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = -6161932252555944539L;
    
    // Attributes ----------------------------------------------------

    /** <i>JNDI</i> name used for lookups. */
    protected final String name;
   
    /** A handle used to restore the correct naming context.  */
    protected final InitialContextHandle initialContextHandle;

    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------

    /**
     * Initialize.
     *
     * @param handle    The initial context handle that will be used
     *                  to restore the naming context or null to use
     *                  a fresh InitialContext object.
     * @param name      A JNDI name.
     */
    public AbstractHandle(final InitialContextHandle handle,
                          final String name)
    {
        this.initialContextHandle = handle;
        this.name = name;
    }
   
    /**
     * Initialize, creating a new initial context handle object.
     *
     * @param name      A JNDI name.
     */
    public AbstractHandle(final String name) {
        this(InitialContextHandle.create(), name);
    }

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
   
    /**
     * Create an <tt>InitialContext</tt> using the saved environment or 
     * create a vanilla <tt>InitialContext</tt> when the enviroment
     * is <i>null</i>.
     *
     * @return  <tt>InitialContext</tt> suitable for the bean that this
     *          is a proxy for.
     *
     * @throws NamingException    Failed to create <tt>InitialContext</tt>.
     */
    protected InitialContext createInitialContext() 
        throws NamingException
    {
        if (initialContextHandle == null) {
            // HACK to allow lookups when using the HomeHandle created by
            // a JRMPContainerInvoker, which does not provide an
            // initialContextHandle
            return new InitialContext();
        }

        return initialContextHandle.getInitialContext();
    }

    /**
     * Get the <tt>EJBHome</tt> reference at the configured <i>JNDI</i>
     * name.
     *
     * @return  <tt>EJBHome</tt> reference.
     *
     * @throws NamingException      Failed to lookup <tt>EJBHome</tt>.
     * @throws ClassCastException   Object at specified name is not an
     *                              instance of EJBHome.
     */
    protected EJBHome lookupEJBHome() throws NamingException {
        // restore the naming context
        InitialContext ctx = createInitialContext();

        try {
            return (EJBHome)ctx.lookup(name);
        }
        finally {
            ctx.close();
        }
    }

    /**
     * Helper to perform the actual lookup and reflection.
     *
     * @param name      The name of the method that is to be used.
     * @param types     The signature of the method.
     * @param args      The arguments to the method.
     * @return          An EJBObject.
     *
     * @throws ServerException      Could not get EJBObject.
     */
    protected EJBObject getEJBObject(final String name,
                                     final Class[] types, 
                                     final Object[] args)
        throws ServerException
    {
        try {
            EJBHome home = lookupEJBHome();
            Class type = home.getClass();
            Method method = type.getMethod(name, types);
         
            return (EJBObject)method.invoke(home, args);
        }
        catch (Exception e) {
            throw new ServerException("Could not get EJBObject", e);
        }
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
