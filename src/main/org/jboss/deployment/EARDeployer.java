/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.jboss.metadata.XmlFileLoader;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

import org.w3c.dom.Element;

// /*
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import javax.management.MBeanException;
import javax.management.JMException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;


import org.jboss.management.j2ee.J2EEApplication;

// */

/**
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.7 $
*/
public class EARDeployer
extends ServiceMBeanSupport
implements EARDeployerMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // my server to lookup for the special deployers
   // <comment author="cgjung">better be protected for subclassing </comment>
   protected MBeanServer server;
   
   // <comment author="cgjung">better be protected for subclassing </comment>
   protected String name;
   
   // Constructors --------------------------------------------------
   
   public EARDeployer()
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
   
   
   // Public --------------------------------------------------------
   
   public boolean accepts(DeploymentInfo di) 
   {
      return di.url.getFile().endsWith("ear");
   }
   
   
   public void init(DeploymentInfo di)
   throws DeploymentException
   {
      try
      {
         if (log.isInfoEnabled())
           log.info("Init J2EE application: " + di.url);
         
         InputStream in = di.localCl.getResourceAsStream("META-INF/application.xml");
         XmlFileLoader xfl = new XmlFileLoader();
         Element root = xfl.getDocument(in, "META-INF/application.xml").getDocumentElement();
         di.metaData = new J2eeApplicationMetaData(root);
         in.close();
         
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
            else di.watch = new URL(di.url, "META-INF/application.xml"); 
         }
      }
      catch (Exception e)
      {
         log.error("Error in init step of ear deployment", e);
         throw new DeploymentException("Error in accessing application metadata: ", e);
      }
      
      // Create the appropriate JSR-77 instance
//      /*  
      ObjectName lApplication = J2EEApplication.create(
         server,
         di.shortName,
         di.url
         // di.getApplicationDeploymentDescriptor()
      );
//      */
   }
   
   
   public void create(DeploymentInfo di)
      throws DeploymentException
   {
      // now try to deploy
      log.info("Deploying J2EE application, create step: " + di.url);
      // Create the appropriate JSR-77 instance
//      /*  
      ObjectName lApplication = J2EEApplication.create(
         server,
         di.shortName,
         di.url
         // di.getApplicationDeploymentDescriptor()
      );
//      */
   }
   
   public void start(DeploymentInfo di)
      throws DeploymentException
   {
      log.info("Deploying J2EE application, start step, a no-op: " + di.url);
   }
   
   public void stop(DeploymentInfo di) throws DeploymentException
   {
      log.info("Undeploying J2EE application, stop step, a no-op: " + di.url);
   }
   
   /**
    * Describe <code>destroy</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   public void destroy(DeploymentInfo di) throws DeploymentException
   {
      log.info("Undeploying J2EE application, destroy step: " + di.url);
//      /*
      // Destroy the appropriate JSR-77 instance
      J2EEApplication.destroy(
         server,
         di.shortName
      );
//      */
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
   
   protected void startService() throws Exception
   {
      try
      {
         // Register with the main deployer
         server.invoke(
            org.jboss.deployment.MainDeployerMBean.OBJECT_NAME,
            "addDeployer",
            new Object[] {this},
            new String[] {"org.jboss.deployment.DeployerMBean"});
      }
      catch (Exception e) {log.error("Could not register with MainDeployer", e);}
  
      log.info("EARDeployer started");
   }
   
   
      
   
   /** undeploys all deployments */
   protected void stopService()
   {
      log.info("EARDeployer stopped");
      
      try
      {
         // Register with the main deployer
         server.invoke(
            org.jboss.deployment.MainDeployerMBean.OBJECT_NAME,
            "removeDeployer",
            new Object[] {this},
            new String[] {"org.jboss.deployment.DeployerMBean"});
      }
      catch (Exception e) {log.error("Could not register with MainDeployer", e);}
  
   }
   
   // Private -------------------------------------------------------
}
