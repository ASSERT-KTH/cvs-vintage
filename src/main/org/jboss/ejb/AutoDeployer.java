/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.ArrayList;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.ObjectName;

import org.jboss.logging.Log;
import org.jboss.util.MBeanProxy;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.deployment.J2eeDeployerMBean;


/**
 *   The AutoDeployer is used to automatically deploy EJB-jars.
 *	  It can be used on either .jar or .xml files. The AutoDeployer can
 *	  be configured to "watch" one or more files. If they are updated they will
 *	  be redeployed.
 *
 *	  If it is set to watch a directory instead of a single file, all files within that
 *	  directory will be watched separately.
 *
 *	  When a jar is to be deployed, the AutoDeployer will use a ContainerFactory to deploy it.
 *
 *   @see ContainerFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.12 $
 */
public class AutoDeployer
	extends ServiceMBeanSupport
   implements AutoDeployerMBean, Runnable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

	// Callback to the JMX agent
   MBeanServer server;

	// in case more then one J2eeDeployers are available
	String namedDeployer = "";

	// JMX name of the ContainerFactory
   ObjectName factoryName;

	// The watch thread
   boolean running = false;

	// Watch these directories for new files
   ArrayList watchedDirectories = new ArrayList();

	// These URL's have been deployed. Check for new timestamp
   HashMap deployedURLs = new HashMap();

	// These URL's are being watched
   ArrayList watchedURLs = new ArrayList();
   
   // URL list
   String urlList = "";

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public AutoDeployer()
   {
      this("");
   }
   
   public AutoDeployer(String urlList)
   {
	   this ("Default", urlList);
   }
	

   public AutoDeployer(String _namedDeployer, String urlList)
   {
	   setDeployer(_namedDeployer);
	   setURLs(urlList);
   }

	public void setURLs(String urlList)
	{
      this.urlList = urlList;
   }
   
   public String getURLs()
   {
      return urlList;
   }
   
   public void setDeployer(String deployer)
   {
      this.namedDeployer = deployer;
   }
   
   public String getDeployer()
   {
      return namedDeployer;
   }

   // Public --------------------------------------------------------
   public void run()
   {
      do
      {
         // Sleep
         if (running)
         {
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
         }

         try
         {
            // Check directories - add new entries to list of files
            for (int i = 0; i < watchedDirectories.size(); i++)
            {
               File dir = (File)watchedDirectories.get(i);
               File[] files = dir.listFiles();
               for (int idx = 0; idx < files.length; idx++)
               {
                  URL fileUrl = files[idx].toURL();

                  // Check if it's a JAR or zip or ear or war
                  if (!(fileUrl.getFile().endsWith(".jar") ||
                        fileUrl.getFile().endsWith(".ear") ||
                        fileUrl.getFile().endsWith(".war") ||
                        fileUrl.getFile().endsWith(".zip")))
                     continue; // Was not a JAR or zip - skip it...

                  if (deployedURLs.get(fileUrl) == null)
                  {
							// This file has not been seen before
							// Add to list of files to deploy automatically
                     watchedURLs.add(new Deployment(fileUrl));
                     deployedURLs.put(fileUrl, fileUrl);
                  }
               }
            }


			// undeploy removed jars
			Iterator iterator = watchedURLs.iterator();

			while (iterator.hasNext()) {
				URL url = ((Deployment)iterator.next()).url;

				// if the url is a file that doesn't exist
				// TODO: real urls
				if (url.getProtocol().startsWith("file") && ! new File(url.getFile()).exists()) {

					// the file does not exist anymore. undeploy
					log.log("Auto undeploy of "+url);
					try {
				   		undeploy(url.toString());
					} catch (Exception e) {
					    log.error("Undeployment failed");
						log.exception(e);
					}
					deployedURLs.remove(url);

					// this should be the safe way to call watchedURLS.remove
					iterator.remove();
				}
			}


            // Check watched URLs
            for (int i = 0; i < watchedURLs.size(); i++)
            {
               Deployment deployment = (Deployment)watchedURLs.get(i);

               // Get last modified timestamp
               long lm;
               if (deployment.watch.getProtocol().startsWith("file"))
               {
						// Get timestamp of file from file system
                  lm = new File(deployment.watch.getFile()).lastModified();
               } else
               {
						// Use URL connection to get timestamp
                  lm = deployment.watch.openConnection().getLastModified();
               }

               // Check old timestamp -- always deploy if first check
               if ((deployment.lastModified == 0) || (deployment.lastModified < lm))
               {
				   /*
                  // in case of file first check if it is really completely copied
                  // (check file size wait 1 second and compare new with old file size)
                  long size = new File(deployment.watch.getFile()).length ();
                  try {
                     Thread.currentThread().sleep (1000L);
                  } catch (InterruptedException _ie) {}
                  if (size != new File(deployment.watch.getFile()).length ())
                  {
                     log.log (deployment.url+" is not yet ready for deploy...");
                     continue;
                  }
				   */
                  
                  log.log("Auto deploy of "+deployment.url);
                  deployment.lastModified = lm;
                  try
                  {
                     deploy(deployment.url.toString());
                  } catch (Throwable e)
                  {
                     log.error("Deployment failed:"+deployment.url);
                     log.exception(e);

							// Deployment failed - won't retry until updated
                  }
               }
            }
         } catch (Exception e)
         {
            e.printStackTrace(System.err);

				// Stop auto deployer
            running = false;
         }
      } while(running);
   }

   // ServiceMBeanSupport overrides ---------------------------------
   public String getName()
   {
      return "Auto deploy";
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
   	this.server = server;
      return new ObjectName(OBJECT_NAME+namedDeployer);
   }

   protected void startService()
      throws Exception
   {
      // Save JMX name of EJB ContainerFactory
      factoryName = new ObjectName(namedDeployer);
      
      StringTokenizer urls = new StringTokenizer(urlList, ",");

      // Add URLs to list
      while (urls.hasMoreTokens())
      {
         String url = urls.nextToken().trim();

         // Check if directory
         File urlFile = new File(url.startsWith ("file:") ? url.substring (5) : url);
         if (urlFile.exists() && urlFile.isDirectory())
         {
            File metaFile = new File(urlFile, "META-INF"+File.separator+"ejb-jar.xml");
            if (metaFile.exists()) // It's unpackaged
            {
               try
               {
                  watchedURLs.add(new Deployment(urlFile.getCanonicalFile().toURL()));
                  log.log("Auto-deploying "+urlFile.getCanonicalFile());
               } catch (Exception e)
               {
                  log.warning("Cannot auto-deploy "+urlFile);
               }
            } else
            {
   			   // This is a directory whose contents shall be checked for deployments
               try
               {
                  watchedDirectories.add(urlFile.getCanonicalFile());
                  log.log("Watching "+urlFile.getCanonicalFile());
               } catch (IOException e)
               {
                  log.warning(e.toString());
               }
            }
         } else if (urlFile.exists()) // It's a file
         {
               // Check if it's a JAR or zip
               if (!(url.endsWith(".jar") ||
                     url.endsWith(".ear") ||
                     url.endsWith(".war") ||
                     url.endsWith(".zip")))
                  continue; // Was not a JAR or zip - skip it...

               try
               {
                  watchedURLs.add(new Deployment(urlFile.getCanonicalFile().toURL()));
                  log.log("Auto-deploying "+urlFile.getCanonicalFile());
               } catch (Exception e)
               {
                  log.warning("Cannot auto-deploy "+urlFile);
               }
         } else // It's a real URL (probably http:)
         {
            // Check if it's a JAR or zip
            if (!(url.endsWith(".jar") ||
                  url.endsWith(".ear") ||
                  url.endsWith(".war") ||
                  url.endsWith(".zip")))
               continue; // Was not a JAR or zip - skip it...

            try
            {
               watchedURLs.add(new Deployment(new URL(url)));
            } catch (MalformedURLException e)
            {
               // Didn't work
               log.warning("Cannot auto-deploy "+url);
            }
         }
      }


      run(); // Pre-deploy. This is done so that deployments available
      		 // on start of container is deployed ASAP

      // Start auto deploy thread
      running = true;
      new Thread(this, "Auto deploy").start();
   }

   protected void stopService()
   {
   	// Stop auto deploy thread
      running = false;
      
      // Clear lists
      watchedDirectories.clear();
      watchedURLs.clear();
      deployedURLs.clear();
   }

   // Protected -----------------------------------------------------
   protected void deploy(String url)
      throws Exception
   {
      try
      {
   		// Call the ContainerFactory that is loaded in the JMX server
         server.invoke(factoryName, "deploy",
                         new Object[] { url }, new String[] { "java.lang.String" });
      } catch (RuntimeMBeanException e)
      {
//          System.out.println("Caught a runtime MBean exception: "+e.getTargetException());
//          e.getTargetException().printStackTrace();
          throw e.getTargetException();
      } catch (MBeanException e)
      {
         throw e.getTargetException();
      } catch (RuntimeErrorException e)
      {
         throw e.getTargetError();
      }
   }

   protected void undeploy(String url)
      throws Exception
   {
      try
      {
   		// Call the ContainerFactory that is loaded in the JMX server
         server.invoke(factoryName, "undeploy",
                         new Object[] { url }, new String[] { "java.lang.String" });
      } catch (MBeanException e)
      {
         throw e.getTargetException();
      } catch (RuntimeErrorException e)
      {
         throw e.getTargetError();
      }
   }

   // Inner classes -------------------------------------------------

	// This class holds info about a deployement, such as the URL and the last timestamp
   static class Deployment
   {
      long lastModified;
      URL url;
      URL watch;

      Deployment(URL url)
         throws MalformedURLException
      {
         this.url = url;
         if (url.getFile().endsWith(".jar") ||
             url.getFile().endsWith(".ear") ||
             url.getFile().endsWith(".war"))
            watch = url;
         else
            watch = new URL(url, "META-INF/ejb-jar.xml");
      }
   }
}
