/*
 * JBoss, the OpenSource J2EE webOS
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
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.7 $
 */
public interface InfoMBean
{
   String listMemoryUsage();
   String listSystemInfo();
   String listThreadDump();
   String runGarbageCollector();
   void traceMethodCalls(boolean flag);
   void traceInstructions(boolean flag);
}
