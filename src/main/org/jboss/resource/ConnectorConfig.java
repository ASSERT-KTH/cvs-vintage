/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.io.PrintWriter;
import java.util.Properties;
import javax.transaction.TransactionManager;

/**
 *   Provides access to configuration parameters for a JCA ConnectionManager.
 *   Specifically, all the properties for a single ResourceAdapter configuration
 *   that the ConnectionManager will use.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @author Aaron Mulder <ammulder@alumni.princeton.edu>
 *   @version $Revision: 1.1 $
 */
public class ConnectorConfig {
    /**
     * Listens for Connection open/close events.  Used by the container to
     * track which beans use which connections, so that if a connection is
     * held by a bean across many transactions, the container can tell the
     * ConnectionManager to re-enlist it with each transaction as the
     * transactions are started.
     */
    public JBossConnectionListener listener;

    /**
     * Holds any implementation-specific properties (such as connection pool
     * configuration).
     */
    public Properties properties;

    /**
     * Used for logging.
     */
    public PrintWriter logWriter;

    /**
     * The TransactionManager to use.
     */
    public TransactionManager tm;

    /**
     * Fetches the current Subject for a Connector request.
     */
    public ResourceSubjectFactory rsf;

    /**
     * Whether an existing connection can be switched to use different
     * authentication settings.  Not possible for JDBC connections, where
     * you specify a username and password when you create the connection,
     * and there's no way to change them later.  May be supported by other
     * Resource Adapters.
     */
    public boolean isReauthenticationSupported;
}
