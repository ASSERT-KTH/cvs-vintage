/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Provides the interface for creating new handles instances and
 * for getting a reference to the initial context.
 *      
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.3 $
 */
public abstract class InitialContextHandle
    implements Serializable
{
    /** Serial Version Identifier. */
    private static final long serialVersionUID = 8271304971930101243L;

    /** The factory for producing handles. */
    private static InitialContextHandleFactory factory = null;
    
    /**
     * Lookup the class of the factory that will be used to construct
     * new handle instances.
     */
    private static Class getFactoryType() {
        String propname = InitialContextHandle.class.getName() + ".factory";
        String classname = System.getProperty(propname, null);

        Class type;
        if (classname != null) {
            try {
                type = Class.forName(classname);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException
                    ("invalid factory class name: " + classname);
            }

            // check if the given class is the correct type before
            // attempting to construct a new object
            if (! InitialContextHandleFactory.class.isAssignableFrom(type)) {
                throw new RuntimeException("does not implement: " +
                                           InitialContextHandleFactory.class);
            }
        }
        else {
            type = DefaultInitialContextHandle.Factory.class;
        }
        
        return type;
    }
    
    /**
     * Construct a new factory if one has not been created yet.
     */
    private static synchronized void createFactory() {
        if (factory != null) return;

        // get the type of factory that will be used
        Class type = getFactoryType();

        // create a new instance
        try {
            factory = (InitialContextHandleFactory)type.newInstance();
        }
        catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException)e;
            
            // should really use a nesting exception here to preserve
            // the target throwables detail.
            throw new RuntimeException("failed to construct factory: " + e);
        }
    }
    
    /**
     * Factory method for producting state objects.
     *
     * @return  A state object.
     */
    public static InitialContextHandle create() {
        // lazy initialize the factory
        if (factory == null) {
            createFactory();
        }

        return factory.create();
    }

    /**
     * Get the initial context for this handle.
     *
     * @return <tt>InitialContext</tt>.
     *
     * @throws NamingException    Failed to create <tt>InitialContext</tt>.
     */
    public abstract InitialContext getInitialContext() throws NamingException;
}
