/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.local;

import java.lang.reflect.Method;

import javax.naming.Name;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

/**
 * The client-side proxy for an EJB Home object.
 *      
 * @author  <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 */
public abstract class LocalHomeProxy
    extends LocalProxy
{
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
            final Class type = EJBLocalHome.class;

            REMOVE_BY_PRIMARY_KEY = type.getMethod("remove", new Class[] { 
                Object.class 
            });

            // Get the "remove" method from the EJBObject
            REMOVE_OBJECT = EJBLocalObject.class.getMethod("remove", empty);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);            
        }
    }
    
    
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
       String jndiName = getJndiName();
        // Implement local methods
        if (m.equals(TO_STRING)) {
            return jndiName + "Home";
        }
        else if (m.equals(EQUALS)) {
            // equality of the proxy home is based on names...
            Object temp = invoke(proxy, TO_STRING, args);
            return new Boolean(temp.equals(jndiName + "Home"));
        }
        else if (m.equals(HASH_CODE)) {
            return new Integer(this.hashCode());
        }
        
        // Implement local EJB calls
       return null;
    }
}

