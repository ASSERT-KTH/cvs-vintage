/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.logging.Logger;
import org.jboss.management.j2ee.CountStatistic;
import org.jboss.management.j2ee.TimeStatistic;

/**
*   <description>
*
*  @see <related>
*  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*  @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
*  @version $Revision: 1.36 $
*
*  <p><b>Revisions:</b>
*  <p><b>20010709 andreas schaefer:</b>
*  <ul>
*  <li>- Added statistics gathering
*  </ul>
*  <p><b>20010801 marc fleury:</b>
*  <ul>
*  <li>- insertion in cache upon create in now done in the instance interceptor
*  </ul>
*/
public class BMPPersistenceManager
implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   Logger log = Logger.getLogger(BMPPersistenceManager.class);

   EntityContainer con;

   Method ejbLoad;
   Method ejbStore;
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;

   /**
    *  Optional isModified method used by storeEntity
    */
   Method isModified;

   HashMap createMethods = new HashMap();
   HashMap postCreateMethods = new HashMap();
   HashMap finderMethods = new HashMap();

   private CountStatistic mCreate = new CountStatistic( "Create", "", "EJBs created" );
   private CountStatistic mRemove = new CountStatistic( "Remove", "", "EJBs removed" );
   private CountStatistic mActiveBean = new CountStatistic( "ActiveBean", "", "Numbers of active EJBs" );
   private TimeStatistic mActivate = new TimeStatistic( "Activation", "ms", "Activation Time" );
   private TimeStatistic mPassivate = new TimeStatistic( "Passivation", "ms", "Passivation Time" );
   private TimeStatistic mLoad = new TimeStatistic( "Load", "ms", "Load Time" );
   private TimeStatistic mStore = new TimeStatistic( "Store", "ms", "Load Time" );

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (EntityContainer)c;
   }

   public void create()
   throws Exception
   {
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);

      // Create cache of create methods
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

      try
      {
         isModified = con.getBeanClass().getMethod("isModified", new Class[0]);
         if (!isModified.getReturnType().equals(Boolean.TYPE))
            isModified = null; // Has to have "boolean" as return type!
      }
      catch (NoSuchMethodException ignored) {}
   }

    /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    *
    * @return the new instance
    */
    public Object createBeanClassInstance() throws Exception {
        return con.getBeanClass().newInstance();
    }

   private void createMethodCache( Method[] methods )
      throws NoSuchMethodException
   {
      for (int i = 0; i < methods.length; i++)
      {
         String name = methods[i].getName();
         if (name.startsWith("create"))
         {
            String nameSuffix = name.substring(0, 1).toUpperCase() + name.substring(1);
            try
            {
               createMethods.put(methods[i], con.getBeanClass().getMethod("ejb" + nameSuffix, methods[i].getParameterTypes()));
            }
            catch (NoSuchMethodException e)
            {
               log.error("Home Method " + methods[i] + " not implemented in bean class");
               throw e;
            }
            try
            {
               postCreateMethods.put(methods[i], con.getBeanClass().getMethod("ejbPost" + nameSuffix, methods[i].getParameterTypes()));
            }
            catch (NoSuchMethodException e)
            {
               log.error("Home Method " + methods[i] + " not implemented in bean class");
               throw e;
            }
         }
      }

      // Create cache of finder methods
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].getName().startsWith("find"))
         {
            try
            {
               finderMethods.put(methods[i], con.getBeanClass().getMethod("ejbF" + methods[i].getName().substring(1), methods[i].getParameterTypes()));
            }
            catch (NoSuchMethodException e)
            {
               log.error("Home Method " + methods[i] + " not implemented in bean class");
               throw e;
            }
         }
      }
   }

   public void start()
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
   }

   public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
   throws Exception
   {
      try {
         Method createMethod = (Method)createMethods.get(m);
         Method postCreateMethod = (Method)postCreateMethods.get(m);

         Object id = null;
         try
         {
            // Call ejbCreate<METHOD)
            id = createMethod.invoke(ctx.getInstance(), args);
         } catch (IllegalAccessException e)
         {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
         } catch (InvocationTargetException ite)
         {
            Throwable e = ite.getTargetException();
            if (e instanceof CreateException)
            {
               // Rethrow exception
               throw (CreateException)e;
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
            else if(e instanceof Exception)
            {
               throw (Exception)e;
            }
            else
            {
               throw (Error)e;
            }
         }

         // set the id
         ctx.setId(id);

         // Create a new CacheKey
         Object cacheKey = ((EntityCache) con.getInstanceCache()).createCacheKey( id );

         // Give it to the context
         ctx.setCacheKey(cacheKey);

         // Create EJBObject
           // Create EJBObject
        if (con.getContainerInvoker() != null)
         ctx.setEJBObject((EJBObject) con.getContainerInvoker().getEntityEJBObject(cacheKey));
        if (con.getLocalHomeClass() != null)
         ctx.setEJBLocalObject(con.getLocalContainerInvoker().getEntityEJBLocalObject(cacheKey));

         try
         {
            postCreateMethod.invoke(ctx.getInstance(), args);
         } catch (IllegalAccessException e)
         {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
         } catch (InvocationTargetException ite)
         {
            Throwable e = ite.getTargetException();
            if (e instanceof CreateException)
            {
               // Rethrow exception
               throw (CreateException)e;
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
            else if(e instanceof Exception)
            {
               throw (Exception)e;
            }
            else
            {
               throw (Error)e;
            }
         }
      }
      finally {
         mCreate.add();
      }
   }

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
   throws Exception
   {
      // call the finder method
      Object objectId = callFinderMethod(finderMethod, args, ctx);

      // get the cache, create a new key and return this new key
      return ((EntityCache)con.getInstanceCache()).createCacheKey( objectId );
   }

   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
   throws Exception
   {
      // call the finder method
      Object result = callFinderMethod(finderMethod, args, ctx);

      if (result == null)
      {
         // for EJB 1.0 compliance
         // if the bean couldn't find any matching entities
         // it returns null, so we return an empty collection
         return new ArrayList();
      }

      if (result instanceof java.util.Enumeration)
      {
         // to preserve 1.0 spec compatiblity
         ArrayList array = new ArrayList();
         Enumeration enum = (Enumeration) result;
         while (enum.hasMoreElements() == true)
         {
            // Wrap a cache key around the given object id/primary key
            array.add(((EntityCache) con.getInstanceCache()).createCacheKey(enum.nextElement()));
         }
         return array;
      }
      else if (result instanceof java.util.Collection)
      {

         ArrayList array = new ArrayList(((Collection) result).size());
         Iterator enum =  ((Collection) result).iterator();
         while (enum.hasNext())
         {
            // Wrap a cache key around the given object id/primary key
            array.add(((EntityCache) con.getInstanceCache()).createCacheKey(enum.next()));
         }
         return array;
      }
      else
      {
         // so we received something that's not valid
         // throw an exception reporting it
         throw new RemoteException("result of finder method is not a valid return type: " + result.getClass());
      }
   }

   public void activateEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
      long lStart = System.currentTimeMillis();
      try
      {
         ejbActivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      } catch (InvocationTargetException ite)
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
      finally {
         mActivate.add( System.currentTimeMillis() - lStart );
      }
   }

   public void loadEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
      long lStart = System.currentTimeMillis();
      try
      {
         ejbLoad.invoke(ctx.getInstance(), new Object[0]);
      } catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      } catch (InvocationTargetException ite)
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
      finally {
         mLoad.add( System.currentTimeMillis() - lStart );
      }
   }

   public boolean isModified(EntityEnterpriseContext ctx) throws Exception 
   {
      if(isModified == null)
      {
         return true;
      }
            
      Object[] args = {};
      Boolean modified = (Boolean) isModified.invoke(ctx.getInstance(), args);
      return modified.booleanValue();
   }
  
   public void storeEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
      long lStart = System.currentTimeMillis();
      //DEBUG       Logger.debug("Store entity");
      try
      {
         ejbStore.invoke(ctx.getInstance(), new Object[0]);
      } catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      } catch (InvocationTargetException ite)
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
      finally {
         mStore.add( System.currentTimeMillis() - lStart );
      }
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
      long lStart = System.currentTimeMillis();
      try
      {
         ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      } catch (InvocationTargetException ite)
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
      finally {
         mPassivate.add( System.currentTimeMillis() - lStart );
      }
   }

   public void removeEntity(EntityEnterpriseContext ctx)
   throws RemoteException, RemoveException
   {
      try
      {
         ejbRemove.invoke(ctx.getInstance(), new Object[0]);
      } catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      } catch (InvocationTargetException ite)
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
      finally {
         mRemove.add();
      }
   }

   public Map retrieveStatistic()
   {
      // Loop through all Interceptors and add Statistic
      Map lStatistics = new HashMap();
      lStatistics.put( "CreateCount", mCreate );
      lStatistics.put( "RemoveCount", mRemove );
      lStatistics.put( "ActiveBeanCount", mActiveBean );
      lStatistics.put( "ActivationTime", mActivate );
      lStatistics.put( "PassivationTime", mPassivate );
      lStatistics.put( "LoadTime", mLoad );
      lStatistics.put( "StoreTime", mStore );
      return lStatistics;
   }
   public void resetStatistic()
   {
      mCreate.reset();
      mRemove.reset();
      mActiveBean.reset();
      mActivate.reset();
      mPassivate.reset();
      mLoad.reset();
      mStore.reset();
   }

   // Z implementation ----------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   private Object callFinderMethod(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
   throws Exception
   {
      // Check if findByPrimaryKey
      // If so we check if the entity is in cache first
      if (finderMethod.getName().equals("findByPrimaryKey"))
      {
         Object key = ctx.getCacheKey();
         if (key == null)
         {
            key = ((EntityCache)con.getInstanceCache()).createCacheKey(args[0]);
         }
         if (con.getInstanceCache().isActive(key))
            return args[0]; // Object is active -> it exists -> no need to call finder
      }

      // get the finder method
      Method callMethod = (Method)finderMethods.get(finderMethod);

      if (callMethod == null)
      {
         throw new RemoteException("couldn't find finder method in bean class. " + finderMethod.toString());
      }

      // invoke the finder method
      Object result = null;
      try
      {
         result = callMethod.invoke(ctx.getInstance(), args);
      } catch (IllegalAccessException e)
        {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
      } catch (InvocationTargetException ite)
        {
            Throwable e = ite.getTargetException();
         if (e instanceof FinderException)
         {
            // Rethrow exception
            throw (FinderException)e;
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
         else if(e instanceof Exception)
         {
            throw (Exception)e;
         }
         else
         {
            throw (Error)e;
         }
      }

      return result;
   }


   // Inner classes -------------------------------------------------
}




