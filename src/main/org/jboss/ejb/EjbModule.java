/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Constructor;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.EJBLocalHome;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;

import org.jboss.ejb.plugins.AbstractInstanceCache;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;

import org.jboss.logging.Logger;

import org.jboss.management.j2ee.EJB;
import org.jboss.management.j2ee.EJBModule;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.metadata.XmlLoadable;

import org.jboss.mx.loading.UnifiedClassLoader;

import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;

import org.jboss.system.Registry;
import org.jboss.system.Service;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.util.NullArgumentException;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.ObjectNameFactory;

import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;

import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;

import org.w3c.dom.Element;

/**
 * An EjbModule represents a collection of beans that are deployed as a
 * unit.
 *
 * <p>The beans may use the EjbModule to access other beans within the same
 *    deployment unit.
 *
 * <p>The beans may use the EjbModule to access other beans within the same
 *    deployment package (e.g. an ear) using findContainer(String).
 *      
 * @see Container
 * @see EJBDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian.Brock</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @version $Revision: 1.38 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class EjbModule 
   extends ServiceMBeanSupport
   implements EjbModuleMBean
{
   public static final String BASE_EJB_MODULE_NAME ="jboss.j2ee:service=EjbModule";

   public static final ObjectName EJB_MODULE_QUERY_NAME = ObjectNameFactory.create(BASE_EJB_MODULE_NAME + ",*");

   public static String DEFAULT_STATELESS_CONFIGURATION = "Default Stateless SessionBean";
   public static String DEFAULT_STATEFUL_CONFIGURATION = "Default Stateful SessionBean";
   public static String DEFAULT_ENTITY_BMP_CONFIGURATION = "Default BMP EntityBean";
   public static String DEFAULT_ENTITY_CMP_CONFIGURATION = "Default CMP EntityBean";
   public static String DEFAULT_MESSAGEDRIVEN_CONFIGURATION = "Default MesageDriven Bean";
   
   // Constants uses with container interceptor configurations
   public static final int BMT = 1;
   public static final int CMT = 2;
   public static final int ANY = 3;
   
   static final String BMT_VALUE = "Bean";
   static final String CMT_VALUE = "Container";
   static final String ANY_VALUE = "Both";
   
   /** Class logger. */
   private static final Logger log = Logger.getLogger(EjbModule.class);
   
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   /** Stores the containers for this deployment unit. */
   HashMap containers = new HashMap();
   HashMap localHomes = new HashMap();
   
   /** Class loader of this deployment unit. */
   ClassLoader classLoader = null;
   
   /** Name of this deployment unit, url it was deployed from */
   final String name;
   
   private final DeploymentInfo deploymentInfo;   

   /** Module Object Name (JSR-77) **/
   private ObjectName moduleName;

   private ServiceControllerMBean serviceController;

   private final Map moduleData = 
      Collections.synchronizedMap(new HashMap());
   
   // Static --------------------------------------------------------
   
   /**
    * Stores a map of DeploymentInfos to EjbModules.
    * 
    * @todo this is silly, do something else.
    */
   private static HashMap ejbModulesByDeploymentInfo = new HashMap();

   // Public --------------------------------------------------------

   //constructor with mbeanserver

   public EjbModule(final DeploymentInfo di)
   {
      this.deploymentInfo = di;
      String name = deploymentInfo.url.toString();
      if (name.endsWith("/"))
      {
         name = name.substring(0, name.length() - 1);
      }
      this.name = name;
   }

   public Map getModuleDataMap()
   {
      return moduleData;
   }
   
   public Object getModuleData(Object key)
   {
      return moduleData.get(key);
   }
   
   public void putModuleData(Object key, Object value)
   {
      moduleData.put(key, value);
   }
   
   public void removeModuleData(Object key)
   {
      moduleData.remove(key);
   }
 
   /**
    * Add a container to this deployment unit.
    *
    * @param   con  
    */
   private void addContainer(Container con)
   {
      String ejbName = con.getBeanMetaData().getEjbName();
      if(containers.containsKey(ejbName))
         log.warn("Duplicate ejb-name. Container for " + ejbName + " already exists.");
      containers.put(ejbName, con);
      con.setEjbModule(this);
   }

   /**
    * Remove a container from this deployment unit.
    *
    * @param   con  
    */
   public void removeContainer(Container con)
   {
      containers.remove(con.getBeanMetaData().getEjbName());
   }
   
   public void addLocalHome(Container con, EJBLocalHome localHome)
   {
      localHomes.put(con.getBeanMetaData().getEjbName(), localHome);
   }
   
   public void removeLocalHome(Container con)
   {
      localHomes.remove(con.getBeanMetaData().getEjbName());
   }
   
   public EJBLocalHome getLocalHome(Container con)
   {
      return (EJBLocalHome)localHomes.get(con.getBeanMetaData().getEjbName());
   }

   /**
    * Get a container from this deployment unit that corresponds to a given name
    *
    * @param   name  ejb-name name defined in ejb-jar.xml
    *
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   public Container getContainer(String name)
   {
      return (Container)containers.get(name);
   }

   /**
    * Get all containers in this deployment unit.
    *
    * @return  a collection of containers for each enterprise bean in this 
    *          deployment unit.
    * @jmx:managed-attribute
    */
   public Collection getContainers()
   {
      return containers.values();
   }

   /**
    * Get the class loader of this deployment unit. 
    *
    * @return     
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Find a container from this deployment package, used to process ejb-link
    *
    * @param   name  ejb-name name defined in ejb-jar.xml in some jar in
    *          the same deployment package
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   public Container findContainer(String name)
      throws DeploymentException
   {
      // Quick check
      Container result = (Container)containers.get(name);
      if (result != null)
      {
         //It is in this module
         return result;
      }
      // Does the name include a path?
      if (name.indexOf('#') != -1) 
      {
         return locateContainerByPath(name);
      } // end of if ()
      
      // Ok, we have to walk the tree
      return locateContainer(name);
   }

   /**
    * Set the class loader of this deployment unit
    *
    * @param   name  
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }
   
   /**
    * Get the URL from which this deployment unit was deployed
    *
    * @return    The URL from which this Application was deployed.
    */
   public URL getURL()
   {
      return deploymentInfo.url;
   }
        
   public ObjectName getModuleName() 
   {
      return moduleName;
   }
   
   public void setModuleName(final ObjectName moduleName) 
   {
      if (moduleName == null)
         throw new NullArgumentException("moduleName");
      
      this.moduleName = moduleName;
   }
        
   // Service implementation ----------------------------------------
   
   protected void createService() throws Exception 
   {
      // Keep track of which deployments are ejbModules
      synchronized(ejbModulesByDeploymentInfo)
      {
         ejbModulesByDeploymentInfo.put(deploymentInfo, this);
      }

      serviceController = (ServiceControllerMBean)
         MBeanProxy.create(ServiceControllerMBean.class,
                           ServiceControllerMBean.OBJECT_NAME,
                           server);
      boolean debug = log.isDebugEnabled();
      log.debug( "Application.start(), begin" );
  
      // Create JSR-77 EJB-Module
      int sepPos = name.lastIndexOf( "/" );
      String lName = name.substring(sepPos >= 0 ? sepPos + 1 : 0);
      
      ObjectName lModule = EJBModule.create(
            server,
            ( deploymentInfo.parent == null ? null : deploymentInfo.parent.shortName ),
            lName,
            deploymentInfo.localUrl,
            getServiceName()
            );
      log.debug("Created module: " + lModule);
      
      if( lModule != null ) 
      {
         setModuleName( lModule );
      }
      
      //Set up the beans in this module.
      try 
      {
         for (Iterator beans = ((ApplicationMetaData) deploymentInfo.metaData).getEnterpriseBeans(); beans.hasNext(); ) 
         {
            BeanMetaData bean = (BeanMetaData) beans.next();
            
            log.info( "Deploying " + bean.getEjbName() );

            Container con = createContainer(bean, deploymentInfo);
            addContainer(con);
         }
         
         //only one iteration should be necessary, but we won't sweat it.   
         //2 iterations are needed by cmp...jdbc/bridge/JDBCCMRFieldBridge which 
         //assumes persistence managers are all set up for every
         //bean in the relationship!
         for (Iterator i = containers.values().iterator(); i.hasNext();)
         {
            Container con = (Container)i.next();
            ObjectName jmxName= con.getJmxName();
            /* Add the container mbean to the deployment mbeans so the state
             of the deployment can be tracked.
            */
            deploymentInfo.mbeans.add(jmxName);
            server.registerMBean(con, jmxName);
            BeanMetaData metaData = con.getBeanMetaData();
            Collection depends = metaData.getDepends();
            serviceController.create(jmxName, depends);
            // Create JSR-77 EJB-Wrapper
            log.debug( "Application.create(), create JSR-77 EJB-Component" );
            //BeanMetaData lMetaData = con.getBeanMetaData();
            int lType =
               metaData.isSession() ?
               ( ( (SessionMetaData) metaData ).isStateless() ? 2 : 1 ) :
               ( metaData.isMessageDriven() ? 3 : 0 );
            ObjectName lEJB = EJB.create(
               server,
               getModuleName() + "",
               lType,
               metaData.getJndiName(),
               jmxName
               );
            log.debug( "Application.start(), EJB: " + lEJB );
            if( lEJB != null ) {
               con.mEJBObjectName = lEJB.toString();
            }
            // We keep the hashCode around for fast creation of proxies
            int jmxHash = jmxName.hashCode();
            Registry.bind(new Integer(jmxHash), jmxName);
            log.debug("Bound jmxName="+jmxName+", hash="+jmxHash+"into Registry");
         }
      }
      catch (Exception e)
      {
         destroyService();
         throw e;
      } // end of try-catch
      
   }

   /**
    * The mbean Service interface <code>start</code> method calls
    * the start method on each contatiner, then the init method on each 
    * container.  Conversion to a different registration system with one-phase 
    * startup is conceivable.
    *
    * @exception Exception if an error occurs
    */
   protected void startService() throws Exception
   {
      boolean debug = log.isDebugEnabled();
      
      for (Iterator i = containers.values().iterator(); i.hasNext();)
      {
         Container con = (Container)i.next();
         if (debug) {
            log.debug( "Application.start(), start container: " + con );
         }
         serviceController.start(con.getJmxName());
      }
   }
        
   /**
    * Stops all the containers of this application.
    */
   protected void stopService() throws Exception
   {
      for (Iterator i = containers.values().iterator(); i.hasNext();)
      {
         Container con = (Container)i.next();
         try 
         {
            serviceController.stop(con.getJmxName());
         }
         catch (Exception e)
         {
            log.error("unexpected exception stopping Container: " + con.getJmxName(), e);
         } // end of try-catch
      }
   }

   protected void destroyService() throws Exception
   {
      WebServiceMBean webServer = 
         (WebServiceMBean)MBeanProxy.create(WebServiceMBean.class, 
                                            WebServiceMBean.OBJECT_NAME);
      for (Iterator i = containers.values().iterator(); i.hasNext();)
      {
         Container con = (Container)i.next();
         ObjectName jmxName =  con.getJmxName();
         int jmxHash = jmxName.hashCode();
         Registry.unbind(new Integer(jmxHash));
         // Unregister the web classloader
         //Removing the wcl should probably be done in stop of the container,
         // but I don't want to look for errors today.
         //Certainly the attempt needs to be before con.destroy 
         //where the reference is discarded.
         ClassLoader wcl = con.getWebClassLoader();
         if( wcl != null )
         {
            try
            {
               webServer.removeClassLoader(wcl);
            }
            catch(Throwable e)
            {
               log.warn("Failed to unregister webClassLoader", e);
            }
         }
         // Remove JSR-77 EJB-Wrapper
         if( con.mEJBObjectName != null )
         {
            EJB.destroy( con.getServer(), con.mEJBObjectName );
         }
         try 
         {
            serviceController.destroy(jmxName);
         }
         catch (Throwable e)
         {
            log.error("unexpected exception destroying Container: " + jmxName, e);
         } // end of try-catch
         try 
         {
            serviceController.remove(jmxName);
         }
         catch (Throwable e)
         {
            log.error("unexpected exception destroying Container: " + jmxName, e);
         } // end of try-catch
      }
      
      log.info( "Remove JSR-77 EJB Module: " + getModuleName() );
      if (getModuleName() != null) 
      {  
         EJBModule.destroy(server, getModuleName().toString() );
      }

      // Keep track of which deployments are ejbModules
      synchronized(ejbModulesByDeploymentInfo)
      {
         ejbModulesByDeploymentInfo.remove(deploymentInfo);
      }

      this.containers.clear();
      this.localHomes.clear();
   }

   // ******************
   // Container Creation
   // ******************
   
   private Container createContainer( BeanMetaData bean, DeploymentInfo sdi)
      throws Exception
   {
      ClassLoader cl = sdi.ucl;
      ClassLoader localCl = sdi.localCl;
      
      Container container = null;
      // Added message driven deployment
      if( bean.isMessageDriven() )
      {
         container = createMessageDrivenContainer( bean, cl, localCl );
      }
      else if( bean.isSession() )   // Is session?
      {
         if( ( (SessionMetaData) bean ).isStateless() )   // Is stateless?
         {
            container = createStatelessSessionContainer( bean, cl, localCl );
         }
         else   // Stateful
         {
            container = createStatefulSessionContainer( bean, cl, localCl );
         }
      }
      else   // Entity
      {
         container = createEntityContainer( bean, cl, localCl );
      }
      return container;
   }
   
   private MessageDrivenContainer createMessageDrivenContainer( BeanMetaData bean,
                                                                ClassLoader cl,
                                                                ClassLoader localCl )
      throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Stolen from Stateless deploy
      // Create container
      MessageDrivenContainer container = new MessageDrivenContainer();
      int transType = bean.isContainerManagedTx() ? CMT : BMT;
      
      initializeContainer( container, conf, bean, transType, cl, localCl );
      createProxyFactories(bean, container, cl);
      container.setInstancePool( createInstancePool( conf, cl ) );
      
      return container;
   }
   
   private StatelessSessionContainer createStatelessSessionContainer( BeanMetaData bean,
                                                                      ClassLoader cl,
                                                                      ClassLoader localCl )
      throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Create container
      StatelessSessionContainer container = new StatelessSessionContainer();
      int transType = bean.isContainerManagedTx() ? CMT : BMT;
      initializeContainer( container, conf, bean, transType, cl, localCl );
      if (bean.getHome() != null)
      {
         createProxyFactories(bean, container, cl);
      }
      container.setInstancePool( createInstancePool( conf, cl ) );
      
      return container;
   }
   
   private StatefulSessionContainer createStatefulSessionContainer( BeanMetaData bean,
                                                                    ClassLoader cl,
                                                                    ClassLoader localCl )
      throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Create container
      StatefulSessionContainer container = new StatefulSessionContainer();
      int transType = bean.isContainerManagedTx() ? CMT : BMT;
      initializeContainer( container, conf, bean, transType, cl, localCl );
      if (bean.getHome() != null)
      {
         createProxyFactories(bean, container, cl);
      }
      container.setInstanceCache( createInstanceCache( conf, false, cl ) );
      // No real instance pool, use the shadow class
      container.setInstancePool( new StatefulSessionInstancePool() );
      // Set persistence manager
      container.setPersistenceManager( (StatefulSessionPersistenceManager) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
      //Set the bean Lock Manager
      container.setLockManager(createBeanLockManager(container, false,conf.getLockConfig(), cl));
      
      return container;
   }
   
   private EntityContainer createEntityContainer( BeanMetaData bean,
                                                  ClassLoader cl,
                                                  ClassLoader localCl )
      throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Create container
      EntityContainer container = new EntityContainer();
      int transType = CMT;
      initializeContainer( container, conf, bean, transType, cl, localCl );
      if (bean.getHome() != null)
      {
         createProxyFactories(bean, container, cl);
      }
      container.setInstanceCache( createInstanceCache( conf, false, cl ) );
      container.setInstancePool( createInstancePool( conf, cl ) );
      //Set the bean Lock Manager
      container.setLockManager(createBeanLockManager(container, ((EntityMetaData) bean).isReentrant(),conf.getLockConfig(), cl));
      
      // Set persistence manager
      if( ( (EntityMetaData) bean ).isBMP() )
      {
         //Should be BMPPersistenceManager
         container.setPersistenceManager( (EntityPersistenceManager) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
      }
      else
      {
         // CMP takes a manager and a store
         org.jboss.ejb.plugins.CMPPersistenceManager persistenceManager =
            new org.jboss.ejb.plugins.CMPPersistenceManager();
         
         //Load the store from configuration
         persistenceManager.setPersistenceStore( (EntityPersistenceStore) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
         // Set the manager on the container
         container.setPersistenceManager( persistenceManager );
      }
      
      return container;
   }
   
   // **************
   // Helper Methods
   // **************
   
   /**
    * Perform the common steps to initializing a container.
    */
   private void initializeContainer( Container container,
                                     ConfigurationMetaData conf,
                                     BeanMetaData bean,
                                     int transType,
                                     ClassLoader cl,
                                     ClassLoader localCl )
      throws NamingException, DeploymentException
   {
      // Create local classloader for this container
      // For loading resources that must come from the local jar.  Not for loading classes!
      container.setLocalClassLoader( new URLClassLoader( new URL[ 0 ], localCl ) );
      // Set metadata (do it *before* creating the container's WebClassLoader)
      container.setEjbModule( this );
      container.setBeanMetaData( bean );

      // Create the container's WebClassLoader 
      // and register it with the web service.
      String webClassLoaderName = getWebClassLoader(conf, bean);
      log.debug("Creating WebClassLoader of class " + webClassLoaderName);
      WebClassLoader wcl = null;
      try 
      {
         Class clazz = cl.loadClass(webClassLoaderName);
         Constructor constructor = clazz.getConstructor(
            new Class[] { ObjectName.class, UnifiedClassLoader.class } );
         wcl = (WebClassLoader)constructor.newInstance(
            new Object[] { container.getJmxName(), cl });
      }
      catch (Exception e) 
      {
         throw new DeploymentException(
            "Failed to create WebClassLoader of class " 
            + webClassLoaderName + ": ", e);
      }
      WebServiceMBean webServer = 
         (WebServiceMBean)MBeanProxy.create(WebServiceMBean.class, 
                                            WebServiceMBean.OBJECT_NAME);
      URL[] codebase = { webServer.addClassLoader(wcl) };
      wcl.setWebURLs(codebase);
      container.setWebClassLoader(wcl);
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < codebase.length; i++)
      {
         sb.append(codebase[i].toString());
         if (i < codebase.length - 1)
         {
            sb.append(" ");
         }
      }
      container.setCodebase(sb.toString());

      // Create classloader for this container
      // Only used to unique the bean ENC and does not augment class loading
      container.setClassLoader( new URLClassLoader( new URL[ 0 ], wcl ) );

      // Set transaction manager
      InitialContext iniCtx = new InitialContext();
      container.setTransactionManager( (TransactionManager) iniCtx.lookup( "java:/TransactionManager" ) );
      
      // Set security domain manager
      String securityDomain = bean.getApplicationMetaData().getSecurityDomain();
      String confSecurityDomain = conf.getSecurityDomain();
      // Default the config security to the application security manager
      if( confSecurityDomain == null )
         confSecurityDomain = securityDomain;
      // Check for an empty confSecurityDomain which signifies to disable security
      if( confSecurityDomain != null && confSecurityDomain.length() == 0 )
         confSecurityDomain = null;
      if( confSecurityDomain != null )
      {   // Either the application has a security domain or the container has security setup
         try
         {
            log.debug("Setting security domain from: "+confSecurityDomain);
            Object securityMgr = iniCtx.lookup(confSecurityDomain);
            AuthenticationManager ejbS = (AuthenticationManager) securityMgr;
            RealmMapping rM = (RealmMapping) securityMgr;
            container.setSecurityManager( ejbS );
            container.setRealmMapping( rM );
         }
         catch(NamingException e)
         {
            throw new DeploymentException("Could not find the security-domain, name="+confSecurityDomain, e);
         }
         catch(Exception e)
         {
            throw new DeploymentException("Invalid security-domain specified, name="+confSecurityDomain, e);
         }
      }

      // Load the security proxy instance if one was configured
      String securityProxyClassName = bean.getSecurityProxy();
      if( securityProxyClassName != null )
      {
         try
         {
            Class proxyClass = cl.loadClass(securityProxyClassName);
            Object proxy = proxyClass.newInstance();
            container.setSecurityProxy(proxy);
            log.debug("setSecurityProxy, "+proxy);
         }
         catch(Exception e)
         {
            throw new DeploymentException("Failed to create SecurityProxy of type: " +
                                          securityProxyClassName, e);
         }
      }
      
      // Install the container interceptors based on the configuration
      addInterceptors(container, transType, conf.getContainerInterceptorsConf());
   }

   /**
    * Return the name of the WebClassLoader class for this ejb.
    */
   private static String getWebClassLoader(ConfigurationMetaData conf, 
                                           BeanMetaData bmd)
      throws DeploymentException
   {
      String webClassLoader = null;
      Iterator it = bmd.getInvokerBindings();
      int count = 0;
      while (it.hasNext())
      {
         String invoker = (String)it.next();
         ApplicationMetaData amd = bmd.getApplicationMetaData();
         InvokerProxyBindingMetaData imd = (InvokerProxyBindingMetaData)
                           amd.getInvokerProxyBindingMetaDataByName(invoker);
         Element proxyFactoryConfig = imd.getProxyFactoryConfig();
         String webCL = MetaData.getOptionalChildContent(proxyFactoryConfig, 
                                                         "web-class-loader");
         if (webCL != null)
         {
            log.debug("Invoker " + invoker 
                      + " specified WebClassLoader class" + webCL);
            webClassLoader = webCL;
            count++;
         }
      }
      if (count > 1) {
         log.warn(count + " invokers have WebClassLoader specifications.");
         log.warn("Using the last specification seen (" 
                  + webClassLoader + ")."); 
      }
      else if (count == 0) {
         webClassLoader = conf.getWebClassLoader();
      }
      return webClassLoader;
   }
   
   /**
    * Given a container-interceptors element of a container-configuration,
    * add the indicated interceptors to the container depending on the container
    * transcation type and metricsEnabled flag.
    *
    *
    * @todo marcf: frankly the transaction type stuff makes no sense to me, we have externalized
    * the container stack construction in jbossxml and I don't see why or why there would be a 
    * type missmatch on the transaction
    * 
    * @param container   the container instance to setup.
    * @param transType   one of the BMT, CMT or ANY constants.
    * @param element     the container-interceptors element from the
    *                    container-configuration.
    */
   private void addInterceptors(Container container,
                                int transType,
                                Element element)
      throws DeploymentException
   {
      // Get the interceptor stack(either jboss.xml or standardjboss.xml)
      Iterator interceptorElements = MetaData.getChildrenByTagName(element, "interceptor");
      String transTypeString = stringTransactionValue(transType);
      ClassLoader loader = container.getClassLoader();
      /* First build the container interceptor stack from interceptorElements
         match transType and metricsEnabled values
      */
      ArrayList istack = new ArrayList();
      while( interceptorElements != null && interceptorElements.hasNext() )
      {
         Element ielement = (Element) interceptorElements.next();
         /* Check that the interceptor is configured for the transaction mode of the bean
            by comparing its 'transaction' attribute to the string representation
            of transType
            FIXME: marcf, WHY???????
         */
         String transAttr = ielement.getAttribute("transaction");
         if( transAttr == null || transAttr.length() == 0 )
            transAttr = ANY_VALUE;
         if( transAttr.equalsIgnoreCase(ANY_VALUE) || transAttr.equalsIgnoreCase(transTypeString) )
         {   // The transaction type matches the container bean trans type, check the metricsEnabled
            String metricsAttr = ielement.getAttribute("metricsEnabled");
            boolean metricsInterceptor = metricsAttr.equalsIgnoreCase("true");
            try 
            {
               boolean metricsEnabled = ((Boolean)server.getAttribute(EJBDeployerMBean.OBJECT_NAME,
                                                                      "MetricsEnabled")).booleanValue();
               if( metricsEnabled == false && metricsInterceptor == true )
               {
                  continue;
               }
            }
            catch (Exception e)
            {
               throw new DeploymentException("couldn't contace EJBDeployer!", e);
            } // end of try-catch
            
           
            String className = null;
            try
            {
               className = MetaData.getElementContent(ielement);
               Class clazz = loader.loadClass(className);
               Interceptor interceptor = (Interceptor) clazz.newInstance();
               interceptor.setConfiguration(ielement);
               istack.add(interceptor);
            }
            catch(Exception e)
            {
               log.warn("Could not load the "+className+" interceptor for this container", e);
            }
         }
      }
      
      if( istack.size() == 0 )
         log.warn("There are no interceptors configured. Check the standardjboss.xml file");
      
      // Now add the interceptors to the container
      for(int i = 0; i < istack.size(); i ++)
      {
         Interceptor interceptor = (Interceptor) istack.get(i);
         container.addInterceptor(interceptor);
      }
      
      /* If there is a security proxy associated with the container add its
         interceptor just before the container interceptor
      */
      if( container.getSecurityProxy() != null )
         container.addInterceptor(new SecurityProxyInterceptor());
      
      // Finally we add the last interceptor from the container
      container.addInterceptor(container.createContainerInterceptor());
   }
   
   private static String stringTransactionValue(int transType)
   {
      String transaction = ANY_VALUE;
      switch( transType )
      {
      case BMT:
         transaction = BMT_VALUE;
         break;
      case CMT:
         transaction = CMT_VALUE;
         break;
      }
      return transaction;
   }
   
   /**
    * Create all proxy factories for this ejb
    */
   private static void createProxyFactories(BeanMetaData conf, Container container,
                                            ClassLoader cl )
      throws Exception
   {
      Iterator it = conf.getInvokerBindings();
      while (it.hasNext())
      {
         String invoker = (String)it.next();
         String jndiBinding = (String)conf.getInvokerBinding(invoker);
         log.debug("creating binding for " + jndiBinding + ":" + invoker);
         InvokerProxyBindingMetaData imd = (InvokerProxyBindingMetaData)conf.getApplicationMetaData().getInvokerProxyBindingMetaDataByName(invoker);
         EJBProxyFactory ci = null;

         // create a ProxyFactory instance
         try
         {
            ci = (EJBProxyFactory) cl.loadClass(imd.getProxyFactory()).newInstance();
            ci.setContainer(container);
            ci.setInvokerMetaData(imd);
            ci.setInvokerBinding(jndiBinding);
            if( ci instanceof XmlLoadable )
            {
               // the container invoker can load its configuration from the jboss.xml element
               ( (XmlLoadable) ci ).importXml(imd.getProxyFactoryConfig());
            }
            container.addProxyFactory(invoker, ci);
         }
         catch( Exception e )
         {
            throw new DeploymentException( "Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml): " + invoker, e );
         }
      }
   }
   
   
   private static BeanLockManager createBeanLockManager( Container container, boolean reentrant, Element config,
                                                         ClassLoader cl )
      throws Exception
   {
      // The bean lock manager
      BeanLockManager lockManager = new BeanLockManager(container);
      String beanLock = MetaData.getElementContent(config, "org.jboss.ejb.plugins.lock.QueuedPessimisticEJBLock");
      Class lockClass = null;
      try
      {
         lockClass =  cl.loadClass( beanLock);
      }
      catch( Exception e )
      {
         throw new DeploymentException( "Missing or invalid lock class (in jboss.xml or standardjboss.xml): " + beanLock+ " - " + e );
      }
      
      lockManager.setLockCLass(lockClass);
      lockManager.setReentrant(reentrant);
      lockManager.setConfiguration(config);
      
      return lockManager;
   }
   
   private static InstancePool createInstancePool( ConfigurationMetaData conf,
                                                   ClassLoader cl )
      throws Exception
   {
      // Set instance pool
      InstancePool ip = null;
      
      try
      {
         ip = (InstancePool) cl.loadClass( conf.getInstancePool() ).newInstance();
      }
      catch( Exception e )
      {
         throw new DeploymentException( "Missing or invalid Instance Pool (in jboss.xml or standardjboss.xml)", e);
      }
      
      if( ip instanceof XmlLoadable )
         ( (XmlLoadable) ip ).importXml( conf.getContainerPoolConf() );
      
      return ip;
   }
   
   private static InstanceCache createInstanceCache( ConfigurationMetaData conf,
                                                     boolean jmsMonitoring,
                                                     ClassLoader cl )
      throws Exception
   {
      // Set instance cache
      InstanceCache ic = null;
      
      try
      {
         ic = (InstanceCache) cl.loadClass( conf.getInstanceCache() ).newInstance();
         
         if( ic instanceof AbstractInstanceCache )
            ( (AbstractInstanceCache) ic ).setJMSMonitoringEnabled( jmsMonitoring );
      }
      catch( Exception e )
      {
         throw new DeploymentException( "Missing or invalid Instance Cache (in jboss.xml or standardjboss.xml)", e );
      }
      
      if( ic instanceof XmlLoadable )
         ( (XmlLoadable) ic ).importXml( conf.getContainerCacheConf() );
      
      return ic;
   }

   /**
    * Find a container from this deployment package, used to process ejb-link
    *
    * @param   name  ejb-name name defined in ejb-jar.xml in some jar in
    *          the same deployment package
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   private Container locateContainer(String name)
   {
      // Get the top level deployment
      DeploymentInfo info = deploymentInfo;
      while (info.parent != null)
         info = info.parent;

      // Start a recursive walk through the deployment tree
      return locateContainer(info, name);
   }

   /**
    * Find a container from this deployment package, used to process ejb-link<p>
    * 
    * Checks the passed deploymentinfo, then all its subdeployments
    *
    * @param   info  the current deploymentinfo
    * @param   name  ejb-name name defined in ejb-jar.xml in some jar in
    *          the same deployment package
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   private Container locateContainer(DeploymentInfo info, String name)
   {
      // Try the current EjbModule
      Container result = getContainerByDeploymentInfo(info, name);
      if (result != null)
      {
         return result;
      }

      // Try the subpackages
      for (Iterator iterator = info.subDeployments.iterator(); iterator.hasNext(); )
      {
         result = locateContainer((DeploymentInfo) iterator.next(), name);
         if (result != null)
         {
            return result;
         }
      }

      // Nothing found
      return null;
   }

   /**
    * Find a container from this deployment package, used to process ejb-link
    * that is a relative path<p>
    * 
    * Determines the path based on the url.
    *
    * @param   name  ejb-name name defined in ejb-jar.xml in some jar in
    *          the same deployment package
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   private Container locateContainerByPath(String name)
      throws DeploymentException
   {
      String path = name.substring(0, name.indexOf('#'));
      String ejbName = name.substring(name.indexOf('#') + 1);
      String us = deploymentInfo.url.toString();
      //remove our jar name
      String ourPath = us.substring(0, us.lastIndexOf('/'));
      for (StringTokenizer segments = new StringTokenizer(path, "/"); segments.hasMoreTokens(); )
      {
         String segment = segments.nextToken();
         //kind of silly, but takes care of ../s1/s2/../s3/myjar.jar
         if (segment.equals("..")) 
         {
            ourPath = ourPath.substring(0, ourPath.lastIndexOf('/'));
         } // end of if ()
         else
         {
            ourPath += "/" + segment;
         } // end of else
      }
      URL target = null;
      try 
      {
         target = new URL(ourPath);
      }
      catch (MalformedURLException mfue)
      {
         throw new DeploymentException("could not construct URL for: " + ourPath);
      } // end of try-catch
      DeploymentInfo targetInfo = null;
      try 
      {
         targetInfo = (DeploymentInfo)server.invoke(MainDeployerMBean.OBJECT_NAME,
                                                    "getDeployment",
                                                    new Object[] {target},
                                                    new String[] {URL.class.getName()});

      }
      catch (Exception e)
      {
         throw new DeploymentException("could not get DeploymentInfo for URL: " + target, e);
      } // end of try-catch
      if (targetInfo == null) 
      {
         throw new DeploymentException("cannot locate deployment info: " + target);
      } // end of if ()
      Container found = getContainerByDeploymentInfo(targetInfo, ejbName);
      if (found == null) 
      {
         throw new DeploymentException("cannot locate container: " + name + " in package at: " + target);
      } // end of if ()
      return found;
   }

   private Container getContainerByDeploymentInfo(DeploymentInfo info, String name)
   {
      EjbModule module = (EjbModule) ejbModulesByDeploymentInfo.get(info);
      if (module != null)
      {
         return module.getContainer(name);
      }
      return null;
   }

}
