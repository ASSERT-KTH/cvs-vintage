/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.ObjectName;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.file.JarUtils;

import org.w3c.dom.Element;

/**
 * Enterprise Archive Deployer.
 *
 * @jmx:mbean name="jboss.j2ee:service=EARDeployer"
 *            extends="org.jboss.deployment.SubDeployerMBean"
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.37 $
 */
public class EARDeployer
   extends SubDeployerSupport
   implements EARDeployerMBean
{
   private ServiceControllerMBean serviceController;

   private boolean isolated = false;

   private boolean callByValue = false;
   
   public EARDeployer()
   {
      super();
   }
   
   /**
    * @jmx:managed-attribute
    * @return whether ear deployments should be isolated
    */
   public boolean isIsolated()
   {
      return isolated;
   }
   
   /**
    * @jmx:managed-attribute
    * @param isolated whether ear deployments should be isolated
    */
   public void setIsolated(boolean isolated)
   {
      this.isolated = isolated;
   }
   
   /**
    * @jmx:managed-attribute
    * @return whether ear deployments should be call by value
    */
   public boolean isCallByValue()
   {
      return callByValue;
   }
   
   /**
    * @jmx:managed-attribute
    * @param callByValue whether ear deployments should be call by value
    */
   public void setCallByValue(boolean callByValue)
   {
      this.callByValue = callByValue;
   }
   
   protected void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
      MBeanProxyExt.create(ServiceControllerMBean.class,
                           ServiceControllerMBean.OBJECT_NAME, server);
      super.startService();
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

         /* Don't require validation of application.xml since an ear may
         just contain a jboss sar specified in the jboss-app.xml descriptor.
         */
         XmlFileLoader xfl = new XmlFileLoader(false);
         J2eeApplicationMetaData metaData = new J2eeApplicationMetaData();
         Element application = xfl.getDocument(in, "META-INF/application.xml").getDocumentElement();
         metaData.importXml(application);
         di.metaData = metaData;
         in.close();

         // Check for a jboss-app.xml descriptor
         Element loader = null;
         in = di.localCl.getResourceAsStream("META-INF/jboss-app.xml");
         if( in != null )
         {
            // Create a new parser with validation enabled for jboss-app.xml
            xfl = new XmlFileLoader(true);
            Element jbossApp = xfl.getDocument(in, "META-INF/jboss-app.xml").getDocumentElement();
            in.close();
            // Import module/service archives to metadata
            metaData.importXml(jbossApp);
            // Check for a loader-repository for scoping
            loader = MetaData.getOptionalChild(jbossApp, "loader-repository");
         }
         initLoaderRepository(di, loader);

         // resolve the watch
         if (di.url.getProtocol().equals("file"))
         {
            File file = new File(di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory())
            {
               di.watch = di.url;
            }
            // If directory we watch the xml files
            else
            {
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
         HashMap extractedJars = new HashMap();

         if (di.isDirectory) 
         {
            parentDir = new File(di.localUrl.getFile());
         } 
         else
         {
            /* Extract each entry so that deployment modules can be processed
             and any manifest entries referenced by the ear modules are located
             in the same unpacked directory structure.
            */
            String urlPrefix = "jar:" + di.localUrl + "!/";
            JarFile jarFile = new JarFile(di.localUrl.getFile());
            // For each entry, test if deployable, if so
            // extract it and store the related URL in map
            for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
            {
               JarEntry entry = (JarEntry)e.nextElement();
               String name = entry.getName();
               try 
               {
                  URL url = new URL(urlPrefix + name);
                  if (isDeployable(name, url))
                  {
                     // Obtain a jar url for the nested jar
                     URL nestedURL = JarUtils.extractNestedJar(url, this.tempDeployDir);
                     // and store in it in map
                     extractedJars.put(name, nestedURL);
                     log.debug("Extracted deployable content: "+name);
                  }
                  else if( entry.isDirectory() == false )
                  {
                     URL nestedURL = JarUtils.extractNestedJar(url, this.tempDeployDir);
                     log.debug("Extracted non-deployable content: "+name);
                  }
               }
               catch (MalformedURLException mue)
               {
                  log.warn("Jar entry invalid. Ignoring: " + name, mue);
               }
               catch (IOException ex)
               {
                  log.warn("Failed to extract nested jar. Ignoring: " + name, ex);
               }
            }
         }

         // Create a top level JACC policy for linking app module policies
         String contextID = di.shortName;
         PolicyConfigurationFactory pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, true);
         di.context.put("javax.security.jacc.PolicyConfiguration", pc);

         // Create subdeployments for the ear modules
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
                  sub = new DeploymentInfo(f.toURL(), di, getServer());
               }
               else
               {
                  // The nested jar url was placed into extractedJars above
                  URL nestedURL = (URL) extractedJars.get(fileName);
                  if( nestedURL == null )
                     throw new DeploymentException("Failed to find module file: "+fileName);
                  sub = new DeploymentInfo(nestedURL, di, getServer());
               }

               // Set the context-root on web modules
               if( mod.isWeb() )
                  sub.webContext = mod.getWebContext();

               // Set the alternative deployment descriptor if there is one
               if (mod.alternativeDD != null)
                  sub.alternativeDD = mod.alternativeDD;

               log.debug("Deployment Info: " + sub + ", isDirectory: " + sub.isDirectory);
            }
         }
         EARDeployment earDeployment = new EARDeployment(di);
         String name = earDeployment.getJMXName();
         ObjectName objectName = new ObjectName(name);
         di.deployedObject = objectName;
         server.registerMBean(earDeployment, objectName);
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error in accessing application metadata: ", e);
      }

      super.init(di);
   }
   
   public void create(DeploymentInfo di) throws DeploymentException
   {
      super.create(di);

      // Create an MBean for the EAR deployment
      try
      {
         serviceController.create(di.deployedObject);
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Error during create of EARDeployment: " + di.url, e);
      }
   }
   
   public void start(DeploymentInfo di)
      throws DeploymentException
   {
      super.start (di);
      try
      {
         serviceController.start(di.deployedObject);
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Error during start of EARDeployment: " + di.url, e);
      }
      log.info ("Started J2EE application: " + di.url);
   }

   public void stop(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         serviceController.stop(di.deployedObject);
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Error during stop of EARDeployment: " + di.url, e);
      }
      super.stop(di);
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
      try
      {
         serviceController.destroy(di.deployedObject);
         serviceController.remove(di.deployedObject);
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Error during destroy of EARDeployment: " + di.url, e);
      }
      super.destroy(di);
   }

   /** Build the ear scoped repository
    *
    * @param di the deployment info passed to deploy
    * @param loader the jboss-app/loader-repository element
    * @throws Exception
    */
   protected void initLoaderRepository(DeploymentInfo di, Element loader)
      throws Exception
   {
      if (loader == null)
      {
         if (isolated && di.parent == null)
         {
            J2eeApplicationMetaData metaData = (J2eeApplicationMetaData) di.metaData;
            String name = EARDeployment.getJMXName(metaData, di) + ",extension=LoaderRepository";
            ObjectName objectName = new ObjectName(name); 

            LoaderRepositoryConfig config = new LoaderRepositoryFactory.LoaderRepositoryConfig();
            config.repositoryName = objectName;
            di.setRepositoryInfo(config);
         }
         return;
      }
         
      LoaderRepositoryConfig config = LoaderRepositoryFactory.parseRepositoryConfig(loader);
      di.setRepositoryInfo(config);
   }

   /**
    * Add -ds.xml and -service.xml as legitimate deployables.
    */
   protected boolean isDeployable(String name, URL url)
   {
      return super.isDeployable(name, url) ||
         name.endsWith("-ds.xml") ||
         name.endsWith("-service.xml") ||
         name.endsWith(".har");
   }

   /** Override the default behavior of looking into the archive for deployables
    * as only those explicitly listed in the application.xml and jboss-app.xml
    * should be deployed.
    *
    * @param di
    */
   protected void processNestedDeployments(DeploymentInfo di)
   {
   }
}
