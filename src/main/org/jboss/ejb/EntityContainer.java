/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.ClassLoader;
import javax.ejb.Timer;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import org.jboss.ejb.timer.ContainerTimer;
import org.jboss.ejb.entity.EntityInvocationRegistry;
import org.jboss.ejb.entity.EntityPersistenceManager;
import org.jboss.ejb.entity.EntityPersistenceManagerXMLFactory;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.monitor.StatisticsProvider;
import org.jboss.security.SecurityAssociation;

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
 * @version $Revision: 1.99 $
 */
public class EntityContainer
   extends Container implements
   InstancePoolContainer, StatisticsProvider
{
   /**
    * The persistence manager for this entity.
    * @todo replace this with a link to an MBean service.
    */
   private EntityPersistenceManager entityPersistenceManager;

   /**
    * The primary key class of this entity.
    */
   private Class primaryKeyClass;

   /**
    * Commit Option D cache invalidation thread.
    */
   private OptionDInvalidator optionDInvalidator;

   // These members contains statistics variable
   //private long createCount = 0;
   //private long removeCount = 0;

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

   /**
    * Gets the persistence manager for this entity.
    */
   public EntityPersistenceManager getPersistenceManager()
   {
      return entityPersistenceManager;
   }

   /**
    * Sets the persistence manager for this entity.
    * @param entityPersistenceManager the new persistence manager for this
    * entity; must not be null
    */
   public void setPersistenceManager(EntityPersistenceManager entityPersistenceManager)
   {
      if(entityPersistenceManager == null)
      {
         throw new IllegalArgumentException("entityPersistenceManager is null");
      }
      this.entityPersistenceManager = entityPersistenceManager;
   }

   /**
    * Describe <code>typeSpecificInitialize</code> method here.
    * entity specific initialization.
    */
   protected void typeSpecificCreate()  throws Exception
   {
      // Make some additional validity checks with regards to the container configuration
      checkCoherency ();

      ConfigurationMetaData conf = getBeanMetaData().getContainerConfiguration();
      setInstanceCache( createInstanceCache( conf, false, getClassLoader() ) );
      setInstancePool( createInstancePool( conf, getClassLoader() ) );
      //Set the bean Lock Manager
      setLockManager(createBeanLockManager(((EntityMetaData)getBeanMetaData()).isReentrant(),conf.getLockConfig(), getClassLoader()));

      EntityMetaData entityMetaData = (EntityMetaData)metaData;
      if(entityMetaData.getPrimaryKeyClass() != null)
      {
         primaryKeyClass = classLoader.loadClass(entityMetaData.getPrimaryKeyClass());
      }

      // Persistence Manager
      EntityPersistenceManagerXMLFactory factory =  new EntityPersistenceManagerXMLFactory();
      entityPersistenceManager = factory.create(this, conf.getPersistenceManagerElement());
      entityPersistenceManager.setContainer(this);
      entityPersistenceManager.create();
      readOnly = ((EntityMetaData)metaData).isReadOnly();
      // Init instance cache
      getInstanceCache().create();
   }

   /*
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

         // Initialize pool
         getInstancePool().create();

         for(Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory proxyFactory =
                  (EJBProxyFactory) proxyFactories.get(invokerBinding);
            proxyFactory.create();
         }

      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   */
   /*
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
            EJBProxyFactory proxyFactory =
                  (EJBProxyFactory)proxyFactories.get(invokerBinding);
            proxyFactory.start();
         }

         // Start the persistence manager
         getPersistenceManager().start();

         // Start instance cache
         getInstanceCache().start();

         // Start the instance pool
         getInstancePool().start();

         // Creat and start the OptionDInvalidator if necessary
         ConfigurationMetaData configuration = getBeanMetaData().getContainerConfiguration();
         if(configuration.getCommitOption() == ConfigurationMetaData.D_COMMIT_OPTION)
         {
            optionDInvalidator = new OptionDInvalidator(
                  configuration.getOptionDRefreshRate());
            optionDInvalidator.start();
         }
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
*/
   protected void typeSpecificStart() throws Exception
   {
         // Start the persistence manager
         getPersistenceManager().start();

         // Start instance cache
         getInstanceCache().start();

         // Creat and start the OptionDInvalidator if necessary
         ConfigurationMetaData configuration = getBeanMetaData().getContainerConfiguration();
         if(configuration.getCommitOption() == ConfigurationMetaData.D_COMMIT_OPTION)
         {
            optionDInvalidator = new OptionDInvalidator(
                  configuration.getOptionDRefreshRate());
            optionDInvalidator.start();
         }
   }

   protected void typeSpecificStop() throws Exception
   {
         if(optionDInvalidator != null)
         {
            optionDInvalidator.die();
            optionDInvalidator = null;
         }
         // Stop instance cache
         getInstanceCache().stop();

         // Start the persistence manager
         getPersistenceManager().stop();
   }
   /*
   protected void stopService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         if(optionDInvalidator != null)
         {
            optionDInvalidator.die();
            optionDInvalidator = null;
         }

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
            EJBProxyFactory proxyFactory =
                  (EJBProxyFactory)proxyFactories.get(invokerBinding);
            proxyFactory.stop();
         }

         // Start the persistence manager
         getPersistenceManager().stop();

         // Call default stop
         super.stopService();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
*/
   protected void typeSpecificDestroy() throws Exception
   {
         // Destroy instance cache
         getInstanceCache().destroy();
         getInstanceCache().setContainer(null);
         // Destroy the persistence manager
         getPersistenceManager().destroy();
         getPersistenceManager().setContainer(null);
   }
   /*
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
            EJBProxyFactory proxyFactory =
                  (EJBProxyFactory)proxyFactories.get(invokerBinding);
            proxyFactory.destroy();
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

         // Start the persistence manager
         getPersistenceManager().destroy();
         getPersistenceManager().setContainer(null);

         // Call default destroy
         super.destroyService();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
*/
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
      return entityPersistenceManager.createEntityInstance();
   }

   /**
    * Stores the entity in the persistence manager.
    *
    * @todo This call sould be completely eliminated.  Only the persistence
    * manager should determine when a store is called.  This means that the
    * persistence manager will be listening for transaction demarcation events
    * and invocation demarcation events. The concept of an invocation
    * demarcation event does not currently exist in JBoss, but it will provide
    * simmilar events to the transaction events and will be used in place of
    * the transaction events when you are not running in a transaction.
    */
   public void storeEntity(EntityEnterpriseContext ctx) throws Exception
   {
      entityPersistenceManager.storeEntity(ctx);
   }

   /**
    * Has this been been modifed during the current transaction or invocation
    * in the event you are running without a transaction.
    *
    * @todo replace this method with calls directly to the persistence manager
    * service when we make the persistence manager a service.
    */
   public boolean isModified(EntityEnterpriseContext ctx) throws Exception
   {
      return entityPersistenceManager.isEntityModified(ctx);
   }

   /**
    * Callback notification from the instance pool/ cache that this entity is
    * about to be activated.
    *
    * @todo replace this method with calls directly to the persistence manager
    * service when we make the persistence manager a service.
    */
   public void activateEntity(EntityEnterpriseContext ctx) throws Exception
   {
      entityPersistenceManager.activateEntity(ctx);
   }

   /**
    * Callback notification from the instance pool/ cache that this entity is
    * about to be passivated.
    *
    * @todo replace this method with calls directly to the persistence manager
    * service when we make the persistence manager a service.
    */
   public void passivateEntity(EntityEnterpriseContext ctx) throws Exception
   {
      entityPersistenceManager.passivateEntity(ctx);
   }

   public void handleEjbTimeout( Timer pTimer ) {
//AS      EntityContext lContext = (EntityContext) ( (ContainerTimer) pTimer ).getContext();
      Object id = ((ContainerTimer)pTimer).getKey();

      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( getClassLoader() );

      try
      {
         Invocation invocation = new Invocation(
            id,
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


   /*
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
   */
   /**
    * Build the container MBean information on attributes, contstructors,
    * operations, and notifications. Currently there are no attributes, no
    * constructors, no notifications, and the following ops:
    * <ul>
    * <li>'invoke' -&gt; invoke(Invocation);</li>
    * <li>'getHome' -&gt; return EBJHome interface;</li>
    * <li>'getRemote' -&gt; return EJBObject interface</li>
    * <li>'getCacheSize' -&gt; return the entity's cache size</li>
    * <li>'flushCache' -&gt; flush the entity's cache</li>
    * </ul>
    */
   public MBeanInfo getMBeanInfo()
   {
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
      if(params != null && params.length == 1 &&
            !(params[0] instanceof Invocation))
      {
         throw new MBeanException(new IllegalArgumentException(
                  "Expected zero or single Invocation argument"));
      }

      // marcf: FIXME: these should be exposed on the cache

      // Check against home, remote, localHome, local, getHome, getRemote, getLocalHome, getLocal
      if (actionName.equals("getCacheSize")) {
         return new Integer(((EntityCache)getInstanceCache()).getCacheSize());
      }
      else if(actionName.equals("flushCache"))
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

   protected void checkCoherency () throws Exception
   {
      // Check clustering cohrency wrt metadata
      //
      if (metaData.isClustered())
      {
         boolean clusteredProxyFactoryFound = false;
         for (java.util.Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            if (ci instanceof org.jboss.proxy.ejb.ClusterProxyFactory)
               clusteredProxyFactoryFound = true;
         }

         if (!clusteredProxyFactoryFound)
         {
            log.warn("*** EJB '" + this.metaData.getEjbName() + "' deployed as CLUSTERED but not a single clustered-invoker is bound to container ***");
         }
      }
   }

   private class OptionDInvalidator extends Thread
   {
      private long refreshRate;
      private volatile boolean shouldRun = true;

      public OptionDInvalidator(long refreshRate)
      {
         super("Option D Invalidator");
         this.refreshRate = refreshRate;
      }

      public void die()
      {
         shouldRun = false;
         interrupt();
      }

      public void run()
      {
         while(shouldRun)
         {
            try
            {
               sleep(refreshRate);
            }
            catch (InterruptedException  e)
            {
               interrupted();
            }

            if(shouldRun)
            {
               if(log.isTraceEnabled())
               {
                  log.trace("Flushing the valid contexts");
               }
               ((EntityCache)getInstanceCache()).flush();
            }
         }
      }
   }
}
