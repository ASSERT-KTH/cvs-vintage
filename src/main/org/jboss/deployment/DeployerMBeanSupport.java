/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.jboss.system.URLClassLoader;
import java.util.List;

/**
* An abstract base class for deployer service implementations.
*
* @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
* @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.15 $
*
* <p><b>Revisions:</b>
*
* <p><b>20010725 Toby Allsopp (patch from David Jencks)</b>
* <ul><li>Added <code>getDeployments</code> method so that subclasses
* can find out what has been deployed.</li></ul>
* <p><b>20011219 Marc Fleury</b>
* <ul><li>Factored out inner class for deployment info</li></ul>
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
   
   /**
   *  The directory that will contain local copies of deployed packages
   */
   private File deployDir;
   
   // Static --------------------------------------------------------
   
   private static int nextNum = 0;
   
   private static synchronized String generateUniqueDirName()
   {
      int thisNum = nextNum++;
      return "deploy." + thisNum;
   }
   
   // Constructors --------------------------------------------------
   public DeployerMBeanSupport()
   {
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
   * The <code>startService</code> method sets up a temporary directory
   * to hold the copies of deployed packages and possibly inflations
   * of those copies.  The format is (jboss.system.home)/tmp/deploy/(deployername).
   *
   * @exception Exception if an error occurs
   */
   protected void startService()
   throws Exception
   {
      
      // FIXME MARCF I think we should keep the old code around (would speed up boot time)
      // Just delete on undeploy and if there is new stuff
      
      deployDir = new File(
         // from the top of the jboss home distribution
         System.getProperty("jboss.system.home"), 
         // create a local temporary deployment directory
         "tmp"+File.separator+"deploy"+File.separator+getName());
      
      if (deployDir.exists())
      {
         log.debug("Found a temp directory left over " +
                   "from a previous run; deleting it.");
                   
         // What could it mean?
         if (!recursiveDelete(deployDir))
         {
            log.warn("Unable to recursively delete temp directory '" +
                     deployDir + "' that appears to be left over from " +
                     "the previous run. This might cause problems.");
         }
      }
      if (!deployDir.exists() && !deployDir.mkdirs())
      {
         throw new DeploymentException("Can't create temp directory '" +
            deployDir + "'");
      }
      log.info("Temporary deploy directory is " + deployDir);
   }
   
   /**
   * The <code>stopService</code> method tries to remove the
   * directory created in the createService method.
   *
   */
   protected void stopService()
   {
      // Remove our temp directory
      if (!recursiveDelete(deployDir))
      {
         log.warn("Unable to recursively delete the temp directory '" +
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
      /*
      //Why doesn't this work???? Maybe in java 1.4?
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
      */
      String filename = url.getFile();
      JarFile jarFile = new JarFile(filename);
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
   
   protected void extractPackages(URL url, ServiceDeploymentInfo di)
   throws DeploymentException, IOException
   {
      if (url.getFile().endsWith(".xml"))
      {
         di.addXmlUrl(url);
         // Nothing to extract
         return;
      }
      
      //if its a zip, jar or war, add to list and stop, these are class suppliers
      if (url.getFile().endsWith(".zip") || url.getFile().endsWith(".jar") || url.getFile().endsWith(".war"))
      {
         di.addClassUrl(url);
         return;
      }
      
      //Sars may contain files or other packages, the sar itself is a class supplier
      if (url.getFile().endsWith(".sar"))  di.addClassUrl(url);
         
      
      URL jarUrl;
      // jar:<theURL>!/...
      try { jarUrl = new URL("jar:" + url.toString() + "!/"); }
         
      catch (MalformedURLException mfue) { throw new DeploymentException("Oops! Couldn't convert URL to a jar URL", mfue);}
      
      JarURLConnection jarConnection =
      (JarURLConnection)jarUrl.openConnection();
      JarFile jarFile = jarConnection.getJarFile();
      
      try
      {
         for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
         {
            JarEntry entry = (JarEntry)e.nextElement();
            String name = entry.getName();
            /*
            //jar urls don't seem to work!
            jar:file:/usr/java/jboss/co6/jboss-all/build/output/jboss-3.0.0alpha/tmp/deploy/ServiceDeployer/copydeploy.28/jmx-ejb-connector-server.sar!/META-INF/jboss-service.xml
            cannot be opened!
            if (name.endsWith(".xml"))
            {
            di.addXmlUrl(new URL(jarUrl, name));
            } // end of if ()
            
            else
            */
            if (name.endsWith(".jar")
               || name.endsWith(".xml")
               || name.endsWith(".sar")
               || name.endsWith(".ear")
               || name.endsWith(".rar")
               || name.endsWith(".war")
               || name.endsWith(".zip"))
            {
               
               File outFile = new File(getUniqueDir(di), name);
               File outFileParent = outFile.getParentFile();
               outFileParent.mkdirs();
               
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
               //stop for these,
               URL packageURL = new URL("file:" + outFile.toString());
               if (name.endsWith(".jar")
                  || name.endsWith(".zip")
                  || name.endsWith(".war"))
               {
                  di.addClassUrl(packageURL);
               } // end of if ()
               //continue for others (xml, rar, ear, sar)
               else
               {
                  extractPackages(packageURL, di);
               } // end of else
            }
         
         } // end of if ()
      
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
   
   public File getDeployDir() { return deployDir; }
   
   
   public File getLocalCopy(URL url, ServiceDeploymentInfo di)
   throws IOException
   {
      // Create a directory into which we are going to put the bytes
      File localDir = new File(deployDir, generateUniqueDirName()+ File.separator);
      
      // Create the directories
      localDir.mkdirs();
      
      // Keep track of the directory we are using
      di.addDir(localDir);
      
      // Get the name of the file (no path) create that file as "copyFile"
      File copyFile = new File(localDir, new File(url.getFile()).getName());
      
      // Copy the stuff into it
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
   
   
   private File getUniqueDir(ServiceDeploymentInfo di)
   {
   File dir = new File(deployDir, generateUniqueDirName() + File.separator);
   di.addDir(dir);
   return dir;
   }
   
   // Inner classes -------------------------------------------------

}
