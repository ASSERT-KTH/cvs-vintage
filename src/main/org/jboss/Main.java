/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
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

/**
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:docodan@nycap.rr.com">Daniel O'Connor</a>.
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 1.41 $
 */
public class Main
{
   // Constants -----------------------------------------------------

   String versionIdentifier = "pre-3.0 [RABBIT-HOLE]";
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   public static void main(final String[] args)
      throws Exception
   {
      /* 
       *  Set a jboss.home property from the location of the Main.class jar
       *  if the property does not exist.
       *  marcf: we don't use this property at all for now 
       *  it should be used for all the modules that need a file "anchor"
       *  it should be moved to an "FileSystemAnchor" MBean
       */
      if( System.getProperty("jboss.home") == null )
      {
         String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
         File runJar = new File(path);
         // Home dir should be the parent of the dir containing run.jar
         File homeDir = new File(runJar.getParent(), "..");
         System.setProperty("jboss.home", homeDir.getCanonicalPath());
      }
      System.out.println("jboss.home = "+System.getProperty("jboss.home"));
      
      String installURL = "file://" + System.getProperty("jboss.home")+File.separatorChar;
      String configDir = "default"; // Default configuration name is "default", i.e. all conf files are in "/conf/default"
      String patchDir = "";
      
      // Given conf name
      
      for(int a = 0; a < args.length; a ++)
      {
         
         if( args[a].startsWith("--patch-dir") || args[a].startsWith("-p"))
            patchDir = args[a+1];
         
         else if( args[a].startsWith("--net-install") || args[a].startsWith("-n"))
         {           
            installURL = args[a+1].startsWith("http://") ?  args[a+1] : "http://"+args[a+1] ;
            if (!installURL.endsWith("/"))
               installURL = installURL+"/";
         }
         
         else if(args[a].startsWith("--conf-dir") || args[a].startsWith("-c"))
            configDir = args[a+1];
      }
      
      configDir = installURL + (installURL.startsWith("http:") ? "conf/"+configDir+"/" : "conf"+ File.separatorChar+ configDir+ File.separatorChar);
      
      final String iURL = installURL;
      final String cDir = configDir;
      //final String lDir = loadDir;
      final String pDir = patchDir;
      
            // Start server - Main does not have the proper permissions
      AccessController.doPrivileged(new PrivilegedAction()      
         {
            public Object run()
            {
               new Main(iURL, cDir, pDir);
               return null;
            }
         });
   }
   
   // Constructors --------------------------------------------------
   public Main(String installURL, String confDir, String patchDir)
   {
      Date startTime = new Date();
      
      try
      {
         
         System.out.println("Using configuration \""+confDir+"\"");
         if (patchDir != null && patchDir != "") 
            System.out.println("with patch directory \""+patchDir+"\"");
         
         final PrintStream err = System.err;
         
         System.setProperty("jboss.system.installURL", installURL);
         System.setProperty("jboss.system.confDir", confDir);
         System.setProperty("jboss.system.patchDir", patchDir);
         System.setProperty("jboss.system.version", versionIdentifier);
         
         com.sun.management.jmx.Trace.parseTraceProperties();
         
         // Give feedback about from where jndi.properties is read
         URL jndiLocation = this.getClass().getResource("/jndi.properties");
         if (jndiLocation instanceof URL) {
            System.out.println("Please make sure the following is intended (check your CLASSPATH):");
            System.out.println(" jndi.properties is read from "+jndiLocation);
         }
         
         // Create MBeanServer
         final MBeanServer server = MBeanServerFactory.createMBeanServer("JBOSS-SYSTEM");
         
         // Add configuration directory to MLet
         URL confDirectory = new URL(confDir);
         URL[] urls = {confDirectory};
         
         // Add any patch jars to the MLet so they are seen ahead of the JBoss jars
         if( patchDir != null && patchDir != "" )
         {
            // The patchDir can only be a File one, local
            File dir = new File(patchDir);
            ArrayList tmp = new ArrayList();
            File[] jars = dir.listFiles(new java.io.FileFilter()
               {
                  public boolean accept(File pathname)
                  {
                     String name = pathname.getName();
                     return name.endsWith(".jar") || name.endsWith(".zip");
                  }
               }
                                        );
            // Add the normal configuration directory
            tmp.add(confDirectory);
            
            // Add the local file patch directory
            tmp.add(patchDir);
            
            for(int j = 0; jars != null && j < jars.length; j ++)
            {
               File jar = jars[j];
               URL u = jar.getCanonicalFile().toURL();
               tmp.add(u);
            }
            urls = new URL[tmp.size()];
            tmp.toArray(urls);
         }
         
         // Create MLet, the MLet loads first from the local patch dir then from the global configuration
         MLet mlet = new MLet(urls);
         server.registerMBean(mlet, new ObjectName(server.getDefaultDomain(), "service", "MLet"));
         
         // Set MLet as classloader for this app
         Thread.currentThread().setContextClassLoader(mlet);
         
         // Initialize Configuration Service
         
         //URL mletConf = mlet.getResource("boot.jmx");
         URL mletConf = new URL(confDir+"boot.jmx");
         
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
            else if (obj instanceof ReflectionException)
               ((ReflectionException)obj).getTargetException().printStackTrace(err);
            else if (obj instanceof Throwable)
               ((Throwable)obj).printStackTrace(err);
         }
         
         // install, configure, init and start MBeans from the services.xml file    
         server.invoke(
            new ObjectName("JBOSS-SYSTEM:service=ServiceController"),
            "deploy", 
            new Object[] {"services.xml"},
            new String[] {"java.lang.String"});
      
      }
      catch (RuntimeOperationsException e)
      {
         System.out.println("Runtime error");
         e.getTargetException().printStackTrace();
      }
      catch (RuntimeErrorException e)
      {
         System.out.println("Runtime error");
         e.getTargetError().printStackTrace();
      }
      catch (MBeanException e)
      {
         e.getTargetException().printStackTrace();
      }
      catch (RuntimeMBeanException e)
      {
         e.getTargetException().printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      // Done
      Date stopTime = new Date();
      Date lapsedTime = new Date(stopTime.getTime()-startTime.getTime());
      System.out.println("JBoss "+versionIdentifier+" Started in "+lapsedTime.getMinutes()+"m:"+lapsedTime.getSeconds()+"s");
   }
}


/*
  // Setup security
  // XXX marcf: what are the reason that would prevent us from making this an MBean
  // Set the JAAS login config file if not already set
  if( System.getProperty("java.security.auth.login.config") == null )
  {
  URL loginConfig = mlet.getResource("auth.conf");
  if( loginConfig != null )
  {
  System.setProperty("java.security.auth.login.config", loginConfig.toExternalForm());
  System.out.println("Using JAAS LoginConfig: "+loginConfig.toExternalForm());
  }
  else
  {
  System.out.println("Warning: no auth.conf found in config="+confName);
  }
  }

  // Set security using the mlet, if a patch was passed it will look in that path first
  URL serverPolicy = mlet.getResource("server.policy");

  if ( serverPolicy == null )
  {
  throw new IOException("server.policy missing");
  }

  System.setProperty("java.security.policy", serverPolicy.getFile());

// Set security manager
// Optional for better performance
if (System.getProperty("java.security.manager") != null)
System.setSecurityManager((SecurityManager)Class.forName(System.getProperty("java.security.manager")).newInstance());
*/

/*
 *   Revisions:
 *   20010618 marcf: 
 *     - Removed the jboss.properties, fully deprecated the use of properties
 *     - Moved security properties to the main body to take advantage of patch dir
 *     - Removed storage of initial configuration... useless!
 *     - Moved to addConfiguration call with explicit services.xml arguments
 *     - New signature support --net-install --patch-dir --conf-dir and [-n -p -c]   
 *     - Support for http based installations added
 *     - Got rid of wildcard imports 
 *     - Moved lib structure to lib 
 *
 */
