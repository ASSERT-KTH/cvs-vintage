/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.logging.Logger;


/**
*   <description>
*
*   @see <related>
*   @author Rickard �berg (rickard.oberg@telkel.com)
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.19 $
*/
public class BMPPersistenceManager
implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   EntityContainer con;

   Method ejbLoad;
   Method ejbStore;
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;

   HashMap createMethods = new HashMap();
   HashMap postCreateMethods = new HashMap();
   HashMap finderMethods = new HashMap();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (EntityContainer)c;
   }

   public void init()
   throws Exception
   {
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);

      // Create cache of create methods
      Method[] methods = con.getHomeClass().getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].getName().equals("create"))
         {
            createMethods.put(methods[i], con.getBeanClass().getMethod("ejbCreate", methods[i].getParameterTypes()));
            postCreateMethods.put(methods[i], con.getBeanClass().getMethod("ejbPostCreate", methods[i].getParameterTypes()));
         }
      }

      // Create cache of finder methods
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].getName().startsWith("find"))
         {
            finderMethods.put(methods[i], con.getBeanClass().getMethod("ejbF" + methods[i].getName().substring(1), methods[i].getParameterTypes()));
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
      Method createMethod = (Method)createMethods.get(m);
      Method postCreateMethod = (Method)postCreateMethods.get(m);

      Object id = null;
      try
      {
         // Call ejbCreate
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
         } else
         {
            // Rethrow exception
            throw (Exception)e;
         }
      }

      // set the id
      ctx.setId(id);

      // Create a new CacheKey
      Object cacheKey = ((EntityCache) con.getInstanceCache()).createCacheKey( id );

      // Give it to the context
      ctx.setCacheKey(cacheKey);

      // Insert in cache, it is now safe
      con.getInstanceCache().insert(ctx);

      // Create EJBObject
      ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(cacheKey));

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
         } else
         {
            // Rethrow exception
            throw (Exception)e;
         }
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
   }

   public void loadEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
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
   }

   public void storeEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
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
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
   throws RemoteException
   {
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
         if (!con.getInstanceCache().isActive(args[0]))
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
         } else
         {
            // Rethrow exception
            throw (Exception)e;
         }
      }

      return result;
   }


   // Inner classes -------------------------------------------------
}




