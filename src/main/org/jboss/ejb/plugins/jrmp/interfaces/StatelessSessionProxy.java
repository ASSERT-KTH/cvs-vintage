/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;


/**
 * An EJB stateless session bean proxy class.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.18 $
 */
public class StatelessSessionProxy
    extends BeanProxy
{
	// Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = 2327647224051998978L;

	// Attributes ----------------------------------------------------
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------

    /**
     * No-argument constructor for externalization.
     */
    public StatelessSessionProxy() {}

    /**
     * Construct a <tt>StatelessSessionProxy</tt>.
     *
     * @param name          The JNDI name of the container that we proxy for.
     * @param container     The remote interface of the invoker for which
     *                      this is a proxy for.
     * @param optimize      True if the proxy will attempt to optimize
     *                      VM-local calls.
     */
	public StatelessSessionProxy(final String name,
                                 final ContainerRemote container,
                                 final boolean optimize)
	{
		super(name, container, optimize);
	}
	
	// Public --------------------------------------------------------
	
	/**
     * InvocationHandler implementation.
     *
     * @param proxy   The proxy object.
     * @param m       The method being invoked.
     * @param args    The arguments for the method.
     *
     * @throws Throwable    Any exception or error thrown while processing.
     */
	public final Object invoke(final Object proxy,
                               final Method m,
                               Object[] args)
        throws Throwable
	{
		// Normalize args to always be an array
		// Isn't this a bug in the proxy call??
		if (args == null)
			args = EMPTY_ARGS;
		
		// Implement local methods
		if (m.equals(TO_STRING)) {
			return name + ":Stateless";
		}
		else if (m.equals(EQUALS)) {
			return invoke(proxy, IS_IDENTICAL, args);
		}
		else if (m.equals(HASH_CODE)) {
			// We base the stateless hash on the hash of the proxy...
			// MF XXX: it could be that we want to return the hash of the name?
			return new Integer(this.hashCode());
		}
		
		// Implement local EJB calls
		else if (m.equals(GET_HANDLE)) {
			return new StatelessHandleImpl(initialContextHandle, name);
		}
		else if (m.equals(GET_PRIMARY_KEY)) {
			// MF FIXME 
			// The spec says that SSB PrimaryKeys should not be returned and the call should throw an exception
			// However we need to expose the field *somehow* so we can check for "isIdentical"
			// For now we use a non-spec compliant implementation and just return the key as is
			// See jboss1.0 for the PKHolder and the hack to be spec-compliant and yet solve the problem
			
			// This should be the following call 
			//throw new RemoteException("Session Beans do not expose their keys, RTFS");
			
			// This is how it can be solved with a PKHolder (extends RemoteException)
			// throw new PKHolder("RTFS", name);
			
			// This is non-spec compliant but will do for now
			// We can consider the name of the container to be the primary key, since all stateless beans
			// are equal within a home
			return name;
		}
        else if (m.equals(GET_EJB_HOME)) {
            return getEJBHome();
        }
		else if (m.equals(IS_IDENTICAL)) {
			// All stateless beans are identical within a home,
			// if the names are equal we are equal
            return isIdentical(args[0], name);
		}
		
		// If not taken care of, go on and call the container
		else {
            return invokeContainer(null, m, args);
		}
	}

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
