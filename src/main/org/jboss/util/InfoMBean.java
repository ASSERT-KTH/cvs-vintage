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
 * @version $Revision: 1.8 $
 */
public interface InfoMBean
{
   public String listMemoryUsage();
   public String listSystemInfo();
   public String listThreadDump();
   public String runGarbageCollector();
   /** Display the java.lang.Package info for the pkgName  */
   public String displayPackageInfo(String pkgName);
   public void traceMethodCalls(boolean flag);
   public void traceInstructions(boolean flag);
}
