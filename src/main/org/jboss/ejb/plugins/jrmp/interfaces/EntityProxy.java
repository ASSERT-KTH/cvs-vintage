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


import org.jboss.ejb.CacheKey;

/**
 * An EJB entity bean proxy class.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.28 $
 */
public class EntityProxy
    extends BeanProxy
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = -1523442773137704949L;
    
    // Attributes ----------------------------------------------------

    /** The primary key of the entity bean. */
    protected CacheKey cacheKey;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------

    /**
     * No-argument constructor for externalization.
     */
    public EntityProxy() {}

    /**
     * Construct a <tt>EntityProxy</tt>.
     *
     * @param name          The JNDI name of the container that we proxy for.
     * @param container     The remote interface of the invoker for which
     *                      this is a proxy for.
     * @param id            The primary key of the entity.
     * @param optimize      True if the proxy will attempt to optimize
     *                      VM-local calls.
     *
     * @throws NullPointerException     Id may not be null.
     */
    public EntityProxy(final String name,
                       final ContainerRemote container,
                       final Object id,
                       final boolean optimize)
    {
       super(name, container, optimize);
       
       if (id == null)
           throw new NullPointerException("Id may not be null");

       // make sure that our ide is a CacheKey
       if (id instanceof CacheKey) {
           this.cacheKey = (CacheKey)id;
       }
       else {
           // In case we pass the Object or anything else we encapsulate
           cacheKey = new CacheKey(id);
       }
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
           return name + ":" + cacheKey.getId().toString();
       }
       else if (m.equals(EQUALS)) {
           return invoke(proxy, IS_IDENTICAL, args);
       }
       else if (m.equals(HASH_CODE)) {
         return new Integer(cacheKey.getId().hashCode());
       }
       
       // Implement local EJB calls
       else if (m.equals(GET_HANDLE)) {
           return new EntityHandleImpl(initialContextHandle, name, cacheKey.getId());
       }
       else if (m.equals(GET_PRIMARY_KEY)) {
           return cacheKey.getId();
       }
	   else if (m.equals(GET_EJB_HOME)) {
           return getEJBHome();
       }
       else if (m.equals(IS_IDENTICAL)) {
           return isIdentical(args[0], cacheKey.getId());
       }
       
       // If not taken care of, go on and call the container
       else {
           return invokeContainer(cacheKey, m, args);
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
       out.writeObject(cacheKey);
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
       cacheKey = (CacheKey)in.readObject();
    }
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}

