/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.logging;

import java.io.PrintStream;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
   
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import org.jboss.util.ThrowableHandler;
import org.jboss.util.ThrowableListener;
import org.jboss.util.NullArgumentException;
import org.jboss.util.Strings;

import org.jboss.logging.util.CategoryStream;

import org.jboss.system.ServiceMBeanSupport;

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
 * @jmx:mbean name="jboss.system:type=Log4jService,service=Logging"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @version <tt>$Revision: 1.22 $</tt>
 * @author <a href="mailto:phox@galactica.it">Fulco Muriglio</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>
 * @author <a href="mailto:davidjencks@earthlink.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Log4jService
   extends ServiceMBeanSupport
   implements Log4jServiceMBean
{
   /**
    * The default url for the configuration file.  Reads the value
    * from the system property <tt>org.jboss.logging.Log4jService.configURL</tt>
    * or if that is not set defaults to <tt>resource:log4j.xml</tt>.
    */
   public static final String DEFAULT_URL =
      System.getProperty(Log4jService.class.getName() + ".configURL", "resource:log4j.xml");

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

   /** Helper to get boolean value from system property or use default if not set. */
   private static boolean getBoolean(String name, boolean defaultValue)
   {
      String value = System.getProperty(name, null);
      if (value == null)
         return defaultValue;
      return new Boolean(value).booleanValue();
   }
   
   /** The URL to the configuration file. */
   private URL configURL;

   /** The time in seconds between checking for new config. */
   private int refreshPeriod;
   
   private ThrowableListenerLoggingAdapter throwableAdapter;

   /** The previous value of System.out. */
   private PrintStream out;

   /** The previous value of System.err. */
   private PrintStream err;

   /** The URL watch timer (in daemon mode). */
   private Timer timer = new Timer(true);

   /** The specialized timer task to watch our config file. */
   private URLWatchTimerTask timerTask;
   
   /**
    * Uses defaults.
    *
    * @throws MalformedURLException    Could not create URL from default (propbably
    *                                  a problem with overridden properties).
    */
   public Log4jService() throws MalformedURLException
   {
      this(DEFAULT_URL, 60);
   }
   
   /**
    * @param url    The configuration URL.
    */
   public Log4jService(final URL url)
   {
      this(url, 60);
   }

   /**
    * @param url    The configuration URL.
    */
   public Log4jService(final String url) throws MalformedURLException
   {
      this(Strings.toURL(url), 60);
   }
   
   /**
    * @param url              The configuration URL.
    * @param refreshPeriod    The refreshPeriod in seconds to wait between each check.
    */
   public Log4jService(final String url, final int refreshPeriod)
      throws MalformedURLException
   {
      this(Strings.toURL(url), refreshPeriod);
   }

   /**
    * @param url              The configuration URL.
    * @param refreshPeriod    The refreshPeriod in seconds to wait between each check.
    */
   public Log4jService(final URL url, final int refreshPeriod)
   {
      this.configURL = url;
      this.refreshPeriod = refreshPeriod;
   }

   /**
    * Get the refresh period.
    *
    * @jmx:managed-attribute
    */
   public int getRefreshPeriod()
   {
      return refreshPeriod;
   }

   /**
    * Set the refresh period.
    *
    * @jmx:managed-attribute
    */
   public void setRefreshPeriod(final int refreshPeriod)
   {
      this.refreshPeriod = refreshPeriod;
   }
   
   /**
    * Get the Log4j configuration URL.
    *
    * @jmx:managed-attribute
    */
   public URL getConfigurationURL()
   {
      return configURL;
   }
   
   /**
    * Set the Log4j configuration URL.
    *
    * @jmx:managed-attribute
    */
   public void setConfigurationURL(final URL url)
   {
      this.configURL = url;
   }

   /**
    * Sets the priority for a logger of the give name.
    *
    * @jmx:managed-operation
    *
    * @param name       The name of the logger to change priority
    * @param priority   The name of the priority to change the logger to.
    */
   public void setLoggerPriority(final String name, final String priority)
   {
      Category c = Category.getInstance(name);
      XLevel p = XLevel.toPriority(priority);

      c.setLevel(p);

      log.info("Priority set to " + p + " for " + name);
   }

   /**
    * Gets the priority of the logger of the give name.
    *
    * @jmx:managed-operation
    *
    * @param name       The name of the logger to inspect.
    */
   public String getLoggerPriority(final String name)
   {
      Category c = Category.getInstance(name);
      Priority p = c.getPriority();

      if (p != null) 
         return p.toString();

      return null;
   }

   /**
    * Force the logging system to reconfigure.
    *
    * @jmx:managed-operation
    */
   public void reconfigure() throws IOException
   {
      if (timerTask == null)
         throw new IllegalStateException("Service stopped or not started");
      
      timerTask.reconfigure();
   }

   /**
    * Hack to reconfigure and change the URL.  This is needed until we
    * have a JMX HTML Adapter that can use PropertyEditor to coerce.
    *
    * @jmx:managed-operation
    *
    * @param url   The new configuration url
    */
   public void reconfigure(final String url) throws IOException, MalformedURLException
   {
      setConfigurationURL(Strings.toURL(url));
      reconfigure();
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                       Concrete Service Overrides                      //
   ///////////////////////////////////////////////////////////////////////////

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   protected void createService() throws Exception
   {
      timerTask = new URLWatchTimerTask(); /* (configURL); */
      timerTask.run();
      timer.schedule(timerTask, 1000 * refreshPeriod, 1000 * refreshPeriod);

      // Make sure Category has loaded
      Category category = Category.getRoot();
      
      // Install listener for unhandled throwables to turn them into log messages
      throwableAdapter = new ThrowableListenerLoggingAdapter();
      ThrowableHandler.addThrowableListener(throwableAdapter);
      log.debug("Added ThrowableListener: " + throwableAdapter);
   }

   private void installSystemAdapters()
   {
      Category category;

      // Install catchers
      if (CATCH_SYSTEM_OUT)
      {
         category = Category.getInstance("STDOUT");
         out = System.out;
         System.setOut(new CategoryStream(category, Priority.INFO, out));
         log.debug("Installed System.out adapter");
      }
      if (CATCH_SYSTEM_ERR)
      {
         category = Category.getInstance("STDERR");
         err = System.err;
         System.setErr(new CategoryStream(category, Priority.ERROR, err));
         log.debug("Installed System.err adapter");
      }
   }

   private void uninstallSystemAdapters()
   {
      // Remove System adapters
      if (out != null) {
         System.out.flush();
         System.setOut(out);
         log.debug("Removed System.out adapter");
         out = null;
      }
      if (err != null) {
         System.err.flush();
         System.setErr(err);
         log.debug("Removed System.err adapter");
         err = null;
      }
   }

   /**
    * Stops the log4j framework by calling the Category.shutdown() method.
    */
   protected void stopService() throws Exception
   {
      timerTask.cancel();
      timerTask = null;
      
      // Remove throwable adapter
      ThrowableHandler.removeThrowableListener(throwableAdapter);
      throwableAdapter = null;

      uninstallSystemAdapters();

      uninstallSystemAdapters();
      
   }


   ///////////////////////////////////////////////////////////////////////////
   //                       ThrowableListener Adapter                      //
   ///////////////////////////////////////////////////////////////////////////
   
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


   ///////////////////////////////////////////////////////////////////////////
   //                         URL Watching Timer Task                       //
   ///////////////////////////////////////////////////////////////////////////
   
   /**
    * A timer task to check when a URL changes (based on 
    * last modified time) and reconfigure Log4j.
    */
   private class URLWatchTimerTask
      extends TimerTask
   {
      private Logger log = Logger.getLogger(URLWatchTimerTask.class);

      private long lastConfigured = -1;

      public void run()
      {
         log.trace("Checking if configuration changed");

         boolean trace = log.isTraceEnabled();
         
         try
         {
            URLConnection conn = configURL.openConnection();
            if (trace)
               log.trace("connection: " + conn);

            long lastModified = conn.getLastModified();
            if (trace)
            {
               log.trace("last configured: " + lastConfigured);
               log.trace("last modified: " + lastModified);
            }

            if (lastConfigured < lastModified)
            {
               reconfigure(conn);
            }
         }
         catch (Exception e)
         {
            log.warn("Failed to check URL: " + configURL, e);
         }
      }

      public void reconfigure() throws IOException
      {
         URLConnection conn = configURL.openConnection();
         reconfigure(conn);
      }
      
      private void reconfigure(final URLConnection conn) 
      {
         log.info("Configuring from URL: " + configURL);
         
         boolean xml = false;
         boolean trace = log.isTraceEnabled();
         
         // check if the url is xml
         String contentType = conn.getContentType();
         if (trace)
            log.trace("content type: " + contentType);
         
         if (contentType == null)
         {
            String filename = configURL.getFile().toLowerCase();
            if (trace) log.trace("filename: " + filename);

            xml = filename.endsWith(".xml");
         }
         else
         {
            xml = contentType.equalsIgnoreCase("text/xml");
         }
         if (trace) log.trace("reconfiguring; xml=" + xml);

         // Dump our config if trace is enabled
         if (trace)
         {
            try
            {
               java.io.InputStream is = conn.getInputStream();
               org.jboss.util.stream.Streams.copy(is, System.out);
            }
            catch (Exception e)
            {
               log.error("Failed to dump config", e);
            }
         }

         // need to uninstall adapters to avoid problems
         uninstallSystemAdapters();

         if (xml)
         {
            DOMConfigurator.configure(configURL);
         }
         else
         {
            PropertyConfigurator.configure(configURL);
         }

         // but make sure they get reinstalled again
         installSystemAdapters();

         // and then remeber when we were last changed
         lastConfigured = System.currentTimeMillis();
      }
   }
}
