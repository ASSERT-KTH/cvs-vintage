/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.util.MethodHashing;

/**
 * The container for <em>stateless</em> session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.44 $
 */
public class StatelessSessionContainer extends Container
   implements EJBProxyFactoryContainer
{
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
   
   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
   }
   
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
         // Acquire classes from CL
         if (metaData.getHome() != null)
            homeInterface = classLoader.loadClass(metaData.getHome());
         if (metaData.getRemote() != null)
            remoteInterface = classLoader.loadClass(metaData.getRemote());

         // Map the bean methods
         setupBeanMapping();

         // Map the home methods
         setupHomeMapping();

         // Map the interfaces to Long
         setupMarshalledInvocationMapping();

         // Initialize pool
         getInstancePool().create();

         // Init container invoker
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
      
      stopTimers();
      try
      {
         // Destroy container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.destroy();
            ci.setContainer(null);
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
   
   // EJBObject implementation --------------------------------------
   
   /**
    * No-op.
    */
   public void remove(Invocation mi)
      throws RemoteException, RemoveException
   {
      //TODO
   }
   
   /**
    * @return    Always null
    */
   public Handle getHandle(Invocation mi)
      throws RemoteException
   {
      // TODO
      return null;
   }
   
   /**
    * @return    Always null
    */
   public Object getPrimaryKey(Invocation mi)
      throws RemoteException
   {
      // TODO
      return null;
   }
   
   public EJBHome getEJBHome(Invocation mi)
      throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null) {
         throw new IllegalStateException();
      }
      return (EJBHome) ci.getEJBHome();
   }
   
   /**
    * @return Always false
    */
   public boolean isIdentical(Invocation mi)
      throws RemoteException
   {
      return false; // TODO
   }
   
   public EJBLocalHome getEJBLocalHome(Invocation mi)
   {
      return localProxyFactory.getEJBLocalHome();
   }
   
   public EJBLocalObject createLocalHome()
      throws CreateException
   {
      if (localProxyFactory == null)
         throw new IllegalStateException();
      return localProxyFactory.getStatelessSessionEJBLocalObject();
   }
   
   /**
    * No-op.
    */
   public void removeLocalHome(Object primaryKey)
   {
      // todo
   }
   
   public EJBObject createHome()
      throws RemoteException, CreateException
   {
       EJBProxyFactory ci = getProxyFactory();
      if (ci == null) {
         throw new IllegalStateException();
      }
      Object obj = ci.getStatelessSessionEJBObject();
      return (EJBObject)obj;
   }
   
   /**
    * No-op.
    */
   public void removeHome(Handle handle)
      throws RemoteException, RemoveException
   {
      // TODO
   }
   
   /**
    * No-op.
    */
   public void removeHome(Object primaryKey)
      throws RemoteException, RemoveException
   {
      // TODO
   }
   
   /**
    * @return    Always null.
    */
   public EJBMetaData getEJBMetaDataHome()
      throws RemoteException
   {
      // TODO
      return null;
   }
   
   /**
    * @return    Always null.
    */
   public HomeHandle getHomeHandleHome()
      throws RemoteException
   {
      // TODO
      return null;
   }
   
   public void retrieveStatistics( List container, boolean reset ) {
      // Loop through all Interceptors and add statistics
      getInterceptor().retrieveStatistics( container, reset );
      if( !( getInstancePool() instanceof Interceptor ) ) {
         getInstancePool().retrieveStatistics( container, reset );
      }
   }
   
   protected void setupHomeMapping()
      throws NoSuchMethodException
   {
      boolean debug = log.isDebugEnabled();

      Map map = new HashMap();

      if (homeInterface != null)
      {
         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            // Implemented by container
            if (debug)
               log.debug("Mapping "+m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName()+"Home", m[i].getParameterTypes()));
         }
      }
      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            // Implemented by container
            if (debug)
               log.debug("Mapping "+m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName()+"LocalHome", m[i].getParameterTypes()));
         }
      }

      homeMapping = map;
   }

   private void setUpBeanMappingImpl( Map map, Method[] m, String declaringClass )
      throws NoSuchMethodException
   {
      boolean debug = log.isDebugEnabled();

      for (int i = 0; i < m.length; i++)
      {
         if (!m[i].getDeclaringClass().getName().equals(declaringClass))
         {
            // Implemented by bean
            try {
               map.put(m[i], beanClass.getMethod(m[i].getName(), m[i].getParameterTypes()));
            }
            catch (NoSuchMethodException ex)
            {
               throw new org.jboss.util.NoSuchMethodException("Not found in bean class: ", m[i]);
            }
            
            if (debug)
               log.debug("Mapped "+m[i].getName()+" "+m[i].hashCode()+"to "+map.get(m[i]));
         }
         else
         {
            try
            {
               // Implemented by container
               if (debug)
                  log.debug("Mapped Container method "+m[i].getName() +" HASH "+m[i].hashCode());
               map.put(m[i], getClass().getMethod(m[i].getName(), new Class[] { Invocation.class }));
            }
            catch (NoSuchMethodException e)
            {
               log.error(m[i].getName() + " in bean has not been mapped", e);
            }
         }
      }
   }

   protected void setupBeanMapping()
      throws NoSuchMethodException
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
      if( TimedObject.class.isAssignableFrom( beanClass ) ) {
          // Map ejbTimeout
          map.put(
             TimedObject.class.getMethod( "ejbTimeout", new Class[] { Timer.class } ),
             beanClass.getMethod( "ejbTimeout", new Class[] { Timer.class } )
          );
      }
      
      beanMapping = map;
   }
   
   protected void setupMarshalledInvocationMapping() throws Exception
   {
      // Create method mappings for container invoker
      if (homeInterface != null) 
      {
         Method [] m = homeInterface.getMethods();
         for (int i = 0 ; i<m.length ; i++)
         {
            marshalledInvocationMapping.put( new Long(MethodHashing.calculateHash(m[i])), m[i]);
         }
      }
      
      if (remoteInterface != null)
      {
         Method [] m = remoteInterface.getMethods();
         for (int j = 0 ; j<m.length ; j++)
         {
            marshalledInvocationMapping.put( new Long(MethodHashing.calculateHash(m[j])), m[j]);
         }
      }
         
      // Get the getEJBObjectMethod
      Method getEJBObjectMethod = Class.forName("javax.ejb.Handle").getMethod("getEJBObject", new Class[0]);
      
      // Hash it
      marshalledInvocationMapping.put(new Long(MethodHashing.calculateHash(getEJBObjectMethod)),getEJBObjectMethod);
   }
   
   Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }
   
   //Moved from EjbModule-------------------
   /**
    * Describe <code>typeSpecificInitialize</code> method here.
    * stateless session specific initialization.
    */
   protected void typeSpecificInitialize()  throws Exception
   {
      ClassLoader cl = getDeploymentInfo().ucl;
      ClassLoader localCl = getDeploymentInfo().localCl;
      int transType = getBeanMetaData().isContainerManagedTx() ? CMT : BMT;
      
      genericInitialize(transType, cl, localCl );
      if (getBeanMetaData().getHome() != null)
      {
         createProxyFactories(cl);
      }
      ConfigurationMetaData conf = getBeanMetaData().getContainerConfiguration();
      setInstancePool( createInstancePool( conf, cl ) );
   }


   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor extends AbstractContainerInterceptor
   {
      public InvocationResponse invoke(Invocation mi) throws Exception
      {
         if(mi.getType().isHome())
         {
            Method m = (Method)homeMapping.get(mi.getMethod());

            try
            {
               return new InvocationResponse(m.invoke(StatelessSessionContainer.this, mi.getArguments()));
            }
            catch (Exception e)
            {
               rethrow(e);
            }

            // We will never get this far, but the compiler does not know that
            throw new org.jboss.util.UnreachableStatementException();         
         }
         else
         {
            // wire the transaction on the context, this is how the instance 
            // remember the tx
            if (((EnterpriseContext) mi.getEnterpriseContext()).getTransaction() == null)
               ((EnterpriseContext) mi.getEnterpriseContext()).setTransaction(mi.getTransaction());

            // Get method and instance to invoke upon
            Method m = (Method)beanMapping.get(mi.getMethod());

            //If we have a method that needs to be done by the container 
            // (EJBObject methods)
            if (m.getDeclaringClass().equals(StatelessSessionContainer.class))
            {
               try
               {
                  return new InvocationResponse(m.invoke(StatelessSessionContainer.this, new Object[] { mi }));
               }
               catch (Exception e)
               {
                  rethrow(e);
               }
            }
            else // we have a method that needs to be done by a bean instance
            {
               // Invoke and handle exceptions
               try
               {
                  return new InvocationResponse(m.invoke(((EnterpriseContext) mi.getEnterpriseContext()).getInstance(), mi.getArguments()));
               }
               catch (Exception e)
               {
                  rethrow(e);
               }
            }

            // We will never get this far, but the compiler does not know that
            throw new org.jboss.util.UnreachableStatementException();         
         }
      }
   }
}
