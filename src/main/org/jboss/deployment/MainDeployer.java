/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.util.ArrayList;
import java.util.Set;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.jboss.system.ServiceMBeanSupport;

import org.jboss.util.DirectoryBuilder;
import org.jboss.util.MBeanProxy;


/**
 * Takes a series of URL to watch, detects changes and calls the appropriate Deployers.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.13 $
 */
public class MainDeployer
   extends ServiceMBeanSupport
   implements MainDeployerMBean, Runnable
{
   /** JMX Server **/
   private MBeanServer server;
   
   /** Deployers **/
   private Set deployers = new HashSet();
   
   /** Scanned Directories **/
   private ArrayList directories = new ArrayList();
   
   /** I always feel like somebody is watching me, contains DeploymentInfo **/
   private Map deployments = new HashMap();
   private ArrayList deploymentsList = new ArrayList();
   
   /** Thread running **/
   private boolean running = false;
   
   /** period of scanning **/
   private int period = 5000;
   
   /** an increment for tmp files **/
   private int id = 0;
   
   /** Given a flat set of files, this is the order of deployment **/
   private String[] order = { "sar", "service.xml", "rar", "jar", "war", "ear", "zip" };
   
   public void setPeriod(int period) {
      this.period = period; 
   }

   public int getPeriod() { 
      return period; 
   }
   
   /** 
    * Directory get set logic, these are "scanning" directories
    * on the local filesystem
    */
   public void setDirectories(String urlList) 
   {
      StringTokenizer urls = new StringTokenizer(urlList, ",");
      
      // Add URLs to list
      while (urls.hasMoreTokens())
      {
         addDirectory(urls.nextToken().trim()) ;
      } 
   }
   
   public void addDirectory(String url) 
   {
      // We are dealing with a relative path URL 
      if (!( url.startsWith("file:") || url.startsWith("http:")))
      {
         url = "file:" + System.getProperty("jboss.system.home") + "/" + url;
      }

      // Only one entry
      try 
      { 
         URL dir = new URL(url);
         if (!directories.contains(dir)) {
	    directories.add(dir); 
	 }
      }
      catch (MalformedURLException bad)
      { 
         log.warn("Failed to add directory scan " + url); 
         return;
      }
      

      if (log.isDebugEnabled()) {
         log.debug("Added directory scan "+url);
      }
   }
   
   public void removeDirectory(String url) 
   {
      // We are dealing with a relative path URL 
      if (!( url.startsWith("file:") || url.startsWith("http:")))
      {
         url = System.getProperty("jboss.system.home") + url;
      }
      
      try 
      { 
         int index = directories.lastIndexOf(new URL(url));
         if (index != -1) {
	    directories.remove(index); 
	 }
      }
      catch (MalformedURLException bad)
      { 
         log.warn("Failed to remove directory scan "+url); 
         return;
      }
      
      if (log.isDebugEnabled()) {
         log.debug("Removed directory scan "+url);
      }
   }

   public String[] getDeployed()
   {
      String[] urls = new String[deployments.size()];
      
      int i = 0;
      Iterator iterator = deployments.keySet().iterator();
      while (iterator.hasNext()) 
      {
         urls[i++] = ((DeploymentInfo) iterator.next()).url.toString();
      }
      return urls;
   }
   
   public void addDeployer(DeployerMBean deployer) {
      deployers.add(deployer);
   }
   
   public void removeDeployer(DeployerMBean deployer) {
      deployers.remove(deployer);
   }
   
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   /**
    * Gets the Name attribute of the AutoDeployer object
    *
    * @return The Name value
    */
   public String getName()
   {
      //
      // What is this used for?  Should this method be depricated?
      //

      return "Main Deployer";
   }
   
   /**
    * Gets the ObjectName attribute of the AutoDeployer object
    */
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      this.server = server;
      return name == null ? OBJECT_NAME : name;
   }
   
   protected void startService()
      throws Exception
   {
      // watch the deploy directory, it is a set so multiple adds 
      // (start/stop) only one entry is present

      // FIXME: Should pull this from ServerConfig

      addDirectory("deploy");
      
      // Do a first pass
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
   
   public boolean getScan() { 
      return running; 
   }

   public void setScan(boolean scan) { 
      running = scan; 
   }
   
   /**
    * Main processing method for the MainDeployer object
    */
   public void run()
   {
      do
      {   
         scan();

         // Sleep
         try
         {
            Thread.sleep(period);
         }
         catch (Exception e)
         {
            log.debug("interrupted");
         }
      } 
      while (running);
   }
   
   public void scan() 
   {   
      try 
      {
	 Iterator iter;

         // Scan diretories for new deployments 
         iter = scanNew().listIterator();
         while (iter.hasNext())
         {
            deploy((URL)iter.next());     
         }

         // Undeploy and redeploy to the modified ones
	 iter = scanModified().listIterator();
         
         while (iter.hasNext())
         {
            DeploymentInfo di = (DeploymentInfo)iter.next();
            try
            {
               // if the url is a file that doesn't exist, it was removed -> undeploy

               // TODO: check connection on http protocol and see if it is removed.

               if (di.url.getProtocol().startsWith("file") && !new File(di.url.getFile()).exists())
               {   
                  undeploy(di);
               }  
               // it is a deployment 
               else 
               {
                  undeploy(di); deploy(di);
               }
            }
            catch (Exception e)
            {
               log.warn("operation failed; ignoring", e);
            }
         }
      }
      catch (Exception e)
      {
         log.warn("operation failed; ignoring", e);
      } 
   }

   public void undeploy(String url)
   {
      try 
      {
         DeploymentInfo sdi = (DeploymentInfo) deployments.get(new URL(url));
         
         if (sdi!= null) {
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
      Iterator subs = di.subDeployments.iterator();
      while (subs.hasNext())
      {
         DeploymentInfo sub = (DeploymentInfo) subs.next();
         
         // undeploy((DeploymentInfo) subs.next());
         if (log.isDebugEnabled())
            log.debug("DEPLOYMENT OF SUB "+sub.url);
         undeploy(sub);      
      }

      // Them remove the deployment itself
      try 
      { 
         // Tell the respective deployer to undeploy this one
         if (di.deployer != null)
            di.deployer.undeploy(di); 

         // remove from local maps
         deployments.remove(di.url);
         if (deploymentsList.lastIndexOf(di) != -1) {
            deploymentsList.remove(deploymentsList.lastIndexOf(di));
	 }
            
         // Nuke my stuff, this includes the class loader
         di.cleanup(log);
         
	 log.info("Undeployed "+di.url);
      }
      catch (Exception e)
      {
         log.error("Undeployment failed: " + di.url, e); 
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
      DeploymentInfo sdi = (DeploymentInfo) deployments.get(url);
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
         log.error("Could not deploy URL "+url, e);
      }
   }

   public void deploy(DeploymentInfo deployment) 
      throws DeploymentException
   {      
      boolean debug = log.isDebugEnabled();

      try
      {
         // If we are already deployed return
         if (deployments.containsKey(deployment.url))
            return;

	 log.info("Deploying: " + deployment.url.toString());

         // Create a local copy of that File, the sdi keeps track of the copy directory
         makeLocalCopy(deployment);
         
         // initialize the unified classloaders for this deployment
         deployment.createClassLoaders();
         
         // What deployer is able to deploy this file
         findDeployer(deployment);
         
         if(deployment.deployer != null) {
            deployment.deployer.init(deployment); 
	 }

         // create subdeployments as needed
         deploySubPackages(deployment);
         
         // Deploy this SDI, if it is a deployable type
         if (deployment.deployer != null) {
            deployment.deployer.deploy(deployment);
	 }

         deployment.status = "Deployed";

         if (debug) {
            log.debug("Done deploying " + deployment.shortName);
	 }
      }  
      catch (DeploymentException e) 
      { 
         deployment.status = "Deployment FAILED reason: " + e.getMessage();         
         throw e;
      }
      finally 
      {
         // whether you do it or not, for the autodeployer
         deployment.lastDeployed = System.currentTimeMillis();
         
         // watch it, it will be picked up as modified below, deployments is a map duplicates are ok
         deployments.put(deployment.url, deployment);
         
         // Do we watch it?

	 // FIXME: Should not hardcode deployment tmp dir
	 
         if (!deployment.url.toString().startsWith("file:"+System.getProperty("jboss.system.home")+ "/tmp/deploy"))
         {
            deploymentsList.add(deployment);
            if (debug) {
               log.debug("Watching new file: " + deployment.url);  
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

      Iterator iter = deployers.iterator();
      while (iter.hasNext())
      {
         DeployerMBean deployer = (DeployerMBean) iter.next();
         if (deployer.accepts(sdi))
         {
            sdi.deployer = deployer;
            if (debug)
               log.debug("using deployer "+deployer);
            return;
         }
      }

      if (debug)
         log.debug("NO DEPLOYER for url "+sdi.url);
   }

   public void preDeregister()
      throws Exception
   {
      running = false;
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
         Iterator iter = directories.listIterator();
         while (iter.hasNext()) 
         {
            File dir = new File(((URL) iter.next()).getFile());
            if (trace)
               log.trace("Scanning directory: " + dir);
            File[] files = dir.listFiles();
            if (files == null)
            {
               log.error("we have a problem null files in directory; should not happen");
            }

            for (int i = 0; i < files.length; i++)
            {
               if( trace )
                  log.trace("Checking deployment file: "+files[i]);
               // It is a new file
               if (!deployments.containsKey(files[i].toURL())) 
                  newDeployments.add(files[i].toURL());
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
    * scanModified scans the existing deployments and return a 
    * Set with the modified deployments
    */
   protected ArrayList scanModified()
   {
      try
      {
         HashSet modified = new HashSet();
         
         if (log.isTraceEnabled())
            log.trace("Scanning installed deployments");

         // People already deployed, scan for modifications  
         Iterator iter = deploymentsList.listIterator();
         
         while (iter.hasNext())
         {
            DeploymentInfo deployment = (DeploymentInfo) iter.next();
            long lastModified = 0;
            
            // Get lastModified of file from file system
            if (deployment.watch.getProtocol().startsWith("file"))
            {
               File theFile = new File(deployment.watch.getFile());
               if ( ! theFile.exists()) modified.add(deployment);
                  
               lastModified = theFile.lastModified();
            }
            
            // Use URL connection to get lastModified on http
            else lastModified = deployment.watch.openConnection().getLastModified();
               
            // Check the record in the DeploymentInfo against the physical one 
            if (deployment.lastDeployed < lastModified) 
               modified.add(deployment);
         }
         
         return sortDeployments(modified);
      }
      catch (Throwable t)
      {
         log.error("operation failed", t);
         return null;
      }
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
      // If XML only no subdeployment to speak of
      // FIXME do the sub deploy for directory and the move to 
      // if (di.isXML) return;
      if (di.isXML || di.isDirectory)
         return ;

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
         throw new DeploymentException(e);
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
               name = name.substring(name.lastIndexOf("/")+1);
            
            try 
            {
               DirectoryBuilder builder = 
                  new DirectoryBuilder(System.getProperty("jboss.system.home"));
               File localCopyDir = builder.cd("tmp").cd("deploy").get();
               
               // We use the name of the entry as the name of the file under deploy 
               File outFile = builder.cd(getNextID () + "." + name).get();
               
               // Copy in and out 
               OutputStream out = new FileOutputStream(outFile); 
               InputStream in = jarFile.getInputStream(entry);
               
               try
               { 
                  copy(in, out);
               }
               finally
               { 
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
                  ("Could not deploy sub deployment "+name+" of deployment "+di.url);
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
      Iterator iter = sortDeployments(subDeployments).listIterator();
      
      // Deploy them all 
      while (iter.hasNext()) 
      { 
         try
         { 
            deploy((DeploymentInfo)iter.next());
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
         if (debug) {
            log.debug("resolveLibraries: " + classPath);
	 }

         while (st.hasMoreTokens())
         {
            URL lib = null;
            
            String tk = st.nextToken();
            
            DeploymentInfo sub = null;

            if (debug) {
               log.debug("new manifest entry for sdi at "+sdi.shortName+" entry is "+tk);
	    }

            try
            {   
               lib = new URL(sdi.url, tk);
               
               if (!deployments.containsKey(lib))
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
      URL dest = null;
      File localCopyDir = null;
      
      try 
      {   
         localCopyDir = new File(System.getProperty("jboss.system.home")+File.separator+"tmp"+File.separator+"deploy");
         
         if (sdi.url.getProtocol().startsWith("file") && sdi.isDirectory)
         {
            // FIXME TODO add support for Directory copying over
            
            sdi.localUrl = sdi.url;
            
            return;
         }
         
         // Are we already in the localCopyDir?

	 // FIXME: Should not hard code deploy tmp dir, should also reference it as a URL, instead
	 //        of this mixed mode crap (is this a file or a URL, url should not use File.sep)

         else if (sdi.url.toString().indexOf(System.getProperty("jboss.system.home")+File.separator+"tmp"+File.separator+"deploy") != -1) 
         {
            sdi.localUrl = sdi.url;
            return;
         }
         else
         {
            sdi.localUrl =  new File (localCopyDir, getNextID ()+"."+sdi.shortName).toURL();
            copy(sdi.url, sdi.localUrl);
         }
      }
      catch (Exception e)
      {
         log.error("Could not make local copy for " + sdi.url, e);
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
   
   protected void copy(URL _src, URL _dest) throws IOException
   {
      if (!_dest.getProtocol ().equals ("file"))
         throw new IOException("only file: protocol is allowed as destination!");
      
      InputStream in;
      OutputStream out;
      
      String s = _dest.getFile ();
      File dir = new File (s.substring (0, s.lastIndexOf("/")));
      if (!dir.exists ())
         dir.mkdirs ();
      
      in = _src.openStream ();
      out = new FileOutputStream (s); 
      
      byte[] buffer = new byte[1024];
      
      int read;
      while (true)
      {
         read = in.read(buffer);
         if (read == -1)
            break;
         
         out.write(buffer, 0, read);
      }
      
      out.flush ();
      
      out.close ();
      in.close ();
   }
   
   public ArrayList sortURLs(Set urls)
   {
      ArrayList list = new ArrayList(urls.size());
      
      for (int i = 0 ; i < order.length ; i++)
      {
         Iterator it = urls.iterator();
         while (it.hasNext()) 
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
         Iterator it = urls.iterator();
         while (it.hasNext()) 
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
      
      return list;
   }
   
   public boolean isDeployed(String url) 
      throws MalformedURLException
   {
      DeploymentInfo di = getDeployment(new URL(url));

      return (di != null);
   }

   public DeploymentInfo getDeployment(URL url)  
   { 
      return (DeploymentInfo)deployments.get(url); 
   }
   
   public DeploymentInfo removeDeployment(DeploymentInfo di) 
   { 
      return (DeploymentInfo)deployments.remove(di.url); 
   } 
   
   private int getNextID() { 
      return id++;
   }
}
