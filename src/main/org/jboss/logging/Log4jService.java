/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.logging;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import org.jboss.util.ThrowableHandler;
import org.jboss.util.ThrowableListener;

/**
 * This is a JMX MBean that provides three features:
 * 
 * <ol>
 * <li>It initalizes the log4j framework from the log4j properties format file
 *     specified by the ConfigurationPath attribute to that the log4j may be
 *     used by JBoss components.
 * <li>It uses the log name as the category to log under, allowing you to turn 
 *     individual components on and off using the log4j configuration file
 *     (automatically reloaded frequently).
 * </ol>
 * 
 * @author <a href="mailto:phox@galactica.it">Fulco Muriglio</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>
 * @author <a href="mailto:davidjencks@earthlink.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.16 $
 */
public class Log4jService
   implements Log4jServiceMBean, MBeanRegistration
{
   /**
    * The default path, either read from system properties or if not set
    * default to log4j.properties.
    *
    * <p>Note, this is a minor HACK to allow loading a different
    *    logging configuration file other that log4j.properties.  This should
    *    be fixed by exposing more configuration in the boot strapping
    *    xml snippet.
    */
   public static final String DEFAULT_PATH =
      System.getProperty(Log4jService.class.getName() + ".configfile", "log4j.properties");

   // Attributes ----------------------------------------------------

   private Category category;
   private String configurationPath;
   private int refreshPeriod;
   private boolean refreshFlag;

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
    * @param path             The path to the log4j.properties format file
    * @param refreshPeriod    The refreshPeriod in seconds to wait between
    *                         each check.
    */
   public Log4jService(final String path, final int refreshPeriod)
   {
      this.configurationPath = path;
      this.refreshPeriod = refreshPeriod;
      this.refreshFlag = true;
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
    * Get the refresh flag. This determines if the log4j.properties file
    * is reloaded every refreshPeriod seconds or not.
    */
   public boolean getRefreshFlag()
   {
      return refreshFlag;
   }
   
   /**
    * Set the refresh flag. This determines if the log4j.properties file
    * is reloaded every refreshPeriod seconds or not.
    */
   public void setRefreshFlag(boolean flag)
   {
      this.refreshFlag = flag;
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
      // See if this is an xml configuration file
      boolean isXML = configurationPath.endsWith(".xml");
      
      // Make sure the config file can be found
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL url = loader.getResource(configurationPath);
      if (url == null) {
         throw new FileNotFoundException
            ("Failed to find logj4 configuration: " + configurationPath);
      }

      if (refreshFlag)
      {
         // configurationPath is a file path
         String path = url.getFile();
         if (isXML) {
            DOMConfigurator.configureAndWatch(path, 1000 * refreshPeriod);
         }
         else {
            PropertyConfigurator.configureAndWatch(path, 1000 * refreshPeriod);
         }
      }
      else
      {
         if (isXML) {
            DOMConfigurator.configure(url);
         }
         else {
            PropertyConfigurator.configure(url);
         }
      }
      
      this.category = Category.getRoot();
      category.info("Started Log4jService, config=" + url);

      // Install listener for unhandled throwables to turn them into log messages
      ThrowableHandler.addThrowableListener(new ThrowableListenerLoggingAdapter());
   }
   
   /**
    * Stops the log4j framework by calling the Category.shutdown() method.
    * @see org.apache.log4j.Category#shutdown()
    */
   public void stop()
   {
      Category.shutdown();
      if (category != null) {
         category.info("Stopped Log4jService");
      }
   }

   // Public --------------------------------------------------------
   
   // --- Begin MBeanRegistration interface methods
   
   /**
    * Invokes start() to configure the log4j framework.
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
