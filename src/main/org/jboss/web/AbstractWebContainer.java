/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.util.file.JarUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** A template pattern class for web container integration into JBoss. This class
 should be subclasses by web container providers wishing to integrate their
 container into a JBoss server.

 @see org.jboss.web.AbstractWebDeployer

 @jmx.mbean extends="org.jboss.deployment.SubDeployerMBean"

 @author  Scott.Stark@jboss.org
 @author  Christoph.Jung@infor.de
 @author  Thomas.Diesler@arcor.de
 @version $Revision: 1.84 $
 */
public abstract class AbstractWebContainer
   extends SubDeployerSupport
   implements AbstractWebContainerMBean
{
   public static final String DEPLOYER = "org.jboss.web.AbstractWebContainer.deployer";
   public static final String WEB_APP = "org.jboss.web.AbstractWebContainer.webApp";

   public static interface WebDescriptorParser
   {
      /** This method is called as part of subclass performDeploy() method implementations
       to parse the web-app.xml and jboss-web.xml deployment descriptors from a
       war deployment. The method creates the ENC(java:comp/env) env-entry,
       resource-ref, ejb-ref, etc element values. The creation of the env-entry
       values does not require a jboss-web.xml descriptor. The creation of the
       resource-ref and ejb-ref elements does require a jboss-web.xml descriptor
       for the JNDI name of the deployed resources/EJBs.

       Because the ENC context is private to the web application, the web
       application class loader is used to identify the ENC. The class loader
       is used because each war typically requires a unique class loader to
       isolate the web application classes/resources. This means that the
       ClassLoader passed to this method must be the thread context ClassLoader
       seen by the server/jsp pages during init/destroy/service/etc. method
       invocations if these methods interace with the JNDI ENC context.

       @param loader the ClassLoader for the web application. May not be null.
       @param metaData the WebMetaData from the WebApplication object passed to
       the performDeploy method.
       */
      public void parseWebAppDescriptors(ClassLoader loader, WebMetaData metaData) throws Exception;

      /** Get the DeploymentInfo for the war the triggered the deployment process.
       * The returned reference may be updated to affect the state of the
       * JBoss DeploymentInfo object. This can be used to assign ObjectNames
       * of MBeans created by the container.
       * @return The DeploymentInfo for the war being deployed.
       */
      public DeploymentInfo getDeploymentInfo();
   }

   /** A mapping of deployed warUrl strings to the WebApplication object */
   protected HashMap deploymentMap = new HashMap();
   /** The parent class loader first model flag */
   protected boolean java2ClassLoadingCompliance = false;
   /** A flag indicating if war archives should be unpacked */
   protected boolean unpackWars = true;

   /** If true, ejb-links that don't resolve don't cause an error (fallback to jndi-name) */
   protected boolean lenientEjbLink = false;

   public AbstractWebContainer()
   {
   }

   /** Get the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public boolean getJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }

   /** Set the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @param flag true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      java2ClassLoadingCompliance = flag;
   }

   /** Set the flag indicating if war archives should be unpacked. This may
    * need to be set to false as long extraction paths under deploy can
    * show up as deployment failures on some platforms.
    * 
    * @jmx.managed-attribute
    * @return true is war archives should be unpacked
    */
   public boolean getUnpackWars()
   {
      return unpackWars;
   }

   /** Get the flag indicating if war archives should be unpacked. This may
    * need to be set to false as long extraction paths under deploy can
    * show up as deployment failures on some platforms.
    * 
    * @jmx.managed-attribute
    * @param flag , true is war archives should be unpacked
    */
   public void setUnpackWars(boolean flag)
   {
      this.unpackWars = flag;
   }


   /**
    * Get the flag indicating if ejb-link errors should be ignored
    * in favour of trying the jndi-name in jboss-web.xml
    * @return the LenientEjbLink flag 
    *    
    * @jmx.managed-attribute
    */
   public boolean getLenientEjbLink()
   {
      return lenientEjbLink;
   }

   /**
    * Set the flag indicating if ejb-link errors should be ignored
    * in favour of trying the jndi-name in jboss-web.xml
    *    
    * @jmx.managed-attribute
    */
   public void setLenientEjbLink(boolean flag)
   {
      lenientEjbLink = flag;
   }

   public abstract AbstractWebDeployer getDeployer(DeploymentInfo di) throws Exception;

   public boolean accepts(DeploymentInfo sdi)
   {
      String warFile = sdi.url.getFile();
      return warFile.endsWith("war") || warFile.endsWith("war/");
   }

   public synchronized void init(DeploymentInfo di)
      throws DeploymentException
   {
      log.debug("Begin init");
      this.server = di.getServer();
      try
      {
         if (di.url.getPath().endsWith("/"))
         {
            // the URL is a unpacked collection, watch the deployment descriptor
            di.watch = new URL(di.url, "WEB-INF/web.xml");
         }
         else
         {
            // just watch the original URL
            di.watch = di.url;
         }

         // Make sure the war is unpacked if unpackWars is true
         File warFile = new File(di.localUrl.getFile());
         if( warFile.isDirectory() == false && unpackWars == true )
         {
            File tmp = new File(warFile.getAbsolutePath()+".tmp");
            if( warFile.renameTo(tmp) == false )
               throw new DeploymentException("Was unable to move war to: "+tmp);
            if( warFile.mkdir() == false )
               throw new DeploymentException("Was unable to mkdir: "+warFile);
            log.debug("Unpacking war to: "+warFile);
            FileInputStream fis = new FileInputStream(tmp);
            JarUtils.unjar(fis, warFile);
            fis.close();
            log.debug("Replaced war with unpacked contents");
            if( tmp.delete() == false )
               log.debug("Was unable to delete war tmp file");
            else
               log.debug("Deleted war archive");
            // Reset the localUrl to end in a '/'
            di.localUrl = warFile.toURL();
            // Reset the localCl to point to the file
            URL[] localCl = new URL[]{di.localUrl};
            di.localCl = new URLClassLoader(localCl);
         }

         WebMetaData metaData = new WebMetaData();
         metaData.setJava2ClassLoadingCompliance(this.java2ClassLoadingCompliance);
         di.metaData = metaData;
         
         // Check for a loader-repository
         XmlFileLoader xfl = new XmlFileLoader();
         InputStream in = di.localCl.getResourceAsStream("WEB-INF/jboss-web.xml");
         if( in != null )
         {
            try
            {
               Element jbossWeb = xfl.getDocument(in, "WEB-INF/jboss-web.xml").getDocumentElement();
               // Check for a war level class loading config
               Element classLoading = MetaData.getOptionalChild(jbossWeb, "class-loading");
               if( classLoading != null )
               {
                  String flagString = classLoading.getAttribute("java2ClassLoadingCompliance");
                  if( flagString.length() == 0 )
                     flagString = "true";
                  boolean flag = Boolean.valueOf(flagString).booleanValue();
                  metaData.setJava2ClassLoadingCompliance(flag);
                  // Check for a loader-repository for scoping
                  Element loader = MetaData.getOptionalChild(classLoading, "loader-repository");
                  if( loader != null )
                  {
                     LoaderRepositoryFactory.LoaderRepositoryConfig config = LoaderRepositoryFactory.parseRepositoryConfig(loader);
                     di.setRepositoryInfo(config);
                  }
               }
            }
            finally
            {
               in.close();
            }
         }

         // Generate an event for the initialization
         super.init(di);
      }
      catch (DeploymentException e)
      {
         log.error("Problem in init ", e);
         throw e;
      }
      catch (Exception e)
      {
         log.error("Problem in init ", e);
         throw new DeploymentException(e);
      }

      log.debug("End init");
   }

   public void create(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         AbstractWebDeployer deployer = getDeployer(di);
         di.context.put(DEPLOYER, deployer);

         // Generate an event for creation
         super.create(di);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Failed to obtain AbstractWebDeployer", e);
      }
   }

   /** A template pattern implementation of the deploy() method. This method
    calls the {@link #performDeploy(String, String) performDeploy()} method to
    perform the container specific deployment steps and registers the
    returned WebApplication in the deployment map. The steps performed are:

    ClassLoader appClassLoader = thread.getContextClassLoader();
    URLClassLoader warLoader = URLClassLoader.newInstance(empty, appClassLoader);
    thread.setContextClassLoader(warLoader);
    WebDescriptorParser webAppParser = ...;
    WebMetaData metaData = di.metaData;
    parseMetaData(ctxPath, warUrl, metaData);
    WebApplication warInfo = new WebApplication(metaData);
    performDeploy(warInfo, warUrl, webAppParser);
    deploymentMap.put(warUrl, warInfo);
    thread.setContextClassLoader(appClassLoader);

    The subclass performDeploy() implementation needs to invoke
    webAppParser.parseWebAppDescriptors(loader, warInfo) to have the JNDI
    java:comp/env namespace setup before any web app component can access
    this namespace.

    Also, an MBean for each servlet deployed should be created and its
    JMX ObjectName placed into the DeploymentInfo.mbeans list so that the
    JSR77 layer can create the approriate model view. The servlet MBean
    needs to provide access to the min, max and total time in milliseconds.
    Expose this information via MinServiceTime, MaxServiceTime and TotalServiceTime
    attributes to integrate seemlessly with the JSR77 factory layer.

    @param di The deployment info that contains the context-root element value
    from the J2EE application/module/web application.xml descriptor. This may
    be null if war was is not being deployed as part of an enterprise application.
    It also contains the URL of the web application war.
    */
   public synchronized void start(DeploymentInfo di) throws DeploymentException
   {
      AbstractWebDeployer deployer = (AbstractWebDeployer) di.context.get(DEPLOYER);
      // Get the war URL
      URL warURL = di.localUrl != null ? di.localUrl : di.url;
      WebApplication webApp = deployer.start(di);
      di.context.put(WEB_APP, webApp);
      deploymentMap.put(warURL, webApp);
      // Generate an event for the startup
      super.start(di);
   }

   /** WARs do not have nested deployments
    * @param di
    */
   protected void processNestedDeployments(DeploymentInfo di) throws DeploymentException
   {
   }

   /** A template pattern implementation of the undeploy() method. This method
    calls the {@link #performUndeploy(String) performUndeploy()} method to
    perform the container specific undeployment steps and unregisters the
    the warUrl from the deployment map.
    */
   public synchronized void stop(DeploymentInfo di)
      throws DeploymentException
   {
      AbstractWebDeployer deployer = (AbstractWebDeployer) di.context.get(DEPLOYER);
      try
      {
         URL warURL = di.localUrl != null ? di.localUrl : di.url;
         String warUrl = warURL.toString();
         WebApplication webApp = (WebApplication) deploymentMap.remove(warURL);
         if( deployer != null && webApp != null )
         {
            deployer.stop(di);
            // Generate an event for the stop
            super.stop(di);
         }
         else
         {
            log.debug("Failed to find deployer/deployment for war: "+warUrl);
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error during stop", e);
      }
   }

   /** See if a war is deployed.
    @jmx.managed-operation
    */
   public boolean isDeployed(String warUrl)
   {
      return deploymentMap.containsKey(warUrl);
   }

   /** Get the WebApplication object for a deployed war.
    @param warUrl the war url string as originally passed to deploy().
    @return The WebApplication created during the deploy step if the
    warUrl is valid, null if no such deployment exists.
    */
   public WebApplication getDeployedApp(String warUrl)
   {
      WebApplication appInfo = (WebApplication) deploymentMap.get(warUrl);
      return appInfo;
   }

   /** Returns the applications deployed by the web container subclasses.
    @jmx.managed-attribute
    @return An Iterator of WebApplication objects for the deployed wars.
    */
   public Iterator getDeployedApplications()
   {
      return deploymentMap.values().iterator();
   }

   /** An accessor for any configuration element set via setConfig. This
    method always returns null and must be overriden by subclasses to
    return a valid value.
    @jmx.managed-attribute
    */
   public Element getConfig()
   {
      return null;
   }

   /** This method is invoked to import an arbitrary XML configuration tree.
    Subclasses should override this method if they support such a configuration
    capability. This implementation does nothing.
    @jmx.managed-attribute
    */
   public void setConfig(Element config)
   {
   }

   /** Use reflection to access a URL[] getURLs method so that non-URLClassLoader
    *class loaders that support this method can provide info.
    */
   public static URL[] getClassLoaderURLs(ClassLoader cl)
   {
      URL[] urls = {};
      try
      {
         Class returnType = urls.getClass();
         Class[] parameterTypes = {};
         Method getURLs = cl.getClass().getMethod("getURLs", parameterTypes);
         if( returnType.isAssignableFrom(getURLs.getReturnType()) )
         {
            Object[] args = {};
            urls = (URL[]) getURLs.invoke(cl, args);
         }
         if( urls == null || urls.length == 0 )
         {
            getURLs = cl.getClass().getMethod("getAllURLs", parameterTypes);
            if( returnType.isAssignableFrom(getURLs.getReturnType()) )
            {
               Object[] args = {};
               urls = (URL[]) getURLs.invoke(cl, args);
            }
         }
      }
      catch(Exception ignore)
      {
      }
      return urls;
   }
   
   /** A utility method that walks up the ClassLoader chain starting at
    the given loader and queries each ClassLoader for a 'URL[] getURLs()'
    method from which a complete classpath of URL strings is built.
    */
   public String[] getCompileClasspath(ClassLoader loader)
   {
      HashSet tmp = new HashSet();
      ClassLoader cl = loader;
      while( cl != null )
      {
         URL[] urls = getClassLoaderURLs(cl);
         addURLs(tmp, urls);
         cl = cl.getParent();
      }
      try
      {
         URL[] globalUrls = (URL[])server.getAttribute(LoaderRepositoryFactory.DEFAULT_LOADER_REPOSITORY,
                                                         "URLs");
         addURLs(tmp, globalUrls);
      }
      catch (Exception e)
      {
         log.warn("Could not get global URL[] from default loader repository!");
      } // end of try-catch
      log.trace("JSP CompileClasspath: " + tmp);
      String[] cp = new String[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }
   private void addURLs(Set urlSet, URL[] urls)
   {
      for(int u = 0; u < urls.length; u ++)
      {
         URL url = urls[u];
         urlSet.add(url.toExternalForm());
      }
   }
}
