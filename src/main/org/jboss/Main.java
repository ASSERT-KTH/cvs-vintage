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
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.loading.MLet;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.MBeanException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeOperationsException;

import org.jboss.system.ServiceLibraries;
import org.jboss.system.MBeanClassLoader;
import org.jboss.system.URLClassLoader;
/**
*
*   @see <related>
*   @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>.
* @version $Revision: 1.44 $
* 
* <p><b>  Revisions:</b>
* <p><b>  20010830 marcf: </b>
*  <ul>
*    <li>Initial import, support for net-install
*  </ul>
*
*
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
		
		// Constants -----------------------------------------------------
		
		String versionIdentifier = "3.0 ALPHA [RABBIT-HOLE]";
		
		// Attributes ----------------------------------------------------
		
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
			System.setProperty("jboss.system.home", homeDir.getCanonicalPath());
		}
		
		String installURL = new File(System.getProperty("jboss.system.home")).toURL().toString()+File.separatorChar;
		String configDir = "default"; // Default configuration name is "default", i.e. all conf files are in "/conf/default"
		String patchDir = "";
		
		// Given conf name
		
		for(int a = 0; a < args.length; a ++)
		{
			
			if (args[a].endsWith("help")) 
			{
				
				System.out.println("Usage: run --patch-dir --net-install --configuration");
				System.out.println("For example: run.sh --net-install http://www.jboss.org/jboss --configuration jboxx");
				System.out.println(" will download from the webserver and run the the configuration called jboxx");
				
			}
			if( args[a].startsWith("--patch-dir") || args[a].startsWith("-p"))
				patchDir = args[a+1];
			
			else if( args[a].startsWith("--net-install") || args[a].startsWith("-n"))
			{           
				installURL = args[a+1].startsWith("http://") ?  args[a+1] : "http://"+args[a+1] ;
				if (!installURL.endsWith("/"))
					installURL = installURL+"/";
			}
			
			else if(args[a].startsWith("--configuration") || args[a].startsWith("-c"))
				configDir = args[a+1];
		}
		
		configDir = installURL + (installURL.startsWith("http:") ? "conf/"+configDir+"/" : "conf"+ File.separatorChar+ configDir+ File.separatorChar);
		String loadDir = installURL + (installURL.startsWith("http:") ? "lib/ext/" : "lib"+ File.separatorChar+"ext"+File.separatorChar);
		String spineDir = installURL + (installURL.startsWith("http:") ? "lib/" : "lib"+ File.separatorChar);
		
		
		final String iURL = installURL;
		final String cDir = configDir;
		final String pDir = patchDir;
		final String lDir = loadDir;
		final String sDir = spineDir;
		
		// Start server - Main does not have the proper permissions
		AccessController.doPrivileged(new PrivilegedAction()      
			{
				public Object run()
				{
					new Main(iURL, cDir, pDir, lDir, sDir);
					return null;
				}
			});
	}
	
	// Constructors --------------------------------------------------
	public Main(String installURL, String confDir, String patchDir, String libDir, String spineDir)
	{
		Date startTime = new Date();
		
		try
		{
			final PrintStream err = System.err;
			
			System.setProperty("jboss.system.installationURL", installURL);
			System.setProperty("jboss.system.configurationDirectory", confDir);
			System.setProperty("jboss.system.patchDirectory", patchDir);
			System.setProperty("jboss.system.libraryDirectory", libDir);
			System.setProperty("jboss.system.version", versionIdentifier);
			
			// Give feedback about from where jndi.properties is read
			URL jndiLocation = this.getClass().getResource("/jndi.properties");
			if (jndiLocation instanceof URL) {
				System.out.println("Please make sure the following is intended (check your CLASSPATH):");
				System.out.println(" jndi.properties is read from "+jndiLocation);
			}
			
			// Create MBeanServer 
			final MBeanServer server = MBeanServerFactory.createMBeanServer("JBOSS-SYSTEM");
			
			// Initialize the MBean libraries repository
			server.registerMBean(ServiceLibraries.getLibraries(), new ObjectName(server.getDefaultDomain(), "spine", "ServiceLibraries"));
			
			// Build the list of URL for the spine to boot
			ArrayList urls = new ArrayList();
			
			// Add the patch directory 
			addJars(patchDir, urls);
			
			// Add configuration directory to be able to load files
			urls.add(new URL(confDir));
			
			// Add the local path stuff
			urls.add(new URL(libDir+"log4j.jar"));
			urls.add(new URL(libDir+"jboss-spine.jar"));
	
			// Crimson and jaxp are fucked up right now, cl usage fixed in new version of jaxp
			// according to the developers.
			//urls.add(new URL(libDir+"jaxp.jar"));
			//urls.add(new URL(libDir+"crimson.jar"));
	
			
			Iterator bootURLs = urls.iterator();
			while (bootURLs.hasNext()) 
			{
				// The libraries will register themselves with the libraries
				new URLClassLoader(new URL[] {(URL) bootURLs.next()});
			}
			
			// Create MBeanClassLoader for the base system
			ObjectName loader = new ObjectName(server.getDefaultDomain(), "spine", "ServiceClassLoader");
			MBeanClassLoader mcl = new MBeanClassLoader(loader);
			
			try { 
				server.registerMBean(mcl, new ObjectName(server.getDefaultDomain(), "spine", "ServiceClassLoader"));
				// Set ServiceClassLoader as classloader for the construction of the basic JBoss-System
				Thread.currentThread().setContextClassLoader(mcl);
				
				System.out.println("Looking for the docuemnt");
				try {
					mcl.loadClass("javax.xml.parsers.DocumentBuilderFactory");
					System.out.println("I am ok, go figure ");
				}
				catch (ClassNotFoundException ed) {ed.printStackTrace();}
				
				//Create the Loggers 
				server.createMBean("org.jboss.logging.Logger",null, loader);
				server.createMBean("org.jboss.logging.Log4jService", null, loader);
				
				// General Purpose Architecture information
				server.createMBean("org.jboss.system.Info", null, loader);
				
				// Shutdown stuff
				server.createMBean("org.jboss.system.Shutdown", null, loader);
				
				/*
				* Service Deployment
				*/
				//Properties for the JAXPconfiguration
				System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.crimson.jaxp.DocumentBuilderFactoryImpl");
				System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.crimson.jaxp.SAXParserFactoryImpl");
				
				// Controller
				server.createMBean("org.jboss.system.ServiceController", null, loader);
				
				// Deployer
				server.createMBean("org.jboss.deployment.ServiceDeployer", null, loader);
				
			}
			catch (RuntimeOperationsException roe) {roe.getTargetException().printStackTrace();}
			catch (RuntimeErrorException ree) {ree.getTargetError().printStackTrace();}
			catch (MBeanException mbe) {mbe.getTargetException().printStackTrace();}
			catch (ReflectionException re) {re.getTargetException().printStackTrace();}
			
			/*	
			//URL mletConf = mlet.getResource("boot.jmx");
			URL mletConf = new URL(confDir+"boot.jmx");
			
			Set beans = mlet.getMBeansFromURL(mletConf);
			
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
			*/
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
	
	
	private void addJars(String directory, ArrayList urls) 
	throws Exception
	{
		if( directory != null && directory != "" )
		{
			// The string must be a local file
			File dir = new File(directory);
			File[] jars = dir.listFiles(new java.io.FileFilter()
				{
					public boolean accept(File pathname)
					{
						String name = pathname.getName();
						return name.endsWith(".jar") || name.endsWith(".zip");
					}
				}
			);
			
			// Add the local file patch directory
			urls.add(directory);
			
			for(int j = 0; jars != null && j < jars.length; j ++)
			{
				File jar = jars[j];
				URL u = jar.getCanonicalFile().toURL();
				urls.add(u);
			}
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
}	