/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.util.Hashtable;
import java.util.List;
import java.io.Serializable;
import java.net.InetAddress;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.naming.NamingServiceMBean;

/**
 * The default implementaion of {@link InitialContextHandle}.
 *
 * <p>Attempts to discover the requried enviroment properties by inspecting
 *    the current system.  This implementation is currently specific to
 *    the <tt>org.jnp.*</tt> naming classes.
 *      
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.2 $
 */
public final class DefaultInitialContextHandle
    extends InitialContextHandle
{
    /** Serial Version Identifier. */
    private static final long serialVersionUID = -7164179332281875677L;
    
    /** The InitialContext enviroment (or null if unable to determine) */
    private Hashtable env;

    /**
     * Construct a <tt>DefaultInitialContextHandle</tt>.
     */
    public DefaultInitialContextHandle() {
        // save the current enviroment
        env = getEnvironment();
    }

    /**
     * Discover the URL of the local naming server.
     */
    private String getUrl() throws Exception {
        // discover the hostname to use
        String hostname = System.getProperty("java.rmi.server.hostname");
        if (hostname == null) {
            hostname = InetAddress.getLocalHost().getHostName();
        }

        // get the port to use
        List list = MBeanServerFactory.findMBeanServer(null);
        if (list.size() == 0) {
            throw new RuntimeException("no MBean servers found");
        }

        // for now just assume that is the first one
        MBeanServer server = (MBeanServer)list.get(0);
        
        // ask the naming service for the port
        Integer port = (Integer)
            server.invoke(new ObjectName(NamingServiceMBean.OBJECT_NAME),
                          "getPort",
                          new Object[] {}, 
                          new String[] {});

        // construct a new url
        return hostname + ":" + port;
    }
    
    /**
     * Get the environment table suitable for passing to an initial context
     * or null if we should use a vanilla one.
     */
    private Hashtable getEnvironment() {
        Hashtable map = new Hashtable();

        try {
            map.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.jnp.interfaces.NamingContextFactory");
            
            map.put(Context.URL_PKG_PREFIXES,
                    "org.jboss.naming:org.jnp.interfaces");

            map.put(Context.PROVIDER_URL, getUrl());
            
            return map;
        }
        catch (Exception e) {
            throw new RuntimeException
                ("failed to discover environment properties: " + e);
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
     * A factory for producing {@link DefaultInitialContextHandle}
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
                        instance = new DefaultInitialContextHandle();
                    }
                }
            }

            return instance;
        }
    }
}
