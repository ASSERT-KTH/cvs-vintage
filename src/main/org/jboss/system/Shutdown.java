/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.ListIterator;import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.RuntimeMBeanException;
import org.apache.log4j.Category;

/**
 * Shutdown service.  Installs a hook to cleanly shutdown the server and
 * provides the ability to handle user shutdown requests.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.10 $
 */
public class Shutdown implements MBeanRegistration, ShutdownMBean
{
   // Constants -----------------------------------------------------
   // Attributes ----------------------------------------------------
   
   /** Instance logger. */
   private final Category log = Category.getInstance(Shutdown.class);
   
   /** The MBean server we are attached to. */
   private MBeanServer server;
   // Public  -------------------------------------------------------
   
   /** Shutdown the virtual machine and run shutdown hooks. */
   public void shutdown()
   {
      log.info("Shutting down");
      System.exit(0); // This will execute the shutdown hook
   }
   
   /** Forcibly terminates the currently running Java virtual machine. */
   public void halt()
   {
      System.err.println("Halting the system now!");
      Runtime.getRuntime().halt(0);
   }
   // MBeanRegistration implementation ------------------------------
   
   /**
    * Saves a reference to the MBean server for later use and installs a shutdown hook.
    * @param server    The MBean server which we are going to be registered.
    * @param name      The object name we have been configured to use.
    * @return          Our preferred object name.
    * @throws MalformedObjectNameException
    */
   public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception
   {
      this.server = server;
      try
      {
         Runtime.getRuntime().addShutdownHook(
            new Thread("JBoss Shutdown Hook")
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
            }
         );
         log.info("Shutdown hook added");
      }
      catch (Throwable e)
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
    * The <code>shutdownServices</code> method  calls the one and only
    * ServiceController to shut down all the  mbeans registered with it.
    * We could make the ServiceController an mbean-ref...
    *
    */
   protected void shutdownServices()
   {
      try
      {
         // set to true for detailed name printouts
         boolean verbose = false;
         // get the deployed objects from ServiceController
         server.invoke(new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
            "shutdown",
            new Object[0],
            new String[0]);
      }
      catch (RuntimeMBeanException rmbe)
      {
         rmbe.getTargetException().printStackTrace();
      }
      catch (Exception e)
      {
         log.error("failed to destroy services", e);
      }
   }
}
