/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.rmi.RemoteException;

import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.management.ObjectName;

import org.jboss.invocation.Invocation;
import org.jboss.ejb.txtimer.TimedObjectInvoker;

/**
 * The container for <em>stateful</em> session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version <tt>$Revision: 1.73 $</tt>
 *
 * @jmx:mbean extends="org.jboss.ejb.ContainerMBean"
 */

public class StatefulSessionContainer
   extends SessionContainer
   implements EJBProxyFactoryContainer, InstancePoolContainer
{
   /** This is the persistence manager for this container */
   protected StatefulSessionPersistenceManager persistenceManager;

   /** The instance cache. */
   protected InstanceCache instanceCache;

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
   
   /** creates and registers the instance cache */
   protected void createInstanceCache() throws Exception {
      // Try to register the instance cache as an MBean
      try
      {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "cache");
         ObjectName cacheName = new ObjectName(containerName.getDomain(), props);
         server.registerMBean(instanceCache, cacheName);
      }
      catch(Throwable t)
      {
         log.debug("Failed to register cache as mbean", t);
      }
      // Init instance cache
      instanceCache.create();
   }

   /** create persistence manager */
   protected void createPersistenceManager() throws Exception {
      persistenceManager.create();
   }

   /** Start persistence */
   protected void startPersistenceManager() throws Exception {
      persistenceManager.start();
   }

   /** Start instance cache */
   protected void startInstanceCache() throws Exception {
      instanceCache.start();
   }

   /** Stop persistence */
   protected void stopPersistenceManager() {
      persistenceManager.stop();
   }

   /** Stop instance cache */
   protected void stopInstanceCache() {
      instanceCache.stop();
   }

   protected void destroyPersistenceManager() {
        // Destroy persistence
         persistenceManager.destroy();
         persistenceManager.setContainer(null);
   }

   protected void destroyInstanceCache() {
      // Destroy instance cache
      instanceCache.destroy();
      instanceCache.setContainer(null);
      try
      {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "cache");
         ObjectName cacheName = new ObjectName(containerName.getDomain(), props);
         server.unregisterMBean(cacheName);
      }
      catch(Throwable ignore)
      {
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
      StatefulSessionEnterpriseContext ctx = (StatefulSessionEnterpriseContext)mi.getEnterpriseContext();
      if (ctx.getId() == null)
      {
         throw new RemoveException("SFSB has been removed already");
      }

      // Remove from storage
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_REMOVE);
         getPersistenceManager().removeSession(ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      // We signify "removed" with a null id
      ctx.setId(null);
      removeCount ++;
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

      // Invoke ejbCreate<METHOD>()
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_CREATE);

         // Build the ejbCreate<METHOD> from the home create<METHOD> sig
         String createName = m.getName();
         Object instance = ctx.getInstance();
         String ejbCreateName = "ejbC" + createName.substring(1);
         Method createMethod = instance.getClass().getMethod(ejbCreateName, m.getParameterTypes());
         if (debug) {
            log.debug("Using create method for session: " + createMethod);
         }
         createMethod.invoke(instance, args);
         createCount++;
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
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
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
      StatefulSessionEnterpriseContext ctx = (StatefulSessionEnterpriseContext) mi.getEnterpriseContext();
      createSession(mi.getMethod(), mi.getArguments(), ctx);
      return ctx.getEJBObject();
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
      StatefulSessionEnterpriseContext ctx = (StatefulSessionEnterpriseContext) mi.getEnterpriseContext();
      createSession(mi.getMethod(), mi.getArguments(), ctx);
      return ctx.getEJBLocalObject();
   }

   /**
    * A method for the getEJBObject from the handle
    */
   public EJBObject getEJBObject(Invocation mi) throws RemoteException
   {
      // All we need is an EJBObject for this Id, the first argument is the Id
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      Object id = mi.getArguments()[0];
      if (id == null)
         throw new IllegalStateException("Cannot get a session interface with a null id");

      // Does the session still exist?
      InstanceCache cache = getInstanceCache();
      BeanLock lock = getLockManager().getLock(id);
      lock.sync();
      try
      {
         if (cache.get(id) == null)
            throw new RemoteException("Session no longer exists: " + id);
      }
      finally
      {
         lock.releaseSync();
         getLockManager().removeLockRef(id);
      }

      // Ok lets create the proxy
      return (EJBObject) ci.getStatefulSessionEJBObject(id);
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
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
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

   protected Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor
      extends AbstractContainerInterceptor
   {
      public Object invokeHome(Invocation mi) throws Exception
      {
         boolean trace = log.isTraceEnabled();

         if (trace)
         {
            log.trace("HOMEMETHOD coming in ");
            log.trace(""+mi.getMethod());
            log.trace("HOMEMETHOD coming in hashcode"+mi.getMethod().hashCode());
            log.trace("HOMEMETHOD coming in classloader"+mi.getMethod().getDeclaringClass().getClassLoader().hashCode());
            log.trace("CONTAINS "+getHomeMapping().containsKey(mi.getMethod()));
         }

         Method miMethod = mi.getMethod();
         Method m = (Method) getHomeMapping().get(miMethod);
         if( m == null )
         {
            String msg = "Invalid invocation, check your deployment packaging"
               +", method="+miMethod;
            throw new EJBException(msg);
         }

         // Invoke and handle exceptions
         if (trace)
         {
            log.trace("HOMEMETHOD m "+m);
            java.util.Iterator iterator = getHomeMapping().keySet().iterator();
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
            return mi.performCall(StatefulSessionContainer.this, m, new Object[] { mi });
         }
         catch (Exception e)
         {
            rethrow(e);
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }

      public Object invoke(Invocation mi) throws Exception
      {
         // wire the transaction on the context, this is how the instance remember the tx
         // Unlike Entity beans we can't do that in the previous interceptors (ordering)
         EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
         if (ctx.getTransaction() == null)
            ctx.setTransaction(mi.getTransaction());

         // Get method
         Method miMethod = mi.getMethod();
         Method m = (Method) getBeanMapping().get(miMethod);
         if( m == null )
         {
            String msg = "Invalid invocation, check your deployment packaging"
               +", method="+miMethod;
            throw new EJBException(msg);
         }

         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(StatefulSessionContainer.class)
               || m.getDeclaringClass().equals(SessionContainer.class))
         {
            // Invoke and handle exceptions
            try
            {
               return mi.performCall(StatefulSessionContainer.this, m, new Object[]{mi});
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
               Object bean = ctx.getInstance();
               return mi.performCall(bean, m, mi.getArguments());
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
