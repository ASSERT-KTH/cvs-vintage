/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
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
import javax.ejb.EJBException;
import org.jboss.logging.Logger;


/**
*   StatelessSessionContainer
*   
*   @see <related>
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @author Daniel OConnor (docodan@mvcsoft.com)
*   @version $Revision: 1.17 $
*/
public class StatelessSessionContainer
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
    
    // This is the instancepool that is to be used
    protected InstancePool instancePool;
    
    // This is the first interceptor in the chain. The last interceptor must be provided by the container itself
    protected Interceptor interceptor;
    
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
    public void init()
        throws Exception
    {
        // Associate thread with classloader
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        
        // Acquire classes from CL
        homeInterface = classLoader.loadClass(metaData.getHome());
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
        containerInvoker.init();        
        
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
        containerInvoker.start();
        
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
        containerInvoker.stop();
        
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
        containerInvoker.destroy();
        
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
        //TODO
    }
    
    public Handle getHandle(MethodInvocation mi)
        throws java.rmi.RemoteException
    {
        // TODO
        return null;
    }
    
    public Object getPrimaryKey(MethodInvocation mi)
        throws java.rmi.RemoteException
    {
        // TODO
        return null;
    }
    
    public EJBHome getEJBHome(MethodInvocation mi)
        throws java.rmi.RemoteException
    {
        return containerInvoker.getEJBHome();
    }
    
    public boolean isIdentical(MethodInvocation mi)
        throws java.rmi.RemoteException
    {
        return false; // TODO
    }
    
    // EJBHome implementation ----------------------------------------
    public EJBObject createHome()
        throws java.rmi.RemoteException, CreateException
    {
        Object obj = containerInvoker.getStatelessSessionEJBObject();
        return (EJBObject)obj;
    }
    
    public void removeHome(Handle handle)
        throws java.rmi.RemoteException, RemoveException
    {
        // TODO
    }
    
    public void removeHome(Object primaryKey)
        throws java.rmi.RemoteException, RemoveException
    {
        // TODO
    }
    
    public EJBMetaData getEJBMetaDataHome()
        throws java.rmi.RemoteException
    {
        // TODO
        return null;
    }
    
    public HomeHandle getHomeHandleHome()
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
            Logger.debug("Mapping "+m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName()+"Home", m[i].getParameterTypes()));
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
            if (!m[i].getDeclaringClass().getName().equals("javax.ejb.EJBObject"))
            {
                // Implemented by bean
                map.put(m[i], beanClass.getMethod(m[i].getName(), m[i].getParameterTypes()));
                Logger.debug("Mapped "+m[i].getName()+" "+m[i].hashCode()+"to "+map.get(m[i]));
            }
            else
            {
                try
                {
                    // Implemented by container
                    Logger.debug("Mapped Container method "+m[i].getName() +" HASH "+m[i].hashCode());
                    map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { MethodInvocation.class }));
                    
                    
                } catch (NoSuchMethodException e)
                {
                    // DEBUG Logger.exception(e);
                    Logger.error(m[i].getName() + " in bean has not been mapped");
                    
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
        
        public Object invokeHome(MethodInvocation mi)
            throws Exception
        {
            Method m = (Method)homeMapping.get(mi.getMethod());
            try
            {
               return m.invoke(StatelessSessionContainer.this, mi.getArguments());
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
        
        public Object invoke(MethodInvocation mi)
            throws Exception
        {
            //wire the transaction on the context, this is how the instance remember the tx
             if (mi.getEnterpriseContext().getTransaction() == null) mi.getEnterpriseContext().setTransaction(mi.getTransaction());
			 
            // Get method and instance to invoke upon
            Method m = (Method)beanMapping.get(mi.getMethod());
            
            //If we have a method that needs to be done by the container (EJBObject methods)
            if (m.getDeclaringClass().equals(StatelessSessionContainer.class)) 
            {
                try 
                {
                    return m.invoke(StatelessSessionContainer.this, new Object[] { mi });
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
            } else // we have a method that needs to be done by a bean instance
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

