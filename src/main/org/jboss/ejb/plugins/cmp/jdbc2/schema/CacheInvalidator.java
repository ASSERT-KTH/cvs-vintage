/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.cache.invalidation.Invalidatable;
import org.jboss.cache.invalidation.InvalidationGroup;
import org.jboss.logging.Logger;

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import java.io.Serializable;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.4 $</tt>
 */
public class CacheInvalidator
   implements Invalidatable
{
   private static final Logger log = Logger.getLogger(CacheInvalidator.class);

   private final Cache cache;
   private final TransactionManager tm;
   private final InvalidationGroup group;

   public CacheInvalidator(Cache cache, TransactionManager tm, InvalidationGroup group)
   {
      this.cache = cache;
      this.tm = tm;
      this.group = group;
      group.register(this);
      log.debug("registered to group " + group.getGroupName());
   }

   public void unregister()
   {
      group.unregister(this);
      log.debug("unregistered from group " + group.getGroupName());
   }

   public void isInvalid(Serializable key)
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch(SystemException e)
      {
         log.error("Failed to obtain the current transaction", e);
         throw new IllegalStateException("Failed to obtain the current transaction: " + e.getMessage());
      }

      if(log.isTraceEnabled())
      {
         log.trace("invalidating key=" + key);
      }

      cache.lock(key);
      try
      {
         cache.remove(tx, key);
      }
      catch(Cache.RemoveException e)
      {
         log.warn(e.getMessage());
      }
      finally
      {
         cache.unlock(key);
      }
   }

   public void areInvalid(Serializable[] keys)
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch(SystemException e)
      {
         log.error("Failed to obtain the current transaction", e);
         throw new IllegalStateException("Failed to obtain the current transaction: " + e.getMessage());
      }

      boolean trace = log.isTraceEnabled();
      for(int i = 0; i < keys.length; ++i)
      {
         if(trace)
         {
            log.trace("invalidating key[" + i + "]=" + keys[i]);
         }

         cache.lock();
         try
         {
            cache.remove(tx, keys[i]);
         }
         catch(Cache.RemoveException e)
         {
            if(trace)
            {
               log.trace(e.getMessage());
            }
         }
         finally
         {
            cache.unlock();
         }
      }
   }

   public void invalidateAll()
   {
      cache.lock();
      try
      {
         cache.flush();
      }
      finally
      {
         cache.unlock();
      }
   }
}
