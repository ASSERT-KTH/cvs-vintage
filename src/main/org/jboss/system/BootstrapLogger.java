/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/** 
 * A utility class that allows the use of log4j by classes that must be loaded
 * from the system classpath. It uses reflection to obtain access to the log4j
 * classes using the thread context class loader.
 *
 * @author  <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.4 $
 */
public class BootstrapLogger
{
   /** The log4j Category class name */
   private static final String CATEGORY_CLASS = "org.apache.log4j.Category";

   /** The log4j Priority class name */
   private static final String PRIORITY_CLASS = "org.apache.log4j.Priority";
   
   /** The custom JBoss TRACE Priority class name */
   private static final String TRACE_PRIORITY_CLASS =  "org.jboss.logging.TracePriority";
   
   /** Indicies into the log4jMethods for the Category methods */
   private static final int GET_INSTANCE = 0;
   private static final int IS_ENABLED_FOR_PRIORITY = 1;
   private static final int LOG_PRIORITY_MSG = 2;
   private static final int LOG_PRIORITY_MSG_EX = 3;
   private static final int N_METHODS = 4;
   
   /** An array of the Log4j priority names. */
   private static final String[] PRIORITY_NAMES = {
      "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"
   };

   /** An array of the org.apache.log4j.Category methods used by BootstrapLogger */
   private static Method[] log4jMethods = null;
      
   /** An array of Priority objects corresponding to the names TRACE..FATAL */
   private static Object[] log4jPriorities = new Object[PRIORITY_NAMES.length];
   
   private static final int TRACE = 0;
   private static final int DEBUG = 1;
   private static final int INFO = 2;
   private static final int WARN = 3;
   private static final int ERROR = 4;
   private static final int FATAL = 5;
   
   /** Should execptions during the load of log4j be dumped to System.err */
   private static boolean logInitFailures = false;
   
   /** The maximum # of initLog4j calls to attempt */
   private static int maxInitAttempts = 100;
   
   /** The number of initialization attempts so far. */
   private static int initAttempts;
   
   /** The threashold used when uninitialized */
   private static int threshold = INFO;

   /** True to enable more verbose logging when log4j is not yet initialized. */
   private static boolean verbose = false;
   
   // Externalize behavior using properties
   static
   {
      try
      {
         logInitFailures = Boolean.getBoolean("org.jboss.system.BootstrapLogger.logInitFailures");
         maxInitAttempts = Integer.getInteger("org.jboss.system.BootstrapLogger.maxInitAttempts", maxInitAttempts).intValue();
         threshold = Integer.getInteger("org.jboss.system.BootstrapLogger.threshold", threshold).intValue();
         verbose = Boolean.getBoolean("org.jboss.system.BootstrapLogger.verbose");
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /** The log4j Category object the BootstrapLogger delegates to. */
   private Object category;
   
   /** The category type. */
   private String categoryType;

   public static BootstrapLogger getLogger(Class type)
   {
      return getLogger(type.getName());
   }
   
   public static BootstrapLogger getLogger(String typename)
   {
      try
      {
         initLog4j();
      }
      catch(Exception ignore) {}

      return new BootstrapLogger(typename);
   }

   // --- Begin log4j Category methods we expose
   
   public void trace(Object msg)
   {
      log(TRACE, msg);
   }
   
   public void trace(Object msg, Throwable ex)
   {
      log(TRACE, msg, ex);
   }
   
   public void debug(Object msg)
   {
      log(DEBUG, msg);
   }
   
   public void debug(Object msg, Throwable ex)
   {
      log(DEBUG, msg, ex);
   }
   
   public void info(Object msg)
   {
      log(INFO, msg);
   }
   
   public void info(Object msg, Throwable ex)
   {
      log(INFO, msg, ex);
   }
   
   public void warn(Object msg)
   {
      log(WARN, msg);
   }
   
   public void warn(Object msg, Throwable ex)
   {
      log(WARN, msg, ex);
   }
   
   public void error(Object msg)
   {
      log(ERROR, msg);
   }
   
   public void error(Object msg, Throwable ex)
   {
      log(ERROR, msg, ex);
   }
   
   public void fatal(Object msg)
   {
      log(FATAL, msg);
   }
   
   public void fatal(Object msg, Throwable ex)
   {
      log(FATAL, msg, ex);
   }
   
   public boolean isTraceEnabled()
   {
      return isEnabledFor(TRACE);
   }
   
   public boolean isDebugEnabled()
   {
      return isEnabledFor(DEBUG);
   }
   
   public boolean isInfoEnabled()
   {
      return isEnabledFor(INFO);
   }

   // --- Begin log4j Category methods we expose
   
   /** 
    * This method is called by all isXXXEnabled methods to invoke
    * the Category.isEnabledForPriority method using the priority instance.
    * If the log4j methods have not been loaded then the priority
    */
   private boolean isEnabledFor(int priorityIdx)
   {
      boolean isEnabled = false;
      
      try
      {
         if( category == null )
         {
            // Try to load the log4j classes
            init();
         
            // if we don't have a category the use our threshold
            if (category == null) {
               return priorityIdx >= threshold;
            }
         }
         
         Object priority = log4jPriorities[priorityIdx];
         Object[] args = {priority};
         Boolean bool = (Boolean)
            log4jMethods[IS_ENABLED_FOR_PRIORITY].invoke(category, args);
         isEnabled = bool.booleanValue();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   
      return isEnabled;
   }

   /** 
    * A magical token to tell _log() which log4j varaity to use.
    *
    * If this value is passed as the second argument to _log(), then
    * the single argument log4j method will be called.
    */
   private static final Object MAGIC_TOKEN = new Object();
   
   /** 
    * The log method is called by all explicit priority log methods
    * that do not accept a Throwable.
    *
    * @param idx   the index into the log4jPriorities array to use as
    *              the priority of the msg
    * @param msg   the log message object
    */
   private void log(int idx, Object msg)
   {
      _log(idx, msg, MAGIC_TOKEN);
   }

   /** 
    * The log method is called by all explicit priority log methods
    * that do accept a Throwable.
    *
    * @param idx   the index into the log4jPriorities array to use as
    *              the priority of the msg
    * @param msg   the log message object
    * @param ex    the exception associated with the msg
    */
   private void log(int idx, Object msg, Throwable ex)
   {
      _log(idx, msg, ex);
   }

   /**
    * Consolidates both logging calls.  If ex is MAGIC_TOKEN then
    * the one argument log() method will be invoked, else the two
    * arg version will be used.
    */
   private void _log(int idx, Object msg, Object ex) {
      // if we don't have a category yet, they try to initialize
      if( category == null )
      {
         // Try to load the log4j classes
         init();
         if( category == null )
         {
            if (!isEnabledFor(idx)) return;

            // construct the message to print
            StringBuffer buff = new StringBuffer();
            buff.append(PRIORITY_NAMES[idx]).append(" ");
            if (verbose) {
               buff.append("[").append(categoryType).append("] ");
            }
            buff.append(msg);
            
            // Failed, dump the msg to System.out & print stack trace
            System.out.println(buff);
            
            // If ex is not MAGIC_TOKEN then it is an exception
            if (ex != MAGIC_TOKEN) {
               if (ex != null) {
                  ((Throwable)ex).printStackTrace();
               }
               else {
                  System.err.println(ex);
               }
            }
            
            return;
         }
      }

      // Category is loaded, try logging to it
      try
      {
         Object pri = log4jPriorities[idx];         
         Object[] args;
         if (ex != MAGIC_TOKEN) {
            args = new Object[] { pri, msg, ex };
            log4jMethods[LOG_PRIORITY_MSG_EX].invoke(category, args);
         }
         else {
            args = new Object[] { pri, msg};
            log4jMethods[LOG_PRIORITY_MSG].invoke(category, args);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   // End log4j methods

   private BootstrapLogger(final String categoryType)
   {
      this.categoryType = categoryType;
      init();
   }

   /** 
    * This method is called to set the logger category and retry loading
    * the log4j classes if they have not been loaded as yet.
    */
   private void init()
   {
      try {
         if (log4jMethods == null) {
            initLog4j();
         }
         
         if (log4jMethods != null) {
            Object[] args = {categoryType};
            category = log4jMethods[GET_INSTANCE].invoke(null, args);
         }
      }
      catch (ClassNotFoundException e) {
         if (logInitFailures == true) {
            e.printStackTrace();
         }
      }
      catch (Exception e)
      {
         // If we get something other than an CNFE then something
         // is probably wrong
         e.printStackTrace();
      }
   }

   /** 
    * Load the log4j classes using the thread context class loader and
    * build an array of the methods and priorities we use in this class
    * using reflection.
    */
   private static synchronized void initLog4j() 
      throws ClassNotFoundException,
             NoSuchMethodException, 
             IllegalAccessException, 
             IllegalArgumentException,
             InvocationTargetException
   {
      if (log4jMethods != null || initAttempts > maxInitAttempts) {
         return;
      }

      initAttempts ++;
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class type;
      
      // Load the custom trace Priority class and log4j Priority
      Class priorityClass = loader.loadClass(PRIORITY_CLASS);

      // Fill in the standard log4j Priority instances
      Class[] toPriorityTypes = {String.class};
      Method toPriority = priorityClass.getDeclaredMethod("toPriority", toPriorityTypes);
      for (int n = 1; n < PRIORITY_NAMES.length; n ++)
      {
         Object[] args = { PRIORITY_NAMES[n] };
         log4jPriorities[n] = toPriority.invoke(null, args);
      }
      
      // Add the custom TRACE priority
      Class traceClass = loader.loadClass(TRACE_PRIORITY_CLASS);
      toPriorityTypes = new Class[] {String.class, priorityClass};
      toPriority = traceClass.getDeclaredMethod("toPriority", toPriorityTypes);
      log4jPriorities[0] = toPriority.invoke(null, new Object[]{"TRACE", log4jPriorities[1]});

      // Build an array of the log4j Category methods we use
      Class categoryClass = loader.loadClass(CATEGORY_CLASS);
      Method[] tmp = new Method[N_METHODS];

      Class[] paramTypes = {String.class};
      tmp[GET_INSTANCE] = categoryClass.getDeclaredMethod("getInstance", paramTypes);
      
      paramTypes = new Class[] {priorityClass};
      tmp[IS_ENABLED_FOR_PRIORITY] = categoryClass.getDeclaredMethod("isEnabledFor", paramTypes);
      
      paramTypes = new Class[] {priorityClass, Object.class};
      tmp[LOG_PRIORITY_MSG] = categoryClass.getMethod("log", paramTypes);
      
      paramTypes = new Class[] {priorityClass, Object.class, Throwable.class};
      tmp[LOG_PRIORITY_MSG_EX] = categoryClass.getMethod("log", paramTypes);
      
      // The log4jMethods is only assigned if all methods were found
      log4jMethods = tmp;
   }
}
