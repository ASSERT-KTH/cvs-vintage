/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collection;
import java.util.ArrayList;
import java.rmi.RemoteException;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

/**
 * The container for <em>stateful</em> session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.36 $
 *
 * <p><b>Revisions</b>
 * <p><b>20010704</b>
 * <ul>
 * <li>Throw an exception when removing a bean in transaction (in remove)?
 *     (I dissagree) (marcf: who is the person writing this comment? please sign)
 * </ul>
 */
public class StatefulSessionContainer
extends Container
implements ContainerInvokerContainer, InstancePoolContainer
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /** This is the Home interface class */
   protected Class homeInterface;
   
   /** This is the Remote interface class */
   protected Class remoteInterface;
   
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
   
   /** This is the container invoker for this container */
   protected ContainerInvoker containerInvoker;
   
   /**
    * This is the first interceptor in the chain. The last interceptor must
    * be provided by the container itself.
    */
   protected Interceptor interceptor;
   
   /** This is the instancepool that is to be used */
   protected InstancePool instancePool;
   
   /** This is the persistence manager for this container */
   protected StatefulSessionPersistenceManager persistenceManager;
   
   /** The instance cache. */
   protected InstanceCache instanceCache;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   public void setContainerInvoker(ContainerInvoker ci)
   {
      if (ci == null)
         throw new IllegalArgumentException("Null invoker");
      
      this.containerInvoker = ci;
      ci.setContainer(this);
   }
   
   public ContainerInvoker getContainerInvoker()
   {
      return containerInvoker;
   }
   
   public LocalContainerInvoker getLocalContainerInvoker()
   {
      return localContainerInvoker;
   }
   
   public void setInstanceCache(InstanceCache ic)
   {
      this.instanceCache = ic;
      ic.setContainer(this);
   }
   
   public InstanceCache getInstanceCache()
   {
      return instanceCache;
   }
   
   public void setInstancePool(InstancePool ip)
   {
      if (ip == null)
         throw new IllegalArgumentException("Null pool");
      
      this.instancePool = ip;
      ip.setContainer(this);
   }
   
   public InstancePool getInstancePool()
   {
      return instancePool;
   }
   
   public StatefulSessionPersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }
   
   public void setPersistenceManager(StatefulSessionPersistenceManager pm)
   {
      persistenceManager = pm;
      pm.setContainer(this);
   }
   
   public void addInterceptor(Interceptor in)
   {
      if (interceptor == null)
      {
         interceptor = in;
      } else
      {
         
         Interceptor current = interceptor;
         while ( current.getNext() != null)
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
   
   public Class getHomeClass()
   {
      return homeInterface;
   }
   
   public Class getRemoteClass()
   {
      return remoteInterface;
   }
   
   // Container implementation --------------------------------------
   /*
   public void init() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Acquire classes from CL
      if (metaData.getHome() != null)
         homeInterface = classLoader.loadClass(metaData.getHome());
      if (metaData.getRemote() != null)
         remoteInterface = classLoader.loadClass(metaData.getRemote());
      
      // Call default init
      super.init();
      
      // Map the bean methods
      setupBeanMapping();
      
      // Map the home methods
      setupHomeMapping();
      
      // Init container invoker
      if (containerInvoker != null)
         containerInvoker.init();
      
      // Init instance cache
      instanceCache.init();
      
      // Initialize pool
      instancePool.init();
      
      // Init persistence
      persistenceManager.init();
      
      // Initialize the interceptor by calling the chain
      Interceptor in = interceptor;
      while (in != null)
      {
         in.setContainer(this);
         in.init();
         in = in.getNext();
      }
      
      // Reset classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   */
   //remove the init calls another day.
   public void start() throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Acquire classes from CL
      if (metaData.getHome() != null)
         homeInterface = classLoader.loadClass(metaData.getHome());
      if (metaData.getRemote() != null)
         remoteInterface = classLoader.loadClass(metaData.getRemote());
      
      // Call default init
      super.start();
      
      // Map the bean methods
      setupBeanMapping();
      
      // Map the home methods
      setupHomeMapping();
      
      // Init container invoker
      if (containerInvoker != null)
         containerInvoker.init();
      
      // Init instance cache
      instanceCache.init();
      
      // Initialize pool
      instancePool.init();
      
      // Init persistence
      persistenceManager.init();
      
      // Initialize the interceptor by calling the chain
      for (Interceptor in = interceptor; in != null; in = in.getNext()) 
      {
         in.setContainer(this);
         in.init();
      }
      
      // Reset classloader
      //Thread.currentThread().setContextClassLoader(oldCl);
      // Associate thread with classloader
      // ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      //Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Call default start
      //super.start();
      
      // Start container invoker
      if (containerInvoker != null)
         containerInvoker.start();
      
      // Start instance cache
      instanceCache.start();
      
      // Start pool
      instancePool.start();
      
      // Start persistence
      persistenceManager.start();
      
      // Start all interceptors in the chain
      for (Interceptor in = interceptor; in != null; in = in.getNext()) 
      {
         in.start();
      }
      
      // Reset classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void stop()
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Call default stop
      super.stop();
      
      // Stop container invoker
      if (containerInvoker != null)
         containerInvoker.stop();
      
      // Stop instance cache
      instanceCache.stop();
      
      // Stop pool
      instancePool.stop();
      
      // Stop persistence
      persistenceManager.stop();
      
      // Stop the instance pool
      instancePool.stop();
      
      // Stop all interceptors in the chain
      for (Interceptor in = interceptor; in != null; in = in.getNext()) 
      {
         in.stop();
      }
      
      // Reset classloader
      //Thread.currentThread().setContextClassLoader(oldCl);
      // Associate thread with classloader
      //ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      //Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Call default destroy
      //super.destroy();
      
      // Destroy container invoker
      if (containerInvoker != null)
         containerInvoker.destroy();
      
      // Destroy instance cache
      instanceCache.destroy();
      
      // Destroy pool
      instancePool.destroy();
      
      // Destroy persistence
      persistenceManager.destroy();
      
      // Destroy all the interceptors in the chain
      for (Interceptor in = interceptor; in != null; in = in.getNext()) 
      {
         in.destroy();
      }
      
      // Reset classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   /*
   public void destroy()
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Call default destroy
      super.destroy();
      
      // Destroy container invoker
      if (containerInvoker != null)
         containerInvoker.destroy();
      
      // Destroy instance cache
      instanceCache.destroy();
      
      // Destroy pool
      instancePool.destroy();
      
      // Destroy persistence
      persistenceManager.destroy();
      
      // Destroy all the interceptors in the chain
      Interceptor in = interceptor;
      while (in != null)
      {
         in.destroy();
         in = in.getNext();
      }
      
      // Reset classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   */
   public Object invokeHome(MethodInvocation mi)
   throws Exception
   {
      return getInterceptor().invokeHome(mi);
   }
   
   /**
    * This method retrieves the instance from an object table, and invokes
    * the method on the particular instance through the chain of interceptors.
    */
   public Object invoke(MethodInvocation mi)
   throws Exception
   {
      // Invoke through interceptors
      return getInterceptor().invoke(mi);
   }
   
   // EJBObject implementation --------------------------------------
   
   public void remove(MethodInvocation mi)
   throws RemoteException, RemoveException
   {
      // 7.6 EJB2.0, it is illegal to remove a bean while in a transaction
      // if (mi.getEnterpriseContext().getTransaction() != null)
      // throw new RemoveException("StatefulSession bean in transaction, cannot remove (EJB2.0 7.6)");
      
      // Remove from storage
      getPersistenceManager().removeSession((StatefulSessionEnterpriseContext)mi.getEnterpriseContext());
      
      // We signify "removed" with a null id
      mi.getEnterpriseContext().setId(null);
   }
   
   /**
    * While the following methods are implemented in the client in the case
    * of JRMP we would need to implement them to fully support other transport
    * protocols
    *
    * @return  Always null
    */
   public Handle getHandle(MethodInvocation mi) throws RemoteException
   {
      // TODO
      return null;
   }
   
   /**
    * @return  Always null
    */
   public Object getPrimaryKey(MethodInvocation mi) throws RemoteException
   {
      // TODO
      return null;
   }
   
   public EJBHome getEJBHome(MethodInvocation mi) throws RemoteException
   {
      if (containerInvoker == null)
         throw new java.lang.IllegalStateException();
      return containerInvoker.getEJBHome();
   }
   
   /**
    * @return   Always false
    */
   public boolean isIdentical(MethodInvocation mi) throws RemoteException
   {
      return false; // TODO
   }
   
   // Home interface implementation ---------------------------------
   
   public EJBObject createHome(MethodInvocation mi)
   throws Exception
   {
      getPersistenceManager().createSession(mi.getMethod(), mi.getArguments(), (StatefulSessionEnterpriseContext)mi.getEnterpriseContext());
      return ((StatefulSessionEnterpriseContext)mi.getEnterpriseContext()).getEJBObject();
   }
   
   // local object interface implementation
   
   public EJBLocalHome getEJBLocalHome(MethodInvocation mi)
   {
      return localContainerInvoker.getEJBLocalHome();
   }
   
   // local home interface implementation
   
   /**
    * @throws Error    Not yet implemented
    */
   public void removeLocalHome(MethodInvocation mi)
   throws RemoteException, RemoveException
   {
      throw new Error("Not Yet Implemented");
   }
   
   public EJBLocalObject createLocalHome(MethodInvocation mi)
   throws Exception
   {
      getPersistenceManager().createSession(mi.getMethod(), mi.getArguments(), (StatefulSessionEnterpriseContext)mi.getEnterpriseContext());
      return ((StatefulSessionEnterpriseContext)mi.getEnterpriseContext()).getEJBLocalObject();
   }
   
   /**
    * A method for the getEJBObject from the handle
    *
    */
   public EJBObject getEJBObject(MethodInvocation mi) throws RemoteException
   {
      // All we need is an EJBObject for this Id, the first argument is the Id
      if (containerInvoker == null)
         throw new IllegalStateException();
      
      return containerInvoker.getStatefulSessionEJBObject(mi.getArguments()[0]);
   }
   
   
   // EJBHome implementation ----------------------------------------
   
   //
   // These are implemented in the local proxy
   //
   
   /**
    * @throws Error    Not yet implemented
    */
   public void removeHome(MethodInvocation mi)
   throws RemoteException, RemoveException
   {
      throw new Error("Not Yet Implemented");
   }
   
   public EJBMetaData getEJBMetaDataHome(MethodInvocation mi)
   throws RemoteException
   {
      if (containerInvoker == null)
         throw new IllegalStateException();
      
      return getContainerInvoker().getEJBMetaData();
   }
   
   /**
    * @throws Error    Not yet implemented
    */
   public HomeHandle getHomeHandleHome(MethodInvocation mi)
   throws RemoteException
   {
      throw new Error("Not Yet Implemented");
   }
   
   
   // Private -------------------------------------------------------
   
   protected void setupHomeMapping()
   throws NoSuchMethodException
   {
      Map map = new HashMap();
      
      if (homeInterface != null)
      {
         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            try
            {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName()+"Home", new Class[]
               { MethodInvocation.class }));
            } catch (NoSuchMethodException e)
            {
               log.info(m[i].getName() + " in bean has not been mapped");
            }
         }
      }
      
      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            try
            {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName()+"LocalHome", new Class[]
               { MethodInvocation.class }));
            } catch (NoSuchMethodException e)
            {
               log.info(m[i].getName() + " in bean has not been mapped");
            }
         }
      }
      
      try
      {
         
         // Get getEJBObject from on Handle, first get the class
         Class handleClass = Class.forName("javax.ejb.Handle");
         
         //Get only the one called handle.getEJBObject
         Method getEJBObjectMethod = handleClass.getMethod("getEJBObject", new Class[0]);
         
         //Map it in the home stuff
         map.put(getEJBObjectMethod, getClass().getMethod("getEJBObject",
            new Class[] {MethodInvocation.class}));
      }
      catch (NoSuchMethodException e)
      {
         log.debug("Couldn't find getEJBObject method on container");
      }
      catch (Exception e)
      {
         log.error("Unexpected exception", e);
      }
      
      homeMapping = map;
   }
   
   
   private void setUpBeanMappingImpl(Map map,
   Method[] m,
   String declaringClass)
   throws NoSuchMethodException
   {
      for (int i = 0; i < m.length; i++)
      {
         if (!m[i].getDeclaringClass().getName().equals(declaringClass))
         {
            // Implemented by bean
            map.put(m[i], beanClass.getMethod(m[i].getName(),
            m[i].getParameterTypes()));
         }
         else
         {
            try
            {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName(),
               new Class[]
               { MethodInvocation.class }));
            } catch (NoSuchMethodException e)
            {
               log.error(m[i].getName() + " in bean has not been mapped", e);
            }
         }
      }
   }
   
   protected void setupBeanMapping() throws NoSuchMethodException
   {
      Map map = new HashMap();
      
      if (remoteInterface != null)
      {
         Method[] m = remoteInterface.getMethods();
         setUpBeanMappingImpl( map, m, "javax.ejb.EJBObject" );
      }
      if (localInterface != null)
      {
         Method[] m = localInterface.getMethods();
         setUpBeanMappingImpl( map, m, "javax.ejb.EJBLocalObject" );
      }
      
      beanMapping = map;
   }
   
   protected Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }
   
   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor
   implements Interceptor
   {
      public void setContainer(Container con)
      {}
      
      public void setNext(Interceptor interceptor)
      {}
      
      public Interceptor getNext()
      { return null; }
      
      public void init()
      {}
      
      public void start()
      {}
      
      public void stop()
      {}
      
      public void destroy()
      {}
      
      public Object invokeHome(MethodInvocation mi)
      throws Exception
      {
         Method m = (Method)homeMapping.get(mi.getMethod());
         // Invoke and handle exceptions
         
         try
         {
            return m.invoke(StatefulSessionContainer.this,
            new Object[]
            { mi });
         } catch (IllegalAccessException e)
         {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
         } catch (InvocationTargetException e)
         {
            Throwable ex = e.getTargetException();
            if (ex instanceof EJBException)
               throw (EJBException)ex;
            else if (ex instanceof RuntimeException)
               // Transform runtime exception into what a bean *should*
               // have thrown
               throw new EJBException((Exception)ex);
            else if (ex instanceof Exception)
               throw (Exception)ex;
            else
               throw (Error)ex;
         }
      }
      
      public Object invoke(MethodInvocation mi)
      throws Exception
      {
         //wire the transaction on the context, this is how the instance remember the tx
         // Unlike Entity beans we can't do that in the previous interceptors (ordering)
         if (mi.getEnterpriseContext().getTransaction() == null) mi.getEnterpriseContext().setTransaction(mi.getTransaction());
         
         // Get method
         Method m = (Method)beanMapping.get(mi.getMethod());
         
         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(StatefulSessionContainer.this.getClass()))
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(StatefulSessionContainer.this, new Object[]
               { mi });
            } catch (IllegalAccessException e)
            {
               // Throw this as a bean exception...(?)
               throw new EJBException(e);
            } catch (InvocationTargetException e)
            {
               Throwable ex = e.getTargetException();
               if (ex instanceof EJBException)
                  throw (EJBException)ex;
               else if (ex instanceof RuntimeException)
                  throw new EJBException((Exception)ex); // Transform runtime exception into what a bean *should* have thrown
               else if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            }
         } else
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(mi.getEnterpriseContext().getInstance(), mi.getArguments());
            } catch (IllegalAccessException e)
            {
               // Throw this as a bean exception...(?)
               throw new EJBException(e);
            } catch (InvocationTargetException e)
            {
               Throwable ex = e.getTargetException();
               if (ex instanceof EJBException)
                  throw (EJBException)ex;
               else if (ex instanceof RuntimeException)
                  throw new EJBException((Exception)ex); // Transform runtime exception into what a bean *should* have thrown
               else if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            }
         }
      }
      // Monitorable implementation ------------------------------------
      public void sample(Object s)
      {
         // Just here to because Monitorable request it but will be removed soon
      }
      public Map retrieveStatistic()
      {
         return null;
      }
      public void resetStatistic()
      {
      }
   }
}
