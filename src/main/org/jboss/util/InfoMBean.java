/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.lang.Object;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 *   @version $Revision: 1.4 $
 */
public interface InfoMBean {
	public String listMemoryUsage();
	public String listSystemInfo();
	public String listThreadDump();
}