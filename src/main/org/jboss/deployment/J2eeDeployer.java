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
*  (if Tomcat runs within the same VM as jBoss) or even Application.ears. <br>
*  The deployment is done by determining the file type of the given url.
*  The file must be a valid zip file and must contain either a META-INF/ejb-jar.xml
*  or META-INF/application.xml or a WEB-INF/web.xml file.
*  Depending on the file type, the whole file (EJBs, WARs)
*  or only the relevant packages (EAR) becoming downloaded. <br>
*  <i> replacing alternative DDs and validation is not yet implementet! </i>
*  The uploaded files are getting passed through to the responsible deployer
*  (ContainerFactory for jBoss and EmbededTomcatService for Tomcat).
*  The state of deployments is persistent and becomes recovered after shutdown
*  or crash.
*
*   @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
*   @version $Revision: 1.4 $
*/
public class J2eeDeployer 
extends ServiceMBeanSupport
implements J2eeDeployerMBean
{
   // Constants -----------------------------------------------------
   public String DEPLOYMENT_DIR = "/home/deployment"; // default? MUST BE ABSOLUTE PATH!!!
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
   
   // the deployments
   Hashtable deployments = new Hashtable ();
   
   
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
      DEPLOYMENT_DIR = _deployDir;
      
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
      log.log ("deploy j2ee application: " + _url);
      
      Deployment d = null;
      try
      {
         d = installApplication (url);
         startApplication (d);
         
         log.log ("j2ee application: " + _url + " is deployed.");
      } 
      catch (IOException _ioe)
      {
         uninstallApplication (_url.substring(Math.max (0, _url.lastIndexOf("/"))));
         throw _ioe;
      }
      catch (J2eeDeploymentException _e)
      {
         if (d != null) // start failed...
            stopApplication (d);
         
         uninstallApplication (_url.substring(Math.max (0, _url.lastIndexOf("/"))));
         throw _e;
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
      String name = _app.substring(Math.max (0, _app.lastIndexOf("/")));
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
         throw new J2eeDeploymentException ("The application \""+name+"\" is not deployed.");
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
      String name = _url.substring(Math.max (0, _url.lastIndexOf("/")));
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
      return "J2ee deployer";
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
      
      //set the deployment directory
      DEPLOYMENT_DIR = new File (DEPLOYMENT_DIR).getCanonicalPath ();
      
      // Save JMX name of the deployers
      jarDeployer = new ObjectName(jarDeployerName);
      warDeployer= new ObjectName(warDeployerName);
   
   }
   
   /** tries to redeploy all deployments before shutdown.*/
   protected void startService()
   throws Exception
   {
      if (!warDeployerAvailable ())
         log.log ("No war deployer found - only EJB deployment available...");
      
      /*
      // this doesnt work properly ?!
      // when something was deployed on server stop and on server restart
      // it becomes restarted (the same files) the ContainerFactory has a
      // strange problem in handling its copys of the jar files in the tmp/deploy dir... 
      
      log.log ("trying to start all applications that were running before service.stop ()...");
      
      File f = new File (DEPLOYMENT_DIR);
      File[] files =  f.listFiles();
      int count = 0;
      for (int i = 0, l = files.length; i<l; ++i)
      {
      if (files[i].isDirectory ())
      {
      // lets give it a try...
      Deployment d = null;
      try
      {
      d = loadConfig (files[i].getName());
      }
      catch (IOException _io)
      {
      continue;
      }
      
      // ok, then lets start it
      log.log ("starting application: " + d.name);
      try
      {
      startApplication (d);
      log.log ("application: " + d.name + " is started.");
      ++count;
      } 
      catch (J2eeDeploymentException _e)
      {
      stopApplication (d);
      uninstallApplication (files[i].getName());
      
      _e.printStackTrace();
      }
      }
      }
      log.log ("started "+count+" applications.");
      */
   }
   
   
   /** undeploys all deployments */
   protected void stopService()
   {
      log.log ("undeploying all applications.");
      
      File f = new File (DEPLOYMENT_DIR);
      File[] files =  f.listFiles();
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
      log.log ("undeployed "+count+" applications.");
   }
   
   // Private -------------------------------------------------------
   
   /** determines deployment type and installs (downloads) all packages needed 
   *   by the given deployment of the given deployment. <br>
   *   This means download the needed packages do some validation...
   *   <i> Validation and do some other things is not yet implemented </i>
   *   @param _d the deployment (= a J2ee application or module)
   *   @throws IOException if the download fails
   *   @throws J2eeDeploymentException if the given package is somehow inconsistent
   */
   Deployment installApplication (URL _downloadUrl) throws IOException, J2eeDeploymentException
   {
      // determine the file type by trying to access one of the possible
      // deployment descriptors...
      Element root = null;
      String[] files = {"META-INF/ejb-jar.xml", "META-INF/application.xml", "WEB-INF/web.xml"};
      boolean directory = false;
      for (int i = 0; i < files.length && root == null; ++i)
      {
         // try it as if it is a jar file
         try {
            root = XmlFileLoader.getDocument (new URL ("jar:" + _downloadUrl.toString () + "!/" + files[i])).getDocumentElement ();
         } catch (Exception _e) {}

         // try it, as if it is a directory
         // but this cant be handled right now
         // try {
         //    root = XmlFileLoader.getDocument (new URL (_downloadUrl.toString () + "/" + files[i])).getDocumentElement ();
         //    directory = true;
         // } catch (Exception _e) {}
      }
      
      if (root == null)
         // no descriptor was found...
      throw new J2eeDeploymentException ("No valid deployment descriptor was found within this URL: "+
         _downloadUrl.toString ()+
         "\nMake sure it points to a valid j2ee package (ejb.jar/web.war/app.ear)!");
      
      // create a Deployment
      Deployment d = new Deployment ();
      URL currentDownload = null; // to give more precize info in error case which download failed
      try {
         d.name = Deployment.getAppName (_downloadUrl);
         String localUrl = "file:"+DEPLOYMENT_DIR+"/"+d.name;
         
         log.log ("create application " + d.name);
         
         
         // do the app type dependent stuff...
         if ("ejb-jar".equals (root.getTagName ()))
         {
            // its a EJB.jar... just take the package
            Deployment.Module m = d.newModule ();
            // localUrl: file:<download_dir> / <appName> / <appName>
            m.name = Deployment.getFileName (_downloadUrl);
            m.localUrl = new URL (localUrl+"/"+m.name);// should never throw an URLEx
            
            log.log ("downloading module " + m.name);
            currentDownload = _downloadUrl;
            URLWizzard.download (currentDownload, m.localUrl);
            d.ejbModules.add (m);      		
         } 
         else if ("web-app".equals (root.getTagName ()))
         {
            // its a WAR.jar... just take the package
            if (!warDeployerAvailable ())
               throw new J2eeDeploymentException ("No war container available!");
            
            Deployment.Module m = d.newModule ();
            m.name = Deployment.getFileName (_downloadUrl);
            m.localUrl = new URL (localUrl+"/"+m.name);// should never throw an URLEx
            m.webContext = Deployment.getWebContext (_downloadUrl);
            
            log.log ("downloading and inflating module " + m.name);
            currentDownload = _downloadUrl;
            URLWizzard.downloadAndInflate (currentDownload, m.localUrl);
            d.webModules.add (m);      		
         } 
         else if ("application".equals (root.getTagName ()))
         {
            // its a EAR.jar... hmmm, its a little more
            // create a MetaData object...
            J2eeApplicationMetaData app;
            app = new J2eeApplicationMetaData (root);
            
            // currently we only take care for the modules
            // no security stuff, no alternative DDs 
            // no dependency and integrity checking... !!!
            J2eeModuleMetaData mod;
            Iterator it = app.getModules ();
            while (it.hasNext ())
            {
               mod = (J2eeModuleMetaData) it.next ();
               // iterate the ear modules
               if (mod.isEjb () || mod.isWeb ())
               {
                  
                  // common stuff
                  Deployment.Module m = d.newModule ();
                  URL downloadUrl;
                  try 
                  {
                     currentDownload = new URL ("jar:" + _downloadUrl.toString () + "!"+
                        (mod.getFileName ().startsWith ("/") ? "" : "/")+
                        mod.getFileName ());// chould throw an URLEx
                     m.name = Deployment.getFileName (currentDownload);
                     m.localUrl = new URL (localUrl + "/" + m.name);// should not throw an URLEx
                  } 
                  catch (MalformedURLException _mue) 
                  { 
                     throw new J2eeDeploymentException ("syntax error in module section in application DD (module uri: " + mod.getFileName()+")");
                  }
                  
                  // web specific
                  if (mod.isWeb ())
                  {
                     if (!warDeployerAvailable ())
                        throw new J2eeDeploymentException ("Application containes .war file. No war container available!");
                     
                     m.webContext = mod.getWebContext ();
                     if (m.webContext == null)
                        m.webContext = Deployment.getWebContext (currentDownload);
                     
                     log.log ("downloading and inflating module " + m.name);
                     URLWizzard.downloadAndInflate (currentDownload, m.localUrl);
                     d.webModules.add (m);      		
                  }
                  // ejb specific
                  else 
                  {
                     log.log ("downloading module " + m.name);
                     URLWizzard.download (currentDownload, m.localUrl);
                     d.ejbModules.add (m);
                  }
                  
                  // here the code for checking and DD replacment...
                  // ...
               } 
            }
            // other packages we dont care about (currently)
         }
         // storing the deployment
         currentDownload = null;
         FileOutputStream fout = new FileOutputStream (localUrl.substring (5, localUrl.length ()) + File.separator + CONFIG);
         ObjectOutputStream out = new ObjectOutputStream (fout);
         out.writeObject (d);
         out.flush ();
         out.close ();
         
         return d;
      } 
      catch (IOException _ioe) //happens on downloading modules 
      {
         if (currentDownload == null)
            throw new IOException ("Error in saving config file "+CONFIG +": "+_ioe.getMessage ());
         else
            throw new IOException ("Error in downloading "+currentDownload.toString() +
            ": "+_ioe.getMessage ());
      }
      catch (DeploymentException _de) // happens on creating J2eeApplicationMetaData 
      { 
         throw new J2eeDeploymentException ("Error in scanning .ear file deploymnet descriptor: " + _de.getMessage ());
      } 
      catch (Exception _e) // when something goes really wrong (runtime exception) 
      { 
         _e.printStackTrace ();
         throw new J2eeDeploymentException ("FATAL ERROR! "+_e.toString ());
      }  			
   }
   
   /** Deletes the file tree of  the specified application. <br>
   *   @param _name the directory (DEPLOYMENT_DIR/<_name> to remove recursivly
   *   @throws IOException if something goes wrong
   */
   void uninstallApplication (String _name) throws IOException
   {
      log.log ("destroying application " + _name);
      URL url = null;
      try 
      {
         url = new URL ("file:"+DEPLOYMENT_DIR+"/"+_name);
         URLWizzard.deleteTree (url);      
         log.log ("file tree "+url.toString ()+" deleted.");
      } catch (MalformedURLException _mfe) { // should never happen
      } catch (IOException _ioe) {
         throw _ioe;
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
            log.log ("starting module " + m.name);
            // Call the ContainerFactory that is loaded in the JMX server
            server.invoke(jarDeployer, "deploy",
               new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
         }
         
         
         // Tomcat
         it = _d.webModules.iterator ();
         while (it.hasNext ())
         {
            m = (Deployment.Module)it.next ();
            log.log ("starting module " + m.name);
            // Call the TomcatDeployer that is loaded in the JMX server
            server.invoke(warDeployer, "deploy",
               new Object[] { m.webContext, m.localUrl.toString ()}, new String[] { "java.lang.String", "java.lang.String" });
         }
      }
      catch (MBeanException _mbe) {
         log.error ("starting "+m.name+" failed!");
         throw new J2eeDeploymentException ("error while starting "+m.name+": " + _mbe.getTargetException ().getMessage ());
      } catch (JMException _jme){
         log.error ("starting failed!");
         throw new J2eeDeploymentException ("fatal error while interacting with deployer MBeans... " + _jme.getMessage ());
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
      StringBuffer error = new StringBuffer ();
      ObjectName[] container = new ObjectName[] {jarDeployer, warDeployer};
      Iterator modules[] = new Iterator[] {_d.ejbModules.iterator (),_d.webModules.iterator ()};
      for (int i = 0; i < container.length; ++i)
      {
         if (container[i] == null && modules[i].hasNext ())
         {
            // in case we are not running with tomcat
            // should only happen for tomcat (i=1)
            log.warning("cannot find .war container");
            continue;
         }         
         
         while (modules[i].hasNext ())
         {
            Deployment.Module m = (Deployment.Module)modules[i].next ();
            try
            {
               // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
               Object result = server.invoke(container[i], "isDeployed",
                  new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
               if (((Boolean)result).booleanValue ())
               {
                  
                  log.log ("stopping module " + m.name);
                  server.invoke(container[i], "undeploy",
                     new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
               }
               else
                  log.log ("module " + m.name+" is not running");
            
            }
            catch (MBeanException _mbe) 
            {
               log.error ("unable to stop module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
               error.append("unable to stop module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
               error.append ("/n");
            } 
            catch (JMException _jme)
            {
               log.error ("unable to stop module " + m.name + ": " + _jme.getMessage ());
               error.append("unable to stop module " + m.name + ": fatal error while calling "+container[i]+": " + _jme.getMessage ());
               error.append ("/n");
            }
         }
      }
      if (!error.toString ().equals (""))
         // there was at least one error...
      throw new J2eeDeploymentException ("error(s) on stopping application "+_d.name+":\n"+error.toString ());
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
            log.warning("cannot find .war container");
            continue;
         }         
         
         while (modules[i].hasNext ())
         {
            Deployment.Module m = (Deployment.Module)modules[i].next ();
            Object o = null;
            try
            {
               
               log.log ("checking module " + m.name);
               // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
               o = server.invoke(container[i], "isDeployed",
                  new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
               
            }
            catch (MBeanException _mbe) 
            {
               log.error ("error while checking module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
            } 
            catch (JMException _jme)
            {
               log.error ("fatal error while checking module " + m.name + ": " + _jme.getMessage ());
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
      throw new J2eeDeploymentException ("application "+_d.name+" is NOT correct deployed! ("+
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
   
   /** tests if the web container deployer is available
   */
   private boolean warDeployerAvailable ()
   {
      return server.isRegistered (warDeployer);
   }
   
   
   /**
   * creates an application class loader for this deployment
   * this class loader will be shared between jboss and tomcat via the contextclassloader
   */
   private void createContextClassLoader(Deployment deployment) {
      
      // find all the urls to add
      ArrayList urls = new ArrayList();
      
      // ejb applications
      Iterator iterator = deployment.ejbModules.iterator();
      while (iterator.hasNext()) {
         // add the local url to the classloader
         urls.add(((Deployment.Module)iterator.next()).localUrl);
      }
      
      // TODO? web applications
      // add WEB-INF/classes and WEB-INF/lib/* from webModules.localUrl
      
      // actually create the class loader. 
      // Keep the current context class loader as a parent (jboss and tomcat classes are in it)
      URL[] urlArray = (URL[])urls.toArray(new URL[urls.size()]);
      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      
      URLClassLoader appCl = new URLClassLoader(urlArray, parent);
      
      // set it as the context class loader for the deployment thread
      Thread.currentThread().setContextClassLoader(appCl);
   }
}
