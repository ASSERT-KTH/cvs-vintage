/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.jboss.logging.Log;
import org.jboss.util.MBeanProxy;
import org.jboss.util.ServiceMBeanSupport;

import org.jboss.metadata.XmlFileLoader;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.ContainerFactoryMBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** J2eeDeployer allows to deploy single EJB.jars as well as Web.wars
*  (if a Servlet Container is present) or even Application.ears. <br>
*  The deployment is done by determining the file type of the given url.
*  The file must be a valid zip file or a directory and must contain either a META-INF/ejb-jar.xml
*  or META-INF/application.xml or a WEB-INF/web.xml file.
*  Depending on the file type, the whole file (EJBs, WARs)
*  or only the relevant packages (EAR) becoming downloaded. <br>
*  <i> replacing alternative DDs and validation is not yet implementet! </i>
*  The uploaded files are getting passed through to the responsible deployer
*  (ContainerFactory for jBoss and EmbededTomcatService for Tomcat).
*
*   @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
*   @version $Revision: 1.10 $
*/
public class J2eeDeployer 
extends ServiceMBeanSupport
implements J2eeDeployerMBean
{
   // Constants -----------------------------------------------------
   public File DEPLOYMENT_DIR = null;//"/home/deployment"; // default? MUST BE ABSOLUTE PATH!!!
   public String CONFIG = "deployment.cfg";   
   
   // Attributes ----------------------------------------------------
   // my server to lookup for the special deployers
   MBeanServer server;
   
   // names of the specials deployers
   ObjectName jarDeployer;
   ObjectName warDeployer;
   
   String jarDeployerName;
   String warDeployerName;
   
   // The logger for this service
   Log log = new Log(getName());
   
   
   // Static --------------------------------------------------------
   /** only for testing...*/
   public static void main (String[] _args) throws Exception
   {
      new J2eeDeployer ("", "EJB:service=ContainerFactory", ":service=EmbeddedTomcat").deploy (_args[0]);
   }
   
   // Constructors --------------------------------------------------
   /** */
   public J2eeDeployer (String _deployDir, String jarDeployerName, String warDeployerName)
   {
      DEPLOYMENT_DIR = new File (_deployDir);
      
      this.jarDeployerName = jarDeployerName;
      this.warDeployerName = warDeployerName;
   
   }
   
   
   // Public --------------------------------------------------------
   /** Deploys the given URL independent if it is a EJB.jar, Web.war
   *   or Application.ear. In case of already deployed, it performes a
   *   redeploy.
   *   @param _url the url (file or http) to the archiv to deploy
   *   @throws MalformedURLException in case of a malformed url
   *   @throws J2eeDeploymentException if something went wrong...
   *   @throws IOException if trouble while file download occurs
   */
   public void deploy (String _url) throws MalformedURLException, IOException, J2eeDeploymentException
   {
      URL url = new URL (_url);
      
      // undeploy first if it is a redeploy
      try
      {
         undeploy (_url);
      }
      catch (Exception _e)
      {}
      
      // now try to deploy
      log.log ("Deploy J2EE application: " + _url);
      
      Deployment d = null;
      try
      {
         d = installApplication (url);
         startApplication (d);
         
         log.log ("J2EE application: " + _url + " is deployed.");
      } 
      catch (Exception _e)
      {
         if (_e instanceof J2eeDeploymentException)
         {
            if (d != null) // start failed...
               stopApplication (d);
            
            uninstallApplication (getAppName (url));
            throw (J2eeDeploymentException)_e;
         }
         else if (_e instanceof IOException)
         {
            // some download failed
            uninstallApplication (getAppName (url));
            throw (IOException)_e;
         }
         else
         {
            // Runtime Exception - shouldnt happen
            log.exception (_e);
            uninstallApplication (getAppName (url));
            throw new J2eeDeploymentException ("Fatal error: "+_e.toString ());
         }
      }
   }
   
   /** Undeploys the given URL (if it is deployed).
   *   Actually only the file name is of interest, so it dont has to be
   *   an URL to be undeployed, the file name is ok as well.
   *   @param _url the url to to undeploy
   *   @throws MalformedURLException in case of a malformed url
   *   @throws J2eeDeploymentException if something went wrong (but should have removed all files)
   *   @throws IOException if file removement fails
   */
   public void undeploy (String _app) throws IOException, J2eeDeploymentException
   {
      String name;
      try
      {
         name = getAppName (new URL (_app));
      }
      catch (MalformedURLException _e)
      {
         // must be only the name
         name = _app;
      }
      
      Deployment d = null;
      File f = new File (DEPLOYMENT_DIR + File.separator + name);
      if (f.exists())
      {
         try
         {
            d = loadConfig (name);
            stopApplication (d);
         }
         catch (IOException _ioe)
         {  // thrown by the load config
            throw _ioe;
         }
         catch (J2eeDeploymentException _e)
         {
            throw _e;
         }
         finally
         {
            uninstallApplication (name);
         }
      
      }
      else
         throw new J2eeDeploymentException ("The application \""+name+"\" has not been deployed.");
   }
   
   /** Checks if the given URL is currently deployed or not.
   *   Actually only the file name is of interest, so it dont has to be
   *   an URL to be undeployed, the file name is ok as well.
   *   @param _url the url to to check
   *   @return true if _url is deployed
   *   @throws MalformedURLException in case of a malformed url
   *   @throws J2eeDeploymentException if the app seems to be deployed, but some of its modules
   *   are not.
   */
   public boolean isDeployed (String _url) throws MalformedURLException, J2eeDeploymentException
   {
      // get Application name
      String name;
      try
      {
         name = getAppName (new URL (_url));
      }
      catch (MalformedURLException _e)
      {
         // must be only the name
         name = _url;
      }
      
      Deployment d = null;
      File f = new File (DEPLOYMENT_DIR + File.separator + name);
      if (f.exists())
      {
         try
         {
            return checkApplication (loadConfig (name));          
         } 
         catch (IOException e)
         {
            // no config found
            throw new J2eeDeploymentException ("The application \""+name+"\" seems to be installed, "+
               "but cant read "+CONFIG+" file!? Cant handle...");
         }
      }
      else
         return false;
   
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   public String getName()
   {
      return "J2EE Deployer";
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return new ObjectName(OBJECT_NAME);
   }
   
   /** */
   protected void initService()
   throws Exception
   {
      
      //check if the deployment dir was set meaningful
      if (!DEPLOYMENT_DIR.exists () &&
          !DEPLOYMENT_DIR.mkdirs ())
         throw new IOException ("Temporary directory \""+DEPLOYMENT_DIR.getCanonicalPath ()+"\" does not exist!");
      
      // Save JMX name of the deployers
      jarDeployer = new ObjectName(jarDeployerName);
      warDeployer= new ObjectName(warDeployerName);
   }
   
   /** */
   protected void startService()
   throws Exception
   {
      if (!warDeployerAvailable ())
         log.log ("No web container found - only EJB deployment available...");
         
      // clean up the deployment directory since on some Windowz the file removement
      // during runtime doesnt work...
      log.log("Cleaning up deployment directory "+DEPLOYMENT_DIR.toURL());
      File[] files =  DEPLOYMENT_DIR.listFiles();
      for (int i = 0, l = files.length; i<l; ++i)
         try
         {
            URLWizzard.deleteTree(files[i].toURL());
         }
         catch (IOException _ioe)
         {
            log.log("Could not remove file tree: "+files[i].toURL().toString());
         }
   }
   
   
   /** undeploys all deployments */
   protected void stopService()
   {
      log.log ("Undeploying all applications.");
      
      File[] files =  DEPLOYMENT_DIR.listFiles();
      int count = 0;
      for (int i = 0, l = files.length; i<l; ++i)
      {
         if (files[i].isDirectory ())
         {
            try
            {
               Deployment d = loadConfig (files[i].getName ());
               stopApplication (d);
            }   
            catch (IOException _ioe)
            {  // thrown by the load config
               //throw _ioe; 
               log.exception(_ioe);
            }
            catch (J2eeDeploymentException _e)
            {
               //throw _e;               
               log.exception(_e);
            }
            finally 
            {
               try {
                  uninstallApplication (files[i].getName ());
               } catch (IOException _ioe)
               {log.exception(_ioe);}
            }
            ++count;
         }
      }
      log.log ("Undeployed "+count+" applications.");
   }
   
   // Private -------------------------------------------------------
   
   /** determines deployment type and installs (downloads) all packages needed 
   *   by the given deployment of the given deployment. <br>
   *   This means download the needed packages do some validation...
   *   <i> Validation and do some other things is not yet implemented </i>
   *   @param _downloadUrl the url that points to the app to install
   *   @throws IOException if the download fails
   *   @throws J2eeDeploymentException if the given package is somehow inconsistent
   */
   Deployment installApplication (URL _downloadUrl) throws IOException, J2eeDeploymentException
   {
      // because of the URLClassLoader problem (doesnt notice when jar content changed)
      // we first make a local copy of the file
      boolean directory = false;
      URL localCopy = null;
      
      // determine if directory or not 
      // directories are only local supported
      if (_downloadUrl.getProtocol().equals ("file") && 
         new File (_downloadUrl.getFile ()).isDirectory ())
      {
         directory = true;
         localCopy = URLWizzard.downloadAndPackTemporary (_downloadUrl, DEPLOYMENT_DIR.toURL (), "copy", ".zip");
      }
      else
         localCopy = URLWizzard.downloadTemporary (_downloadUrl, DEPLOYMENT_DIR.toURL (), "copy", ".zip");
      
      // determine the file type by trying to access one of its possible descriptors
      Element root = null;
      URLClassLoader cl = new URLClassLoader (new URL[] {localCopy});
      String[] files = {"META-INF/ejb-jar.xml", "META-INF/application.xml", "WEB-INF/web.xml"};
      
      for (int i = 0; i < files.length && root == null; ++i)
      {
         try {
            root = XmlFileLoader.getDocument (cl.getResourceAsStream (files[i])).getDocumentElement ();
         } catch (Exception _e) {}
      }
      
      // dont need it anymore...
      cl = null;
      try
      {
         URLWizzard.deleteTree(localCopy);
      } 
      catch (IOException _ioe)
      {
         // dont abort just give a note...
         log.log ("Could not delete temporary file: "+localCopy.getFile ());
      }
      
      if (root == null)
      {
         // no descriptor was found...
         throw new J2eeDeploymentException ("No valid deployment descriptor was found within this URL: "+
            _downloadUrl.toString ()+
            "\nMake sure it points to a valid j2ee package (ejb.jar/web.war/app.ear)!");
      }
      
      // valid descriptor found
      // create a Deployment
      Deployment d = new Deployment ();
      
      d.name = getAppName (_downloadUrl);
      d.localUrl = new File (DEPLOYMENT_DIR, d.name).toURL ();
      log.log ("Create application " + d.name);
      
      // do the app type dependent stuff...
      if ("ejb-jar".equals (root.getTagName ()))
      {
         // its a EJB.jar... just take the package
         installEJB (d, _downloadUrl, null);
      } 
      else if ("web-app".equals (root.getTagName ()))
      {
         // its a WAR.jar... just take the package
         if (directory)
            throw new J2eeDeploymentException ("Currently are only packed web.war archives supported.");
         
         if (!warDeployerAvailable ())
            throw new J2eeDeploymentException ("No web container available!");
         
         String context = getWebContext (_downloadUrl);
         installWAR (d, _downloadUrl, context, null);
      } 
      else if ("application".equals (root.getTagName ()))
      {
         // its a EAR.jar... hmmm, its a little more
         if (directory)
            throw new J2eeDeploymentException ("Application .ear files are only in packed archive format supported.");
         
         // create a MetaData object...
         J2eeApplicationMetaData app = null; 
         try
         {
            app = new J2eeApplicationMetaData (root);
         }
         catch (DeploymentException _de)
         {
            throw new J2eeDeploymentException ("Error in parsing application.xml: "+_de.getMessage ());
         }
         
         // currently we only take care for the modules
         // no security stuff, no alternative DDs 
         // no dependency and integrity checking... !!!
         J2eeModuleMetaData mod;
         Iterator it = app.getModules ();
         for (int i = 0; it.hasNext (); ++i)
         {
            // iterate the ear modules
            mod = (J2eeModuleMetaData) it.next ();
            
            if (mod.isEjb ())
            {
               URL src = new URL ("jar:"+_downloadUrl.toString ()+ "!"+
                  (mod.getFileName ().startsWith ("/") ? "" : "/")+
                  mod.getFileName ());
               installEJB (d, src, null);
            }
            else if (mod.isWeb ())
            {
               if (!warDeployerAvailable ())
                  throw new J2eeDeploymentException ("Application contains .war file, but no web container available!");
               
               String context = mod.getWebContext ();
               if (context == null)
                  context = mod.getFileName ().substring (Math.max (0, mod.getFileName ().lastIndexOf ("/")));
               
               URL src = new URL ("jar:"+_downloadUrl.toString ()+ "!"+
                  (mod.getFileName ().startsWith ("/") ? "" : "/")+
                  mod.getFileName ());
               installWAR (d, src, context, null);
            }
            // other packages we dont care about (currently)
         } 
      }
      saveConfig (d);
      
      return d;
   }
   
   void installEJB (Deployment _d, URL _source, URL[] _altDD) throws IOException
   {
      Deployment.Module m = _d.newModule ();
      m.name = getAppName (_source);
      log.log ("Installing EJB package: " + m.name);
      
      // download the package...
      URL localUrl = URLWizzard.downloadTemporary(_source, _d.localUrl, "ejb", ".jar");
      
      // download alternative Descriptors (ejb-jar.xml, jboss.xml, ...)
      // DOESNT WORK YET!!!
      if (_altDD != null && _altDD.length > 0)
      {
         ;
      }
      
      m.localUrls.add (localUrl);
      
      // download libraries we depend on
      Manifest mf = new JarFile (localUrl.getFile ()).getManifest ();
      addCommonLibs (_d, mf, _source);

      // add module to the deployment
      _d.ejbModules.add (m);
   }
   
   
   void installWAR (Deployment _d, URL _source, String _context, URL[] _altDD) throws IOException
   {
      Deployment.Module m = _d.newModule ();
      m.name = getAppName (_source);
      log.log ("Installing web package: " + m.name);
      
      // download the package...
      URL localUrl = URLWizzard.downloadAndInflateTemporary (_source, _d.localUrl, "war");
      
      // save the contextName
      m.webContext = _context;
      
      // download alternative Descriptors (web.xml, ...)
      // DOESNT WORK YET!!!
      if (_altDD != null && _altDD.length > 0)
      {
         ;
      }
      
      m.localUrls.add (localUrl);
      
      // download libraries we depend on
      FileInputStream in = new FileInputStream (localUrl.getFile ()+File.separator+"META-INF"+File.separator+"MANIFEST.MF");
      Manifest mf = new Manifest (in);
      in.close ();
      addCommonLibs (_d, mf, _source);

      // add module to the deployment
      _d.webModules.add (m);
   }
   
   /** downloads all Class-Path: files of this manifest and adds the local urls 
   *   to the deployments commonUrls.
   *   @param _d the deployment
   *   @param _mf the manifest
   *   @param _base the base url to which the manifest entries are relative
   *   @throws IOExcepiton in case of error while downloading
   */
   private void addCommonLibs (Deployment _d, Manifest _mf, URL _base) throws IOException
   {
      String cp = _mf.getMainAttributes ().getValue(Attributes.Name.CLASS_PATH);
      
      if (cp != null)
      {
         StringTokenizer cpTokens = new StringTokenizer (cp," ");
         while (cpTokens.hasMoreTokens ())
         {                     
            String classPath = cpTokens.nextToken();
            try
            {
               URL src = new URL(_base, classPath);
               _d.commonUrls.add (URLWizzard.downloadTemporary (src, _d.localUrl, "lib", ".jar"));
               log.error("Added " + classPath + " to common classpath");
            } 
            catch (Exception e)
            {
               log.error("Could not add " + classPath + " to common classpath");
            }
         }
      }
   }
   
   /** Deletes the file tree of  the specified application. <br>
   *   @param _name the directory (DEPLOYMENT_DIR/<_name> to remove recursivly
   *   @throws IOException if something goes wrong
   */
   void uninstallApplication (String _name) throws IOException
   {
      log.log ("Destroying application " + _name);
      URL url = null;
      try 
      {
         url = new URL (DEPLOYMENT_DIR.toURL (), _name);
         
         // because of the deletion problem in some WIndowz 
         // we remove the CONFIG separatly (this should always work)
         // so that on stopService () now exceptions were thrown when he thinks
         // some apps are still deployed...
         URLWizzard.deleteTree (new URL (url, CONFIG));      
         log.log (CONFIG+" file deleted.");
         
         URLWizzard.deleteTree (url);      
         log.log ("File tree "+url.toString ()+" deleted.");
      } catch (MalformedURLException _mfe) { // should never happen
      } catch (IOException _ioe) {
         log.log ("Could not remove file: "+url.toString ());
         // throw _ioe;
      }
   }
   
   /** Starts the successful downloaded deployment. <br>
   *   Means the modules are deployed by the responsible container deployer
   *   @param _d the deployment to start
   *   @throws J2eeDeploymentException if an error occures for one of these
   *           modules
   */
   private void startApplication (Deployment _d) throws J2eeDeploymentException
   {
      // save the old classloader 
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader ();
      
      // set the context classloader for this application
      createContextClassLoader(_d);
      
      // redirect all modules to the responsible deployers
      Deployment.Module m = null;
      String message;
      try
      {
         // jBoss
         Iterator it = _d.ejbModules.iterator ();
         while (it.hasNext ())
         {
            m = (Deployment.Module)it.next ();
            
            log.log ("Starting module " + m.name);
            
            // Call the ContainerFactory that is loaded in the JMX server
            server.invoke(jarDeployer, "deploy",
               new Object[] { m.localUrls.firstElement().toString () }, new String[] { "java.lang.String" });
         }
         
         
         // Tomcat
         it = _d.webModules.iterator ();
         while (it.hasNext ())
         {
            m = (Deployment.Module)it.next ();
            
            log.log ("Starting module " + m.name);
            
            // Call the TomcatDeployer that is loaded in the JMX server
            server.invoke(warDeployer, "deploy",
               new Object[] { m.webContext, m.localUrls.firstElement().toString ()}, new String[] { "java.lang.String", "java.lang.String" });
         }
      }
      catch (MBeanException _mbe) {
         log.error ("Starting "+m.name+" failed!");
         throw new J2eeDeploymentException ("Error while starting "+m.name+": " + _mbe.getTargetException ().getMessage ());
      } catch (JMException _jme){
         log.error ("Starting failed!");
         throw new J2eeDeploymentException ("Fatal error while interacting with deployer MBeans... " + _jme.getMessage ());
      } finally {  			
         Thread.currentThread().setContextClassLoader (oldCl);
      }
   }
   
   /** Stops a running deployment. <br>
   *   Means the modules are undeployed by the responsible container deployer
   *   @param _d the deployment to stop
   *   @throws J2eeDeploymentException if an error occures for one of these
   *           modules
   */
   private void stopApplication (Deployment _d) throws J2eeDeploymentException
   {
      // save the old classloader, tomcat replaces my classloader somehow?!
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader ();
      
      StringBuffer error = new StringBuffer ();
      ObjectName[] container = new ObjectName[] {jarDeployer, warDeployer};
      Iterator modules[] = new Iterator[] {_d.ejbModules.iterator (),_d.webModules.iterator ()};
      for (int i = 0; i < container.length; ++i)
      {
         if (container[i] == null && modules[i].hasNext ())
         {
            // in case we are not running with tomcat
            // should only happen for tomcat (i=1)
            log.warning("Cannot find web container");
            continue;
         }         
         
         while (modules[i].hasNext ())
         {
            Deployment.Module m = (Deployment.Module)modules[i].next ();
            try
            {
               // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
               Object result = server.invoke(container[i], "isDeployed",
                  new Object[] { m.localUrls.firstElement ().toString () }, new String[] { "java.lang.String" });
               if (((Boolean)result).booleanValue ())
               {
                  
                  log.log ("Stopping module " + m.name);
                  server.invoke(container[i], "undeploy",
                     new Object[] { m.localUrls.firstElement ().toString () }, new String[] { "java.lang.String" });
               }
               else
                  log.log ("Module " + m.name+" is not running");
            
            }
            catch (MBeanException _mbe) 
            {
               log.error ("Unable to stop module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
               error.append("Unable to stop module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
               error.append ("/n");
            } 
            catch (JMException _jme)
            {
               log.error ("Unable to stop module " + m.name + ": " + _jme.getMessage ());
               error.append("Unable to stop module " + m.name + ": fatal error while calling "+container[i]+": " + _jme.getMessage ());
               error.append ("/n");
            }
         }
      }
      if (!error.toString ().equals (""))
         // there was at least one error...
      throw new J2eeDeploymentException ("Error(s) on stopping application "+_d.name+":\n"+error.toString ());

      // restore the classloader
      Thread.currentThread().setContextClassLoader (oldCl);
   }
   
   /** Checks the Deplyment if it is correctly deployed.
   *   @param app to check
   *   @throws J2eeDeploymentException if some inconsistency in the deployment is
   *           detected
   */
   private boolean checkApplication (Deployment _d) throws J2eeDeploymentException
   {
      boolean result = false;
      int count = 0;
      int others = 0;
      
      ObjectName[] container = new ObjectName[] {jarDeployer, warDeployer};
      Iterator modules[] = new Iterator[] {_d.ejbModules.iterator (),_d.webModules.iterator ()};
      for (int i = 0; i < container.length; ++i)
      {
         if (container[i] == null && modules[i].hasNext ())
         {
            // in case we are not running with tomcat
            // should only happen for tomcat (i=1)
            log.warning("Cannot find web container");
            continue;
         }         
         
         while (modules[i].hasNext ())
         {
            Deployment.Module m = (Deployment.Module)modules[i].next ();
            Object o = null;
            try
            {
               
               log.log ("Checking module " + m.name);
               // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
               o = server.invoke(container[i], "isDeployed",
                  new Object[] { m.localUrls.firstElement ().toString () }, new String[] { "java.lang.String" });
            
            }
            catch (MBeanException _mbe) 
            {
               log.error ("Error while checking module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
            } 
            catch (JMException _jme)
            {
               log.error ("Fatal error while checking module " + m.name + ": " + _jme.getMessage ());
            }
            
            if (o == null) // had an exception
               ++others;
            else if (count++ == 0) // first module -> set state
               result = ((Boolean)o).booleanValue ();
            else if (result != ((Boolean)o).booleanValue ()) // only if differs from state
               ++others;
         }
      }
      if (others > 0)
         // there was at least one error...
      throw new J2eeDeploymentException ("Application "+_d.name+" is not correctly deployed! ("+
         (result ? count-others : others)+
         " modules are running "+
         (result ? others : count-others)+
         " are not)");
      return result;
   }
   
   
   /** Loads the serialized CONFIG file for the specified app. <br>
   *   loads DEPLOYMENT_DIR / _app / CONFIG
   *   @param _app the name of the app
   *   @throws IOException if an error occures
   */
   private Deployment loadConfig (String _app) throws IOException
   {          
      Deployment d = null;
      try
      {
         ObjectInputStream in = new ObjectInputStream (
            new FileInputStream (DEPLOYMENT_DIR + File.separator + _app + File.separator + CONFIG));
         d = (Deployment)in.readObject ();
         in.close ();
      }
      catch (ClassNotFoundException _snfe){} // should never happen...
      
      return d;
   }
   
   /** Serializes Deployment as CONFIG file. <br>
   *   saves DEPLOYMENT_DIR / _d.name / CONFIG
   *   @param _app the name of the app
   *   @throws IOException if an error occures
   */
   private void saveConfig (Deployment _d) throws IOException
   {          
      ObjectOutputStream out = new ObjectOutputStream (
         new FileOutputStream (DEPLOYMENT_DIR + File.separator + _d.name + File.separator + CONFIG));
      out.writeObject (_d);
      out.flush ();
      out.close ();
   }
   
   /** tests if the web container deployer is available */
   private boolean warDeployerAvailable ()
   {
      return server.isRegistered (warDeployer);
   }
   
   /** returns the filename this url points to */
   public String getAppName (URL _url)
   {
      String s = _url.toString ();
      
      if (s.endsWith ("/"))
         s = s.substring (0, s.length() - 1);
      
      s = s.substring (s.lastIndexOf ("/") + 1);
      return s;
   }
   
   /** composes a webContext name of the file this url points to */
   public String getWebContext (URL _downloadUrl)
   {
      String s = getAppName (_downloadUrl);
      
      // truncate the file extension
      int p = s.lastIndexOf (".");
      if (p != -1)
         s = s.substring (0, p);
      
      return "/" + s.replace ('.', '/');
   }	  
   
   
   
   /**
   * creates an application class loader for this deployment
   * this class loader will be shared between jboss and tomcat via the contextclassloader
   */
   private void createContextClassLoader(Deployment deployment) {
      
      // get urls we want all classloaders of this application to share 
      URL[] urls = new URL[deployment.commonUrls.size ()];
      for (int i = 0, l = deployment.commonUrls.size (); i < l; ++i)
         urls[i] = (URL)deployment.commonUrls.elementAt (i); 
      
      // create classloader
      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      URLClassLoader appCl = new URLClassLoader(urls, parent);
      
      // set it as the context class loader for the deployment thread
      Thread.currentThread().setContextClassLoader(appCl);
   }


}
