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
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface Cache
{
   void lock();

   void unlock();

   Object[] getFields(Object pk);

   Object[] getRelations(Object pk);

   void put(Transaction tx, Object pk, Object[] fields, Object[] relations);

   void remove(Transaction tx, Object pk) throws Exception;

   boolean contains(Transaction tx, Object pk);

   void lockForUpdate(Transaction tx, Object pk) throws Exception;

   void releaseLock(Transaction tx, Object pk) throws Exception;

   Cache NONE = new Cache()
   {
      public void lock()
      {
      }

      public void unlock()
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
   };

   interface CacheLoader
   {
      Object loadFromCache(Object value);

      Object getCachedValue();
   }
}
