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
 * @version $Revision: 1.5 $
 */
public interface ServerMBean
{
   //
   // Should eventually expose init, start & stop to allow admin clients
   // to manage the state of the server dynamically.
   //

   /**
    * Enable or disable exiting the JVM when {@link #shutdown} is called.
    * If enabled, then shutdown calls {@link #exit}.  If disabled, then
    * only the shutdown hook will be run.
    *
    * @param flag    True to enable calling exit on shutdown.
    */
   void setExitOnShutdown(boolean flag);

    /**
     * Get the current value of the exit on shutdown flag.  Default value is
     * false, though it will be set to true when bootstrapped with 
     * {@link org.jboss.Main}.
     *
     * @return    The current value of the exit on shutdown flag.
     */
   boolean getExitOnShutdown();

   /**
    * Shutdown the server and run shutdown hooks.  If the exit on shutdown
    * flag is true, then {@link exit} is called, else only the shutdown hook 
    * is run.
    */
   void shutdown();

   /**
    * Shutdown the server, the JVM and run shutdown hooks.
    *
    * @param exitcode   The exit code returned to the operating system.
    */
   void exit(int exitcode);

   /**
    * Shutdown the server, the JVM and run shutdown hooks. Exits with 
    * code 1. 
    */
   void exit();
   
   /** 
    * Forcibly terminates the currently running Java virtual machine.
    *
    * @param exitcode   The exit code returned to the operating system.
    */
   void halt(int exitcode);

   /** 
    * Forcibly terminates the currently running Java virtual machine. 
    * Exits with code 1. 
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
