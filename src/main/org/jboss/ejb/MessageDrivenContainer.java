/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.metadata.ConfigurationMetaData;
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
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.29 $
 */
public class MessageDrivenContainer extends Container
   implements EJBProxyFactoryContainer
{
   /**
    * These are the mappings between the remote interface methods
    * and the bean methods.
    */
   protected Map beanMapping;

   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
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

   protected void createService() throws Exception
   {
      typeSpecificInitialize();

      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default init
         super.createService();
         // Map the bean methods
         Map map = new HashMap();
         Method m = MessageListener.class.getMethod("onMessage", new Class[] { Message.class });
         map.put(m, beanClass.getMethod(m.getName(), m.getParameterTypes()));
         log.debug("Mapped " + m.getName() + " " + m.hashCode() + " to " + map.get(m));
         if( TimedObject.class.isAssignableFrom( beanClass ) ) {
             // Map ejbTimeout
             map.put(
                TimedObject.class.getMethod( "ejbTimeout", new Class[] { Timer.class } ),
                beanClass.getMethod( "ejbTimeout", new Class[] { Timer.class } )
             );
         }
         beanMapping = map;

         // Initialize pool
         getInstancePool().create();

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

   protected void startService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default start
         super.startService();

         // Start container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.start();
         }
         
         // Start the instance pool
         getInstancePool().start();

      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void stopService() throws Exception
   {
      log.info("Stopping");
      
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());

      try
      {
         // Call default stop
         super.stopService();

         // Stop container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.stop();
         }

         // Stop the instance pool
         getInstancePool().stop();

      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void destroyService() throws Exception
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
         getInstancePool().destroy();
         getInstancePool().setContainer(null);

         // Destroy all the interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.destroy();
            in.setContainer(null);
            in = in.getNext();
         }

         // Call default destroy
         super.destroyService();
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * @throws UnsupportedOperationException Not valid for MDB
    */
   public Object invokeHome(Invocation mi) throws Exception
   {
      throw new UnsupportedOperationException(
            "invokeHome not valid for MessageDriven beans");
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

   // StatisticsProvider implementation ------------------------------------
   
   public void retrieveStatistics( List container, boolean reset ) {
      // Loop through all Interceptors and add statistics
      getInterceptor().retrieveStatistics( container, reset );
      if( !( getInstancePool() instanceof Interceptor ) ) {
         getInstancePool().retrieveStatistics( container, reset );
      }
   }

   //Moved from EjbModule-------------------
   /**
    * Describe <code>typeSpecificInitialize</code> method here.
    * MDB specific initialization.
    */
   protected void typeSpecificInitialize()  throws Exception
   {
      ClassLoader cl = getDeploymentInfo().ucl;
      ClassLoader localCl = getDeploymentInfo().localCl;
      int transType = getBeanMetaData().isContainerManagedTx() ? CMT : BMT;
      
      genericInitialize(transType, cl, localCl );
      createProxyFactories(cl);
      ConfigurationMetaData conf = getBeanMetaData().getContainerConfiguration();
      setInstancePool( createInstancePool( conf, cl ) );
   }


   //end moved from EjbModule---------------
   
   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor extends AbstractContainerInterceptor
   {
      /**
       * FIXME Design problem, who will do the acknowledging for
       * beans with bean managed transaction?? Probably best done in the
       * listener "proxys"
       */
      public InvocationResponse invoke(Invocation mi) throws Exception
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
            return new InvocationResponse(m.invoke(ctx.getInstance(), mi.getArguments()));
         }
         catch (Exception e) {
            rethrow(e);
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();         
      }
   }
}
