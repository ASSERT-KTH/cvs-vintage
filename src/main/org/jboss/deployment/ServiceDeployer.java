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
import java.util.LinkedList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;

import javax.management.ObjectName;
import javax.management.ReflectionException;
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
 * @author    <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author    <a href="mailtod_jencks@users.sourceforge.net">David Jencks</a>
 * @version   $Revision: 1.7 $ <p>
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
       extends ServiceMBeanSupport
       implements ServiceDeployerMBean
{

   // Attributes --------------------------------------------------------

   // each url can spawn a series of MLet classloaders that are specific to it and cycled
   private Map urlToClassLoadersSetMap;

   // each url can describe many Services, we keep the ObjectNames in here
   // order is important so we use a list
   private Map urlToServicesListMap;

   // To determine when we can remove a classloader, keep track of which urls reference it.
   private Map classLoaderToUrlsSetMap;

   // To keep track of when we must undeploy depending jsrs/ *-service.xml's
   private Map urlToPrimaryClassLoaderMap;

   //To keep track of services suspended when we undeploy a jsr
   private Map suspendedUrlDependencyMap;


   // each url is associated with an xml document
   private Map urlToDocumentMap;

   // each url specifies a number of services it is dependent on
   private Map urlToDependsSetMap;

   // maps mbeans to the urls that are dependent on them.
   private Map mbeanToURLSetMap;

   // JMX
   private MBeanServer server;

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
               return (filename.endsWith(".jsr")
                      || filename.endsWith(".sar")
                      || filename.endsWith("service.xml"));
            }
         };
   }

   /**
    * Determines if the urlString references a currently deployed package
    *
    * @param urlString                  url string for package
    * @return                           Whether the package is currently
    *      deployed.
    * @exception MalformedURLException  Thrown if a malformed url string is
    *      supplied.
    */
   public boolean isDeployed(String urlString)
          throws MalformedURLException
   {
      URL url = new URL(urlString);
      return urlToClassLoadersSetMap.containsKey(url);
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
   public void deploy(String url)
          throws MalformedURLException, IOException, DeploymentException
   {

      log.debug("deploying document " + url);

      if (isDeployed(url))
      {
         undeploy(url);
         log.debug("undeployed previous version of document " + url);
      }

      // The Document describing the service
      Document document = null;

      /**
       * First register the classloaders for this deployment If it is a jsr, the
       * jsr points to itself If it is a something-service.xml then it looks for
       * <classpath><codebase>http://bla.com (or file://bla.com)</codebase>
       * default is system library dir <archives>bla.jar, bla2.jar, bla3.jar
       * </archives>where bla is relative to codebase</classpath>
       */

      // Support for the new packaged format
      try
      {
         if (url.endsWith(".jsr")
                || url.endsWith(".sar"))
         {
            //use java.net.URLClassLoader here
            ClassLoader cl = new java.net.URLClassLoader(new URL[]{new URL(url)});
            document = getDocument("META-INF/jboss-service.xml", cl);
            log.debug("got document jboss-service.xml from cl");
         }

         //no mbeans to deploy for jars
         else if(url.endsWith(".jar")
                   || url.endsWith(".zip"))
         {
            document = null;
         }

         // We can deploy bare xml files as well
         else if (url.endsWith("service.xml"))
         {
            document = getDocument(url, null);
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

      Set dependsSet = new HashSet();

      if(document != null){
         
         // Support for depends tag
         List dependsBeans = new ArrayList();
   
         // Attempt to parse all depends tags first, catches all errors before we get too far.
         try {
            NodeList nl = document.getElementsByTagName("depends");
            for (int i = 0 ; i < nl.getLength() ; i++) 	
            {
               //get object name for dependent mbean
               Element dependsElement = (Element) nl.item(i);
               dependsBeans.add(parseObjectName(dependsElement));
            }
         } 
         catch(Exception e) 
         { 
            throw new DeploymentException("Error parsing depends elements.",e);
         }
   
         for (int i = 0 ; i < dependsBeans.size() ; i++) 	
         {
            ObjectName objectName = (ObjectName) dependsBeans.get(i);
   
            //check if mbean is registered (deployed)
            if(!server.isRegistered(objectName))
            {
               log.debug("Deployment of '"+url+"' will have to wait for mbean "+objectName);
   
               dependsSet.add(objectName);
               
               Set urlsForMBean = (Set) mbeanToURLSetMap.get(objectName);
               if(urlsForMBean == null)
               {
                  urlsForMBean = new HashSet();
                  mbeanToURLSetMap.put(objectName,urlsForMBean);
               }
   
               urlsForMBean.add(url);
            }
         }
      }

      urlToDocumentMap.put(url,document);

      LinkedList toDeploy = new LinkedList();

      //if we have no dependents we can deploy
      if(dependsSet.isEmpty())
      {
         toDeploy.addLast(url);
      }
      else
      {
         urlToDependsSetMap.put(url,dependsSet);   
      }

      //loop, continuing to deploy until all possible deployments are made
      while(!toDeploy.isEmpty())
      {
         url = (String) toDeploy.removeFirst();
         document = (Document) urlToDocumentMap.remove(url);

         List mbeanList = null;
         try
         {
            log.debug("Deploying '"+url+"'");
            mbeanList = doDeployment(url,document);
         }
         catch(Exception e)
         {
            log.error("Error during deployment of '"+url+"'.",e);
            mbeanList = new ArrayList();
         }

         Iterator deployedMBeans = mbeanList.iterator();

         //for each deployed mbean check for pending deployments waiting for it.
         while(deployedMBeans.hasNext())
         {
            ObjectName mbean = (ObjectName) deployedMBeans.next();
            Set urlsForMBean = (Set)mbeanToURLSetMap.remove(mbean);
            
            if(urlsForMBean != null)
            {
               //for each pending deployment remove mbean and see whether we can now deploy
               Iterator urls = urlsForMBean.iterator();
               while(urls.hasNext())
               {
                  Object nextURL = urls.next();
                  Set setToCheck = (Set) urlToDependsSetMap.get(nextURL);						
                  setToCheck.remove(mbean);
                  
                  if(setToCheck.isEmpty())
                  {
                     log.debug("Found available deployment "+nextURL+" to do.");
                     urlToDependsSetMap.remove(nextURL); //remove from map
                     toDeploy.addLast(nextURL);
                  }

               }
            }
         }// for each deployed mbean...

      }// while we still have deployments to do

   }


   private List doDeployment(String urlString, Document document)
          throws MalformedURLException, IOException, DeploymentException
   {
      //convert to canonical form - an URL object.
      URL url = new URL(urlString);

      // The set of classloaders for this url
      Set classLoaders = new HashSet();

      // The list of mbeans deployed by this call
      List deployedMBeans = new ArrayList();
      
      /**
       * First register the classloaders for this deployment If it is a jsr, the
       * jsr points to itself If it is a something-service.xml then it looks for
       * <classpath><codebase>http://bla.com (or file://bla.com)</codebase>
       * default is system library dir <archives>bla.jar, bla2.jar, bla3.jar
       * </archives>where bla is relative to codebase</classpath>
       */

      // Support for the new packaged format
      try
      {
         if (urlString.endsWith(".jsr")
                || urlString.endsWith(".sar")
                || urlString.endsWith(".jar")
                || urlString.endsWith(".zip"))
         {

            URLClassLoader cl = new URLClassLoader(new URL[]{url});
            log.debug("got classloader for archive " + url);
            urlToPrimaryClassLoaderMap.put(url, cl);
            log.debug("added classloader to urlToPrimaryClassLoaderMap");
            classLoaders.add(cl);
            log.debug("added classloader to set of cl to register on success");
         }
      }
      catch (Exception ignored)
      {
         log.error("Problem deploying url " + url + ", no valid service.xml file found.", ignored);
         throw new DeploymentException("No valid service.xml file found" + ignored.getMessage());
      }

      if (document != null)
      {
         log.debug("found *service.xml file for url " + url);
         // The service.xml file can define jar the classes it contains depend on
         // We should only have one codebase (or none at all)
         //And why is that??
         NodeList classpaths = document.getElementsByTagName("classpath");
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
                  codebase = new File(urlString).getParent();
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
                     log.debug("addding URLClassLoader for url " + u);
                     URLClassLoader cl0 = new URLClassLoader(new URL[]{u});
                     classLoaders.add(cl0);
                  }
               }
               catch (Exception e)
               {
                  e.printStackTrace();
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
                  String dependencyString = codebase + jar;
                  URL dependency = new URL(dependencyString);
                  //look to see if this package was undeployed by hand--
                  //if so we should not try to redeploy it automatically,
                  //we should wait till it is redeployed by hand.
                  log.debug("Looking for suspension dependencies for url : " + dependency);
                  Set dependents = (Set)suspendedUrlDependencyMap.get(dependency);
                  log.debug("Looking for suspension dependencies for url : " + dependency + ", returned " + dependents);
                  if ((dependents != null) && (dependents.size() > 0))
                  {
                     //We have to wait till it's redeployed
                     //Put in a request to be deployed ourselves.
                     if (!dependents.contains(urlString))
                     {
                        dependents.add(urlString);
                     }
                     //may need more cleanup...
                     urlToPrimaryClassLoaderMap.remove(url);
                     //we'll try again later...
                     return deployedMBeans;
                  }

                  //log.debug("adding URLClassLoader for url archive " + dependency);
                  //URLClassLoader cl1 = new URLClassLoader(new URL[]{dependency});
                  if (!isDeployed(dependencyString))
                  {
                     log.debug("recursively deploying " + dependency);
                     deploy(dependencyString);
                  }
                  //This won't work if we only put into primary if deployed by hand,
                  //not recursively
                  //It also doesn't work if we put service.xml files in the classpath
                  Object cl1 = urlToPrimaryClassLoaderMap.get(dependency);
                  if (cl1 != null)
                  {
                     classLoaders.add(cl1);
                  }
               }
            }

            else if (codebase.startsWith("http:"))
            {
               throw new DeploymentException("Loading from a http:// codebase with no jars specified. Please fix jboss-service.xml in your configuration");
            }
         }
         //end loop through classpaths

         // The libraries are loaded we can now load the mbeans
         List services = (List)urlToServicesListMap.get(url);
         if (services == null)
         {
            services = Collections.synchronizedList(new ArrayList());
            urlToServicesListMap.put(url, services);
         }
         NodeList nl = document.getElementsByTagName("mbean");
         for (int i = 0; i < nl.getLength(); i++)
         {
   
            Element mbean = (Element)nl.item(i);
   
            try
            {
               log.debug("deploying with ServiceController mbean " + mbean);
               ObjectName service = (ObjectName)server.invoke(
                     new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
                     "deploy",
                     new Object[]{mbean},
                     new String[]{"org.w3c.dom.Element"});
   
               // marcf: I don't think we should keep track and undeploy...
               //david jencks what do you mean by this???
               services.add(service);
               deployedMBeans.add(service);
            }
            catch (MBeanException mbe)
            {
               log.error("Mbean exception while creating mbean", mbe.getTargetException());
            }
            catch (RuntimeMBeanException rbe)
            {
               log.error("Runtime Mbean exception while creating mbean", rbe.getTargetException());
            }
            catch (MalformedObjectNameException mone)
            {
               log.error("MalformedObjectNameException  while creating mbean", mone);
            }
            catch (ReflectionException re)
            {
               log.error("ReflectionException while creating mbean", re);
            }
            catch (InstanceNotFoundException re)
            {
               log.error("InstanceNotFoundException while creating mbean", re);
            }
            catch (Exception e)
            {
               log.error("Exception while creating mbean", e);
            }
         }
   
         //init the mbeans in our package
         for (Iterator it = services.iterator(); it.hasNext(); )
         {
            ObjectName service = (ObjectName)it.next();
   
            try
            {
               server.invoke(
                     new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
                     "init",
                     new Object[]{service},
                     new String[]{"javax.management.ObjectName"});
            }
            catch (MBeanException mbe)
            {
               log.error("Mbean exception while creating mbean", mbe.getTargetException());
            }
            catch (RuntimeMBeanException rbe)
            {
               log.error("Runtime Mbean exception while creating mbean", rbe.getTargetException());
            }
            catch (MalformedObjectNameException mone)
            {
               log.error("MalformedObjectNameException  while creating mbean", mone);
            }
            catch (ReflectionException re)
            {
               log.error("ReflectionException while creating mbean", re);
            }
            catch (InstanceNotFoundException re)
            {
               log.error("InstanceNotFoundException while creating mbean", re);
            }
            catch (Exception e)
            {
               log.error("Exception while creating mbean", e);
            }
         }
   
         //iterate through services and start.
         for (Iterator it = services.iterator(); it.hasNext(); )
         {
            ObjectName service = (ObjectName)it.next();
   
            try
            {
               server.invoke(
                     new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
                     "start",
                     new Object[]{service},
                     new String[]{"javax.management.ObjectName"});
            }
            catch (MBeanException mbe)
            {
               log.error("Mbean exception while creating mbean", mbe.getTargetException());
            }
            catch (RuntimeMBeanException rbe)
            {
               log.error("Runtime Mbean exception while creating mbean", rbe.getTargetException());
            }
            catch (MalformedObjectNameException mone)
            {
               log.error("MalformedObjectNameException  while creating mbean", mone);
            }
            catch (ReflectionException re)
            {
               log.error("ReflectionException while creating mbean", re);
            }
            catch (InstanceNotFoundException re)
            {
               log.error("InstanceNotFoundException while creating mbean", re);
            }
            catch (Exception e)
            {
               log.error("Exception while creating mbean", e);
            }
         }

      }// if document != null
         
      //We loaded Ok, register all our classloaders
      registerClassLoaders(url, classLoaders);

      //Ok, now we've loaded this jsr.  Are other packages waiting for us?
      Set dependSet = (Set)suspendedUrlDependencyMap.remove(url);
      if (dependSet != null)
      {
         Iterator dependencies = dependSet.iterator();
         while (dependencies.hasNext())
         {
            String dependent = (String)dependencies.next();
            deployedMBeans.addAll( doDeployment(dependent, (Document)urlToDocumentMap.get(dependent)) );
            //don't care about removing from dependSet, we're throwing it away
         }
      }
      
      return deployedMBeans;
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
   public void undeploy(String urlString)
          throws MalformedURLException, IOException, DeploymentException
   {
      log.debug("undeploying document " + urlString);
      URL url = new URL(urlString);

      List services = (List)urlToServicesListMap.remove(url);

      if (services != null)
      {

         //stop services
         for (Iterator iterator = services.iterator(); iterator.hasNext(); )
         {
            ObjectName name = (ObjectName)iterator.next();

            try
            {
               server.invoke(
                     new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
                     "stop",
                     new Object[]{name},
                     new String[]{"javax.management.ObjectName"});
            }
            catch (Exception e)
            {
               log.error("exception stopping mbean " + name, e);
            }
         }

         //destroy services
         for (Iterator iterator = services.iterator(); iterator.hasNext(); )
         {
            ObjectName name = (ObjectName)iterator.next();

            try
            {
               server.invoke(
                     new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
                     "destroy",
                     new Object[]{name},
                     new String[]{"javax.management.ObjectName"});
            }
            catch (Exception e)
            {
               log.error("exception stopping mbean " + name, e);
            }
         }

         Iterator iterator = services.iterator();
         while (iterator.hasNext())
         {
            ObjectName name = (ObjectName)iterator.next();
            log.debug("undeploying mbean " + name);
            try
            {
               server.invoke(
                     new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
                     "undeploy",
                     new Object[]{name},
                     new String[]{"javax.management.ObjectName"});
            }
            catch (Exception e)
            {
               log.error("problem undeploying mbean " + name, e);
            }
         }
      }

      //now remove the classloaders we set up
      Set classLoaderSet = (Set)urlToClassLoadersSetMap.remove(url);
      if (classLoaderSet != null)
      {

         Iterator iterator = classLoaderSet.iterator();
         while (iterator.hasNext())
         {
            //Need to see if we are the only user of this classloader- if not, don't remove.
            URLClassLoader cl = (URLClassLoader)iterator.next();
            log.debug("considering undeploying classloader " + cl);
            HashSet urls = (HashSet)classLoaderToUrlsSetMap.get(cl);
            if (urls == null)
            {
               throw new DeploymentException("No urls for a classloader from url!! url: " + url + ", classloader: " + cl);
            }
            log.debug("set of urls for classloader: " + urls);
            urls.remove(url);
            //if this is a primary cl, we force undeploy of depending urls
            if (cl.equals(urlToPrimaryClassLoaderMap.get(url)))
            {
               urlToPrimaryClassLoaderMap.remove(url);
               Set suspended = (Set)suspendedUrlDependencyMap.get(url);
               log.debug("making suspended set for url " + url);
               if (suspended == null)
               {
                  suspended = new HashSet();
                  suspendedUrlDependencyMap.put(url, suspended);
               }
               Iterator dependencies = ((Set)urls.clone()).iterator();
               while (dependencies.hasNext())
               {
                  URL dependentUrl = (URL)dependencies.next();
                  log.debug("undeploying dependent url: " + dependentUrl);
                  suspended.add(dependentUrl);
                  undeploy(dependentUrl.toString());
               }
            }

            if (urls.size() == 0)
            {
               //last one, we can remove it if we auto-deployed it
               log.debug("actually undeploying classloader " + cl);
               classLoaderToUrlsSetMap.remove(cl);
               try
               {
                  ServiceLibraries.getLibraries().removeClassLoader(cl);
               }
               catch (Exception e)
               {
                  log.error("problem removing classloader " + cl, e);
               }
            }
         }
      }
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

      log.debug("About to load the CLassPath");
      this.server = server;

      return name == null ? new ObjectName(OBJECT_NAME) : name;
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

         //Encapsulate with a ServiceClassLoader
         urlToClassLoadersSetMap = Collections.synchronizedMap(new HashMap());
         urlToServicesListMap = Collections.synchronizedMap(new HashMap());
         classLoaderToUrlsSetMap = Collections.synchronizedMap(new HashMap());
         urlToPrimaryClassLoaderMap = Collections.synchronizedMap(new HashMap());
         suspendedUrlDependencyMap = Collections.synchronizedMap(new HashMap());

         // depends maps
         urlToDocumentMap = Collections.synchronizedMap(new HashMap());
         urlToDependsSetMap = Collections.synchronizedMap(new HashMap());
         mbeanToURLSetMap = Collections.synchronizedMap(new HashMap());


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

         doDeployment(urlString,document);
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

   private void registerClassLoaders(URL url, Set classLoaders)
   {

      try
      {
         /*
          * log.debug("=====================");
          * log.debug("registering classloaders for url: " + url);
          * Iterator cls0 = classLoaders.iterator();
          * while (cls0.hasNext())
          * {
          * log.debug("classloader for url: " + ((URLClassLoader)cls0.next()).getURL());
          * }
          * log.debug("=====================");
          */
         urlToClassLoadersSetMap.put(url, classLoaders);
         //need a classloader to url set map too, to keep track of
         //when we can unload a classloader.
         Iterator cls = classLoaders.iterator();
         while (cls.hasNext())
         {
            URLClassLoader cl = (URLClassLoader)cls.next();
            Set urls = (Set)classLoaderToUrlsSetMap.get(cl);
            log.debug("urls for classloader " + cl + ", url: " + cl.getURL() + " are " + urls);
            if (urls == null)
            {
               log.debug("creating new urls for classLoaderToUrlSetMap, cl: " + cl);
               urls = new HashSet();
               classLoaderToUrlsSetMap.put(cl, urls);
            }
            urls.add(url);
            log.debug("added to classLoaderToUrlsSetMap cl: " + cl + ", url: " + url);
         }

      }
      catch (Exception e)
      {
         log.error("Problem registering classloader for url " + url, e);
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
         ("Depends element must have a value.");
      }
      return new ObjectName(name);
   }

}

