/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.net.URL;
import java.security.*;
import java.util.*;

import javax.management.*;
import javax.management.loading.*;

import org.jboss.dependencies.DependencyManager;
import org.jboss.system.SecurityAssociation;

/**
 *
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @author <a href="mailto:docodan@nycap.rr.com">Daniel O'Connor</a>.
 *   @version $Revision: 1.19 $
 */
public class Main
{
   // Constants -----------------------------------------------------

    String versionIdentifier = "BETA-PROD-PRE-04";
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   public static void main(final String[] args)
      throws Exception
   {
      String cn = "default"; // Default configuration name is "default", i.e. all conf files are in "/conf/default"
      
      // Given conf name?
      if (args.length == 1)
         cn = args[0];
      
      final String confName = cn;   
      
      // Load system properties
      InputStream propertiesIn = Main.class.getClassLoader().getResourceAsStream(confName+"/jboss.properties");

      if ( propertiesIn == null ) {

          throw new IOException("jboss.properties missing");
      }

      System.getProperties().load(propertiesIn);

      // Set security
      URL serverPolicy = Main.class.getClassLoader().getResource(confName+"/server.policy");

      if ( serverPolicy == null ) {

          throw new IOException("server.policy missing");
      }

      System.setProperty("java.security.policy", serverPolicy.getFile());

      // Set security manager
      // Optional for better performance
      if (System.getProperty("java.security.manager") != null)
         System.setSecurityManager((SecurityManager)Class.forName(System.getProperty("java.security.manager")).newInstance());

      // use thread-local principal and credential propagation
      SecurityAssociation.setServer();

      // Start server - Main does not have the proper permissions
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            new Main(confName);
            return null;
         }
      });
   }

   // Constructors --------------------------------------------------
    public Main(String confName)
    {
      try
      {
         System.out.println("Using configuration \""+confName+"\"");
      
         final PrintStream err = System.err;

         com.sun.management.jmx.Trace.parseTraceProperties();

         // Create MBeanServer
         final MBeanServer server = MBeanServerFactory.createMBeanServer();
   
         // Add configuration directory to MLet
         URL confDirectory = new File("../conf/"+confName).getCanonicalFile().toURL();
         
         // Create MLet
         MLet mlet = new MLet(new URL[] { confDirectory });
         server.registerMBean(mlet, new ObjectName(server.getDefaultDomain(), "service", "MLet"));
   
         // Set MLet as classloader for this app
         Thread.currentThread().setContextClassLoader(mlet);
         
         // Load configuration
          URL mletConf = mlet.getResource("jboss.conf");
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
             else if (obj instanceof Throwable)
                 ((Throwable)obj).printStackTrace(err);
          }

       // Load settings from XML
       InputStream conf = mlet.getResourceAsStream("jboss.jcml");
       byte[] arr = new byte[conf.available()];
       conf.read(arr);
       String cfg = new String(arr);

          // Invoke configuration loader
          server.invoke(new ObjectName(":service=Configuration"), "load", new Object[] { cfg }, new String[] { "java.lang.String" });

          // Get configuration from service
          cfg = (String)server.invoke(new ObjectName(":service=Configuration"), "save", new Object[0] , new String[0]);

       // Store config
       // This way, the config will always contain a complete mirror of what's in the server
         URL confUrl = mlet.getResource("jboss.jcml");
         PrintWriter out = new PrintWriter(new FileWriter(confUrl.getFile()));
         out.println(cfg);
         out.close();

         // Start MBeans
         InputStream depFile = mlet.getResourceAsStream("jboss.dependencies");
         byte[] depBytes = new byte[depFile.available()];
         depFile.read(depBytes);
         String depXML = new String(depBytes);
         final DependencyManager manager = new DependencyManager();
         manager.loadXML(depXML);
         manager.startMBeans(server);

          // Add shutdown hook
          try
          {
             Runtime.getRuntime().addShutdownHook(new Thread()
             {
                public void run()
                {
                    manager.stopMBeans(server);
/*
                   err.println("Shutdown");
                   Set mBeans = server.queryNames(null, null);
                   Iterator names = mBeans.iterator();
                   err.println("Shutting down "+mBeans.size() +" MBeans");
                   while (names.hasNext())
                   {
                      ObjectName name = (ObjectName)names.next();
                      try
                      {
                         server.invoke(name, "destroy", new Object[0], new String[0]);
                      } catch (Throwable e)
                      {
//	                        err.println(e);
                      }
                   }
                   err.println("Shutting done");
*/
                }
             });
             System.out.println ("Shutdown hook added");
          } catch (Throwable e)
          {
             System.out.println("Could not add shutdown hook");
             // JDK 1.2.. ignore!
          }

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
      } catch (RuntimeMBeanException e)
      {
         e.getTargetException().printStackTrace();
      } catch (Exception e)
      {
         e.printStackTrace();
      }

       // Done
       System.out.println("jBoss 2.0 "+versionIdentifier+" Started");
    }
}
