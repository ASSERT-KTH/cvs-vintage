
/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
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

import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.logging.Logger;

/**
*   A ContainerFactory is used to deploy EJB applications. It can be given a URL to
*  an EJB-jar or EJB-JAR XML file, which will be used to instantiate containers and make
*  them available for invocation.
*
*   Now also works with message driven beans
*   @see Container
*   @author Rickard �berg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @author Peter Antman (peter.antman@tim.se)
*
*   @version $Revision: 1.65 $
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
  public static String DEFAULT_MESSAGEDRIVEN_CONFIGURATION = "Default MesageDriven Bean";
  // Attributes ----------------------------------------------------
  // Temp directory where deployed jars are stored
  File tmpDir;
  // The logger of this service
  Log log = Log.createLog( getName() );
  // A map of current deployments. If a deployment is made and it is already in this map,
  // then undeploy it first (i.e. make it a re-deploy).
  HashMap deployments = new HashMap();
  // Verify EJB-jar contents on deployments
  boolean verifyDeployments = false;
  boolean verifierVerbose = false;
  // Enable metrics interceptor
  boolean metricsEnabled = false;
  /* Enable JMS monitoring of the bean cache */
  private boolean m_beanCacheJMSMonitoring;

  // Public --------------------------------------------------------

  /**
   * Returns the deployed applications.
   */
  public java.util.Iterator getDeployedApplications()
    {
    return deployments.values().iterator();
    }

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
  public ObjectName getObjectName( MBeanServer server, ObjectName name )
    throws javax.management.MalformedObjectNameException
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
  *   Deploy the file at this URL. This method is typically called from remote administration
  *   tools that cannot handle java.net.URL's as parameters to methods
  *
  * @param   url
  * @exception   MalformedURLException
  * @exception   DeploymentException
  */
  public void deploy( String url )
    throws MalformedURLException, DeploymentException
    {
    // Delegate to "real" deployment
    deploy( new URL( url ) );
    }
//
// Richard Gyger
//
  public void deploy( String appUrl, String[] jarUrls )
    throws MalformedURLException, DeploymentException
    {
    // Delegate to "real" deployment
    URL[] tmp = new URL[ jarUrls.length ];

    for( int i = 0; i < tmp.length; i++ )
      tmp[ i ] = new URL( jarUrls[ i ] );

    deploy( new URL( appUrl ), tmp );
    }

  /**
  *   Undeploy the file at this URL. This method is typically called from remote administration
  *   tools that cannot handle java.net.URL's as parameters to methods
  *
  * @param   url
  * @exception   MalformedURLException
  * @exception   DeploymentException
  */
  public void undeploy( String url )
    throws MalformedURLException, DeploymentException
    {
    // Delegate to "real" undeployment
    undeploy( new URL( url ) );
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
  public synchronized void deploy( URL url )
    throws DeploymentException
    {
    deploy( url, new URL[]{ url } );
    }
//
// Richard Gyger
//
  public synchronized void deploy( URL appUrl, URL[] jarUrls )
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

      // create the _real_ classloader for this app
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      app.setClassLoader( cl );

      for( int i = 0; i < jarUrls.length; i++ )
       deploy( app, jarUrls[ i ], cl );

      // Init application
      app.init();
      // Start application
      app.start();

      // Add to webserver so client can access classes through dynamic class downloading
      WebServiceMBean webServer = (WebServiceMBean) MBeanProxy.create( WebServiceMBean.class, WebServiceMBean.OBJECT_NAME );

      webServer.addClassLoader( cl );
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
    }

  private void deploy( Application app, URL url, ClassLoader cl )
    throws NamingException, Exception
    {
      // Create a file loader with which to load the files
      XmlFileLoader efm = new XmlFileLoader();

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
  *   Remove previously deployed EJBs.
  *
  * @param   url
  * @exception   DeploymentException
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
  *   is the aplication with this url deployed
  *
  * @param   url
  * @exception   MalformedURLException
  */
  public boolean isDeployed( String url )
    throws MalformedURLException
    {
    return isDeployed( new URL( url ) );
    }

  /**
  *   check if the application with this url is deployed
  *
  * @param   url
  * @return true if deployed
  */
  public boolean isDeployed( URL url )
    {
    return ( deployments.get( url ) != null );
    }


  // ******************
  // Container Creation
  // ******************

  private Container createContainer( BeanMetaData bean, ClassLoader cl, ClassLoader localCl )
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

  private MessageDrivenContainer createMessageDrivenContainer( BeanMetaData bean, ClassLoader cl, ClassLoader localCl )
    throws Exception
    {
    // get the container configuration for this bean
    // a default configuration is now always provided
    ConfigurationMetaData conf = bean.getContainerConfiguration();
    // Stolen from Stateless deploy
    // Create container
    MessageDrivenContainer container = new MessageDrivenContainer();

    initializeContainer( container, conf, bean, cl, localCl );
    container.setContainerInvoker( createContainerInvoker( conf, cl ) );
    container.setInstancePool( createInstancePool( conf, cl ) );
    // Create interceptors
    container.addInterceptor( new LogInterceptor() );
    container.addInterceptor( new SecurityInterceptor() );

    if( ( (MessageDrivenMetaData) bean ).isContainerManagedTx() )
      {
      // CMT
      container.addInterceptor( new TxInterceptorCMT() );

      if( metricsEnabled )
        container.addInterceptor( new MetricsInterceptor() );

      container.addInterceptor( new MessageDrivenInstanceInterceptor() );
      }
    else
      {
      // BMT
      container.addInterceptor( new MessageDrivenInstanceInterceptor() );
      // FIXME. should we have a special BMT tx interceptor
      // to place ACK there???
      container.addInterceptor( new MessageDrivenTxInterceptorBMT() );

      if( metricsEnabled )
        container.addInterceptor( new MetricsInterceptor() );
      }

    // Finally we add the last interceptor from the container
    container.addInterceptor( container.createContainerInterceptor() );

    return container;
    }

  private StatelessSessionContainer createStatelessSessionContainer( BeanMetaData bean, ClassLoader cl, ClassLoader localCl )
    throws Exception
    {
    // get the container configuration for this bean
    // a default configuration is now always provided
    ConfigurationMetaData conf = bean.getContainerConfiguration();
    // Create container
    StatelessSessionContainer container = new StatelessSessionContainer();

    initializeContainer( container, conf, bean, cl, localCl );
    container.setContainerInvoker( createContainerInvoker( conf, cl ) );
    container.setInstancePool( createInstancePool( conf, cl ) );
    // Create interceptors
    container.addInterceptor( new LogInterceptor() );
    container.addInterceptor( new SecurityInterceptor() );

    if( ( (SessionMetaData) bean ).isContainerManagedTx() )
      {
      // CMT
      container.addInterceptor( new TxInterceptorCMT() );

      if( metricsEnabled )
        container.addInterceptor( new MetricsInterceptor() );

      container.addInterceptor( new StatelessSessionInstanceInterceptor() );
      }
    else
      {
      // BMT
      container.addInterceptor( new StatelessSessionInstanceInterceptor() );
      container.addInterceptor( new TxInterceptorBMT() );

      if( metricsEnabled )
        container.addInterceptor( new MetricsInterceptor() );
      }

    // Finally we add the last interceptor from the container
    container.addInterceptor( container.createContainerInterceptor() );

    return container;
    }

  private StatefulSessionContainer createStatefulSessionContainer( BeanMetaData bean, ClassLoader cl, ClassLoader localCl )
    throws Exception
    {
    // get the container configuration for this bean
    // a default configuration is now always provided
    ConfigurationMetaData conf = bean.getContainerConfiguration();
    // Create container
    StatefulSessionContainer container = new StatefulSessionContainer();

    initializeContainer( container, conf, bean, cl, localCl );
    container.setContainerInvoker( createContainerInvoker( conf, cl ) );
    container.setInstanceCache( createInstanceCache( conf, m_beanCacheJMSMonitoring, cl ) );
    // No real instance pool, use the shadow class
    container.setInstancePool( new StatefulSessionInstancePool() );
    // Set persistence manager
    container.setPersistenceManager( (StatefulSessionPersistenceManager) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
    // Create interceptors
    container.addInterceptor( new LogInterceptor() );

    if( ( (SessionMetaData) bean ).isContainerManagedTx() )
      {
      // CMT
      container.addInterceptor( new TxInterceptorCMT() );

      if( metricsEnabled )
        container.addInterceptor( new MetricsInterceptor() );

      container.addInterceptor( new StatefulSessionInstanceInterceptor() );
      }
    else
      {
      // BMT : the tx interceptor needs the context from the instance interceptor
      container.addInterceptor( new StatefulSessionInstanceInterceptor() );
      container.addInterceptor( new TxInterceptorBMT() );

      if( metricsEnabled )
        container.addInterceptor( new MetricsInterceptor() );
      }

    container.addInterceptor( new SecurityInterceptor() );
    container.addInterceptor( container.createContainerInterceptor() );

    return container;
    }

  private EntityContainer createEntityContainer( BeanMetaData bean, ClassLoader cl, ClassLoader localCl )
    throws Exception
    {
    // get the container configuration for this bean
    // a default configuration is now always provided
    ConfigurationMetaData conf = bean.getContainerConfiguration();
    // Create container
    EntityContainer container = new EntityContainer();

    initializeContainer( container, conf, bean, cl, localCl );
    container.setContainerInvoker( createContainerInvoker( conf, cl ) );
    container.setInstanceCache( createInstanceCache( conf, m_beanCacheJMSMonitoring, cl ) );
    container.setInstancePool( createInstancePool( conf, cl ) );

    // Set persistence manager
    if( ( (EntityMetaData) bean ).isBMP() )
      {
      //Should be BMPPersistenceManager
      container.setPersistenceManager( (EntityPersistenceManager) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
      }
    else
      {
      // CMP takes a manager and a store
      org.jboss.ejb.plugins.CMPPersistenceManager persistenceManager = new org.jboss.ejb.plugins.CMPPersistenceManager();

      //Load the store from configuration
      persistenceManager.setPersistenceStore( (EntityPersistenceStore) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
      // Set the manager on the container
      container.setPersistenceManager( persistenceManager );
      }

    // Create interceptors
    container.addInterceptor( new LogInterceptor() );
    container.addInterceptor( new SecurityInterceptor() );
    // entity beans are always CMT
    container.addInterceptor( new TxInterceptorCMT() );

    if( metricsEnabled )
      container.addInterceptor( new MetricsInterceptor() );

    container.addInterceptor( new EntityInstanceInterceptor() );
    container.addInterceptor( new EntitySynchronizationInterceptor() );
    container.addInterceptor( container.createContainerInterceptor() );

    return container;
    }

  // **************
  // Helper Methods
  // **************

  private static void initializeContainer( Container container, ConfigurationMetaData conf, BeanMetaData bean, ClassLoader cl, ClassLoader localCl )
    throws NamingException, DeploymentException
    {
    // Create classloader for this container
    // Only used to identify bean. Not really used for class loading!
    container.setClassLoader( new URLClassLoader( new URL[ 0 ], cl ) );
    // Create local classloader for this container
    // For loading resources that must come from the local jar.  Not for loading classes!
    container.setLocalClassLoader( new URLClassLoader( new URL[ 0 ], localCl ) );
    // Set metadata
    container.setBeanMetaData( bean );
    // Set transaction manager
    container.setTransactionManager( (TransactionManager) new InitialContext().lookup( "java:/TransactionManager" ) );

    // Set security manager & role mapping manager
    String securityManagerJNDIName = conf.getAuthenticationModule();
    String roleMappingManagerJNDIName = conf.getRoleMappingManager();

    if( ( securityManagerJNDIName != null ) && ( roleMappingManagerJNDIName != null ) )
      {
      try
        {
        EJBSecurityManager ejbS = (EJBSecurityManager) new InitialContext().lookup( securityManagerJNDIName );

        container.setSecurityManager( ejbS );
        }
      catch( NamingException ne )
        {
        throw new DeploymentException( "Could not find the Security Manager specified for this container", ne );
        }

      try
        {
        RealmMapping rM = (RealmMapping) new InitialContext().lookup( roleMappingManagerJNDIName );

        container.setRealmMapping( rM );
        }
      catch( NamingException ne )
        {
        throw new DeploymentException( "Could not find the Role Mapping Manager specified for this container", ne );
        }
      }
    }

  private static ContainerInvoker createContainerInvoker( ConfigurationMetaData conf, ClassLoader cl )
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

  private static InstancePool createInstancePool( ConfigurationMetaData conf, ClassLoader cl )
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

  private static InstanceCache createInstanceCache( ConfigurationMetaData conf, boolean jmsMonitoring, ClassLoader cl )
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


/*------ Formatted by Jindent 3.23 Basic 1.0 --- http://www.jindent.de ------*/
