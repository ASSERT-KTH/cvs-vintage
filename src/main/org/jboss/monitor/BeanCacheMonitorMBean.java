/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.monitor;

import org.jboss.monitor.client.BeanCacheSnapshot;

/**
 *   
 * @see Monitorable
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.1 $
 */
public interface BeanCacheMonitorMBean
{
	// Constants ----------------------------------------------------
	
	// Static -------------------------------------------------------

	// Public -------------------------------------------------------
	/**
	 * Returns the cache data at the call instant.
	 * @return null if a problem is encountered while sampling the cache,
	 * otherwise an array (possibly of size 0) with the cache data.
	 */
	public BeanCacheSnapshot[] getSnapshots();

	// Inner classes -------------------------------------------------
}
