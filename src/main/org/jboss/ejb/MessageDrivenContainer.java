/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

import javax.jms.MessageListener;
import javax.jms.Message;

import org.jboss.invocation.Invocation;
import org.jboss.ejb.EnterpriseContext;

import org.jboss.util.NullArgumentException;

/**
 * The container for <em>MessageDriven</em> beans.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @version $Revision: 1.22 $
 */
public class MessageDrivenContainer
   extends Container
   implements EJBProxyFactoryContainer, InstancePoolContainer
{
   /**
    * These are the mappings between the remote interface methods
    * and the bean methods.
    */
   protected Map beanMapping;

   /** This is the instancepool that is to be used. */
   protected InstancePool instancePool;

   /**
    * This is the first interceptor in the chain.
    * The last interceptor must be provided by the container itself.
    */
   protected Interceptor interceptor;

   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
   }

   public void setInstancePool(final InstancePool instancePool)
   {
      if (instancePool == null)
         throw new NullArgumentException("instancePool");

      this.instancePool = instancePool;
      this.instancePool.setContainer(this);
   }

   public InstancePool getInstancePool()
   {
      return instancePool;
   }

   public void addInterceptor(Interceptor in)
   {
      if (interceptor == null) {
         interceptor = in;
      }
      else {
         Interceptor current = interceptor;

         while (current.getNext() != null) {
            current = current.getNext();
         }

         current.setNext(in);
      }
   }

   public Interceptor getInterceptor()
   {
      return interceptor;
   }

   /**
    * EJBProxyFactoryContainer - not needed, should we skip inherit this
    * or just throw Error??
    */
   public Class getHomeClass()
   {
      //throw new Error("HomeClass not valid for MessageDriven beans");
      return null;
   }

   public Class getRemoteClass()
   {
      //throw new Error("RemoteClass not valid for MessageDriven beans");
      return null;
   }

   public Class getLocalClass()
   {
      return null;
   }

   public Class getLocalHomeClass()
   {
      //throw new Error("LocalHomeClass not valid for MessageDriven beans");
      return null;
   }

   // Container implementation - overridden here ----------------------

   public void create() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default init
         super.create();

         // Map the bean methods
         Map map = new HashMap();
         Method m = MessageListener.class.getMethod("onMessage", new Class[] { Message.class });
         map.put(m, beanClass.getMethod(m.getName(), m.getParameterTypes()));
         log.debug("Mapped " + m.getName() + " " + m.hashCode() + " to " + map.get(m));
         beanMapping = map;

         // Initialize pool
         instancePool.create();

         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.create();
         }

         // Initialize the interceptor by calling the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.setContainer(this);
            in.create();
            in = in.getNext();
         }

      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public void start() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default start
         super.start();

         // Start container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.start();
         }
         
         // Start the instance pool
         instancePool.start();

         // Start all interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.start();
            in = in.getNext();
         }
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public void stop()
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default stop
         super.stop();

         // Stop container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.stop();
         }

         // Stop the instance pool
         instancePool.stop();

         // Stop all interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.stop();
            in = in.getNext();
         }
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public void destroy()
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
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.setContainer(null);
            ci.destroy();
         }

         // Destroy the pool
         instancePool.destroy();
         instancePool.setContainer(null);

         // Destroy all the interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.destroy();
            in.setContainer(null);
            in = in.getNext();
         }

         // Call default destroy
         super.destroy();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }


   public Object invokeHome(Invocation mi)
      throws Exception
   {
      throw new Error("invokeHome not valid for MessageDriven beans");
      //return getInterceptor().invokeHome(mi);
   }

   /**
    * This method does invocation interpositioning of tx and security,
    * retrieves the instance from an object table, and invokes the method
    * on the particular instance
    */
   public Object invoke(Invocation mi)
      throws Exception
   {
      // Invoke through interceptors
      return getInterceptor().invoke(mi);
   }


   // EJBHome implementation ----------------------------------------

   public EJBObject createHome()
      throws java.rmi.RemoteException, CreateException
   {
      throw new Error("createHome not valid for MessageDriven beans");
   }


   public void removeHome(Handle handle)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("removeHome not valid for MessageDriven beans");
      // TODO
   }

   public void removeHome(Object primaryKey)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("removeHome not valid for MessageDriven beans");
      // TODO
   }

   public EJBMetaData getEJBMetaDataHome()
      throws java.rmi.RemoteException
   {
      // TODO
      //return null;
      throw new Error("getEJBMetaDataHome not valid for MessageDriven beans");
   }

   public HomeHandle getHomeHandleHome()
      throws java.rmi.RemoteException
   {
      // TODO
      //return null;
      throw new Error("getHomeHandleHome not valid for MessageDriven beans");
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

      public void create() {}
      public void start() {}
      public void stop() {}
      public void destroy() {}

      public Object invokeHome(Invocation mi)
         throws Exception
      {
         throw new Error("invokeHome not valid for MessageDriven beans");
      }
      
      /**
       * FIXME Design problem, who will do the acknowledging for
       * beans with bean managed transaction?? Probably best done in the
       * listener "proxys"
       */
      public Object invoke(Invocation mi)
         throws Exception
      {
         EnterpriseContext ctx = (EnterpriseContext)mi.getEnterpriseContext();
         
         // wire the transaction on the context,
         // this is how the instance remember the tx
         if (ctx.getTransaction() == null) {
            ctx.setTransaction(mi.getTransaction());
         }

         // Get method and instance to invoke upon
         Method m = (Method)beanMapping.get(mi.getMethod());

         // we have a method that needs to be done by a bean instance
         try {
            return m.invoke(ctx.getInstance(), mi.getArguments());
         }
         catch (IllegalAccessException e) {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
         }
         catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            
            if (t instanceof RuntimeException) {
               if (t instanceof EJBException) {
                  throw (EJBException)t;
               }
               else {
                  // Transform runtime exception into what a bean *should* have thrown
                  throw new EJBException((RuntimeException)t);
               }
            }
            else if (t instanceof Exception) {
               throw (Exception)t;
            }
            else if (t instanceof Error) {
               throw (Error)t;
            }
            else {
               throw new org.jboss.util.NestedError("Unexpected Throwable", t);
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
