/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
import javax.management.*;

import org.jboss.logging.Log;
import org.jboss.cluster.*;
import org.jboss.util.MBeanProxy;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class AutoDeployer
   implements AutoDeployerMBean, MBeanRegistration, Runnable
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "EJB:service=AutoDeployer";
    
   // Attributes ----------------------------------------------------
   MBeanServer server;
   ObjectName factoryName;
   
   boolean running = false;
   
   ArrayList watchedDirectories = new ArrayList();
   HashMap deployedURLs = new HashMap();
   ArrayList watchedURLs = new ArrayList();
   
   Log log = new Log("Auto deploy");

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public AutoDeployer(String urlList)
   {
      StringTokenizer urls = new StringTokenizer(urlList, ",");
      
      while (urls.hasMoreTokens())
      {
         String url = urls.nextToken();
         
         // Check if directory
         File urlFile = new File(url);
         if (urlFile.exists() && urlFile.isDirectory())
         {
/*            
            File[] files = url.listFiles();
            for (int i = 0; i < files.length; i++)
            {
               if (!deployedURLs.contains(files[i].toURL()))
               {
                  watchedURLs.addElement(new Deployment(files[i].toURL()));
               }
            }
*/          

            File metaFile = new File(urlFile, "META-INF/ejb-jar.xml");
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
               try
               {
                  watchedDirectories.add(urlFile.getCanonicalFile());
                  log.log("Watching "+urlFile.getCanonicalFile());
               } catch (IOException e)
               {
                  log.warning(e.toString());
               }
            }
         } else if (urlFile.exists()) // It's a file (.jar)
         {
               try
               {
                  watchedURLs.add(new Deployment(urlFile.getCanonicalFile().toURL()));
                  log.log("Auto-deploying "+urlFile.getCanonicalFile());
               } catch (Exception e)
               {
                  log.warning("Cannot auto-deploy "+urlFile);
               }
         } else // It's a real URL
         {
            try
            {
               watchedURLs.add(new Deployment(new URL(url)));
            } catch (MalformedURLException e)
            {
               // Didn't work
               log.log("Cannot auto-deploy "+url);
            }
         }
      }
   }
   
   // Public --------------------------------------------------------
   public void start()
      throws Exception
   {
      running = true;
      new Thread(this).start();
   }
   
   public void stop()
   {
      running = false;
   }

   public void deploy(String url)
      throws Exception
   {
      try
      {
         server.invoke(factoryName, "deploy",
                         new Object[] { url }, new String[] { "java.lang.String" });
      } catch (MBeanException e)
      {
         throw e.getTargetException();
      } catch (RuntimeErrorException e)
      {
         throw e.getTargetError();
      }
   }
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      this.server = server;
      factoryName = new ObjectName(ContainerFactoryMBean.OBJECT_NAME);
      
      run(); // Pre-deploy
      start();
      return new ObjectName(OBJECT_NAME);
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
   }
   
   public void postDeregister()
   {
   }
   
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
            // Check directories
            for (int i = 0; i < watchedDirectories.size(); i++)
            {
               File dir = (File)watchedDirectories.get(i);
               File[] files = dir.listFiles();
               for (int idx = 0; idx < files.length; idx++)
               {
                  URL fileUrl = files[idx].toURL();
                  if (deployedURLs.get(fileUrl) == null)
                  {
                     watchedURLs.add(new Deployment(fileUrl));
                     deployedURLs.put(fileUrl, fileUrl);
                  }
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
                  lm = new File(deployment.watch.getFile()).lastModified();
               } else
               {
                  lm = deployment.watch.openConnection().getLastModified();
               }
               
               // Check old timestamp -- always deploy if first check
               if ((deployment.lastModified == 0) || (deployment.lastModified < lm))
               {
                  log.log("Auto deploy of "+deployment.url);
                  deployment.lastModified = lm;
                  try
                  {
                     deploy(deployment.url.toString());
                  } catch (DeploymentException e)
                  {
                     log.error("Deployment failed:"+deployment.url);
                     log.exception(e.getCause());
                  }
               }
            }
         } catch (Exception e)
         {
            e.printStackTrace(System.err);
            running = false;
         }
      } while(running);
   }
   // Protected -----------------------------------------------------
   
   static class Deployment
   {
      long lastModified;
      URL url;
      URL watch;
      
      Deployment(URL url)
         throws MalformedURLException
      {
         this.url = url;
         if (url.getFile().endsWith(".jar"))
            watch = url;
         else
            watch = new URL(url, "META-INF/ejb-jar.xml");
      }
   }
}
