/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.pools;

/**
 * A listener for object pool events.
 * @version $Revision: 1.2 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface PoolEventListener {
    /**
     * The pooled object was closed and should be returned to the pool.
     */
    public void objectClosed(PoolEvent evt);
    /**
     * The pooled object had an error and should be returned to the pool.
     */
    public void objectError(PoolEvent evt);
    /**
     * The pooled object was used and its timestamp should be updated.
     */
    public void objectUsed(PoolEvent evt);
}