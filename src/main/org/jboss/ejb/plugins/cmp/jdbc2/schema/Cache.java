/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;


import javax.transaction.Transaction;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.6 $</tt>
 */
public interface Cache
{
   void lock();

   void lock(Object key);

   void unlock();

   void unlock(Object key);

   Object[] getFields(Object pk);

   Object[] getRelations(Object pk);

   void put(Transaction tx, Object pk, Object[] fields, Object[] relations);

   void remove(Transaction tx, Object pk) throws Exception;

   boolean contains(Transaction tx, Object pk);

   void lockForUpdate(Transaction tx, Object pk) throws Exception;

   void releaseLock(Transaction tx, Object pk) throws Exception;

   void start() throws Exception;

   void stop() throws Exception;

   Cache NONE = new Cache()
   {
      public void lock()
      {
      }

      public void lock(Object key)
      {
      }

      public void unlock()
      {
      }

      public void unlock(Object key)
      {
      }

      public Object[] getFields(Object pk)
      {
         return null;
      }

      public Object[] getRelations(Object pk)
      {
         return null;
      }

      public void put(Transaction tx, Object pk, Object[] fields, Object[] relations)
      {
      }

      public void remove(Transaction tx, Object pk) throws Exception
      {
      }

      public boolean contains(Transaction tx, Object pk)
      {
         return false;
      }

      public int size()
      {
         return 0;
      }

      public void lockForUpdate(Transaction tx, Object pk) throws Exception
      {
      }

      public void releaseLock(Transaction tx, Object pk) throws Exception
      {
      }

      public void start()
      {
      }

      public void stop()
      {
      }
   };

   interface CacheLoader
   {
      Object loadFromCache(Object value);

      Object getCachedValue();
   }

   interface Listener
   {
      void contention(int partitionIndex, long time);

      void eviction(int partitionIndex, Object pk, int size);

      void hit(int partitionIndex);

      void miss(int partitionIndex);

      public Listener NOOP = new Listener()
      {
         public void contention(int partitionIndex, long time)
         {
         }

         public void eviction(int partitionIndex, Object pk, int size)
         {
         }

         public void hit(int partitionIndex)
         {
         }

         public void miss(int partitionIndex)
         {
         }
      };
   }
}
