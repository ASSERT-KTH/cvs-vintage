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
import org.jboss.system.Service;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* This is the main Service Deployer API.
*
* @see       org.jboss.system.Service
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
* @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
* @version   $Revision: 1.8 $ <p>
*
*      <b>20010830 marc fleury:</b>
*      <ul>initial import
*        <li>
*      </ul>
*
*      <p><b>20010905 david maplesden:</b>
*      <ul>
*      <li>Changed deployment procedure to deploy all listed mbeans, then
*      initialise them all before finally starting them all.  Changed services
*      sets to lists to maintain ordering.
*      </ul>
*
*      <b>20010908 david jencks</b>
*      <ol>
*        <li> fixed tabs to spaces and log4j logging. Made the urlToServiceSet
*        map actually use the url supplied to deploy. Made postRegister use
*        deploy. Made undeploy work, and implemented sar dependency management
*        and recursive deploy/undeploy.
*      </ol>
*
*      <p><b>20010907 david maplesden:</b>
*      <ul>
*      <li>Added support for "depends" tag
*      </ul>
*
*      <p><b>20011210 marc fleury:</b>
*      <ul>
*      <li>Removing the classpath dependency to explicit jars
*      </ul>
*      <p><b>20011211 marc fleury:</b>
*      <ul>
*      <li>rewrite
*      </ul>
*/
public class SARDeployer
   extends ServiceMBeanSupport
   implements SARDeployerMBean
{
   // Attributes --------------------------------------------------------
   private ObjectName objectName;
   
   //Find all the deployment info for a url
   private final Map urlToDeploymentInfoMap = new HashMap();
   
   //Find what package an mbean came from.
   private final Map objectNameToSupplyingPackageMap = new HashMap();
   
   
   
   // Public --------------------------------------------------------
   
   /**
   * Gets the Name of the ServiceDeployer object
   *
   * @return   returns "ServiceDeployer"
   */
   public String getName()
   {
      return "ServiceDeployer";
   }
   
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
            File file = new File (di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) di.watch = di.url;
               
            // If directory we watch the xml files
            else di.watch = new URL(di.url, "META-INF/jboss-service.xml"); 
         }
         
         // Get the document
         parseDocument(di);     
         
         
         // In case there is a dependent classpath defined parse it
         // This creates 
         parseXMLClasspath(di);
         
         //Copy local directory if local-directory element is present
         
         NodeList lds = di.document.getElementsByTagName("local-directory");
         log.debug("about to copy " + lds.getLength() + " local directories");
         for (int i = 0; i< lds.getLength(); i++)
         {
            Element ld = (Element)lds.item(i);
            String path = ld.getAttribute("path");
            log.debug("about to copy local directory at " + path);
            File jbossHomeDir = new File(System.getProperty("jboss.system.home"));
            File localBaseDir = new File(jbossHomeDir, "db"+File.separator);
            //Get the url of the local copy from the classloader.
            log.debug("copying from " + di.localUrl.toString() + path);
            log.debug("copying to " + localBaseDir);
            
            inflateJar(di.localUrl, localBaseDir, path);
         } // end of for ()
      }
      catch (Exception e) 
      {
         log.error("Problem in init", e);
         throw new DeploymentException(e.getMessage());
      }
   }

   public void deploy(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // install the MBeans in this descriptor
         if (log.isInfoEnabled())
            log.info("Deploying SAR: url " + di.url);
         
         List mbeans = di.mbeans;
         mbeans.clear();
         
         NodeList nl = di.document.getElementsByTagName("mbean");
         
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element mbean = (Element)nl.item(i);
            
            log.debug("deploying with ServiceController mbean " + mbean);
            
            ObjectName service = (ObjectName)invoke(
               getServiceControllerName(),
               "install",
               new Object[]{mbean},
               new String[]{"org.w3c.dom.Element"});
            
            if (service != null)
            {
               mbeans.add(service);
               //      objectNameToSupplyingPackageMap.put(service, url);
            }
         }
         
         // create the services
         Iterator iterator = di.mbeans.iterator();
         while (iterator.hasNext()) 
         {
            ObjectName service = (ObjectName) iterator.next();
            
            // The service won't be created until explicitely dependent mbeans are created
            invoke(
               getServiceControllerName(),
               "create",
               new Object[]{service},
               new String[]{"javax.management.ObjectName"});
         }
         
         // start the services
         Iterator iterator2 = di.mbeans.iterator();
         while (iterator2.hasNext()) 
         {
            ObjectName service2 = (ObjectName) iterator2.next();
            
            // The service won't be started until explicitely dependent mbeans are started
            invoke(
               getServiceControllerName(),
               "start",
               new Object[]{service2},
               new String[]{"javax.management.ObjectName"});
         }
      }
      catch (Exception e ) {log.error("Error in deploy ", e);}
   }
   
   protected void parseXMLClasspath(DeploymentInfo di) 
      throws DeploymentException
   {
      Set classpath = new HashSet();
      
      NodeList classpaths = di.document.getElementsByTagName("classpath");
      for (int i = 0; i < classpaths.getLength(); i++)
      {
         Element classpathElement = (Element)classpaths.item(i);
         log.debug("Found classpath element: " + classpathElement);
         
         //String codebase = System.getProperty("jboss.system.libraryDirectory");
         String codebase = "";
         String archives = "";
         
         //Does it specify a codebase?
         if (classpathElement != null)
         {
            // Load the codebase
            codebase = classpathElement.getAttribute("codebase").trim();
            log.debug("Setting up classpath from raw codebase: " + codebase);
            
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
               codebase = System.getProperty("jboss.system.installationURL")+codebase;
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
            log.debug("codebase is " + codebase);
            //get the archives string
            archives = classpathElement.getAttribute("archives").trim();
            log.debug("archives are " + archives);
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
                     *      file.
                     */
                     public boolean accept(File pathname)
                     {
                        String name2 = pathname.getName();
                        return 
                        
                        (name2.endsWith(".jar") || name2.endsWith(".zip"));
                     }
                  }
               );
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
            if (codebase.equals(""))
               codebase = System.getProperty("jboss.system.libraryDirectory");
               
            if (archives.equals("*")) 
            {
               // Safeguard
               if (!codebase.startsWith("file:") && archives.equals("*"))
                  throw new DeploymentException("No wildcard permitted in non-file URL deployment you must specify individual jars");

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
                        *      file.
                        */
                        public boolean accept(File pathname)
                        {
                           String name2 = pathname.getName();
                           return name2.endsWith(".jar") || name2.endsWith(".zip");
                        }
                     }
                  );
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
            String msg = "A classpath entry was declared but no non-file codebase"
               + "and no jars specified. Please fix jboss-service.xml in your configuration";
            throw new DeploymentException(msg);
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
            
            invoke(
               new ObjectName(org.jboss.deployment.MainDeployerMBean.OBJECT_NAME),
               "deploy",
               new Object[] {sub},
               new String[] {"org.jboss.deployment.DeploymentInfo"});
         }
         catch (Exception e)
         {
            log.error("operation failed", e);
         }
         log.debug("deployed classes for " + neededUrl);
      } // end of while ()
   }
   
   /**
   * Undeploys the package at the url string specified. This will: Undeploy
   * packages depending on this one. Stop, destroy, and unregister all the
   * specified mbeans Unload this package and packages this package deployed
   * via the classpath tag. Keep track of packages depending on this one that
   * we undeployed so that they can be redeployed should this one be
   * redeployed.
   *
   * @param urlString                  The location of the package to be
   *      undeployed (used to index the packages, not to read service.xml on
   *      undeploy!
   * @exception MalformedURLException  Thrown if the url string is not valid.
   * @exception IOException            Thrown if something could not be read.
   * @exception DeploymentException    Thrown if the package could not be
   *      undeployed
   */
   public void undeploy(DeploymentInfo sdi)
      throws DeploymentException
   {
      log.debug("undeploying document " + sdi.url);
      
      List services = sdi.mbeans;
      int lastService = services.size();
      //stop services in reverse order.
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("stopping mbean " + name);
         invoke(getServiceControllerName(),
            "stop",
            new Object[] {name},
            new String[] {"javax.management.ObjectName"});
      }
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("destroying mbean " + name);
         invoke(getServiceControllerName(),
            "destroy",
            new Object[] {name},
            new String[] {"javax.management.ObjectName"});
      }
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("removing mbean " + name);
         invoke(getServiceControllerName(),
            "remove",
            new Object[] {name},
            new String[] {"javax.management.ObjectName"});
         //we don't supply it any more, maybe someone else will later.
         //        objectNameToSupplyingPackageMap.remove(name);
      }
   }
   
   
   /**
   * MBeanRegistration interface. Get the mbean server.
   * This is the only deployer that registers with the MainDeployer here
   *
   * @param server                   Our mbean server.
   * @param name                     our proposed object name.
   * @return                         our actual object name
   * @exception java.lang.Exception  Thrown if we are supplied an invalid name.
   */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      super.preRegister(server, name);
      log.debug("ServiceDeployer preregistered with mbean server");
      objectName = name == null ? new ObjectName(OBJECT_NAME) : name;
      
      // Register with the main deployer
      server.invoke(
         new ObjectName(org.jboss.deployment.MainDeployerMBean.OBJECT_NAME),
         "addDeployer",
         new Object[] {this},
         new String[] {"org.jboss.deployment.DeployerMBean"});
      
      return objectName;
   }
   
   /**
   * PostRegister initialized the ServiceDeployed mbean and tries to load a
   * spine package to set up basic jboss. At the moment the spine package
   * should be jboss-service.xml or (deprecated) jboss.jcml. Soon we should
   * have an actual sar with the code as well as configuration info.
   *
   * @param registrationDone  Description of Parameter
   */
   /*public void postRegister(java.lang.Boolean registrationDone)
   {
   try
   {
   super.postRegister(registrationDone);
   
   //Start us up, which also sets up the deploy temp directory
   invoke(getServiceControllerName(),
   "create",
   new Object[] {objectName},
   new String[] {"javax.management.ObjectName"});
   
   invoke(getServiceControllerName(),
   "start",
   new Object[] {objectName},
   new String[] {"javax.management.ObjectName"});
   }
   catch (Exception e)
   {
   log.error("Problem postregistering ServiceDeployer", e);
   }
   }
   */
   
   public void preDeregister()
      throws Exception
   {
      server.invoke(
         new ObjectName(org.jboss.deployment.MainDeployerMBean.OBJECT_NAME),
         "removeDeployer",
         new Object[] {this},
         new String[] {"org.jboss.deployment.DeployerMBean"});
   
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
      catch (SAXException e)
      {
         log.warn("SaxException getting document:", e);
         throw new DeploymentException(e.getMessage());
      }
      catch (ParserConfigurationException pce)
      {
         log.warn("ParserConfigurationException getting document:", pce);
         throw new DeploymentException(pce.getMessage());
      }
      catch (Exception e)
      {
         log.warn("Exception getting document:", e);
         throw new DeploymentException(e.getMessage());
      } // end of try-catch
   }
   
   
   // Private --------------------------------------------------------
   
   private ObjectName getServiceControllerName()
      throws DeploymentException
   {
      try
      {
         return new ObjectName(ServiceControllerMBean.OBJECT_NAME);
      }   
      catch (Exception e)
      {
         throw new DeploymentException ("Couldn't get the ObjectName for the ServiceControllerMBean");
      }
   }

   private void removeMBeans(URL url, DeploymentInfo sdi) throws DeploymentException
   {
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
                  
                  if (entry.isDirectory())  outFile.mkdirs();
                     
                  else
                  {
                     InputStream in = jarFile.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile);
                     
                     try
                     {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) > 0) out.write(buffer, 0, read);
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
   
   
   /**
   * Parse an object name from the given element attribute 'name'.
   *
   * @param element    Element to parse name from.
   * @return           Object name.
   *
   * @throws ConfigurationException   Missing attribute 'name'
   *                                  (thrown if 'name' is null or "").
   * @throws MalformedObjectNameException
   */
   private ObjectName parseObjectName(final Element element)
      throws org.jboss.system.ConfigurationException, MalformedObjectNameException
   {
      String name = ((org.w3c.dom.Text)element.getFirstChild()).getData().trim();
      if (name == null || name.trim().equals("")) {
         throw new org.jboss.system.ConfigurationException
         ("Name element must have a value.");
      }
      return new ObjectName(name);
   }
   
   
   /* Calls server.invoke, unwraps exceptions, and returns server output
   */
   private Object invoke(ObjectName name, String method, Object[] args, String[] sig)
   {
      try
      {
         return server.invoke(name, method, args, sig);
      }
      catch (MBeanException mbe)
      {
         log.error("Mbean exception while executing " + method + " on " + args, mbe.getTargetException());
      }
      catch (RuntimeMBeanException rbe)
      {
         log.error("Runtime Mbean exception while executing " + method + " on " + args, rbe.getTargetException());
      }
      catch (RuntimeErrorException ree)
      {
         log.error("Runtime Error exception while executing " + method + " on " + args, ree.getTargetError());
      }
      catch (ReflectionException re)
      {
         log.error("ReflectionException while executing " + method + " on " + args, re);
      }
      catch (InstanceNotFoundException re)
      {
         log.error("InstanceNotFoundException while executing " + method + " on " + args, re);
      }
      catch (Exception e)
      {
         log.error("Exception while executing " + method + " on " + args, e);
      }
      return null;
   }
}
