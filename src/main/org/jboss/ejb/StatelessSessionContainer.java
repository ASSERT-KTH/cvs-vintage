/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.HashMap;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.4 $
 */
public class StatelessSessionContainer
   extends Container
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // This is the container invoker for this container
   protected ContainerInvoker containerInvoker;
	
   Map homeMapping;
   Map beanMapping;
   
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
      
      // Reset classloader
      Thread.currentThread().setContextClassLoader(oldCl);
   }
	
   public Object invokeHome(Method method, Object[] args)
      throws Exception
   {
      return getInterceptor().invokeHome(method, args, null);
   }

   /**
    *   This method does invocation interpositioning of tx and security, 
    *   retrieves the instance from an object table, and invokes the method
    *   on the particular instance
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
   public void remove(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException, RemoveException
   {
		// Do nothing
   }
   
   public Handle getHandle(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      // TODO
   	throw new Error("Not yet implemented");
   }

   public Object getPrimaryKey(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
		throw new java.rmi.RemoteException("Sessions do not have primary keys");
   }
   
   public EJBHome getEJBHome(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
      return containerInvoker.getEJBHome();
   }
   
   public boolean isIdentical(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
      throws java.rmi.RemoteException
   {
   	throw new Error("Not yet implemented");
   }
	
   // EJBHome implementation ----------------------------------------
   public EJBObject create()
      throws java.rmi.RemoteException, CreateException
   {
      Object obj = containerInvoker.getStatelessSessionEJBObject();
      return (EJBObject)obj;
   }

   public void remove(Handle handle)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("Not yet implemented");
   }
   
   public void remove(java.lang.Object primaryKey)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("Not yet implemented");
   }
   
   public EJBMetaData getEJBMetaData()
      throws java.rmi.RemoteException
   {
      throw new Error("Not yet implemented");
   }
   
   public HomeHandle getHomeHandle()
      throws java.rmi.RemoteException   
   {
      // TODO
      return null;
   }
      
   // Protected  ----------------------------------------------------
   protected void setupHomeMapping()
      throws DeploymentException
   {
      Map map = new HashMap();
      
      Method[] m = homeInterface.getMethods();
      for (int i = 0; i < m.length; i++)
      {
			try
			{
	         // Implemented by container
	         map.put(m[i], getClass().getMethod(m[i].getName(), m[i].getParameterTypes()));
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
               map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { Method.class, Object[].class , StatelessSessionEnterpriseContext.class}));
	         }
	      } catch (NoSuchMethodException e)
	      {
	      	throw new DeploymentException("Could not find matching method for "+m[i], e);
	      }
      }
      
      beanMapping = map;
   }
   
   Interceptor createContainerInterceptor()
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
         return m.invoke(StatelessSessionContainer.this, args);
      }
         
      public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
         throws Exception
      {
         // Get method and instance to invoke upon
         Method m = (Method)beanMapping.get(method);

         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(StatelessSessionContainer.class))
         {
            // Invoke and handle exceptions
            try
            {
               return m.invoke(StatelessSessionContainer.this, new Object[] { method, args, ctx });
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

