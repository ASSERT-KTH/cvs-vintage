/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss;

import java.net.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;

/**
 * Starts multiple applications using seperate classloaders.
 * This allows multiple applications to co-exist even if they typicaly could not due to 
 * class version problems.  Each application is started in it's own thread.
 * 
 * Usage is Boot [-debug] -cp app-classpath app-class-name app-arguments ( , -cp app-classpath app-class-name app-arguments )*
 * 
 * Where:
 * 	app-classpath is a comma seperated URL form classpath to the application classes.
 * 	app-class-name is the class that will be started
 * 	app-arguments will be the String[] that will be passed to the main method of the application class
 * 
 * Jboss + Another Application boot example:
 *     Boot -cp file:run.jar org.jboss.Main default , -cp file:./myapp.jar,file:./util.jar test.App2TEST arg1 arg2
 * Would start the JBoss Server using the default configuration and it would
 * start the test.App2TEST application.  
 * Important Note: Notice that there are spaces before and after the ","!!!
 * 
 * TODO: Add debug print statments to help users figure out when they are improperly using this class.
 * TODO: Bring in some of the features that are in the org.jboss.main like the jboss.boot.loader.name property
 * 
 * @author <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * 
 */
public class Boot
{

   /**
    * Indicates whether this instance is running in debug mode.
    */
   protected boolean debug = false;
   
   /**
    * For each booted application, we will store a ApplicationBoot object in this linked list.
    */
   protected LinkedList applicationBoots;

   // constants
   private static final String DEBUG = "-debug";
   private static final String BOOT_APP_SEPERATOR = System.getProperty("org.jboss.Boot.APP_SEPERATOR", ",");
   private static final String CP = "-cp";

   /**
    * Data that is extracted for each mbus app that is specified on the command line
    */
   class ApplicationBoot implements Runnable
   {

      LinkedList classpath = new LinkedList();
      String applicationClass;
      LinkedList passThruArgs = new LinkedList();
      URLClassLoader classloader;
      boolean isRunning;

	  /**
	   * This is what actually loads the application classes and
	   * invokes the main method.  We send any unhandled exceptions to 
	   * System.err
	   */
      public void run()
      {
         try
         {
            boot();
         }
         catch (Throwable e)
         {
            System.err.println("Exception durring " + applicationClass + " application run: ");
            e.printStackTrace(System.err);
         }
      }

	  /**
	   * This is what actually loads the application classes and
	   * invokes the main method.
	   */
      public void boot()
         throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
      {
         URL urls[] = new URL[classpath.size()];
         urls = (URL[]) classpath.toArray(urls);

         String args[] = new String[passThruArgs.size()];
         args = (String[]) passThruArgs.toArray(args);

         classloader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
         Class appClass = classloader.loadClass(applicationClass);
         Method mainMethod = appClass.getMethod("main", new Class[] { String[].class });

         try
         
            {
            isRunning = true;
            mainMethod.invoke(null, new Object[] { args });
         }
         catch (InvocationTargetException e)
         {
            if (e.getTargetException() instanceof Error)
               throw (Error) e.getTargetException();
            else
               throw e;
         }
         finally
         {
            isRunning = false;
         }
      }
   }

   /**
    * Main entry point when called from the command line
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      try
      {
         new Boot().run(args);
      }
      catch (Throwable e)
      {
         System.err.println("Exception launching the application(s):");
         e.printStackTrace(System.err);
      }
   }


   /**
    * @param args the arguments to the Boot class, see class description
    * @exception thrown if a problem occurs during launching
    */
   public void run(String[] args) throws Exception
   {
      // Put the args in a linked list since it easier to work with.
      LinkedList llargs = new LinkedList();
      for (int i = 0; i < args.length; i++)
      {
         llargs.add(args[i]);
      }

      applicationBoots = processCommandLine(llargs);
      Iterator i = applicationBoots.iterator();
      while (i.hasNext())
      {
         ApplicationBoot bootData = (ApplicationBoot) i.next();
         bootApplication(bootData);
      }
   }

   /**
    * Boots the application in a new threadgroup and thread.
    * 
    * @param bootData the application to boot.
    * @exception thrown if a problem occurs during launching
    */
   public void bootApplication(ApplicationBoot bootData) throws Exception
   {
      ThreadGroup threads = new ThreadGroup(bootData.applicationClass);
      new Thread(threads, bootData, "main").start();
   }

   /**
    * Processes the Boot class's command line arguments
    * 
    * @return a linked list with ApplicationBoot objects
    * @param args the command line arguments
    */
   protected LinkedList processCommandLine(LinkedList args) throws Exception
   {
      LinkedList rc = new LinkedList();

      processBootOptions(args);
      while (args.size() > 0)
      {
         ApplicationBoot d = processAppBootCommandLine(args);
         if (d != null)
            rc.add(d);
      }

      if (rc.size() == 0)
      {
         throw new Exception("Invlid usage: An application class name must be provided.");
      }

      return rc;
   }

   /**
    * Processes to global options.
    * 
    * @param args the command line arguments
    */
   protected void processBootOptions(LinkedList args) throws Exception
   {
      Iterator i = args.iterator();
      while (i.hasNext())
      {
         String arg = (String) i.next();
         if (arg.equalsIgnoreCase(DEBUG))
         {
            debug = true;
            i.remove();
            continue;
         }

         // Didn't recognize it a boot option, then we must have started the application 
         // boot options.
         return;
      }
   }

   /**
    * Processes the command line argumenst for the next application on the command line.
    * 
    * @param args the command line arguments
    */
   protected ApplicationBoot processAppBootCommandLine(LinkedList args) throws Exception
   {
      ApplicationBoot rc = new ApplicationBoot();
      Iterator i = args.iterator();

      while (i.hasNext())
      {
         String arg = (String) i.next();
         i.remove();

         if (rc.applicationClass == null)
         {
            if (arg.equalsIgnoreCase(CP))
            {
               if (!i.hasNext())
                  throw new Exception("Invalid option: classpath missing after the " + CP + " option.");
               String cp = (String) i.next();
               i.remove();

               StringTokenizer st = new StringTokenizer(cp, ",", false);
               while (st.hasMoreTokens())
               {
                  String t = st.nextToken();
                  if (t.length() == 0)
                     continue;
                  try
                  {
                     URL u = new URL(t);
                     rc.classpath.add(u);
                  }
                  catch (MalformedURLException e)
                  {
                     throw new Exception("Application classpath value was invalid: " + e.getMessage());
                  }
               }
               continue;
            }

            rc.applicationClass = arg;
            continue;
         }
         else
         {
            if (arg.equalsIgnoreCase(BOOT_APP_SEPERATOR))
            {
               break;
            }
            rc.passThruArgs.add(arg);
         }

      }

      if (rc.applicationClass == null)
         return null;

      return rc;
   }

   /**
    * This method is here so that if JBoss is running under
    * Alexandria (An NT Service Installer), Alexandria can shutdown
    * the system down correctly.
    */
   public static void systemExit(String argv[])
   {
      System.exit(0);
   }
}
