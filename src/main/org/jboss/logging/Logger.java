/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/** A custom log4j Category wrapper that adds a trace level priority and only
 exposes the relevent factory and logging methods.

 * @see #isTraceEnabled
 * @see #trace(Object message)
 * @see #trace(Object, Throwable)

 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.16 $
 */
public class Logger
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private Category log;

   /**
    * The analysis handler
    */
   private static LogAnalysis analysis = null;

   // Static --------------------------------------------------------

   /** Create a Logger instance given the category name.
    @param name, the category name
    */
   public static Logger getLogger(String name)
   {
      Logger logger = new Logger(name);
      return logger;
   }
   /** Create a Logger instance given the category class. This simply
    calls create(clazz.getName()).
    @param clazz, the Class whose name will be used as the category name
    */
   public static Logger getLogger(Class clazz)
   {
      Logger logger = new Logger(clazz.getName());
      return logger;
   }

  // Constructors --------------------------------------------------
   /** Creates new JBossCategory with the given category name.
    @param name, the category name.
   */
   protected Logger(String name)
   {
      log = Category.getInstance(name);
   }

   public Category getCategory()
   {
      return log;
   }

   /** Check to see if the TRACE priority is enabled for this category.
   @return true if a {@link #trace(String)} method invocation would pass
   the msg to the configured appenders, false otherwise.
   */
   public boolean isTraceEnabled()
   {
      if( log.isEnabledFor(TracePriority.TRACE) == false )
         return false;
      return TracePriority.TRACE.isGreaterOrEqual(log.getChainedPriority());
   }

   /** Issue a log msg with a priority of TRACE.
   Invokes log.log(TracePriority.TRACE, message);
   */
   public void trace(Object message)
   {
      if (analysis != null)
        analysis.add(log, TracePriority.TRACE);
      log.log(TracePriority.TRACE, message);
   }
   /** Issue a log msg and throwable with a priority of TRACE.
   Invokes log.log(TracePriority.TRACE, message, t);
   */
   public void trace(Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, TracePriority.TRACE);
      log.log(TracePriority.TRACE, message, t);
   }

   /** Check to see if the TRACE priority is enabled for this category.
   @return true if a {@link #trace(String)} method invocation would pass
   the msg to the configured appenders, false otherwise.
   */
   public boolean isDebugEnabled()
   {
      Priority p = Priority.DEBUG;
      if( log.isEnabledFor(p) == false )
         return false;
      return p.isGreaterOrEqual(log.getChainedPriority());
   }

   /** Issue a log msg with a priority of DEBUG.
   Invokes log.log(Priority.DEBUG, message);
   */
   public void debug(Object message)
   {
      if (analysis != null)
        analysis.add(log, Priority.DEBUG);
      log.log(Priority.DEBUG, message);
   }
   /** Issue a log msg and throwable with a priority of DEBUG.
   Invokes log.log(Priority.DEBUG, message, t);
   */
   public void debug(Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, Priority.DEBUG);
      log.log(Priority.DEBUG, message, t);
   }

   /** Check to see if the INFO priority is enabled for this category.
   @return true if a {@link #info(String)} method invocation would pass
   the msg to the configured appenders, false otherwise.
   */
   public boolean isInfoEnabled()
   {
      Priority p = Priority.INFO;
      if( log.isEnabledFor(p) == false )
         return false;
      return p.isGreaterOrEqual(log.getChainedPriority());
   }

   /** Issue a log msg with a priority of INFO.
   Invokes log.log(Priority.INFO, message);
   */
   public void info(Object message)
   {
      if (analysis != null)
        analysis.add(log, Priority.INFO);
      log.log(Priority.INFO, message);
   }
   /** Issue a log msg and throwable with a priority of INFO.
   Invokes log.log(Priority.INFO, message, t);
   */
   public void info(Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, Priority.INFO);
      log.log(Priority.INFO, message, t);
   }

   /** Issue a log msg with a priority of WARN.
   Invokes log.log(Priority.WARN, message);
   */
   public void warn(Object message)
   {
      if (analysis != null)
        analysis.add(log, Priority.WARN);
      log.log(Priority.WARN, message);
   }
   /** Issue a log msg and throwable with a priority of WARN.
   Invokes log.log(Priority.WARN, message, t);
   */
   public void warn(Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, Priority.WARN);
      log.log(Priority.WARN, message, t);
   }

   /** Issue a log msg with a priority of ERROR.
   Invokes log.log(Priority.ERROR, message);
   */
   public void error(Object message)
   {
      if (analysis != null)
        analysis.add(log, Priority.ERROR);
      log.log(Priority.ERROR, message);
   }
   /** Issue a log msg and throwable with a priority of ERROR.
   Invokes log.log(Priority.ERROR, message, t);
   */
   public void error(Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, Priority.ERROR);
      log.log(Priority.ERROR, message, t);
   }

   /** Issue a log msg with a priority of FATAL.
   Invokes log.log(Priority.FATAL, message);
   */
   public void fatal(Object message)
   {
      if (analysis != null)
        analysis.add(log, Priority.FATAL);
      log.log(Priority.FATAL, message);
   }
   /** Issue a log msg and throwable with a priority of FATAL.
   Invokes log.log(Priority.FATAL, message, t);
   */
   public void fatal(Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, Priority.FATAL);
      log.log(Priority.FATAL, message, t);
   }

   /** Issue a log msg with the given priority.
   Invokes log.log(p, message);
   */
   public void log(Priority p, Object message)
   {
      if (analysis != null)
        analysis.add(log, p);
      log.log(p, message);
   }
   /** Issue a log msg with the given priority.
   Invokes log.log(p, message, t);
   */
   public void log(Priority p, Object message, Throwable t)
   {
      if (analysis != null)
        analysis.add(log, p);
      log.log(p, message, t);
   }

  // Package Private -----------------------------------------------

  /**
   * Set the analysis handler.
   *
   * @param value the analysis handler.
   */
  static void setAnalysis(LogAnalysis value)
  {
    analysis = value;
  }
}
