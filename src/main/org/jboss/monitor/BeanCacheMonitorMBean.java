/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.monitor;

import org.jboss.monitor.client.BeanCacheSnapshot;

/**
 *   
 * @see Monitorable
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.3 $
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
