/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.util.Hashtable;
import java.util.Properties;
import java.io.Serializable;
import java.io.InputStream;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * An implementation of {@link InitialContextHandle} that reads a properties
 * file from a configured URL for the environment.
 *      
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.3 $
 */
public final class PropertiesInitialContextHandle
    extends InitialContextHandle
{
    /** Serial Version Identifier. */
    private static final long serialVersionUID = -7458882217772107915L;
    
    /** The InitialContext enviroment (or null if unable to determine) */
    private Hashtable env;

    /**
     * Construct a <tt>PropertiesInitialContextHandle</tt>.
     */
    public PropertiesInitialContextHandle() {
        // save the current enviroment
        env = getEnvironment();
    }

    /**
     * Get the environment table suitable for passing to an initial context
     * or null if we should use a vanilla one.
     */
    private Hashtable getEnvironment() {
        String propname =
            PropertiesInitialContextHandle.class.getName() +
            ".environment";
        String spec = System.getProperty(propname, null);
        
        if (spec == null)
            return null;

        try {
            URL url = new URL(spec);
            InputStream input = url.openStream();
            Properties props;
            
            try {
                props = new Properties();
                props.load(input);
            }
            finally {
                input.close();
            }

            return props;
        }
        catch (Exception e) {
            throw new RuntimeException
                ("failed to load environment properties: " + e);
        }
    }

    /**
     * Get the initial context for this handle.
     *
     * @return <tt>InitialContext</tt>.
     *
     * @throws NamingException    Failed to create <tt>InitialContext</tt>.
     */
    public InitialContext getInitialContext() throws NamingException {
        InitialContext ctx;

        // if the environment is not null, then use it else
        // assume there is a system property set.
        if (env != null) {
            ctx = new InitialContext(env);
        }
        else {
            ctx = new InitialContext();
        }

        return ctx;
    }

    /**
     * A factory for producing {@link PropertiesInitialContextHandle}
     * instance objects.
     *
     * <p>Only one instance is created to help minimize the overhead
     *    required to inspect the running configuration.
     */
    public static final class Factory
        implements InitialContextHandleFactory
    {
        /** The single handle instace. */
        private InitialContextHandle instance;
        
        /**
         * Creates an initial context handle suitable creating an initial
         * context for the current virtual machine.
         *
         * @return  An initial context handle suitable for the current vm.
         */
        public InitialContextHandle create() {
            // only synchronize once during initialization
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) {
                        instance = new PropertiesInitialContextHandle();
                    }
                }
            }

            return instance;
        }
    }
}
