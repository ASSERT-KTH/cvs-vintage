/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

/**
 *   Listens for connection open/close events.  Used by the EJB container to
 *   track which beans use which resources.  That way if a bean keeps a
 *   resource across several transactions, the container will know to
 *   re-enlist it each time.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @author Aaron Mulder <ammulder@alumni.princeton.edu>
 *   @version $Revision: 1.1 $
 */
public interface JBossConnectionListener {
    /**
     * A new Connection was issued.  Known as a handle because the instance of
     * ManagedConnection represents the "physical" connection, and the clients
     * only receive handles to it, not the real thing.
     */
    void connectionHandleIssued(Object connection);

    /**
     * A connection handle was closed.  The physical connection is probably
     * still open, but the client cannot access it any more.
     */
    void connectionHandleClosed(Object connection);
}