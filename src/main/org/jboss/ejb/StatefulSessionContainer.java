/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.util.MethodHashing;

/**
 * The container for <em>stateful</em> session beans.
 *
 * @version <tt>$Revision: 1.54 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class StatefulSessionContainer extends Container
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
   
   /** This is the persistence manager for this container */
   protected StatefulSessionPersistenceManager persistenceManager;
   
   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
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

         // Init container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.create();
         }

         // Init instance cache
         getInstanceCache().create();

         // Initialize pool
         getInstancePool().create();

         // Init persistence
         persistenceManager.create();

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

         // Start instance cache
         getInstanceCache().start();

         // Start pool
         getInstancePool().start();

         // Start persistence
         persistenceManager.start();

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

         // Stop instance cache
         getInstanceCache().stop();

         // Stop pool
         getInstancePool().stop();

         // Stop persistence
         persistenceManager.stop();

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
            ci.destroy();
            ci.setContainer(null);
         }

         // Destroy instance cache
         getInstanceCache().destroy();
         getInstanceCache().setContainer(null);

         // Destroy pool
         getInstancePool().destroy();
         getInstancePool().setContainer(null);

         // Destroy persistence
         persistenceManager.destroy();
         persistenceManager.setContainer(null);

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
   
   public void remove(Invocation mi)
      throws RemoteException, RemoveException
   {
      // 7.6 EJB2.0, it is illegal to remove a bean while in a transaction
      // if (((EnterpriseContext) mi.getEnterpriseContext()).getTransaction() != null)
      // throw new RemoveException("StatefulSession bean in transaction, cannot remove (EJB2.0 7.6)");

      // if the session is removed already then let the user know they have a problem
      if (((EnterpriseContext) mi.getEnterpriseContext()).getId() == null) {
         throw new RemoveException("SFSB has been removed already");
      }
      
      // Remove from storage
      getPersistenceManager().removeSession((StatefulSessionEnterpriseContext)mi.getEnterpriseContext());
      
      // We signify "removed" with a null id
      ((EnterpriseContext) mi.getEnterpriseContext()).setId(null);
   }
   
   /**
    * While the following methods are implemented in the client in the case
    * of JRMP we would need to implement them to fully support other transport
    * protocols
    *
    * @return  Always null
    */
   public Handle getHandle(Invocation mi) throws RemoteException
   {
      // TODO
      return null;
   }
   
   /**
    * @return  Always null
    */
   public Object getPrimaryKey(Invocation mi) throws RemoteException
   {
      // TODO
      return null;
   }
   
   public EJBHome getEJBHome(Invocation mi) throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null) {
         throw new IllegalStateException();
      }
      
      return (EJBHome) ci.getEJBHome();
   }
   
   /**
    * @return   Always false
    */
   public boolean isIdentical(Invocation mi) throws RemoteException
   {
      return false; // TODO
   }
   
   // Home interface implementation ---------------------------------

   private void createSession(final Method m,
                              final Object[] args,
                              final StatefulSessionEnterpriseContext ctx)
      throws Exception
   {
      boolean debug = log.isDebugEnabled();
      
      // Create a new ID and set it
      Object id = getPersistenceManager().createId(ctx);
      if (debug) {
         log.debug("Created new session ID: " + id);
      }
      ctx.setId(id);
        
      // Invoke ejbCreate()
      try
      {
         Method createMethod = getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
         if (debug) {
            log.debug("Using create method for session: " + createMethod);
         }
         
         createMethod.invoke(ctx.getInstance(), args);
      }
      catch (IllegalAccessException e)
      {
         ctx.setId(null);
         
         throw new EJBException(e);
      }
      catch (InvocationTargetException e)
      {
         ctx.setId(null);
            
         Throwable t = e.getTargetException();
         if (t instanceof RuntimeException)
         {
            if (t instanceof EJBException)
               throw (EJBException)t;
            // Wrap runtime exceptions
            throw new EJBException((Exception)t);
         }
         else if (t instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception)t;
         }
         else if (t instanceof Error)
         {
            throw (Error)t;
         }
         else {
            throw new org.jboss.util.UnexpectedThrowable(t);
         }
      }

      // call back to the PM to let it know that ejbCreate has been called with success
      getPersistenceManager().createdSession(ctx);

      // Insert in cache
      getInstanceCache().insert(ctx);

      // Create EJBObject
      if (getProxyFactory() != null)
         ctx.setEJBObject((EJBObject)getProxyFactory().getStatefulSessionEJBObject(id));
      
      // Create EJBLocalObject
      if (getLocalHomeClass() != null)
         ctx.setEJBLocalObject(getLocalProxyFactory().getStatefulSessionEJBLocalObject(id));
   }
   
   public EJBObject createHome(Invocation mi)
      throws Exception
   {
      createSession(mi.getMethod(), mi.getArguments(),
                    (StatefulSessionEnterpriseContext)mi.getEnterpriseContext());
      
      return ((StatefulSessionEnterpriseContext)mi.getEnterpriseContext()).getEJBObject();
   }
   
   // local object interface implementation
   
   public EJBLocalHome getEJBLocalHome(Invocation mi)
   {
      return localProxyFactory.getEJBLocalHome();
   }
   
   // local home interface implementation
   
   /**
    * @throws Error    Not yet implemented
    */
   public void removeLocalHome(Invocation mi)
      throws RemoteException, RemoveException
   {
      throw new Error("Not Yet Implemented");
   }
   
   public EJBLocalObject createLocalHome(Invocation mi)
      throws Exception
   {
      createSession(mi.getMethod(), mi.getArguments(),
                    (StatefulSessionEnterpriseContext)mi.getEnterpriseContext());
      
      return ((StatefulSessionEnterpriseContext)mi.getEnterpriseContext()).getEJBLocalObject();
   }
   
   /**
    * A method for the getEJBObject from the handle
    */
   public EJBObject getEJBObject(Invocation mi) throws RemoteException
   {
      // All we need is an EJBObject for this Id, the first argument is the Id
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null) {
         throw new IllegalStateException();
      }
      return (EJBObject) ci.getStatefulSessionEJBObject(mi.getArguments()[0]);
   }
   
   
   // EJBHome implementation ----------------------------------------
   
   //
   // These are implemented in the local proxy
   //
   
   /**
    * @throws Error    Not yet implemented
    */
   public void removeHome(Invocation mi)
      throws RemoteException, RemoveException
   {
      throw new Error("Not Yet Implemented");
   }
   
   public EJBMetaData getEJBMetaDataHome(Invocation mi)
      throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null) {
         throw new IllegalStateException();
      }
      
      return ci.getEJBMetaData();
   }
   
   /**
    * @throws Error    Not yet implemented
    */
   public HomeHandle getHomeHandleHome(Invocation mi)
      throws RemoteException
   {
      throw new Error("Not Yet Implemented");
   }
   
   // StatisticsProvider implementation ------------------------------------
   
   public void retrieveStatistics( List container, boolean reset ) {
      // Loop through all Interceptors and add statistics
      getInterceptor().retrieveStatistics( container, reset );
/* AS Method is not implemented in StatefulSessionPersistenceManager
      if( !( getPersistenceManager() instanceof Interceptor ) ) {
         getPersistenceManager().retrieveStatistics( container, reset );
      }
*/
      if( !( getInstancePool() instanceof Interceptor ) ) {
         getInstancePool().retrieveStatistics( container, reset );
      }
   }
   
   // Private -------------------------------------------------------
   
   protected void setupHomeMapping() throws Exception
   {
      // Adrian Brock: This should go away when we don't support EJB1x
      boolean isEJB1x = metaData.getApplicationMetaData().isEJB1x();

      Map map = new HashMap();
      
      if (homeInterface != null)
      {

         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            try
            {
               // Implemented by container
               if (isEJB1x == false && m[i].getName().startsWith("create")) {
                  map.put(m[i], getClass().getMethod("createHome",
                                                     new Class[] { Invocation.class }));
               }
               else {
                  map.put(m[i], getClass().getMethod(m[i].getName()+"Home",
                                                     new Class[] { Invocation.class }));
               }
            }
            catch (NoSuchMethodException e)
            {
               log.info(m[i].getName() + " in bean has not been mapped");
            }
         }
      }
      
      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            try
            {
               // Implemented by container
               if (isEJB1x == false && m[i].getName().startsWith("create")) {
                  map.put(m[i], getClass().getMethod("createLocalHome",
                                                     new Class[] { Invocation.class }));
               }
               else {
                  map.put(m[i], getClass().getMethod(m[i].getName()+"LocalHome",
                                                     new Class[] { Invocation.class }));
               }
            }
            catch (NoSuchMethodException e)
            {
               log.info(m[i].getName() + " in bean has not been mapped");
            }
         }
      }
      
      try
      {
         // Get getEJBObject from on Handle, first get the class
         Class handleClass = Class.forName("javax.ejb.Handle");
         
         //Get only the one called handle.getEJBObject
         Method getEJBObjectMethod = handleClass.getMethod("getEJBObject", new Class[0]);
         
         //Map it in the home stuff
         map.put(getEJBObjectMethod, getClass().getMethod("getEJBObject",
                                                          new Class[] {Invocation.class}));
      }
      catch (NoSuchMethodException e)
      {
         log.debug("Couldn't find getEJBObject method on container");
      }
      
      homeMapping = map;
   }
   
   
   private void setUpBeanMappingImpl(Map map,
                                     Method[] m,
                                     String declaringClass)
      throws NoSuchMethodException
   {
      for (int i = 0; i < m.length; i++)
      {
         if (!m[i].getDeclaringClass().getName().equals(declaringClass))
         {
            // Implemented by bean
            map.put(m[i], beanClass.getMethod(m[i].getName(),
                  m[i].getParameterTypes()));
         }
         else
         {
            try
            {
               // Implemented by container
               map.put(m[i], getClass().getMethod(m[i].getName(),
                     new Class[]
                     { Invocation.class }));
            } catch (NoSuchMethodException e)
            {
               log.error(m[i].getName() + " in bean has not been mapped", e);
            }
         }
      }
   }
   
   protected void setupBeanMapping() throws NoSuchMethodException
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
   
   protected Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }
   
   /**
    * Describe <code>typeSpecificInitialize</code> method here.
    * stateful session specific initialization.
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
      setInstanceCache( createInstanceCache( conf, false, cl ) );
      setInstancePool(new StatefulSessionInstancePool());
      // Set persistence manager
      setPersistenceManager( (StatefulSessionPersistenceManager) cl.loadClass( conf.getPersistenceManager() ).newInstance() );
      //Set the bean Lock Manager
      setLockManager(createBeanLockManager(false, conf.getLockConfig(), cl));
      
   }

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor extends AbstractContainerInterceptor
   {
      public Object invoke(Invocation mi) throws Exception
      {
         if(mi.getType().isHome())
         {
            boolean trace = log.isTraceEnabled();

            if (trace)
            {
               log.trace("HOMEMETHOD coming in ");
               log.trace(""+mi.getMethod());
               log.trace("HOMEMETHOD coming in hashcode"+mi.getMethod().hashCode());
               log.trace("HOMEMETHOD coming in classloader"+mi.getMethod().getDeclaringClass().getClassLoader().hashCode());
               log.trace("CONTAINS "+homeMapping.containsKey(mi.getMethod()));
            }

            Method m = (Method)homeMapping.get(mi.getMethod());
            // Invoke and handle exceptions

            if (trace)
            {
               log.trace("HOMEMETHOD m "+m);
               java.util.Iterator iterator = homeMapping.keySet().iterator();
               while(iterator.hasNext()) 
               {
                  Method me = (Method) iterator.next();

                  if (me.getName().endsWith("create")) 
                  {
                     log.trace(me.toString());
                     log.trace(""+me.hashCode());
                     log.trace(""+me.getDeclaringClass().getClassLoader().hashCode());
                     log.trace("equals "+me.equals(mi.getMethod())+ " "+mi.getMethod().equals(me));
                  }
               }
            }

            try
            {
               return m.invoke(StatefulSessionContainer.this, new Object[] { mi });
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
            // remember the tx. Unlike Entity beans we can't do that in the 
            // previous interceptors (ordering)
            if (((EnterpriseContext) mi.getEnterpriseContext()).getTransaction() == null)
               ((EnterpriseContext) mi.getEnterpriseContext()).setTransaction(mi.getTransaction());

            // Get method
            Method m = (Method)beanMapping.get(mi.getMethod());

            // log.info("METHOD coming in "+mi.getMethod());
            // log.info("METHOD m "+m);

            // Select instance to invoke (container or bean)
            if (m.getDeclaringClass().equals(StatefulSessionContainer.this.getClass()))
            {
               // Invoke and handle exceptions
               try
               {
                  return m.invoke(StatefulSessionContainer.this, new Object[] { mi });
               }
               catch (Exception e)
               {
                  rethrow(e);
               }
            }
            else
            {
               // Invoke and handle exceptions
               try
               {
                  return m.invoke(((EnterpriseContext) mi.getEnterpriseContext()).getInstance(), mi.getArguments());
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
