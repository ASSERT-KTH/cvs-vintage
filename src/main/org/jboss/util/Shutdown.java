/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @version $Revision: 1.3 $
 */
public class Shutdown
   implements MBeanRegistration, ShutdownMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":type=Shutdown";
    
   // Attributes ----------------------------------------------------
   Log log = Log.createLog("Shutdown");
   
   List mbeans = new ArrayList();
   MBeanServer server;
   
   // Public  -------------------------------------------------------
   public void shutdown()
   {
      System.exit(0); // This will execute the shutdown hook
   }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(final MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      this.server = server;
      try
      {
         Runtime.getRuntime().addShutdownHook(new Thread()
         {
            public void run()
            {
               System.out.println("Shutting down");
               
               // Make sure all services are down properly
               shutdownServices();
               
               System.out.println("Shutdown complete");
            }
         });
         log.log("Shutdown hook added");
      } catch (Throwable e)
      {
         log.error("Could not add shutdown hook");
      }
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
   }
   
   public void postDeregister()
   {
   }
   
   protected void shutdownServices()
   {
      try
      {
         // Stop services
         server.invoke(new ObjectName(":service=ServiceControl"), "stop", new Object[0] , new String[0]);
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      try
      {
         // Destroy services
         server.invoke(new ObjectName(":service=ServiceControl"), "destroy", new Object[0] , new String[0]);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}


