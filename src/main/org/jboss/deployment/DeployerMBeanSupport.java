/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * An abstract base class for deployer service implementations.
 *
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.8 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20010725 Toby Allsopp (patch from David Jencks)</b>
 * <ul><li>Added <code>getDeployments</code> method so that subclasses
 * can find out what has been deployed.</li></ul>
 */
public abstract class DeployerMBeanSupport
   extends ServiceMBeanSupport
   implements DeployerMBean
{
   // Constants -----------------------------------------------------
   private static final String SERVICE_CONTROLLER_NAME = "JBOSS-SYSTEM:spine=ServiceController";
   // Attributes --------------------------------------------------------

   private ObjectName serviceControllerName;


   private Map deployments = new HashMap();

   /*
    * log4j Category for logging
    */
   protected Logger category;

   /**
    *  The directory that will contain local copies of deployed packages
    */
   private File deployDir;
    
   // Static --------------------------------------------------------
   
   private static int nextNum = 0;

   private static String generateUniqueDirName()
   {
      int thisNum = nextNum++;
      return "deploy." + thisNum + ".";
   }

   // Constructors --------------------------------------------------
   public DeployerMBeanSupport()
   {
      category =  Logger.create(getClass());
   }

   
   // Public --------------------------------------------------------

   // DeployerMBean implementation ----------------------------------

   public void deploy (String url)
      throws MalformedURLException, IOException, DeploymentException
   {
      URL u = new URL(url);
      synchronized (deployments)
      {
         if (deployments.containsKey(u))
         {
            log.info("not deploying package because it is already deployed: " + u);
            return;
            /*Object info = deployments.get(u);
            try
            {
               undeploy(u, info);
            }
            catch (Throwable t)
            {
               log.error("undeploy failed", t);
               if (t instanceof Exception)
               {
                  if (t instanceof IOException) throw (IOException) t;
                  if (t instanceof DeploymentException)
                     throw (DeploymentException) t;
                  throw (RuntimeException) t;
               }
               throw (Error) t;
               }*/
         }
         try
         {
            Object info = deploy(u);
            deployments.put(u, info);
         }
         catch (Throwable t)
         {
            log.error("deploy failed", t);
            if (t instanceof Exception)
            {
               if (t instanceof IOException) throw (IOException) t;
               if (t instanceof DeploymentException)
                  throw (DeploymentException) t;
               throw (RuntimeException) t;
            }
            throw (Error) t;
         }
      }
   }

   public void undeploy (String url)
      throws MalformedURLException, IOException, DeploymentException
   {
      URL u = new URL(url);
      synchronized (deployments)
      {
         if (deployments.containsKey(u))
         {
            Object info = deployments.remove(u);
            undeploy(u, info);
         }
      }
   }

   public boolean isDeployed (String url)
      throws MalformedURLException, DeploymentException
   {
      URL u = new URL(url);
      synchronized (deployments)
      {
         return deployments.containsKey(u);
      }
   }
    
   // ServiceMBeanSupport overrides ---------------------------------
   
    /**
     * The <code>initService</code> method sets up a temporary directory
     * to hold the copies of deployed packages and possibly inflations 
     * of those copies.  The format is (jboss.system.home)/tmp/deploy/(deployername).
     *
     * @exception Exception if an error occurs
     */
    public void initService()
          throws Exception
   {
      // find the temp directory - referenced to jboss.system.home property
      File jbossHomeDir = new File(System.getProperty("jboss.system.home"));
      File tmpDir = new File(jbossHomeDir, "tmp"+File.separator);

      // Create our temp directory
      File deployTmpDir = new File(tmpDir, "deploy");
      deployDir = new File(deployTmpDir, getName());
      if (deployDir.exists())
      {
         category.info("Found a temp directory left over from a previous run - " +
               "deleting it.");
         // What could it mean?
         if (!recursiveDelete(deployDir))
         {
            category.warn("Unable to recursively delete temp directory '" +
                  deployDir + "' that appears to be left over from " +
                  "the previous run. This might cause problems.");
         }
      }
      if (!deployDir.exists() && !deployDir.mkdirs())
      {
         throw new DeploymentException("Can't create temp directory '" +
               deployDir + "'");
      }
      category.info("Temporary deploy directory is " + deployDir);
   }

    /**
     * The <code>destroyService</code> method tries to remove the 
     * directory created in the initService method.
     *
     */
    public void destroyService()
   {
      // Remove our temp directory
      if (!recursiveDelete(deployDir))
      {
         category.warn("Unable to recursively delete the temp directory '" +
               deployDir + "' - it should be cleaned up when the " +
               "server is next restarted.");
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------

   /**
    * Retrieves the object associated with a deployment. This
    * association is made during deployment using the object returned
    * from <code>deploy(URL)</code>. If there is no such deployment,
    * null is returned. Note that this is distinguishable from the
    * case of a deployment with an null information object only using
    * <code>isDeployed(URL)</code>.
    *
    * @param url the deployment for which information is required
    * @return an object, possibly null
    */
   protected Object getInfo(URL url)
   {
      synchronized (deployments)
      {
         return deployments.get(url);
      }
   }

   /**
    * Subclasses override to perform actual deployment.
    *
    * @param url the location to be deployed
    * @return an object, possibly null, that will be passed back to
    *         <code>undeploy</code> and can be obtained using
    *         <code>getInfo(URL)</code>
    */
   protected abstract Object deploy(URL url)
      throws IOException, DeploymentException;

   /**
    * Subclasses override to perform any actions neccessary for
    * undeployment.
    *
    * @param url the location to be undeployed
    * @param info the object that was returned by the corresponding
    *             <code>deploy</code>
    */
   protected abstract void undeploy(URL url, Object info)
      throws IOException, DeploymentException;

   /**
    * Returns the deployments that have been deployed by this
    * deployer.  The <code>Map</code> returned from this method is a
    * snapshot of the deployments at the time the method is called and
    * will not reflect any subsequent deployments or undeployments.
    *
    * @return a mapping from <code>URL</code> to
    *         <code>DeploymentInfo</code>
    */
   protected Map getDeployments()
   {
      Map ret = new HashMap();
      synchronized (deployments)
      {
         ret.putAll(deployments);
      }
      return ret;
   }


    // Below here are helper methods to deal with copying packages, 
    // unpacking packages recursively, finding things in packages,
    // and similar tasks needed for most deployment activities.
     //
    
   protected Collection recursiveFind(File dir, FileFilter filter)
   {
      Collection files = new ArrayList();
      File[] candidates = dir.listFiles();
      if (candidates == null)
      {
         return null;
      }

      for (int i = 0; i < candidates.length; ++i)
      {
         File candidate = candidates[i];
         if (candidate.isDirectory())
         {
            files.addAll(recursiveFind(candidate, filter));
         }
         else if (filter.accept(candidate))
         {
            files.add(candidate);
         }
      }

      return files;
   }

   protected void copyDirectory(File srcDir, File destDir)
          throws DeploymentException, IOException
   {
      File[] files = srcDir.listFiles();
      if (files == null)
      {
         throw new DeploymentException("Not a directory: '" +
               srcDir + "'");
      }

      destDir.mkdirs();
      for (int i = 0; i < files.length; ++i)
      {
         File file = files[i];
         File dest = new File(destDir, file.getName());
         if (file.isDirectory())
         {
            copyDirectory(file, dest);
         }
         else
         {
            copyFile(file, dest);
         }
      }
   }

   protected void copyFile(File src, File dest)
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

    /**
     * The <code>inflateJar</code> copies the jar entries
     * from the jar url jarUrl to the directory destDir.
     * It can be used on the whole jar, a directory, or 
     * a specific file in the jar.
     *
     * @param jarUrl the <code>URL</code> if the directory or entry to copy.
     * @param destDir the <code>File</code> value of the directory in which to 
     * place the inflated copies.
     * @exception DeploymentException if an error occurs
     * @exception IOException if an error occurs
     */
    protected void inflateJar(URL url, File destDir, String path)
          throws DeploymentException, IOException
   {
      URL jarUrl;
      try
      {
         jarUrl = new URL("jar:" + url.toString() + "!/");
      }
      catch (MalformedURLException mfue)
      {
         throw new DeploymentException("Oops! Couldn't convert URL to a jar URL", mfue);
      }

      JarURLConnection jarConnection =
            (JarURLConnection)jarUrl.openConnection();
      JarFile jarFile = jarConnection.getJarFile();

      try
      {
         for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
         {
            JarEntry entry = (JarEntry)e.nextElement();
            String name = entry.getName();
            if (path == null || name.startsWith(path)) 
            {
               File outFile = new File(destDir, name);
               if (!outFile.exists()) 
               {
            
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
                    
               } // end of if (outFile.exists())
            } // end of if (matches path)
             
         }
      }
      finally
      {
         jarFile.close();
      }
   }

   protected void copy(InputStream in, OutputStream out)
          throws IOException
   {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) > 0)
      {
         out.write(buffer, 0, read);
      }
   }

   protected boolean recursiveDelete(File f)
   {
      if (f.isDirectory())
      {
         File[] files = f.listFiles();
         for (int i = 0; i < files.length; ++i)
         {
            if (!recursiveDelete(files[i]))
            {
               return false;
            }
         }
      }
      return f.delete();
   }

   protected File getDeployDir()
   {
      return deployDir;
   }


   protected File getLocalCopy(URL url, String dirName)
       throws IOException
   {
      if (dirName == null ) 
      {
         dirName = generateUniqueDirName();     
      }
      String name = new File(url.getFile()).getName();//end of file name,no path
       
      File copyFile = new File(getDeployDir(), "copy" + dirName + name);
      InputStream input = url.openStream();
      try
      {
         OutputStream output = new FileOutputStream(copyFile);
         try
         {
            copy(input, output);
         }
         finally
         {
            output.close();
         }
      }
      finally
      {
         input.close();
      }
      return copyFile;
   }

   protected Collection getUrlsInDir(File unpackedDir, FileFilter filter)
       throws MalformedURLException
   {
      Collection jars = new ArrayList();

      Collection jarFiles = recursiveFind(unpackedDir, filter);
      category.debug("Adding the following URLs to classpath:");
      for (Iterator i = jarFiles.iterator(); i.hasNext(); )
      {
         File file = (File)i.next();
         URL jarUrl = file.toURL();
         jars.add(jarUrl);
         category.debug(jarUrl.toString());
      }
      return jars;
   }

    /**
     * The <code>recursiveUnpack</code> method unpacks packages
     * such as rar, ear, war and then calls itself to unpack
     * contained packages.  It returns urls to all packages added to
     * the urls collection parameter and urls to all xml files 
     * added to the xmls parameter.  It returns a File representing the 
     * jar or xml file if that is what was supplied or the directory 
     * into which the supplied url was unpacked.
     *
     * @param url an <code>URL</code> value
     * @param urls a <code>Collection</code> value
     * @param xmls a <code>Collection</code> value
     * @return a <code>File</code> value
     * @exception MalformedURLException if an error occurs
     * @exception DeploymentException if an error occurs
     * @exception IOException if an error occurs
     */
    protected File recursiveUnpack(URL url, Collection urls, Collection xmls) 
       throws MalformedURLException, DeploymentException, IOException
  {
      //TODO: get xml files out of jars that remain packed.
     if (url.getFile().endsWith(".xml")) 
     {
        xmls.add(url);
        return new File(url.getFile());          
     }
      
     if (url.getFile().endsWith(".jar") || url.getFile().endsWith(".sar")) 
     {
        urls.add(url);
        return new File(url.getFile());          
     }
      
     String unpackedDirName = generateUniqueDirName();
     File unpackedDir = new File(getDeployDir(), unpackedDirName);
     category.debug("unpacking " + url + " into  " + unpackedDir);
     inflateJar(url, unpackedDir, null);
     FileFilter jarFilter = new FileFilter() 
        {
            /**
             *  #Description of the Method
             *
             * @param  file  Description of Parameter
             * @return       Description of the Returned Value
             */
           public boolean accept(File file)
           {
              return !(file.getName().endsWith(".sar") 
                 || file.getName().endsWith(".war")
                 || file.getName().endsWith(".ear")
                 || file.getName().endsWith(".rar"));
           }
        };
     //these go directly into classpath
     urls.addAll(getUrlsInDir(unpackedDir, jarFilter));
     FileFilter xmlFilter = new FileFilter() 
        {
            /**
             *  #Description of the Method
             *
             * @param  file  Description of Parameter
             * @return       Description of the Returned Value
             */
           public boolean accept(File file)
           {
              return file.getName().endsWith(".xml");
           }
        };
     xmls.addAll(getUrlsInDir(unpackedDir, xmlFilter));
     //now find what we need to recursively deploy
     FileFilter unpackFilter = new FileFilter()
        {
            /**
             *  #Description of the Method
             *
             * @param  file  Description of Parameter
             * @return       Description of the Returned Value
             */
           public boolean accept(File file)
           {
              return file.getName().endsWith(".sar") 
                 || file.getName().endsWith(".war")
                 || file.getName().endsWith(".ear")
                 || file.getName().endsWith(".rar");
           }
        };
     Collection deployables = getUrlsInDir(unpackedDir, unpackFilter);
     Iterator subdeploy = deployables.iterator();
     while (subdeploy.hasNext()) 
     {
        recursiveUnpack((URL)subdeploy.next(), urls, xmls);         
     }
     return unpackedDir;
   }

   protected ObjectName getServiceControllerName() throws DeploymentException
   {
      if (serviceControllerName == null)
      {
         try
         {
            serviceControllerName = new ObjectName(SERVICE_CONTROLLER_NAME);
         }
         catch(MalformedObjectNameException mone)
         {
            throw new DeploymentException("Can't construct service controller object name!!" + mone);
         }
      }
      return serviceControllerName;
   }


   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
