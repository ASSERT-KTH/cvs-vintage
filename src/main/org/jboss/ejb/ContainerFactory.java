/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.MalformedObjectNameException;
import javax.transaction.TransactionManager;

import org.w3c.dom.Element;

import org.jboss.ejb.plugins.AbstractInstanceCache;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;
import org.jboss.ejb.BeanLockManager;
import org.jboss.logging.Log;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;
import org.jboss.util.MBeanProxy;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;
import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;

/**
 * A ContainerFactory is used to deploy EJB applications. It can be given a
 * URL to an EJB-jar or EJB-JAR XML file, which will be used to instantiate
 * containers and make them available for invocation.
*
* <p>Now also works with message driven beans.
*
* @see Container
*
* @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
* @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
* @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
* @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
* @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
* @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
* @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
* @version $Revision: 1.89 $
*/
public class ContainerFactory
   extends ServiceMBeanSupport
   implements ContainerFactoryMBean
{
   // Constants -----------------------------------------------------

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

   // Attributes ----------------------------------------------------

   /** The logger of this service */
   Log log = Log.createLog(this.getClass().getName());

   /**
    * A map of current deployments. If a deployment is made and it is
    * already in this map, then undeploy it first (i.e. make it a re-deploy).
    */
   HashMap deployments = new HashMap();

   /** Verify EJB-jar contents on deployments */
   boolean verifyDeployments = false;

   /** Enable verbose verification. */
   boolean verifierVerbose = false;

   /** Enable metrics interceptor */
   boolean metricsEnabled = false;

   /** Enable JMS monitoring of the bean cache */
   private boolean m_beanCacheJMSMonitoring;

   /** A flag indicating if deployment descriptors should be validated */
   private boolean validateDTDs;

   // Public --------------------------------------------------------

   /**
    * Returns the deployed applications.
    */
   public Iterator getDeployedApplications()
   {
      return deployments.values().iterator();
   }

   /**
    * Implements the abstract <code>getObjectName()</code> method in superclass
    * to return this service's name.
    *
    * @param server
    * @param name
    * @return
    *
    * @throws MalformedObjectNameException
    */
   public ObjectName getObjectName( MBeanServer server, ObjectName name )
      throws MalformedObjectNameException
   {
      return new ObjectName( OBJECT_NAME );
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
    * Implements the template method in superclass. This method stops all the
    * applications in this server.
    */
   public void stopService()
   {
      Iterator apps = deployments.values().iterator();

      while( apps.hasNext() )
      {
         Application app = (Application) apps.next();

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

      while( apps.hasNext() )
      {
         Application app = (Application) apps.next();

         app.destroy();
      }

      deployments.clear();
   }

   /**
    * Enables/disables the application bean verification upon deployment.
    *
    * @param   verify  true to enable; false to disable
    */
   public void setVerifyDeployments( boolean verify )
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
   public void setVerifierVerbose( boolean verbose )
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
    * Enables/disables the metrics interceptor for containers.
    *
    * @param enable  true to enable; false to disable
    */
   public void setMetricsEnabled( boolean enable )
   {
      metricsEnabled = enable;
   }

   /**
    * Checks if this container factory initializes the metrics interceptor.
    *
    * @return   true if metrics are enabled; false otherwise
    */
   public boolean isMetricsEnabled()
   {
      return metricsEnabled;
   }

   /**
    * Set JMS monitoring of the bean cache.
    */
   public void setBeanCacheJMSMonitoringEnabled( boolean enable )
   {
      m_beanCacheJMSMonitoring = enable;
   }

   /**
    * Get the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public boolean getValidateDTDs()
   {
      return validateDTDs;
   }

   /**
    * Set the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public void setValidateDTDs(boolean validate)
   {
      this.validateDTDs = validate;
   }

   /**
    * Deploy the file at this URL. This method is typically called from
    * remote administration tools that cannot handle java.net.URL's as
    * parameters to methods
    *
    * @param url
    *
    * @throws MalformedURLException
    * @throws DeploymentException
    */
   public void deploy( String url, String appId )
      throws MalformedURLException, DeploymentException
   {
      // Delegate to "real" deployment
      deploy( new URL( url ), appId );
   }

   //
   // Richard Gyger
   //
   public void deploy( String appUrl, String[] jarUrls, String appId )
      throws MalformedURLException, DeploymentException
   {
      // Delegate to "real" deployment
      URL[] tmp = new URL[ jarUrls.length ];

      for( int i = 0; i < tmp.length; i++ )
         tmp[ i ] = new URL( jarUrls[ i ] );

      deploy( new URL( appUrl ), tmp, appId );
   }

   /**
    * Undeploy the file at this URL. This method is typically called from
    * remote administration tools that cannot handle java.net.URL's as
    * parameters to methods
    *
    * @param url
    *
    * @throws MalformedURLException
    * @throws DeploymentException
    */
   public void undeploy( String url )
      throws MalformedURLException, DeploymentException
   {
      // Delegate to "real" undeployment
      undeploy( new URL( url ) );
   }

   /**
    * Deploy EJBs pointed to by an URL.
    * The URL may point to an EJB-JAR, an EAR-JAR, or an codebase
    * whose structure resembles that of an EJB-JAR. <p>
    *
    * The latter is useful for development since no packaging is required.
    *
    * @param url    URL where EJB deployment information is contained
    *
    * @throws DeploymentException
    */
   public synchronized void deploy( URL url, String appId )
      throws DeploymentException
   {
      deploy( url, new URL[]{ url }, appId );
   }

   //
   // Richard Gyger
   //
   public synchronized void deploy( URL appUrl, URL[] jarUrls, String appId )
      throws DeploymentException
   {
      // Create application
      Application app = new Application();

      try
      {
         Log.setLog( log );

         // Check if already deployed -> undeploy first, this is re-deploy
         if( deployments.containsKey( appUrl ) )
            undeploy( appUrl );

         app.setURL( appUrl );
         log.log( "Deploying:" + appUrl );

         /* Create a subclass of URLClassLoader that allows for dynamic class
            loading via the WebServiceMBean
         */
         WebClassLoader cl = new WebClassLoader( jarUrls, Thread.currentThread().getContextClassLoader() );
         app.setClassLoader( cl );

         // Add to webserver so client can access classes through dynamic class downloading
         // This needs to be done before we deploy our app, as the RMI subsystem will use, as a codebase, the URL we
         // set here to our WebClassLoader (when creating home proxies for example)
         WebServiceMBean webServer = (WebServiceMBean) MBeanProxy.create( WebServiceMBean.class, WebServiceMBean.OBJECT_NAME );
         URL[] codebase = { webServer.addClassLoader(cl) };
         cl.setWebURLs(codebase);

         for( int i = 0; i < jarUrls.length; i++ )
            deploy( app, jarUrls[ i ], cl );

         // Init application
         app.init();
         // Start application
         app.start();
         // Startup the Management MBean Wrapper for the containers
         Iterator i = app.containers.values().iterator();
         while( i.hasNext() ) {
            handleContainerManagement( (Container) i.next(), true );
         }

         // Done
         log.log( "Deployed application: " + app.getName() );
         // Register deployment. Use the application name in the hashtable
         deployments.put( appUrl, app );
      }
      catch( Exception e )
      {
         if( e instanceof NullPointerException )
         {
            // Avoids useless 'null' messages on a server trace.
            // Let's be honest and spam them with a stack trace.
            // NPE should be considered an internal server error anyways.
            Logger.exception( e );
         }

         Logger.exception( e );
         //Logger.debug(e.getMessage());
         app.stop();
         app.destroy();

         throw new DeploymentException( "Could not deploy " + appUrl.toString(), e );
      }
      finally
      {
         Log.unsetLog();
      }
      // Inform the Data Collector that new/old EJBs were deployed
/* AS Temporary not available
      try
      {
         getServer().invoke(new ObjectName( "Management", "service", "Collector" ),
                            "refresh",
                            new Object[] {},
                            new String[] {}
                            );
      }
      catch( Exception e )
      {
         Logger.exception(e);
      }
*/
   }

   private void deploy( Application app, URL url, ClassLoader cl )
      throws NamingException, Exception
   {
      // Create a file loader with which to load the files
      XmlFileLoader efm = new XmlFileLoader(validateDTDs);

      // the file manager gets its file from the classloader
      // create a classloader that to access the metadata
      // this one dont has the contextclassloader as parent
      // in case of the contextclassloader has a ejb package in its
      // classpath the metadata of this package would be used.
      ClassLoader localCl = new URLClassLoader( new URL[]{ url } );
      efm.setClassLoader( localCl );

      // Load XML
      ApplicationMetaData metaData = efm.load();

      // Check validity
      Log.setLog( Log.createLog( "Verifier" ) );

      // wrapping this into a try - catch block to prevent errors in
      // verifier from stopping the deployment
      try
      {
         if( verifyDeployments )
         {
            BeanVerifier verifier = new BeanVerifier();

            verifier.addVerificationListener( new VerificationListener()
               {
                  public void beanChecked( VerificationEvent event )
                  {
                     Logger.debug( event.getMessage() );
                  }

                  public void specViolation( VerificationEvent event )
                  {
                     if( verifierVerbose )
                        Logger.log( event.getVerbose() );
                     else
                        Logger.log( event.getMessage() );
                  }
               } );
            Logger.log( "Verifying " + url );
            verifier.verify( url, metaData, cl );
         }
      }
      catch( Throwable t )
      {
         Logger.exception( t );
      }

      // unset verifier log
      Log.unsetLog();

      // Get list of beans for which we will create containers
      Iterator beans = metaData.getEnterpriseBeans();
      // Deploy beans
      Context ctx = new InitialContext();

      while( beans.hasNext() )
      {
         BeanMetaData bean = (BeanMetaData) beans.next();

         log.log( "Deploying " + bean.getEjbName() );
         app.addContainer( createContainer( bean, cl, localCl ) );
      }
   }

   /**
    * Remove previously deployed EJBs.
    *
    * @param url
    *
    * @throws DeploymentException
    */
   public void undeploy( URL url )
      throws DeploymentException
   {
      // Get application from table
      Application app = (Application) deployments.get( url );

      // Check if deployed
      if( app == null )
      {
         throw new DeploymentException( "URL not deployed" );
      }

      // Undeploy application
      Log.setLog( log );
      log.log( "Undeploying:" + url );
      // Shutdown the Management MBean Wrapper for the containers
      Iterator i = app.containers.values().iterator();
      while( i.hasNext() ) {
         handleContainerManagement( (Container) i.next(), false );
      }
      app.stop();
      app.destroy();
      try {
         if ( app.getClassLoader() != null ) {
            // Remove from webserver
            WebServiceMBean webServer = (WebServiceMBean) MBeanProxy.create( WebServiceMBean.class, WebServiceMBean.OBJECT_NAME );
            webServer.removeClassLoader( app.getClassLoader() );
         }
      } catch( Exception e ) {
         throw new DeploymentException( "Error during undeploy of " + app.getURL().toString(), e );
      } finally {
         // Remove deployment
         deployments.remove( url );
         // Done
         log.log( "Undeployed application: " + app.getName() );
         Log.unsetLog();
      }
   }

   /**
    * Is the aplication with this url deployed.
    *
    * @param url
    *
    * @throws MalformedURLException
    */
   public boolean isDeployed( String url )
      throws MalformedURLException
   {
      return isDeployed( new URL( url ) );
   }

   /**
    * Check if the application with this url is deployed.
    *
    * @param url
    * @return       true if deployed
    */
   public boolean isDeployed( URL url )
   {
      return ( deployments.get( url ) != null );
   }

   // ******************
   // Container Creation
   // ******************

   private Container createContainer( BeanMetaData bean,
                                      ClassLoader cl,
                                      ClassLoader localCl )
      throws Exception
   {
      // Added message driven deployment
      if( bean.isMessageDriven() )
      {
         return createMessageDrivenContainer( bean, cl, localCl );
      }
      else if( bean.isSession() )   // Is session?
      {
         if( ( (SessionMetaData) bean ).isStateless() )   // Is stateless?
         {
            return createStatelessSessionContainer( bean, cl, localCl );
         }
         else   // Stateful
         {
            return createStatefulSessionContainer( bean, cl, localCl );
         }
      }
      else   // Entity
      {
         return createEntityContainer( bean, cl, localCl );
      }
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
         container.setContainerInvoker( createContainerInvoker( conf, cl ) );
      container.setInstanceCache( createInstanceCache( conf, m_beanCacheJMSMonitoring, cl ) );
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
         container.setContainerInvoker( createContainerInvoker( conf, cl ) );
      container.setInstanceCache( createInstanceCache( conf, m_beanCacheJMSMonitoring, cl ) );
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
    * Either creates the Management MBean wrapper for a container and start
    * it or it destroy it.
    **/
   private void handleContainerManagement( Container container, boolean doStart ) {
      try {
         // Create and register the ContainerMBean
         ObjectName name = new ObjectName( "Management", "jndiName", container.getBeanMetaData().getJndiName() );
         if( doStart ) {
            getServer().createMBean(
                                    "org.jboss.management.ContainerManagement",
                                    name,
                                    new Object[] { container },
                                    new String[] { "org.jboss.ejb.Container" }
                                    );
            getServer().invoke( name, "init", new Object[] {}, new String[] {} );
            getServer().invoke( name, "start", new Object[] {}, new String[] {} );
         }
         else {
            // If not startup then assume that the MBean is there
            getServer().invoke( name, "stop", new Object[] {}, new String[] {} );
            getServer().invoke( name, "destroy", new Object[] {}, new String[] {} );
            // Unregister the MBean to avoid future name conflicts
            getServer().unregisterMBean( name );
         }
      }
      catch( Exception e )
      {
         if( e instanceof RuntimeMBeanException )
         {
            RuntimeMBeanException rme = (RuntimeMBeanException) e;
            Logger.exception(rme.getTargetException());
         }
         else
         {
            Logger.exception(e);
         }
      }
   }

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
            EJBSecurityManager ejbS = (EJBSecurityManager) securityMgr;
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
            throw new DeploymentException("Failed to create SecurityProxy of type: " + securityProxyClassName + ", "+ conf.getContainerInvoker() +" - " + e);
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
    * @param container   the container instance to setup.
    * @param transType   one of the BMT, CMT or ANY constants.
    * @param element     the container-interceptors element from the
    *                    container-configuration.
    */
   private void addInterceptors(Container container,
                                int transType,
                                Element element)
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
         */
         String transAttr = ielement.getAttribute("transaction");
         if( transAttr == null || transAttr.length() == 0 )
            transAttr = ANY_VALUE;
         if( transAttr.equalsIgnoreCase(ANY_VALUE) || transAttr.equalsIgnoreCase(transTypeString) )
         {   // The transaction type matches the container bean trans type, check the metricsEnabled
            String metricsAttr = ielement.getAttribute("metricsEnabled");
            boolean metricsInterceptor = metricsAttr.equalsIgnoreCase("true");
            if( metricsEnabled == false && metricsInterceptor == true )
               continue;

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
               Logger.warning("Could not load the "+className+" interceptor for this container");
               Logger.exception(e);
            }
         }
      }

      if( istack.size() == 0 )
         Logger.warning("There are no interceptors configured. Check the standardjboss.xml file");

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

   private static ContainerInvoker createContainerInvoker( ConfigurationMetaData conf,
                                                           ClassLoader cl )
      throws Exception
   {
      // Set container invoker
      ContainerInvoker ci = null;

      try
      {
         ci = (ContainerInvoker) cl.loadClass( conf.getContainerInvoker() ).newInstance();
      }
      catch( Exception e )
      {
         throw new DeploymentException( "Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml): " + conf.getContainerInvoker() + " - " + e );
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
         throw new DeploymentException( "Missing or invalid Instance Pool (in jboss.xml or standardjboss.xml)" );
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
         throw new DeploymentException( "Missing or invalid Instance Cache (in jboss.xml or standardjboss.xml)" );
      }

      if( ic instanceof XmlLoadable )
         ( (XmlLoadable) ic ).importXml( conf.getContainerCacheConf() );

      return ic;
   }
}

/* Change log:
 *
 * o Thu Jun 14 23:28:58  2001 UTC, starksm
 *   Added support for validation of the j2ee related deployment descriptors.
 */
