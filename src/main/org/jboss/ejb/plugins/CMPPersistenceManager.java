/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EntityBean;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;


import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.metadata.EntityMetaData;

import org.jboss.util.Sync;

import org.jboss.management.j2ee.SampleData;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * The CMP Persistence Manager implements the semantics of the CMP
 * EJB 1.1 call back specification.
 *
 * This Manager works with a "EntityPersistenceStore" that takes care of the
 * physical storing of instances (JAWS, JDBC O/R, FILE, Object).
 *
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.44 $
 *
 *  <p><b>Revisions:</b>
 *  <p><b>20010621 Bill Burke:</b>
 *  <ul>
 *  <li>- removed loadEntities call because CMP read-ahead is now
 *        done directly by the finder.
 *  </ul>
 *  <p><b>20010709 Andreas Schaefer:</b>
 *  <ul>
 *  <li>- added statistics gathering
 *  </ul>
 *  <p><b>20011201 Dain Sundstrom:</b>
 *  <ul>
 *  <li>- moved createBeanInstance and initEntity back into
 *        the persistence store.
 *  </ul>
 *  <p><b>20020413 dain sundstrom:</b>
 *  <ul>
 *  <li>- Moved ejbPostCreate call to postCreateEntity method
 *  </ul>
 */
public class CMPPersistenceManager
   implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   EntityContainer con;
   // Physical persistence implementation
   EntityPersistenceStore store;

   // The EJB Methods, the reason for this class
   Method ejbLoad;
   Method ejbStore;
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;

   HashMap createMethods = new HashMap();
   HashMap postCreateMethods = new HashMap();
   private int commitOption;

   private SampleData createCount = new SampleData( "CreateCount", SampleData.COUNT );
   private SampleData removeCount = new SampleData( "CreateCount", SampleData.COUNT );
   private SampleData readyRange = new SampleData( "CreateCount", SampleData.RANGE );
   private SampleData pooledRange = new SampleData( "CreateCount", SampleData.RANGE );
   
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (EntityContainer)c;
      if (store != null)
         store.setContainer(c);
      if( con != null )
      {
         ConfigurationMetaData configuration = con.getBeanMetaData().getContainerConfiguration();
         commitOption = configuration.getCommitOption();
      }
   }

   /**
    * Gets the entity persistence store.
    */
   public EntityPersistenceStore getPersistenceStore()
   {
      return store;
   }

   public void setPersistenceStore(EntityPersistenceStore store)
   {
      this.store= store;
      
      //Give it the container
      if (con!= null) store.setContainer(con);
   }

   public void create()
      throws Exception
   {
      // The common EJB methods
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
      
      if (con.getHomeClass() != null)
      {
         Method[] methods = con.getHomeClass().getMethods();
         createMethodCache( methods );
      }
      if (con.getLocalHomeClass() != null)
      {
         Method[] methods = con.getLocalHomeClass().getMethods();
         createMethodCache( methods );
      }
      
      store.create();
   }

   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    *
    * @return the new instance
    */
   public Object createBeanClassInstance() throws Exception
   {
      return store.createBeanClassInstance();
   }

   private void createMethodCache( Method[] methods )
      throws NoSuchMethodException
   {
      // Create cache of create methods
      Class beanClass = con.getBeanClass();
      for (int i = 0; i < methods.length; i++)
      {
         String name = methods[i].getName();
         if (name.startsWith("create"))
         {
            Class[] types = methods[i].getParameterTypes();
            try
            {
               String nameSuffix = name.substring(0, 1).toUpperCase() + name.substring(1);
               Method beanMethod = beanClass.getMethod("ejb" + nameSuffix, types);
               createMethods.put(methods[i], beanMethod);
               beanMethod =  beanClass.getMethod("ejbPost" + nameSuffix, types);
               postCreateMethods.put(methods[i], beanMethod);
            }
            catch (NoSuchMethodException nsme)
            {
               throw new NoSuchMethodException("Can't find ejb[Post]Create in "+beanClass.getName());
            }
         }
      }
   }

   public void start()
      throws Exception
   {
      store.start();
   }
   
   public void stop()
   {
      store.stop();
   }
   
   public void destroy()
   {
      store.destroy();
   }
   
   public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws Exception
   {
      // Deligate initialization of bean to persistence store
      store.initEntity(ctx);

      // Call ejbCreate on the target bean
      try
      {
         Method createMethod = (Method)createMethods.get(m);
         createMethod.invoke(ctx.getInstance(), args);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if(e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
         else if(e instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception)e;
         }
         else
         {
            throw (Error)e;
         }
      }

      // Have the store persist the new instance, the return is the key
      Object id = store.createEntity(m, args, ctx);
      
      // Set the key on the target context
      ctx.setId(id);
      
      // Create a new CacheKey
      Object cacheKey = ((EntityCache) con.getInstanceCache()).createCacheKey( id );
      
      // Give it to the context
      ctx.setCacheKey(cacheKey);
      
      // Create EJBObject
      if (con.getProxyFactory() != null)
      {
         ctx.setEJBObject((EJBObject) con.getProxyFactory().getEntityEJBObject(cacheKey));
      }
      if (con.getLocalHomeClass() != null)
      {
         ctx.setEJBLocalObject(con.getLocalProxyFactory().getEntityEJBLocalObject(cacheKey));
      }
      createCount.add();
   }

   public void postCreateEntity(
         Method m, 
         Object[] args, 
         EntityEnterpriseContext ctx)
      throws Exception
   {
      try
      {
         Method postCreateMethod = (Method)postCreateMethods.get(m);
         postCreateMethod.invoke(ctx.getInstance(), args);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
         else if (e instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception)e;
         }
         else
         {
            throw (Error)e;
         }
      }
   }

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws Exception
   {
      // For now only optimize fBPK
      if (finderMethod.getName().equals("findByPrimaryKey")
          && commitOption != ConfigurationMetaData.B_COMMIT_OPTION
          && commitOption != ConfigurationMetaData.C_COMMIT_OPTION)
      {
         Object key = ctx.getCacheKey();
         if (key == null)
         {
            key = ((EntityCache)con.getInstanceCache()).createCacheKey(args[0]);
         }
         if (con.getInstanceCache().isActive(key))
         {
            return key; // Object is active -> it exists -> no need to call finder
         }
      }

      // The store will find the entity and return the primaryKey
      Object id = store.findEntity(finderMethod, args, ctx);

      // We return the cache key
      return ((EntityCache) con.getInstanceCache()).createCacheKey(id);
   }
   
   /** find multiple entities */
   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws Exception
   {
      // return the finderResults so that the invoker layer can extend this back
      // giving the client an OO 'cursor'
      return store.findEntities(finderMethod, args, ctx);
   }

   /*
    * activateEntity(EnterpriseContext ctx)
    *
    * The method calls the target beans for spec compliant callbacks.
    * Since these are pure EJB calls it is not obvious that the store should
    * expose the interfaces.  In case of jaws however we found that store specific
    * contexts could be set in the activateEntity calls and hence a propagation of
    * the call made sense.  The persistence store is called for "extension" purposes.
    *
    * @see activateEntity on EntityPersistenceStore.java
    */
   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      
      // Call bean
      try
      {
         ejbActivate.invoke(ctx.getInstance(), new Object[0]);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof RemoteException)
         {
            // Rethrow exception
            throw (RemoteException)e;
         }
         else if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
      }
      
      // The implementation of the call can be left absolutely empty, the 
      // propagation of the call is just a notification for stores that would
      // need to know that an instance is being activated
      store.activateEntity(ctx);
   }

   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      
      // Have the store load the fields of the instance
      store.loadEntity(ctx);
      
      invokeLoad(ctx);
   }
   
   public boolean isModified(EntityEnterpriseContext ctx) throws Exception
   {
      return store.isModified(ctx);
   }

   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      //      Logger.debug("Store entity");
      try
      {
         
         // Prepare the instance for storage
         ejbStore.invoke(ctx.getInstance(), new Object[0]);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof RemoteException)
         {
            // Rethrow exception
            throw (RemoteException)e;
         }
         else if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
      }
      
      // Have the store deal with storing the fields of the instance
      store.storeEntity(ctx);
      
   }
   
   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      
      try
      {
         // Prepare the instance for passivation
         ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof RemoteException)
         {
            // Rethrow exception
            throw (RemoteException)e;
         }
         else if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
      }

      store.passivateEntity(ctx);
   }

   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      try
      {
         
         // Call ejbRemove
         ejbRemove.invoke(ctx.getInstance(), new Object[0]);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof RemoveException)
         {
            // Rethrow exception
            throw (RemoveException)e;
         }
         else if (e instanceof RemoteException)
         {
            // Rethrow exception
            throw (RemoteException)e;
         }
         else if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
      }
      
      store.removeEntity(ctx);
      removeCount.add();
   }

   protected void invokeLoad(EntityEnterpriseContext ctx) throws RemoteException
   {
      try
      {
         // Call ejbLoad on bean instance, wake up!
         ejbLoad.invoke(ctx.getInstance(), new Object[0]);
         
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof RemoteException)
         {
            // Rethrow exception
            throw (RemoteException)e;
         }
         else if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
      }
   }

   // StatisticsProvider implementation ------------------------------------
   
   public void retrieveStatistics( List container, boolean reset ) {
      container.add( createCount );
      container.add( removeCount );
      container.add( readyRange );
      container.add( pooledRange );
      if( reset ) {
         createCount.reset();
         removeCount.reset();
         readyRange.reset();
         pooledRange.reset();
      }
   }
   
   // Z implementation ----------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
