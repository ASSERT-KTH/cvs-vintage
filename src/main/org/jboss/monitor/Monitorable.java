/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.monitor;

/**
 *   
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.1 $
 */
public interface Monitorable
{
	// Constants ----------------------------------------------------
	
	// Static -------------------------------------------------------

	// Public -------------------------------------------------------
	/**
	 * Samples the status of the implementor object and register the status
	 * into the snapshot argument.
	 */
	public void sample(Object snapshot);

	// Inner classes -------------------------------------------------
}
