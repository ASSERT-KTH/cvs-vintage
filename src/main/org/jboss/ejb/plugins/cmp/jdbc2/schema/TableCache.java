/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.metadata.MetaData;
import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

import javax.transaction.Transaction;
import java.util.Map;
import java.util.HashMap;


/**
 * Simple LRU cache. Items are evicted when maxCapacity is exceeded.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.8 $</tt>
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class TableCache
   extends ServiceMBeanSupport
   implements Cache, TableCacheMBean
{
   private Cache.Listener listener = Cache.Listener.NOOP;
   private final Map rowsById;
   private CachedRow head;
   private CachedRow tail;
   private int maxCapacity;
   private final int minCapacity;

   private boolean locked;

   private final int partitionIndex;

   public TableCache(int partitionIndex, int initialCapacity, int maxCapacity)
   {
      this.maxCapacity = maxCapacity;
      this.minCapacity = initialCapacity;
      rowsById = new HashMap(initialCapacity);
      this.partitionIndex = partitionIndex;
   }

   public TableCache(Element conf) throws DeploymentException
   {
      String str = MetaData.getOptionalChildContent(conf, "min-capacity");
      minCapacity = (str == null ? 1000 : Integer.parseInt(str));
      rowsById = new HashMap(minCapacity);

      str = MetaData.getOptionalChildContent(conf, "max-capacity");
      maxCapacity = (str == null ? 10000 : Integer.parseInt(str));

      this.partitionIndex = 0;
   }

   /**
    * @jmx.managed-operation
    */
   public void registerListener(Cache.Listener listener)
   {
      log.debug("Registered listener for " + getServiceName());
      this.listener = listener;
   }

   /**
    * @jmx.managed-operation
    */
   public int size()
   {
      lock();
      try
      {
         return rowsById.size();
      }
      finally
      {
         unlock();
      }
   }

   /**
    * @jmx.managed-attribute
    */
   public int getMaxCapacity()
   {
      return maxCapacity;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setMaxCapacity(int maxCapacity)
   {
      this.maxCapacity = maxCapacity;
   }

   /**
    * @jmx.managed-attribute
    */
   public int getMinCapacity()
   {
      return minCapacity;
   }

   public synchronized void lock()
   {
      if(locked)
      {
         long start = System.currentTimeMillis();
         while(locked)
         {
            try
            {
               wait();
            }
            catch(InterruptedException e)
            {
            }
         }

         listener.contention(partitionIndex, System.currentTimeMillis() - start);
      }
      locked = true;
   }

   public void lock(Object key)
   {
      lock();
   }

   public synchronized void unlock()
   {
      if(!locked)
      {
         throw new IllegalStateException("The instance is not locked!");
      }
      locked = false;
      notify();
   }

   public void unlock(Object key)
   {
      unlock();
   }

   public Object[] getFields(Object pk)
   {
      Object[] fields;
      CachedRow row = (CachedRow) rowsById.get(pk);
      if(row != null && row.locker == null)
      {
         promoteRow(row);
         fields = new Object[row.fields.length];
         System.arraycopy(row.fields, 0, fields, 0, fields.length);
         listener.hit(partitionIndex);
      }
      else
      {
         fields = null;
         listener.miss(partitionIndex);
      }
      return fields;
   }

   public Object[] getRelations(Object pk)
   {
      Object[] relations;
      CachedRow row = (CachedRow) rowsById.get(pk);
      if(row != null && row.relations != null && row.locker == null)
      {
         promoteRow(row);
         relations = new Object[row.relations.length];
         System.arraycopy(row.relations, 0, relations, 0, relations.length);
      }
      else
      {
         relations = null;
      }
      return relations;
   }

   public void put(Transaction tx, Object pk, Object[] fields, Object[] relations)
   {
      CachedRow row = (CachedRow) rowsById.get(pk);
      if(row == null) // the row is not cached
      {
         Object[] fieldsCopy = new Object[fields.length];
         System.arraycopy(fields, 0, fieldsCopy, 0, fields.length);
         row = new CachedRow(pk, fieldsCopy);

         if(relations != null)
         {
            Object[] relationsCopy = new Object[relations.length];
            System.arraycopy(relations, 0, relationsCopy, 0, relations.length);
            row.relations = relationsCopy;
         }

         rowsById.put(pk, row);

         if(head == null)
         {
            head = row;
            tail = row;
         }
         else
         {
            head.prev = row;
            row.next = head;
            head = row;
         }
      }
      else if(row.locker == null || row.locker.equals(tx)) // the row is cached
      {
         promoteRow(row);
         System.arraycopy(fields, 0, row.fields, 0, fields.length);

         if(relations != null)
         {
            if(row.relations == null)
            {
               row.relations = new Object[relations.length];
            }
            System.arraycopy(relations, 0, row.relations, 0, relations.length);
         }

         row.locker = null;
      }

      CachedRow victim = tail;
      while(rowsById.size() > maxCapacity && victim != null)
      {
         CachedRow nextVictim = victim.prev;
         if(victim.locker == null)
         {
            dereference(victim);
            rowsById.remove(victim.pk);
            listener.eviction(partitionIndex, row.pk, rowsById.size());
         }
         victim = nextVictim;
      }
   }

   public void remove(Transaction tx, Object pk) throws Exception
   {
      CachedRow row = (CachedRow) rowsById.remove(pk);
      if(row == null || row.locker != null && !tx.equals(row.locker))
      {
         String msg = "removal of " +
            pk +
            " rejected for " +
            tx +
            ": " +
            (row == null ? "the entry could not be found" : "the entry is locked for update by " + row.locker);
         throw new Exception(msg);
      }

      dereference(row);
      row.locker = null;
   }

   public boolean contains(Transaction tx, Object pk)
   {
      CachedRow row = (CachedRow) rowsById.get(pk);
      return row != null && (row.locker == null || tx.equals(row.locker));
   }

   public void lockForUpdate(Transaction tx, Object pk) throws Exception
   {
      CachedRow row = (CachedRow) rowsById.get(pk);
      if(row != null)
      {
         if(row.locker != null && !tx.equals(row.locker))
         {
            throw new Exception("lock acquisition rejected for " +
               tx +
               ", the entry is locked for update by " + row.locker + ", id=" + pk);
         }
         row.locker = tx;
      }
      // else?!
   }

   public void releaseLock(Transaction tx, Object pk) throws Exception
   {
      CachedRow row = (CachedRow) rowsById.get(pk);
      if(row != null)
      {
         if(!tx.equals(row.locker))
         {
            throw new Exception("rejected to release lock for " +
               tx +
               ", the entry is locked for update by " + row.locker + ", id=" + pk);
         }
         row.locker = null;
      }
      // else?!
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append('[');

      try
      {
         lock();

         CachedRow cursor = head;
         while(cursor != null)
         {
            buf.append('(')
               .append(cursor.pk)
               .append('|');

            for(int i = 0; i < cursor.fields.length; ++i)
            {
               if(i > 0)
               {
                  buf.append(',');
               }

               buf.append(cursor.fields[i]);
            }

            buf.append(')');

            cursor = cursor.next;
         }
      }
      finally
      {
         unlock();
      }

      buf.append(']');
      return buf.toString();
   }

   // Private

   private void dereference(CachedRow row)
   {
      CachedRow next = row.next;
      CachedRow prev = row.prev;

      if(row == head)
      {
         head = next;
      }

      if(row == tail)
      {
         tail = prev;
      }

      if(next != null)
      {
         next.prev = prev;
      }

      if(prev != null)
      {
         prev.next = next;
      }

      row.next = null;
      row.prev = null;
   }

   private void promoteRow(CachedRow row)
   {
      if(head == null) // this is the first row in the cache
      {
         head = row;
         tail = row;
      }
      else if(row == head) // this is the head
      {
      }
      else if(row == tail) // this is the tail
      {
         tail = row.prev;
         tail.next = null;

         row.prev = null;
         row.next = head;

         head.prev = row;
         head = row;
      }
      else // somewhere in the middle
      {
         CachedRow next = row.next;
         CachedRow prev = row.prev;

         if(prev != null)
         {
            prev.next = next;
         }

         if(next != null)
         {
            next.prev = prev;
         }

         head.prev = row;
         row.next = head;
         row.prev = null;
         head = row;
      }
   }

   private class CachedRow
   {
      public final Object pk;
      public final Object[] fields;
      public Object[] relations;
      private Transaction locker;

      private CachedRow next;
      private CachedRow prev;

      public CachedRow(Object pk, Object[] fields)
      {
         this.pk = pk;
         this.fields = fields;
      }
   }
}
