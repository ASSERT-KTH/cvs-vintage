/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;



//import org.jboss.management.j2ee.EjbModule;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.ejb.EJBLocalHome;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.plugins.AbstractInstanceCache;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.EJB;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.metadata.XmlLoadable;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.system.Service;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.jmx.MBeanProxy;
import java.util.ArrayList;
import javax.naming.InitialContext;
import java.net.URLClassLoader;
import org.jboss.deployment.DeploymentException;
import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;

import org.w3c.dom.Element;

import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.jboss.util.jmx.ObjectNameFactory;

/**
 * An Application represents a collection of beans that are deployed as a
 * unit.
 *
 * <p>The beans may use the Application to access other beans within the same
 *    deployment unit.
 *      
 * @see Container
 * @see EJBDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.6 $
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
   
   /** Stores the containers for this application unit. */
   HashMap containers = new HashMap();
   HashMap localHomes = new HashMap();
   
   /** Class loader of this application. */
   ClassLoader classLoader = null;
   
   /** Name of this application, url it was deployed from */
   final String name;
   
   private final DeploymentInfo deploymentInfo;   

   /** Application Object Name (JSR-77) **/
   private String applicationName;
   /** Module Object Name (JSR-77) **/
   private String moduleName;

   private ServiceControllerMBean serviceController;

   //private MBeanServer server;
   
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   //constructor with mbeanserver

   public EjbModule(final DeploymentInfo di)
   {
      this.deploymentInfo = di;
      this.name = deploymentInfo.url.toString();
   }
   /**
    * Add a container to this application. This is called by the
    * EJBDeployer.
    *
    * @param   con  
    */
   public void addContainer(Container con)
   {
      containers.put(con.getBeanMetaData().getEjbName(), con);
      con.setEjbModule(this);
   }

   /**
    * Remove a container from this application.
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
    * Get a container from this Application that corresponds to a given name
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
    * Get all containers in this Application.
    *
    * @return  a collection of containers for each enterprise bean in this 
    *          application unit.
    * @jmx:managed-attribute
    */
   public Collection getContainers()
   {
      return containers.values();
   }

   /**
    * Get the class loader of this Application. 
    *
    * @return     
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Set the class loader of this Application
    *
    * @param   name  
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }

   /**
    * Get the name of this Application. 
    *
    * @return    The name of this application.
    * @jmx:managed-attribute
    */
   public String getName()
   {
      return name;
   }

   
   /**
    * Get the URL from which this Application was deployed
    *
    * @return    The URL from which this Application was deployed.
    */
   public URL getURL()
   {
      return deploymentInfo.url;
   }

   /**
   * @return Application Name if this is a standalone EJB module (JAR file)
   *         otherwise null
   **/
   public String getApplicationName() {
      return applicationName;
   }
   
   public void setApplicationName( String pApplicationName ) {
      applicationName = pApplicationName;
   }
	
   public String getModuleName() {
      return moduleName;
   }
   
   public void setModuleName( String pModuleName ) {
      moduleName = pModuleName;
   }
	
   // Service implementation ----------------------------------------
   public void createService() throws Exception 
   {
      serviceController = (ServiceControllerMBean)
	 MBeanProxy.create(ServiceControllerMBean.class,
			   ServiceControllerMBean.OBJECT_NAME,
			   server);
      boolean debug = log.isDebugEnabled();
      log.debug( "Application.start(), begin" );
  
      // Create JSR-77 EJB-Module
      int sepPos = getName().lastIndexOf( "/" );
      String lName = getName().substring(sepPos >= 0 ? sepPos + 1 : 0);
      // If Parent is not set then this is a standalone EJB module
      // therefore create the JSR-77 application beforehand
      if( deploymentInfo.parent == null ) {
         ObjectName lApplication = org.jboss.management.j2ee.J2EEApplication.create(
            server,
            lName,
            null
         );
         if( lApplication != null ) {
            setApplicationName( lApplication.toString() );
         }
      }
      ObjectName lModule = 
         org.jboss.management.j2ee.EjbModule.create(
            server,
            ( deploymentInfo.parent == null ? lName : deploymentInfo.parent.shortName ),
            lName,
            deploymentInfo.localUrl
         );
      if( lModule != null ) 
      {
         setModuleName( lModule.toString() );
      }
      //Set up the beans in this module.
      for (Iterator beans = ((ApplicationMetaData) deploymentInfo.metaData).getEnterpriseBeans(); beans.hasNext(); ) 
      {
         BeanMetaData bean = (BeanMetaData) beans.next();
         
         log.info( "Deploying " + bean.getEjbName() );
         try 
         {
            addContainer( createContainer( bean, deploymentInfo ) );
         } 
         catch (Exception e) 
         {
            log.error("error adding container to app.", e);
            throw e;
         } // end of try-catch
      }
      //only one iteration should be necessary!!!!!!!!!!!!!!!!!!   
      for (Iterator i = containers.values().iterator(); i.hasNext();)
      {
         Container con = (Container)i.next();
         ObjectName jmxName= con.getJmxName();
         server.registerMBean(con, jmxName);
         serviceController.create(jmxName);
         // Create JSR-77 EJB-Wrapper
         log.debug( "Application.create(), create JSR-77 EJB-Component" );
         ObjectName lEJB = EJB.create(
            server,
            getModuleName(),
            con.getBeanMetaData()
         );
         if (debug) {
            log.debug( "Application.start(), EJB: " + lEJB );
         }
         if( lEJB != null ) {
            con.mEJBObjectName = lEJB.toString();
         }
      }
   }

   /**
    * The mbean Service interface <code>start</code> method calls
    * the start method on each contatiner, then the init method on each 
    * container.  Conversion to a different registration system with one-phase 
    * startup is conceivable.
    *
    * @exception Exception if an error occurs
    */
   public void startService() throws Exception
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
   public void stopService()
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
            //no log here, but this shouldn't happen.
            //log.error("unexpected exception stopping Container: " + con.getJmxName(), e);
         } // end of try-catch
         
      }
   }

   public void destroyService()
   {
      for (Iterator i = containers.values().iterator(); i.hasNext();)
      {
         Container con = (Container)i.next();
         // Remove JSR-77 EJB-Wrapper
         if( con.mEJBObjectName != null ) {
            EJB.destroy( con.mbeanServer, con.mEJBObjectName );
         }
         try 
         {
            serviceController.destroy(con.getJmxName());
            serviceController.remove(con.getJmxName());
         }
         catch (Exception e)
         {
            //log.error("unexpected exception destroying Container: " + con.getJmxName(), e);
         } // end of try-catch
      }
      log.info( "Remove EJB Module: " + getModuleName() );
      if (getModuleName() != null) 
      {
         org.jboss.management.j2ee.EjbModule.destroy(server, getModuleName() );
      }
      log.info( "Remove Application: " + getApplicationName() );
      if( getApplicationName() != null )
      {
         org.jboss.management.j2ee.J2EEApplication.destroy( server, getApplicationName() );
      }
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
      container.setContainerInvoker( createContainerInvoker( conf, cl ) );
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
         container.setContainerInvoker( createContainerInvoker( conf, cl ) );
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
         container.setContainerInvoker( createContainerInvoker( conf, cl ) );
      }
      boolean beanCacheJMSMonitoring = ((Boolean)server.getAttribute(EJBDeployerMBean.OBJECT_NAME,
                                                           "BeanCacheJMSMonitoringEnabled")).booleanValue();
      container.setInstanceCache( createInstanceCache( conf, beanCacheJMSMonitoring, cl ) );
      // No real instance pool, use the shadow class
      container.setInstancePool( new StatefulSessionInstancePool() );
      // Set persistence manager
      container.setPersistenceManager( (StatefulSessionPersistenceManager) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
      //Set the bean Lock Manager
      container.setLockManager(createBeanLockManager(false,conf.getLockClass(), cl));
      
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
         container.setContainerInvoker( createContainerInvoker( conf, cl ) );
      }
      boolean beanCacheJMSMonitoring = ((Boolean)server.getAttribute(EJBDeployerMBean.OBJECT_NAME,
                                                           "BeanCacheJMSMonitoringEnabled")).booleanValue();
      container.setInstanceCache( createInstanceCache( conf, beanCacheJMSMonitoring, cl ) );
      container.setInstancePool( createInstancePool( conf, cl ) );
      //Set the bean Lock Manager
      container.setLockManager(createBeanLockManager(((EntityMetaData) bean).isReentrant(),conf.getLockClass(), cl));
      
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
      // Create classloader for this container
      // Only used to unique the bean ENC and does not augment class loading
      container.setClassLoader( new URLClassLoader( new URL[ 0 ], cl ) );
      // Create local classloader for this container
      // For loading resources that must come from the local jar.  Not for loading classes!
      container.setLocalClassLoader( new URLClassLoader( new URL[ 0 ], localCl ) );
      // Set metadata
      container.setBeanMetaData( bean );
      // Set transaction manager
      InitialContext iniCtx = new InitialContext();
      container.setTransactionManager( (TransactionManager) iniCtx.lookup( "java:/TransactionManager" ) );
      
      // Set security domain manager
      String securityDomain = bean.getApplicationMetaData().getSecurityDomain();
      String confSecurityDomain = conf.getSecurityDomain();
      /* These are deprecated.
      String securityManagerJNDIName = conf.getAuthenticationModule();
      String roleMappingManagerJNDIName = conf.getRoleMappingManager();
      */
      
      if( securityDomain != null || confSecurityDomain != null )
      {   // Either the application has a security domain or the container has security setup
         try
         {
            if( confSecurityDomain == null )
               confSecurityDomain = securityDomain;
            //System.out.println("lookup securityDomain manager name: "+confSecurityDomain);
            Object securityMgr = iniCtx.lookup(confSecurityDomain);
            AuthenticationManager ejbS = (AuthenticationManager) securityMgr;
            RealmMapping rM = (RealmMapping) securityMgr;
            container.setSecurityManager( ejbS );
            container.setRealmMapping( rM );
         }
         catch (NamingException ne)
         {
            throw new DeploymentException( "Could not find the Security Manager specified for this container, name="+confSecurityDomain, ne);
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
            //System.out.println("setSecurityProxy, "+proxy);
         }
         catch(Exception e)
         {
            throw new DeploymentException("Failed to create SecurityProxy of type: " +
                                          securityProxyClassName + ", "+ conf.getContainerInvoker(), e);
         }
      }
      
      // Install the container interceptors based on the configuration
      addInterceptors(container, transType, conf.getContainerInterceptorsConf());
   }
   
   /**
   * Given a container-interceptors element of a container-configuration,
   * add the indicated interceptors to the container depending on the container
   * transcation type and metricsEnabled flag.
   *
   * FIXME marcf: frankly the transaction type stuff makes no sense to me, we have externalized
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
   * createCOntainerInvoker DEPRACATED CONTAINER INVOKER DOES NOTHING BUT MANUFACTURE EJBs
   *
   * Move to EJBFactory, implement with ProxyFactory.   The EJBFactory must be made aware of invocation type
   *
   * FIXME : TEMPORARY 
   */
   private static ContainerInvoker createContainerInvoker( ConfigurationMetaData conf,
      ClassLoader cl )
   throws Exception
   {
      // Set container invoker
      ContainerInvoker ci = null;
      
      String invoker =conf.getContainerInvoker();
      
      // Just a nicety for 2.4 legacy users
      if (invoker.equals("org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker"))
         invoker = "org.jboss.proxy.ejb.ProxyFactory";
      
      try
      {
         
         ci = (ContainerInvoker) cl.loadClass(invoker).newInstance();
      }
      catch( Exception e )
      {
         throw new DeploymentException( "Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml): " + invoker, e );
      }
      
      if( ci instanceof XmlLoadable )
         // the container invoker can load its configuration from the jboss.xml element
      ( (XmlLoadable) ci ).importXml( conf.getContainerInvokerConf() );
      
      return ci;
   }
   
   
   private static BeanLockManager createBeanLockManager( boolean reentrant, String beanLock,
      ClassLoader cl )
   throws Exception
   {
      // The bean lock manager
      BeanLockManager lockManager = new BeanLockManager();
      
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
   
}
