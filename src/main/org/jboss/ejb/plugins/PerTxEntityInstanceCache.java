/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.EntityCache;
import org.jboss.tm.TransactionLocal;
import org.jboss.logging.Logger;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Per transaction instance cache.
 *
 * @jmx:mbean
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class PerTxEntityInstanceCache
   implements EntityCache, PerTxEntityInstanceCacheMBean
{
   private static final Logger log = Logger.getLogger(PerTxEntityInstanceCache.class);

   // per container tx instance cache
   private final TransactionLocal txLocalCache = new TransactionLocal()
   {
      protected Object initialValue()
      {
         return new HashMap();
      }
   };

   private EntityContainer container;

   // EntityCache implementation

   public Object createCacheKey(Object id)
   {
      return id;
   }

   public EnterpriseContext get(Object id) throws RemoteException, NoSuchObjectException
   {
      if(id == null) throw new IllegalArgumentException("Can't get an object with a null key");

      Map cache = getLocalCache();
      EntityEnterpriseContext instance = (EntityEnterpriseContext) cache.get(id);
      if(instance == null)
      {
         try
         {
            // acquire
            instance = (EntityEnterpriseContext) container.getInstancePool().get();
            // set key
            instance.setId(id);
            instance.setCacheKey(id);
            // activate
            container.getPersistenceManager().activateEntity(instance);
            // insert
            cache.put(id, instance);
         }
         catch(Throwable x)
         {
            throw new NoSuchObjectException(x.getMessage());
         }
      }
      return instance;
   }

   public void insert(EnterpriseContext instance)
   {
      if(instance == null) throw new IllegalArgumentException("Can't insert a null object in the cache");

      EntityEnterpriseContext entity = (EntityEnterpriseContext) instance;
      getLocalCache().put(entity.getCacheKey(), instance);
   }

   public void release(EnterpriseContext instance)
   {
      if(instance == null) throw new IllegalArgumentException("Can't release a null object");

      tryToPassivate(instance);
   }

   public void remove(Object id)
   {
      getLocalCache().remove(id);
   }

   public boolean isActive(Object id)
   {
      return getLocalCache().containsKey(id);
   }

   public long getCacheSize()
   {
      return 0;
   }

   public void flush()
   {
   }

   // ContainerPlugin implementation

   public void setContainer(Container con)
   {
      this.container = (EntityContainer) con;
   }

   // Service implementation

   public void create() throws Exception
   {
   }

   public void start() throws Exception
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
   }

   // Protected

   protected void tryToPassivate(EnterpriseContext instance)
   {
      Object id = instance.getId();
      if(id != null)
      {
         BeanLock lock = container.getLockManager().getLock(id);
         try
         {
            lock.sync();
            if(canPassivate(instance))
            {
               try
               {
                  remove(id);
                  EntityEnterpriseContext entity = (EntityEnterpriseContext) instance;
                  container.getPersistenceManager().passivateEntity(entity);
                  container.getInstancePool().free(instance);
               }
               catch(Exception ignored)
               {
                  log.warn("failed to passivate, id=" + id, ignored);
               }
            }
            else
            {
               log.warn("Unable to passivate due to ctx lock, id=" + id);
            }
         }
         finally
         {
            lock.releaseSync();
            container.getLockManager().removeLockRef(id);
         }
      }
   }

   protected boolean canPassivate(EnterpriseContext ctx)
   {
      if(ctx.isLocked())
      {
         // The context is in the interceptor chain
         return false;
      }

      if(ctx.getTransaction() != null)
      {
         return false;
      }

      Object key = ((EntityEnterpriseContext) ctx).getCacheKey();
      return container.getLockManager().canPassivate(key);
   }

   // Private

   private Map getLocalCache()
   {
      return (Map) txLocalCache.get();
   }
}
