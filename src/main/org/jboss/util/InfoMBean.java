/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.lang.Object;

/**
 * The management interface for the Info bean.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 * @version $Revision: 1.5 $
 */
public interface InfoMBean
{
   String listMemoryUsage();
   String listSystemInfo();
   String listThreadDump();
   String runGarbageCollector();
}
