/*
* JBoss, the OpenSource EJB server
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.Enumeration;


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
*  (ContainerFactory for JBoss and EmbededTomcatService for Tomcat).
*
*   @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
*   @version $Revision: 1.13 $
*/
public class J2eeDeployer 
extends ServiceMBeanSupport
implements J2eeDeployerMBean
{
   // Constants -----------------------------------------------------
   public File DEPLOYMENT_DIR = null;//"/home/deployment"; // default? MUST BE ABSOLUTE PATH!!!
   public static String CONFIG = "deployment.cfg";   
   
	public static final int EASY = 0;
	public static final int RESTRICTIVE = 1;

   // Attributes ----------------------------------------------------
   // my server to lookup for the special deployers
   MBeanServer server;

	String name;
   
   // names of the specials deployers
   ObjectName jarDeployer;
   ObjectName warDeployer;
   
   String jarDeployerName;
   String warDeployerName;
   
	int classpathPolicy = EASY;

	InstallerFactory installer;
   
   // Static --------------------------------------------------------
   /** only for testing...*/
   public static void main (String[] _args) throws Exception
   {
      new J2eeDeployer().deploy (_args[0]);
   }
   
   // Constructors --------------------------------------------------
   public J2eeDeployer()
   {
      this("Default", "EJB:service=ContainerFactory", ":service=EmbeddedTomcat");
   }
   
   public J2eeDeployer (String _name, String jarDeployerName, String warDeployerName)
   {
      setDeployerName(_name);
      setJarDeployerName(jarDeployerName);
      setWarDeployerName(warDeployerName);
   }
   
   public void setDeployerName(String name)
   {
      name = name.equals("") ? "" : " "+name;
      this.name = name;
      this.log = new Log(getName());
   }
   
   public String getDeployerName()
   {
      return name.trim();
   }
   
   public void setJarDeployerName(String jarDeployerName)
   {
      this.jarDeployerName = jarDeployerName;
   }
   
   public String getJarDeployerName()
   {
      return jarDeployerName;
   }
   
   public void setWarDeployerName(String warDeployerName)
   {
      this.warDeployerName = warDeployerName;
   }

   public String getWarDeployerName()
   {
      return warDeployerName;
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
      
      Deployment d = installApplication (url);

	  try
	  {
		  startApplication (d);
		  log.log ("J2EE application: " + _url + " is deployed.");
      } 
      catch (Exception _e)
      {
		  try
		  {
			  stopApplication (d);          
		  }
		  catch (Exception _e2)
		  {
			  log.error("unable to stop application "+d.name+": "+_e2);
		  }
		  finally
		  {
			  try
			  {
				  uninstallApplication (_url);
			  }
			  catch (Exception _e3)
			  {
				  log.error("unable to uninstall application "+d.name+": "+_e3);
			  }
		  }

		  if (_e instanceof J2eeDeploymentException)
		  {
			  throw (J2eeDeploymentException)_e;
		  }
		  else
		  {
			  log.exception(_e);
			  throw new J2eeDeploymentException ("fatal error: "+_e);
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
	   Deployment d = installer.findDeployment (_app);
	   
	   if (d == null)
		   throw new J2eeDeploymentException ("The application \""+name+"\" has not been deployed.");

	   try
	   {
		   stopApplication (d);
	   }
	   catch (J2eeDeploymentException _e)
	   {
		   throw _e;
	   }
	   finally
	   {
		   uninstallApplication (d);
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
   public boolean isDeployed (String _url) throws MalformedURLException, J2eeDeploymentException
   {
	   boolean result = false;

	   Deployment d = installer.findDeployment (_url);

	   if (d != null)
	   {
		   result = checkApplication (d);          
	   }
	   
	   return result;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   public String getName()
   {
      return "J2EE Deployer" + this.name;
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return name == null ? new ObjectName(OBJECT_NAME+this.name) : name;
   }
   
   /** */
   protected void initService()
   throws Exception
   {
      URL tmpDirUrl = getClass().getResource("/tmp.properties");
      //check if the deployment dir was set meaningful
	   File dir = new File(new File(tmpDirUrl.getFile()).getParentFile(), "deploy/"+getDeployerName());
      if (!dir.exists () &&
          !dir.mkdirs ())
         throw new IOException ("Temporary directory \""+dir.getCanonicalPath ()+"\" does not exist!");
      
	  installer = new InstallerFactory(dir, log);

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
      log.log("Cleaning up deployment directory");
	  installer.unclutter();
   }
   
   
   /** undeploys all deployments */
   protected void stopService()
   {
      log.log ("Undeploying all applications.");
      
	  Deployment[] deps = installer.getDeployments();
      int count = 0;
      for (int i = 0, l = deps.length; i<l; ++i)
      {
		  try
		  {
			  stopApplication (deps[i]);
		  }   
		  catch (J2eeDeploymentException _e)
		  {
			  //throw _e;               
			  log.exception(_e);
		  }
		  finally 
		  {
				try {
					uninstallApplication (deps[i]);
				} catch (IOException _ioe)
				{log.exception(_ioe);}
          }
		  ++count;
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
	   return installer.install(_downloadUrl);
   }
   
   
   /** Deletes the file tree of  the specified application. <br>
   *   @param _name the directory (DEPLOYMENT_DIR/<_name> to remove recursivly
   *   @throws IOException if something goes wrong
   */
   void uninstallApplication (String _pattern) throws IOException
   {
	   Deployment d = installer.findDeployment (_pattern);

	   if (d != null)
		   uninstallApplication (d);
   }
   
	void uninstallApplication (Deployment _d) throws IOException
	{
		log.log ("Destroying application " + _d.name);
		installer.uninstall(_d);
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
         // Tomcat
         Iterator it = _d.webModules.iterator ();
		 if (it.hasNext() && !warDeployerAvailable())
			 throw new J2eeDeploymentException ("application contains war files but no web container available");
			 
		 
         while (it.hasNext ())
         {
            m = (Deployment.Module)it.next ();
            
            log.log ("Starting module " + m.name);
            
            // Call the TomcatDeployer that is loaded in the JMX server
            server.invoke(warDeployer, "deploy",
               new Object[] { m.webContext, m.localUrls.firstElement().toString ()}, new String[] { "java.lang.String", "java.lang.String" });
         }

		 // since tomcat changes the context classloader...
         Thread.currentThread().setContextClassLoader (oldCl);

         // JBoss
         it = _d.ejbModules.iterator ();
         while (it.hasNext ())
         {
            m = (Deployment.Module)it.next ();
            
            log.log ("Starting module " + m.name);
            
            // Call the ContainerFactory that is loaded in the JMX server
            server.invoke(jarDeployer, "deploy",
               new Object[] { m.localUrls.firstElement().toString () }, new String[] { "java.lang.String" });
         }       
	  }
      catch (MBeanException _mbe) {
         log.error ("Starting "+m.name+" failed!");
         throw new J2eeDeploymentException ("Error while starting "+m.name+": " + _mbe.getTargetException ().getMessage (), _mbe.getTargetException ());
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
   
   
   
   /** tests if the web container deployer is available */
   private boolean warDeployerAvailable ()
   {
      return server.isRegistered (warDeployer);
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
