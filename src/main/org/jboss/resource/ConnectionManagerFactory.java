/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.io.PrintWriter;
import java.util.Properties;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;

/**
 *   Generates ConnectionManager instances for the container to use.  The
 *   factory may return one ConnectionManager per request, or use the same
 *   ConnectionManager to service multiple ManagedConnectionFactories.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @author Aaron Mulder <ammulder@alumni.princeton.edu>
 *   @version $Revision: 1.1 $
 */
public interface ConnectionManagerFactory {
    /**
     * Default logger for all ConnectionManagers.
     */
    void setLogWriter(PrintWriter writer);

    /**
     * Default TM for all ConnectionManagers.
     */
    void setTransactionManager(TransactionManager tm);

    /**
     * Default subject factory for all ConnectionManagers.
     */
    void setResourceSubjectFactory(ResourceSubjectFactory rsf);

    /**
     * Sets any properties for the factory, which may apply to the factory
     * itself, or by extension, all the ConnectionManagers it creates.
     * These properties should *not* be specific to the factories serviced
     * by the ConnectionManagers.
     */
    void setProperties(Properties props);

    /**
     * Creates and/or configures a ConnectionManager for the specified
     * ManagedConnectionFactory.
     * @param factory The factory to prepare a CM for
     * @param config The factory-specific settings for the CM (such as pool
     *               configuration, etc.)
     * @param name A name to use for this factory configuration, for logging,
     *             management, etc.  May be null, in which case a name will
     *             be generated automatically.
     */
    JBossConnectionManager addManagedConnectionFactory(
                              ManagedConnectionFactory factory,
                              ConnectorConfig config,
                              String name)
                           throws ResourceException;
}