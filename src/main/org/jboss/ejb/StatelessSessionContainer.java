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
*   StatelessSessionContainer
*   
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.5 $
*/
public class StatelessSessionContainer
extends Container
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	
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
		
		
		// Map the bean methods
		setupBeanMapping();
		
		// Map the home methods
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
	public void remove()
	throws java.rmi.RemoteException, RemoveException
	{
		// TODO
	}
	
	public void remove(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
	throws java.rmi.RemoteException, RemoveException
	{
		//TODO
	}
	
	public Handle getHandle(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
	throws java.rmi.RemoteException
	{
		// TODO
		return null;
	}
	
	public Object getPrimaryKey(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
	throws java.rmi.RemoteException
	{
		// TODO
		return null;
	}
	
	public EJBHome getEJBHome(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
	throws java.rmi.RemoteException
	{
		return containerInvoker.getEJBHome();
	}
	
	public boolean isIdentical(Method m, Object[] args, StatelessSessionEnterpriseContext ctx)
	throws java.rmi.RemoteException
	{
		return false; // TODO
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
		// TODO
	}
	
	public void remove(java.lang.Object primaryKey)
	throws java.rmi.RemoteException, RemoveException
	{
		// TODO
	}
	
	public EJBMetaData getEJBMetaData()
	throws java.rmi.RemoteException
	{
		// TODO
		return null;
	}
	
	public HomeHandle getHomeHandle()
	throws java.rmi.RemoteException   
	{
		// TODO
		return null;
	}
	
	// Protected  ----------------------------------------------------
	protected void setupHomeMapping()
	throws NoSuchMethodException
	{
		Map map = new HashMap();
		
		Method[] m = homeInterface.getMethods();
		for (int i = 0; i < m.length; i++)
		{
			// Implemented by container
			//System.out.println("Mapping "+m[i].getName());
			map.put(m[i], getClass().getMethod(m[i].getName(), m[i].getParameterTypes()));
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
				//System.out.println("Mapped "+m[i].getName()+" "+m[i].hashCode()+"to "+map.get(m[i]));
			}
			else
			{
				try
				{
					// Implemented by container
					//System.out.println("Mapped Container method "+m[i].getName() +" HASH "+m[i].hashCode());
					map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { Method.class, Object[].class, StatelessSessionEnterpriseContext.class}));
					
					
				} catch (NoSuchMethodException e)
				{
					e.printStackTrace();
					System.out.println(m[i].getName() + " in bean has not been mapped");
					
				}
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
			
			Object instance;
			
			//If we have a method that needs to be fielded by the container
			if (m.getDeclaringClass().equals(StatelessSessionContainer.this.getClass())) {
				
		    	try {
             		
					// Return by the present container, adapt the method invocation parameters
					return m.invoke(StatelessSessionContainer.this, new Object[] { method, args, ctx });
            	
				} catch (InvocationTargetException e) {
               
			   		Throwable ex = e.getTargetException();
               
			   		if (ex instanceof Exception) throw (Exception)ex;
               		else throw (Error)ex;
            	}
			}
			
		    // we have a method that needs to be fielded by a bean instance
			else {
            
				// Invoke and handle exceptions
            	try {
               
			   		return m.invoke(ctx.getInstance(), args);
            
				} 
				catch (InvocationTargetException e) {
               	
					Throwable ex = e.getTargetException();
               		
					if (ex instanceof Exception) throw (Exception)ex;
               		else throw (Error)ex;
            	} 
         	}
      	}
	}
}

