/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.util.List;
import java.util.ArrayList;

import javax.management.MBeanRegistration;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Category;

/**
 * Shutdown service.  Installs a hook to cleanly shutdown the server and
 * provides the ability to handle user shutdown requests.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.6 $
 */
public class Shutdown
   implements MBeanRegistration, ShutdownMBean
{
   // Constants -----------------------------------------------------

   /** The default object name to use. */
   public static final String OBJECT_NAME = ":type=Shutdown";
   
   // Attributes ----------------------------------------------------

   /** Instance logger. */
   private final Category log = Category.getInstance(Shutdown.class);

   /** The MBean server we are attached to. */
   private MBeanServer server;
   
   // Public  -------------------------------------------------------

   /**
    * Shutdown the virtual machine and run shutdown hooks.
    */
   public void shutdown()
   {
      log.info("Shutting down");
      System.exit(0); // This will execute the shutdown hook
   }

   /**
    * Forcibly terminates the currently running Java virtual machine.
    */
   public void halt()
   {
      System.err.println("Halting the system now!");
      Runtime.getRuntime().halt(0);
   }
   
   // MBeanRegistration implementation ------------------------------

   /**
    * Saves a reference to the MBean server for later use and installs
    * a shutdown hook.
    *
    * @param server    The MBean server which we are going to be registered.
    * @param name      The object name we have been configured to use.
    * @return          Our preferred object name.
    *
    * @throws MalformedObjectNameException
    */
   public ObjectName preRegister(final MBeanServer server,
                                 final ObjectName name)
      throws Exception
   {
      this.server = server;
      try
      {
         Runtime.getRuntime().addShutdownHook(new Thread("JBoss Shutdown Hook")
         {
            public void run()
            {
               log.info("Shutting down all services");
               System.out.println("Shutting down");
               
               // Make sure all services are down properly
               shutdownServices();

               log.info("Shutdown complete");
               System.out.println("Shutdown complete");
            }
         });
         
         log.info("Shutdown hook added");
      } catch (Throwable e)
      {
         log.error("Could not add shutdown hook", e);
      }
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
   
   public void postRegister(Boolean registrationDone)
   {
      // empty
   }
   
   public void preDeregister() throws Exception
   {
      // empty
   }
   
   public void postDeregister()
   {
      // empty
   }

   /**
    * Attempt to <em>stop</em> and <em>destroy</em> all services
    * running inside of the MBean server which we are attached too by
    * asking the <tt>ServiceControl</tt> to do the dirty work.
    */
   protected void shutdownServices()
   {
      try
      {
         // Stop services
         server.invoke(new ObjectName(":service=ServiceControl"),
                       "stop", new Object[0] , new String[0]);
      } catch (Exception e)
      {
         log.error("failed to stop services", e);
      }

      try
      {
         // Destroy services
         server.invoke(new ObjectName(":service=ServiceControl"),
                       "destroy", new Object[0] , new String[0]);
      } catch (Exception e)
      {
         log.error("failed to destroy services", e);         
      }
   }
}


