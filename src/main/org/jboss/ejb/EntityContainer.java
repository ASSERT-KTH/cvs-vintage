/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.ClassLoader;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.EntityContext;
import javax.ejb.Timer;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.transaction.Transaction;
import javax.transaction.TransactionRolledbackException;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.timer.ContainerTimer;
import org.jboss.ejb.entity.EntityInvocationKey;
import org.jboss.ejb.entity.EntityInvocationType;
import org.jboss.ejb.entity.EntityInvocationRegistry;
import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.logging.Logger;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.monitor.StatisticsProvider;
import org.jboss.security.SecurityAssociation;
import org.jboss.util.collection.SerializableEnumeration;
import org.jboss.util.MethodHashing;

/**
 * This is a Container for EntityBeans (both BMP and CMP).
 *
 * @see Container
 * @see EntityEnterpriseContext
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.91 $
 */
public class EntityContainer
   extends Container implements EJBProxyFactoryContainer,
   InstancePoolContainer, StatisticsProvider
{
   private Interceptor rootEntityInterceptor;

   private Class primaryKeyClass;


   // These members contains statistics variable
   private long createCount = 0;
   private long removeCount = 0;

   /**
    * Determines if state can be written to resource manager.
    */
   private boolean readOnly = false;

   /**
    * Does this container support multiple instances with the same id?
    */
   private boolean multiInstance = false;

   /**
    * This provides a way to find the entities that are part of a given
    * transaction EntitySynchronizationInterceptor and InstanceSynchronization
    * manage this instance.
    */
   private static EntityInvocationRegistry entityInvocationRegistry = 
         new EntityInvocationRegistry();
   
   public static EntityInvocationRegistry getEntityInvocationRegistry()
   {
      return entityInvocationRegistry;
   }

   /**
    * Gets the primary key class of the Entity.
    */
   public Class getPrimaryKeyClass()
   {
     return primaryKeyClass;
   }

   /**
    * Is this entity read only?
    */
   public boolean isReadOnly()
   {
      return readOnly;
   }

   /**
    * Does this container user multiple instances with the same id?
    */
   public boolean isMultiInstance()
   {
      return multiInstance;
   }

   /**
    * Set the container to use or not user multiple instances with the
    * same id?
    */
   public void setMultiInstance(boolean multiInstance)
   {
      this.multiInstance = multiInstance;
   }

   public Interceptor getRootEntityInterceptor()
   {
      return rootEntityInterceptor;
   }

   public void setRootEntityInterceptor(Interceptor rootEntityInterceptor)
   {
      this.rootEntityInterceptor = rootEntityInterceptor;
   }

   protected void createService() throws Exception
   {
      typeSpecificInitialize();
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default init
         super.createService();

         // Acquire classes from CL
         if(metaData.getHome() != null)
         {
            homeInterface = classLoader.loadClass(metaData.getHome());
         }
         if(metaData.getRemote() != null)
         {
            remoteInterface = classLoader.loadClass(metaData.getRemote());
         }
         EntityMetaData entityMetaData = (EntityMetaData)metaData;
         if(entityMetaData.getPrimaryKeyClass() != null) {
            primaryKeyClass = classLoader.loadClass(
                  entityMetaData.getPrimaryKeyClass());
         }

         // Map the interfaces to Long
         setupMarshalledInvocationMapping();

         // Initialize the interceptor by calling the chain
         Interceptor in = interceptor;
         while(in != null)
         {
            in.setContainer(this);
            in.create();
            in = in.getNext();
         }

         readOnly = ((EntityMetaData)metaData).isReadOnly();

         // Initialize pool
         getInstancePool().create();

         for(Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory proxyFactory = 
                  (EJBProxyFactory) proxyFactories.get(invokerBinding);
            proxyFactory.create();
         }

         // Init instance cache
         getInstanceCache().create();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void startService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default start
         super.startService();

         // Start container invokers
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci =
                  (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.start();
         }

         // Start instance cache
         getInstanceCache().start();

         // Start the instance pool
         getInstancePool().start();

      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void stopService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         //Stop items in reverse order from start
         //This assures that CachedConnectionInterceptor will get removed
         //from in between this and the pm before the pm is stopped.
         // Stop all interceptors in the chain
         //??Might be a problem, the superclass is now also stopping the interceptors.
         Interceptor in = interceptor;
         while (in != null)
         {
            in.stop();
            in = in.getNext();
         }

         // Stop the instance pool
         getInstancePool().stop();

         // Stop instance cache
         getInstanceCache().stop();

         // Stop container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci =
                  (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.stop();
         }

         // Call default stop
         super.stopService();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void destroyService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Destroy container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci =
                  (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.destroy();
         }

         // Destroy instance cache
         getInstanceCache().destroy();
         getInstanceCache().setContainer(null);

         // Destroy the pool
         getInstancePool().destroy();
         getInstancePool().setContainer(null);

         // Destroy all the interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.destroy();
            in.setContainer(null);
            in = in.getNext();
         }

         // Call default destroy
         super.destroyService();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * If this is 1.x cmp, simply return a new instance of the bean class.
    * If this is 2.x cmp, return a subclass that provides an implementation
    * of the abstract accessors.
    *
    * @see java.lang.Class#newInstance
    *
    * @return   The new instance.
    */
   public Object createBeanClassInstance() throws Exception
   {
      Invocation invocation = new Invocation();
      invocation.setValue(
            EntityInvocationKey.TYPE,
            EntityInvocationType.CREATE_INSTANCE,
            PayloadKey.TRANSIENT);
      invocation.setValue(Entrancy.ENTRANCY_KEY,
            Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
      invocation.setType(InvocationType.LOCALHOME);
      InvocationResponse response = invokeEntityInterceptor(invocation);
      return response.getResponse();
      //return invokeHome(invocation);
   }

   public void storeEntity(EntityEnterpriseContext ctx) throws Exception
   {
      if(ctx.getId() == null)
      {
         return;
      }

      if(!isModified(ctx))
      {
         return;
      }

      Invocation invocation = new Invocation();
      invocation.setValue(
            EntityInvocationKey.TYPE,
            EntityInvocationType.STORE,
            PayloadKey.TRANSIENT);
      invocation.setValue(Entrancy.ENTRANCY_KEY,
            Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
      invocation.setType(InvocationType.LOCAL);
      invocation.setEnterpriseContext(ctx);
      invocation.setId(ctx.getId());
      invocation.setTransaction(ctx.getTransaction());
      invocation.setPrincipal(SecurityAssociation.getPrincipal());
      invocation.setCredential(SecurityAssociation.getCredential());
      invokeEntityInterceptor(invocation);
   }

   public boolean isModified(EntityEnterpriseContext ctx) throws Exception
   {
      Invocation invocation = new Invocation();
      invocation.setValue(
            EntityInvocationKey.TYPE,
            EntityInvocationType.IS_MODIFIED,
            PayloadKey.TRANSIENT);
      invocation.setValue(Entrancy.ENTRANCY_KEY,
            Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
      invocation.setType(InvocationType.LOCAL);
      invocation.setEnterpriseContext(ctx);
      invocation.setId(ctx.getId());
      invocation.setTransaction(ctx.getTransaction());
      invocation.setPrincipal(SecurityAssociation.getPrincipal());
      invocation.setCredential(SecurityAssociation.getCredential());
      InvocationResponse response = invokeEntityInterceptor(invocation);

      return ((Boolean)response.getResponse()).booleanValue();
   }

   public void activateEntity(EntityEnterpriseContext ctx) throws Exception
   {
      Invocation invocation = new Invocation();
      invocation.setValue(
            EntityInvocationKey.TYPE,
            EntityInvocationType.ACTIVATE,
            PayloadKey.TRANSIENT);
      invocation.setValue(Entrancy.ENTRANCY_KEY,
            Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
      invocation.setType(InvocationType.LOCAL);
      invocation.setEnterpriseContext(ctx);
      invocation.setId(ctx.getId());
      invocation.setTransaction(ctx.getTransaction());
      //invocation.setPrincipal(SecurityAssociation.getPrincipal());
      //invocation.setCredential(SecurityAssociation.getCredential());
      invokeEntityInterceptor(invocation);
   }

   public void passivateEntity(EntityEnterpriseContext ctx) throws Exception
   {
      Invocation invocation = new Invocation();
      invocation.setValue(
            EntityInvocationKey.TYPE,
            EntityInvocationType.PASSIVATE,
            PayloadKey.TRANSIENT);
      invocation.setValue(Entrancy.ENTRANCY_KEY,
            Entrancy.NON_ENTRANT, PayloadKey.AS_IS);
      invocation.setType(InvocationType.LOCAL);
      invocation.setEnterpriseContext(ctx);
      invocation.setId(ctx.getId());
      invocation.setTransaction(ctx.getTransaction());
      //invocation.setPrincipal(SecurityAssociation.getPrincipal());
      //invocation.setCredential(SecurityAssociation.getCredential());
      invokeEntityInterceptor(invocation);
   }

   public void handleEjbTimeout( Timer pTimer ) {
      EntityCache cache = (EntityCache) getInstanceCache();
//AS      EntityContext lContext = (EntityContext) ( (ContainerTimer) pTimer ).getContext();
      Object cacheKey = cache.createCacheKey( ( (ContainerTimer) pTimer ).getKey() );

      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( getClassLoader() );

      try
      {
         Invocation invocation = new Invocation(
            cacheKey,
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
         invocation.setValue(
            InvocationKey.CALLBACK_METHOD,
            beanClass.getMethod( "ejbTimeout", new Class[] { Timer.class } ),
            PayloadKey.TRANSIENT
         );
         invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS,
            invocation.getArguments(),
            PayloadKey.TRANSIENT
         );

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

   private InvocationResponse invokeEntityInterceptor(Invocation invocation)
         throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      try
      {
         return rootEntityInterceptor.invoke(invocation);
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public void retrieveStatistics( List container, boolean reset ) {
      // Loop through all Interceptors and add statistics
      getInterceptor().retrieveStatistics( container, reset );
      if( !( getInstancePool() instanceof Interceptor ) ) {
         getInstancePool().retrieveStatistics( container, reset );
      }
      log.info( "retrieveStatistics(), ended with container: " + container );
   }

   protected void setupMarshalledInvocationMapping() throws Exception
   {
      if(homeInterface != null)
      {
         Method[] methods = homeInterface.getMethods();
         for(int i = 0; i < methods.length; i++)
         {
            marshalledInvocationMapping.put(
                  new Long(MethodHashing.calculateHash(methods[i])),
                  methods[i]);
         }
      }

      if(remoteInterface != null)
      {
         Method[] methods = remoteInterface.getMethods();
         for(int i = 0; i < methods.length; i++)
         {
            marshalledInvocationMapping.put(
                  new Long(MethodHashing.calculateHash(methods[i])),
                  methods[i]);
         }
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
    * <li>'getCacheSize' -> return the entity's cache size</li>
    * <li>'flushCache' -> flush the entity's cache</li>
    * </ul>
    */
   public MBeanInfo getMBeanInfo()
   {
      MBeanParameterInfo miInfo = new MBeanParameterInfo("method", Invocation.class.getName(), "Invocation data");
      MBeanConstructorInfo[] ctorInfo = null;

      MBeanParameterInfo[] miInfoParams = new MBeanParameterInfo[] {new MBeanParameterInfo("method", Invocation.class.getName(), "Invocation data")};
      MBeanParameterInfo[] noParams = new MBeanParameterInfo[] {};

      MBeanInfo superInfo = super.getMBeanInfo();
      int superOpInfoCount = superInfo.getOperations().length;
      MBeanOperationInfo[] opInfo = new MBeanOperationInfo[superOpInfoCount +2];
      System.arraycopy(superInfo.getOperations(), 0, opInfo, 0, superOpInfoCount);
      opInfo[superOpInfoCount] =
         new MBeanOperationInfo("getCacheSize", "Get the Container cache size.",
                                noParams,
                                "java.lang.Integer", MBeanOperationInfo.INFO);
      opInfo[superOpInfoCount + 1] =
         new MBeanOperationInfo("flushCache", "Flush the Container cache.",
            noParams,
            "void", MBeanOperationInfo.ACTION);
      return new MBeanInfo(getClass().getName(),
                           "EJB Entity Container MBean",
                           superInfo.getAttributes(),
                           superInfo.getConstructors(),
                           opInfo,
                           superInfo.getNotifications());
   }

   /**
    * Handle a operation invocation.
    */
   public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {
      if( params != null && params.length == 1 && (params[0] instanceof Invocation) == false )
         throw new MBeanException(new IllegalArgumentException("Expected zero or single Invocation argument"));

      Object value = null;
      Invocation invocation = null;
      if( params != null && params.length == 1 )
         invocation = (Invocation) params[0];

      // marcf: FIXME: these should be exposed on the cache

      // Check against home, remote, localHome, local, getHome, getRemote, getLocalHome, getLocal
      if (actionName.equals("getCacheSize")) {
         return new Integer(((EntityCache)getInstanceCache()).getCacheSize());
      }
      else if (actionName.equals("flushCache"))
      {
         log.info("flushing cache");
         ((EntityCache)getInstanceCache()).flush();
         return null;

      }
      else
      {
         return super.invoke(actionName, params, signature);
      }
   }

   Interceptor createContainerInterceptor()
   {
      return null;
   }

   /**
    * Describe <code>typeSpecificInitialize</code> method here.
    * entity specific initialization.
    */
   protected void typeSpecificInitialize()  throws Exception
   {
      ClassLoader cl = getDeploymentInfo().ucl;
      ClassLoader localCl = getDeploymentInfo().localCl;
      int transType = CMT;

      genericInitialize(transType, cl, localCl );
      if (getBeanMetaData().getHome() != null)
      {
         createProxyFactories(cl);
      }
      ConfigurationMetaData conf = getBeanMetaData().getContainerConfiguration();
      setInstanceCache( createInstanceCache( conf, false, cl ) );
      setInstancePool( createInstancePool( conf, cl ) );
      //Set the bean Lock Manager
      setLockManager(createBeanLockManager(((EntityMetaData)getBeanMetaData()).isReentrant(),conf.getLockConfig(), cl));

   }
}
