/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss;

import java.io.File;
import java.net.URL;

import javax.management.MBeanException;
import javax.management.RuntimeMBeanException;

import org.jboss.system.MBeanClassLoader;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.UnifiedClassLoader;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.jboss.system.Server;
import org.jboss.system.ServerConfig;

/**
 * The main entry-point for the JBoss server.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.66 $
 */
public class Main
   implements Runnable
{
   private final String[] args;
   
   public Main(final String[] args) {
      this.args = args;
   }

   public void run() {
      try {
         boot();
      }
      catch (MBeanException e) {
         e.getTargetException().printStackTrace();
      }
      catch (RuntimeMBeanException e) {
         e.getTargetException().printStackTrace();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void boot() throws Exception {
      //
      // Set a jboss.home property from the location of the Main.class jar
      // if the property does not exist.
      // 
      // marcf: we don't use this property at all for now
      // it should be used for all the modules that need a file "anchor"
      // it should be moved to an "FileSystemAnchor" MBean
      //
      if (System.getProperty("jboss.home") == null) {
         String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
         File runJar = new File(path);
         // Home dir should be the parent of the dir containing run.jar
         File homeDir = new File(runJar.getParent(), "..");
         System.setProperty("jboss.home", homeDir.getCanonicalPath());
      }
      String home = System.getProperty("jboss.home");
      
      if (System.getProperty("jboss.system.home") == null) {
         // default to jboss.home if jboss.system.home is not set
         System.setProperty("jboss.system.home", home);
      }
      String systemHome = System.getProperty("jboss.system.home");

      // Create a new server configuration object.  This object holds all
      // of the required information to get the server up and running.
      ServerConfig config = new ServerConfig(new File(systemHome));
         
      // set this from a system property or default to jboss
      String programName = System.getProperty("jboss.boot.loader.name", "jboss");
      String sopts = "-:hD:p:n:c:Vj:";
      LongOpt[] lopts = {
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("help-examples", LongOpt.NO_ARGUMENT, null, 10),
         new LongOpt("patch-dir", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
         new LongOpt("net-boot", LongOpt.REQUIRED_ARGUMENT, null, 'n'),
         new LongOpt("configuration", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
         new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'V'),
	 new LongOpt("jaxp", LongOpt.REQUIRED_ARGUMENT, null, 'j'),
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
               System.out.println("    -p, --patch-dir <dir>         Set the patch directory; Must be absolute");
               System.out.println("    -n, --net-boot <url>          Boot from net with the given url as base");
               System.out.println("    -c, --configuration <name>    Set the server configuration name");
               System.out.println("    -V, --version                 Show version information");
               System.out.println("    -j, --jaxp=<type>             Set the JAXP impl type (ie. crimson)");
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
               arg = getopt.getOptarg();
               config.setPatchURL(new URL(arg));
               break;
            
            case 'n':
               // set the net boot url
               arg = getopt.getOptarg();
               
               // make sure there is a protocol ?
               arg = arg.startsWith("http://") ? arg : "http://" + arg;
               
               // make sure there is a trailing '/'
               if (!arg.endsWith("/")) arg += "/";
               
               config.setInstallURL(new URL(arg));
               break;
            
            case 'c':
               // set the configuration name
               arg = getopt.getOptarg();
               config.setConfigName(arg);
               break;
               
            case 'V':
               // show version information
               System.out.println("JBoss " + Version.getInstance());
               System.out.println();
               System.out.println("Distributable under LGPL license.");
               System.out.println("See terms of license at gnu.org.");
               System.out.println();
               System.exit(0);
               break; // for completness

            case 'j':
	       // set the JAXP impl type
	       arg = getopt.getOptarg().toLowerCase();
	       String domFactoryType, saxFactoryType;

	       if (arg.equals("crimson")) {
		  domFactoryType = "org.apache.crimson.jaxp.DocumentBuilderFactoryImpl";
		  saxFactoryType = "org.apache.crimson.jaxp.SAXParserFactoryImpl";
	       }
	       else if (arg.equals("xerces")) {
		  domFactoryType = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
		  saxFactoryType = "org.apache.xerces.jaxp.SAXParserFactoryImpl";
	       }
	       else {
		  System.err.println("Invalid JAXP type: " + arg +
				     " (Expected 'crimson' or 'xerces')");
		  // don't continue, user needs to fix this!
		  System.exit(1);

		  // trick the compiler, so it does not complain that 
		  // the above variables might not being set
		  break;
	       }

	       // set the controlling properties
	       System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				  domFactoryType);
	       System.setProperty("javax.xml.parsers.SAXParserFactory",
				  saxFactoryType);
	       break;

            default:
               // this should not happen,
               // if it does throw an error so we know about it
               throw new Error("unhandled option code: " + code);
         }
      }
   
      // setup legecy properties
      // should do away with components that depend on these
      System.setProperty("jboss.system.installationURL", 
                         config.getInstallURL().toString());
      System.setProperty("jboss.system.configurationDirectory", 
                         config.getConfigURL().toString());
      System.setProperty("jboss.system.libraryDirectory", 
                         config.getLibraryURL().toString());

      // Make sure that shutdown exits the VM
      config.setExitOnShutdown(true);

      // Create & start the server
      Server server = new Server(config);
   }
   
   /**
    * This is where the magic begins.
    *
    * <P>Starts up inside of a "jboss" thread group to allow better
    *    identification of JBoss threads.
    *
    * @param args    The command line arguments.
    */
   public static void main(final String[] args) throws Exception {
      ThreadGroup threads = new ThreadGroup("jboss");
      new Thread(threads, new Main(args), "jboss-main").start();
   }

   /**
    * This method is here so that if JBoss is running under
    * Alexandria (An NT Service Installer), Alexandria can shutdown 
    * the system down correctly.
    */
   public static void systemExit(String argv[]) {
      System.exit(0);
   }      
}
