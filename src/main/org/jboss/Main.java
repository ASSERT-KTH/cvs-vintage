/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.MLet;

import org.jboss.system.MBeanClassLoader;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.URLClassLoader;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
* The main entry point for the JBoss server.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
* @version $Revision: 1.59 $
*
* <b>Revisions:</b>
* <p>
* <b>20010830 marcf:</b>
* <ul>
*   <li>Initial import, support for net-install
* </ul>
* <b>20010925 jason:</b>
* <ul>
*   <li>Replaced custom command line option parsing with gnu.getopt.
*   <li>Added -D option to set system properties
* </ul>
* <b>20011030 marcf:</b>
* <ul>
*   <li>Replaced net-install by net-boot.  Net-install should be reserved for installation 
*       that really duplicate the code on the local machines. net-boot doesn't and just runs in VM
* </ul>
*/
public class Main
{
   /**
   * The version & build information holder.
   */
   private Version version = Version.getInstance();
   
   /**
   * Constructor for the Main object
   *
   * @param installURL    The install URL.
   * @param confDir       The configuration directory.
   * @param patchDir      The patch directory.
   * @param libDir        The library directory.
   * @param spineDir      The spine directory.
   */
   public Main(String installURL,
      String confDir,
      String patchDir,
      String libDir,
      String spineDir)
   {
      long startTime = System.currentTimeMillis();
      
      try
      {
         final PrintStream err = System.err;
         
         System.setProperty("jboss.system.started", new Date(startTime).toString());
         System.setProperty("jboss.system.installationURL", installURL);
         System.setProperty("jboss.system.configurationDirectory", confDir);
         System.setProperty("jboss.system.patchDirectory", patchDir);
         System.setProperty("jboss.system.libraryDirectory", libDir);
         System.setProperty("jboss.system.version", version.toString());
         System.setProperty("jboss.system.version.name", version.getName());
         
         // Give feedback about from where jndi.properties is read
         URL jndiLocation = this.getClass().getResource("/jndi.properties");
         if (jndiLocation instanceof URL)
         {
            System.out.println("Please make sure the following is intended " +
               "(check your CLASSPATH): jndi.properties is " +
               "read from " + jndiLocation);
         }
         
         // Create MBeanServer
         final MBeanServer server =
         MBeanServerFactory.createMBeanServer("JBOSS-SYSTEM");
         
         // Initialize the MBean libraries repository
         server.registerMBean(ServiceLibraries.getLibraries(),
            new ObjectName(server.getDefaultDomain(),
               "spine",
               "ServiceLibraries"));
         
         // Build the list of URL for the spine to boot
         ArrayList urls = new ArrayList();
         
         // Add the patch directory
         addJars(patchDir, urls);
         
         // Add configuration directory to be able to load files
         urls.add(new URL(confDir));
         
         // Add the local path stuff
         urls.add(new URL(libDir + "log4j.jar"));
         urls.add(new URL(libDir + "jboss-spine.jar"));
         
         // Crimson and jaxp are fucked up right now, cl usage fixed in new
         // version of jaxp according to the developers.
         //urls.add(new URL(libDir+"jaxp.jar"));
         //urls.add(new URL(libDir+"crimson.jar"));
         
         Iterator bootURLs = urls.iterator();
         while (bootURLs.hasNext())
         {
            // The libraries will register themselves with the libraries
            URL thisUrl = (URL) bootURLs.next();
            //Only the boot urls are keyed on themselves: 
            //everything else is copied for loading but keyed on the
            //original deployed url.
            new URLClassLoader(new URL[]{thisUrl}, thisUrl);
         }
         
         // Create MBeanClassLoader for the base system
         ObjectName loader = new ObjectName(server.getDefaultDomain(),
            "spine",
            "ServiceClassLoader");
         MBeanClassLoader mcl = new MBeanClassLoader(loader);
         
         try
         {
            server.registerMBean(mcl, loader);
            
            // Set ServiceClassLoader as classloader for the construction of
            // the basic JBoss-System
            Thread.currentThread().setContextClassLoader(mcl);
                        
            // Create the Loggers
            server.createMBean("org.jboss.logging.Log4jService", null, loader);
            
            // General Purpose Architecture information
            server.createMBean("org.jboss.system.Info", null, loader);
            
            // Shutdown stuff
            server.createMBean("org.jboss.system.Shutdown", null, loader);
            
            // Properties for the JAXP configuration
            if (System.getProperty("javax.xml.parsers.DocumentBuilderFactory") == null) {
               System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                  "org.apache.crimson.jaxp.DocumentBuilderFactoryImpl");
            }
            if (System.getProperty("javax.xml.parsers.SAXParserFactory") == null) {
               System.setProperty("javax.xml.parsers.SAXParserFactory",
                  "org.apache.crimson.jaxp.SAXParserFactoryImpl");
            }
            
            //
            // Service Deployment
            //
            
            // Controller
            server.createMBean("org.jboss.system.ServiceController",
               null, loader);
            
            // Deployer
            ObjectName serviceDeployer = server.createMBean(
               "org.jboss.deployment.ServiceDeployer",
               null, loader).getObjectName();

            //Ok, now deploy jboss-service.xml
            server.invoke(serviceDeployer, 
                          "deploy", 
                          new Object[] {confDir + "jboss-service.xml"},
                          new String[] {"java.lang.String"});
         }
         catch(RuntimeMBeanException e)
         {
    //        e.getTargetException().printStackTrace();
         }
         catch (RuntimeOperationsException roe)
         {
    //        roe.getTargetException().printStackTrace();
         }
         catch (RuntimeErrorException ree)
         {
    //        ree.getTargetError().printStackTrace();
         }
         catch (MBeanException mbe)
         {
    //        mbe.getTargetException().printStackTrace();
         }
         catch (ReflectionException re)
         {
    //        re.getTargetException().printStackTrace();
         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      // Done
      long stopTime = System.currentTimeMillis();
      long lapsedTime = stopTime - startTime;
      long minutes = lapsedTime / 60000;
      long seconds = (lapsedTime - 60000 * minutes) / 1000;
      long milliseconds = (lapsedTime -60000 * minutes - 1000 * seconds);

      System.out.println("JBoss (MX microkernel) " + version +
         " [" + version.getName() + "] Started in " +
         minutes  + "m:" + seconds  + "s:" +milliseconds +"ms");
   }
   
   /**
   * The main entry-point for the Main class
   *
   * @param args    The command line arguments
   * 
   * @throws Exception     Description of Exception
   */
   public static void main(final String[] args) throws Exception
   {
      //
      // Set a jboss.home property from the location of the Main.class jar
      // if the property does not exist.
      // 
      // marcf: we don't use this property at all for now
      // it should be used for all the modules that need a file "anchor"
      // it should be moved to an "FileSystemAnchor" MBean
      //
      if (System.getProperty("jboss.home") == null)
      {
         String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
         File runJar = new File(path);
         // Home dir should be the parent of the dir containing run.jar
         File homeDir = new File(runJar.getParent(), "..");
         System.setProperty("jboss.home", homeDir.getCanonicalPath());
      }
      String home = System.getProperty("jboss.home");
      
      if (System.getProperty("jboss.system.home") == null)
      {
         // default to jboss.home if jboss.system.home is not set
         System.setProperty("jboss.system.home", home);
      }
      String systemHome = System.getProperty("jboss.system.home");
      
      String installURL = new File(systemHome).toURL().toString();
      if (!installURL.endsWith(File.separator)) {
         installURL += File.separator;
      }
      
      // Default configuration name is "default",
      // i.e. all conf files are in "/conf/default"
      String configDir = "default";
      String patchDir = "";
      
      // Given conf name
      
      //
      // parse command line options
      //
      
      // set this from a system property or default to jboss
      String programName = System.getProperty("jboss.boot.loader.name",
         "jboss");
      String sopts = "-:hD:p:n:c:";
      LongOpt[] lopts = {
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("help-examples", LongOpt.NO_ARGUMENT, null, 10),
         new LongOpt("patch-dir", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
         new LongOpt("net-boot", LongOpt.REQUIRED_ARGUMENT, null, 'n'),
         new LongOpt("configuration", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
      };
      
      Getopt getopt = new Getopt(programName, args, sopts, lopts);
      int code;
      String arg;
      
      while ((code = getopt.getopt()) != -1) {
         switch (code) {
            case ':':
            case '?':
               // for now both of these should exit with error status
               System.exit(1);
            break; // for completeness
            
            case 1:
               // this will catch non-option arguments
               // (which we don't currently care about)
               System.err.println(programName +
                  ": unused non-option argument: " +
                  getopt.getOptarg());
            break; // for completeness
            
            case 'h':
               // show command line help
               System.out.println("usage: " + programName + " [options]");
               System.out.println();
               System.out.println("options:");
               System.out.println("    -h, --help                    Show this help message");
               System.out.println("    --help-examples               Show some command line examples");
               System.out.println("    --                            Stop processing options");
               System.out.println("    -D<name>[=<value>]            Set a system property");
               System.out.println("    -p, --patch-dir <dir>         Set the patch directory, takes an absolute file name");
               System.out.println("    -n, --net-boot <url>          Boot from net with the given url as base");
               System.out.println("    -c, --configuration <name>    Set the server configuration name");
               System.out.println();               
               System.exit(0);
            break; // for completeness
            
            case 'D':
               // set a system property
               arg = getopt.getOptarg();
               String name, value;
               int i = arg.indexOf("=");
               if (i == -1) {
                  name = arg;
                  value = "true";
               }
               else {
                  name = arg.substring(0, i);
                  value = arg.substring(i + 1, arg.length());
               }
               System.setProperty(name, value);
            break;
            
            case 10:
               // show help examples
               System.out.println("example: " + programName + " --net-boot http://www.jboss.org/jboss --configuration jboxx --patch-dir /tmp/dir");
               System.out.println("will download from the webserver and run the the configuration called jboxx, it will uses jar patches found in /tmp/dir");
               System.out.println();
               System.exit(0);
            break; // for completeness
            
            case 'p':
               // set the local patch directory
               patchDir = getopt.getOptarg();
            break;
            
            case 'n':
               // set the net boot url
               arg = getopt.getOptarg();
               installURL = arg.startsWith("http://") ? arg : "http://" + arg;
               
               // make sure there is a trailing '/'
               if (!installURL.endsWith("/")) installURL += "/";
            break;
            
            case 'c':
               // set the configuration name
               configDir = getopt.getOptarg();
            break;
            
            default:
               // this should not happen,
               // if it does throw an error so we know about it
               throw new Error("unhandled option code: " + code);
         }
      }
      
      //
      // setup the boot environment and get things cooking
      //
      
      // should really turn these into file:// urls so we don't have to worry about File.sep* fluff
      configDir = installURL + (installURL.startsWith("http:") ? "conf/" + configDir + "/" : "conf" + File.separatorChar + configDir + File.separatorChar);
      String loadDir = installURL + (installURL.startsWith("http:") ? "lib/ext/" : "lib" + File.separatorChar + "ext" + File.separatorChar);
      String spineDir = installURL + (installURL.startsWith("http:") ? "lib/" : "lib" + File.separatorChar);
      
      final String iURL = installURL;
      final String cDir = configDir;
      final String pDir = patchDir;
      final String lDir = loadDir;
      final String sDir = spineDir;
      
      // Start server - Main does not have the proper permissions
      AccessController.doPrivileged(
         new PrivilegedAction()
         {
            public Object run()
            {
               new Main(iURL, cDir, pDir, lDir, sDir);
               return null;
            }
         });
   }
   
   private void addJars(String directory, ArrayList urls)
   throws Exception
   {
      if (directory != null && directory != "")
      {
         // The string must be a local file
         File dir = new File(directory);
         if (dir.exists()) {
            
            File[] jars = dir.listFiles(new java.io.FileFilter()
               {
                  public boolean accept(File pathname)
                  {
                     String name = pathname.getName();
                     return name.endsWith(".jar") || name.endsWith(".zip");
                  }
               });
            
            // Add the local file patch directory
            urls.add(dir.toURL());
            
            for (int j = 0; jars != null && j < jars.length; j++)
            {
               File jar = jars[j];
               URL u = jar.getCanonicalFile().toURL();
               urls.add(u);
            }
         }
      }
   }
}
