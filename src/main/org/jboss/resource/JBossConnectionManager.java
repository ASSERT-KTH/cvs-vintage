/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

/**
 *   Custom extensions to the standard ConnectionManager interface.  This
 *   supports re-enlisting connections under new transactions, and closing
 *   down the ConnectionManager.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @author Aaron Mulder <ammulder@alumni.princeton.edu>
 *   @version $Revision: 1.2 $
 */
public interface JBossConnectionManager extends ConnectionManager {
    /**
     * Reuses connections for different transactions.  This call tells the
     * ConnectionManager that a connection generated previously under a
     * different transaction is now going to be reused under the current
     * transaction.  The connection must never have been closed for this to
     * work (i.e. stuffed in an instance variable of a stateful session bean).
     */
    void enlistExistingConnection(Object connection) throws ResourceException;

    /**
     * Closes all pools, connections, etc.
     */
    void shutDown();
}