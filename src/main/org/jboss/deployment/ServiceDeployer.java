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
import java.util.StringTokenizer;
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
import org.jboss.system.Service;
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
* @version   $Revision: 1.21 $ <p>
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
public class ServiceDeployer
extends DeployerMBeanSupport
implements ServiceDeployerMBean
{
   // Attributes --------------------------------------------------------
   private ObjectName objectName;
   
   //Find all the deployment info for a url
   private final Map urlToServiceDeploymentInfoMap = new HashMap();
   
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
   public FilenameFilter getDeployableFilter()
   {
      return
      new FilenameFilter()
      {
         /**
         * Determines which files are accepted by the Deployer.
         *
         * @param dir       Directory of candidate file.
         * @param filename  Filename of candidate file.
         * @return          Whether candidate file should be deployed by
         *      this deployer.
         */
         public boolean accept(File dir, String filename)
         {
            filename = filename.toLowerCase();
            return (filename.endsWith(".sar")
               || filename.endsWith("service.xml"));
         }
      };
   }
   
   
   /**
   * Deploys a package identified by a url string. This stops if a previous
   * version exists. The package can be a *service.xml file, a sar service
   * archive, or a plain jar or zip file with no deployment descriptor.
   * In any case, all classes found in the package or sar subpackages are
   * added to the extensible classloader.  If there are classpath elements
   * in the configuration file, the named packages are loaded
   * (in separate classloaders) unless they have been explicitly undeployed,
   * in which case deployment of this package is suspended until they have been
   * redeployed.  Then the mbeans named in the configuration file are created
   * using the ServiceController.  Dependencies between mbeans are handled by
   * the ServiceController.
   *
   * @param urlString                  The location of the package to deploy
   * @exception MalformedURLException  Thrown if a malformed url string is
   *      supplied.
   * @exception IOException            Thrown if some read operation failed.
   * @exception DeploymentException    Thrown if the package could not be
   *      deployed.
   */
   public Object deploy(URL url)
   throws MalformedURLException, IOException, DeploymentException
   {
      ServiceDeploymentInfo sdi = getSdi(url, true);
      
      if (sdi.state == ServiceDeploymentInfo.MBEANSLOADED)
      {
         log.debug("document " + url + " is already deployed, undeploy first if you wish to redeploy");
         return sdi;
      }
      
      // Install the files, get the deployment descriptor
      if (sdi.state == ServiceDeploymentInfo.EMPTY ) 
      {
         // This install everything from this URL and parses the XML dd contained in it
         // If the file contains classes these classes are added to the SDI 
         loadFiles(url);
      }
      
      // The deployment descriptor was loaded by deployLocalClasses
      if(sdi.dd != null)
      {
         // In case there is a dependent classpath defined parse it
         // This creates 
         parseXMLClasspath(url, sdi);
         
         //Copy local directory if local-directory element is present
         try
         {
            NodeList lds = sdi.dd.getElementsByTagName("local-directory");
            log.debug("about to copy " + lds.getLength() + " local directories");
            for (int i = 0; i< lds.getLength(); i++)
            {
               Element ld = (Element)lds.item(i);
               String path = ld.getAttribute("path");
               log.debug("about to copy local directory at " + path);
               File jbossHomeDir = new File(System.getProperty("jboss.system.home"));
               File localBaseDir = new File(jbossHomeDir, "db"+File.separator);
               //Get the url of the local copy from the classloader.
               URL localUrl = (URL)sdi.getClassUrls().get(0);
               log.debug("copying from " + localUrl.toString() + path);
               log.debug("copying to " + localBaseDir);
               
               inflateJar(localUrl, localBaseDir, path);
            } // end of for ()
         
         
         } catch (Exception e)
         {
            log.error("Problem copying local directory", e);
         } // end of try-catch
         
         // install the MBeans in this descriptor
         log.debug("addMBeans: url " + url);
         
         List mbeans = sdi.mbeans;
         mbeans.clear();
         
         NodeList nl = sdi.dd.getElementsByTagName("mbean");
         
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
               objectNameToSupplyingPackageMap.put(service, url);
            }
         }
         // create the services
         Iterator iterator = sdi.mbeans.iterator();
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
         Iterator iterator2 = sdi.mbeans.iterator();
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
      return sdi;
   }
   
   
   private ServiceDeploymentInfo loadFiles(URL url)
   throws DeploymentException
   {
      
      ServiceDeploymentInfo sdi = getSdi(url, true);
      
      if (sdi.state == ServiceDeploymentInfo.EMPTY)
      {
         try
         {
            log.debug("deploying document " + url);
            
            // A service can be as simple as a directory from which to load classes
            if(url.toString().endsWith("/"))  
            {
               sdi.addClassUrl(url);
               sdi.createClassLoader();//sets state also.
               //sdi.state = ServiceDeploymentInfo.CLASSESLOADED;
            }
            else // That "service" is a file
            {
               
               // Create a local copy of that File, the sdi keeps track of the copy directory
               File localFile = getLocalCopy(url, sdi);
               
               // unpack SARs
               extractPackages(localFile.toURL(), sdi);
               
               log.debug("jars from deployment: " + sdi.getClassUrls());
               log.debug("xml's from deployment: " + sdi.getXmlUrls());
               
               //OK, what are we trying to deploy? create the Deployement Descriptor
               
               //A plain xml file with mbean classpath elements and mbean config.
               if (localFile.getName().endsWith("service.xml"))
               {
                  sdi.dd = getDocument(localFile.toURL());
                  sdi.state = ServiceDeploymentInfo.CLASSESLOADED;
               }
               
               //a service archive with classes, jars, and META-INF/jboss-service.xml
               //configuration file
               else if (localFile.getName().endsWith(".sar"))
               {
                  sdi.createClassLoader();
                  docfound:
                  {
                     for (Iterator i = sdi.getXmlUrls().iterator(); i.hasNext();)
                     {
                        URL docUrl = (URL)i.next();
                        if (docUrl.getFile().endsWith("META-INF/jboss-service.xml"))
                        {
                           
                           sdi.dd = getDocument(docUrl);
                           
                           break docfound;
                        } // end of if ()
                     
                     } // end of for ()
                     throw new DeploymentException("No META-INF/jboss-service.xml found in alleged sar!");
                  }
                  //sdi.state = ServiceDeploymentInfo.CLASSESLOADED;
                  log.debug("got document jboss-service.xml from cl");
               }
               
               //Or maybe its just a jar to be put in the extensible classloader.
               else if(localFile.getName().endsWith(".jar")
                  || localFile.getName().endsWith(".zip"))
               {
                  sdi.createClassLoader();
                  //sdi.state = ServiceDeploymentInfo.CLASSESLOADED;
               }
               //Not for us.
               else
               {
                  throw new Exception("not a deployable file type");
               }
            }
         }
         catch (Exception ignored)
         {
            log.error("Problem deploying url " + url, ignored);
            throw new DeploymentException(ignored.getMessage());
         }
      
      } // end of if (EMPTY)
      
      return sdi;
   
   }
   
   private void parseXMLClasspath(URL url, ServiceDeploymentInfo sdi) throws DeploymentException
   {
      Document dd = sdi.dd;
      
      Collection classpath = sdi.classpath;
      classpath.clear();
      
      NodeList classpaths = dd.getElementsByTagName("classpath");
      for (int i = 0; i < classpaths.getLength(); i++)
      {
         Element classpathElement = (Element)classpaths.item(i);
         log.debug("found classpath " + classpath);
         
         //String codebase = System.getProperty("jboss.system.libraryDirectory");
         String codebase = "";
         String archives = "";
         
         //Does it specify a codebase?
         if (classpathElement != null)
         {
            
            log.debug("setting up classpath " + classpath);
            // Load the codebase
            codebase = classpathElement.getAttribute("codebase").trim();
            
            if ("".equals(codebase)) codebase = "lib/ext";
               
            // Do we have a relative codebase?
            if (!(codebase.startsWith("http:") || codebase.startsWith("file:"))) 
            {
               // put the jboss/system base in front of it
               codebase = System.getProperty("jboss.system.installationURL")+codebase;
            }
            //if codebase is ".", construct codebase from current url.
            if (".".equals(codebase))
            {
               //does this work with http???
               // marcf: for http we could just get the substring to the last "/"
               codebase = new File(url.getProtocol() + "://" + url.getFile()).getParent();
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
            //Load the archives
            archives = classpathElement.getAttribute("archives").trim();
            log.debug("archives are " + archives);
         }
         
         if (codebase.startsWith("file:") && archives.equals("*"))
         {
            try
            {
               File dir = new File(codebase.substring(5));
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
               throw new DeploymentException(e.getMessage());
            }
         }
         
         // We have an archive whatever the codebase go ahead and load the libraries
         else if (!archives.equals(""))
         {
            
            // We have a real codebase specified in xml
            // We add it to the classpath so we load files from it (http/file will work)
            if (!codebase.equals(""))
            {  
               try 
               {
                  // Add the codebase to the classpath
                  classpath.add( new URL(codebase));
               }
               catch (Exception e2) 
               {
                  log.error("Couldn't create URL for codebase "+codebase, e2);
                  throw new DeploymentException(e2.getMessage());
               
               }
            }
            
            // Still no codebase? safeguard
            if (codebase.equals("")) codebase = System.getProperty("jboss.system.libraryDirectory");
               
            if (archives.equals("*") || archives.equals("")) 
            {
               // Safeguard
               if (!codebase.startsWith("file:") && archives.equals("*")) throw new DeploymentException("No wildcard permitted in http deployment you must specify individual jars");
                  
               try
               {
                  File dir = new File(codebase.substring(5));
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
                  throw new DeploymentException(e.getMessage());
               }
            }
            
            
            else // A real archive listing (as opposed to wildcard)
            {
               StringTokenizer jars = new StringTokenizer(archives, ",");
               //iterate through the packages in archives
               while (jars.hasMoreTokens())
               {
                  // The format is simple codebase + jar
                  try { classpath.add(new URL(codebase +jars.nextToken().trim()));} 
                     
                  catch (MalformedURLException mfue) { log.error("couldn't resolve package reference: ", mfue);} // end of try-catch
               }
            }
         }
         
         else //codebase is empty and archives is empty but we did have a classpath entry
         {
            throw new DeploymentException("A classpath entry was declared but no codebase and no jars specified. Please fix jboss-service.xml in your configuration");
         }
      }
      
      //Ok, now we've found the list of urls we need... deploy their classes.
      Iterator jars = classpath.iterator();
      
      URL neededUrl = null;
      while (jars.hasNext())
      {
         try
         {
            
            neededUrl = (URL)jars.next();
            
            loadFiles(neededUrl);
            
            log.debug("deployed classes for " + neededUrl);
         
         
         } catch (DeploymentException e) {
            log.error("problem deploying classes for " + neededUrl, e);
            //put in list of failures TODO
         } // end of try-catch
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
   public void undeploy(URL url, Object localurlObject)
   throws MalformedURLException, IOException, DeploymentException
   {
      log.debug("undeploying document " + url);
      
      ServiceDeploymentInfo sdi = getSdi(url, false);
      
      //first of all, undeploy the mbeans.
      removeMBeans(url, sdi);
      
      // Those that provide classes must be removed
      // FIXME track the dependencies in the service libraries
      ServiceLibraries.getLibraries().removeClassLoader(sdi.removeClassLoader());
      
      //set the state - we've removed the Cl
      //delete the copied directories if possible.
      sdi.cleanup(getLog());
      
      //Hey, man, we're all cleaned up!
   }
   
   
   /**
   * MBeanRegistration interface. Get the mbean server.
   *
   * @param server                   Our mbean server.
   * @param name                     our proposed object name.
   * @return                         our actual object name
   * @exception java.lang.Exception  Thrown if we are supplied an invalid name.
   */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws java.lang.Exception
   {
      super.preRegister(server, name);
      log.debug("ServiceDeployer preregistered with mbean server");
      objectName = name == null ? new ObjectName(OBJECT_NAME) : name;
      
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
   public void postRegister(java.lang.Boolean registrationDone)
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
   
   protected Document getDocument(URL url)
   throws DeploymentException
   {
      try
      {
         DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         InputStream stream = url.openStream();
         InputSource is = new InputSource(stream);
         return parser.parse(is);
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
   
   
   
   private void removeMBeans(URL url, ServiceDeploymentInfo sdi) throws DeploymentException
   {
      log.debug("removeMBeans: url " + url);
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
         objectNameToSupplyingPackageMap.remove(name);
      }
      sdi.state = ServiceDeploymentInfo.CLASSESLOADED;
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
         return getServer().invoke(name, method, args, sig);
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
   
   
   private ServiceDeploymentInfo getSdi(URL url, boolean createIfMissing) throws DeploymentException
   {
      ServiceDeploymentInfo sdi = (ServiceDeploymentInfo)urlToServiceDeploymentInfoMap.get(url);
      if (sdi == null)
      {
         if (createIfMissing)
         {
            sdi = new ServiceDeploymentInfo(url);
            urlToServiceDeploymentInfoMap.put(url, sdi);
         } // end of if ()
         else
         {
            throw new DeploymentException(url + " is not deployed as expected");
         } // end of else
      }
      return sdi;
   }
}


