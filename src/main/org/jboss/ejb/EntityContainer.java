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

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class EntityContainer
   extends Container
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Map createMapping;
   Map postCreateMapping;
   Map homeMapping;
   Map beanMapping;
   
   Log log;
   
   EntityPersistenceManager persistenceManager;
   InstanceCache instanceCache;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   public void setInstanceCache(InstanceCache ic)
   { 
      this.instanceCache = ic; 
      ic.setContainer(this);
   }
   
   public InstanceCache getInstanceCache() 
   { 
      return instanceCache; 
   }
   
   public EntityPersistenceManager getPersistenceManager() { return persistenceManager; }
   public void setPersistenceManager(EntityPersistenceManager pm) 
   { 
      persistenceManager = pm; 
      pm.setContainer(this);
   }
   
   // Container implementation --------------------------------------
   public void init()
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      log = new Log(getMetaData().getEjbName() + " EJB");
      
      super.init();
      
      // Init persistence
      persistenceManager.init();
      
      setupBeanMapping();
      setupHomeMapping();
      
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void start()
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      super.start();
      
      // Start persistence
      persistenceManager.start();
      
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void stop()
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      // Stop persistence
      persistenceManager.stop();
      
      super.stop();
      
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void destroy()
   {
      // Destroy persistence
      persistenceManager.destroy();
      
      super.destroy();
   }
   
   public Object invokeHome(Method method, Object[] args)
      throws Exception
   {
      try
      {
         return getInterceptor().invokeHome(method, args, null);
      } finally
      {
//         System.out.println("Invoke home on bean finished");
      }
   }

   /**
    *   This method retrieves the instance from an object table, and invokes the method
    *   on the particular instance through the chain of interceptors
    *
    * @param   id  
    * @param   m  
    * @param   args  
    * @return     
    * @exception   Exception  
    */
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
      return null;
   }

   public Object getPrimaryKey(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      // TODO
      return null;
   }
   
   public EJBHome getEJBHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return containerInvoker.getEJBHome();
   }
   
   public boolean isIdentical(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return false; // TODO
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
      getPersistenceManager().createEntity(m, args, ctx);
      return ctx.getEJBObject();
   }

   // EJBHome implementation ----------------------------------------
   public void removeHome(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws java.rmi.RemoteException, RemoveException
   {
      // TODO
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
      return null;
   }
      
   // Private -------------------------------------------------------
   protected void setupHomeMapping()
      throws NoSuchMethodException
   {
      Map map = new HashMap();
      
      Method[] m = homeInterface.getMethods();
      for (int i = 0; i < m.length; i++)
      {
         // Implemented by container
         if (m[i].getName().startsWith("find"))
            map.put(m[i], getClass().getMethod("find", new Class[] { Method.class, Object[].class, EntityEnterpriseContext.class }));
         else            
            map.put(m[i], getClass().getMethod(m[i].getName()+"Home", new Class[] { Method.class, Object[].class, EntityEnterpriseContext.class }));
      }
      
      homeMapping = map;
   }

   protected void setupBeanMapping()
      throws NoSuchMethodException
   {
      Map map = new HashMap();
      
      Method[] m = remoteInterface.getMethods();
      for (int i = 0; i < m.length; i++)
      {
         if (!m[i].getDeclaringClass().equals(EJBObject.class))
         {
            // Implemented by bean
            map.put(m[i], beanClass.getMethod(m[i].getName(), m[i].getParameterTypes()));
         }
         else
         {
            try
            {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { Method.class, Object[].class , EntityEnterpriseContext.class}));
            } catch (NoSuchMethodException e)
            {
               System.out.println(m[i].getName() + " in bean has not been mapped");
            }
         }
      }
      
      beanMapping = map;
   }
   
   protected Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }
   
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
         Method m = (Method)homeMapping.get(method);
         // Invoke and handle exceptions
         
         Log.setLog(log);
         try
         {
            return m.invoke(EntityContainer.this, new Object[] { method, args, ctx});
         } catch (InvocationTargetException e)
         {
            Throwable ex = e.getTargetException();
            if (ex instanceof Exception)
               throw (Exception)ex;
            else
               throw (Error)ex;
         } finally
         {
            Log.unsetLog();
         }
      }
         
      public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
         throws Exception
      {
         // Get method
         Method m = (Method)beanMapping.get(method);
         
         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(EntityContainer.this.getClass()))
         {
            // Invoke and handle exceptions
            Log.setLog(log);
            try
            {
               return m.invoke(EntityContainer.this, new Object[] { method, args, ctx });
            } catch (InvocationTargetException e)
            {
               Throwable ex = e.getTargetException();
               if (ex instanceof Exception)
                  throw (Exception)ex;
               else
                  throw (Error)ex;
            } finally
            {
               Log.unsetLog();
            }
         } else
         {
            // Invoke and handle exceptions
            Log.setLog(log);
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
            } finally
            {
               Log.unsetLog();
            }
         }
      }
   }
}

