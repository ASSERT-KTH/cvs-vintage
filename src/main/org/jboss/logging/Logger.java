/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import org.apache.log4j.Category;
import org.apache.log4j.spi.CategoryFactory;

/** A custom log4j Category subclass that add a trace level priority.
 * @see #isTraceEnabled
 * @see #trace(Object message)
 * @see #trace(Object, Throwable)

 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.14 $
 */
public class Logger extends Category
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static CategoryFactory factory = new LoggerFactory();

   // Static --------------------------------------------------------
   /** This method overrides {@link Category#getInstance} by supplying
   its own factory type as a parameter.
    @param name, the category name
   */
   public static Category getInstance(String name)
   {
      return Category.getInstance(name, factory); 
   }
   /** This method overrides {@link Category#getInstance} by supplying
   its own factory type as a parameter.
    @param clazz, the Class whose name will be used as the category name
   */
   public static Category getInstance(Class clazz)
   {
      return Category.getInstance(clazz.getName(), factory); 
   }

   /** Create a Logger instance given the category name.
    @param name, the category name
    */
   public static Logger create(String name)
   {
      Logger logger = (Logger) Category.getInstance(name, factory);
      return logger;
   }
   /** Create a Logger instance given the category class. This simply
    calls create(clazz.getName()).
    @param clazz, the Class whose name will be used as the category name
    */
   public static Logger create(Class clazz)
   {
      Logger logger = (Logger) Category.getInstance(clazz.getName(), factory);
      return logger;
   }

   /** Create a Logger instance given the category name.
    @param name, the category name
    */
   public static Logger getLogger(String name)
   {
      Logger logger = (Logger) Category.getInstance(name, factory);
      return logger;
   }
   /** Create a Logger instance given the category class. This simply
    calls create(clazz.getName()).
    @param clazz, the Class whose name will be used as the category name
    */
   public static Logger getLogger(Class clazz)
   {
      Logger logger = (Logger) Category.getInstance(clazz.getName(), factory);
      return logger;
   }

  // Constructors --------------------------------------------------
   /** Creates new JBossCategory with the given category name.
    @param name, the category name.
   */
   public Logger(String name)
   {
      super(name);
   }

   /** Check to see if the TRACE priority is enabled for this category.
   @return true if a {@link #trace(String)} method invocation would pass
   the msg to the configured appenders, false otherwise.
   */
   public boolean isTraceEnabled()
   {
      if( hierarchy.isDisabled(TracePriority.TRACE_INT) )
         return false;
      return TracePriority.TRACE.isGreaterOrEqual(this.getChainedPriority());
   }

   /** Issue a log msg with a priority of TRACE.
   Invokes super.log(TracePriority.TRACE, message);
   */
   public void trace(Object message)
   {
      super.log(TracePriority.TRACE, message);
   }
   /** Issue a log msg and throwable with a priority of TRACE.
   Invokes super.log(TracePriority.TRACE, message, t);
   */
   public void trace(Object message, Throwable t)
   {
      super.log(TracePriority.TRACE, message, t);
   }
}
