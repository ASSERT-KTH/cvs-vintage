/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import javax.transaction.Transaction;
import javax.transaction.Synchronization;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.logging.Logger;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.tm.TransactionLocal;

/**
 * PrefetchCache stores all of the data readahead for an entity.
 * Data is stored in the JDBCStoreManager entity tx data map on a per entity
 * basis. The read ahead data for each entity is stored with a soft reference.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public final class PrefetchCache 
{
   /**
    * To simplify null values handling in the prefetch cache data pool we use 
    * this value instead of 'null'
    */
   private static final Object NULL_VALUE = new Object();
   private static final Object LOAD_KEYS = new Object();
   private static final Object READ_AHEAD = new Object();

   private final Logger log;
   private final TransactionLocal prefetchCacheLocal;
   private final JDBCStoreManager manager;

   public PrefetchCache(JDBCStoreManager manager)
   {
      this.manager = manager;

      // Create the Log
      log = Logger.getLogger(getClass().getName());

      prefetchCacheLocal = new TransactionLocal(new CacheSynchronization())
      {
         protected Object initialValue()
         {
            return new HashMap();
         }
      };
   }

   public synchronized void addFinderResults(
         List results,
         JDBCReadAheadMetaData readahead) 
   {
      if(results.size() < 2) 
      {
         log.trace("Finder results not added: result-size=" + results.size());
         // nothing to see here... move along
         return;
      }

      if(log.isTraceEnabled()) 
      {
         log.trace("Add finder results:" +
               " result-size=" + results.size() +
               " readahead=" + readahead);
      }

      // 
      // Create a map between the primary keys and the next page of primary
      // keys as defined by the readahead metadata. This is how 
      // readhead on-load works.
      //
      Iterator iter = results.iterator();
      for(int i=0; iter.hasNext(); i++) 
      {
         Object primaryKey = iter.next();

         Map prefetchData = getPrefetchData(primaryKey, true);
         prefetchData.put(READ_AHEAD, readahead);

         if(readahead.isNone()) 
         {
            // disassociate any existing load keys
            prefetchData.remove(LOAD_KEYS);
         }
         else
         {
            List loadKeys = results.subList(
                  i, 
                  Math.min(results.size(), i + readahead.getPageSize()));

            prefetchData.put(LOAD_KEYS, loadKeys);
         }
      }
   }

   /**
    * Loads all of the prefetched data for the ctx into it.
    * @param ctx the context that will be loaded
    */
   public void loadPrefetchData(EntityEnterpriseContext ctx) 
   {
      if(log.isTraceEnabled()) 
      {
         log.trace("load data: pk="+ctx.getId());
      }

      // get the prefetch data
      Map prefetchData = getPrefetchData(ctx.getId(), false);
      if(prefetchData == null) 
      {
         // no prefetch data for this entity
         if(log.isTraceEnabled()) 
         {
            log.trace("No prefetch data found: pk="+ctx.getId());
         }
         return;
      }

      // read ahead data
      JDBCReadAheadMetaData readahead = 
            (JDBCReadAheadMetaData) prefetchData.remove(READ_AHEAD);
      if(readahead != null)
      {
         JDBCContext.setReadAheadMetaData(ctx, readahead);
      }
      
      // load keys
      List loadKeys = (List) prefetchData.remove(LOAD_KEYS);
      if(loadKeys != null)
      {
         JDBCContext.setLoadKeys(ctx, loadKeys);
      }

      // iterate over the keys in the prefetch data
      Iterator iter = prefetchData.keySet().iterator();
      while(iter.hasNext()) 
      {
         Object field = iter.next();
      
         // get the value that was prefetched for this field
         Object value = prefetchData.get(field);

         // if we didn't get a value something is seriously hosed
         if(value == null) 
         {
            throw new IllegalStateException("Prefetched value not found");
         }
      
         // remove this value from the prefetch data as it is about 
         // to be loaded into the context
         iter.remove();

         // check for null value standin
         if(value == NULL_VALUE) 
         {
            value = null;
         }
      
         if(field instanceof JDBCCMPFieldBridge) 
         {
            JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge)field;

            if(!cmpField.isLoaded(ctx)) 
            {
               if(log.isTraceEnabled()) 
               {
                  log.trace("Prefetch data:" +
                        " pk="+ctx.getId()+
                        " cmpField="+cmpField.getFieldName());
               }

               // set the value
               cmpField.setInstanceValue(ctx, value);
   
               // mark this field clean as it's value was just loaded
               cmpField.setClean(ctx);
            } 
            else 
            {
               if(log.isTraceEnabled()) 
               {
                  log.trace("CMRField already loaded:" +
                        " pk="+ctx.getId()+
                        " cmpField="+cmpField.getFieldName());
               }
            }
         } 
         else if(field instanceof JDBCCMRFieldBridge) 
         {
            JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)field;

            if(!cmrField.isLoaded(ctx)) 
            {
               if(log.isTraceEnabled()) 
               {
                  log.trace("Prefetch data:" +
                        " pk="+ctx.getId()+
                        " cmrField="+cmrField.getFieldName());
               }
     
               // set the value
               cmrField.load(ctx, (List)value);

               // add the loaded list to the related entity's prefetch cache
               JDBCStoreManager relatedManager = 
                     cmrField.getRelatedCMRField().getJDBCStoreManager();
               PrefetchCache relatedPrefetchCache = 
                     relatedManager.getPrefetchCache();
               relatedPrefetchCache.addFinderResults(
                     (List)value, cmrField.getReadAhead());

               // mark this field clean as it's value was just loaded
               cmrField.setClean(ctx);
            } 
            else 
            {
               if(log.isTraceEnabled()) 
               {
                  log.trace("CMRField already loaded:" +
                        " pk=" + ctx.getId() +
                        " cmrField=" + cmrField.getFieldName());
               }
            } 
         }
      }

      // remove all prefetch data for this entity as it was just loaded 
      Map prefetchCache = (Map)prefetchCacheLocal.get();
      prefetchCache.remove(ctx.getId());
   }

   /**
    * Add prefetched data for an entity within the scope of a transaction
    */
   public void addPrefetchData(
         Object primaryKey,
         JDBCFieldBridge field,
         Object fieldValue) 
   {
      if(field instanceof JDBCCMRFieldBridge) 
      {
         if(fieldValue == null) 
         {
            fieldValue = Collections.EMPTY_LIST;
         } 
         else if(!(fieldValue instanceof Collection)) 
         {
            fieldValue = Collections.singletonList(fieldValue);
         }
      }

      if(log.isTraceEnabled()) 
      {
         log.trace("Add prefetch data:" +
               " pk=" + primaryKey +
               " field=" + field.getFieldName());
      }

      // convert null values to a null value standing object
      if(fieldValue == null) 
      {
         fieldValue = NULL_VALUE;
      }

      // store the prefetched data
      getPrefetchData(primaryKey, true).put(field, fieldValue);
   }

   public void removePrefetchData(Object primaryKey) 
   {
      if(log.isTraceEnabled()) 
      {
         log.trace("Removing prefetched data for " + primaryKey);
      }
         
      // remove the prefetched data
      Map prefetchCache = (Map)prefetchCacheLocal.get();
      prefetchCache.remove(primaryKey);
   }

   /**
    * Gets the map of prefetch data.
    * @param primaryKey the primary key of the entity 
    * @param create should a new preload data map be created if one is not found
    * @return the preload data map for null if one is not found
    */
   private Map getPrefetchData(Object primaryKey, boolean create) 
   {
      // 
      // Be careful in this code. A soft reference may be cleared at any time,
      // so don't check if a reference has a value and then get that value. 
      // Instead get the value and then check if it is null.
      //
      
      // get the soft reference to the prefetch data
      Map prefetchCache = (Map)prefetchCacheLocal.get();
      SoftReference prefetchDataRef = 
            (SoftReference)prefetchCache.get(primaryKey);

      // did we get a reference
      if(prefetchDataRef != null) 
      {
         // get the  map from the reference
         Map prefetchData = (Map)prefetchDataRef.get();

         // did we actually get a map? (will be null if it has been GC'd)
         if(prefetchData != null) 
         {
            return prefetchData;
         }
      }
      
      //
      // at this point we did not get an existing value
      //

      // if we got a dead reference remove it
      if(prefetchDataRef != null) 
      {
         prefetchCache.remove(primaryKey);
      }

      // if we are not creating, we're done
      if(!create) 
      {
         return null;
      }

      // create the new prefetch data map
      Map prefetchData = new HashMap();

      // create new soft reference
      prefetchDataRef = new SoftReference(prefetchData);

      // store the reference
      prefetchCache.put(primaryKey, prefetchDataRef);

      // return the new prefetch data map
      return prefetchData;
   }

   private void mergePrefetchData(
         Object primaryKey, 
         int commitOption,
         Map prefetchData) throws Exception
   {
      EntityContainer container = manager.getContainer();
      Transaction transaction = 
            container.getTransactionManager().getTransaction();

      BeanLock lock = container.getLockManager().getLock(primaryKey);
      log.trace("Attempting to get lock for pk: " + primaryKey);
      if(lock.lockNoWait(transaction))
      {
         log.trace("Got lock now sync for pk: " + primaryKey);
         lock.sync();
         log.trace("Got lock sync for pk: " + primaryKey);
         try
         {
            EntityEnterpriseContext ctx = (EntityEnterpriseContext) 
                  container.getInstanceCache().get(primaryKey);
            loadPrefetchData(ctx);
            if(commitOption == ConfigurationMetaData.A_COMMIT_OPTION)
            {  
               ctx.setValid(true);
            }
         }
         finally
         {
            log.trace("Releasing lock for pk: " + primaryKey);
            lock.endTransaction(transaction);
            log.trace("Released lock now for the sync for pk: " + primaryKey);
            lock.releaseSync();
            log.trace("Released lock sync for pk: " + primaryKey);
         }
      }
      else
      {
         log.trace("Couldn't get lock for pk: " + primaryKey);
      }
   }

   private class CacheSynchronization implements Synchronization
   {
      public void beforeCompletion()
      {
      }

      public void afterCompletion(int status)
      {
         log.trace("Merging prefetch cache into main");
         try{

         EntityContainer container = manager.getContainer();
         if(container == null)
         {
            // no container... we are in some sort of shutdown state
            log.trace("No container");
            return;
         }
         
         // just return if we are using commit option B or C as it is a waste
         // of effort
         // FIXME: Allow option B to go throught when we get optimistic locking
         ConfigurationMetaData configuration = 
               container.getBeanMetaData().getContainerConfiguration();
         int commitOption = configuration.getCommitOption();
         if(commitOption == ConfigurationMetaData.B_COMMIT_OPTION &&
               commitOption == ConfigurationMetaData.C_COMMIT_OPTION)
         {
            return;
         }

         // get the soft reference to the prefetch data
         Map prefetchCache = new HashMap((Map)prefetchCacheLocal.get());

         log.trace("Got " + prefetchCache.size() + " keys to merge");
         for(Iterator iterator = prefetchCache.keySet().iterator(); 
               iterator.hasNext(); )
         {
            try {
               Object primaryKey = iterator.next();
               SoftReference prefetchDataRef = 
                     (SoftReference)prefetchCache.get(primaryKey);

               // get the  map from the reference
               Map prefetchData = (Map)prefetchDataRef.get();

               // did we actually get a map? (will be null if it has been GC'd)
               if(prefetchData != null) 
               {
                  log.trace("Merging data: pk=" + primaryKey + 
                        " prefetchData.size()=" + prefetchData.size());
                  mergePrefetchData(primaryKey, commitOption, prefetchData);
               }
               else
               {
                  log.trace("No merge data for: pk=" + primaryKey);
               }
            }
            catch(Exception e)
            {
               // not a big deal becaus we just can't merge this bean's data
               log.error("An exception occured while attempting to merge " +
                     "prefetched data into the main cache", e);
            }
         }
         }finally{
         log.trace("Done merging prefetch cache into main");
         }
      }
   }
}
