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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.Enumeration;
import java.util.jar.JarEntry;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.system.ServerConfigMBean;
import org.jboss.system.Service;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.jmx.MBeanProxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is the main Service Deployer API.
 *
 * @see org.jboss.system.Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.13 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *    <li>initial import
 * </ul>
 *
 * <p><b>20010905 david maplesden:</b>
 * <ul>
 *    <li>Changed deployment procedure to deploy all listed mbeans, then
 *        initialise them all before finally starting them all.  Changed services
 *        sets to lists to maintain ordering.
 * </ul>
 *
 * <p><b>20010908 david jencks</b>
 * <ul>
 *    <li>fixed tabs to spaces and log4j logging. Made the urlToServiceSet
 *        map actually use the url supplied to deploy. Made postRegister use
 *        deploy. Made undeploy work, and implemented sar dependency management
 *        and recursive deploy/undeploy.
 * </ul>
 *
 * <p><b>20010907 david maplesden:</b>
 * <ul>
 *    <li>Added support for "depends" tag
 * </ul>
 *
 * <p><b>20011210 marc fleury:</b>
 * <ul>
 *    <li>Removing the classpath dependency to explicit jars
 * </ul>
 *
 * <p><b>20011211 marc fleury:</b>
 * <ul>
 *   <li>rewrite
 * </ul>
 */
public class SARDeployer
   extends ServiceMBeanSupport
   implements SARDeployerMBean
{
   /** A proxy to the ServiceController. */
   private ServiceControllerMBean serviceController;

   /** A proxy to the MainDeployer. */
   private MainDeployerMBean mainDeployer;

   /** The system data directory. */
   private File dataDir;

   /** The system install URL. */
   private URL installURL;

   /** The system library URL. */
   private URL libraryURL;
   
   // Public --------------------------------------------------------
   
   /**
    * Gets the FilenameFilter that the AutoDeployer uses to decide which files
    * will be deployed by the ServiceDeployer. Currently .jsr, .sar, and files
    * ending in service.xml are accepted.
    *
    * @return   The FileNameFilter for use by the AutoDeployer.
    */
   public boolean accepts(DeploymentInfo di) 
   {
      return (di.url.toString().endsWith(".sar")
              || di.url.toString().endsWith("service.xml"));
   }   

   public void init(DeploymentInfo di)
      throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();
      
      try 
      {
         // resolve the watch
         if (di.url.getProtocol().startsWith("http"))
         {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
         else if(di.url.getProtocol().startsWith("file"))
         {
            File file = new File(di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) {
               di.watch = di.url;
            }
            // If directory we watch the xml files
            else {
               di.watch = new URL(di.url, "META-INF/jboss-service.xml");
            }
         }
         
         // Get the document
         parseDocument(di);     
         
         
         // In case there is a dependent classpath defined parse it
         // This creates 
         parseXMLClasspath(di);
         
         //Copy local directory if local-directory element is present
         
         NodeList lds = di.document.getElementsByTagName("local-directory");
         if (debug) {
            log.debug("about to copy " + lds.getLength() + " local directories");
         }
         
         for (int i = 0; i< lds.getLength(); i++)
         {
            Element ld = (Element)lds.item(i);
            String path = ld.getAttribute("path");
            if (debug) {
               log.debug("about to copy local directory at " + path);
            }

            // Get the url of the local copy from the classloader.
            if (debug) {
               log.debug("copying from " + di.localUrl + path + " -> " + dataDir);
            }
            
            inflateJar(di.localUrl, dataDir, path);
         }
      }
      catch (Exception e) 
      {
         log.error("Problem in init", e);
         throw new DeploymentException(e);
      }
   }

   public void deploy(DeploymentInfo di)
      throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();

      try
      {
         // install the MBeans in this descriptor
         if (debug) {
            log.debug("Deploying SAR: url " + di.url);
	 }
         
         List mbeans = di.mbeans;
         mbeans.clear();
         
         NodeList nl = di.document.getElementsByTagName("mbean");
         
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element mbean = (Element)nl.item(i);
            
            log.debug("deploying with ServiceController mbean " + mbean);
	    ObjectName service = serviceController.install(mbean);
            
            if (service != null)
            {
               mbeans.add(service);
               // objectNameToSupplyingPackageMap.put(service, url);
            }
         }
         
	 Iterator iter;
	 ObjectName service;

         // create the services
         iter = di.mbeans.iterator();
         while (iter.hasNext()) 
         {
            service = (ObjectName)iter.next();
            
            // The service won't be created until explicitely dependent mbeans are created
	    serviceController.create(service);
         }
         
         // start the services
         iter = di.mbeans.iterator();
         while (iter.hasNext()) 
         {
            service = (ObjectName)iter.next();
            
            // The service won't be started until explicitely dependent mbeans are started
	    serviceController.start(service);
         }
      }
      catch (Exception e) {
	 log.error("operation failed", e);
	 throw new DeploymentException(e);
      }
   }
   
   protected void parseXMLClasspath(DeploymentInfo di) 
      throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();
      Set classpath = new HashSet();
      
      NodeList classpaths = di.document.getElementsByTagName("classpath");
      for (int i = 0; i < classpaths.getLength(); i++)
      {
         Element classpathElement = (Element)classpaths.item(i);
         if (debug) {
            log.debug("Found classpath element: " + classpathElement);
         }
         
         String codebase = "";
         String archives = "";
         
         //Does it specify a codebase?
         if (classpathElement != null)
         {
            // Load the codebase
            codebase = classpathElement.getAttribute("codebase").trim();
            if (debug) {
               log.debug("Setting up classpath from raw codebase: " + codebase);
            }
            
            if ("".equals(codebase) || ".".equals(codebase))
            {  
               //does this work with http???
               // marcf: for http we could just get the substring to the last "/"
               codebase = new File(di.url.getProtocol() + "://" + di.url.getFile()).getParent();
            }   
            // Do we have a relative codebase?
            if (!(codebase.startsWith("http:") || codebase.startsWith("file:"))) 
            {
               // put the jboss/system base in front of it
               try {
                  codebase = new URL(installURL, codebase).toString();
               }
               catch (MalformedURLException e) {
                  throw new DeploymentException(e);
               }
            }
            
            // Let's make sure the formatting of the codebase ends with the /
            if (codebase.startsWith("file:") && !codebase.endsWith("/"))
            {
               codebase += "/";
            }
            else if (codebase.startsWith("http:") && !codebase.endsWith("/"))
            {
               codebase += "/";
            }

            if (debug) {
               log.debug("codebase is " + codebase);
            }
            
            //get the archives string
            archives = classpathElement.getAttribute("archives").trim();
            if (debug) {
               log.debug("archives are " + archives);
            }
         }
         
         if (codebase.startsWith("file:") && archives.equals("*"))
         {
            try
            {
               URL fileURL = new URL(codebase);
               File dir = new File(fileURL.getFile());
               // The patchDir can only be a File one, local
               File[] jars = dir.listFiles(
                  new java.io.FileFilter()
                  {
                     /**
                      * filters for jar and zip files in the local directory.
                      *
                      * @param pathname  Path to the candidate file.
                      * @return          True if the file is a jar or zip
                      *                  file.
                      */
                     public boolean accept(File pathname)
                     {
                        String name2 = pathname.getName();
                        return 
                           (name2.endsWith(".jar") || name2.endsWith(".zip"));
                     }
                  });

               for (int j = 0; jars != null && j < jars.length; j++)
               {
                  classpath.add(jars[j].getCanonicalFile().toURL());
               }
            }
            catch (Exception e)
            {
               log.error("problem listing files in directory", e);
               throw new DeploymentException("problem listing files in directory", e);
            }
         }
         // A directory that is to be added to the classpath
         else if(codebase.startsWith("file:") && archives.equals(""))
         {
            try
            {
               URL fileURL = new URL(codebase);
               File dir = new File(fileURL.getFile());
               classpath.add(dir.getCanonicalFile().toURL());
            }
            catch(Exception e)
            {
               log.error("Failed to add classpath dir", e);
               throw new DeploymentException("Failed to add classpath dir", e);
            }
         }
         // We have an archive whatever the codebase go ahead and load the libraries
         else if (!archives.equals(""))
         {
            // Still no codebase? safeguard
            if (codebase.equals("")) {
               codebase = libraryURL.toString();
            }
               
            if (archives.equals("*")) 
            {
               // Safeguard
               if (!codebase.startsWith("file:") && archives.equals("*")) {
                  throw new DeploymentException
		     ("No wildcard permitted in non-file URL deployment you must specify individual jars");
	       }

               try
               {
                  URL fileURL = new URL(codebase);
                  File dir = new File(fileURL.getFile());
                  // The patchDir can only be a File one, local
                  File[] jars = dir.listFiles(
                     new java.io.FileFilter()
                     {
                        /**
                         * filters for jar and zip files in the local directory.
                         *
                         * @param pathname  Path to the candidate file.
                         * @return          True if the file is a jar or zip
                         *                  file.
                         */
                        public boolean accept(File pathname)
                        {
                           String name2 = pathname.getName();
                           return name2.endsWith(".jar") || name2.endsWith(".zip");
                        }
                     });
                  
                  for (int j = 0; jars != null && j < jars.length; j++)
                  {
                     classpath.add(jars[j].getCanonicalFile().toURL());
                  }
               }
               catch (Exception e)
               {
                  log.error("problem listing files in directory", e);
                  throw new DeploymentException("problem listing files in directory", e);
               }
            }

            else // A real archive listing (as opposed to wildcard)
            {
               StringTokenizer jars = new StringTokenizer(archives, ",");
               //iterate through the packages in archives
               while (jars.hasMoreTokens())
               {
                  // The format is simple codebase + jar
                  try
                  {
                     String archive = codebase + jars.nextToken().trim();
                     URL archiveURL = new URL(archive);
                     classpath.add(archiveURL);
                  }    
                  catch (MalformedURLException mfue)
                  {
                     log.error("couldn't resolve package reference: ", mfue);
                  } // end of try-catch
               }
            }
         }
         //codebase is empty and archives is empty but we did have a classpath entry
         else
         {
            throw new DeploymentException
	       ("A classpath entry was declared but no non-file codebase " +
		"and no jars specified. Please fix jboss-service.xml in your configuration");
         }
      }
      
      //Ok, now we've found the list of urls we need... deploy their classes.
      Iterator jars = classpath.iterator();
      
      URL neededUrl = null;
      while (jars.hasNext())
      {
         neededUrl = (URL)jars.next();
         
         // Call the main deployer with it
         try
         {   
            // Create a new Deployment not as a subdeployment,
            // An external package is not a "subdeployment" it is a stand alone 
            // deployment scanned as such 
            DeploymentInfo sub = new DeploymentInfo(neededUrl, null);
            mainDeployer.deploy(sub);

         }
         catch (Exception e)
         {
            log.error("operation failed", e);
         }

         if (debug) {
            log.debug("deployed classes for " + neededUrl);
         }
      }
   }
   
   /**
    * Undeploys the package at the url string specified. This will: Undeploy
    * packages depending on this one. Stop, destroy, and unregister all the
    * specified mbeans Unload this package and packages this package deployed
    * via the classpath tag. Keep track of packages depending on this one that
    * we undeployed so that they can be redeployed should this one be
    * redeployed.
    *
    * @param urlString    The location of the package to be
    *                     undeployed (used to index the packages, not to read 
    *                     service.xml on undeploy!
    *
    * @exception MalformedURLException  Thrown if the url string is not valid.
    * @exception IOException            Thrown if something could not be read.
    * @exception DeploymentException    Thrown if the package could not be
    *                                   undeployed
    */
   public void undeploy(DeploymentInfo sdi)
      throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();

      if (debug) {
         log.debug("undeploying document " + sdi.url);
      }
      
      List services = sdi.mbeans;
      int lastService = services.size();

      try {
	 // stop services in reverse order.
	 for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
	 {
	    ObjectName name = (ObjectName)i.previous();
            if (debug) {
               log.debug("stopping mbean " + name);
            }
	    serviceController.stop(name);
	 }

	 for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
         {
	    ObjectName name = (ObjectName)i.previous();
            if (debug) {
               log.debug("destroying mbean " + name);
            }
	    serviceController.destroy(name);
	 }

	 for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
         {
	    ObjectName name = (ObjectName)i.previous();
            if (debug) {
               log.debug("removing mbean " + name);
            }
	    serviceController.remove(name);
	 }
      }
      catch (Exception e) {
	 throw new DeploymentException(e);
      }
   }
   
   /**
    * MBeanRegistration interface. Get the mbean server.
    * This is the only deployer that registers with the MainDeployer here
    *
    * @param server    Our mbean server.
    * @param name      Our proposed object name.
    * @return          Our actual object name
    * 
    * @throws Exception    Thrown if we are supplied an invalid name.
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      super.preRegister(server, name);
      log.debug("ServiceDeployer preregistered with mbean server");
      
      mainDeployer = (MainDeployerMBean)
	 MBeanProxy.create(MainDeployerMBean.class,
			   MainDeployerMBean.OBJECT_NAME,
                           server);

      // Register with the main deployer
      mainDeployer.addDeployer(this);

      // get the controller proxy
      serviceController = (ServiceControllerMBean)
	 MBeanProxy.create(ServiceControllerMBean.class,
			   ServiceControllerMBean.OBJECT_NAME,
			   server);
      
      // get the data directory, install url & library url
      dataDir = (File)
         server.getAttribute(ServerConfigMBean.OBJECT_NAME, "DataDir");
      installURL = (URL)
         server.getAttribute(ServerConfigMBean.OBJECT_NAME, "InstallURL");
      libraryURL = (URL)
         server.getAttribute(ServerConfigMBean.OBJECT_NAME, "LibraryURL");

      return name == null ? OBJECT_NAME : name;
   }

   public void preDeregister()
      throws Exception
   {
      mainDeployer.removeDeployer(this);
   }
   
   protected void parseDocument(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         InputStream stream = null;
         
         // If we are in a xml only get the URL
         if (di.isXML)
            stream = di.localUrl.openStream();
         // Else load from the jar or directory
         else
            stream = di.localCl.getResourceAsStream("META-INF/jboss-service.xml");
         // Validate that the stream is not null
         if( stream == null )
            throw new DeploymentException("Failed to find META-INF/jboss-service.xml");

         InputSource is = new InputSource(stream);
         di.document = parser.parse(is);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Exception getting document", e);
      }
   }
   
   
   // Private --------------------------------------------------------
   
   /**
    * The <code>inflateJar</code> copies the jar entries
    * from the jar url jarUrl to the directory destDir.
    * It can be used on the whole jar, a directory, or
    * a specific file in the jar.
    *
    * @param jarUrl    the <code>URL</code> if the directory or entry to copy.
    * @param destDir   the <code>File</code> value of the directory in which to
    *                  place the inflated copies.
    *
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
                  
                  if (entry.isDirectory()) {
                     outFile.mkdirs();
                  }
                  else {
                     InputStream in = jarFile.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile);
                     
                     try
                     {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) > 0) {
                           out.write(buffer, 0, read);
                        }
                     }                
                     finally
                     {
                        in.close();out.close();
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
}
