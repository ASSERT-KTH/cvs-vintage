/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.system.ServiceMBeanSupport;

import javax.transaction.Transaction;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.5 $</tt>
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class PartitionedTableCache
   extends ServiceMBeanSupport
   implements Cache, PartitionedTableCacheMBean
{
   private Cache.Listener listener = Cache.Listener.NOOP;

   private final int minCapacity;
   private final int minPartitionCapacity;
   private int maxCapacity;
   private int maxPartitionCapacity;

   private final TableCache[] partitions;

   public PartitionedTableCache(int minCapacity, int maxCapacity, int partitionsTotal)
   {
      this.minCapacity = minCapacity;
      this.maxCapacity = maxCapacity;

      minPartitionCapacity = minCapacity / partitionsTotal + 1;
      maxPartitionCapacity = maxCapacity / partitionsTotal + 1;
      partitions = new TableCache[partitionsTotal];
      for(int i = 0; i < partitions.length; ++i)
      {
         partitions[i] = new TableCache(i, minPartitionCapacity, maxPartitionCapacity);
      }

      if(log.isTraceEnabled())
      {
         log.trace("min-capacity=" + minCapacity + ", max-capacity=" + maxCapacity + ", partitions=" + partitionsTotal);
      }
   }

   /**
    * @jmx.managed-operation
    */
   public void registerListener(Cache.Listener listener)
   {
      log.debug("Registered listener for " + getServiceName());
      this.listener = listener;
      for(int i = 0; i < partitions.length; ++i)
      {
         partitions[i].registerListener(listener);
      }
   }

   /**
    * @jmx.managed-operation
    */
   public int size()
   {
      int size = 0;
      for(int i = 0; i < partitions.length; ++i)
      {
         size += partitions[i].size();
      }
      return size;
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
      this.maxPartitionCapacity = maxCapacity / partitions.length + 1;
      for(int i = 0; i < partitions.length; ++i)
      {
         partitions[i].setMaxCapacity(maxPartitionCapacity);
      }
   }

   /**
    * @jmx.managed-attribute
    */
   public int getMinCapacity()
   {
      return minCapacity;
   }

   /**
    * @jmx.managed-attribute
    */
   public int getPartitionsTotal()
   {
      return partitions.length;
   }

   /**
    * @jmx.managed-attribute
    */
   public int getMinPartitionCapacity()
   {
      return minPartitionCapacity;
   }

   /**
    * @jmx.managed-attribute
    */
   public int getMaxPartitionCapacity()
   {
      return maxPartitionCapacity;
   }

   public void lock()
   {
   }

   public void lock(Object key)
   {
      int partitionIndex = getPartitionIndex(key);
      partitions[partitionIndex].lock(key);
   }

   public void unlock()
   {
   }

   public void unlock(Object key)
   {
      int partitionIndex = getPartitionIndex(key);
      partitions[partitionIndex].unlock(key);
   }

   public Object[] getFields(Object pk)
   {
      final int i = getPartitionIndex(pk);
      return partitions[i].getFields(pk);
   }

   public Object[] getRelations(Object pk)
   {
      final int i = getPartitionIndex(pk);
      return partitions[i].getRelations(pk);
   }

   public void put(Transaction tx, Object pk, Object[] fields, Object[] relations)
   {
      final int i = getPartitionIndex(pk);
      partitions[i].put(tx, pk, fields, relations);
   }

   public void remove(Transaction tx, Object pk)
   {
      final int i = getPartitionIndex(pk);
      partitions[i].remove(tx, pk);
   }

   public boolean contains(Transaction tx, Object pk)
   {
      final int i = getPartitionIndex(pk);
      return partitions[i].contains(tx, pk);
   }

   public void lockForUpdate(Transaction tx, Object pk) throws Exception
   {
      final int i = getPartitionIndex(pk);
      partitions[i].lockForUpdate(tx, pk);
   }

   public void releaseLock(Transaction tx, Object pk) throws Exception
   {
      final int i = getPartitionIndex(pk);
      partitions[i].releaseLock(tx, pk);
   }

   // Private

   private int getPartitionIndex(Object key)
   {
      return Math.abs(key.hashCode()) % partitions.length;
   }
}
