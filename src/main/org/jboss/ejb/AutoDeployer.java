/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;


/**
 *   The AutoDeployer is used to automatically deploy applications or
 *   components thereof.
 *
 *   <p> It can be used on either .jar or .xml files. The AutoDeployer
 *   can be configured to "watch" one or more files. If they are
 *   updated they will be redeployed.
 *
 *   <p> If it is set to watch a directory instead of a single file,
 *   all files within that directory will be watched separately.
 *
 *   <p> When a file is to be deployed, the AutoDeployer will use the
 *   configured deployer to deploy it.
 *
 *   @see org.jboss.deployment.J2eeDeployer
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *   @version $Revision: 1.18 $
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
   String deployerList = "";

   /** JMX names of the configured deployers */
   ObjectName[] deployerNames;

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

   /** Filters, one per configured deployer, to decide which files are
       deployable and which should be ignored */
   FilenameFilter[] deployableFilters = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public AutoDeployer()
   {
      this("");
   }

   public AutoDeployer(String urlList)
   {
      this ("J2EE:service=J2eeDeployer", urlList);
   }

   public AutoDeployer(String _namedDeployer, String urlList)
   {
      setDeployers(_namedDeployer);
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

   public void setDeployers(String deployers)
   {
      this.deployerList = deployers;
   }

   public String getDeployers()
   {
      return deployerList;
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

                  // Check if it's a deployable file
                  for (int j=0; j<deployerNames.length; ++j)
                  {
                     if (!deployableFilters[j].accept(null, fileUrl.getFile()))
                        continue; // Was not deployable - skip it...

                     if (deployedURLs.get(fileUrl) == null)
                     {
                        // This file has not been seen before
                        // Add to list of files to deploy automatically
                        watchedURLs.add(new Deployment(fileUrl));
                        deployedURLs.put(fileUrl, fileUrl);
                     }
                  }
               }
            }


            // undeploy removed jars
            Iterator iterator = watchedURLs.iterator();

            while (iterator.hasNext()) {
               Deployment deployment = (Deployment)iterator.next();
               URL url = deployment.url;

               // if the url is a file that doesn't exist
               // TODO: real urls
               if (url.getProtocol().startsWith("file") && ! new File(url.getFile()).exists()) {

                  // the file does not exist anymore. undeploy
                  log.log("Auto undeploy of "+url);
                  try {
                     undeploy(url.toString(), deployment.deployerName);
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
                  log.log("Auto deploy of "+deployment.url);
                  deployment.lastModified = lm;
                  try
                  {
                     deploy(deployment.url.toString(), deployment.deployerName);
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
      return name==null ? new ObjectName(OBJECT_NAME) : name;
   }

   protected void startService()
      throws Exception
   {
      // Save JMX names of configured deployers
      StringTokenizer deployers = new StringTokenizer(deployerList, ";");
      deployerNames = new ObjectName[deployers.countTokens()];
      deployableFilters = new FilenameFilter[deployerNames.length];
      for (int i=0; i<deployerNames.length && deployers.hasMoreTokens(); ++i)
      {
         String deployerName = deployers.nextToken().trim();
         try
         {
            deployerNames[i] = new ObjectName(deployerName);
         }
         catch (MalformedObjectNameException mfone)
         {
            log.warning("The string '" + deployerName + "'is not a valid " +
                        "object name - ignoring it.");
            continue;
         }

         // Ask the deployer for a filter to detect deployable files
         try
         {
            deployableFilters[i] = (FilenameFilter) server.invoke(
               deployerNames[i], "getDeployableFilter", new Object[0],
               new String[0]);
         }
         catch (ReflectionException re)
         {
            log.log("Deployer '" + deployerNames[i] + "' doesn't provide a " +
                    "filter - will try to deploy all files");
            deployableFilters[i] = new FilenameFilter()
               {
                  public boolean accept(File dir, String filename)
                  {
                     return true;
                  }
               };
         }
      }
      
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
                  watchedURLs.add(new Deployment(
                     urlFile.getCanonicalFile().toURL()));
                  log.log("Auto-deploying "+urlFile.getCanonicalFile());
               } catch (Exception e)
               {
                  log.warning("Cannot auto-deploy "+urlFile);
               }
            } else
            {
               // This is a directory whose contents shall be checked
               // for deployments
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
               try
               {
                  watchedURLs.add(new Deployment(
                     urlFile.getCanonicalFile().toURL()));
                  log.log("Auto-deploying "+urlFile.getCanonicalFile());
               } catch (Exception e)
               {
                  log.warning("Cannot auto-deploy "+urlFile);
               }
         } else // It's a real URL (probably http:)
         {
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
   protected void deploy(String url, ObjectName deployerName)
      throws Exception
   {
      try
      {
         // Call the appropriate deployer through the JMX server
         server.invoke(deployerName, "deploy", new Object[] { url },
                       new String[] { "java.lang.String" });
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

   protected void undeploy(String url, ObjectName deployerName)
      throws Exception
   {
      try
      {
         // Call the appropriate deployer through the JMX server
         server.invoke(deployerName, "undeploy", new Object[] { url },
                       new String[] { "java.lang.String" });
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
   class Deployment
   {
      long lastModified;
      URL url;
      URL watch;
      ObjectName deployerName;

      Deployment(URL url)
         throws MalformedURLException
      {
         this.url = url;
         for (int i=0; i<deployableFilters.length; ++i)
         {
            if (deployableFilters[i].accept(null, url.getFile()))
            {
               watch = url;
               deployerName = deployerNames[i];
               return;
            }
         }
         watch = new URL(url, "META-INF/ejb-jar.xml");
         // assume first configured deployer is responsible for
         // ejb-jars
         deployerName = deployerNames[0];
      }
   }
}
