/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

import java.beans.Beans;
import java.beans.beancontext.BeanContextServicesSupport;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.jboss.logging.Log;
import org.jboss.logging.ConsoleLogging;
import org.jboss.logging.ConsoleLoggingMBean;

import org.jboss.util.MBeanProxy;
import org.jboss.web.WebServiceMBean;

import org.jboss.ejb.plugins.*;

import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;

import org.jboss.system.EJBSecurityManager;
import org.jboss.system.RealmMapping;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.logging.Logger;



/**
*   A ContainerFactory is used to deploy EJB applications. It can be given a URL to
*  an EJB-jar or EJB-JAR XML file, which will be used to instantiate containers and make
*  them available for invocation.
*
*   @see Container
*   @author Rickard �berg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*
*   @version $Revision: 1.53 $
*/
public class ContainerFactory
    extends org.jboss.util.ServiceMBeanSupport
    implements ContainerFactoryMBean
{
   // Constants -----------------------------------------------------
   public static String DEFAULT_STATELESS_CONFIGURATION = "Default Stateless SessionBean";
   public static String DEFAULT_STATEFUL_CONFIGURATION = "Default Stateful SessionBean";
   public static String DEFAULT_ENTITY_BMP_CONFIGURATION = "Default BMP EntityBean";
   public static String DEFAULT_ENTITY_CMP_CONFIGURATION = "Default CMP EntityBean";

   // Attributes ----------------------------------------------------
   // Temp directory where deployed jars are stored
   File tmpDir;

   // The logger of this service
   Log log = new Log(getName());

   // A map of current deployments. If a deployment is made and it is already in this map,
   // then undeploy it first (i.e. make it a re-deploy).
   HashMap deployments = new HashMap();

   // Verify EJB-jar contents on deployments
   boolean verifyDeployments = false;
   boolean verifierVerbose   = false;

   // Public --------------------------------------------------------

   /**
   * Implements the abstract <code>getObjectName()</code> method in superclass
   * to return this service's name.
   *
   * @param   server
   * @param   name
   *
   * @exception MalformedObjectNameException
   * @return
   */
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }

   /**
   * Implements the abstract <code>getName()</code> method in superclass to
   * return the name of this object.
   *
   * @return <tt>'Container factory'</code>
   */
   public String getName()
   {
      return "Container factory";
   }

   /**
   * Implements the template method in superclass. This method inits the factory
   */
   public void initService()
   {
      URL tmpFile = getClass().getResource("/tmp.properties");
      if (tmpFile != null)
      {
         tmpDir = new File(new File(tmpFile.getFile()).getParent(),"deploy/");
         tmpDir.mkdirs();

         log.debug("Temporary directory set to:"+tmpDir);

         // Clear tmp directory of previously deployed files
         // This is to clear up if jBoss previously crashed, hence not removing files properly
         File[] files = tmpDir.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            files[i].delete();
         }

         if (files.length > 0)
         {
            log.debug("Previous deployments removed");
         }
      }
      else
      {
         log.debug("Using the systems temporary directory");
      }
   }

   /**
   * Implements the template method in superclass. This method stops all the
   * applications in this server.
   */
   public void stopService()
   {
      Iterator apps = deployments.values().iterator();
      while (apps.hasNext())
      {
         Application app = (Application)apps.next();
         app.stop();
      }
   }

   /**
   * Implements the template method in superclass. This method destroys all
   * the applications in this server and clears the deployments list.
   */
   public void destroyService()
   {
      Iterator apps = deployments.values().iterator();
      while (apps.hasNext())
      {
         Application app = (Application)apps.next();
         app.destroy();
      }

      deployments.clear();
   }

   /**
   * Enables/disables the application bean verification upon deployment.
   *
   * @param   verify  true to enable; false to disable
   */
   public void setVerifyDeployments(boolean verify)
   {
      verifyDeployments = verify;
   }

   /**
   * Returns the state of bean verifier (on/off)
   *
   * @return   true if enabled; false otherwise
   */
   public boolean getVerifyDeployments()
   {
      return verifyDeployments;
   }

   /**
   * Enables/disables the verbose mode on the verifier.
   *
   * @param   verbose  true to enable; false to disable
   */
   public void setVerifierVerbose(boolean verbose)
   {
      verifierVerbose = verbose;
   }

   /**
   * Returns the state of the bean verifier (verbose/non-verbose mode)
   *
   * @return true if enabled; false otherwise
   */
   public boolean getVerifierVerbose()
   {
      return verifierVerbose;
   }

   /**
   *   Deploy the file at this URL. This method is typically called from remote administration
   *   tools that cannot handle java.net.URL's as parameters to methods
   *
   * @param   url
   * @exception   MalformedURLException
   * @exception   DeploymentException
   */
   public void deploy(String url)
   throws MalformedURLException, DeploymentException
   {
      // Delegate to "real" deployment
      deploy(new URL(url));
   }


   /**
   *   Undeploy the file at this URL. This method is typically called from remote administration
   *   tools that cannot handle java.net.URL's as parameters to methods
   *
   * @param   url
   * @exception   MalformedURLException
   * @exception   DeploymentException
   */
   public void undeploy(String url)
   throws MalformedURLException, DeploymentException
   {
      // Delegate to "real" undeployment
      undeploy(new URL(url));
   }

   /**
   *   Deploy EJBs pointed to by an URL.
   *   The URL may point to an EJB-JAR, an EAR-JAR, or an codebase
   *   whose structure resembles that of an EJB-JAR. <p>
   *
   *   The latter is useful for development since no packaging is required.
   *
   * @param       url  URL where EJB deployment information is contained
   *
   * @exception   DeploymentException
   */
   public synchronized void deploy(URL url)
   throws DeploymentException
   {
      // Create application
      Application app = new Application();

      try
      {
         Log.setLog(log);

         // Check if already deployed -> undeploy first, this is re-deploy
         if (deployments.containsKey(url))
            undeploy(url);


         app.setURL(url);

         log.log("Deploying:"+url);

         // URL's to put in classloader
         URL[] urls;

         // save the name of the jar before copying -> undeploy with the same name
         URL origUrl = url;

         // copy the jar file to prevent locking - redeploy failure
         if (url.getProtocol().startsWith("file") && !url.getFile().endsWith("/"))
         {

            File jarFile = new File(url.getFile());
            File tmp;
            if (tmpDir == null)
            {
               tmp = File.createTempFile("tmpejbjar",".jar");
            }
            else
            {
               tmp = File.createTempFile("tmpejbjar",".jar", tmpDir);
            }
            tmp.deleteOnExit();
            FileInputStream fin = new FileInputStream(jarFile);
            byte[] bytes = new byte[(int)jarFile.length()];
            fin.read(bytes);
            FileOutputStream fout = new FileOutputStream(tmp);
            fout.write(bytes);
            fin.close();
            fout.close();

            // Get the URL's from the deployments Class-Path: manifest file.
            // These should be added to the classloader
            JarFile jar = new JarFile(tmp);
            Manifest mf = jar.getManifest();
            ArrayList urlList = new ArrayList();
            if (mf != null)
            {
               Attributes attributes = mf.getMainAttributes();
               String classPath = attributes.getValue(Attributes.Name.CLASS_PATH);
               if (classPath != null)
               {
                  StringTokenizer classPathTokens = new StringTokenizer(classPath, " ");
                  while (classPathTokens.hasMoreTokens())
                  {
                     String classPathEntry = classPathTokens.nextToken();
                     try
                     {

                        URL u;
                        File dir;

                        // Extension to "Class-Path:" format: dir/*
                        // add jar files in the dir to the classpath
                        if (classPathEntry.endsWith("/*"))
                        {
                           classPathEntry = classPathEntry.substring(0, classPathEntry.length() - 1);
                           dir = new File((new URL(url, classPathEntry)).getFile());
                           String[] files = dir.list();
                           for (int i = 0; i < files.length; i++)
                           {
                              if (files[i].endsWith(".jar") || files[i].endsWith(".zip"))
                              {
                                 urlList.add(new URL(dir.toURL(), files[i]));
                                 log.debug("Added " + dir + File.separator + files[i]);
                              }
                           }
                        }
                        else
                        {
                           urlList.add(new URL(url, classPathEntry));
                           log.debug("Added "+ classPathEntry);
                        }
                     } catch (MalformedURLException e)
                     {
                        log.error("Could not add " + classPathEntry);
                     }
                  }
               }
            }

            // Add URL to tmp file
            url = tmp.toURL();
            urlList.add(url);

            urls = new URL[urlList.size()];
            urls = (URL[])urlList.toArray(urls);
         }
         else
         {
            urls = new URL[] { url };
         }

         // Create the ClassLoader for this application
         // TODO : the ClassLoader should come from the JMX manager if we want to be able to share it (tomcat)
         ClassLoader cl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

         // Create a file loader with which to load the files
         XmlFileLoader efm = new XmlFileLoader();

         // the file manager gets its file from the classloader
         efm.setClassLoader(cl);

         // Load XML
         ApplicationMetaData metaData = efm.load();



         // Check validity
         Log.setLog(new Log("Verifier"));

         // wrapping this into a try - catch block to prevent errors in
         // verifier from stopping the deployment
         try {

            if (verifyDeployments)
            {
               BeanVerifier verifier = new BeanVerifier();

               verifier.addVerificationListener(new VerificationListener()
                  {
                  public void beanChecked(VerificationEvent event)
                  {
                     Logger.debug(event.getMessage());
                  }

                  public void specViolation(VerificationEvent event)
                  {
                     if (verifierVerbose)
                        Logger.log(event.getVerbose());
                     else
                        Logger.log(event.getMessage());
                  }
               });


               Logger.log("Verifying " + url);

               verifier.verify(url, metaData, cl);
            }
         }
         catch (Throwable t)
         {
            Logger.exception(t);
         }

         // unset verifier log
         Log.unsetLog();

         // Get list of beans for which we will create containers
         Iterator beans = metaData.getEnterpriseBeans();

         // Deploy beans
         Context ctx = new InitialContext();
         while(beans.hasNext())
         {
            BeanMetaData bean = (BeanMetaData)beans.next();

            log.log("Deploying "+bean.getEjbName());

            if (bean.isSession()) // Is session?
            {
               if (((SessionMetaData)bean).isStateless()) // Is stateless?
               {
                  // Create container
                  StatelessSessionContainer container = new StatelessSessionContainer();

                  // Create classloader for this container
                  // Only used to identify bean. Not really used for class loading!
                  container.setClassLoader(new URLClassLoader(new URL[0], cl));

                  // Set metadata
                  container.setBeanMetaData(bean);

                  // get the container configuration for this bean
                  // a default configuration is now always provided
                  ConfigurationMetaData conf = bean.getContainerConfiguration();

                  // Set transaction manager
                  container.setTransactionManager((TransactionManager)new InitialContext().lookup("TransactionManager"));

                  // Set security manager & role mapping manager
                  String securityManagerJNDIName = conf.getAuthenticationModule();
                  String roleMappingManagerJNDIName = conf.getRoleMappingManager();

                  if ((securityManagerJNDIName != null) && (roleMappingManagerJNDIName != null))
                  {
                     try
                     {
                        EJBSecurityManager ejbS = (EJBSecurityManager)new InitialContext().lookup(securityManagerJNDIName);
                        container.setSecurityManager( ejbS );
                     }
                     catch (NamingException ne)
                     {
                        throw new DeploymentException( "Could not find the Security Manager specified for this container", ne );
                     }

                     try
                     {
                        RealmMapping rM = (RealmMapping)new InitialContext().lookup(roleMappingManagerJNDIName);
                        container.setRealmMapping( rM );
                     }
                     catch (NamingException ne)
                     {
                        throw new DeploymentException( "Could not find the Role Mapping Manager specified for this container", ne );
                     }
                  }

                  // Set container invoker
                  ContainerInvoker ci = null;
                 try {
                    ci = (ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance();
                     } catch(Exception e) {
                    throw new DeploymentException("Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml)");
                     }
                 if (ci instanceof XmlLoadable) {
                   // the container invoker can load its configuration from the jboss.xml element
                   ((XmlLoadable)ci).importXml(conf.getContainerInvokerConf());
                     }
                 container.setContainerInvoker(ci);

                  // Set instance pool
                  InstancePool ip = null;
                 try {
                    ip = (InstancePool)cl.loadClass(conf.getInstancePool()).newInstance();
                     } catch(Exception e) {
                    throw new DeploymentException("Missing or invalid Instance Pool (in jboss.xml or standardjboss.xml)");
                     }
                  if (ip instanceof XmlLoadable) {
                   ((XmlLoadable)ip).importXml(conf.getContainerPoolConf());
                     }
                 container.setInstancePool(ip);

                  // Create interceptors

                  container.addInterceptor(new LogInterceptor());
                  container.addInterceptor(new SecurityInterceptor());

                  if (((SessionMetaData)bean).isContainerManagedTx())
                  {
                     // CMT
                     container.addInterceptor(new TxInterceptorCMT());
                     container.addInterceptor(new StatelessSessionInstanceInterceptor());

                  }
                  else
                  {
                     // BMT
                     container.addInterceptor(new StatelessSessionInstanceInterceptor());
                     container.addInterceptor(new TxInterceptorBMT());
                  }

                  // Finally we add the last interceptor from the container
                  container.addInterceptor(container.createContainerInterceptor());

                  // Add container to application
                  app.addContainer(container);
               }
               else // Stateful
               {
                  // Create container
                  StatefulSessionContainer container = new StatefulSessionContainer();

                  // Create classloader for this container
                  // Only used to identify bean. Not really used for class loading!
                  container.setClassLoader(new URLClassLoader(new URL[0], cl));

                  // Set metadata
                  container.setBeanMetaData(bean);

                  // Set transaction manager
                  container.setTransactionManager((TransactionManager)new InitialContext().lookup("TransactionManager"));

                  // Get container configuration
                  ConfigurationMetaData conf = bean.getContainerConfiguration();

                  // Set security manager & role mapping manager
                  String securityManagerJNDIName = conf.getAuthenticationModule();
                  String roleMappingManagerJNDIName = conf.getRoleMappingManager();

                  if ((securityManagerJNDIName != null) && (roleMappingManagerJNDIName != null))
                  {
                     try
                     {
                        EJBSecurityManager ejbS = (EJBSecurityManager)new InitialContext().lookup(securityManagerJNDIName);
                        container.setSecurityManager( ejbS );
                     }
                     catch (NamingException ne)
                     {
                        throw new DeploymentException( "Could not find the Security Manager specified for this container", ne );
                     }

                     try
                     {
                        RealmMapping rM = (RealmMapping)new InitialContext().lookup(roleMappingManagerJNDIName);
                        container.setRealmMapping( rM );
                     }
                     catch (NamingException ne)
                     {
                        throw new DeploymentException( "Could not find the Role Mapping Manager specified for this container", ne );
                     }
                  }

                  // Set container invoker
                  ContainerInvoker ci = null;
                 try {
                    ci = (ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance();
                     } catch(Exception e) {
                    throw new DeploymentException("Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml)");
                     }
                 if (ci instanceof XmlLoadable) {
                   // the container invoker can load its configuration from the jboss.xml element
                   ((XmlLoadable)ci).importXml(conf.getContainerInvokerConf());
                     }
                 container.setContainerInvoker(ci);

                  // Set instance cache
                  InstanceCache ic = null;
                 try {
                    ic = (InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance();
                     } catch(Exception e) {
                    throw new DeploymentException("Missing or invalid Instance Cache (in jboss.xml or standardjboss.xml)");
                     }
                 if (ic instanceof XmlLoadable) {
                   ((XmlLoadable)ic).importXml(conf.getContainerCacheConf());
                     }
                 container.setInstanceCache(ic);

                  // No real instance pool, use the shadow class
                  container.setInstancePool(new StatefulSessionInstancePool());

                  // Set persistence manager
                  container.setPersistenceManager((StatefulSessionPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());

                  // Create interceptors
                  container.addInterceptor(new LogInterceptor());

                  if (((SessionMetaData)bean).isContainerManagedTx())
                  {
                     // CMT
                     container.addInterceptor(new TxInterceptorCMT());
                     container.addInterceptor(new StatefulSessionInstanceInterceptor());

                  }
                  else
                  {
                     // BMT : the tx interceptor needs the context from the instance interceptor
                     container.addInterceptor(new StatefulSessionInstanceInterceptor());
                     container.addInterceptor(new TxInterceptorBMT());
                  }

                  container.addInterceptor(new SecurityInterceptor());

                  container.addInterceptor(container.createContainerInterceptor());

                  // Add container to application
                  app.addContainer(container);
               }
            }
            else // Entity
            {
               // Create container
               EntityContainer container = new EntityContainer();

               // Create classloader for this container
               // Only used to identify bean. Not really used for class loading!
               container.setClassLoader(new URLClassLoader(new URL[0], cl));

               // Set metadata
               container.setBeanMetaData(bean);

               // Set transaction manager
               container.setTransactionManager((TransactionManager)new InitialContext().lookup("TransactionManager"));

               // Get container configuration
               ConfigurationMetaData conf = bean.getContainerConfiguration();

               // Set security manager & role mapping manager
               String securityManagerJNDIName = conf.getAuthenticationModule();
               String roleMappingManagerJNDIName = conf.getRoleMappingManager();

               if ((securityManagerJNDIName != null) && (roleMappingManagerJNDIName != null))
               {
                  try
                  {
                     EJBSecurityManager ejbS = (EJBSecurityManager)new InitialContext().lookup(securityManagerJNDIName);
                     container.setSecurityManager( ejbS );
                  }
                  catch (NamingException ne)
                  {
                     throw new DeploymentException( "Could not find the Security Manager specified for this container", ne );
                  }

                  try
                  {
                     RealmMapping rM = (RealmMapping)new InitialContext().lookup(roleMappingManagerJNDIName);
                     container.setRealmMapping( rM );
                  }
                  catch (NamingException ne)
                  {
                     throw new DeploymentException( "Could not find the Role Mapping Manager specified for this container", ne );
                  }
               }

               // Set container invoker
               ContainerInvoker ci = null;
              try {
                 ci = (ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance();
                  } catch(Exception e) {
                 throw new DeploymentException("Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml)");
                  }
              if (ci instanceof XmlLoadable) {
                // the container invoker can load its configuration from the jboss.xml element
                ((XmlLoadable)ci).importXml(conf.getContainerInvokerConf());
                  }
              container.setContainerInvoker(ci);

               // Set instance cache
               InstanceCache ic = null;
              try {
                 ic = (InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance();
                  } catch(Exception e) {
                 throw new DeploymentException("Missing or invalid Instance Cache (in jboss.xml or standardjboss.xml)");
                  }
              if (ic instanceof XmlLoadable) {
                ((XmlLoadable)ic).importXml(conf.getContainerCacheConf());
                  }
              container.setInstanceCache(ic);

               // Set instance pool
               InstancePool ip = null;
              try {
                 ip = (InstancePool)cl.loadClass(conf.getInstancePool()).newInstance();
                  } catch(Exception e) {
                 throw new DeploymentException("Missing or invalid Instance Pool (in jboss.xml or standardjboss.xml)");
                  }
               if (ip instanceof XmlLoadable) {
                ((XmlLoadable)ip).importXml(conf.getContainerPoolConf());
                  }
              container.setInstancePool(ip);

               // Set persistence manager
               if (((EntityMetaData) bean).isBMP())
               {

                  //Should be BMPPersistenceManager
                  container.setPersistenceManager((EntityPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());
               }
               else
               {

                  // CMP takes a manager and a store
                  org.jboss.ejb.plugins.CMPPersistenceManager persistenceManager = new org.jboss.ejb.plugins.CMPPersistenceManager();

                  //Load the store from configuration
                  persistenceManager.setPersistenceStore((EntityPersistenceStore)cl.loadClass(conf.getPersistenceManager()).newInstance());

                  // Set the manager on the container
                  container.setPersistenceManager(persistenceManager);
               }

               // Create interceptors
               container.addInterceptor(new LogInterceptor());
               container.addInterceptor(new SecurityInterceptor());

               // entity beans are always CMT
               container.addInterceptor(new TxInterceptorCMT());

               container.addInterceptor(new EntityInstanceInterceptor());
               container.addInterceptor(new EntitySynchronizationInterceptor());

               container.addInterceptor(container.createContainerInterceptor());

               // Add container to application
               app.addContainer(container);
            }
         }

         // Init application
         app.init();

         // Start application
         app.start();

         // Add to webserver so client can access classes through dynamic class downloading
         //WebServiceMBean webServer = (WebServiceMBean)MBeanProxy.create(WebServiceMBean.class, WebServiceMBean.OBJECT_NAME);
         //webServer.addClassLoader(cl);

         // Done
         log.log("Deployed application: "+app.getName());

         // Register deployment. Use the original name in the hashtable
         deployments.put(origUrl, app);
      }
      catch (Exception e)
      {
         if (e instanceof NullPointerException)
         {
            // Avoids useless 'null' messages on a server trace.
            // Let's be honest and spam them with a stack trace.
            // NPE should be considered an internal server error anyways.
            Logger.exception(e);
         }

         Logger.exception(e);
         //Logger.debug(e.getMessage());

         app.stop();
         app.destroy();

         throw new DeploymentException("Could not deploy "+url.toString(), e);
      } finally
      {
         Log.unsetLog();
      }
   }


   /**
   *   Remove previously deployed EJBs.
   *
   * @param   url
   * @exception   DeploymentException
   */
   public void undeploy(URL url)
   throws DeploymentException
   {
      // Get application from table
      Application app = (Application)deployments.get(url);

      // Check if deployed
      if (app == null)
      {
         throw new DeploymentException("URL not deployed");
      }

      // Undeploy application
      Log.setLog(log);
      log.log("Undeploying:"+url);
      app.stop();
      app.destroy();

      // Remove deployment
      deployments.remove(url);

      // Done
      log.log("Undeployed application: "+app.getName());

      Log.unsetLog();
   }

   /**
   *   is the aplication with this url deployed
   *
   * @param   url
   * @exception   MalformedURLException
   */
   public boolean isDeployed(String url)
   throws MalformedURLException
   {
      return isDeployed (new URL (url));
   }

   /**
   *   check if the application with this url is deployed
   *
   * @param   url
   * @return true if deployed
   */
   public boolean isDeployed (URL url)
   {
      return (deployments.get(url) != null);
   }
}

