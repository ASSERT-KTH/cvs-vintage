/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.Handle;
import javax.management.ObjectName;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.metadata.SessionMetaData;

/**
 * <p>
 * Container dedicated to session beans. Contains factored out
 * redundancies between stateless and stateful treatments, because
 * (extending the spec) we would like to also support stateful
 * web services.
 * </p>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.2 $
 * @since 30.10.2003
 */

public abstract class SessionContainer extends Container {
   /**
    * These are the mappings between the home interface methods and the
    * container methods.
    */
   protected Map homeMapping;

   /**
    * These are the mappings between the remote interface methods and the
    * bean methods.
    */
   protected Map beanMapping;

   /**
    * This is the first interceptor in the chain. The last interceptor must
    * be provided by the container itself
    */
   protected Interceptor interceptor;

   /** this is the service endpoint class */
   protected Class serviceEndpoint;

   /** This is the instancepool that is to be used */
   protected InstancePool instancePool;

   /** set the instance pool */
   public void setInstancePool(InstancePool ip) {
      if (ip == null)
         throw new IllegalArgumentException("Null pool");

      this.instancePool = ip;
      ip.setContainer(this);
   }

   /** return instance pool */
   public InstancePool getInstancePool() {
      return instancePool;
   }

   /** return local proxy factory */
   public LocalProxyFactory getLocalProxyFactory() {
      return localProxyFactory;
   }

   /** add an additional interceptor to the chain */
   public void addInterceptor(Interceptor in) {
      if (interceptor == null) {
         interceptor = in;
      } else {
         Interceptor current = interceptor;
         while (current.getNext() != null) {
            current = current.getNext();
         }

         current.setNext(in);
      }
   }

   /** return first interceptor */
   public Interceptor getInterceptor() {
      return interceptor;
   }

   /** return home class */
   public Class getHomeClass() {
      return homeInterface;
   }

   /** return remote class */
   public Class getRemoteClass() {
      return remoteInterface;
   }

   /** return service endpoint */
   public Class getServiceEndpoint() {
      return serviceEndpoint;
   }

   // Container stuff

   protected void createService() throws Exception {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try {
         // Acquire classes from CL
         if (metaData.getHome() != null)
            homeInterface = classLoader.loadClass(metaData.getHome());
         if (metaData.getRemote() != null)
            remoteInterface = classLoader.loadClass(metaData.getRemote());
         if (((SessionMetaData) metaData).getServiceEndpoint() != null) {
            serviceEndpoint =
               classLoader.loadClass(
                  ((SessionMetaData) metaData).getServiceEndpoint());
         }

         // Call default init
         super.createService();

         // Make some additional validity checks with regards to the container configuration
         checkCoherency();

         // Map the bean methods
         setupBeanMapping();

         // Map the home methods
         setupHomeMapping();

         // Map the interfaces to Long
         setupMarshalledInvocationMapping();

         createInvokers();

         createInstanceCache();

         createInstancePool();

         createPersistenceManager();

         createInterceptors();
      } finally {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * how home methods are treated by container
    */
   protected abstract void setupHomeMapping() throws Exception;

   /** loop through methods and setup mapping */
   protected void setUpBeanMappingImpl(
      Map map,
      Method[] m,
      String declaringClass)
      throws NoSuchMethodException {
      boolean debug = log.isDebugEnabled();

      for (int i = 0; i < m.length; i++) {
         if (!m[i].getDeclaringClass().getName().equals(declaringClass)) {
            // Implemented by bean
            try {
               map.put(
                  m[i],
                  beanClass.getMethod(
                     m[i].getName(),
                     m[i].getParameterTypes()));
            } catch (NoSuchMethodException ex) {
               throw new org.jboss.util.NoSuchMethodException(
                  "Not found in bean class: ",
                  m[i]);
            }

            if (debug)
               log.debug(
                  "Mapped "
                     + m[i].getName()
                     + " HASH "
                     + m[i].hashCode()
                     + "to "
                     + map.get(m[i]));
         } else {
            try {
               // Implemented by container
               if (debug)
                  log.debug(
                     "Mapped Container method "
                        + m[i].getName()
                        + " HASH "
                        + m[i].hashCode());
               map.put(
                  m[i],
                  getClass().getMethod(
                     m[i].getName(),
                     new Class[] { Invocation.class }));
            } catch (NoSuchMethodException e) {
               log.error(m[i].getName() + " in bean has not been mapped", e);
            }
         }
      }
   }

   /** build bean mappings for application logic */
   protected void setupBeanMapping() throws NoSuchMethodException {
      Map map = new HashMap();

      if (remoteInterface != null) {
         Method[] m = remoteInterface.getMethods();
         setUpBeanMappingImpl(map, m, "javax.ejb.EJBObject");
      }
      if (localInterface != null) {
         Method[] m = localInterface.getMethods();
         setUpBeanMappingImpl(map, m, "javax.ejb.EJBLocalObject");
      }
      if (serviceEndpoint != null) {
         Method[] m = serviceEndpoint.getMethods();
         setUpBeanMappingImpl(map, m, "java.rmi.Remote");
      }
      beanMapping = map;
   }

   protected void setupMarshalledInvocationMapping() throws Exception {
      // Create method mappings for container invoker
      if (homeInterface != null) {
         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++) {
            marshalledInvocationMapping.put(
               new Long(MarshalledInvocation.calculateHash(m[i])),
               m[i]);
         }
      }

      if (remoteInterface != null) {
         Method[] m = remoteInterface.getMethods();
         for (int j = 0; j < m.length; j++) {
            marshalledInvocationMapping.put(
               new Long(MarshalledInvocation.calculateHash(m[j])),
               m[j]);
         }
      }
      // Get the getEJBObjectMethod
      Method getEJBObjectMethod =
         Class.forName("javax.ejb.Handle").getMethod(
            "getEJBObject",
            new Class[0]);

      // Hash it
      marshalledInvocationMapping.put(
         new Long(MarshalledInvocation.calculateHash(getEJBObjectMethod)),
         getEJBObjectMethod);
   }

   protected void checkCoherency() throws Exception {
      // Check clustering cohrency wrt metadata
      //
      if (metaData.isClustered()) {
         boolean clusteredProxyFactoryFound = false;
         for (Iterator it = proxyFactories.keySet().iterator();
            it.hasNext();
            ) {
            String invokerBinding = (String) it.next();
            EJBProxyFactory ci =
               (EJBProxyFactory) proxyFactories.get(invokerBinding);
            if (ci instanceof org.jboss.proxy.ejb.ClusterProxyFactory)
               clusteredProxyFactoryFound = true;
         }

         if (!clusteredProxyFactoryFound) {
            log.warn(
               "*** EJB '"
                  + this.metaData.getEjbName()
                  + "' deployed as CLUSTERED but not a single clustered-invoker is bound to container ***");
         }
      }
   }

   /** creates a new instance pool */
   protected void createInstancePool() throws Exception {
      // Initialize pool
      instancePool.create();

      // Try to register the instance pool as an MBean
      try {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "pool");
         ObjectName poolName = new ObjectName(containerName.getDomain(), props);
         server.registerMBean(instancePool, poolName);
      } catch (Throwable t) {
         log.debug("Failed to register pool as mbean", t);
      }
   }

   /**
    * no instance cache per default
    */
   protected void createInstanceCache() throws Exception {
   }

   /** creates the invokers */
   protected void createInvokers() throws Exception {
      // Init container invoker
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();) {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci =
            (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.create();
      }
   }

   /** Initialize the interceptors by calling the chain */
   protected void createInterceptors() throws Exception {
      Interceptor in = interceptor;
      while (in != null) {
         in.setContainer(this);
         in.create();
         in = in.getNext();
      }
   }

   /**
    * no persistence manager per default
    */
   protected void createPersistenceManager() throws Exception {
   }

   protected void startService() throws Exception {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try {
         // Call default start
         super.startService();

         startInvokers();

         startInstanceCache();

         startInstancePool();

         startPersistenceManager();

         startInterceptors();
      } finally {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * no persistence manager per default
    */
   protected void startPersistenceManager() throws Exception {
   }

   /**
    * no instance cache per default
    */
   protected void startInstanceCache() throws Exception {
   }

   /** Start container invokers */
   protected void startInvokers() throws Exception {
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();) {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci =
            (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.start();
      }
   }

   /** Start pool */
   protected void startInstancePool() throws Exception {
      instancePool.start();
   }

   /** Start all interceptors in the chain **/
   protected void startInterceptors() throws Exception {
      Interceptor in = interceptor;
      while (in != null) {
         in.start();
         in = in.getNext();
      }
   }

   protected void stopService() throws Exception {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try {
         // Call default stop
         super.stopService();

         stopInvokers();

         stopInstanceCache();

         stopInstancePool();

         stopPersistenceManager();

         stopInterceptors();
      } finally {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /** Stop all interceptors in the chain */
   protected void stopInterceptors() {
      Interceptor in = interceptor;
      while (in != null) {
         in.stop();
         in = in.getNext();
      }
   }

   /** no persistence */
   protected void stopPersistenceManager() {
   }

   /** Stop pool */
   protected void stopInstancePool() {
      instancePool.stop();
   }

   /** no instance cache */
   protected void stopInstanceCache() {
   }

   /** Stop container invoker */
   protected void stopInvokers() {
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();) {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci =
            (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.stop();
      }
   }

   protected void destroyService() throws Exception {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try {
         destroyInvokers();

         destroyInstanceCache();

         destroyInstancePool();

         destroyPersistenceManager();

         destroyInterceptors();

         destroyMarshalledInvocationMapping();

         // Call default destroy
         super.destroyService();
      } finally {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void destroyMarshalledInvocationMapping() {
      MarshalledInvocation.removeHashes(homeInterface);
      MarshalledInvocation.removeHashes(remoteInterface);
   }

   protected void destroyInterceptors() {
      // Destroy all the interceptors in the chain
      Interceptor in = interceptor;
      while (in != null) {
         in.destroy();
         in.setContainer(null);
         in = in.getNext();
      }
   }

   protected void destroyPersistenceManager() {
   }

   protected void destroyInstancePool() {
      // Destroy pool
      instancePool.destroy();
      instancePool.setContainer(null);
      try {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "pool");
         ObjectName poolName = new ObjectName(containerName.getDomain(), props);
         server.unregisterMBean(poolName);
      } catch (Throwable ignore) {
      }
   }

   protected void destroyInstanceCache() {
   }

   protected void destroyInvokers() {
      // Destroy container invoker
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();) {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci =
            (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.destroy();
         ci.setContainer(null);
      }
   }

   public Object internalInvokeHome(Invocation mi) throws Exception {
      return getInterceptor().invokeHome(mi);
   }

   /**
   * This method does invocation interpositioning of tx and security,
   * retrieves the instance from an object table, and invokes the method
   * on the particular instance
   */
   public Object internalInvoke(Invocation mi) throws Exception {
      // Invoke through interceptors
      return getInterceptor().invoke(mi);
   }

   // EJBObject implementation --------------------------------------

   /**
    * While the following methods are implemented in the client in the case
    * of JRMP we would need to implement them to fully support other transport
    * protocols
    *
    * @return  Always null
    */
   public Handle getHandle(Invocation mi) throws RemoteException {
      // TODO
      return null;
   }

   /**
    * @return  Always null
    */
   public Object getPrimaryKey(Invocation mi) throws RemoteException {
      // TODO
      return null;
   }

   public EJBHome getEJBHome(Invocation mi) throws RemoteException {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null) {
         String msg =
            "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      return (EJBHome) ci.getEJBHome();
   }

   /**
    * @return   Always false
    */
   public boolean isIdentical(Invocation mi) throws RemoteException {
      return false; // TODO
   }

   // Home interface implementation ---------------------------------

   // local object interface implementation

   public EJBLocalHome getEJBLocalHome(Invocation mi) {
      return localProxyFactory.getEJBLocalHome();
   }

   /**
    * needed for sub-inner-class access (old jdk compiler bug) 
    * @return
    */
   protected Map getHomeMapping() {
      return homeMapping;
   }

   /**
    * needed for sub-inner-class access (old jdk compiler bug) 
    * @return
    */
   protected Map getBeanMapping() {
      return beanMapping;
   }

}
