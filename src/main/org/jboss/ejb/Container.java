/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.plugins.AbstractInstanceCache;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.local.BaseLocalProxyFactory;
import org.jboss.ejb.timer.ContainerTimerService;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.management.j2ee.EJB;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EjbLocalRefMetaData;
import org.jboss.metadata.EjbRefMetaData;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.ResourceEnvRefMetaData;
import org.jboss.metadata.ResourceRefMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.monitor.StatisticsProvider;
import org.jboss.mx.loading.UnifiedClassLoader;
import org.jboss.naming.ENCThreadLocalKey;
import org.jboss.util.naming.Util;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.NestedError;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;
import org.jboss.system.Registry;
import org.jboss.util.jmx.ObjectNameConverter;
import org.w3c.dom.Element;

/**
 * This is the base class for all EJB-containers in JBoss. A Container
 * functions as the central hub of all metadata and plugins. Through this
 * the container plugins can get hold of the other plugins and any metadata
 * they need.
 *
 * <p>The EJBDeployer creates instances of subclasses of this class
 *    and calls the appropriate initialization methods.
 *    
 * <p>A Container does not perform any significant work, but instead delegates
 *    to the plugins to provide for all kinds of algorithmic functionality.
 *
 * @see EJBDeployer
 * 
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 * @author <a href="mailto:rickard.oberg@jboss.org">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.111 $
 *
 * @todo convert all the deployment/service lifecycle stuff to an 
 * aspect/interceptor.  Make this whole stack into a model mbean.
 */
public abstract class Container extends ServiceMBeanSupport
   implements MBeanRegistration, DynamicMBean, 
	      StatisticsProvider, InstancePoolContainer
{
   public final static String BASE_EJB_CONTAINER_NAME = 
         "jboss.j2ee:service=EJB";

   public final static ObjectName EJB_CONTAINER_QUERY_NAME = 
         ObjectNameFactory.create(BASE_EJB_CONTAINER_NAME + ",*");
   
   // Constants uses with container interceptor configurations
   public static final int BMT = 1;
   public static final int CMT = 2;
   public static final int ANY = 3;
   
   static final String BMT_VALUE = "Bean";
   static final String CMT_VALUE = "Container";
   static final String ANY_VALUE = "Both";
   
   /** A reference to {@link TimedObject#ejbTimeout}. */
   protected static final Method EJB_TIMEOUT;
   
   /**
    * Initialize <tt>TimedObject</tt> method references.
    */
   static {
      try {
         EJB_TIMEOUT = TimedObject.class.getMethod( "ejbTimeout", new Class[] { Timer.class } );
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   /**
    * Externally supplied configuration data
    */
   private DeploymentInfo di;

   /**
    * This is the new metadata. it includes information from both ejb-jar and
    * jboss.xml the metadata for the application can be accessed trough
    * metaData.getApplicationMetaData()
    */
   protected BeanMetaData metaData;
   
   /** 
    * This is the application that this container is a part of 
    */
   protected EjbModule ejbModule;
   
   /** 
    * ObjectName of Container
    * @todo use getObjectName() from serviceMBeanSupport.
    */
   private ObjectName jmxName;

   /**
    * This is the local classloader of this container. Used for loading
    * resources that must come from the local jar file for the container.
    * NOT for loading classes!
    */
   protected ClassLoader localClassLoader;
   
   /**
    * This is the classloader of this container. All classes and resources that
    * the bean uses will be loaded from here. By doing this we make the bean
    * re-deployable
    */
   protected ClassLoader classLoader;
   
   /** 
    * The class loader for remote dynamic classloading 
    */
   protected ClassLoader webClassLoader;

   /** 
    * This is the EnterpriseBean class.
    */
   protected Class beanClass;
   
   /** 
    * This is the Home interface class.
    */
   protected Class homeInterface;
   
   /**
    * This is the Remote interface class.
    */
   protected Class remoteInterface;
   
   /** 
    * This is the Local Home interface class
    */
   protected Class localHomeInterface;
   
   /**
    * This is the Local interface class
    */
   protected Class localInterface;
    
   /** 
    * This is the instance cache for this container 
    */
   private InstanceCache instanceCache;
   
   /** 
    * This is the instancepool that is to be used 
    */
   private InstancePool instancePool;
  
   /** 
    * This is the TransactionManager
    */
   protected TransactionManager tm;
   
   /**
    * This is the SecurityManager 
    */
   protected AuthenticationManager sm;
   
   /** 
    * This is the realm mapping
    */
   protected RealmMapping rm;
   
   /**
    * The custom security proxy used by the SecurityInterceptor
    */
   protected Object securityProxy;
   
   /**
    * This is the bean lock manager that is to be used
    */
   protected BeanLockManager lockManager;
   
   /**
    * The proxy factory for local interfaces
    */
   protected LocalProxyFactory localProxyFactory = new BaseLocalProxyFactory();
   
   /**
    * This is a cache for method permissions
    */
   private HashMap methodPermissionsCache = new HashMap();
   
   /**
    * Maps for MarshalledInvocation mapping
    */
   protected Map marshalledInvocationMapping = new HashMap();
   
   /**
    * This Container's codebase, a sequence of URLs separated by spaces
    */
   protected String codebase = "";
   
   /**
    * ObjectName of the JSR-77 EJB representation
    */
   protected String mEJBObjectName;
   
   /**
    * ??? What is this for ???
    */
   protected HashMap proxyFactories = new HashMap();

   /** 
    * The Proxy factory is set in the Invocation.  This TL is used
    * for methods that do not have access to the Invocation.
    */
   protected ThreadLocal proxyFactoryTL = new ThreadLocal();

   /**
    * boolean <code>started</code> indicates if this container is currently 
    * started. if not, calls to non lifecycle methods will raise exceptions.
    */
   private boolean started = false;
   
   /**
    * This is the first interceptor in the chain. The last interceptor must
    * be provided by the container itself.
    */
   protected Interceptor interceptor;
   
   /**
    * Timer Service for this Container
    **/
   public HashMap timerServices = new HashMap();

   private Map methodToTxSupportMap;
   
   /**
    * Get the Di value.
    * @return the Di value.
    */
   public DeploymentInfo getDeploymentInfo()
   {
      return di;
   }

   /**
    * Set the Di value.
    * @param newDi The new Di value.
    */
   public void setDeploymentInfo(DeploymentInfo di)
   {
      this.di = di;
   }

   /**
    * Sets the application deployment unit for this container. All the bean
    * containers within the same application unit share the same instance.
    *
    * @param   app     application for this container
    */
   public void setEjbModule(EjbModule app)
   {
      if(app == null)
      {
         throw new IllegalArgumentException("Null EjbModule");
      }
      ejbModule = app;
   }
   
   public EjbModule getEjbModule()
   {
      return ejbModule;
   }
   
   /**
    * Sets the meta data for this container. The meta data consists of the
    * properties found in the XML descriptors.
    *
    * @param metaData
    */
   public void setBeanMetaData(final BeanMetaData metaData)
   {
      this.metaData = metaData;
   }
   
   /**
    * Returns the metadata of this container.
    *
    * @return metaData;
    */
   public BeanMetaData getBeanMetaData()
   {
      return metaData;
   }

   public Class getHomeClass()
   {
      return homeInterface;
   }
   
   public Class getRemoteClass()
   {
      return remoteInterface;
   }
 
   public Class getLocalClass() 
   {
      return localInterface;
   }
   
   public Class getLocalHomeClass() 
   {
      return localHomeInterface;
   }

   public void setInstancePool(InstancePool instancePool)
   {
      if(instancePool == null) 
      {
         throw new IllegalArgumentException("instancePool is null");
      }

      this.instancePool = instancePool;
      instancePool.setContainer(this);
   }
   
   public InstancePool getInstancePool()
   {
      return instancePool;
   }

   public void setInstanceCache(InstanceCache instanceCache)
   {
      if(instanceCache == null)
      {
         throw new IllegalArgumentException("instanceCache is null");
      }
      this.instanceCache = instanceCache;
      instanceCache.setContainer(this);
   }

   public InstanceCache getInstanceCache()
   {
      return instanceCache;
   }

   /**
    * Sets a transaction manager for this container.
    *
    * @see javax.transaction.TransactionManager
    *
    * @param tm
    */
   public void setTransactionManager(final TransactionManager tm)
   {
      this.tm = tm;
   }
   
   /**
    * Returns this container's transaction manager.
    *
    * @return    A concrete instance of javax.transaction.TransactionManager
    */
   public TransactionManager getTransactionManager()
   {
      return tm;
   }
   
   public void setSecurityManager(AuthenticationManager sm)
   {
      this.sm = sm;
   }
   
   public AuthenticationManager getSecurityManager()
   {
      return sm;
   }
   
   public BeanLockManager getLockManager()
   {
      return lockManager;
   }
   
   public void setLockManager(final BeanLockManager lockManager) 
   {
      this.lockManager = lockManager;
      lockManager.setContainer(this);
   }
   
   public void addProxyFactory(String invokerBinding, EJBProxyFactory factory)
   {
      proxyFactories.put(invokerBinding, factory);
   }

   public void setRealmMapping(final RealmMapping rm)
   {
      this.rm = rm;
   }
   
   public RealmMapping getRealmMapping()
   {
      return rm;
   }
   
   public void setSecurityProxy(Object proxy)
   {
      this.securityProxy = proxy;
   }
   
   public Object getSecurityProxy()
   {
      return securityProxy;
   }
   
   public EJBProxyFactory getProxyFactory()
   {
      return (EJBProxyFactory)proxyFactoryTL.get();
   }

   public void setProxyFactory(Object factory)
   {
      proxyFactoryTL.set(factory);
   }
   
   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
   }
   
   public EJBProxyFactory lookupProxyFactory(String binding)
   {
      return (EJBProxyFactory)proxyFactories.get(binding);
   }

   /**
    * Sets the local class loader for this container. 
    * Used for loading resources from the local jar file for this container. 
    * NOT for loading classes!
    *
    * @param cl the new local class loader
    */
   public void setLocalClassLoader(ClassLoader cl)
   {
      this.localClassLoader = cl;
   }
   
   /**
    * Returns the local classloader for this container.
    *
    * @return the local classloader for this container.
    */
   public ClassLoader getLocalClassLoader()
   {
      return localClassLoader;
   }
   
   /**
    * Sets the class loader for this container. All the classes and resources
    * used by the bean in this container will use this classloader.
    *
    * @param cl the new class loader
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }
   
   /**
    * Returns the class loader for this container.
    *
    * @return the class loader for this container
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /** 
    * Get the class loader for dynamic class loading via http.
    */
   public ClassLoader getWebClassLoader()
   {
      return webClassLoader;
   }

   /** 
    * Set the class loader for dynamic class loading via http.
    */
   public void setWebClassLoader(final ClassLoader webClassLoader)
   {
      this.webClassLoader = webClassLoader;
   }


   /**
    * Get the MethodToTxSupportMap value.
    * @return the MethodToTxSupportMap value.
    */
   public Map getMethodToTxSupportMap()
   {
      return methodToTxSupportMap;
   }

   public Map getMethodHashToTxSupportMap()
   {
      Map result = new HashMap(methodToTxSupportMap.size());
      for (Iterator i = methodToTxSupportMap.entrySet().iterator(); i.hasNext();)
      {
	 Map.Entry e = (Map.Entry)i.next();
	 result.put(new Integer(e.getKey().hashCode()), e.getValue()); 
      } // end of for ()
      return result;
   }

   /**
    * Set the MethodToTxSupportMap value.
    * @param methodToTxSupportMap The new MethodToTxSupportMap value.
    */
   public void setMethodToTxSupportMap(Map methodToTxSupportMap)
   {
      this.methodToTxSupportMap = methodToTxSupportMap;
   }

   
   
   /**
    * Returns the permissions for a method. (a set of roles)
    *
    * @return assemblyDescriptor;
    */
   public Set getMethodPermissions(Method m, InvocationType iface)
   {
      Set permissions;
      
      if (methodPermissionsCache.containsKey(m))
      {
         permissions = (Set) methodPermissionsCache.get( m );
      }
      else
      {
         String name = m.getName();
         Class[] sig = m.getParameterTypes();
         permissions = getBeanMetaData().getMethodPermissions(name, sig, iface);
         methodPermissionsCache.put(m, permissions);
      }

      return permissions;
   }
   
   /**
    * Returns the bean class instance of this container.
    *
    * @return    instance of the Enterprise bean class.
    */
   public Class getBeanClass()
   {
      return beanClass;
   }
   
   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * This factory style method is speciffically used by a container to supply
    * an implementation of the abstract accessors in EJB2.0, but could be 
    * usefull in other situations. This method should ALWAYS be used instead 
    * of getBeanClass().newInstance();
    * 
    * @return    the new instance
    * 
    * @see java.lang.Class#newInstance 
    */
   public Object createBeanClassInstance() throws Exception {
      return getBeanClass().newInstance();
   }
   
   /**
    * Sets the codebase of this container.
    * 
    * @param   codebase a possibly empty, but non null String with 
    *                   a sequence of URLs separated by spaces
    */
   public void setCodebase(final String codebase) 
   { 
      // Why not throw an IllegalArgumentException???
      if(codebase != null) 
      {
         this.codebase = codebase;
      }
   }

   /**
    * Gets the codebase of this container.
    * 
    * @return    this container's codebase String, a sequence of URLs 
    *            separated by spaces 
    */
   public String getCodebase() 
   { 
      return codebase; 
   }
 
   public ObjectName getJmxName()
   {
      BeanMetaData beanMetaData = getBeanMetaData();
      String jndiName = beanMetaData.getHome() != null ?
         beanMetaData.getJndiName() : beanMetaData.getLocalJndiName();

      if (jndiName == null) 
      {
         throw new IllegalStateException("cannot get Container object " +
               "name unless jndi name is set!");
      }

      if (jmxName == null)
      {
         // The name must be escaped since the jndiName may be arbitrary
         String name = BASE_EJB_CONTAINER_NAME + ",jndiName=" + jndiName;
         try
         {
            jmxName = ObjectNameConverter.convert(name);
         }
         catch(MalformedObjectNameException e)
         {
            throw new RuntimeException("Failed to create ObjectName, msg="+e.getMessage());
         }
      }
      return jmxName;
   }
 
   /**
    * The EJBDeployer calls this method.  The EJBDeployer has set
    * all the plugins and interceptors that this bean requires and now proceeds
    * to initialize the chain.  The method looks for the standard classes in 
    * the URL, sets up the naming environment of the bean. The concrete 
    * container classes should override this method to introduce
    * implementation specific initialization behaviour.
    *
    * @throws Exception    if loading the bean class failed
    *                      (ClassNotFoundException) or setting up "java:"
    *                      naming environment failed (DeploymentException)
    */
   protected void createService() throws Exception
   {
      // Create JSR-77 EJB-Wrapper
      log.debug( "Application.create(), create JSR-77 EJB-Component" );

      int lType =
         metaData.isSession() ?
         ( ( (SessionMetaData) metaData ).isStateless() ? 2 : 1 ) :
         ( metaData.isMessageDriven() ? 3 : 0 );
      ObjectName lEJB = EJB.create(
         server,
         getEjbModule().getModuleName() + "",
         lType,
         metaData.getJndiName(),
         jmxName
         );
      log.debug( "Application.start(), EJB: " + lEJB );
      if( lEJB != null ) {
         mEJBObjectName = lEJB.toString();
      }


      // Acquire classes from CL
      beanClass = classLoader.loadClass(metaData.getEjbClass());
      
      if (metaData.getLocalHome() != null)
         localHomeInterface = classLoader.loadClass(metaData.getLocalHome());
      if (metaData.getLocal() != null)
         localInterface = classLoader.loadClass(metaData.getLocal());
      
      localProxyFactory.setContainer( this );
      localProxyFactory.create();
      if (localHomeInterface != null)
         ejbModule.addLocalHome(this, localProxyFactory.getEJBLocalHome() );
   }
   
   /**
    * Creates the single Timer Servic for this container if not already created
    *
    * @param pContext Context of the EJB
    *
    * @return Container Timer Service
    *
    * @throws IllegalStateException If the type of EJB is not allowed to use the timer service
    *
    * @see javax.ejb.EJBContext#getTimerService
    *
    * @jmx:managed-operation
    **/
   public TimerService getTimerService( Object pKey )
      throws IllegalStateException
   {
      if( this instanceof StatefulSessionContainer ) {
         throw new IllegalStateException( "Statefull Session Beans are not allowed to access Timer Service" );
      }
      TimerService timerService = (TimerService) timerServices.get(
         ( pKey == null ? "null" : pKey )
      );
      if( timerService == null ) {
         try {
            timerService = (TimerService) server.invoke(
               new ObjectName( "jboss:service=EJBTimerService" ),
               "createTimerService",
               new Object[] { getJmxName().toString(), this, pKey },
               new String[] { String.class.getName(), Container.class.getName(), Object.class.getName() }
            );
            timerServices.put(
               ( pKey == null ? "null" : pKey ),
               timerService
            );
         }
         catch( Exception e ) {
            throw new RuntimeException( "Could not create timer service: " + e );
         }
      }
      return timerService;
   }
   
   /**
    * Stops all the timers created by beans of this container
    **/
   public void stopTimers() {
      Iterator i = timerServices.values().iterator();
      while( i.hasNext() ) {
         TimerService timerService = (TimerService) i.next();
         ( (ContainerTimerService) timerService ).stopService();
         i.remove();
      }
   }
   
   /**
    * Handles an Timed Event by gettting the appropriate EJB instance,
    * invoking the "ejbTimeout()" method on it with the given timer
    *
    * @param pTimer Timer causing this event
    **/
   public void handleEjbTimeout( Timer pTimer ) {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( getClassLoader() );
      
      try
      {
         Invocation invocation = new Invocation(
            "JNDI-NAME ??",
            EJB_TIMEOUT, 
            new Class[] { Timer.class },
            ( getTransactionManager() == null ?
                 null:
                 getTransactionManager().getTransaction()
            ),
            SecurityAssociation.getPrincipal(), 
            SecurityAssociation.getCredential()
         );
         invocation.setArguments( new Object[] { pTimer } );
         invocation.setType( InvocationType.LOCAL );
         
         invoke( invocation );
      }
      catch( Exception e ) {
          e.printStackTrace();
          throw new RuntimeException( "call ejbTimeout() failed: " + e );
      }
/*AS TODO: Manage the exceptions properly
      catch (AccessException ae)
      {
         throw new AccessLocalException( ae.getMessage(), ae );
      }
      catch (NoSuchObjectException nsoe)
      {
         throw new NoSuchObjectLocalException( nsoe.getMessage(), nsoe );
      }
      catch (TransactionRequiredException tre)
      {
         throw new TransactionRequiredLocalException( tre.getMessage() );
      }
      catch (TransactionRolledbackException trbe)
      {
         throw new TransactionRolledbackLocalException( 
               trbe.getMessage(), trbe );
      }
*/
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   /**
    * A default implementation of starting the container service.
    * The container registers it's dynamic MBean interface in the JMX base.
    * 
    * The concrete container classes should override this method to introduce
    * implementation specific start behaviour.
    *
    * @todo implement the service lifecycle methods in an xmbean interceptor so 
    * non lifecycle managed ops are blocked when mbean is not started.
    * 
    * @throws Exception    An exception that occured during start
    */
   protected void startService() throws Exception
   {
      // Setup "java:comp/env" namespace
      setupEnvironment();
      started = true;
      // Start all interceptors in the chain
      Interceptor in = interceptor;
      while (in != null)
      {
	 in.start();
	 in = in.getNext();
      }
      localProxyFactory.start();

      // We keep the hashCode around for fast creation of proxies
      int jmxHash = jmxName.hashCode();
      Registry.bind(new Integer(jmxHash), jmxName);
      log.debug("Bound jmxName="+jmxName+", hash="+jmxHash+"into Registry");
      if( !( this instanceof StatefulSessionContainer ) ) {
         // Restore Timers
         ContainerTimerService temp = (ContainerTimerService) getTimerService( null );
         // Start Recovery
         temp.startRecovery();
      }
   }
   
   /**
    * A default implementation of stopping the container service (no-op). The
    * concrete container classes should override this method to introduce
    * implementation specific stop behaviour.
    */
   protected void stopService() throws Exception
   {
      int jmxHash = jmxName.hashCode();
      Registry.unbind(new Integer(jmxHash));

      started = false;
      localProxyFactory.stop();
      log.info( "=======================> Stop Timers" );
      stopTimers();
      teardownEnvironment();
      WebServiceMBean webServer = 
         (WebServiceMBean)MBeanProxy.create(WebServiceMBean.class, 
                                            WebServiceMBean.OBJECT_NAME);
      ClassLoader wcl = getWebClassLoader();
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

      // Stop all interceptors in the chain
      Interceptor in = interceptor;
      while (in != null)
      {
	 in.stop();
	 in = in.getNext();
      }
   }
   
   /**
    * A default implementation of destroying the container service
    * The concrete container classes should override this method to introduce
    * implementation specific destroy behaviour.
    */
   protected void destroyService() throws Exception
   {
      // Remove JSR-77 EJB-Wrapper
      if( mEJBObjectName != null )
      {
         EJB.destroy( getServer(), mEJBObjectName );
      }
      localProxyFactory.destroy();
      ejbModule.removeLocalHome( this );
      this.classLoader = null;
      this.webClassLoader = null;
      this.localClassLoader = null;
      
      // this.lockManager = null; Setting this to null causes AbstractCache 
      // to fail on undeployment
      this.methodPermissionsCache.clear();
   }

   /**
    * This method is called when a method call comes
    * in on an EJBObject.  The Container forwards this call to the interceptor
    * chain for further processing.
    *
    * @param invocation the invocation information
    * @return the result of the invocation
    * @throws Exception if a problem occurs 
    */
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      try
      {
         return getInterceptor().invoke(invocation);
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   // DynamicMBean interface implementation ----------------------------------
   
   public Object getAttribute(String attribute)
      throws AttributeNotFoundException,
             MBeanException,
             ReflectionException
   {
      if("ClassLoader".equals(attribute))
      {
         return getClassLoader();
      }
      if("BeanClass".equals(attribute))
      {
         return getBeanClass();
      }
      if("BeanMetaData".equals(attribute))
      {
         return getBeanMetaData();
      }
      if("State".equals(attribute))
      {
         return new Integer(getState());
      }
      if("StateString".equals(attribute))
      {
         return getStateString();
      }
      throw new AttributeNotFoundException("invalid attribute: " + attribute);
   }

   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException,
             InvalidAttributeValueException,
             MBeanException,
             ReflectionException
   {
   }
   
   public AttributeList getAttributes(String[] attributes)
   {
      return null;
   }
   
   public AttributeList setAttributes(AttributeList attributes)
   {
      return null;
   }
   
   /**
    * Handle a operation invocation.
    *
    * @todo fix all the "remove when cl integrated" code", marc.
    *
    * @param ignored a <code>String</code> value
    * @param params an <code>Object[]</code> value
    * @param signature a <code>String[]</code> value
    * @return an <code>Object</code> value
    * @exception MBeanException if an error occurs
    * @exception ReflectionException if an error occurs
    */
   public Object invoke(String ignored, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {      
      if (params != null && 
            params.length == 1 && 
            (params[0] instanceof Invocation))
      {
         if (!started) 
         {
            throw new IllegalStateException("container is not started, you " +
                  "cannot invoke ejb methods on it");
         }
      
         // We are have a valid (not-null) invocation because of 
         // the instanceof check above
         Invocation invocation = (Invocation)params[0];

         // set the thread context class loader
         // dain: do we need to reset the class loader at the end of the call?
         ClassLoader callerClassLoader = 
               Thread.currentThread().getContextClassLoader();
         try
         {
            Thread.currentThread().setContextClassLoader(this.classLoader);        
            // Check against home, remote, localHome, local, getHome, 
            // getRemote, getLocalHome, getLocal
            Object type = invocation.getType();
            if(type == InvocationType.REMOTE ||
                  type == InvocationType.LOCAL) 
            {
               if (invocation instanceof MarshalledInvocation)
               {
                  ((MarshalledInvocation) invocation).setMethodMap(
                        marshalledInvocationMapping);
                  
                  if (log.isTraceEnabled())
                  {
                     log.trace("METHOD REMOTE INVOKE "+
                           invocation.getObjectName()+"||"+
                           invocation.getMethod().getName()+"||");
                  }
               }
               
               return invoke(invocation);
            }
            else if(type == InvocationType.HOME ||
                  type == InvocationType.LOCALHOME)
            {
               if (invocation instanceof MarshalledInvocation)
               {
                  
                  ((MarshalledInvocation) invocation).setMethodMap(
                        marshalledInvocationMapping);
                  
                  if (log.isTraceEnabled())
                  {
                     log.trace("METHOD HOME INVOKE " +
                           invocation.getObjectName() + "||"+
                           invocation.getMethod().getName() + "||"+
                           invocation.getArguments().toString());
                  }
               }
               
               return invoke(invocation);
            }
            else 
            {
               throw new MBeanException(new IllegalArgumentException(
                        "Unknown invocation type: " + type));
            }
         }
         catch (Exception e)
         {
            throw new MBeanException(e, "invoke failed");
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(callerClassLoader);
         }
      }
      else if (params == null || params.length == 0) 
      {
         try 
         {
            if ("create".equals(ignored)) 
            {
               create();
            }
            else if ("start".equals(ignored)) 
            {
               start();
            }
            else if ("stop".equals(ignored)) 
            {
               stop();
            }
            else if ("destroy".equals(ignored)) 
            {
               destroy();
            }
            else
            {
               throw new IllegalArgumentException("unknown operation! " + 
                     ignored);
            }
            
            return null;
         }
         catch (Exception e)
         {
            throw new MBeanException(e, 
                  "Exception in service lifecyle operation: " + ignored);
         }
      }
      else if (params != null && params.length == 2 && params[0] instanceof List && params[1] instanceof Boolean)
      {
         retrieveStatistics( (List) params[0], ( (Boolean) params[1] ).booleanValue() );
         return null;
      }
      else
      {
         throw new IllegalArgumentException(
               "Expected zero or single Invocation argument");
      }
   }
   
   /**
    * Build the container MBean information on attributes, contstructors,
    * operations, and notifications. Currently there are no attributes, no
    * constructors, no notifications, and the following ops:
    * <ul>
    * <li>'home' -> invokeHome(Invocation);</li>
    * <li>'remote' -> invoke(Invocation);</li>
    * <li>'localHome' -> not implemented;</li>
    * <li>'local' -> not implemented;</li>
    * <li>'getHome' -> return EBJHome interface;</li>
    * <li>'getRemote' -> return EJBObject interface</li>
    * <li>'create' -> create service lifecycle operation</li>
    * <li>'start' -> start service lifecycle operation</li>
    * <li>'stop' -> stop service lifecycle operation</li>
    * <li>'destroy' -> destroy service lifecycle operation</li>
    * </ul>
    */
   public MBeanInfo getMBeanInfo()
   {
      MBeanParameterInfo[] miInfoParams = new MBeanParameterInfo[] {
         new MBeanParameterInfo(
               "method", 
               Invocation.class.getName(), 
               "Invocation data")
      };
      
      MBeanParameterInfo[] miStatisticsParams = new MBeanParameterInfo[] {
         new MBeanParameterInfo(
               "container", 
               List.class.getName(), 
               "Statitic Data Container"),
         new MBeanParameterInfo(
               "reset", 
               Boolean.TYPE.getName(), 
               "If true reset statisitcs data")
      };
      
      MBeanParameterInfo[] noParams = new MBeanParameterInfo[] {};
      
      MBeanConstructorInfo[] ctorInfo = new  MBeanConstructorInfo[] {};
      
      MBeanAttributeInfo[] attrInfo = new MBeanAttributeInfo[] {
	 new MBeanAttributeInfo("ClassLoader",
				"java.lang.ClassLoader",
				"Return the contained object's classloader",
				true,
				false,
				false),
	 new MBeanAttributeInfo("BeanClass",
				"java.lang.Class",
				"Return the Beans class",
				true,
				false,
				false),
	 new MBeanAttributeInfo("BeanMetaData",
				"org.jboss.metadata.BeanMetaData",
				"Return Beans metadata object",
				true,
				false,
				false),
	 new MBeanAttributeInfo("State",
				"int",
				"Return the containers state",
				true,
				false,
				false),
	 new MBeanAttributeInfo("StateString",
				"java.lang.String",
				"Return the container's state as a String",
				true,
				false,
				false)
      };
      
      MBeanOperationInfo[] opInfo = {
         new MBeanOperationInfo("home",
                                "Invoke an EJBHome interface method",
                                miInfoParams,
                                "java.lang.Object",
                                MBeanOperationInfo.ACTION_INFO),
         
         new MBeanOperationInfo("remote",
                                "Invoke an EJBObject interface method",
                                miInfoParams,
                                "java.lang.Object",
                                MBeanOperationInfo.ACTION_INFO),
         
         new MBeanOperationInfo("getHome",
                                "Get the EJBHome interface class",
                                noParams,
                                "java.lang.Class",
                                MBeanOperationInfo.INFO),
         
         new MBeanOperationInfo("getRemote",
                                "Get the EJBObject interface class",
                                noParams,
                                "java.lang.Class",
                                MBeanOperationInfo.INFO),
         
         new MBeanOperationInfo("create",
                                "create service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION),
         
         new MBeanOperationInfo("start",
                                "start service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION),
         
         new MBeanOperationInfo("stop",
                                "stop service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION),
         
         new MBeanOperationInfo("destroy",
                                "destroy service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION),
                                
         new MBeanOperationInfo("retrieveStatistics",
                                "retrieve the performance statistics",
                                miStatisticsParams,
                                "void",
                                MBeanOperationInfo.ACTION)
      };
      
      MBeanNotificationInfo[] notifyInfo = null;
      return new MBeanInfo(getClass().getName(), 
                           "EJB Container MBean",
                           attrInfo, 
                           ctorInfo, 
                           opInfo, 
                           notifyInfo);
   }
   
   // End DynamicMBean interface
   
   abstract Interceptor createContainerInterceptor();
   
   public void addInterceptor(Interceptor in)
   {
      if (interceptor == null)
      {
         interceptor = in;
      }
      else
      {
         Interceptor current = interceptor;
         while (current.getNext() != null)
         {
            current = current.getNext();
         }
         
         current.setNext(in);
      }
   }
   
   public Interceptor getInterceptor()
   {
      return interceptor;
   }
  
   /**
    * This method sets up the naming environment of the bean.
    * We create the java:comp/env namespace with properties, EJB-References,
    * and DataSource ressources.
    */
   private void setupEnvironment() throws Exception
   {
      boolean debug = log.isDebugEnabled();
      BeanMetaData beanMetaData = getBeanMetaData();

      if (debug)
      {
         log.debug("Begin java:comp/env for EJB: "+beanMetaData.getEjbName());
         log.debug("TCL: "+Thread.currentThread().getContextClassLoader());
      }
      
      // Since the BCL is already associated with this thread we can start
      // using the java: namespace directly
      Context ctx = (Context) new InitialContext().lookup("java:comp");
      Context envCtx = ctx.createSubcontext("env");
         
      // Bind environment properties
      {
         Iterator enum = beanMetaData.getEnvironmentEntries();
         while(enum.hasNext())
         {
            EnvEntryMetaData entry = (EnvEntryMetaData)enum.next();
            if (debug) {
               log.debug("Binding env-entry: "+entry.getName()+" of type: "+
                         entry.getType()+" to value:"+entry.getValue());
            }
               
            EnvEntryMetaData.bindEnvEntry(envCtx, entry);
         }
      }
      
      // Bind EJB references
      {
         Iterator enum = beanMetaData.getEjbReferences();
         while(enum.hasNext())
         {
            EjbRefMetaData ref = (EjbRefMetaData)enum.next();
            if (debug)
               log.debug("Binding an EJBReference "+ref.getName());
            
            if (ref.getLink() != null)
            {
               // Internal link
               if (debug) {
                  log.debug("Binding "+ref.getName()+
                        " to internal JNDI source: "+ref.getLink());
               }
               String jndiName = EjbUtil.findEjbLink( server, di, ref.getLink() );

               Util.bind(
                     envCtx, 
                     ref.getName(), 
                     new LinkRef(jndiName));
               
            }
            else
            {
               Iterator it = beanMetaData.getInvokerBindings();
               Reference reference = null;
               while (it.hasNext())
               {
                  String invokerBinding = (String)it.next();
                  String name = ref.getInvokerBinding(invokerBinding);
                  if (name == null) name = ref.getJndiName();
                  if (name == null) // still null?
                  {
                     throw new DeploymentException
                        ("ejb-ref "+ref.getName()+
                         ", expected either ejb-link in ejb-jar.xml or " +
                         "jndi-name in jboss.xml");
                  }
                  
                  StringRefAddr addr = new StringRefAddr(invokerBinding, name);
                  log.debug("adding " + invokerBinding + ":" + name + 
                        " to Reference");
                  
                  if (reference == null)
                  {
                     reference = new Reference("javax.naming.LinkRef",
                           ENCThreadLocalKey.class.getName(),
                           null);
                  }
                  reference.add(addr);
               }
               if (reference != null)
               {
                  if (ref.getJndiName() != null)
                  {
                     // Add default
                     StringRefAddr addr = 
                           new StringRefAddr("default", ref.getJndiName());
                     reference.add(addr);
                  }
                  Util.bind(envCtx, ref.getName(), reference);
               }
               else
               {
                  if (ref.getJndiName() == null)
                  {
                     throw new DeploymentException("ejb-ref " + ref.getName()+
                         ", expected either ejb-link in ejb-jar.xml " +
                         "or jndi-name in jboss.xml");
                  }
                  Util.bind(
                        envCtx, 
                        ref.getName(), 
                        new LinkRef(ref.getJndiName()));
               }
            }
         }
      }
         
      // Bind Local EJB references
      {
         Iterator enum = beanMetaData.getEjbLocalReferences();
         // unique key name
         String localJndiName = beanMetaData.getLocalJndiName();
         while(enum.hasNext())
         {
            EjbLocalRefMetaData ref = (EjbLocalRefMetaData)enum.next();
            String refName = ref.getName();
            log.debug("Binding an EJBLocalReference "+ref.getName());
            
            if (ref.getLink() != null)
            {
               // Internal link
               log.debug("Binding "+refName+" to bean source: "+ref.getLink());

               String jndiName = EjbUtil.findLocalEjbLink( server, di,
                  ref.getLink() );

               Util.bind(envCtx,
                     ref.getName(),
                     new LinkRef(jndiName));
            }
            else
            {
               throw new DeploymentException("Local references currently " +
                     "require ejb-link" );
            }
         }
      }

      // Bind resource references
      {
         Iterator enum = beanMetaData.getResourceReferences();
         
         // let's play guess the cast game ;)  New metadata should fix this.
         ApplicationMetaData application = 
               beanMetaData.getApplicationMetaData();
         
         while(enum.hasNext())
         {
            ResourceRefMetaData ref = (ResourceRefMetaData)enum.next();
            
            String resourceName = ref.getResourceName();
            String finalName = application.getResourceByName(resourceName);
            // If there was no resource-manager specified then an immeadiate
            // jndi-name or res-url name should have been given
            if (finalName == null)
               finalName = ref.getJndiName();
            
            if (finalName == null)
            {
               // the application assembler did not provide a resource manager
               // if the type is javax.sql.Datasoure use the default one
                  
               if (ref.getType().equals("javax.sql.DataSource"))
               {
                  // Go through JNDI and look for DataSource - use the first one
                  Context dsCtx = new InitialContext();
                  try
                  {
                     // Check if it is available in JNDI
                     dsCtx.lookup("java:/DefaultDS");
                     finalName = "java:/DefaultDS";
                  }
                  catch (Exception e)
                  {
                     if (debug)
                        log.debug("failed to lookup DefaultDS; ignoring", e);
                  }
                  finally {
                     dsCtx.close();
                  }
               }
                  
               // Default failed? Warn user and move on
               // POTENTIALLY DANGEROUS: should this be a critical error?
               if (finalName == null)
               {
                  log.warn("No resource manager found for " +
                        ref.getResourceName());
                  continue;
               }
            }
            
            if (ref.getType().equals("java.net.URL"))
            {
               // URL bindings
               if (debug)
                  log.debug("Binding URL: " + finalName + 
                        " to JDNI ENC as: " + ref.getRefName());
               Util.bind(envCtx, ref.getRefName(), new URL(finalName));
            }
            else
            {
               // Resource Manager bindings, should validate the type...
               if (debug) {
                  log.debug("Binding resource manager: "+finalName+
                            " to JDNI ENC as: " +ref.getRefName());
               }
               
               Util.bind(envCtx, ref.getRefName(), new LinkRef(finalName));
            }
         }
      }
         
      // Bind resource env references
      {
         Iterator enum = beanMetaData.getResourceEnvReferences();
         while( enum.hasNext() )
         {
            ResourceEnvRefMetaData resRef = 
                  (ResourceEnvRefMetaData) enum.next();
            String encName = resRef.getRefName();
            String jndiName = resRef.getJndiName();
            // Should validate the type...
            if (debug)
               log.debug("Binding env resource: " + jndiName + 
                     " to JDNI ENC as: " +encName);
            Util.bind(envCtx, encName, new LinkRef(jndiName));
         }
      }
         
      // Create a java:comp/env/security/security-domain link to the container
      // or application security-domain if one exists so that access to the
      // security manager can be made without knowing the global jndi name.

      String securityDomain = 
            metaData.getContainerConfiguration().getSecurityDomain();
      if( securityDomain == null )
         securityDomain = metaData.getApplicationMetaData().getSecurityDomain();
      if( securityDomain != null )
      {
         if (debug) {
            log.debug("Binding securityDomain: "+securityDomain+
                      " to JDNI ENC as: security/security-domain");
         }
         
         Util.bind(
               envCtx, 
               "security/security-domain", 
               new LinkRef(securityDomain));
         Util.bind(
               envCtx, 
               "security/subject", 
               new LinkRef(securityDomain+"/subject"));
      }
      
      if (debug)
         log.debug("End java:comp/env for EJB: "+beanMetaData.getEjbName());
   }

   /**
    *The <code>teardownEnvironment</code> method unbinds everything from
    * the comp/env context.  It would be better do destroy the env context
    * but destroyContext is not currently implemented..
    *
    * @exception Exception if an error occurs
    */
   private void teardownEnvironment() throws Exception
   {
      Context ctx = (Context)new InitialContext().lookup("java:comp");
      ctx.unbind("env");
      log.debug("Removed bindings from java:comp/env for EJB: "+getBeanMetaData().getEjbName());
   }


   //----------------------------------------
   //Moved from EjbModule

   /**
    * Describe <code>typeSpecificInitialize</code> method here.
    * Override in type-specific subclasses.  Each implementation calls genericInitialize.
    */
   protected void typeSpecificInitialize()  throws Exception
   {}

   /**
    * Perform the common steps to initializing a container.
    *
    * @todo see if the webserver registration can be moved to the start step.
    */
   protected void genericInitialize( int transType,
                                     ClassLoader cl,
                                     ClassLoader localCl )
      throws NamingException, DeploymentException
   {
      // Create local classloader for this container
      // For loading resources that must come from the local jar.  Not for loading classes!
      setLocalClassLoader( new URLClassLoader( new URL[ 0 ], localCl ) );
      // Create the container's WebClassLoader 
      // and register it with the web service.
      String webClassLoaderName = getWebClassLoaderClassName();
      log.debug("Creating WebClassLoader of class " + webClassLoaderName);
      WebClassLoader wcl = null;
      try 
      {
         Class clazz = cl.loadClass(webClassLoaderName);
         Constructor constructor = clazz.getConstructor(
            new Class[] { ObjectName.class, UnifiedClassLoader.class } );
         wcl = (WebClassLoader)constructor.newInstance(
            new Object[] { getJmxName(), cl });
      }
      catch (Exception e) 
      {
         throw new DeploymentException(
            "Failed to create WebClassLoader of class " 
            + webClassLoaderName + ": ", e);
      }
      //THis is an invalid operation in the create lifecycle step.  
      //The webserver might not be started.
      WebServiceMBean webServer = 
         (WebServiceMBean)MBeanProxy.create(WebServiceMBean.class, 
                                            WebServiceMBean.OBJECT_NAME);
      URL[] codebase = { webServer.addClassLoader(wcl) };
      wcl.setWebURLs(codebase);
      setWebClassLoader(wcl);
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < codebase.length; i++)
      {
         sb.append(codebase[i].toString());
         if (i < codebase.length - 1)
         {
            sb.append(" ");
         }
      }
      setCodebase(sb.toString());

      // Create classloader for this container
      // Only used to unique the bean ENC and does not augment class loading
      setClassLoader( new URLClassLoader( new URL[ 0 ], wcl ) );

      // Set transaction manager
      InitialContext iniCtx = new InitialContext();
      setTransactionManager( (TransactionManager) iniCtx.lookup( "java:/TransactionManager" ) );
      
      // Set security domain manager
      String securityDomain = getBeanMetaData().getApplicationMetaData().getSecurityDomain();
      String confSecurityDomain = getBeanMetaData().getContainerConfiguration().getSecurityDomain();
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
            setSecurityManager( ejbS );
            setRealmMapping( rM );
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
      String securityProxyClassName = getBeanMetaData().getSecurityProxy();
      if( securityProxyClassName != null )
      {
         try
         {
            Class proxyClass = cl.loadClass(securityProxyClassName);
            Object proxy = proxyClass.newInstance();
            setSecurityProxy(proxy);
            log.debug("setSecurityProxy, "+proxy);
         }
         catch(Exception e)
         {
            throw new DeploymentException("Failed to create SecurityProxy of type: " +
                                          securityProxyClassName, e);
         }
      }
      
      // Install the container interceptors based on the configuration
      addInterceptors(transType, getBeanMetaData().getContainerConfiguration().getContainerInterceptorsConf());
   }

   /**
    * Return the name of the WebClassLoader class for this ejb.
    */
   private String getWebClassLoaderClassName()
      throws DeploymentException
   {
      String webClassLoader = null;
      Iterator it = getBeanMetaData().getInvokerBindings();
      int count = 0;
      while (it.hasNext())
      {
         String invoker = (String)it.next();
         ApplicationMetaData amd = getBeanMetaData().getApplicationMetaData();
         InvokerProxyBindingMetaData imd = (InvokerProxyBindingMetaData)
                           amd.getInvokerProxyBindingMetaDataByName(invoker);
         Element proxyFactoryConfig = imd.getProxyFactoryConfig();
         String webCL = MetaData.getOptionalChildContent(proxyFactoryConfig, 
                                                         "web-class-loader");
         if (webCL != null)
         {
            log.debug("Invoker " + invoker + " specified WebClassLoader class" + webCL);
            webClassLoader = webCL;
            count++;
         }
      }
      if (count > 1) {
         log.warn(count + " invokers have WebClassLoader specifications.");
         log.warn("Using the last specification seen (" + webClassLoader + ")."); 
      }
      else if (count == 0) {
         webClassLoader = getBeanMetaData().getContainerConfiguration().getWebClassLoader();
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
   private void addInterceptors(int transType,
                                Element element)
      throws DeploymentException
   {
      // Get the interceptor stack(either jboss.xml or standardjboss.xml)
      Iterator interceptorElements = MetaData.getChildrenByTagName(element, "interceptor");
      String transTypeString = stringTransactionValue(transType);
      ClassLoader loader = getClassLoader();
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
               throw new DeploymentException("couldn't contact EJBDeployer!", e);
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
      
      // Now add the interceptors
      for(int i = 0; i < istack.size(); i ++)
      {
         Interceptor interceptor = (Interceptor) istack.get(i);
         addInterceptor(interceptor);
      }
      
      /* If there is a security proxy associated with the container add its
         interceptor just before the container interceptor
      */
      if( getSecurityProxy() != null )
      {
         addInterceptor(new SecurityProxyInterceptor());
      }
      
      // Finally we add the last interceptor from the container
      addInterceptor(createContainerInterceptor());
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
   protected void createProxyFactories(ClassLoader cl )
      throws Exception
   {
      Iterator it = getBeanMetaData().getInvokerBindings();
      while (it.hasNext())
      {
         String invoker = (String)it.next();
         String jndiBinding = (String)getBeanMetaData().getInvokerBinding(invoker);
         log.debug("creating binding for " + jndiBinding + ":" + invoker);
         InvokerProxyBindingMetaData imd = (InvokerProxyBindingMetaData)getBeanMetaData().getApplicationMetaData().getInvokerProxyBindingMetaDataByName(invoker);
         EJBProxyFactory ci = null;

         // create a ProxyFactory instance
         try
         {
            ci = (EJBProxyFactory) cl.loadClass(imd.getProxyFactory()).newInstance();
            ci.setContainer(this);
            ci.setInvokerMetaData(imd);
            ci.setInvokerBinding(jndiBinding);
            if( ci instanceof XmlLoadable )
            {
               // the container invoker can load its configuration from the jboss.xml element
               ( (XmlLoadable) ci ).importXml(imd.getProxyFactoryConfig());
            }
            addProxyFactory(invoker, ci);
         }
         catch( Exception e )
         {
            throw new DeploymentException( "Missing or invalid Container Invoker (in jboss.xml or standardjboss.xml): " + invoker, e );
         }
      }
   }
   
   
   protected BeanLockManager createBeanLockManager(boolean reentrant, Element config,
                                                         ClassLoader cl )
      throws Exception
   {
      // The bean lock manager
      BeanLockManager lockManager = new BeanLockManager(this);
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
   
   protected  InstancePool createInstancePool( ConfigurationMetaData conf,
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
   
   protected static InstanceCache createInstanceCache( ConfigurationMetaData conf,
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
    * The base class for container interceptors.
    * 
    * <p>
    * All container interceptors perform the same basic functionality
    * and only differ slightly.
    */
   protected abstract class AbstractContainerInterceptor 
         extends AbstractInterceptor
   {
      protected void rethrow(Exception e)
         throws Exception
      {
         if (e instanceof IllegalAccessException) {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
         }
         else if(e instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException)e).getTargetException();

            if (t instanceof EJBException) {
               throw (EJBException)t;
            }
            else if (t instanceof Exception) {
               throw (Exception)t;
            }
            else if (t instanceof Error) {
               throw (Error)t;
            }
            else {
               throw new NestedError("Unexpected Throwable", t);
            }
         }

         throw e;
      }
   }
}
