/*
* JBoss, the OpenSource EJB server
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

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.logging.Logger;
import org.jboss.util.SerializableEnumeration;

/**
*   This is a Container for EntityBeans (both BMP and CMP).
*   
*
*   @see Container
*   @see EntityEnterpriseContext
*   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*   @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
*   @author <a href="bill@burkecentral.com">Bill Burke</a>
*   @version $Revision: 1.44 $
*
* 
*   <p><b>Revisions:</b>
*
*   <p><b>20010701 marc fleury:</b>
*   <ul>
*   <li>Transaction to context wiring was moved to the instance interceptor
*   </ul>
*/


public class EntityContainer
extends Container
implements ContainerInvokerContainer, InstancePoolContainer
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // This is the Home interface class
   protected Class homeInterface;

   // This is the Remote interface class
   protected Class remoteInterface;
   
   // These are the mappings between the home interface methods and the container methods
   protected Map homeMapping;

   // These are the mappings between the remote interface methods and the bean methods
   protected Map beanMapping;

   
   // This is the container invoker for this container
   protected ContainerInvoker containerInvoker;

   // This is the persistence manager for this container
   protected EntityPersistenceManager persistenceManager;

   // This is the instance cache for this container
   protected InstanceCache instanceCache;

   // This is the instancepool that is to be used
   protected InstancePool instancePool;

   // This provides a way to find the entities that are part of a given transaction
   // EntitySynchronizationInterceptor and InstanceSynchronization manage
   // this instance.
   protected TxEntityMap txEntityMap = new TxEntityMap();

   // This is the first interceptor in the chain. The last interceptor must be provided by the container itself
   protected Interceptor interceptor;

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

   public void setInstanceCache(InstanceCache ic)
   {
      if (ic == null)
         throw new IllegalArgumentException("Null cache");

         this.instanceCache = ic;
      ic.setContainer(this);
   }

   public InstanceCache getInstanceCache()
   {
      return instanceCache;
   }

   public EntityPersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   public TxEntityMap getTxEntityMap()
   {
      return txEntityMap;
   }

   public void setPersistenceManager(EntityPersistenceManager pm)
   {
      if (pm == null)
         throw new IllegalArgumentException("Null persistence manager");

         persistenceManager = pm;
      pm.setContainer(this);
   }

   public void addInterceptor(Interceptor in)
   {
      if (interceptor == null)
      {
         interceptor = in;
      }
      else
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
 
 	/**
	* Returns a new instance of the bean class or a subclass of the bean class.
	* If this is 1.x cmp, simply return a new instance of the bean class.
	* If this is 2.x cmp, return a subclass that provides an implementation
	* of the abstract accessors.
	* 
	* @see java.lang.Class#newInstance 
	* @return the new instance
	*/
	public Object createBeanClassInstance() throws Exception {
		return persistenceManager.createBeanClassInstance();
	}

   // Container implementation --------------------------------------
   public void init()
   throws Exception
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

      // Initialize pool
      instancePool.init();

      // Init container invoker
      if (containerInvoker != null)
         containerInvoker.init();

      // Init instance cache
      instanceCache.init();

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

   public void start()
   throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      // Call default start
      super.start();

      // Start container invoker
      if (containerInvoker != null)
         containerInvoker.start();

      // Start instance cache
      instanceCache.start();

      // Start persistence
      persistenceManager.start();

      // Start the instance pool
      instancePool.start();

      // Start all interceptors in the chain
      Interceptor in = interceptor;
      while (in != null)
      {
         in.start();
         in = in.getNext();
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

      // Stop persistence
      persistenceManager.stop();

      // Stop the instance pool
      instancePool.stop();

      // Stop all interceptors in the chain
      Interceptor in = interceptor;
      while (in != null)
      {
         in.stop();
         in = in.getNext();
      }

      // Reset classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }

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

      // Destroy persistence
      persistenceManager.destroy();

      // Destroy the pool
      instancePool.destroy();

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

   public Object invokeHome(MethodInvocation mi)
   throws Exception
   {
      return getInterceptor().invokeHome(mi);
   }

   public Object invoke(MethodInvocation mi)
   throws Exception
   {
      // Invoke through interceptors
      return getInterceptor().invoke(mi);
   }

   // EJBObject implementation --------------------------------------
   public void remove(MethodInvocation mi)
   throws java.rmi.RemoteException, RemoveException
   {
      // Get the persistence manager to do the dirty work
      getPersistenceManager().removeEntity((EntityEnterpriseContext)mi.getEnterpriseContext());

      // We signify "removed" with a null id
		// There is no need to synchronize on the context since all the threads reaching here have
		// gone through the InstanceInterceptor so the instance is locked and we only have one thread 
		// the case of reentrant threads is unclear (would you want to delete an instance in reentrancy)
      mi.getEnterpriseContext().setId(null);
   }

   public Handle getHandle(MethodInvocation mi)
   throws java.rmi.RemoteException
   {
      // TODO
      throw new Error("Not yet implemented");
   }

   public Object getPrimaryKey(MethodInvocation mi)
   throws java.rmi.RemoteException
   {
      // TODO
      throw new Error("Not yet implemented");
   }

   public EJBHome getEJBHome(MethodInvocation mi)
   throws java.rmi.RemoteException
   {
      if (containerInvoker == null)
         throw new java.lang.IllegalStateException();
      return containerInvoker.getEJBHome();
   }


   public boolean isIdentical(MethodInvocation mi)
   throws java.rmi.RemoteException
   {
      return ((EJBObject)mi.getArguments()[0]).getPrimaryKey().equals(mi.getEnterpriseContext().getId());
      // TODO - should also check type
   }

   /**
    * MF FIXME these are implemented on the client
    */

    public EJBLocalHome getEJBLocalHome(MethodInvocation mi)
    {
      return localContainerInvoker.getEJBLocalHome();
    }
 
   public void removeLocalHome(MethodInvocation mi)
    throws java.rmi.RemoteException, RemoveException
   {
       throw new Error("Not Yet Implemented");
   }
    
    // Local home interface implementation
   
   public EJBLocalObject createLocalHome(MethodInvocation mi)
   throws Exception
   {

      // The persistence manager takes care of the wiring and creating the EJBLocalObject
      getPersistenceManager().createEntity(mi.getMethod(),mi.getArguments(),
         (EntityEnterpriseContext) mi.getEnterpriseContext());

      // The context implicitely carries the EJBObject
      return ((EntityEnterpriseContext)mi.getEnterpriseContext()).getEJBLocalObject();
   }
   
   
   public Object findLocal(MethodInvocation mi)
   throws Exception
   {

      // Multi-finder?
      if (!mi.getMethod().getReturnType().equals(getLocalClass()))
      {
         // Iterator finder
         Collection c = getPersistenceManager().findEntities(mi.getMethod(), mi.getArguments(), (EntityEnterpriseContext)mi.getEnterpriseContext());

         // Get the EJBObjects with that
         Collection ec = localContainerInvoker.getEntityLocalCollection(c);

         // BMP entity finder methods are allowed to return java.util.Enumeration.
         try {
            if (mi.getMethod().getReturnType().equals(Class.forName("java.util.Enumeration")))
            {
               return java.util.Collections.enumeration(ec);
            }
            else
            {
               return ec;
            }
         } catch (ClassNotFoundException e)
         {
            // shouldn't happen
            return ec;
         }

      }
      else
      {

         // Single entity finder
         Object id = getPersistenceManager().findEntity(mi.getMethod(),
            mi.getArguments(),
            (EntityEnterpriseContext)mi.getEnterpriseContext());

         //create the EJBObject
         return (EJBLocalObject)localContainerInvoker.getEntityEJBLocalObject(id);
      }
   }
   
   
   // Home interface implementation ---------------------------------

   /*
   * find(MethodInvocation)
   *
   * This methods finds the target instances by delegating to the persistence manager
   * It then manufactures EJBObject for all the involved instances found
   */

   public Object find(MethodInvocation mi)
   throws Exception
   {

      // Multi-finder?
      if (!mi.getMethod().getReturnType().equals(getRemoteClass()))
      {
         // Iterator finder
         Collection c = getPersistenceManager().findEntities(mi.getMethod(), mi.getArguments(), (EntityEnterpriseContext)mi.getEnterpriseContext());

         // Get the EJBObjects with that
         Collection ec = containerInvoker.getEntityCollection(c);

         // BMP entity finder methods are allowed to return java.util.Enumeration.
         // We need a serializable Enumeration, so we can't use Collections.enumeration()
         try {
            if (mi.getMethod().getReturnType().equals(Class.forName("java.util.Enumeration")))
            {
               return new SerializableEnumeration(ec);
            }
            else
            {
               return ec;
            }
         } catch (ClassNotFoundException e)
         {
            // shouldn't happen
            return ec;
         }

      }
      else
      {

         // Single entity finder
         Object id = getPersistenceManager().findEntity(mi.getMethod(),
            mi.getArguments(),
            (EntityEnterpriseContext)mi.getEnterpriseContext());

         //create the EJBObject
         return (EJBObject)containerInvoker.getEntityEJBObject(id);
      }
   }

   /**
   * createHome(MethodInvocation)
   *
   * This method takes care of the wiring of the "EJBObject" trio (target, context, proxy)
   * It delegates to the persistence manager.
   *
   */

   public EJBObject createHome(MethodInvocation mi)
   throws Exception
   {

      // The persistence manager takes care of the wiring and creating the EJBObject
      getPersistenceManager().createEntity(mi.getMethod(),mi.getArguments(),
         (EntityEnterpriseContext) mi.getEnterpriseContext());

      // The context implicitely carries the EJBObject
      return ((EntityEnterpriseContext)mi.getEnterpriseContext()).getEJBObject();
   }

   /**
   * A method for the getEJBObject from the handle
   *
   */
   public EJBObject getEJBObject(MethodInvocation mi)
   throws java.rmi.RemoteException
   {

      // All we need is an EJBObject for this Id;
      return (EJBObject)containerInvoker.getEntityEJBObject(((EntityCache) instanceCache).createCacheKey(mi.getId()));
   }



   // EJBHome implementation ----------------------------------------


      public void removeHome(MethodInvocation mi)
   throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("Not yet implemented");
   }

   public EJBMetaData getEJBMetaDataHome(MethodInvocation mi)
   throws java.rmi.RemoteException
   {
      return getContainerInvoker().getEJBMetaData();
   }

   public HomeHandle getHomeHandleHome(MethodInvocation mi)
   throws java.rmi.RemoteException
   {
      // TODO
      throw new Error("Not yet implemented");
   }


   // Private -------------------------------------------------------
   
   private void setupHomeMappingImpl( Map map, Method[] m, String finderName, String append )
     throws DeploymentException
   {
      for (int i = 0; i < m.length; i++)
      {
         try
         {
            try // Try home method
            {
               String methodName = "ejbHome" + Character.toUpperCase(m[i].getName().charAt(0)) + m[i].getName().substring(1);
               map.put(m[i], beanClass.getMethod(methodName, m[i].getParameterTypes()));

               continue;
            } catch (NoSuchMethodException e)
            {
               // Ignore - just go on with other types of methods
            }

            // Implemented by container (in both cases)
            if (m[i].getName().startsWith("find"))
            {
               map.put(m[i], this.getClass().getMethod(finderName, new Class[] { MethodInvocation.class }));
            }else
            {
               map.put(m[i], this.getClass().getMethod(m[i].getName()+append, new Class[] { MethodInvocation.class }));
            }
         } catch (NoSuchMethodException e)
         {
            throw new DeploymentException("Could not find matching method for "+m[i]);
         }
      }
   }
   
   protected void setupHomeMapping()
   throws DeploymentException
   {
      Map map = new HashMap();

      if (homeInterface != null)
      {
         Method[] m = homeInterface.getMethods();
         setupHomeMappingImpl( map, m, "find", "Home" );
      }
      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         setupHomeMappingImpl( map, m, "findLocal", "LocalHome" );
      }

      // Special methods

      try {

         // Get the One on Handle (getEJBObject), get the class
         Class handleClass = Class.forName("javax.ejb.Handle");

         // Get the methods (there is only one)
         Method[] handleMethods = handleClass.getMethods();

         //Just to make sure let's iterate
         for (int j=0; j<handleMethods.length ;j++)
         {

            try
            {

               //Get only the one called handle.getEJBObject
               if (handleMethods[j].getName().equals("getEJBObject"))
               {

                  //Map it in the home stuff
                  map.put(handleMethods[j], this.getClass().getMethod("getEJBObject", new Class[]
                     {MethodInvocation.class
                     }));
                  }
               }
               catch (NoSuchMethodException e)
               {
               throw new DeploymentException("Couldn't find getEJBObject method on container");
            }
         }
      }
      catch (Exception e)
      {
         // DEBUG Logger.exception(e);
      }

      // We are done keep the home map
      homeMapping = map;
   }
   
   private void setupBeanMappingImpl( Map map, Method[] m, String intfName )
      throws DeploymentException
   {
      for (int i = 0; i < m.length; i++)
      {
         try
         {
            if (!m[i].getDeclaringClass().getName().equals(intfName))
            {
               // Implemented by bean
               map.put(m[i], beanClass.getMethod(m[i].getName(), m[i].getParameterTypes()));
            }
            else
            {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName(), new Class[]
                  { MethodInvocation.class
                  }));
               }
            } catch (NoSuchMethodException e)
            {
            throw new DeploymentException("Could not find matching method for "+m[i], e);
         }
      }
   }

   protected void setupBeanMapping()
   throws DeploymentException
   {
      Map map = new HashMap();

      if (remoteInterface != null)
      {
         Method[] m = remoteInterface.getMethods();
         setupBeanMappingImpl( map, m, "javax.ejb.EJBObject" );
      }
      if (localInterface != null)
      {
         Method[] m = localInterface.getMethods();
         setupBeanMappingImpl( map, m, "javax.ejb.EJBLocalObject" );
      }

      beanMapping = map;

   }

   Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }
   
   // Inner classes -------------------------------------------------
   // This is the last step before invocation - all interceptors are done
   class ContainerInterceptor
   implements Interceptor
   {
      public void setContainer(Container con)
      {
      }

      public void setNext(Interceptor interceptor)
      {
      }
      public Interceptor getNext()
      { return null;
      }

      public void init()
      {
      }
      public void start()
      {
      }
      public void stop()
      {
      }
      public void destroy()
      {
      }

      public Object invokeHome(MethodInvocation mi)
      throws Exception
      {
         // Invoke and handle exceptions
         Method m = (Method)homeMapping.get(mi.getMethod());

         if (m.getDeclaringClass().equals(EntityContainer.class))
         {
            try
            {
               return m.invoke(EntityContainer.this, new Object[] { mi });
            } catch (IllegalAccessException e)
            {
               // Throw this as a bean exception...(?)
               throw new EJBException(e);
            } catch (InvocationTargetException e)
            {
               Throwable ex = e.getTargetException();
               if (ex instanceof EJBException)
                  throw (Exception)ex;
               else if (ex instanceof RuntimeException)
                  throw new EJBException((Exception)ex); // Transform runtime exception into what a bean *should* have thrown
               else if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            }
         } else // Home method
         {
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
                  throw (Exception)ex;
               else if (ex instanceof RuntimeException)
                  throw new EJBException((Exception)ex); // Transform runtime exception into what a bean *should* have thrown
               else if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            }
         }
      }

      public Object invoke(MethodInvocation mi)
      throws Exception
      {
         // Get method
         Method m = (Method)beanMapping.get(mi.getMethod());

         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(EntityContainer.class))
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(EntityContainer.this, new Object[]
               {
                  mi
               });
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
         else
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
   }
}


