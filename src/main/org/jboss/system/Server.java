/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.io.File;
import java.io.FileFilter;

import java.net.URL;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.RuntimeMBeanException;

import org.jboss.Version;

/**
 * The main container component of a JBoss server instance.
 *      
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1 $
 */
public class Server
   implements ServerMBean
{
   /** Class logger */
   private static final BootstrapLogger log = BootstrapLogger.getLogger(Server.class);

   /** Container for version information. */
   private final Version version = Version.getInstance();
   
   /** The basic configuration for the server. */
   private final ServerConfig config;

   /** The JMX MBeanServer which will serve as our communication bus. */
   private final MBeanServer server;

   /** Wen the server was started. */
   private final Date started;
   
   /** 
    * Creates a new instance of Server.
    *
    * @param config   The basic configuration of the server instance.
    *
    * @throws Exception   Failed to initialize server instance.
    */
   public Server(final ServerConfig config) throws Exception {
      if (config == null)
         throw new IllegalArgumentException("config is null");

      this.config = config;

      log.info("JBoss (MX MicroKernel) " + version);
      
      // remeber when we we started
      started = new Date();
      log.info("Starting General Purpose Architecture (GPA)...");
            
      // Create the MBeanServer
      server = MBeanServerFactory.createMBeanServer(config.getDomain());
      String systemDomain = server.getDefaultDomain() + ".system";
      
      // Register ourselves first
      server.registerMBean(this,
                           new ObjectName(systemDomain,
                                          "service",
                                          "Server"));
      
      // Initialize the MBean libraries repository
      server.registerMBean(ServiceLibraries.getLibraries(),
                           new ObjectName(systemDomain,
                                          "service",
                                          "ServiceLibraries"));
      
      // Initialize spine boot libraries
      initBootLibraries();
         
      // Create MBeanClassLoader for the base system
      ObjectName loaderName = new ObjectName(systemDomain,
                                             "service", 
                                             "ServiceClassLoader");
      MBeanClassLoader mcl = new MBeanClassLoader(loaderName);
      server.registerMBean(mcl, loaderName);
            
      // Set ServiceClassLoader as classloader for the construction of
      // the basic system
      Thread.currentThread().setContextClassLoader(mcl);
                        
      // Setup logging
      server.createMBean("org.jboss.logging.Log4jService", 
                         new ObjectName(systemDomain,
                                        "service", 
                                        "Logging"),
                         loaderName);
      
      // Log the basic configuration elements
      log.info("Home Dir: " + config.getHomeDir());
      log.info("Install URL: " + config.getInstallURL());
      log.info("Configuration URL: " + config.getConfigURL());
      log.info("Spine URL: " + config.getSpineURL());
      log.info("Library URL: " + config.getLibraryURL());
      log.info("Patch URL: " + config.getPatchURL());

      // General Purpose Architecture information
      server.createMBean("org.jboss.system.Info",
                         new ObjectName(systemDomain,
                                        "service", 
                                        "Info"),
                         loaderName);

      // Service Controller
      ObjectName controllerName = 
         server.createMBean("org.jboss.system.ServiceController", 
                            new ObjectName(systemDomain,
                                           "service", 
                                           "ServiceController"),
                            loaderName).getObjectName();
            
      // Install the shutdown hook
      ShutdownHook hook = new ShutdownHook(controllerName);
      Runtime.getRuntime().addShutdownHook(hook);
      log.info("Shutdown hook added");

      // Service Deployer
      ObjectName deployerName = 
         server.createMBean("org.jboss.deployment.ServiceDeployer",
                            new ObjectName(systemDomain,
                                           "service", 
                                           "ServiceDeployer"),
                            loaderName).getObjectName();

      // Ok, now deploy jboss-service.xml
      URL serviceURL = new URL(config.getConfigURL(), "jboss-service.xml");
      server.invoke(deployerName, 
                    "deploy", 
                    new Object[] { serviceURL.toString() },
                    new String[] { "java.lang.String" });

      // Calculate how long it took
      long lapsedTime = System.currentTimeMillis() - started.getTime();
      long minutes = lapsedTime / 60000;
      long seconds = (lapsedTime - 60000 * minutes) / 1000;
      long milliseconds = (lapsedTime -60000 * minutes - 1000 * seconds);

      // Tell the world how fast it was =)
      log.info("Started in " + minutes  + "m:" + 
               seconds  + "s:" +
               milliseconds +"ms");
   }

   /**
    * Initialize the boot libraries.
    */
   private void initBootLibraries() throws Exception {
      // Build the list of URL for the spine to boot
      List list = new ArrayList();
         
      // Add the patch URL.  If the url protocol is file, then
      // add the contents of the directory it points to
      URL patchURL = config.getPatchURL();
      if (patchURL != null) {
         if (patchURL.getProtocol().equals("file")) {
            File dir = new File(patchURL.getFile());
            if (dir.exists()) {
               // Add the local file patch directory
               list.add(dir.toURL());

               // Add the contents of the directory too
               File[] jars = dir.listFiles(new FileFilter()
                  {
                     public boolean accept(File file)
                     {
                        String name = file.getName().toLowerCase();
                        return name.endsWith(".jar") || name.endsWith(".zip");
                     }
                  });
               for (int j = 0; jars != null && j < jars.length; j++)
               {
                  list.add(jars[j].getCanonicalFile().toURL());
               }
            }
         }
         else {
            list.add(patchURL);
         }
      }
      
      // Add configuration directory to be able to load files
      list.add(config.getConfigURL());
      
      // Add the local path stuff
      list.add(new URL(config.getLibraryURL(), "log4j.jar"));
      list.add(new URL(config.getLibraryURL(), "jboss-spine.jar"));
      
      // Create loaders for each URL
      Iterator iter = list.iterator();
      while (iter.hasNext()) {
         URL url = (URL)iter.next();
         
         // Construction of URLClassLoader also registers with ServiceLibraries
         // Should probably remove this "side-effect" and make this more explicit
         
         // This is a boot URL, so key it on itself.
         URLClassLoader loader = new URLClassLoader(url);
      }
   }
   
   /**
    * Shutdown the server, virtual machine and run shutdown hooks.
    *
    * @throws Exception   Failed to shutdown.
    */
   public void shutdown() throws Exception {
      // start in new thread so that we might have a chance to gice positive
      // feed back to requesting client of success.
      new Thread() {
         public void run() {
            log.info("Shutting down");
            System.exit(0); // This will execute the shutdown hook
         }
      }.start();
   }
   
   /** 
    * Forcibly terminates the currently running Java virtual machine.
    */
   public void halt() {
      // start in new thread so that we might have a chance to gice positive
      // feed back to requesting client of success.
      new Thread() {
         public void run() {
            System.err.println("Halting the system now!");
            Runtime.getRuntime().halt(0);
         }
      }.start();
   }
   

   ///////////////////////////////////////////////////////////////////////////
   //                            Runtime Access                             //
   ///////////////////////////////////////////////////////////////////////////
   
   private void logMemoryUsage(final Runtime rt) {
      log.info("Total/free memory: " + rt.totalMemory() + "/" + rt.freeMemory());
   }
   
   public void runGarbageCollector() {
      Runtime rt = Runtime.getRuntime();
      
      logMemoryUsage(rt);
      rt.gc();
      log.info("Hinted to the JVM to run garbage collection");
      logMemoryUsage(rt);
   }

   public void runFinalization() {
      Runtime.getRuntime().runFinalization();
      log.info("Hinted to the JVM to run any pending object finalizations");
   }
   
   /**
    * Enable or disable tracing method calls at the Runtime level.
    */
   public void traceMethodCalls(final Boolean flag) {
      Runtime.getRuntime().traceMethodCalls(flag.booleanValue());
   }
	
   /**
    * Enable or disable tracing instructions the Runtime level.
    */
   public void traceInstructions(final Boolean flag) {
      Runtime.getRuntime().traceInstructions(flag.booleanValue());
   }

   
   ///////////////////////////////////////////////////////////////////////////
   //                          Server Information                           //
   ///////////////////////////////////////////////////////////////////////////

   public Date getStarted() {
      return started;
   }
   
   public Long getTotalMemory() {
      return new Long(Runtime.getRuntime().totalMemory());
   }
   
   public Long getFreeMemory() {
      return new Long(Runtime.getRuntime().freeMemory());
   }
   
   public Long getMaxMemory() {
      /* Uncomment when JDK 1.4 is the base JVM
      return new Long(Runtime.getRuntime().maxMemory());
      */
      return new Long(-1);
   }

   public String getVersion() {
      return version.toString();
   }
   
   public String getHomeDir() {
      return config.getHomeDir().toString();
   }
   
   public String getInstallURL() {
      return config.getInstallURL().toString();
   }
   
   public String getSpineURL() {
      return config.getSpineURL().toString();
   }
   
   public String getConfigURL() {
      return config.getConfigURL().toString();
   }
   
   public String getLibraryURL() {
      return config.getLibraryURL().toString();
   }
   
   public String getPatchURL() {
      URL patchURL = config.getPatchURL();
      if (patchURL == null)
         return null;
      return patchURL.toString();
   }

   
   ///////////////////////////////////////////////////////////////////////////
   //                             Shutdown Hook                             //
   ///////////////////////////////////////////////////////////////////////////

   private class ShutdownHook
      extends Thread
   {
      /** The ServiceController which we will ask to shut things down with. */
      private ObjectName contollerName;
      
      public ShutdownHook(final ObjectName contollerName) {
         super("JBoss Shutdown Hook");
         
         this.contollerName = contollerName;
      }
      
      public void run() {
         log.info("Shutting down all services");
         System.out.println("Shutting down");
                  
         // Make sure all services are down properly
         shutdownServices();
                  
         log.info("Shutdown complete");
         System.out.println("Shutdown complete");
      }

      /**
       * The <code>shutdownServices</code> method calls the one and only
       * ServiceController to shut down all the mbeans registered with it.
       */
      protected void shutdownServices()
      {
         try
         {
            // get the deployed objects from ServiceController
            server.invoke(contollerName,
                          "shutdown",
                          new Object[0],
                          new String[0]);
         }
         catch (MBeanException e)
         {
            log.error("failed to shutdown", e.getTargetException());
         }
         catch (RuntimeMBeanException e)
         {
            log.error("failed to shutdown", e.getTargetException());
         }
         catch (Exception e)
         {
            log.error("failed to shutdown", e);
         }
      }
   }
}
