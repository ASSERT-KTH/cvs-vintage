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




/**
 * An EJB stateful session bean proxy class.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.23 $
 */
public class StatefulSessionProxy
   extends BeanProxy
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = 1379411137308931705L;
    
    // Attributes ----------------------------------------------------

    /** JBoss generated identifier. */
    protected Object id;
   
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * No-argument constructor for externalization.
     */
    public StatefulSessionProxy() {}

    /**
     * Construct a <tt>StatefulSessionProxy</tt>.
     *
     * @param name          The JNDI name of the container that we proxy for.
     * @param container     The remote interface of the invoker for which
     *                      this is a proxy for.
     * @param id            JBoss generated identifier.
     * @param optimize      True if the proxy will attempt to optimize
     *                      VM-local calls.
     */
    public StatefulSessionProxy(final String name,
                                final ContainerRemote container,
                                final Object id,
                                final boolean optimize)
    {
       super(name, container, optimize);
       this.id = id;
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
            return name + ":" + id.toString();
        }
        else if (m.equals(EQUALS)) {
            return invoke(proxy, IS_IDENTICAL, args);
        }
        else if (m.equals(HASH_CODE)) {
            return new Integer(id.hashCode());
        }
      
        // Implement local EJB calls
        else if (m.equals(GET_HANDLE)) {
            return new StatefulHandleImpl(initialContextHandle, name, id);
        }
        else if (m.equals(GET_EJB_HOME)) {
            return getEJBHome();
        }
        else if (m.equals(GET_PRIMARY_KEY)) {
            // MF FIXME 
            // The spec says that SSB PrimaryKeys should not be returned and the call should throw an exception
            // However we need to expose the field *somehow* so we can check for "isIdentical"
            // For now we use a non-spec compliant implementation and just return the key as is
            // See jboss1.0 for the PKHolder and the hack to be spec-compliant and yet solve the problem
         
            // This should be the following call 
            //throw new RemoteException("Session Beans do not expose their keys, RTFS");
      
            // This is how it was solved in jboss1.0
            // throw new PKHolder("RTFS", id);
         
            // This is non-spec compliant but will do for now
            return id;
        }
        else if (m.equals(IS_IDENTICAL)) {
            // MF FIXME
            // See above, this is not correct but works for now (do jboss1.0 PKHolder hack in here)
            return isIdentical(args[0], id);
        }
      
        // If not taken care of, go on and call the container
        else {
            return invokeContainer(id, m, args);
        }
    }

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------

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
        out.writeObject(id);
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
        id = in.readObject();
    }
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
