/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.io.File;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.ServerConfigMBean;

import org.jboss.util.jmx.MBeanProxy;

/**
 * MainDeployer
 *
 * Takes a series of URL to watch, detects changes and calls the appropriate Deployers 
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 1.20 $
 */
public class MainDeployer
   extends ServiceMBeanSupport
   implements MainDeployerMBean, Runnable
{
   /** Deployers **/
   private final Set deployers = new HashSet();
   
   /** Scanned Directories **/
   private final ArrayList directories = new ArrayList();
   
   /** I always feel like somebody is watching me, contains DeploymentInfo **/
   private final Map deployments = new HashMap();
   private final ArrayList deploymentsList = new ArrayList();

   /** Thread running **/
   private boolean running = false;
   
   /** period of scanning **/
   private int period = 5000;
   
   /** an increment for tmp files **/
   private int id = 0;
   
   /** Given a flat set of files, this is the order of deployment **/
   private String[] order = { "sar", "service.xml", "rar", "jar", "war", "ear", "zip" };

   /** The temporary directory for deployments. */
   private File tempDir;

   /** The string naming the tempDir **/
   private String tempDirString;

   /** The system home directory (for dealing with relative file names). */
   private File homeDir;

   public void setPeriod(int period) {
      this.period = period; 
   }

   public int getPeriod() 
   {
      return period;
   }
   
   
   /** 
    * Directory get set logic, these are "scanning" directories
    * on the local filesystem
    */
   public void setDirectories(String urlList) throws MalformedURLException
   {
      StringTokenizer urls = new StringTokenizer(urlList, ",");
      
      // Add URLs to list
      while (urls.hasMoreTokens())
      {
         addDirectory(urls.nextToken().trim()) ;
     } 
   }
   
   public void addDirectory(String url) throws MalformedURLException
   {
      // We are dealing with a relative path URL 
      if (!( url.startsWith("file:") || url.startsWith("http:"))) {
         addDirectory(new URL(homeDir.toURL(), url));
      }
      else {
         addDirectory(new URL(url));
      }
   }

   public void addDirectory(URL url) {
      if (!directories.contains(url)) {
         directories.add(url);
         
         if (log.isDebugEnabled()) {
            log.debug("Added directory scan "+url);
         }
      }
   }
   
   public void removeDirectory(String url) throws MalformedURLException
   {
      // We are dealing with a relative path URL 
      if (!(url.startsWith("file:") || url.startsWith("http:"))) {
         removeDirectory(new URL(homeDir.toURL(), url));
      }
      else {
         removeDirectory(new URL(url));
      }
   }

   public void removeDirectory(URL url)
   {
      int index = directories.lastIndexOf(url);
      if (index != -1) {
         directories.remove(index); 
         
         if (log.isDebugEnabled()) {
            log.debug("Removed directory scan " + url);
         }
      }
   }

   public Collection listDeployed()
   {
      synchronized (deploymentsList)
      {
         return new ArrayList(deploymentsList);
      }
   }
   
   public void addDeployer(DeployerMBean deployer) 
   {
      log.info("adding deployer: " + deployer);
      deployers.add(deployer); 
   }
   
   public void removeDeployer(DeployerMBean deployer) 
   {
      deployers.remove(deployer); 
   }
   
   
   // ServiceMBeanSupport overrides ---------------------------------
   
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
      return name == null ? OBJECT_NAME : name;
   }

   protected void createService()
      throws Exception
   {
      // watch the deploy directory, it is a set so multiple adds 
      // (start/stop) only one entry is present
      // get the temporary directory to use
      tempDir = new File((File)server.getAttribute(ServerConfigMBean.OBJECT_NAME, "TempDir"),
                         "deploy");

      //used in isWatched
      tempDirString = tempDir.toURL().toString(); 

      // get the system home directory
      homeDir = (File)server.getAttribute(ServerConfigMBean.OBJECT_NAME, "HomeDir");

      //Watch in our standard directory.  This should be derived from configuration info.      
      addDirectory("deploy");
   }
   
   /**
    * Get the local state data directory from the server configuration.
    */
   protected void startService()
      throws Exception
   {
      //just so we can time startup...
      scan();

      // Start auto deploy thread
      running = true;
      
      // Kick off the thread
      new Thread(this, "MainDeployer").start();
   }
   
   protected void stopService()
   {
      // Stop auto deploy thread
      running = false;
   }
   
   public boolean getScan() 
   { 
      return running; 
   }

   public void setScan(boolean scan) 
   {
      running = scan; 
   }
   
   /**
    * Main processing method for the MainDeployer object
    */
   public void run()
   {
      do
      {   
         // Sleep
         try
         {
            Thread.sleep(period);
         }
         catch (Exception e)
         {
            log.debug("interrupted");
         }
         scan();
      } 
      while (running);
   }
   
   public void scan() 
   {   
      // Scan directories for new deployments 
      for (Iterator newDeployments = scanNew().listIterator(); newDeployments.hasNext();)
      {
         URL deployable = (URL) newDeployments.next();
         try 
         {
            deploy(deployable);     
         }
         catch (Throwable t)
         {
            log.error("Could not deploy: " + deployable, t); 
         } // end of try-catch
         
      }

      // Undeploy the removed ones
      for (Iterator removed = scanRemoved().listIterator(); removed.hasNext();)
      {
         DeploymentInfo di = (DeploymentInfo) removed.next();
         // if the url is a file that doesn't exist, it was removed -> undeploy
         undeploy(di);
      }  

      // Undeploy and redeploy the modified ones
      for (Iterator modified = scanModified().listIterator(); modified.hasNext();)
      {
         DeploymentInfo di = (DeploymentInfo) modified.next();
         undeploy(di);
         try 
         {
            deploy(di);     
         }
         catch (Throwable t)
         {
            log.error("Could not deploy: " + di.url, t); 
         } // end of try-catch
      }
   }

   public void undeploy(String url)
   {
      try 
      {
         DeploymentInfo sdi = getDeployment(new URL(url));
         
         if (sdi!= null)
         {
            undeploy(sdi);
         }
      }  
      catch (Exception e)
      {
         log.error("Couldn't undeploy url " + url, e);
      } 
   }
   
   
   public void undeploy(DeploymentInfo di)
   {
      log.info("Undeploying "+di.url);

      // First remove all sub-deployments
      for (Iterator subs = di.subDeployments.iterator(); subs.hasNext();)
      {
         DeploymentInfo sub = (DeploymentInfo) subs.next();
         
         if (log.isDebugEnabled())
         {
            log.debug("UNDEPLOYMENT OF SUB "+sub.url);
         }
         undeploy(sub);      
      }

      // Then remove the deployment itself
      try 
      { 
         // Tell the respective deployer to undeploy this one
         if (di.deployer != null)
         {
            di.deployer.undeploy(di); 
         }
      }
      catch (Exception e)
      {
         log.error("Undeployment failed: " + di.url, e); 
      }
      catch (Throwable t)
      {
         log.error("Undeployment failed: " + di.url, t); 
      }
      try
      {
         // remove from local maps
         synchronized (deploymentsList)
         {
            deployments.remove(di.url);
            if (deploymentsList.lastIndexOf(di) != -1)
            {
               deploymentsList.remove(deploymentsList.lastIndexOf(di));
            }
         }
         // Nuke my stuff, this includes the class loader
         di.cleanup(log);
         
         log.info("Undeployed "+di.url);
      
      }
      catch (Exception e)
      {
         log.error("Undeployment cleanup failed: " + di.url, e); 
      }
      catch (Throwable t)
      {
         log.error("Undeployment cleanup failed: " + di.url, t); 
      }
   }
   
   public void deploy(String url)
   {
      // Just format it correctly 
      try 
      {
         // if no protocol, assume file based and prepend protocol
         if (! url.startsWith("http") && ! url.startsWith("file"))
         {
            deploy(new URL("file:"+url));
         }
         else
         {
            deploy(new URL(url));
         }
      }
      catch (Exception e)
      {
         log.error("Problem with URL "+url, e);
      }
   }

   public void deploy(URL url)
   {
      DeploymentInfo sdi = getDeployment(url);
      try 
      {
         // if it does not exist create a new deployment
         if (sdi == null)
         {
            sdi = new DeploymentInfo(url, null);
            deploy(sdi);
         }
      }
      catch (DeploymentException e)
      {
         log.error("Couldn't deploy URL "+url, e);
      }
   }

   public void deploy(DeploymentInfo deployment) 
      throws DeploymentException
   {      
      boolean debug = log.isDebugEnabled();

      try
      {
         // If we are already deployed return
         if (isDeployed(deployment.url))
         {
            return;
         }

         if (log.isInfoEnabled());
           log.info("Deploying: " + deployment.url.toString());

         // Create a local copy of that File, the sdi keeps track of the copy directory
         makeLocalCopy(deployment);
         
         // initialize the unified classloaders for this deployment
         deployment.createClassLoaders();
         
         // What deployer is able to deploy this file
         findDeployer(deployment);
         
         if(deployment.deployer != null)
         {
            deployment.deployer.init(deployment); 
         }

         // create subdeployments as needed
         deploySubPackages(deployment);
         
         // Deploy this SDI, if it is a deployable type
         if (deployment.deployer != null)
         {
            deployment.deployer.deploy(deployment);
         }

         deployment.status="Deployed";

         if (debug)
            log.debug("Done deploying " + deployment.shortName);
      }  
      catch (Throwable t) 
      { 
         log.error("could not deploy :" + deployment.url, t);
         deployment.status = "Deployment FAILED reason: " + t.getMessage();         
         throw new DeploymentException("Could not deploy: " + deployment.url, t);
      }
      finally 
      {
         // whether you do it or not, for the autodeployer
         deployment.lastDeployed = System.currentTimeMillis();
         
         synchronized (deploymentsList)
         {
            //watch it, it will be picked up as modified below, deployments is a map duplicates are ok
            deployments.put(deployment.url, deployment);
            
            // Do we watch it? Watch only urls outside our copy directory.
            if (!deployment.url.toString().startsWith(tempDirString)) 
            {
               deploymentsList.add(deployment);
               if (debug)
               {
                  log.debug("Watching new file: " + deployment.url);  
               }
            }
         }
      }
   }


   public void findDeployer(DeploymentInfo sdi) 
   {
      boolean debug = log.isDebugEnabled();

      // Defensive
      sdi.deployer = null;
      
      //
      // To deploy directories of beans one should just name the directory
      // mybean.ear/bla...bla, so that the directory gets picked up by the right deployer
      //
      for (Iterator iterator = deployers.iterator(); iterator.hasNext(); )
      {
         DeployerMBean deployer = (DeployerMBean) iterator.next();
         if (deployer.accepts(sdi))
         {
            sdi.deployer = deployer;
            if (debug)
            {
               log.debug("using deployer "+deployer);
            }
            return;
         }
      }
      if (debug)
      {
         log.debug("NO DEPLOYER for url "+sdi.url);
      }
   }

   
   /**
    * ScanNew scans the directories that are given to it and returns a 
    * Set with the new deployments
    */
   protected ArrayList scanNew()
   {
      try 
      {
         HashSet newDeployments = new HashSet();
         boolean trace = log.isTraceEnabled();
         
         // Scan directories
         for (Iterator iterator = directories.listIterator(); iterator.hasNext();) 
         {
            File dir = new File(((URL) iterator.next()).getFile());
            if (trace)
            {
               log.trace("Scanning directory: " + dir);
            }
            File[] files = dir.listFiles();
            if (files == null)
            {
               log.error("we have a problem null files in directory; should not happen");
            }

            for (int i = 0; i < files.length; i++)
            {
               if( trace )
               {
                  log.trace("Checking deployment file: "+files[i]);
               }
               // It is a new file
               if (!isDeployed(files[i].toURL())) 
               {
                  newDeployments.add(files[i].toURL());
               }
            }   
         }
         
         return sortURLs(newDeployments);
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
         return null;
      }
   }
   
   /**
    * scans the existing deployments and return a 
    * Set with the removed deployments
    */
   protected List scanRemoved()
   {
      HashSet removed = new HashSet();
         
      if (log.isTraceEnabled())
      {
         log.trace("Scanning installed deployments");
      }
      // People already deployed, scan for modifications  
      synchronized (deploymentsList)
      {
         for (Iterator it = deploymentsList.listIterator(); it.hasNext(); )
         {
            DeploymentInfo deployment = (DeploymentInfo) it.next();
            if (deployment.url.getProtocol().startsWith("file")) 
            {
               File theFile = new File(deployment.url.getFile());
               if (!theFile.exists())
               {
                  removed.add(deployment);
               }
               
            } // end of if ()
            else
            {
               try 
               {
                  deployment.url.openConnection();
               }
               catch (java.io.IOException ioe)
               {
                  removed.add(deployment);
               } // end of try-catch
            }
         }
      }
      return sortDeployments(removed);
   }
   
   /**
    * scanModified scans the existing deployments and return a 
    * Set with the modified deployments
    */
   protected ArrayList scanModified()
   {
      HashSet modified = new HashSet();
         
      if (log.isTraceEnabled())
      {
         log.trace("Scanning installed deployments");
      }
      // People already deployed, scan for modifications  
      synchronized (deploymentsList)
      {
         for (Iterator it = deploymentsList.listIterator(); it.hasNext(); )
         {
            DeploymentInfo deployment = (DeploymentInfo) it.next();
            
            long lastModified = 0;
            
            // if noone told us what to watch, we'll look for something...
            if (deployment.watch == null)
            {
               deployment.watch = deployment.url;
            }
            // Get lastModified of file from file system
            if (deployment.watch.getProtocol().startsWith("file"))
            {
               File theFile = new File(deployment.watch.getFile());
               if (theFile.exists())
               {
                  lastModified = theFile.lastModified();
               }
            }
            
            // Use URL connection to get lastModified on http
            else 
            {
               try 
               {
                  lastModified = deployment.watch.openConnection().getLastModified();
               }
               catch (java.io.IOException ioe)
               {
                  //ignored, watch is missing.
               } // end of try-catch
            }
               
            // Check the record in the DeploymentInfo against the physical one 
            if (deployment.lastDeployed < lastModified) 
            {
               modified.add(deployment);
            }
         }
      }
      return sortDeployments(modified);
   }
   

   /**
    * extractPackages 
    * 
    * TODO marcf: support directories as well right now depends on the jar format
    *
    * In case of identifiable sub-deployment we recursively call the deploy method 
    * on the deployer
    */
   protected void deploySubPackages(DeploymentInfo di)
      throws DeploymentException
   {
      // If XML only no subdeployment to speak of. We also do not
      // break a war into subdeployments as this opens the door to
      // servlet 2.3 classloading stuff we don't want to deal with here
      // FIXME do the sub deploy for directory and the move to 
      if (di.isXML || di.isDirectory || di.shortName.endsWith(".war") )
      {
         return ;
      }
      // J2EE legacy goo in manifest
      parseManifestLibraries(di);
      
      JarFile jarFile =null;
      
      // Then the packages inside the package being deployed
      HashSet subDeployments = new HashSet();
      
      // marcf FIXME FIXME FIXME add support for directories not just jar files
      
      // Do we have a jar file jar:<theURL>!/..
      try
      {
         URL jarURL = new URL("jar:"+di.localUrl.toString()+"!/");
         JarURLConnection jarConn = (JarURLConnection) jarURL.openConnection();
         jarFile = jarConn.getJarFile();
      }
      catch (Exception e)
      {

         //maybe this is not a jar nor a directory...
         log.info("deploying non-jar/xml file: " + di.url);
         return;
      }

      for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
      {
         JarEntry entry = (JarEntry)e.nextElement();
         String name = entry.getName();
         
         // Everything that is not 
         // a- an XML file
         // b- a class in a normal directory structure
         // is a "package" and will be deployed
         if (name.endsWith(".jar")
            || name.endsWith(".sar")
            || name.endsWith(".ear")
            || name.endsWith(".rar")
            || name.endsWith(".war")
            || name.endsWith(".zip"))
         {
            // Make sure the name is flat no directory structure in subs name
            // example war's WEBINF/lib/myjar.jar appears as myjar.jar in 
            // the tmp directory
            if (name.lastIndexOf("/") != -1)  
            {
               name = name.substring(name.lastIndexOf("/")+1);
            }
            
            try 
            {
               // We use the name of the entry as the name of the file under deploy 
               File outFile = new File(tempDir, getNextID() + "." + name);
               
               // Copy in and out 
               OutputStream out = new FileOutputStream(outFile); 
               InputStream in = jarFile.getInputStream(entry);
               
               try {
                  copy(in, out);
               }
               finally {
                  out.close(); 
               }

               // It is a sub-deployment
               URL subURL = outFile.toURL();
               DeploymentInfo sub = new DeploymentInfo(subURL, di);
               
               // And deploy it, this call is recursive
               subDeployments.add(sub);
            }
            catch (Exception ex) 
            { 
               log.error("Error in subDeployment with name "+name, ex);
               throw new DeploymentException
                  ("Could not deploy sub deployment "+name+" of deployment "+di.url, ex);
            }
         }
      
         // WARNING: Do not close the jarFile let it hang until undeployment 
         // The reason is that if you close the jarFile you cannot open streams 
         // to files inside. The bug can be seen as follow 
         // Thread.currentThread().getContextClassLoader().getResource("a text file in jar").openStream())
         // Doesn't work while
         // Thread.currentThread().getContextClassLoader().loadClass("a class in the jar")
         // works
         // We should encapsulate "opening and closing of the jarFile" in the DeploymentInfo
         // Here we let it be open and cached
      }
      
      // Order the deployments
      for (Iterator lt = sortDeployments(subDeployments).listIterator(); lt.hasNext();) 
      { 
         try
         { 
            deploy((DeploymentInfo) lt.next());
         }
         catch (DeploymentException e)
         {
            di.subDeployments.remove(di);
         }
      }
   }
   
   public void parseManifestLibraries(DeploymentInfo sdi) throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();

      String classPath = null;
      
      Manifest mf = sdi.getManifest();
      
      if( mf != null )
      {
         Attributes mainAttributes = mf.getMainAttributes();
         classPath = mainAttributes.getValue(Attributes.Name.CLASS_PATH);
      }
      
      URL[] libs = {};
      if (classPath != null)
      {
         ArrayList tmp = new ArrayList();
         StringTokenizer st = new StringTokenizer(classPath);
         if (debug)
         {
            log.debug("resolveLibraries: "+classPath);
         }

         while (st.hasMoreTokens())
         {
            URL lib = null;
            
            String tk = st.nextToken();
            
            DeploymentInfo sub = null;

            if (debug)
            {
               log.debug("new manifest entry for sdi at "+sdi.shortName+" entry is "+tk);
            }

            try
            {   
               lib = new URL(sdi.url, tk);
               
               if (!isDeployed(lib))
               {
                  // Try having it as a full subdeployment
                  sub = new DeploymentInfo(lib, sdi);
                  
                  deploy(sub);
               }
            }
            catch (Exception ignore)
            { 
               log.warn("The manifest entry in "+sdi.url+" references URL "+lib+ 
                  " which could not be opened, entry ignored");
            } 
         }
      }
   }

   /** 
    * Downloads the jar file or directory the src URL points to.
    * In case of directory it becomes packed to a jar file.
    *
    * @return a File object representing the downloaded module
    * @throws IOException
    */
   public void makeLocalCopy(DeploymentInfo sdi) 
      throws DeploymentException
   {
      try 
      {   
         if (sdi.url.getProtocol().equals("file") && sdi.isDirectory)
         {
            // FIXME TODO add support for Directory copying over
            
            sdi.localUrl = sdi.url;
            
            return;
            // FIXME TODO add support for Directory copying over
         }
         
         // Are we already in the localCopyDir?
         else if (sdi.url.toString().indexOf(tempDir.toString()) != -1)
         {
            sdi.localUrl = sdi.url;
            return;
         }
         else
         {
            sdi.localUrl =  new File(tempDir, getNextID() + "." + sdi.shortName).toURL();
            copy(sdi.url, sdi.localUrl);
         }
      }
      catch (Exception e)
      {
         log.error("Could not make local copy for "+sdi.url.toString(), e);
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
   
   protected void copy (URL _src, URL _dest) throws IOException
   {
      if (!_dest.getProtocol().equals("file"))
         throw new IllegalArgumentException
            ("only file: protocol is allowed as destination!");
      
      InputStream in;
      OutputStream out;
      
      String s = _dest.getFile();
      File dir = new File(s.substring (0, s.lastIndexOf("/")));
      if (!dir.exists()) {
         dir.mkdirs();
      }
      
      in = _src.openStream();
      out = new FileOutputStream(s); 
      
      byte[] buffer = new byte[1024];
      
      int read;
      while (true)
      {
         read = in.read(buffer);
         if (read == -1)
            break;
         
         out.write(buffer, 0, read);
      }
      
      out.flush();
      
      out.close();
      in.close();
   }
   
   public ArrayList sortURLs(Set urls)
   {
      ArrayList list = new ArrayList(urls.size());
      
      for (int i = 0 ; i < order.length ; i++)
      {
         for (Iterator it = urls.iterator(); it.hasNext();) 
         {
            URL url = (URL) it.next();
            
            if (url.toString().endsWith(order[i]))
            {      
               list.add(url); it.remove();   
            }         
         }
      }
      
      // Unknown types deployed at the end
      list.addAll(urls);
      
      return list;
   }
   
   public ArrayList sortDeployments(Set urls)
   {
      ArrayList list = new ArrayList(urls.size());
      
      for (int i = 0 ; i < order.length ; i++)
      {
         for (Iterator it = urls.iterator(); it.hasNext();) 
         {
            DeploymentInfo di = (DeploymentInfo) it.next();
            
            if (di.url.toString().endsWith(order[i]))
            {      
               list.add(di); it.remove();
            }         
         }
      }
      
      // Unknown types deployed at the end
      list.addAll(urls);
      if (log.isTraceEnabled()) 
      {
         log.trace("about to do something with: " + list);
      } // end of if ()
      return list;
   }
   
   public boolean isDeployed(String url) 
      throws MalformedURLException
   {
      return isDeployed(new URL(url));
   }

   public boolean isDeployed(URL url)
   {
      return getDeployment(url) != null;
   }

   public DeploymentInfo getDeployment(URL url)  
   { 
      synchronized (deploymentsList)
      {
         return (DeploymentInfo) deployments.get(url); 
      }
   }
   
   public DeploymentInfo removeDeployment(DeploymentInfo di) 
   { 
      synchronized (deploymentsList)
      {
         return (DeploymentInfo) deployments.remove(di.url); 
      }
   } 
   
   private synchronized int getNextID() { return id++;}
}
