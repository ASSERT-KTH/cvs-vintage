/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.log4j.NDC;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeployerMBean;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.plugins.AbstractInstanceCache;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;
import org.jboss.management.j2ee.J2EEDeployedObject;
import org.jboss.logging.Logger;
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
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.MBeanProxy;
import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;
import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;
import org.w3c.dom.Element;

import org.jboss.management.j2ee.EjbModule;

/**
* A EJBDeployer is used to deploy EJB applications. It can be given a
* URL to an EJB-jar or EJB-JAR XML file, which will be used to instantiate
* containers and make them available for invocation.
*
* <p>Now also works with message driven beans.
*
* @see Container
*
* @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
* @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
* @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
* @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
* @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
* @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
* @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
* @version $Revision: 1.7 $ 
*/
public class EJBDeployer
extends ServiceMBeanSupport
implements EJBDeployerMBean
{
   // Constants -----------------------------------------------------
   private static final String SERVICE_CONTROLLER_NAME = "jboss.system:spine=ServiceController";
   
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
   
   /**
   * A map of current deployments. 
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
      return OBJECT_NAME;
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
   
   public void startService()
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
   }
   /**
   * Implements the template method in superclass. This method stops all the
   * applications in this server.
   */
   public void stopService()
   {
      for (Iterator apps = deployments.values().iterator(); apps.hasNext(); )
      {
         Application app = (Application) apps.next();
         
         app.stop();
      }
      deployments.clear();
    
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
   
   /**
   * Implements the template method in superclass. This method destroys all
   * the applications in this server and clears the deployments list.
   */
   /*
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
   
   
   */
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
   
   public boolean accepts(DeploymentInfo sdi) 
   {
      
      // To be accepted the deployment's root name must end in jar
      if (!sdi.url.getFile().endsWith("jar")) return false;
         
      // However the jar must also contain at least one ejb-jar.xml
      
      try 
      {
         URL dd = sdi.localCl.getResource("META-INF/ejb-jar.xml");
         
         if (dd == null) return false;
            
         else return true;
      
      }
      catch (Exception e) { return false;}
   }
   
   
   
   public void init(DeploymentInfo sdi) 
   throws DeploymentException
   {
      try {
         //Resolve what to watch      
         if (sdi.url.getProtocol().startsWith("http"))
         {
            // We watch the top only, no directory support
            sdi.watch = sdi.url;
         
         }
         else if(sdi.url.getProtocol().startsWith("file"))
         {
            
            File file = new File (sdi.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) sdi.watch = sdi.url;
               
            // If directory we watch the xml files
            else sdi.watch = new URL(sdi.url, "META-INF/ejb-jar.xml");
         }    
      }
      catch (Exception e) {throw new DeploymentException("problem in init"+e.getMessage());}
   }
   
   
   
   public synchronized void deploy(DeploymentInfo sdi)
      throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();
      
      try 
      {
         // Create a file loader with which to load the files
         XmlFileLoader efm = new XmlFileLoader(validateDTDs);
         
         efm.setClassLoader( sdi.localCl );
         
         // Load XML
         sdi.metaData =  efm.load();
      }
      catch (Exception e) {
         log.error("Problem loading metaData ", e);
      }
      
      // Check validity
      NDC.push("Verifier" );
      // wrapping this into a try - catch block to prevent errors in
      // verifier from stopping the deployment
      try
      {
         if( verifyDeployments )
         {
            BeanVerifier verifier = new BeanVerifier();
            verifier.addVerificationListener( new DeployListener ());
            if (debug) {
               log.debug("Verifying " + sdi.url);
            }
            
            verifier.verify( sdi.url, (ApplicationMetaData) sdi.metaData, sdi.ucl );
         }
      }
      catch( Throwable t )
      {
         log.error("Verfiy failed", t );
      }
      
      // unset verifier log
      NDC.pop();
      
      // Create application
      Application app = new Application();
      
      try
      {
         
         app.setURL( sdi.url );
         
         if (debug) {
            log.debug( "Deploying: " + sdi.url );
         }
         
         
         for (Iterator beans = ((ApplicationMetaData) sdi.metaData).getEnterpriseBeans(); beans.hasNext(); ) 
         {
            BeanMetaData bean = (BeanMetaData) beans.next();
            
            log.info( "Deploying " + bean.getEjbName() );
            try 
            {
               app.addContainer( createContainer( bean, sdi ) );
            } 
            catch (Exception e) 
            {
               log.error("error adding container to app.", e);
               throw e;
            } // end of try-catch
         }
         
         // Init application
         //app.create();
         // Start application
         log.info( "start application, deploymentInfo: " + sdi +
            ", short name: " + sdi.shortName +
            ", parent short name: " + ( sdi.parent == null ? "null" : sdi.parent.shortName )
         );
         // Create JSR-77 EJB-Module
         int i = app.getName().lastIndexOf( "/" );
         String lName = app.getName().substring(
            i >= 0 ? i + 1 : 0
         );
         if( sdi.parent != null ) {
            ObjectName lModule = EjbModule.create(
               getServer(),
               sdi.parent.shortName,
               lName,
               app.url
            );
            if( lModule != null ) {
               app.setModuleName( lModule.toString() );
            }
         }
         app.start();
         
         // Done
         if (debug) {
            log.debug( "Deployed: " + app.getName() );
         }
         
         // Register deployment. Use the application name in the hashtable
         deployments.put( sdi.url, app );
      }
      catch( Exception e )
      {
         log.error("Could not deploy " + sdi.url, e);
         app.stop();
         //app.destroy();
         
         throw new DeploymentException( "Could not deploy " + sdi.url, e );
      }
   }
   
   /**
   * Remove previously deployed EJBs.
   *
   * @param url
   *
   * @throws DeploymentException
   */
   public void undeploy(DeploymentInfo di)
      throws DeploymentException
   {
      undeploy(di.url);  
   }
   
   public void undeploy(URL url )
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
      log.info( "Undeploying:" + url );
      app.stop();
      //app.destroy();
      
      
      /*
      try {
      if ( app.getClassLoader() != null ) {
      // Remove from webserver
      WebServiceMBean webServer = (WebServiceMBean) MBeanProxy.create( WebServiceMBean.class, WebServiceMBean.OBJECT_NAME );
      webServer.removeClassLoader( app.getClassLoader() );
      }
      } catch( Exception e ) {
      throw new DeploymentException( "Error during undeploy of " + app.getURL().toString(), e );
      } finally {
      // Remove JSR-77 Module
      if( app.getModuleName() != null ) {
      EjbModule.destroy( getServer(), app.getModuleName() );
      }
      // Done
      if (log.isInfoEnabled())
         log.info( "Undeployed application: " + app.getName() );
      }
      
      */
      
      // Remove deployment
      deployments.remove( url );
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
      container.setMBeanServer(this.getServer());
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
   
   /**
    * A callback listener for the EJB verifier.
    */
   class DeployListener implements VerificationListener
   {
      /* Accessing the EJBDeployer.log directory is
       * causing a NoSuchMethodError when the log is used
       * so obtain it via the getLog() method and then use
       * logger
       */
      final Logger logger = EJBDeployer.this.getLog();
      
      public void beanChecked( VerificationEvent event )
      {
         logger.debug( event.getMessage() );
      }
      
      public void specViolation( VerificationEvent event )
      {
         if( verifierVerbose )
            logger.info( event.getVerbose() );
         else
            logger.info( event.getMessage() );
      }
   }
}

