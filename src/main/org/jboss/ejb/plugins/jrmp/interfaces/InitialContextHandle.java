/*
 * JBoss, the OpenSource EJB server
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
 * A simple handle to facilitate serialization of an initial context.
 *      
 * @author  Jason Dillon <a href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</a>
 * @version $Revision: 1.1 $
 */
public class InitialContextHandle
    implements Serializable
{
    /** Serial Version Identifier. */
    private static final long serialVersionUID = 5716858030389936723L;
    
    /**
     * The property name, which is a URL spec of a properties file to read
     */
    public static final String ENV_PROPERTIES =
        InitialContextHandle.class.getName() + ".environment";

    /** The single instance. */
    private static InitialContextHandle instance = null;
    
    /**
     * Factory method for producting state objects.
     *
     * @return  A state object.
     */
    public static InitialContextHandle create() {
        //
        // This should do the right thing *most* of the time with respect
        // to multi-threadded access.  If there is a concurrency problem
        // then more than one object will be created, but they should be
        // the same, so rather than sync lets just let that happen.
        //
        if (instance == null) {
            instance = new InitialContextHandle();
        }

        return instance;
    }
    
    /** The InitialContext enviroment (or null if unable to determine) */
    private Hashtable env;

    /**
     * Construct a <tt>InitialContextHandle</tt>.
     */
    public InitialContextHandle() {
        // save the current enviroment
        env = getEnvironment();
    }

    /**
     * Get the environment table suitable for passing to an initial context
     * or null if we should use a vanilla one.
     *
     * <p>Checks for a non-null value for the system property
     *    {@link #ENV_PROPERTIES}.  If it finds one, it assumes that
     *    it is a url specification which can be used to populate a
     *    properties table.
     */
    private Hashtable getEnvironment() {
        String spec = System.getProperty(ENV_PROPERTIES);
        
        try {
            if (spec != null) {
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
        }
        catch (Exception ignore) {}

        return null;
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
}
