/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * Interface that gives wait - notify primitives to implementors.
 *
 * @see Semaphore
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.1 $
 */
public interface WaitSync extends Sync
{
	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------
	/**
	 * Pone in wait status this sync, until {@link #doNotify} is called to wake it up.
	 * @see #doNotify
	 */
	public void doWait() throws InterruptedException;
	/**
	 * Wakes up this sync that has been posed in wait status by a {@link #doWait} call.
	 * If this sync is not waiting, invoking this method should have no effect.
	 * @see #doWait
	 */
	public void doNotify() throws InterruptedException;
}
