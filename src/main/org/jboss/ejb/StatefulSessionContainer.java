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
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.9 $
 */
public class StatefulSessionContainer
   extends Container
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Map createMapping;
   Map postCreateMapping;
  
   
   // This is the persistence manager for this container
   protected StatefulSessionPersistenceManager persistenceManager;
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

   public void setInstanceCache(InstanceCache ic)
   { 
      this.instanceCache = ic; 
      ic.setContainer(this);
   }
   
   public InstanceCache getInstanceCache() 
   { 
      return instanceCache; 
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
   
   // Container implementation --------------------------------------
   public void init()
      throws Exception
   {
   	  // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
   	  // Call default init
      super.init();
	  
	  // Map the bean methods
	  setupBeanMapping();
	  
	  // Map the home methods
      setupHomeMapping();
      
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
   
	public void stop() {
		
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
   public void remove(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException, RemoveException
   {
      getPersistenceManager().removeSession(ctx);
      ctx.setId(null);
   }
   
   public Handle getHandle(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      // TODO
      return null;
   }

   public Object getPrimaryKey(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      // TODO
      return null;
   }
   
   public EJBHome getEJBHome(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
	   
      return containerInvoker.getEJBHome();
   }
   
   public boolean isIdentical(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return false; // TODO
   }
   
   // Home interface implementation ---------------------------------
   public EJBObject createHome(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException, CreateException
   {
	  
      System.out.println("The context is "+ctx);
	  
	  System.out.println("In creating Home "+m.getDeclaringClass()+m.getName()+m.getParameterTypes().length);
	   
	  getPersistenceManager().createSession(m, args, ctx);
      return ctx.getEJBObject();
   }

   // EJBHome implementation ----------------------------------------
   public void removeHome(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException, RemoveException
   {
      // TODO
   }
   
   public EJBMetaData getEJBMetaDataHome(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return getContainerInvoker().getEJBMetaData();
   }
   
   public HomeHandle getHomeHandleHome(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
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
         map.put(m[i], getClass().getMethod(m[i].getName()+"Home", new Class[] { Method.class, Object[].class, StatefulSessionEnterpriseContext.class }));
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
               map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { Method.class, Object[].class , StatefulSessionEnterpriseContext.class}));
            
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
         
         try
         {
            return m.invoke(StatefulSessionContainer.this, new Object[] { method, args, ctx});
         } catch (InvocationTargetException e)
         {
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
         
         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(StatefulSessionContainer.this.getClass()))
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(StatefulSessionContainer.this, new Object[] { method, args, ctx });
            } catch (InvocationTargetException e)
            {
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

