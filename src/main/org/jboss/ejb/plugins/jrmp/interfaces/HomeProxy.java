/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.lang.reflect.Method;
import java.rmi.MarshalledObject;

import javax.naming.Name;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;

import org.jboss.ejb.CacheKey;

/**
 * The client-side proxy for an EJB Home object.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.26 $
 */
public class HomeProxy
    extends GenericProxy
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = 432426690456622923L;
    
    // Static --------------------------------------------------------

    /** {@link EJBHome#getEJBMetaData} method reference. */
    protected static final Method GET_EJB_META_DATA;

    /** {@link EJBHome#getHomeHandle} method reference. */
    protected static final Method GET_HOME_HANDLE;

    /** {@link EJBHome#remove(Handle)} method reference. */
    protected static final Method REMOVE_BY_HANDLE;

    /** {@link EJBHome#remove(Object)} method reference. */
    protected static final Method REMOVE_BY_PRIMARY_KEY;

    /** {@link EJBObject#remove} method reference. */
    protected static final Method REMOVE_OBJECT;
    
    /**
     * Initialize {@link EJBHome} and {@link EJBObject} method references.
     */
    static {
        try {
            final Class empty[] = {};
            final Class type = EJBHome.class;

            GET_EJB_META_DATA = type.getMethod("getEJBMetaData", empty);
            GET_HOME_HANDLE = type.getMethod("getHomeHandle", empty);
            REMOVE_BY_HANDLE = type.getMethod("remove", new Class[] { 
                Handle.class 
            });
            REMOVE_BY_PRIMARY_KEY = type.getMethod("remove", new Class[] { 
                Object.class 
            });

            // Get the "remove" method from the EJBObject
            REMOVE_OBJECT = EJBObject.class.getMethod("remove", empty);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);            
        }
    }
    
    // Attributes ----------------------------------------------------

    /** The EJB meta-data for the {@link EJBHome} reference. */    
    protected EJBMetaData ejbMetaData;
    
    // Constructors --------------------------------------------------

    /**
     * No-argument constructor for externalization.
     */
    public HomeProxy() {}

    /**
     * Construct a <tt>HomeProxy</tt>.
     *
     * @param name          The JNDI name of the container that we proxy for.
     * @param ejbMetaData   ???
     * @param container     The remote interface of the invoker for which
     *                      this is a proxy for.
     * @param optimize      True if the proxy will attempt to optimize
     *                      VM-local calls.
     */
    public HomeProxy(final String name,
                     final EJBMetaData ejbMetaData,
                     final ContainerRemote container,
                     final boolean optimize)
    {
        super(name, container, optimize);
        this.ejbMetaData = ejbMetaData;
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
    public Object invoke(final Object proxy,
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
            return name + "Home";
        }
        else if (m.equals(EQUALS)) {
            // equality of the proxy home is based on names...
            Object temp = invoke(proxy, TO_STRING, args);
            return new Boolean(temp.equals(name + "Home"));
        }
        else if (m.equals(HASH_CODE)) {
            return new Integer(this.hashCode());
        }
        
        // Implement local EJB calls
        else if (m.equals(GET_HOME_HANDLE)) {
            return new HomeHandleImpl(initialContextHandle, name);
        }
        else if (m.equals(GET_EJB_META_DATA)) {
            return ejbMetaData;
        }
        else if (m.equals(REMOVE_BY_HANDLE)) {
            // First get the EJBObject
            EJBObject object = ((Handle) args[0]).getEJBObject();
            
            // remove the object from here
            object.remove();
            
            // Return Void
            return Void.TYPE;
        }
        else if (m.equals(REMOVE_BY_PRIMARY_KEY)) {
            // Session beans must throw RemoveException (EJB 1.1, 5.3.2)
            if (ejbMetaData.isSession())
                throw new RemoveException("Session beans cannot be removed by primary key.");

            // The trick is simple we trick the container in believe it
            // is a remove() on the instance
            Object id = new CacheKey(args[0]);
            return invokeContainer(id, REMOVE_OBJECT, EMPTY_ARGS);
        }
        
        // If not taken care of, go on and call the container
        else {
            return invokeHome(m, args);
        }
    }

    /**
     * Externalization support.
     *
     * @param out
     *
     * @throws IOException
     */
    public void writeExternal(final ObjectOutput out)
        throws IOException
    {
        super.writeExternal(out);
        out.writeObject(ejbMetaData);
    }
    
    /**
     * Externalization support.
     *
     * @param in
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(final ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        ejbMetaData = (EJBMetaData)in.readObject();
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------

    /**
     * Invoke the container to handle this <tt>EJBHome</tt> method
     * invocation.
     *
     * @param method    The method to invoke.
     * @param args      The arguments passed to the method.
     *
     * @throws Throwable    Failed to invoke container.
     */
    private Object invokeHome(final Method method, final Object[] args)
        throws Throwable
    {
        Object result;

        // Optimize if calling another bean in same EJB-application
        if (optimize && isLocal()) {
            result = container.invokeHome(method,
                                          args,
                                          getTransaction(),
                                          getPrincipal(),
                                          getCredential());
        }
        else {
            MarshalledObject mo = createMarshalledObject(null, method, args);

            // Invoke on the remote server, enforce marshaling
            if (isLocal()) {
                // ensure marshaling of exceptions is done properly
                try {
                    result = container.invokeHome(mo).get();
                }
                catch (Throwable e) {
                    throw (Throwable)new MarshalledObject(e).get();
                }
            }
            else {
                // Marshaling is done by RMI
                return container.invokeHome(mo).get();
            }
        }

        return result;
    }

    // Inner classes -------------------------------------------------
}
