/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.Notification;

import org.jboss.configuration.ConfigurationServiceMBean;
import org.jboss.deployment.DeployerMBeanSupport;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Log;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.util.MBeanProxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *   Service that deploys ".rar" files containing resource
 *   adapters. Deploying the RAR file is the first step in making the
 *   resource adapter available to application components; once it is
 *   deployed, one or more connection factories must be configured and
 *   bound into JNDI, a task performed by the
 *   <code>ConnectionFactoryLoader</code> service.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 *
 *   @see org.jboss.resource.ConnectionFactoryLoader
 */
public class RARDeployer
   extends DeployerMBeanSupport
   implements RARDeployerMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   /** The directory that will contain local copies of deployed RARs */
   private File rarTmpDir;

   /** The next sequence number to be used in notifications about
       (un)deployment */
   private int nextMessageNum = 0;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // RARDeployerMBean implementation -------------------------------

   // DeployerMBeanSupport overrides ---------------------------------

   public String getName() { return "RARDeployer"; }

   public void initService() throws Exception
   {
      // find the temp directory - it contains the file
      // "tmp.properties"
      URL tmpPropURL = getClass().getResource("/tmp.properties");
      File tmpDir = new File(tmpPropURL.getFile()).getParentFile();

      // Create our temp directory
      File deployTmpDir = new File(tmpDir, "deploy");
      rarTmpDir = new File(deployTmpDir, getName());
      if (rarTmpDir.exists())
      {
         log.log("Found a temp directory left over from a previous run - " +
                 "deleting it.");
         // What could it mean?
         if (!recursiveDelete(rarTmpDir))
         {
            log.warning("Unable to recursively delete temp directory '" +
                        rarTmpDir + "' that appears to be left over from " +
                        "the previous run. This might cause problems.");
         }
      }
      if (!rarTmpDir.exists() && !rarTmpDir.mkdir())
      {
         throw new DeploymentException("Can't create temp directory '" +
                                       rarTmpDir + "'");
      }
   }

   public void destroyService()
   {
      // Remove our temp directory
      if (!recursiveDelete(rarTmpDir))
      {
         log.warning("Unable to recursively delete the temp directory '" + 
                     rarTmpDir + "' - it should be cleaned up when the " +
                     "server is next restarted.");
      }
   }

   protected Object deploy(URL url) throws IOException, DeploymentException
   {
      log.log("Attempting to deploy RAR at '" + url + "'");

      // We want to take a local copy of the RAR so that we don't run
      // into problems if the original is removed/replaced. We also
      // need the RAR in unpacked form so that we can get at the
      // included JARs for classloading (I don't think URLClassLoader
      // deals with JARs within JARs).

      File unpackedDir = new File(rarTmpDir, generateUniqueDirName(url));
      if (unpackedDir.exists())
      {
         throw new DeploymentException("The application at URL '" + url + "' " +
                                       "appears to already have been " +
                                       "deployed because the directory '" +
                                       unpackedDir + "' exists");
      }
      unpackedDir.mkdirs();

      if (url.getFile().endsWith("/"))
      {
         // this is a directory - we can only deal with directories in
         // the local filesystem (because we can't get a list of files
         // from a general URL)
         if (!url.getProtocol().equals("file"))
            throw new DeploymentException("Can only deploy directories " +
                                          "specified by 'file:' URLs");
         copyDirectory(new File(url.getFile()), unpackedDir);
      }
      else
      {
         // this is a .rar file somewhere
         inflateJar(url, unpackedDir);
      }

      // Right, now we can forget about URLs and just use the file
      // system.

      File ddFile = new File(unpackedDir, "META-INF/ra.xml");
      
      if (!ddFile.exists())
      {
         throw new DeploymentException("No deployment descriptor " +
                                       "('META-INF/ra.xml') found in alleged " +
                                       "resource adapter at '" + url + "'");
      }

      Document dd;
      try
      {
         dd = XmlFileLoader.getDocument(ddFile.toURL());
      }
      catch (org.jboss.ejb.DeploymentException de)
      {
         throw new DeploymentException(de.getMessage(), de.getCause());
      }

      Element root = dd.getDocumentElement();

      RARMetaData metadata = new RARMetaData();
      Log.setLog(log);
      try
      {
         metadata.importXml(root);
      }
      finally
      {
         Log.unsetLog();
      }

      // Create a class loader that can load classes from any JARs
      // inside the RAR

      // First, we need to find the JARs. The procedure for this
      // depends on whether the URL points to a RAR file or a
      // directory.

      Collection jars = new ArrayList();

      FileFilter filter = new FileFilter()
         {
            public boolean accept(File file)
            {
               return file.getName().endsWith(".jar");
            }
         };
      Collection jarFiles = recursiveFind(unpackedDir, filter);
      for (Iterator i = jarFiles.iterator(); i.hasNext(); )
      {
         File file = (File) i.next();
         jars.add(file.toURL());
      }

      log.debug("Adding the following URLs to classpath:");
      for (Iterator i = jars.iterator(); i.hasNext(); )
         log.debug(((URL) i.next()).toString());

      // Ok, now we have the URLs of the JARs contained in the RAR we
      // can create a classloader that loads classes from them

      ClassLoader cl = new URLClassLoader(
         (URL[]) jars.toArray(new URL[0]),
         Thread.currentThread().getContextClassLoader());

      metadata.setClassLoader(cl);

      // Look for a META-INF/ra-jboss.xml file that defines connection
      // factories. My current idea is that this can define connection
      // factory loaders in exactly the same way as jboss.jcml.

      /* not quite implemented yet
      File jbossDDFile = new File(unpackedDir, "META-INF/ra-jboss.xml");

      if (jbossDDFile.exists())
      {
         log.log("Loading JBoss deployment descriptor at '" + jbossDDFile +
                 "'");
         try
         {
            Document jbossDD = XmlFileLoader.getDocument(jbossDDFile.toURL());
            ConfigurationServiceMBean cs = (ConfigurationServiceMBean)
               MBeanProxy.create(ConfigurationServiceMBean.class,
                                 ConfigurationServiceMBean.OBJECT_NAME);
            cs.load(jbossDD);
            //FIXME need to get reference to newly created MBean so we
            //can (a) call init and start on it and (b) destroy it
            //when this RAR is undeployed
         }
         catch (Exception e)
         {
            log.warning("Problem occurred while loading '" + jbossDDFile + "'");
            log.exception(e);
         }
      }
      */

      // Let's tell the waiting hordes (of connection factory loaders)
      // that this resource adapter is available

      Notification notification = new Notification(
         ConnectionFactoryLoaderMBean.DEPLOYMENT_NOTIFICATION +
         ConnectionFactoryLoaderMBean.DEPLOY_NOTIFICATION, this,
         nextMessageNum++, metadata.getDisplayName());
      notification.setUserData(metadata);
      sendNotification(notification);

      DeploymentInfo info = new DeploymentInfo();
      info.metadata = metadata;
      info.unpackedDir = unpackedDir;
      return info;
   }

   protected void undeploy(URL url, Object o) throws DeploymentException
   {
      log.log("Undeploying RAR at '" + url + "'");
      
      DeploymentInfo info = (DeploymentInfo) o;

      if (info == null)
      {
         throw new DeploymentException("There doesn't appear to be a RAR " +
                                       "deployed at '" + url + "'");
      }

      // Tell the waiting hordes (of connection factory loaders)
      // that this resource adapter is no longer available
      
      RARMetaData metadata = info.metadata;
      Notification notification = new Notification(
         ConnectionFactoryLoaderMBean.DEPLOYMENT_NOTIFICATION +
         ConnectionFactoryLoaderMBean.UNDEPLOY_NOTIFICATION, this,
         nextMessageNum++, metadata.getDisplayName());
      sendNotification(notification);

      // Remove the temporary copy

      File unpackedDir = info.unpackedDir;
      if (!recursiveDelete(unpackedDir))
      {
         log.warning("Unable to recursively delete temp directory '" +
                     unpackedDir + "' - this should be cleaned up either " +
                     "when the server is shut down or when it restarts.");
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   private static int nextNum = 0;

   private static String generateUniqueDirName(URL u)
   {
      int thisNum = nextNum++;
      return "rar." + thisNum;
   }

   private Collection recursiveFind(File dir, FileFilter filter)
   {
      Collection files = new ArrayList();
      File[] candidates = dir.listFiles();
      if (candidates == null) return null;

      for (int i = 0; i < candidates.length; ++i)
      {
         File candidate = candidates[i];
         if (candidate.isDirectory())
            files.addAll(recursiveFind(candidate, filter));
         else if (filter.accept(candidate))
            files.add(candidate);
      }

      return files;
   }

   private void copyDirectory(File srcDir, File destDir)
      throws DeploymentException, IOException
   {
      File[] files = srcDir.listFiles();
      if (files == null) throw new DeploymentException("Not a directory: '" +
                                                       srcDir + "'");

      destDir.mkdirs();
      for (int i = 0; i < files.length; ++i)
      {
         File file = files[i];
         File dest = new File(destDir, file.getName());
         if (file.isDirectory())
            copyDirectory(file, dest);
         else
            copyFile(file, dest);
      }
   }

   private void copyFile(File src, File dest)
      throws IOException
   {
      InputStream in = new FileInputStream(src);
      try
      {
         OutputStream out = new FileOutputStream(dest);
         try
         {
            copy(in, out);
         }
         finally
         {
            out.close();
         }
      }
      finally
      {
         in.close();
      }
   }

   private void inflateJar(URL url, File destDir)
      throws DeploymentException, IOException
   {
      URL jarUrl;
      try
      {
         jarUrl = new URL("jar:" + url.toString() + "!/");
      }
      catch (MalformedURLException mfue)
      {
         throw new DeploymentException("Oops! Couldn't convert URL to a " +
                                       "jar URL", mfue);
      }
         
      JarURLConnection jarConnection =
         (JarURLConnection) jarUrl.openConnection();
      JarFile jarFile = jarConnection.getJarFile();

      for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
      {
         JarEntry entry = (JarEntry) e.nextElement();
         String name = entry.getName();
         File outFile = new File(destDir, name);
         if (entry.isDirectory())
         {
            outFile.mkdirs();
         }
         else
         {
            InputStream in = jarFile.getInputStream(entry);
            try
            {
               OutputStream out = new FileOutputStream(outFile);
               try
               {
                  copy(in, out);
               }
               finally
               {
                  out.close();
               }
            }
            finally
            {
               in.close();

            }
         }
      }
      
      jarFile.close();
   }

   private void copy(InputStream in, OutputStream out) throws IOException
   {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) > 0)
      {
         out.write(buffer, 0, read);
      }
   }

   private boolean recursiveDelete(File f)
   {
      if (f.isDirectory())
      {
         File[] files = f.listFiles();
         for (int i=0; i<files.length; ++i)
         {
            if (!recursiveDelete(files[i])) return false;
         }
      }
      return f.delete();
   }

   // Inner classes -------------------------------------------------

   private static class DeploymentInfo
   {
      public RARMetaData metadata;
      public File unpackedDir;
   }
}
