/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.logging;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import org.jboss.util.ThrowableHandler;
import org.jboss.util.ThrowableListener;

import org.jboss.system.BootstrapLogger;

import org.jboss.logging.log4j.CategoryStream;

/**
 * Initializes the Log4j logging framework.  Supports XML and standard
 * configuration file formats.  Defaults to using 'log4j.xml' read
 * from a system resource.
 *
 * <p>Sets up a {@link ThrowableListener} to adapt unhandled
 *    throwables to a category.
 *
 * <p>Installs CategoryStream adapters for System.out and System.err
 *    to catch and redirect calls to Log4j.
 *
 * @version <tt>$Revision: 1.19 $</tt>
 * @author <a href="mailto:phox@galactica.it">Fulco Muriglio</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>
 * @author <a href="mailto:davidjencks@earthlink.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Log4jService
   implements Log4jServiceMBean, MBeanRegistration
{
   /**
    * The default path for the configuration file.  Reads the value
    * from the system property <tt>org.jboss.logging.Log4jService.configfile</tt>
    * or if that is not set defaults to <tt>log4j.xml</tt>.
    */
   public static final String DEFAULT_PATH =
      System.getProperty(Log4jService.class.getName() + ".configfile", "log4j.xml");

   /**
    * Default flag to enable/disable cacthing System.out.  Reads the value
    * from the system property <tt>org.jboss.logging.Log4jService.catchSystemOut</tt>
    * or if not set defaults to <tt>true</tt>.
    */
   public static final boolean CATCH_SYSTEM_OUT =      
      getBoolean(Log4jService.class.getName() + ".catchSystemOut", true);

   /**
    * Default flag to enable/disable cacthing System.err.  Reads the value
    * from the system property <tt>org.jboss.logging.Log4jService.catchSystemErr</tt>
    * or if not set defaults to <tt>true</tt>.
    */
   public static final boolean CATCH_SYSTEM_ERR =
      getBoolean(Log4jService.class.getName() + ".catchSystemErr", true);

   private static final BootstrapLogger log = BootstrapLogger.getLogger(Log4jService.class);

   /** Helper to get boolean value from system property or use default if not set. */
   private static boolean getBoolean(String name, boolean defaultValue)
   {
      String value = System.getProperty(name, null);
      if (value == null)
         return defaultValue;
      return new Boolean(value).booleanValue();
   }
   
   // Attributes ----------------------------------------------------

   private String configurationPath;
   private int refreshPeriod;
   private ThrowableListenerLoggingAdapter throwableAdapter;

   /** The previous value of System.out. */
   private PrintStream out;

   /** The previous value of System.err. */
   private PrintStream err;

   // Constructors --------------------------------------------------
   
   public Log4jService()
   {
      this(DEFAULT_PATH, 60);
   }
   
   public Log4jService(String path)
   {
      this(path, 60);
   }
   
   /**
    * @param path             The path to the configuration file
    * @param refreshPeriod    The refreshPeriod in seconds to wait between
    *                         each check.
    */
   public Log4jService(final String path, final int refreshPeriod)
   {
      this.configurationPath = path;
      this.refreshPeriod = refreshPeriod;

      out = System.out;
      err = System.err;
   }

   public int getRefreshPeriod()
   {
      return refreshPeriod;
   }
   
   /**
    * Get the log4j.properties format config file path
    */
   public String getConfigurationPath()
   {
      return configurationPath;
   }
   
   /**
    * Set the log4j.properties format config file path
    */
   public void setConfigurationPath(String path)
   {
      this.configurationPath = path;
   }

   /**
    * Configures the log4j framework using the current service properties
    * and sets the service category to the log4j root Category. This method
    * throws a FileNotFoundException exception if the current
    * configurationPath cannot be located to avoid interaction problems
    * between the log4j framework and the JBoss ConsoleLogging service.
    */
   public void start() throws Exception
   {
      log.debug("Configuration path: " + configurationPath);
      
      // Make sure the config file can be found
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL url = loader.getResource(configurationPath);
      if (url == null)
      {
         throw new FileNotFoundException
            ("Failed to find logj4 configuration: " + configurationPath);
      }
      log.debug("Configuration URL: " + url);
      
      // configurationPath is a file path
      String path = url.getFile();
      boolean isXML = configurationPath.endsWith(".xml");
      log.debug("isXML: " + isXML);
      if (isXML)
      {
         DOMConfigurator.configureAndWatch(path, 1000 * refreshPeriod);
      }
      else
      {
         PropertyConfigurator.configureAndWatch(path, 1000 * refreshPeriod);
      }

      // Make sure Category has loaded
      Category category = Category.getRoot();
      
      // Mark the BootstrapLogger as initialized
      BootstrapLogger.LOG4J_INITIALIZED = true;
      
      // Install listener for unhandled throwables to turn them into log messages
      throwableAdapter = new ThrowableListenerLoggingAdapter();
      ThrowableHandler.addThrowableListener(throwableAdapter);
      log.debug("Added ThrowableListener: " + throwableAdapter);
      
      // Install catchers
      if (CATCH_SYSTEM_OUT)
      {
         category = Category.getInstance("STDOUT");
         System.setOut(new CategoryStream(category, Priority.INFO, out));
         log.debug("Install System.out adapter");
      }
      if (CATCH_SYSTEM_ERR)
      {
         category = Category.getInstance("STDERR");
         System.setErr(new CategoryStream(category, Priority.ERROR, err));
         log.debug("Install System.err adapter");
      }

      log.info("Started");
   }
   
   /**
    * Stops the log4j framework by calling the Category.shutdown() method.
    * 
    * @see org.apache.log4j.Category#shutdown()
    */
   public void stop()
   {
      // Remove throwable adapter
      ThrowableHandler.removeThrowableListener(throwableAdapter);

      // Remove System adapters
      if (out != null) {
         System.out.flush();
         System.setOut(out);
      }
      if (err != null) {
         System.err.flush();
         System.setErr(err);
      }
      
      // Shutdown Log4j
      Category.shutdown();
      
      log.debug("Stopped");
   }

   // Public --------------------------------------------------------
   
   // --- Begin MBeanRegistration interface methods
   
   /**
    * Invokes {@link start} to configure the Log4j framework.
    * 
    * @return the name of this mbean.
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      start();

      return name == null ? OBJECT_NAME : name;
   }
   
   public void postRegister(Boolean b)
   {
   }
   
   public void preDeregister()
   {
   }
   
   public void postDeregister()
   {
   }
   
   // --- End MBeanRegistration interface methods

   /**
    * Adapts ThrowableHandler to the Loggger interface.  Using nested 
    * class instead of anoynmous class for better category naming.
    */
   private static class ThrowableListenerLoggingAdapter
      implements ThrowableListener
   {
      private Logger log = Logger.getLogger(ThrowableListenerLoggingAdapter.class);
      
      public void onThrowable(int type, Throwable t)
      {
         switch (type)
         {
             default:
                // if type is not valid then make it any error
             
             case ThrowableHandler.Type.ERROR:
                log.error("unhandled throwable", t);
                break;
             
             case ThrowableHandler.Type.WARNING:
                log.warn("unhandled throwable", t);
                break;
             
             case ThrowableHandler.Type.UNKNOWN:
                // these could be red-herrings, so log them as trace
                log.trace("unhandled throwable, status is unknown", t);
                break;
         }
      }
   }
}
