/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;


import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.loading.MLet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jboss.system.MBeanClassLoader;
import org.jboss.system.Service;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.URLClassLoader;
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
 * @version   $Revision: 1.13 $ <p>
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
 */
public class ServiceDeployer
       extends DeployerMBeanSupport
       implements ServiceDeployerMBean
{
   // Attributes --------------------------------------------------------
   private ObjectName objectName;

   //Find all the deployment info for a url
   private final Map urlToSarDeploymentInfoMap = new HashMap();

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
    * Deploys a package identified by a url string. This undeploys a previous
    * version if necessary, checks for a classpath element with codebase and
    * archives attributes to locate packages this package depends on, deploys
    * them (if not suspended by an undeploy, adds this package to the
    * ServiceLibraries classpath, and loads the mbeans specified.
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
      SarDeploymentInfo sdi = getSdi(url, true);
      if (sdi.state == MBEANSLOADED)
      {
         log.debug("document " + url + " is already deployed, undeploy first if you wish to redeploy");
         return sdi;
      }

      if (sdi.state == EMPTY || sdi.state == GHOST) 
      {
         deployLocalClasses(url, null, true);              
         //Let others waiting on our classes finish deploying.
         resolveSuspensions(url, sdi);
      } // end of if ()
      

      if(sdi.dd != null){
         boolean suspended = deployNeededPackages(url, sdi);

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
                URL localUrl = sdi.classloader.getURLs()[0];
                log.debug("copying from " + localUrl.toString() + path);
                log.debug("copying to " + localBaseDir);

                inflateJar(localUrl, localBaseDir, path);
            } // end of for ()
            

         } catch (Exception e) 
         {
             log.error("Problem copying local directory", e);
         } // end of try-catch
         //if we are suspended, we must wait till all classes are available.
         if (!suspended) 
         {
            addMBeans(url, sdi);
         } // end of if ()

      }
      return sdi;
   }

   private void resolveSuspensions(URL url, SarDeploymentInfo sdi) 
       throws DeploymentException
   {
      Iterator suspensions = (sdi.weSupplyClassesTo).iterator();
      while (suspensions.hasNext()) 
      {
         //check if all suspension dependencies resolved and if so call
         //addMBeans on it.
         URL suspendedUrl = (URL) suspensions.next();
         SarDeploymentInfo suspendedSdi = getSdi(suspendedUrl, false);
         if (suspendedSdi.state != SUSPENDED) 
         {
            throw new DeploymentException("Depending module " + suspendedUrl + " is not marked as suspended when deploying url: " + url);
         } // end of if ()
         
         boolean canDeploySuspended = true;
         Iterator others = suspendedSdi.weNeedClassesFrom.iterator();
         while (others.hasNext()) 
         {
            URL otherUrl = (URL)others.next();
            SarDeploymentInfo otherSdi = getSdi(otherUrl, false);
            if (otherUrl != url && otherSdi.state != LOCALCLASSESLOADED
                && otherSdi.state != CLASSESLOADED
                && otherSdi.state != MBEANSLOADED) 
            {
               canDeploySuspended = false;
               break;
            } // end of if ()

         } // end of while ()
         if (canDeploySuspended) 
         {
            addMBeans(suspendedUrl, suspendedSdi);        
         } // end of if ()
      } // end of while ()
   }
     

   private SarDeploymentInfo deployLocalClasses(URL url, URL needsme, boolean reloadSuspended) 
        throws DeploymentException
   {
      SarDeploymentInfo sdi = getSdi(url, false);
      
      if (reloadSuspended || sdi.state == EMPTY || sdi.state == GHOST) 
      {
         try 
         {
            log.debug("deploying document " + url);
            File localCopy = getLocalCopy(url, null);
            URL localUrl = localCopy.toURL();
            String localName = localCopy.getName();//just the filename, no path
            ArrayList jars = new ArrayList();
            ArrayList xmls = new ArrayList();
            File unpackedDir = recursiveUnpack(localUrl, jars, xmls);


            /**
             * First register the classloaders for this deployment If it is a jsr, the
             * jsr points to itself If it is a something-service.xml then it looks for
             * <classpath><codebase>http://bla.com (or file://bla.com)</codebase>
             * default is system library dir <archives>bla.jar, bla2.jar, bla3.jar
             * </archives>where bla is relative to codebase</classpath>
             */
         // Support for the new packaged format
            if (localName.endsWith("service.xml"))
            {
               sdi.dd = getDocument(localUrl.toString(), null);
               sdi.state = LOCALCLASSESLOADED;
            }
            else if (localName.endsWith(".sar"))
            {
               URLClassLoader cl = new URLClassLoader(new URL[] {localUrl}, url);
               sdi.dd = getDocument("META-INF/jboss-service.xml", cl);
               sdi.classloader = cl;
               sdi.state = LOCALCLASSESLOADED;
               log.debug("got document jboss-service.xml from cl");
            }

            //no mbeans to deploy for jars
            else if(localName.endsWith(".jar")
                   || localName.endsWith(".zip"))
            {
               URLClassLoader cl = new URLClassLoader(new URL[] {localUrl}, url);
               sdi.classloader = cl;
               sdi.state = CLASSESLOADED;
            }

            else
            {
               throw new Exception("not a deployable file type");
            }
          
         }
         catch (Exception ignored)
         {
            log.error("Problem deploying url " + url + ", no valid service.xml file found.", ignored);
            throw new DeploymentException("No valid service.xml file found" + ignored.getMessage());
         }

      } // end of if (EMPTY)

      //in any case, we may need to add dependency info
      if ((needsme != null) && !sdi.weSupplyClassesTo.contains(needsme))
      {
         sdi.weSupplyClassesTo.add(needsme);   
      } // end of if ()
      
      return sdi;

   }

   private boolean deployNeededPackages(URL url, SarDeploymentInfo sdi) throws DeploymentException
   {
      Document dd = sdi.dd;
      Collection weNeedClassesFrom = sdi.weNeedClassesFrom;
      weNeedClassesFrom.clear();
         NodeList classpaths = dd.getElementsByTagName("classpath");
         for (int i = 0; i < classpaths.getLength(); i++)
         {
            Element classpath = (Element)classpaths.item(i);
            log.debug("found classpath " + classpath);
            String codebase = "";
            String archives = "";

            //Does it specify a codebase?
            if (classpath != null)
            {
               log.debug("setting up classpath " + classpath);
               // Load the codebase
               codebase = classpath.getAttribute("codebase").trim();

               //if codebase is ".", construct codebase from current url.
               if (".".equals(codebase))
               {
                  //does this work with http???
                  codebase = new File(url.getProtocol() + "://" + url.getFile()).getParent();
               }

               // Let's make sure the formatting of the codebase ends with the /
               if (codebase.startsWith("file:") && !codebase.endsWith(File.separator))
               {
                  codebase += File.separator;
               }
               else if (codebase.startsWith("http:") && !codebase.endsWith("/"))
               {
                  codebase += "/";
               }
               log.debug("codebase is " + codebase);
               //Load the archives
               archives = classpath.getAttribute("archives").trim();
               log.debug("archives are " + archives);
            }

            if (codebase.startsWith("file:") && archives.equals(""))
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
                     File jar = jars[j];
                     URL u = jar.getCanonicalFile().toURL();
                     if (!weNeedClassesFrom.contains(u)) {
                        weNeedClassesFrom.add(u);
                     } // end of if ()
                  }
               }
               catch (Exception e)
               {
                  log.error("problem listing files in directory", e);
                  throw new DeploymentException(e.getMessage());
               }
            }

            // Still no codebase? get the system default
            else if (codebase.equals(""))
            {
               codebase = System.getProperty("jboss.system.libraryDirectory");
            }

            // We have an archive whatever the codebase go ahead and load the libraries
            if (!archives.equals(""))
            {

               StringTokenizer st = new StringTokenizer(archives, ",");
               //iterate through the packages in archives
               while (st.hasMoreTokens())
               {
                  String jar = st.nextToken().trim();
                  String urlString = codebase + jar;
                  try 
                  {
                     URL u = new URL(urlString);
                     if (!weNeedClassesFrom.contains(u)) 
                     {
                        weNeedClassesFrom.add(u);
                     } // end of if ()
                  } catch (MalformedURLException mfue) 
                  {
                     log.error("couldn't resolve package reference: ", mfue);
                  } // end of try-catch
              }
            }

            else if (codebase.startsWith("http:"))
            {
               throw new DeploymentException("Loading from a http:// codebase with no jars specified. Please fix jboss-service.xml in your configuration");
            }
         }
         //Ok, now we've found the list of urls we need... deploy their classes.
         Iterator jars = weNeedClassesFrom.iterator();
         boolean suspended = false;
         URL neededUrl = null;
         while (jars.hasNext())
         {
             try 
             {
                neededUrl = (URL)jars.next();
                SarDeploymentInfo jarSdi = getSdi(neededUrl, true);
                //find out if any of these were undeployed... if so we can't deploy them nor our mbeans
                if (jarSdi.state == GHOST) {
                   suspended = true;                    
                   log.debug("did not deploy classes for " + neededUrl + ", it's a ghost, we are suspended"); 
                } // end of if ()
                else 
                {
                   deployLocalClasses(neededUrl, url, false);          
                   log.debug("deployed classes for " + neededUrl); 

                } // end of else
                
             } catch (DeploymentException e) {
                log.error("problem deploying classes for " + neededUrl, e);
                 //put in list of failures TODO
             } // end of try-catch
             
             
         } // end of while ()
         return suspended;

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
      log.debug("undeploying document " + url, new Exception("trace"));

      SarDeploymentInfo sdi = getSdi(url, false);
      
      //first of all, undeploy the depending (via depends tag) packages' mbeans and our mbeans.
      removeMBeans(url, sdi);

      //Now undeploy mbeans from packages we supply classes to.
      //Mark them suspended.. they are waiting for our classes to come back.
      Iterator suppliedToPackages = sdi.weSupplyClassesTo.iterator();
      while (suppliedToPackages.hasNext()) 
      {
         URL suppliedToPackage = (URL)suppliedToPackages.next();
         SarDeploymentInfo suppliedToSdi = getSdi(suppliedToPackage, false);
         if (suppliedToSdi.state == MBEANSLOADED) 
         {
            removeMBeans(suppliedToPackage, suppliedToSdi);
            suppliedToSdi.state = SUSPENDED;
         } // end of if ()
         
      } // end of while ()

      //Now tell packages we need classes from we are leaving: 
      //if they are ghosts (undeployed but remembering we need them)
      //and we are the last dependency, we can forget about them completely.
      //if they are not explicitly deployed (use parents isDeployed), 
      //and we are the last dependency, we can undeploy them also.
      Iterator weNeedClassesFrom = sdi.weNeedClassesFrom.iterator();
      while (weNeedClassesFrom.hasNext()) 
      {
         URL packageWeUse = (URL)weNeedClassesFrom.next();
         SarDeploymentInfo packageWeUseInfo = getSdi(packageWeUse, false);
         packageWeUseInfo.weSupplyClassesTo.remove(url);
         //if we're the last dependency for that package...
         if (packageWeUseInfo.weSupplyClassesTo.isEmpty()) 
         {
            //and it was undeployed
            if (packageWeUseInfo.state == GHOST) 
            {
               //bye bye
               urlToSarDeploymentInfoMap.remove(packageWeUse);
            } // end of if ()
            //or if it was implicitly deployed through a classpath
            else if (!isDeployed(packageWeUse.toString())) //getFile???
            {
               //bye bye
               undeploy(packageWeUse, null);
            } // end of if ()
         } // end of if ()
         
         
      } // end of while ()
      //Ok, we're done with upwards and downwards dependencies: 
      //we can decide if we're a ghost, and remove our classloader, and maybe ourselves.
      ServiceLibraries.getLibraries().removeClassLoader(sdi.classloader);

      sdi.classloader = null;
      if (sdi.weSupplyClassesTo.isEmpty()) 
      {
         //no one is using us, we can disappear without a trace
         urlToSarDeploymentInfoMap.remove(url);
      } // end of if ()
      else 
      {
         //someone is still suspended on us, we turn into a ghost so they
         //can be deployed if we are redeployed.
         sdi.state = GHOST;         
      } // end of else
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
         //super.startService();//set up the deploy temp directory
         getServer().invoke(getServiceControllerName(),
                            "registerAndStartService",
                            new Object[] {objectName, null},
                            new String[] {"javax.management.ObjectName", "java.lang.String"});
         //Initialize the libraries for the server by default we add the libraries in lib/services
         // and client
         String urlString = System.getProperty("jboss.system.configurationDirectory") + "jboss-service.xml";
         Document document = null;

         try
         {
            document = getDocument(urlString, null);
         }
         catch (Exception e)
         {
            log.error("problem getting jboss-service.xml.", e);
            // for legacy reasons try jboss.jcml
            urlString = System.getProperty("jboss.system.configurationDirectory") + "jboss.jcml";
            document = getDocument(urlString, null);

         }

         deploy(new URL(urlString));
      }
      catch (Exception e)
      {
         log.error("Problem postregistering ServiceDeployer", e);
      }
   }

   private Document getDocument(String urlString, ClassLoader cl)
          throws DeploymentException, MalformedURLException, IOException
   {
      // Define the input stream to the configuration file
      InputStream input = null;

      // Try the contextClassLoader
      if (cl == null)
      {
         input = Thread.currentThread().getContextClassLoader().getResourceAsStream(urlString);
      }
      else
      {
         input = cl.getResourceAsStream(urlString);
      }

      //If still no document, try to interpret the url as an absolute URL
      if (input == null)
      {

         // Try to understand the URL litterally
         input = (new URL(urlString)).openStream();
      }

      //Load the configuration with a buffered reader
      StringBuffer sbufData = new StringBuffer();
      BufferedReader br = new BufferedReader(new InputStreamReader(input));

      //String sTmp;
      char[] buffer = new char[1024];
      int read;
      try
      {
         while ((read = br.read(buffer)) > 0)
         {
            sbufData.append(buffer, 0, read);
         }
      }
      finally
      {
         br.close();
      }

      try
      {
         // Parse XML
         DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();

         return parser.parse(new InputSource(new StringReader(sbufData.toString())));
      }
      catch (SAXException e)
      {
         throw new DeploymentException(e.getMessage());
      }
      catch (ParserConfigurationException pce)
      {
         throw new DeploymentException(pce.getMessage());
      }

   }

   // Private --------------------------------------------------------
   private void addMBeans(URL url, SarDeploymentInfo sdi) throws DeploymentException
   {
      log.debug("addMBeans: url " + url);
      List mbeans = sdi.mbeans;
      mbeans.clear();
      NodeList nl = sdi.dd.getElementsByTagName("mbean");
      for (int i = 0; i < nl.getLength(); i++)
      {

         Element mbean = (Element)nl.item(i);
         log.debug("deploying with ServiceController mbean " + mbean);
         ObjectName service = (ObjectName)invoke(getServiceControllerName(),
                                                 "deploy",
                                                 new Object[]{mbean},
                                                 new String[]{"org.w3c.dom.Element"});
         // marcf: I don't think we should keep track and undeploy...
         //david jencks what do you mean by this???
         if (service != null)
         {
            mbeans.add(service);
            objectNameToSupplyingPackageMap.put(service, url);
         }
      }
      sdi.state = MBEANSLOADED;
   }


   private void removeMBeans(URL url, SarDeploymentInfo sdi) throws DeploymentException
   {
      log.debug("removeMBeans: url " + url);
      List services = sdi.mbeans;
      int lastService = services.size();
      //stop services in reverse order.
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("undeploying mbean " + name);
         invoke(getServiceControllerName(),
                 "undeploy",
                 new Object[] {name},
                 new String[] {"javax.management.ObjectName"});
         //we don't supply it any more, maybe someone else will later.
         objectNameToSupplyingPackageMap.remove(name);
      }
      sdi.state = CLASSESLOADED;
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
         ("Depends element must have a value.");
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


   private SarDeploymentInfo getSdi(URL url, boolean createIfMissing) throws DeploymentException
   {
      SarDeploymentInfo sdi = (SarDeploymentInfo)urlToSarDeploymentInfoMap.get(url);
      if (sdi == null)
      {
         if (createIfMissing)
         {
            sdi = new SarDeploymentInfo();
            urlToSarDeploymentInfoMap.put(url, sdi);          
         } // end of if ()
         else 
         {
            throw new DeploymentException(url + " is not deployed as expected");
         } // end of else
      }
      return sdi;
   }

   private static final int EMPTY = 0;
   private static final int LOCALCLASSESLOADED = 1;
   private static final int CLASSESLOADED = 2;
   private static final int MBEANSLOADED = 3;
   private static final int SUSPENDED = 4;
   private static final int GHOST = 5; //undeployed but packages are suspended on us.


   private static class SarDeploymentInfo
   {
      URLClassLoader classloader;
      Collection weNeedClassesFrom = new ArrayList();
      Collection weSupplyClassesTo = new ArrayList();
      List mbeans = new ArrayList();
      Document dd;
      int state = EMPTY;
   }


}

