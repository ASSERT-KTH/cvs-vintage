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
 *   @author Daniel Schulze (daniel.schulze@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class J2eeDeployer 
   extends ServiceMBeanSupport
   implements J2eeDeployerMBean
{
   // Constants -----------------------------------------------------
   public String DEPLOYMENT_DIR = "/home/deployment"; // default?
   public String CONFIG = "deployments.conf";   

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
   */
   public void deploy (String _url) throws MalformedURLException, J2eeDeploymentException
   {
   	URL url = new URL (_url);
   	
   	// undeploy first if it is a redeploy
   	if (deployments.containsKey (url))
   	   undeploy (_url);
      
      // now try to deploy
      log.log ("deploy j2ee application: " + _url);

      Deployment d = installApplication (url);
         
      deployments.put (d.downloadUrl, d);
      storeConfig ();
      log.log ("j2ee application: " + _url + " is deployed.");
   }
   
   /** Undeploys the given URL (if it is deployed).
   *   @param _url the url to to undeploy 
   *   @throws MalformedURLException in case of a malformed url
   *   @throws J2eeDeploymentException if something went wrong (but should have removed all files)
   */
   public void undeploy (String _url) throws MalformedURLException, J2eeDeploymentException
   {
   	URL url = new URL (_url);
   	
      log.log ("undeploy j2ee application: " + _url);
   	Deployment d = (Deployment)deployments.get (url);
   	if (d != null)
   	{
         try 
         {      
            uninstallApplication (d);
      	} 
      	catch (J2eeDeploymentException e)
      	{
      		throw e;
      	} 
      	finally
      	{
         	deployments.remove (url);
            storeConfig ();
            log.log ("j2ee application: " + _url + " is undeployed.");
         }
      } else
   	  log.warning (_url + " is NOT deployed!");
   }
   
   /** Checks if the given URL is currently deployed or not.
   *   @param _url the url to to check
   *   @return true if _url is deployed
   *   @throws MalformedURLException in case of a malformed url
   *   @throws J2eeDeploymentException if the app seems to be deployed, but some of its modules
   *   are not.
   */
   public boolean isDeployed (String _url) throws MalformedURLException, J2eeDeploymentException
   {
   	URL url = new URL (_url);

	   boolean deployed = deployments.containsKey (url);
	   if (deployed)
	   {
         try 
         {
   	   	// check all modules ...
   	   	Deployment d = (Deployment)deployments.get (url);
   
   	   	// jBoss
   	   	Iterator i = d.ejbModules.iterator ();
   	   	while (i.hasNext () && deployed)
   	   	{
   	   		Deployment.Module m = (Deployment.Module)i.next ();
   	   		// Call the ContainerFactory that is loaded in the JMX server
               Object result = server.invoke(jarDeployer, "isDeployed",
                                             new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
               deployed = ((Boolean)result).booleanValue ();
            }
   
   	   	// Tomcat
   	   	i = d.webModules.iterator ();
   	   	if (i.hasNext () && !warDeployerAvailable ())
   	   	   throw new J2eeDeploymentException ("the application containes web modules but the tomcat service isn't running?!");
   	   	
   	   	while (i.hasNext () && deployed)
   	   	{
   	   		Deployment.Module m = (Deployment.Module)i.next ();
               Object result = server.invoke(warDeployer, "isDeployed",
                                             new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
               deployed = ((Boolean)result).booleanValue ();
            }
      	   
      	   if (!deployed)	
      	      throw new J2eeDeploymentException ("The application is not correct deployed!");

         } 
         catch (MBeanException _mbe) {
         	throw new J2eeDeploymentException ("error while interacting with deployer MBeans... " + _mbe.getTargetException ().getMessage ());
         } catch (JMException _jme){
         	throw new J2eeDeploymentException ("fatal error while interacting with deployer MBeans... " + _jme.getMessage ());
         }
	   }
	   return deployed;
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
   	
   	  // load the configuration
   	  loadConfig ();

   }

   /** tries to redeploy all deployments before shutdown.*/
   protected void startService()
      throws Exception
   {
      if (!warDeployerAvailable ())
         log.log ("No war deployer found - only EJB deployment available...");


      log.log ("trying to redeploy all applications that were running before shutdown...");
   	Iterator it = deployments.values ().iterator ();
   	while (it.hasNext ())
   	{
    		Deployment d = (Deployment) it.next ();
   		try
   	   {
      		deploy (d.downloadUrl.toString ());
         }
         catch (J2eeDeploymentException _e)
         {
         	log.warning ("unable to redeploy application "+d.name+" -> delete it.");
         }
   	}
      log.log ("deployment state recovered.");
   }


   /** */
   protected void stopService()
   {
/*      // Uncomment method body to get redeploy only on server crash
      log.log ("undeploy all applications...");
   	Iterator it = deployments.values ().iterator ();
   	while (it.hasNext ())
   	{
    		Deployment d = (Deployment) it.next ();
   		try
   	   {
      		undeploy (d.downloadUrl.toString ());
         }
         catch (J2eeDeploymentException _e)
         {
         	log.warning ("hmmm... "+d.name+" dont wants to become undeployed?!");
         }
   	}
   	storeConfig ();
      log.log ("all applications undeployed.");
*/   }
    
   // Private -------------------------------------------------------

   /** Deploys all packages of the given deployment. <br>
   *   This means download the needed packages do some validation and/or
   *   other things and pass the references to the special deployers.
   *   <i> Validation and do some other things is not yet implemented </i>
   *   @param _d the deployment (= a J2ee application or module)
   */
   Deployment installApplication (URL _downloadUrl) throws J2eeDeploymentException
   {
      // determine the file type by trying to access one of the possible
      // deployment descriptors...
      Element root = null;
      String[] files = {"META-INF/ejb-jar.xml", "META-INF/application.xml", "WEB-INF/web.xml"};
      for (int i = 0; i < files.length && root == null; ++i)
      {
      	try 
      	{
            root = XmlFileLoader.getDocument (new URL ("jar:" + _downloadUrl.toString () + "!/" + files[i])).getDocumentElement ();
      	} catch (Exception _e) {}
      }

      // create a Deployment
      Deployment d = new Deployment ();
      try {
         // if we found a deployment descriptor
         if (root != null)
         {         
            d.name = d.getAppName (_downloadUrl);
            d.downloadUrl = _downloadUrl;
            StringBuffer sb = new StringBuffer ("file:");
            sb.append (DEPLOYMENT_DIR);
            sb.append ("/");
            // sb.append (d.getAdminCtx (d.downloadUrl));
            // sb.append ("/");
            sb.append (d.name);
            d.localUrl = new URL (sb.toString ()); // should never throw an URLEx

         	log.log ("create application " + d.name);


            // do the app type dependent stuff...
           	if ("ejb-jar".equals (root.getTagName ()))
           	{
           		// its a EJB.jar... just take the package
           		Deployment.Module m = d.newModule ();
               // localUrl: file:<download_dir> / <adminCtx> / <appName> / <appName>
               m.downloadUrl = d.downloadUrl;
               m.name = d.getFileName (m.downloadUrl);
               m.localUrl = new URL (d.localUrl.toString () + "/" + m.name);// should never throw an URLEx

            	log.log ("downloading module " + m.name);
               URLWizzard.download (m.downloadUrl, m.localUrl);
               d.ejbModules.add (m);      		
           	} 
           	else if ("web-app".equals (root.getTagName ()))
           	{
           		// its a WAR.jar... just take the package
           		if (!warDeployerAvailable ())
           		   throw new J2eeDeploymentException ("No war deployer available!");
           		
           		Deployment.Module m = d.newModule ();
               m.downloadUrl = d.downloadUrl;
               m.name = d.getFileName (m.downloadUrl);
               m.localUrl = new URL (d.localUrl.toString () + "/" + m.name);// should never throw an URLEx
               m.webContext = d.getWebContext (m.downloadUrl);

            	log.log ("downloading module " + m.name);
               URLWizzard.downloadAndInflate (m.downloadUrl, m.localUrl);
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
                  	// a EJB or WEB package...
                     Deployment.Module m = d.newModule ();
                     try 
                     {
                        m.downloadUrl = new URL ("jar:" + _downloadUrl.toString () + "!/" + mod.getFileName ());// chould throw an URLEx
                        m.name = d.getFileName (m.downloadUrl);
                        m.localUrl = new URL (d.localUrl.toString () + "/" + m.name);// chould throw an URLEx
                     } catch (MalformedURLException _mue) { 
                        throw new J2eeDeploymentException ("syntax error in module section in application DD : " + _mue.getMessage ());
                     }
                     
                     if (mod.isWeb ())
                     {
                  		if (!warDeployerAvailable ())
                  		   throw new J2eeDeploymentException ("No war deployer available!");

                        m.webContext = mod.getWebContext ();
                        if (m.webContext == null)
                           m.webContext = d.getWebContext (m.downloadUrl);
         
                     	log.log ("downloading and inflating module " + m.name);
                        URLWizzard.downloadAndInflate (m.downloadUrl, m.localUrl);
                        d.webModules.add (m);      		
                     }
                     else
                     {
                     	log.log ("downloading module " + m.name);
                        URLWizzard.download (m.downloadUrl, m.localUrl);
                        d.ejbModules.add (m);
                     }

                     // here the code for checking and DD replacment...
                     // ...
                     } 
               }
                  // other packages we dont care about (currently)
           	}
      	
			// set the context classloader for this application
			createContextClassLoader(d);
			
			// redirect all modules to the responsible deployer
			Deployment.Module m = null;
				
			try
            {
          		// jBoss
               Iterator it = d.ejbModules.iterator ();
               while (it.hasNext ())
               {
               	m = (Deployment.Module)it.next ();
                	log.log ("deploying module " + m.name);
            		// Call the ContainerFactory that is loaded in the JMX server
                  server.invoke(jarDeployer, "deploy",
                                  new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
               }
      
         		
         		// Tomcat
               it = d.webModules.iterator ();
               while (it.hasNext ())
               {
               	m = (Deployment.Module)it.next ();
                	log.log ("deploying module " + m.name);
            		// Call the TomcatDeployer that is loaded in the JMX server
                  server.invoke(warDeployer, "deploy",
                                  new Object[] { m.webContext, m.localUrl.toString ()}, new String[] { "java.lang.String", "java.lang.String" });
         		
            	}
            }
            catch (MBeanException _mbe) {
              	log.log ("deploying failed!");
       		   uninstallApplication (d);
            	throw new J2eeDeploymentException ("error while interacting with deployer MBeans... " + _mbe.getTargetException ().getMessage ());
            } catch (JMException _jme){
              	log.log ("deploying failed!");
       		   uninstallApplication (d);
            	throw new J2eeDeploymentException ("fatal error while interacting with deployer MBeans... " + _jme.getMessage ());
            }  			
            

            return d;
         }

 		} catch (IOException _ioe) {
 		   uninstallApplication (d);
 		   throw new J2eeDeploymentException ("Error in downloading module: " + _ioe.getMessage ());
 		} catch (DeploymentException _de) { 
 		   uninstallApplication (d);
 		   throw new J2eeDeploymentException ("Unable scan .ear file: " + _de.getMessage ());
 		} catch (Exception _e) { 
 		   _e.printStackTrace ();
 		   uninstallApplication (d);
 		   throw new J2eeDeploymentException ("FATAL ERROR!");
      }  			
      throw new J2eeDeploymentException (_downloadUrl.toString ()+" points not to a valid j2ee application (app.ear/ejb.jar/web.war)!");
   }

   void uninstallApplication (Deployment _d) throws J2eeDeploymentException
   {
    	log.log ("destroying application " + _d.name);
      String error = "";
      Iterator it = _d.ejbModules.iterator ();
      while (it.hasNext ())
      {
      	Deployment.Module m = (Deployment.Module)it.next ();
      	log.log ("uninstalling module " + m.name);
      	
   		//jBoss
         try
         {
      		// Call the ContainerFactory that is loaded in the JMX server
            server.invoke(jarDeployer, "undeploy",
                            new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
         }
         catch (MBeanException _mbe) {
         	log.warning ("unable to undeploy module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
         } catch (JMException _jme){
         	error += "[fatal error while interacting with deployer MBeans... " + _jme.getMessage () + "]";
     		}
      }
      it = _d.webModules.iterator ();
      while (it.hasNext ())
      {
      	Deployment.Module m = (Deployment.Module)it.next ();
      	log.log ("uninstalling module " + m.name);

    		// tomcat
     		if (warDeployerAvailable ())
     		{
            try
            {
               server.invoke(warDeployer, "undeploy",
                               new Object[] { m.localUrl.toString () }, new String[] { "java.lang.String" });
            }
            catch (MBeanException _mbe) {
            	log.warning ("unable to undeploy module " + m.name + ": " + _mbe.getTargetException ().getMessage ());
            } catch (JMException _jme){
            	error += "[fatal error while interacting with deployer MBeans... " + _jme.getMessage () + "]";
        		}
         }
      }   	
      
      try 
      {
         URLWizzard.deleteTree (_d.localUrl);      
      	log.log ("file tree "+_d.localUrl.toString ()+" deleted.");
      } catch (IOException _ioe) {
 		   throw new J2eeDeploymentException ("Error in removing local application files ("+_d.localUrl+"): " + _ioe.getMessage ());
      }

      if (!error.equals (""))
 		   throw new J2eeDeploymentException (error);
   }



   private void loadConfig ()
   {
      try {
   	ObjectInputStream in = new ObjectInputStream (new FileInputStream (DEPLOYMENT_DIR + File.separator + CONFIG));
   	deployments = (Hashtable)in.readObject ();
   	in.close ();
      } catch (Exception e)
      {
      	log.log ("no config file found...");
      }
   }

   private void storeConfig ()
   {
      try {
   	ObjectOutputStream out = new ObjectOutputStream (new FileOutputStream (DEPLOYMENT_DIR + File.separator + CONFIG));
   	out.writeObject (deployments);
   	out.close ();
      } catch (IOException e)
      {
      	log.warning ("storing config failed...");
      }
   }

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
