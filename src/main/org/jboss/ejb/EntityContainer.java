/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.logging.Logger;

/**
 *   This is a Container for EntityBeans (both BMP and CMP).
 *      
 *   @see Container
 *   @see EntityEnterpriseContext
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.8 $
 */
public class EntityContainer
   extends Container
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	
	// These are the mappings between the create methods and the ejbCreate methods
   protected Map createMapping;
	
   // These are the mappings between the create methods and the ejbPostCreate methods
   protected Map postCreateMapping;
	
   // This is the persistence manager for this container
   protected EntityPersistenceManager persistenceManager;
	
   // This is the instance cache for this container
   protected InstanceCache instanceCache;
   
   // Public --------------------------------------------------------
   public void setContainerInvoker(ContainerInvoker ci) 
   { 
      if (ci == null)
      	throw new IllegalArgumentException("Null invoker");
   		
      this.containerInvoker = ci; 
      ci.setContainer(this);
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
	
   public void setPersistenceManager(EntityPersistenceManager pm) 
   { 
      if (pm == null)
      	throw new IllegalArgumentException("Null persistence manager");
			
      persistenceManager = pm; 
      pm.setContainer(this);
   }
   
   // Container implementation --------------------------------------
   public void init()
      throws Exception
   {
		// Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
		// Call default init
      super.init();
      
      // Init container invoker
      containerInvoker.init();
		
      // Init instance cache
      instanceCache.init();
		
      // Init persistence
      persistenceManager.init();
      
      setupBeanMapping();
      setupHomeMapping();
      
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
      containerInvoker.start();
      
      // Start instance cache
      instanceCache.start();
      
      // Start persistence
      persistenceManager.start();
      
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
	   containerInvoker.stop();
	   
	   // Stop instance cache
	   instanceCache.stop();
	   
	   // Stop persistence
	   persistenceManager.stop();
	   
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
	   containerInvoker.destroy();
	   
	   // Destroy instance cache
	   instanceCache.destroy();
	   
	   // Destroy persistence
	   persistenceManager.destroy();
	   
	   // Reset classloader
	   Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public Object invokeHome(Method method, Object[] args)
      throws Exception
   {
	   return getInterceptor().invokeHome(method, args, null);
   }

   public Object invoke(Object id, Method method, Object[] args)
      throws Exception
   {
      // Invoke through interceptors
      return getInterceptor().invoke(id, method, args, null);
   }
   
   // EJBObject implementation --------------------------------------
   public void remove(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException, RemoveException
   {
      getPersistenceManager().removeEntity(ctx);
      ctx.setId(null);
   }
   
   public Handle getHandle(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      // TODO
		throw new Error("Not yet implemented");
   }

   public Object getPrimaryKey(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      // TODO
      throw new Error("Not yet implemented");
   }
   
   public EJBHome getEJBHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return containerInvoker.getEJBHome();
   }
   
   public boolean isIdentical(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
		return ((EJBObject)args[0]).getPrimaryKey().equals(ctx.getId());
		// TODO - should also check type
   }
   
   // Home interface implementation ---------------------------------
   public Object find(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException, FinderException
   {
      // Multi-finder?
      if (!m.getReturnType().equals(getRemoteClass()))
      {
         // Iterator finder
         Collection c = getPersistenceManager().findEntities(m, args, ctx);
         return containerInvoker.getEntityCollection(c);
      } else
      {
         // Single entity finder
         Object id = getPersistenceManager().findEntity(m, args, ctx);
         return (EJBObject)containerInvoker.getEntityEJBObject(id);
      }
   }

   public EJBObject createHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException, CreateException
   {
	   System.out.println("In creating Home "+m.getDeclaringClass()+m.getName()+m.getParameterTypes().length);
	   
      getPersistenceManager().createEntity(m, args, ctx);
      return ctx.getEJBObject();
   }

   // EJBHome implementation ----------------------------------------
   public void removeHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("Not yet implemented");
   }
   
   public EJBMetaData getEJBMetaDataHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return getContainerInvoker().getEJBMetaData();
   }
   
   public HomeHandle getHomeHandleHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException   
   {
      // TODO
      throw new Error("Not yet implemented");
   }
      
   // Private -------------------------------------------------------
   protected void setupHomeMapping()
      throws DeploymentException
   {
      Map map = new HashMap();
      
      Method[] m = homeInterface.getMethods();
      for (int i = 0; i < m.length; i++)
      {
		  System.out.println("THE NEW METHOD IS "+m[i].getName()+m[i].getParameterTypes().length);
			try
			{
	         // Implemented by container
	         if (m[i].getName().startsWith("find"))
	            map.put(m[i], getClass().getMethod("find", new Class[] { Method.class, Object[].class, EntityEnterpriseContext.class }));
	         else            
	            map.put(m[i], getClass().getMethod(m[i].getName()+"Home", new Class[] { Method.class, Object[].class, EntityEnterpriseContext.class }));
			} catch (NoSuchMethodException e)
			{
				throw new DeploymentException("Could not find matching method for "+m[i], e);
			}
      }
      
      homeMapping = map;
   }

   protected void setupBeanMapping()
      throws DeploymentException
   {
      Map map = new HashMap();
      
      Method[] m = remoteInterface.getMethods();
      for (int i = 0; i < m.length; i++)
      {
			try
			{
	         if (!m[i].getDeclaringClass().equals(EJBObject.class))
	         {
	            // Implemented by bean
	            map.put(m[i], beanClass.getMethod(m[i].getName(), m[i].getParameterTypes()));
	         }
	         else
	         {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { Method.class, Object[].class , EntityEnterpriseContext.class}));
	         }
	      } catch (NoSuchMethodException e)
	      {
	      	throw new DeploymentException("Could not find matching method for "+m[i], e);
	      }
      }
      
      beanMapping = map;
   }
   
   public Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }
   
	// Inner classes -------------------------------------------------
   // This is the last step before invocation - all interceptors are done
   class ContainerInterceptor
      implements Interceptor
   {
      public void setContainer(Container con) {}
      
      public void setNext(Interceptor interceptor) {}
      public Interceptor getNext() { return null; }
      
      public void init() {}
      public void start() {}
      public void stop() {}
      public void destroy() {}
      
      public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
         throws Exception
      {
		 
		  //Debug
		 System.out.println("InvokingHome "+method.getName());
         //Debug
		 
         Method m = (Method)homeMapping.get(method);
         // Invoke and handle exceptions
         
		 try
         {
            return m.invoke(EntityContainer.this, new Object[] { method, args, ctx});
         } catch (InvocationTargetException e)
         {
			//Debug
			e.printStackTrace();
			System.out.println("Home Exception seen  "+e.getMessage());
            //Debug
			Throwable ex = e.getTargetException();
            if (ex instanceof Exception)
               throw (Exception)ex;
            else
               throw (Error)ex;
         }
      }
         
      public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
         throws Exception
      {
         // Get method
         Method m = (Method)beanMapping.get(method);
		 
		 //Debug
		 System.out.println("InvokingBean "+method.getName());
		 //Debug
         
         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(EntityContainer.class))
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(EntityContainer.this, new Object[] { method, args, ctx });
            } catch (InvocationTargetException e)
            {
               //Debug
			   System.out.println("Bean Exception seen  "+e.getMessage());
			   //Debug
			   Throwable ex = e.getTargetException();
               if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            } 
         } else
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(ctx.getInstance(), args);
            } catch (InvocationTargetException e)
            {
               Throwable ex = e.getTargetException();
               if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            } 
         }
      }
   }
}

