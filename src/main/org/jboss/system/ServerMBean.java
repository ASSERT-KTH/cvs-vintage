/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.Map;
import java.util.Date;

/**
 * The JMX MBean interface for the <tt>Server</tt> component.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.2 $
 */
public interface ServerMBean
{
   //
   // Should eventually expose init, start & stop to allow admin clients
   // to manage the state of the server dynamically.
   //
   
   /**
    * Shutdown the server.
    *
    * @throws Exception   Failed to shutdown.
    */
   void shutdown() throws Exception;
   
   /** 
    * Forcibly terminates the currently running Java virtual machine.
    */
   void halt();

   
   ///////////////////////////////////////////////////////////////////////////
   //                            Runtime Access                             //
   ///////////////////////////////////////////////////////////////////////////

   void runGarbageCollector();

   void runFinalization();

   /**
    * Enable or disable tracing method calls at the Runtime level.
    */
   void traceMethodCalls(Boolean flag);
   
   /**
    * Enable or disable tracing instructions the Runtime level.
    */
   void traceInstructions(Boolean flag);
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                          Server Information                           //
   ///////////////////////////////////////////////////////////////////////////

   Date getStarted();
   
   Long getTotalMemory();
   
   Long getFreeMemory();

   Long getMaxMemory();
   
   String getVersion();

   String getVersionName();

   String getBuildNumber();

   String getBuildID();

   String getBuildDate();
   
   String getHomeDir();
   
   String getInstallURL();
   
   String getSpineURL();
   
   String getConfigURL();
   
   String getLibraryURL();
   
   String getPatchURL();
}
