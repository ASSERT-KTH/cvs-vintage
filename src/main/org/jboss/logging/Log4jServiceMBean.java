/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

/**
 * MBean interface.
 */
public interface Log4jServiceMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.system:type=Log4jService,service=Logging");

   /**
    * Set the catch <tt>System.out</tt> flag.
    * @param flag True to enable, false to disable.    */
  void setCatchSystemOut(boolean flag) ;

   /**
    * Get the catch <tt>System.out</tt> flag.
    * @return True if enabled, false if disabled.    */
  boolean getCatchSystemOut() ;

   /**
    * Set the catch <tt>System.err</tt> flag.
    * @param flag True to enable, false to disable.    */
  void setCatchSystemErr(boolean flag) ;

   /**
    * Get the catch <tt>System.err</tt> flag.
    * @return True if enabled, false if disabled.    */
  boolean getCatchSystemErr() ;

   /**
    * Get the org.apache.log4j.helpers.LogLog.setQuietMode flag
    * @return True if enabled, false if disabled.    */
  boolean getLog4jQuietMode() ;

   /**
    * Set the org.apache.log4j.helpers.LogLog.setQuietMode flag
    * @return True if enabled, false if disabled.    */
  void setLog4jQuietMode(boolean flag) ;

   /**
    * Get the refresh period.
    */
  int getRefreshPeriod() ;

   /**
    * Set the refresh period.
    */
  void setRefreshPeriod(int refreshPeriod) ;

   /**
    * Get the Log4j configuration URL.
    */
  java.net.URL getConfigurationURL() ;

   /**
    * Set the Log4j configuration URL.
    */
  void setConfigurationURL(java.net.URL url) ;

   /**
    * Sets the level for a logger of the give name. <p>Values are trimmed before used.
    * @param name The name of the logger to change level
    * @param levelName The name of the level to change the logger to.    */
  void setLoggerLevel(java.lang.String name,java.lang.String levelName) ;

   /**
    * Sets the levels of each logger specified by the given comma seperated list of logger names.
    * @see #setLoggerLevel
    * @param list A comma seperated list of logger names.
    * @param levelName The name of the level to change the logger to.    */
  void setLoggerLevels(java.lang.String list,java.lang.String levelName) ;

   /**
    * Gets the level of the logger of the give name.
    * @param name The name of the logger to inspect.    */
  java.lang.String getLoggerLevel(java.lang.String name) ;

   /**
    * Force the logging system to reconfigure.
    */
  void reconfigure() throws java.io.IOException;

   /**
    * Hack to reconfigure and change the URL. This is needed until we have a JMX HTML Adapter that can use PropertyEditor to coerce.
    * @param url The new configuration url    */
  void reconfigure(java.lang.String url) throws java.io.IOException, java.net.MalformedURLException;

}
