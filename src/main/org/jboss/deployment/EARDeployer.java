/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;

import org.jboss.logging.Logger;
import org.jboss.management.j2ee.J2EEApplication;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.file.JarUtils;

import org.w3c.dom.Element;


/**
 * Enterprise Archive Deployer.
 *
 * @jmx:mbean name="jboss.j2ee:service=EARDeployer"
 *            extends="org.jboss.deployment.SubDeployerMBean"
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.16 $
 */
public class EARDeployer
   extends SubDeployerSupport
   implements EARDeployerMBean
{
   public EARDeployer()
   {
      super();
   }
   
   public boolean accepts(DeploymentInfo di) 
   {
      String urlStr = di.url.getFile();
      return urlStr.endsWith("ear") || urlStr.endsWith("ear/");
   }
   
   public void init(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         log.info("Init J2EE application: " + di.url);
         
         InputStream in = di.localCl.getResourceAsStream("META-INF/application.xml");
         if( in == null )
            throw new DeploymentException("No META-INF/application.xml found");

         XmlFileLoader xfl = new XmlFileLoader();
         Element root = xfl.getDocument(in, "META-INF/application.xml").getDocumentElement();
         J2eeApplicationMetaData metaData = new J2eeApplicationMetaData(root);
         di.metaData = metaData;
         in.close();
         
         // resolve the watch
         if (di.url.getProtocol().equals("file"))
         {
            File file = new File(di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) {
               di.watch = di.url;
            }
            // If directory we watch the xml files
            else {
               di.watch = new URL(di.url, "META-INF/application.xml");
            }
         }
         else
         {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
         
         // Obtain the sub-deployment list
         File parentDir = null;
         String urlPrefix = null;
         if (di.isDirectory) 
         {
            parentDir = new File(di.localUrl.getFile());
         } 
         else
         {
            // Build a jar url to then ear archive entry
            urlPrefix = "jar:" + di.localUrl + "!/";
         }
         for (Iterator iter = metaData.getModules(); iter.hasNext(); )
         {
            J2eeModuleMetaData mod = (J2eeModuleMetaData)iter.next();
            String fileName = mod.getFileName();
            if (fileName != null && (fileName = fileName.trim()).length() > 0)
            {
               DeploymentInfo sub = null;
               if (di.isDirectory)
               {
                  File f = new File(parentDir, fileName);
                  sub = new DeploymentInfo(f.toURL(), di);
               }
               else
               {
                  String entryURL = urlPrefix + fileName;
                  URL jarURL = new URL(entryURL);
                  // Extract the nested jar from its ear
                  URL nestedURL = JarUtils.extractNestedJar(jarURL, super.tempDeployDir);
                  sub = new DeploymentInfo(nestedURL, di);

               }
               // Set the context-root on web modules
               if( mod.isWeb() )
                  sub.webContext = mod.getWebContext();
               log.debug("Deployment Info: " + sub + ", isDirectory: " + sub.isDirectory);
            }
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error in accessing application metadata: ", e);
      }
      
      // Create the appropriate JSR-77 instance, this has to be done in init
      // EAR create is called after sub-component creates that need this MBean
      ObjectName lApplication = J2EEApplication.create(
         server,
         di.shortName,
         di.localUrl
      );
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

      // Destroy the appropriate JSR-77 instance
      J2EEApplication.destroy(server, di.shortName);
   }
   
   
   // ServiceMBeanSupport overrides ---------------------------------

   /** 
    * @todo make this.name an attribute rather than appending 
    */
   protected ObjectName getObjectName(final MBeanServer server,
                                      final ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME: name;
   }
}
