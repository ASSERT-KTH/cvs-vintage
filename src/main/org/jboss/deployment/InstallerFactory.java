/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;
import java.util.zip.ZipEntry;import java.util.zip.ZipInputStream;import java.util.Enumeration;

import org.jboss.logging.Logger;

/** This class is used by the J2eeDeployer to create, remove or find a particular
 *  Deployment. It uses the Installer class to create a Deployment.
 *
 *	@see <related>
 *	@author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 *  @author <a href="mailto:wburke@commercetone.com">Bill Burke</a>
 *	@version $Revision: 1.12 $
 */
public class InstallerFactory
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   /** The deployment base directory (for the temporary files). */
   protected File baseDir;
   
   /** Instance logger. */
   protected Logger log = Logger.getLogger(InstallerFactory.class);
   
   // Constructors --------------------------------------------------
   
   /** Constructs a new InstallerFactory, only one is needed per J2eeDeployer
    * @param _tmpDir the temporary deployment directory
    */
   public InstallerFactory(File _tmpDir) throws IOException
   {
      baseDir = _tmpDir.getCanonicalFile();
      if (log.isDebugEnabled()) {
         log.debug("Using base directory: " + baseDir);
      }
   }
   
   // Public --------------------------------------------------------
   
   /** installs the J2ee component the URL points to and returns a Deployment object as its
    *  representation.
    *  @param _src J2ee module (ejb/war/ear) to deploy
    *  @return a Deployment object representing the deployment
    *  @throws J2eeDeploymentException if the module is not installable for some reasons
    *  (syntactical errors, ...?)
    *  @throws IOException if a file operation (_src download jar file extraction) fails
    */
   public Deployment install(URL _src) throws J2eeDeploymentException, IOException
   {
      
      return new Installer(this, _src).execute();
      
   }
   
   /** uninstalls the files represented by the given Deployment.
    *  @param _d Deployment to remove
    *  @throws IOException if file deletion fails
    */
   public void uninstall(Deployment _d) throws IOException
   {
      File appDir = new File(_d.localUrl.getFile());
      
      deleteRecursive(appDir);
   }
   
   /** Finds all Deployments currently installed.
    *  @return array of all found deployments
    */
   public Deployment[] getDeployments()
   {
      Vector found = new Vector();
      
      File[] files =  baseDir.listFiles();
      for (int i = 0, l = files.length; i<l; ++i)
      {
         File deployment = new File(files[i], J2eeDeployer.CONFIG);
         if (deployment.exists())
         {
            try
            {
               found.add(loadConfig(deployment));
            }
            catch (IOException _ioe)
            {
               log.error("exception while searching deployments", _ioe);
            }
         }
      }
      
      Deployment[] result = new Deployment[found.size()];
      Iterator it = found.iterator();
      for (int i = 0; it.hasNext(); ++i)
         result[i] = (Deployment)it.next();
      
      return result;
   }
   
   /** Finds a particular Deployment.
    *  @param _pattern wether the name of the application or the src URL of the application
    *         (the one that was given on install (URL))
    *  @return the Deployment object for this app or null if not found
    */
   public Deployment findDeployment(String _pattern)
   {
      if (_pattern == null)
         return null;
      
      Deployment result = null;
      String realPattern = null;
      // First try to see if _pattern is a URL.
      try
      {
         URL u = new URL(_pattern);
         String realtmp = u.getFile();
         File fp = new File(realtmp);
         realPattern = fp.getName();
      }
      catch (MalformedURLException ex)
      {
            /* Ignore, this is ok. We're dealing with an actual file path */
      }
      
      if (realPattern == null)
      {
         // If it's not a URL maybe it is a file path
         try
         {
            File fp = new File(_pattern);
            realPattern = fp.getName();
         }
         catch (Exception ex)
         {
         }
      }
      if (realPattern == null)
      {
         // REVISIT: Maybe we should log something here?
         return result;
      }
      
      File[] files =  baseDir.listFiles();
      for (int i = 0, l = files.length; i<l; ++i)
      {
         if (realPattern.equals(files[i].getName()))
         {
            try
            {
               result = loadConfig(new File(files[i], J2eeDeployer.CONFIG));
               break;
            }
            catch (FileNotFoundException e)
            {
               // Ignore as the config may have been removed
            }
            catch (IOException _ioe)
            {
               log.error("exception while searching deployment", _ioe);
            }
         }
      }
      
      return result;
   }
   
   /** Does some cleanup in the deployments. Intended to remove files that didnt become removed
    *  in previous sessions because of the Win2k removal problems.
    *  @throws IOException since file deletions can fail
    */
   public void unclutter() throws IOException
   {
      File[] files =  baseDir.listFiles();
      for (int i = 0, l = files.length; i<l; ++i)
      {
         File dep = new File(files[i], J2eeDeployer.CONFIG);
         if (dep.exists())
         {
            // is a deployment... clean up its directory
            try
            {
               Deployment d = loadConfig(dep);
               Collection needed = d.getAllFiles();
               File[] parts = files[i].listFiles();
               for (int j = 0; j < parts.length; ++j)
               {
                  if (!needed.contains(parts[j].getName()))
                  {
                     // not needed -> delete
                     deleteRecursive(parts[j]);
                  }
               }
            }
            catch (IOException _ioe)
            {
               log.error("exception while uncluttering deployment "+files[i], _ioe);
            }
         }
         else
         {
            // is something we dont know -> we dont care about it...
            deleteRecursive(files[i]);
         }
      }
   }
   
   
   // Private -------------------------------------------------------
   
   /** Deserializes the Deployments pointed to by the given File.
    *  @param _file a serialized Deployment
    *  @return the deserialized Deployment
    *  @throws IOException when something went wrong
    */
   private Deployment loadConfig(File _file) throws IOException
   {
      Deployment d = null;
      try
      {
         ObjectInputStream in = new ObjectInputStream(new FileInputStream(_file));
         d = (Deployment)in.readObject();
         in.close();
      }
      catch (ClassNotFoundException _snfe)
      {} // should never happen...
      
      return d;
   }
   
   /** Truncates the the name (the last cluster of letters after the last slash.
    *  @param _url an URL or something like that
    */
   private String getName(String _url)
   {
      String result = _url;
      
      if (result.endsWith("/"))
         result = result.substring(0, result.length() - 1);
      
      result = result.substring(result.lastIndexOf("/") + 1);
      
      return result;
   }
   
   /** deletes the given File recursive.
    *  @param _file to delete
    *  @throws IOException when something goes wrong
    */
   private void deleteRecursive(File _file) throws IOException
   {
      if (_file.exists())
      {
         if (_file.isDirectory())
         {
            File[] files = _file.listFiles();
            for (int i = 0, l = files.length; i < l; ++i)
               deleteRecursive(files[i]);
         }
         _file.delete();
      }
   }
   
}
