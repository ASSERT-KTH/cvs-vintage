/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * Interface that gives synchronization semantic to implementors
 *
 * @see Semaphore
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.3 $
 */
public interface Sync 
{
	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------
	/**
	 * Acquires this sync
	 * @see #release
	 */
	public void acquire() throws InterruptedException;
	/**
	 * Releases this sync
	 * @see #acquire
	 */
	public void release();
}
