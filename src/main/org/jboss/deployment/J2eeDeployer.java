/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
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
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.management.j2ee.J2EEApplication;

/**
 * J2eeDeployer allows to deploy single EJB.jars as well as Web.wars
 * (if a Servlet Container is present) or even Application.ears. <br>
 * The deployment is done by determining the file type of the given url.
 * The file must be a valid zip file or a directory and must contain either a META-INF/ejb-jar.xml
 * or META-INF/application.xml or a WEB-INF/web.xml file.
 * Depending on the file type, the whole file (EJBs, WARs)
 * or only the relevant packages (EAR) becoming downloaded. <br>
 * <i> replacing alternative DDs and validation is not yet implementet! </i>
 * The uploaded files are getting passed through to the responsible deployer
 * (ContainerFactory for JBoss and EmbededTomcatService for Tomcat).
 * <br>
 * Extended and refactored on 5th October 2001 by CGJ to cater for rar and
 * java-client modules within applications.
 *
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>.
 * @version $Revision: 1.51 $
 */
public class J2eeDeployer
extends ServiceMBeanSupport
implements J2eeDeployerMBean
{
   // Constants -----------------------------------------------------
   public File DEPLOYMENT_DIR = null;//"/home/deployment"; // default? MUST BE ABSOLUTE PATH!!!
   public static String CONFIG = "deployment.cfg";
   final public static String DEFAULT_NAME="Default";
   final public static String DEFAULT_JAR_DEPLOYER_NAME="EJB:service=ContainerFactory";
   final public static String DEFAULT_WAR_DEPLOYER_NAME=":service=EmbeddedTomcat";
   final public static String DEFAULT_RAR_DEPLOYER_NAME=":service=RarDeployer";
   final public static String DEFAULT_JAVA_DEPLOYER_NAME=":service=JavaDeployer";
   
   public static final int EASY = 0;
   public static final int RESTRICTIVE = 1;
   
   // Attributes ----------------------------------------------------
   // my server to lookup for the special deployers
   // <comment author="cgjung">better be protected for subclassing </comment>
   protected MBeanServer server;
   
   // <comment author="cgjung">better be protected for subclassing </comment>
   protected String name;
   
   // names of the specials deployers
   // <comment author="cgjung">better be protected for subclassing </comment>
   protected ObjectName jarDeployer;
   protected ObjectName warDeployer;
   protected ObjectName rarDeployer;
   protected ObjectName javaDeployer;
   
   int classpathPolicy = EASY;
   
   // <comment author="cgjung"> better be protected for subclassing </comment>
   protected InstallerFactory installer;
   
   
   // Constructors --------------------------------------------------
   
   public J2eeDeployer()
   {
   }
   
   public void setDeployerName(final String name)
   {
      this.log = Logger.getLogger(getClass().getName() + "#" + name);
      this.name = name;
   }
   
   public String getDeployerName()
   {
      return name.trim();
   }
   
   public void setJarDeployer(ObjectName jarDeployer)
   {
      this.jarDeployer = jarDeployer;
   }
   
   public ObjectName getJarDeployer()
   {
      return jarDeployer;
   }
   
   public void setWarDeployer(ObjectName warDeployer)
   {
      this.warDeployer = warDeployer;
   }
   
   public ObjectName getWarDeployer()
   {
      return warDeployer;
   }
   
   public void setRarDeployer(ObjectName rarDeployer)
   {
      this.rarDeployer = rarDeployer;
   }
   
   public ObjectName getRarDeployer()
   {
      return rarDeployer;
   }
   
   public void setServiceDeployer(ObjectName javaDeployer)
   {
      this.javaDeployer = javaDeployer;
   }
   
   public ObjectName getServiceDeployer()
   {
      return javaDeployer;
   }
   
   // Public --------------------------------------------------------
   
   public FilenameFilter getDeployableFilter()
   {
      return new FilenameFilter()
      {
         public boolean accept(File dir, String filename)
         {
            filename=filename.toLowerCase();
            boolean accept = filename.endsWith(".jar") ||
            filename.endsWith(".war") ||
            filename.endsWith(".ear") ||
            filename.endsWith(".zip");
            return accept;
         }
      };
   }
   
   /** Deploys the given URL independent if it is a EJB.jar, Web.war
    *   or Application.ear. In case of already deployed, it performes a
    *   redeploy.
    *   @param _url the url (file or http) to the archiv to deploy
    *   @throws MalformedURLException in case of a malformed url
    *   @throws J2eeDeploymentException if something went wrong...
    *   @throws IOException if trouble while file download occurs
    */
   public void deploy(String _url)
   throws MalformedURLException, IOException, J2eeDeploymentException
   {
      URL url = new URL(_url);
      
      // undeploy first if it is a redeploy
      try
      {
         undeploy(_url);
      }
      catch (Exception _e)
      {
      }
      
      // now try to deploy
      log.info("Deploying J2EE application: " + _url);
      
      Deployment d = installApplication(url);
      
      try
      {
         // <comment author="cgjung"> factored out for subclass access
         startApplication(d);
         log.info("Deployed J2EE application: " + _url);
      }
      catch (Exception _e)
      {
         try
         {
            stopApplication(d);
         }
         catch (Exception _e2)
         {
            log.error("unable to stop application " + d.name, _e2);
         }
         finally
         {
            try
            {
               uninstallApplication(_url);
            }
            catch (Exception _e3)
            {
               log.error("unable to uninstall application " + d.name, _e3);
            }
         }
         
         if (_e instanceof J2eeDeploymentException)
         {
            throw (J2eeDeploymentException)_e;
         }
         else
         {
            log.error("fatal error:", _e);
            throw new J2eeDeploymentException("fatal error", _e);
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
   public void undeploy(String _app) throws IOException, J2eeDeploymentException
   {
      Deployment d = installer.findDeployment(_app);
      
      if (d == null)
         throw new J2eeDeploymentException("The application \""+name+"\" has not been deployed.");
      
      try
      {
         stopApplication(d);
      }
      catch (J2eeDeploymentException _e)
      {
         throw _e;
      }
      finally
      {
         uninstallApplication(d);
      }
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
   public boolean isDeployed(String _url) throws MalformedURLException, J2eeDeploymentException
   {
      boolean result = false;
      
      Deployment d = installer.findDeployment(_url);
      
      if (d != null)
      {
         result = checkApplication(d);
      }
      
      return result;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   public String getName()
   {
      return "J2EE Deployer " + name;
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return name == null ? new ObjectName(OBJECT_NAME+this.name) : name;
   }

   protected File getTempDir() throws IOException {
      File dir = new File(System.getProperty("jboss.system.home"));
      dir = new File(dir, "tmp");
      dir = new File(dir, "deploy");
      dir = new File(dir, name);

      if (!dir.exists() && !dir.mkdirs()) {
         throw new IOException
            ("failed to create temp directory: " + dir);
      }

      return dir;
   }
   
   protected void startService() throws Exception
   {
      File dir = getTempDir();
      log.info("Using temporary directory: " + dir);
      
      installer = new InstallerFactory(dir);
      
      if (warDeployer == null)
         log.info("No web container found - only EJB deployment available...");
      
      // clean up the deployment directory since on some Windowz the file removement
      // during runtime doesnt work...
      log.info("Cleaning up deployment directory");
      installer.unclutter();
   }
   
   /** undeploys all deployments */
   protected void stopService()
   {
      log.info("Undeploying all applications.");
      
      Deployment[] deps = installer.getDeployments();
      int count = 0;
      for (int i = 0, l = deps.length; i<l; ++i)
      {
         try
         {
            stopApplication(deps[i]);
         }
         catch (J2eeDeploymentException _e)
         {
            //throw _e;
            log.error("Failed to stop app", _e);
         }
         finally
         {
            try
            {
               uninstallApplication(deps[i]);
            }
            catch (IOException _ioe)
            {
               log.error("Failed to unistall app", _ioe);
            }
         }
         ++count;
      }
      log.info("Undeployed "+count+" applications.");
   }
   
   // Private -------------------------------------------------------
   
   /** determines deployment type and installs (downloads) all packages needed
    *   by the given deployment of the given deployment. <br>
    *   This means download the needed packages do some validation...
    *   <i> Validation and do some other things is not yet implemented </i>
    *   <comment author="cgjung"> better be protected for subclassing </comment>
    *   @param _downloadUrl the url that points to the app to install
    *   @throws IOException if the download fails
    *   @throws J2eeDeploymentException if the given package is somehow inconsistent
    */
   protected Deployment installApplication(URL _downloadUrl) throws IOException, J2eeDeploymentException
   {
      return installer.install(_downloadUrl);
   }
   
   
   /** Deletes the file tree of  the specified application. <br>
    *  <comment author="cgjung">better be protected for subclassing</comment>
    *   @param _name the directory (DEPLOYMENT_DIR/<_name> to remove recursivly
    *   @throws IOException if something goes wrong
    */
   protected void uninstallApplication(String _pattern) throws IOException
   {
      Deployment d = installer.findDeployment(_pattern);
      
      if (d != null)
         uninstallApplication(d);
   }
   
   protected void uninstallApplication(Deployment _d) throws IOException
   {
      log.info("Destroying application " + _d.name);
      installer.uninstall(_d);
      log.info("Destroyed");
   }
   
   
   
   /** Starts the successful downloaded deployment. <br>
    *   Means the modules are deployed by the responsible container deployer
    *   <comment author="cgjung">better be protected for subclassing </comment>
    *   @param _d the deployment to start
    *   @throws J2eeDeploymentException if an error occures for one of these
    *           modules
    */
   protected void startApplication(Deployment _d) throws J2eeDeploymentException
   {
      // Create the appropriate JSR-77 instance
      ObjectName lApplication = J2EEApplication.create(
         getServer(),
         _d.getName(),
         _d.getApplicationDeploymentDescriptor()
      );
      
      // save the old classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      
      // set the context classloader for this application
      createContextClassLoader(_d);
      
      // save the application classloader for later
      ClassLoader appCl = Thread.currentThread().getContextClassLoader();
      
      // <comment author="cgjung">module deployment factored out for subclass
      // access </comment>
      // redirect all modules to the responsible deployers
      startModules(_d,appCl,oldCl);
   }
   
   /** factored out method to start individual modules and reinstall
    *  context classloader afterwards
    */
   protected void startModules(Deployment _d, ClassLoader appCl, ClassLoader oldCl) throws J2eeDeploymentException
   {
      Deployment.Module m = null;
      String moduleName = null;
      String message;
      try
      {
         // Deploy the ejb modules
         moduleName = _d.name;
         Vector tmp = new java.util.Vector();
         Iterator it = _d.ejbModules.iterator();
         while( it.hasNext() )
         {
            m = (Deployment.Module) it.next();
            tmp.add( m.localUrls.firstElement().toString() );
         }
         String[] jarUrls = new String[ tmp.size() ];
         tmp.toArray( jarUrls );
         // Call the ContainerFactory that is loaded in the JMX server
         if (log.isDebugEnabled())
         {
            log.debug("about to invoke deploy on jardeployer:" + jarDeployer);
         }
         server.invoke(jarDeployer, "deploy",
            new Object[]{
               _d.getName(),
               _d.localUrl.toString(),
               jarUrls,
               moduleName,
            },
            new String[]{
               String.class.getName(),
               String.class.getName(),
               String[].class.getName(),
               String.class.getName()
            }
         );
         
         // Deploy the web application modules
         it = _d.webModules.iterator();
         if (it.hasNext() && !warDeployerAvailable())
            throw new J2eeDeploymentException("application contains war files but no web container available");
         
         while( it.hasNext() )
         {
            m = (Deployment.Module)it.next();
            moduleName = m.name;
            log.info("Starting module " + moduleName);
            
            // Call the TomcatDeployer that is loaded in the JMX server
            Object[] args = { m.webContext, m.localUrls.firstElement().toString()};
            String[] types = { "java.lang.String", "java.lang.String" };
            server.invoke(warDeployer, "deploy", args, types);

            // since tomcat changes the context classloader...
            Thread.currentThread().setContextClassLoader(appCl);
         }
         
         // Deploy the connector modules
         it = _d.connectorModules.iterator();
         if (it.hasNext() && !rarDeployerAvailable())
            throw new J2eeDeploymentException("application contains rar files but no rar deployer available");
         
         while( it.hasNext() )
         {
            m = (Deployment.Module)it.next();
            moduleName = m.name;
            log.info("Starting module " + moduleName);

            // Call the rar deployer that is loaded in the JMX server
            Object[] args = {m.localUrls.firstElement().toString()};
            String[] types = { "java.lang.String" };
            server.invoke(rarDeployer, "deploy", args, types);            
         }

         // Deploy the connector modules
         it = _d.javaModules.iterator();
         if (it.hasNext() && !javaDeployerAvailable())
            throw new J2eeDeploymentException("application contains client java files but no java deployer available");
         
         while( it.hasNext() )
         {
            m = (Deployment.Module)it.next();
            moduleName = m.name;
            log.info("Starting module " + moduleName);
            // Call the rar deployer that is loaded in the JMX server
            Object[] args = {m.localUrls.firstElement().toString()};
            String[] types = { "java.lang.String" };
            server.invoke(javaDeployer, "deploy", args, types);
         }
      }
      catch (MBeanException e)
      {
         Exception ex = e.getTargetException();
         log.error("Starting "+moduleName+" failed!", ex);
         throw new J2eeDeploymentException("Error while starting "+moduleName+": "
            + ex.getMessage(), ex);
      }
      catch (RuntimeErrorException e)
      {
         Throwable ex = e.getTargetError();
         log.error("Starting "+moduleName+" failed!", ex);
         throw new J2eeDeploymentException("Error while starting "+moduleName+": "
            + ex.getMessage(), ex);
      }
      catch (RuntimeMBeanException e)
      {
         Exception ex = e.getTargetException();
         log.error("Starting "+moduleName+" failed!", ex);
         throw new J2eeDeploymentException("Error while starting "+moduleName+": "
            + ex.getMessage(), ex);
      }
      catch (JMException e)
      {
         log.error("Starting failed!", e);
         throw new J2eeDeploymentException("Fatal error while interacting with deployer MBeans... " + e.getMessage());
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   /**
    * Stops a running deployment. <br>
    * Means the modules are undeployed by the responsible container deployer
    * @param _d the deployment to stop
    * @throws J2eeDeploymentException if an error occures for one of these
    *           modules
    */
   protected void stopApplication(Deployment _d) throws J2eeDeploymentException
   {
      // save the old classloader, tomcat replaces my classloader somehow?!
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      StringBuffer error = new StringBuffer();
      
      // stop the web modules
      if( warDeployerAvailable() )
      {
         for( Iterator webModules = _d.webModules.iterator(); webModules.hasNext(); )
         {
            Deployment.Module m = (Deployment.Module)webModules.next();
            stopModule( warDeployer, m.name, m.localUrls.firstElement().toString(), error );
         }
      }
      else if (!_d.webModules.isEmpty())
      {
         // in case we are not running with tomcat
         // should only happen for tomcat (i=1)
         log.warn("Cannot find web container anymore!");
      }
      
      // stop the java modules
      if( javaDeployerAvailable() )
      {
         for( Iterator javaModules = _d.javaModules.iterator(); javaModules.hasNext(); )
         {
            Deployment.Module m = (Deployment.Module)javaModules.next();
            stopModule( javaDeployer, m.name, m.localUrls.firstElement().toString(), error );
         }
      }
      else if (!_d.javaModules.isEmpty())
      {
         // in case we are not running with tomcat
         // should only happen for tomcat (i=1)
         log.warn("Cannot find java deployer anymore!");
      }
      
      // stop the jar modules (the ContainerFactory is responsible for undeploying
      // all jars associated w/ a given application)
      stopModule( jarDeployer, _d.name, _d.localUrl.toString(), error );
      
      // stop the java modules
      if( rarDeployerAvailable() )
      {
         for( Iterator connectorModules = _d.connectorModules.iterator(); connectorModules.hasNext(); )
         {
            Deployment.Module m = (Deployment.Module)connectorModules.next();
            stopModule( rarDeployer, m.name, m.localUrls.firstElement().toString(), error );
         }
      }
      else if (!_d.connectorModules.isEmpty())
      {
         // in case we are not running with tomcat
         // should only happen for tomcat (i=1)
         log.warn("Cannot find rar deployer anymore!");
      }
      
      // Destroy the appropriate JSR-77 instance
      J2EEApplication.destroy(
         getServer(),
         _d.getName()
      );
      
      if (!error.toString().equals("")) // there was at least one error...
         throw new J2eeDeploymentException("Error(s) on stopping application "+_d.name+":\n"+error.toString());
      
      // restore the classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   private void stopModule( ObjectName container, String moduleName, String moduleUrl, StringBuffer error )
   {
      try
      {
         // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
         Object result = server.invoke(container, "isDeployed",
            new Object[] { moduleUrl },
            new String[] { "java.lang.String" }
         );
         if (((Boolean)result).booleanValue())
         {
            log.info("Stopping module " + moduleName);
            server.invoke(container, "undeploy",
            new Object[] { moduleUrl }, new String[]
            { "java.lang.String" });
         }
         else
            log.info("Module " + moduleName + " is not running");
      }
      catch (MBeanException _mbe)
      {
         log.error("Unable to stop module " + moduleName, _mbe.getTargetException());
         error.append("Unable to stop module " + moduleName + ": " + _mbe.getTargetException().getMessage());
         error.append("/n");
      }
      catch (JMException _jme)
      {
         log.error("Unable to stop module " + moduleName + ": " + _jme.getMessage());
         error.append("Unable to stop module " + moduleName + ": fatal error while calling " + container + ": " + _jme.getMessage());
         error.append("/n");
      }
   }
   
   /** Checks the Deplyment if it is correctly deployed.
    *   @param app to check
    *   @throws J2eeDeploymentException if some inconsistency in the deployment is
    *           detected
    */
   private boolean checkApplication(Deployment _d) throws J2eeDeploymentException
   {
      boolean result = false;
      int count = 0;
      int others = 0;
      
      
      // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
      Object o = checkModule( jarDeployer, _d.name, _d.localUrl.toString() );
      
      if( o == null )
         ++others;
      else
         result = ((Boolean) o).booleanValue();
      
      if (warDeployer != null )
      {
         for( Iterator webModules = _d.webModules.iterator(); webModules.hasNext(); )
         {
            Deployment.Module m = (Deployment.Module)webModules.next();
            o = checkModule( warDeployer, m.name, m.localUrls.firstElement().toString() );
            
            if (o == null) // had an exception
               ++others;
            else if (count++ == 0) // first module -> set state
               result = ((Boolean)o).booleanValue();
            else if (result != ((Boolean)o).booleanValue()) // only if differs from state
               ++others;
         }
      }
      else
      {
         // in case we are not running with tomcat
         log.warn("Cannot find web container");
      }
      
      if (others > 0)
         // there was at least one error...
         throw new J2eeDeploymentException("Application "+_d.name+" is not correctly deployed! ("+
         (result ? count-others : others)+
         " modules are running "+
         (result ? others : count-others)+
         " are not)");
         return result;
   }
   
   private Object checkModule( ObjectName container, String moduleName, String moduleUrl )
   {
      try
      {
         log.info("Checking module " + moduleName);
         // Call the ContainerFactory/EmbededTomcat that is loaded in the JMX server
         return server.invoke(container, "isDeployed",
         new Object[]
         { moduleUrl }, new String[]
         { "java.lang.String" });
      }
      catch (MBeanException _mbe)
      {
         log.error("Error while checking module " + moduleName + ": " + _mbe.getTargetException().getMessage());
         return null;
      }
      catch (JMException _jme)
      {
         log.error("Fatal error while checking module " + moduleName + ": " + _jme.getMessage());
         return null;
      }
   }
   
   
   
   /** tests if the web container deployer is available
    *  <comment author="cgjung"> better be protected for subclassing </comment>
    */
   protected boolean warDeployerAvailable()
   {
      return server.isRegistered(warDeployer);
   }
   
   /** tests if the web container deployer is available
    */
   protected boolean rarDeployerAvailable()
   {
      return server.isRegistered(rarDeployer);
   }
   
   /** tests if the web container deployer is available
    */
   protected boolean javaDeployerAvailable()
   {
      return server.isRegistered(javaDeployer);
   }
   
   /**
    * creates an application class loader for this deployment
    * this class loader will be shared between jboss and tomcat via the contextclassloader. May throw
    * a J2eeDeploymentException to indicate problems stting up the classloader.
    * <comment author="cgjung"> should be protected in order to allow reasonable subclassing. Needs an
    * exception in case that you do more sophisticated meta-data installations.
    * </comment>
    */
   protected void createContextClassLoader(Deployment deployment) throws J2eeDeploymentException
   {
      
      // get urls we want all classloaders of this application to share
      URL[] urls = new URL[deployment.commonUrls.size()];
      for (int i = 0, l = deployment.commonUrls.size(); i < l; ++i)
         urls[i] = (URL)deployment.commonUrls.elementAt(i);
      
      // create classloader
      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      URLClassLoader appCl = new URLClassLoader(urls, parent);
      
      // set it as the context class loader for the deployment thread
      Thread.currentThread().setContextClassLoader(appCl);
   }
}
