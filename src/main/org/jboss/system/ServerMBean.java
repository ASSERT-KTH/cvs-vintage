/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.Date;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

/**
 * The JMX MBean interface for the <tt>Server</tt> component.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.8 $
 */
public interface ServerMBean
{
   /** The default JMX object name for this MBean. */
   ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system", "service", "Server");
   
   //
   // Should eventually expose init, start & stop to allow admin clients
   // to manage the state of the server dynamically.
   //

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
}
