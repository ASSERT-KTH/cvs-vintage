/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

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
import javax.management.InstanceNotFoundException;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.MBeanServerNotification;

import org.jboss.system.ServiceMBeanSupport;

/**
 * The AutoDeployer is used to automatically deploy applications or components
 * thereof. <p>
 *
 * It can be used on either .jar or .xml files. The AutoDeployer can be
 * configured to "watch" one or more files. If they are updated they will be
 * redeployed. <p>
 *
 * If it is set to watch a directory instead of a single file, all files within
 * that directory will be watched separately. <p>
 *
 * When a file is to be deployed, the AutoDeployer will use the configured
 * deployer to deploy it. <p>
 *
 * If a given deployer mbean does not exist at startup, files for that deployer
 * will not be deployed until that deployer does exist (is registered with main
 * MBeanServer).
 *
 * @see org.jboss.deployment.J2eeDeployer
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @version $Revision: 1.1 $
 */
public class FarmAutoDeployer
       extends ServiceMBeanSupport
       implements FarmAutoDeployerMBean, NotificationListener,Runnable
{
   // Constants -----------------------------------------------------
   // Attributes ----------------------------------------------------
   
   /**
    * Callback to the JMX agent.
    */
   MBeanServer server;

   /**
    * In case more then one J2eeDeployers are available.
    */
   String deployerList = "";

   /**
    * JMX names of the configured deployers
    */
   ObjectName[] deployerNames;

   /**
    * The watch thread.
    */
   boolean running = false;

   /**
    * Watch these directories for new files.
    */
   ArrayList watchedDirectories = new ArrayList();

   /**
    * These URL's have been deployed. Check for new timestamp.
    */
   HashMap deployedURLs = new HashMap();

   /**
    * These URL's are being watched.
    */
   ArrayList watchedURLs = new ArrayList();

   /**
    * URL list.
    */
   String urlList = "";

   /**
    * TimeOut that in case of big ears to deploy should be set high enough.
    */
   int timeout = 3000;

   /**
    * Filters, one per configured deployer, to decide which files are deployable
    * and which should be ignored.
    */
   FilenameFilter[] deployableFilters;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Constructor for the AutoDeployer object
    */
   public FarmAutoDeployer()
   {
      this("");
   }

   /**
    * Constructor for the AutoDeployer object
    *
    * @param urlList Description of Parameter
    */
   public FarmAutoDeployer(String urlList)
   {
      this("J2EE:service=J2eeDeployer", urlList);
   }

   /**
    * Constructor for the AutoDeployer object
    *
    * @param _namedDeployer Description of Parameter
    * @param urlList Description of Parameter
    */
   public FarmAutoDeployer(String _namedDeployer, String urlList)
   {
      setDeployers(_namedDeployer);
      setURLs(urlList);
   }

   /**
    * Sets the URLs attribute of the AutoDeployer object
    *
    * @param urlList The new URLs value
    */
   public void setURLs(String urlList)
   {
      this.urlList = urlList;
   }

   /**
    * Sets the Deployers attribute of the AutoDeployer object
    *
    * @param deployers The new Deployers value
    */
   public void setDeployers(String deployers)
   {
      this.deployerList = deployers;
   }

   /**
    * Sets the Timeout attribute of the AutoDeployer object
    *
    * @param to The new Timeout value
    */
   public void setTimeout(int to)
   {
      this.timeout = to;
   }

   /**
    * Gets the URLs attribute of the AutoDeployer object
    *
    * @return The URLs value
    */
   public String getURLs()
   {
      return urlList;
   }

   /**
    * Gets the Deployers attribute of the AutoDeployer object
    *
    * @return The Deployers value
    */
   public String getDeployers()
   {
      return deployerList;
   }

   /**
    * Gets the Timeout attribute of the AutoDeployer object
    *
    * @return The Timeout value
    */
   public int getTimeout()
   {
      return timeout;
   }

   // ServiceMBeanSupport overrides ---------------------------------

   /**
    * Gets the Name attribute of the AutoDeployer object
    *
    * @return The Name value
    */
   public String getName()
   {
      return "Auto Deployer";
   }

   // Public --------------------------------------------------------

   /**
    * Main processing method for the AutoDeployer object
    */
   public void run()
   {
      do
      {
         // Sleep
         if (running)
         {
            try
            {
               if (log.isTraceEnabled())
               {
                  log.trace("Wait for " + timeout / 1000 + " seconds");
               }
               Thread.sleep(timeout);
            }
            catch (InterruptedException e)
            {
               log.debug("interrupted; ignoring", e);
            }
         }

         try
         {
            // Check directories - add new entries to list of files
            scanWatchedDirectories();

            // Undeploy removed jars
            Iterator iterator = watchedURLs.iterator();

            while (iterator.hasNext())
            {
               Deployment deployment = (Deployment)iterator.next();
               URL url = deployment.url;

               // if the url is a file that doesn't exist
               // TODO: real urls
               if (url.getProtocol().startsWith("file") && !new File(url.getFile()).exists())
               {

                  // the file does not exist anymore. undeploy
                  log.info("Auto undeploy of " + url);
                  try
                  {
                     undeploy(url.toString(), deployment.deployerName);
                  }
                  catch (Exception e)
                  {
                     log.error("Undeployment failed", e);
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
               }
               else
               {
                  // Use URL connection to get timestamp
                  lm = deployment.watch.openConnection().getLastModified();
               }

               // Check old timestamp -- always deploy if first check
               if ((deployment.lastModified == 0) || (deployment.lastModified < lm))
               {
                  log.info("Auto deploy of " + deployment.url);
                  deployment.lastModified = lm;
                  try
                  {
                     deploy(deployment.url.toString(), deployment.deployerName);
                  }
                  catch (Throwable e)
                  {
                     log.error("Deployment failed:" + deployment.url, e);

                     // Deployment failed - won't retry until updated
                  }
               }
            }
         }
         catch (Exception e)
         {
            log.fatal("auto deployer failure; can not continue", e);
            // Stop auto deployer
            running = false;
         }
      } while (running);
   }

   /**
    * Gets the ObjectName attribute of the AutoDeployer object
    *
    * @param server Description of Parameter
    * @param name Description of Parameter
    * @return The ObjectName value
    * @exception MalformedObjectNameException Description of Exception
    */
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
          throws MalformedObjectNameException
   {
      this.server = server;
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }

   /**
    * #Description of the Method
    *
    * @exception Exception Description of Exception
    */
   protected void startService()
          throws Exception
   {
      // Save JMX names of configured deployers
      StringTokenizer deployers = new StringTokenizer(deployerList, ";");
      deployerNames = new ObjectName[deployers.countTokens()];
      deployableFilters = new FilenameFilter[deployerNames.length];
      for (int i = 0; i < deployerNames.length && deployers.hasMoreTokens(); ++i)
      {
         String deployerName = deployers.nextToken().trim();
         try
         {
            deployerNames[i] = new ObjectName(deployerName);
         }
         catch (MalformedObjectNameException mfone)
         {
            log.warn("The string '" + deployerName + "'is not a valid " +
                  "object name - ignoring it.");
            continue;
         }

         // Ask the deployer for a filter to detect deployable files
         try
         {
            deployableFilters[i] = (FilenameFilter)server.invoke(
                  deployerNames[i], "getDeployableFilter", new Object[0],
                  new String[0]);
         }
         catch (ReflectionException re)
         {
            log.info("Deployer '" + deployerNames[i] + "' doesn't provide a " +
                  "filter - will try to deploy all files");
            deployableFilters[i] =
               new FilenameFilter()
               {
                  /**
                   * #Description of the Method
                   *
                   * @param dir Description of Parameter
                   * @param filename Description of Parameter
                   * @return Description of the Returned Value
                   */
                  public boolean accept(File dir, String filename)
                  {
                     return true;
                  }
               };
         }
         catch (InstanceNotFoundException e)
         {
            log.info("Deployer '" + deployerNames[i] + "' isn't yet registered " +
                    "files for this deployer will not be deployed until it is deployed.");
            deployableFilters[i] = null;
         }
      }

      StringTokenizer urls = new StringTokenizer(urlList, ",");

      // Add URLs to list
      while (urls.hasMoreTokens())
      {
         String url = urls.nextToken().trim();

         // Check if directory
         File urlFile = new File(url.startsWith("file:") ? url.substring(5) : url);
         if (urlFile.exists() && urlFile.isDirectory())
         {
            File metaFile = new File(urlFile, "META-INF" + File.separator + "ejb-jar.xml");
            if (metaFile.exists())
            {
               // It's unpackaged

               try
               {
                  watchedURLs.add(new Deployment(
                        urlFile.getCanonicalFile().toURL()));
                  log.info("Auto-deploying " + urlFile.getCanonicalFile());
               }
               catch (Exception e)
               {
                  log.warn("Cannot auto-deploy " + urlFile);
               }
            }
            else
            {
               // This is a directory whose contents shall be checked
               // for deployments
               try
               {
                  File dir = urlFile.getCanonicalFile();
                  watchedDirectories.add(dir);
                  log.info("Watching " + dir);

                  // scan the directory now, so that the ordered deployments
                  // will work, when only a base directory is specified
                  scanDirectory(dir);
               }
               catch (IOException e)
               {
                  log.warn("failed to add watched directory", e);
               }
            }
         }
         else if (urlFile.exists())
         {
            // It's a file

            try
            {
               watchedURLs.add(new Deployment(
                     urlFile.getCanonicalFile().toURL()));
               log.info("Auto-deploying " + urlFile.getCanonicalFile());
            }
            catch (Exception e)
            {
               log.warn("Cannot auto-deploy " + urlFile);
            }
         }
         else
         {
            // It's a real URL (probably http:)

            try
            {
               watchedURLs.add(new Deployment(new URL(url)));
            }
            catch (MalformedURLException e)
            {
               // Didn't work
               log.warn("Cannot auto-deploy " + url);
            }
         }
      }

      // listen for the deployers to be deployed or undeployed
      // has to be done before the pre-deploy below so that deployers deployed 
      // during the pre-deploy are not missed.
      server.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"),this,null,null);

      // Pre-deploy. This is done so that deployments available
      // on start of container is deployed ASAP
//AS      run();

      // Start auto deploy thread
      running = true;
      new Thread(this, "AutoDeployer").start();
      
   }

   public void handleNotification(Notification notification, Object handback)
   {
      String type = notification.getType();
      if(type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
      {
         ObjectName mbean = ((MBeanServerNotification)notification).getMBeanName();
         
         log.debug("Received notification of mbean "+mbean+"'s deployment.");
         
         for(int i=0; i<deployerNames.length; i++)
         {
            if(deployerNames[i].equals(mbean))
            {
               log.info("Deployer '" + deployerNames[i] + "' deployed, " +
                       "now available for deployments.");
               try
               {
                  deployableFilters[i] = (FilenameFilter) server.invoke(
                     deployerNames[i], "getDeployableFilter", new Object[0],
                     new String[0]);
               }
               catch (ReflectionException re)
               {
                  log.info("Deployer '" + deployerNames[i] + "' doesn't provide a " +
                          "filter - will try to deploy all files");
                  deployableFilters[i] = new FilenameFilter()
                     {
                        public boolean accept(File dir, String filename)
                        {
                           return true;
                        }
                     };
               }
               catch(Exception e)
               {
                  log.error("Exception occurred accessing deployer '" + deployerNames[i] + "'.",e);
               }
            }
         }
      }
      // if a unregister simply clear filter to signal no more deployments
      else if(type.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
      {
         ObjectName mbean = ((MBeanServerNotification)notification).getMBeanName();
         for(int i=0; i<deployerNames.length; i++)
         {
            if(deployerNames[i].equals(mbean))
            {
               deployableFilters[i] = null;
               log.info("Deployer '" + deployerNames[i] + "' undeployed, " +
                       "no longer available for deployments.");
            }
         }
      }
   }
   
   // Protected -----------------------------------------------------

   /**
    * Scan the watched directories list, add new deployement entires for each
    * that does not already exist in the watched urls map.
    *
    * @throws MalformedURLException
    */
   protected void scanWatchedDirectories()
          throws MalformedURLException
   {
      for (int i = 0; i < watchedDirectories.size(); i++)
      {
         File dir = (File)watchedDirectories.get(i);
         scanDirectory(dir);
      }
   }

   /**
    * Scan a single directory and add new deployment entries for each deployable
    * file that does not already exist.
    *
    * @param dir The directory to scan.
    * @throws MalformedURLException
    */
   protected void scanDirectory(final File dir)
          throws MalformedURLException
   {
      File[] files = dir.listFiles();
      for (int idx = 0; idx < files.length; idx++)
      {
         URL fileUrl = files[idx].toURL();
         // Check if it's a deployable file
         for (int j = 0; j < deployerNames.length; ++j)
         {
            if (deployableFilters[j] == null || !deployableFilters[j].accept(null, fileUrl.getFile()))
            {
               continue; 
            }
            // Was not deployable - skip it...
            
            if (deployedURLs.get(fileUrl) == null)
            {
               // This file has not been seen before
               // Add to list of files to deploy automatically
               watchedURLs.add(new Deployment(fileUrl));
               deployedURLs.put(fileUrl, fileUrl);
               log.info("Auto-deploying " + fileUrl);
            }
         }
      }
   }

   /**
    * #Description of the Method
    */
   protected void stopService()
   {
      // Stop auto deploy thread
      running = false;

      // Clear lists
      watchedDirectories.clear();
      watchedURLs.clear();
      deployedURLs.clear();
   }

   /**
    * #Description of the Method
    *
    * @param url Description of Parameter
    * @param deployerName Description of Parameter
    * @exception Exception Description of Exception
    */
   protected void deploy(String url, ObjectName deployerName)
          throws Exception
   {
      try
      {
         // Call the appropriate deployer through the JMX server
         server.invoke(deployerName, "deploy", new Object[]{url},
               new String[]{"java.lang.String"});
      }
      catch (RuntimeMBeanException e)
      {
         throw e.getTargetException();
      }
      catch (MBeanException e)
      {
         throw e.getTargetException();
      }
      catch (RuntimeErrorException e)
      {
         throw e.getTargetError();
      }
   }

   /**
    * #Description of the Method
    *
    * @param url Description of Parameter
    * @param deployerName Description of Parameter
    * @exception Exception Description of Exception
    */
   protected void undeploy(String url, ObjectName deployerName)
          throws Exception
   {
      log.debug("undeploying url " + url + " using deployer " + deployerName);
      try
      {
         // Call the appropriate deployer through the JMX server
         server.invoke(deployerName, "undeploy", new Object[]{url},
               new String[]{"java.lang.String"});
      }
      catch (MBeanException e)
      {
         throw e.getTargetException();
      }
      catch (RuntimeErrorException e)
      {
         throw e.getTargetError();
      }
   }

   // Inner classes -------------------------------------------------

   /**
    * This class holds info about a deployement, such as the URL and the last
    * timestamp.
    */
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
         for (int i = 0; i < deployableFilters.length; ++i)
         {
            if (deployableFilters[i] != null && deployableFilters[i].accept(null, url.getFile()))
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
