/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.*;
import java.util.*;

import javax.management.*;
import javax.management.loading.*;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class Main
   implements Runnable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   MBeanServer server;
   String confName;
   
   // Static --------------------------------------------------------
   public static void main(String[] args)
      throws Exception
   {
      // Add shutdown hook
      final java.io.PrintStream out = System.err;
      
      // Load system properties
      System.getProperties().load(Main.class.getClassLoader().getResourceAsStream("jboss.properties"));
      
      // Set security
      System.setProperty("java.security.policy",Main.class.getClassLoader().getResource("server.policy").getFile());
      System.setSecurityManager(new SecurityManager());
      
      // Create server
      final Main main = new Main();
      
      if (args.length > 0)
         main.confName = args[0];
      
      // Start server - Main does not have the proper permissions
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            main.run();
            return null;
         }
      });
   }

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void run()
   {
      try
      {
         PrintStream err = System.err;
         
         com.sun.management.Trace.parseTraceProperties();
         
         server = new MBeanServer();
      
         // Create MLet
         MLet mlet = new MLet();
         server.registerMBean(mlet, new ObjectName(server.getDefaultDomain(), "service", "MLet"));
         
         // Set MLet as classloader for this app
         Thread.currentThread().setContextClassLoader(mlet);
         
         // Read default configuration
         URL mletConf = getClass().getClassLoader().getResource("jboss.conf");
         Set beans = (Set)mlet.getMBeansFromURL(mletConf);
         Iterator enum = beans.iterator();
         while (enum.hasNext())
         {
            Object obj = enum.next();
            if (obj instanceof RuntimeOperationsException)
               ((RuntimeOperationsException)obj).getTargetException().printStackTrace(err);
            else if (obj instanceof RuntimeErrorException)
               ((RuntimeErrorException)obj).getTargetError().printStackTrace(err);
            else if (obj instanceof MBeanException)
               ((MBeanException)obj).getTargetException().printStackTrace(err);
            else if (obj instanceof RuntimeMBeanException)
               ((RuntimeMBeanException)obj).getTargetException().printStackTrace(err);
         }
         
         // Read additional configuration
         if (confName != null)
         {
            mletConf = getClass().getClassLoader().getResource(confName);
            
            if (mletConf == null)
            {
               mletConf = new File(confName).toURL();
            }
            
            beans = (Set)mlet.getMBeansFromURL(mletConf);
            enum = beans.iterator();
            while (enum.hasNext())
            {
               Object obj = enum.next();
               if (obj instanceof RuntimeOperationsException)
                  ((RuntimeOperationsException)obj).getTargetException().printStackTrace(err);
               else if (obj instanceof RuntimeErrorException)
                  ((RuntimeErrorException)obj).getTargetError().printStackTrace(err);
               else if (obj instanceof MBeanException)
                  ((MBeanException)obj).getTargetException().printStackTrace(err);
            }
         }
         
         // Start adaptor
         server.invoke(new ObjectName("Adaptor:name=html,port=8082"), "start", new Object[0], new String[0]);
         
         // Add shutdown hook
         try
         {
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
               public void run()
               {
                  System.out.println("Shutdown");
                  Set mBeans = server.queryNames(null, null);
                  Iterator names = mBeans.iterator();
                  System.out.println("Shutting down "+mBeans.size() +" MBeans");
                  while (names.hasNext())
                  {
                     ObjectName name = (ObjectName)names.next();
                     try
                     {
                        System.out.println(server.invoke(name, "toString", new Object[0], new String[0]));
                     } catch (Throwable e)
                     {
                        // Ignore
                     }
                  }
                     
               }
            });
         } catch (Throwable e)
         {
            System.out.println("Could not add shutdown hook");
            // JDK 1.2.. ignore!
         }
         // Done
         System.out.println("jBoss 2.0 Started");

/*         
         // Command tool
         // Should be replaced with a MBean?
         
         BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
         String line;
         while (true)
         {
            // Get command
            line = reader.readLine();
            
            if (line.equals("shutdown"))
            {
               Set mBeans = server.queryNames(null, null);
               Iterator names = mBeans.iterator();
               while (names.hasNext())
               {
                  ObjectName name = (ObjectName)names.next();
                  try
                  {
                     server.invoke(name, "stop", new Object[0], new String[0]);
                  } catch (Throwable e)
                  {
                     // Ignore
                  }
               }
               
               System.exit(0);
            } else
            {
               Set mBeans = server.queryNames(null, null);
               Iterator names = mBeans.iterator();
               while (names.hasNext())
               {
                  ObjectName name = (ObjectName)names.next();
                  try
                  {
                     server.invoke(name, line, new Object[0], new String[0]);
                  } catch (Throwable e)
                  {
                     // Ignore
                  }
               }
            }
         }
*/         
      } catch (RuntimeOperationsException e)
      {
         System.out.println("Runtime error");
         e.getTargetException().printStackTrace();
      } catch (MBeanException e)
      {
         e.getTargetException().printStackTrace();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
