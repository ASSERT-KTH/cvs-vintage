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
import java.util.Map;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.logging.Logger;
import org.jboss.ejb.FinderResults;
import org.jboss.util.LRUCachePolicy;

/**
 * ReadAheadCache stores all of the data readahead for an entity.
 * Data is stored in the JDBCStoreManager entity tx data map on a per entity
 * basis. The read ahead data for each entity is stored with a soft reference.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public class ReadAheadCache {
   /**
    * To simplify null values handling in the preloaded data pool we use 
    * this value instead of 'null'
    */
   private static final Object NULL_VALUE = new Object();

   private final JDBCStoreManager manager;
   private final Logger log;
   private Map listMap;
   private ListCache listCache;

   public ReadAheadCache(JDBCStoreManager manager) {
      this.manager = manager;

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public void create() {
      // Create the list map
      listMap = new HashMap();

      // Create the list cache
      int listCacheMax = manager.getEntityBridge().getListCacheMax();
      listCache = new ListCache(listCacheMax);

      listCache.create();
   }

   public void start() {
      listCache.start();
   }
   
   public void stop() {
      listMap.clear();
      listCache.stop();
   }

   public void destroy() {
      listCache.destroy();
      listCache = null;
      listMap = null;
   }

   public synchronized void addFinderResult(FinderResults finderResults) {
      if(finderResults.size() <= 1) {
         // only cache results with more then one entry
         return;
      }
      if(!(finderResults.getAllKeys() instanceof List)) {
         log.warn("FinderResults does not contain a List. Read ahead will be " +
               "disabled for this query");
         return;
      }

      // add the finder to the LRU list
      listCache.add(finderResults);

      // 
      // Create a map between the entity prumary keys and the list.
      // The primary key will point to the last list added that contained the
      // primary key.
      //
      HashSet dereferencedResults = new HashSet();
      Iterator iter = finderResults.iterator();
      for(int i=0; iter.hasNext(); i++) {
         Object primaryKey = iter.next();

         // Keep track of the resutls that have been dereferenced. Later we 
         // all results from the list cache that are no longer referenced.
         EntityMapEntry oldInfo = (EntityMapEntry)listMap.put(
               primaryKey, new EntityMapEntry(i, finderResults));
         if(oldInfo != null) {
            dereferencedResults.add(oldInfo.finderResults);
         }
      }
      
      //
      // Now we remove all lists from the list cache that are no longer 
      // referenced in the list map.
      //

      // if we don't have any dereferenced results at this point we are done
      if(dereferencedResults.isEmpty()) {
         return;
      }
      
      // remove all lists from the dereferenced set that are still referenced
      // in the listMap
      iter =  listMap.values().iterator();
      while(iter.hasNext()) {
         EntityMapEntry entry = (EntityMapEntry)iter.next();
         dereferencedResults.remove(entry.finderResults);
      }

      // if we don't have any dereferenced results at this point we are done
      if(dereferencedResults.isEmpty()) {
         return;
      }

      // remove all results from the cache that are no longer referenced
      iter = dereferencedResults.iterator();
      while(iter.hasNext()) {
         Object obj = iter.next();
         if(log.isTraceEnabled()) {
            log.trace("Removing dereferenced finder results: " + obj);
         }
         listCache.remove(obj);
      }
   }

   public synchronized void removeFinderResult(FinderResults finderResults) {
      // remove the list from the list cache
      listCache.remove(finderResults);

      // remove all primary keys from the listMap that reference this list
      if(listMap != null && !listMap.isEmpty()) {
         Iterator iter =  listMap.values().iterator();
         while(iter.hasNext()) {
            EntityMapEntry entry = (EntityMapEntry)iter.next();
            if(entry.finderResults.equals(finderResults)) {
               iter.remove();
            }
         }
      }
   }

   public synchronized EntityReadAheadInfo getEntityReadAheadInfo(Object pk) { 
      EntityMapEntry entry = (EntityMapEntry)listMap.get(pk);
      // we're using this finder results so promote it to the head of the
      // LRU list
      if(entry != null) {
         listCache.promote(entry.finderResults);

         int pageSize;
         Object queryData = entry.finderResults.getQueryData();
         if(queryData instanceof JDBCReadAheadMetaData) {
            pageSize = ((JDBCReadAheadMetaData)queryData).getPageSize();
         } else {
            pageSize = manager.getMetaData().getReadAhead().getPageSize();
         }
         int from = entry.index;
         int to = Math.min(entry.finderResults.size(), entry.index + pageSize);
         List loadKeys = 
               ((List)entry.finderResults.getAllKeys()).subList(from, to);

         return new EntityReadAheadInfo(loadKeys);
      } else {
         return new EntityReadAheadInfo(Collections.singletonList(pk));
      }
   }

   /**
    * Loads all of the preloaded data for the ctx into it.
    * @param ctx the context that will be loaded
    */
   public void load(EntityEnterpriseContext ctx) {
      if(log.isTraceEnabled()) {
         log.trace("load data:" +
               " entity="+manager.getEntityBridge().getEntityName()+
               " pk="+ctx.getId());
      }

      // get the preload data map
      Map preloadDataMap = getPreloadDataMap(ctx.getId(), false);
      if(preloadDataMap == null) {
         // no preloaded data for this entity
         if(log.isTraceEnabled()) {
            log.trace("No preload data found:"+
                  " entity="+manager.getEntityBridge().getEntityName()+
                  " pk="+ctx.getId());
         }
         return;
      }

      // iterate over the keys in the preloaded map
      Iterator iter = preloadDataMap.keySet().iterator();
      while(iter.hasNext()) {
         Object field = iter.next();
      
         // get the value that was preloaded for this field
         Object value = preloadDataMap.get(field);

         // if we didn't get a value something is seriously hosed
         if(value == null) {
            throw new IllegalStateException("Preloaded value not found");
         }
      
         // remove this value from the preload cache as it is about to be loaded
         iter.remove();

         // check for null value standin
         if(value == NULL_VALUE) {
            value = null;
         }
      
         if(field instanceof JDBCCMPFieldBridge) {
            JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge)field;

            if(!cmpField.isLoaded(ctx)) {
               // set the value
               cmpField.setInstanceValue(ctx, value);
   
               // mark this field clean as it's value was just loaded
               cmpField.setClean(ctx);
            }
         } else if(field instanceof JDBCCMRFieldBridge) {
            JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)field;

            if(!cmrField.isLoaded(ctx)) {
               if(log.isTraceEnabled()) {
                  log.trace("Preloading data:" +
                        " entity="+manager.getEntityBridge().getEntityName()+
                        " pk="+ctx.getId()+
                        " cmrField="+cmrField.getFieldName());
               }
     
               // set the value
               cmrField.loadPreloadedValue(ctx, (List)value);

               // add the loaded list to the related entity's readahead cache
               JDBCStoreManager relatedManager = 
                     cmrField.getRelatedCMRField().getJDBCStoreManager();
               ReadAheadCache relatedReadAheadCache = 
                     relatedManager.getReadAheadCache();
               relatedReadAheadCache.addFinderResult(new FinderResults(
                     (List)value, cmrField.getReadAhead(), null, null));
            } else {
               if(log.isTraceEnabled()) {
                  log.trace("CMRField already loaded:" +
                        " entity="+manager.getEntityBridge().getEntityName()+
                        " pk="+ctx.getId()+
                        " cmrField="+cmrField.getFieldName());
               }
            } 
         }
      }

      // remove all preload data map as all of the data has been loaded
      manager.removeEntityTxData(new PreloadKey(ctx.getId()));
   }

   /**
    * Add preloaded data for an entity within the scope of a transaction
    */
   public void addPreloadData(
         Object entityPrimaryKey,
         JDBCFieldBridge field,
         Object fieldValue) {

      if(field instanceof JDBCCMRFieldBridge) {
         if(fieldValue == null) {
            fieldValue = Collections.EMPTY_LIST;
         } else if(!(fieldValue instanceof Collection)) {
            fieldValue = Collections.singletonList(fieldValue);
         }
      }

      if(log.isTraceEnabled()) {
         log.trace("Add preload data:" +
               " entity="+manager.getEntityBridge().getEntityName()+
               " pk="+entityPrimaryKey+
               " field="+field.getFieldName());
      }

      // convert null values to a null value standing object
      if(fieldValue == null) {
         fieldValue = NULL_VALUE;
      }

      // store the preloaded data
      getPreloadDataMap(entityPrimaryKey, true).put(field, fieldValue);
   }

   public synchronized void removeCachedData(Object primaryKey) {
      if(log.isTraceEnabled()) {
         log.trace("Removing cached data for "+primaryKey);
      }
         
      // remove the preloaded data
      manager.removeEntityTxData(new PreloadKey(primaryKey));

      EntityMapEntry oldInfo = 
            (EntityMapEntry)listMap.remove(primaryKey);
      
      // if the entity didn't have readahead entry; return
      if(oldInfo == null) {
         return;
      }

      // check to see if the dereferenced finder result is still referenced
      Iterator iter = listMap.values().iterator();
      while(iter.hasNext()) {
         EntityMapEntry entry = (EntityMapEntry)iter.next();
         if(entry.finderResults.equals(oldInfo.finderResults)) {
            // ok it is still referenced
            return;
         }
      }

      // a reference to the old finder set was not found so remove it
      if(log.isTraceEnabled()) {
         log.trace("Removing dereferenced finder results: " + 
               oldInfo.finderResults);
      }
      listCache.remove(oldInfo.finderResults);
   }

   /**
    * Gets the map of preloaded data.
    * @param entityPrimaryKey the primary key of the entity 
    * @param create should a new preload data map be created if one is not found
    * @return the preload data map for null if one is not found
    */
   private Map getPreloadDataMap(Object entityPrimaryKey, boolean create) {
      // 
      // Be careful in this code. A soft reference may be cleared at any time,
      // so don't check if a reference has a value and then get that value. 
      // Instead get the value and then check if it is null.
      //
      
      // create a preload key for the entity 
      PreloadKey preloadKey = new PreloadKey(entityPrimaryKey);

      // get the soft reference to the preload data map
      SoftReference ref = (SoftReference)manager.getEntityTxData(preloadKey);

      // did we get a reference
      if(ref != null) {
         // get the  map from the reference
         Map preloadDataMap = (Map)ref.get();

         // did we actually get a map? (will be null if it has been GC'd)
         if(preloadDataMap != null) {
            return preloadDataMap;
         }
      }
      
      //
      // at this point we did not get an existing value
      //

      // if we got a dead reference remove it
      if(ref != null) {
         manager.removeEntityTxData(preloadKey);
      }

      // if we are not creating, we're done
      if(!create) {
         return null;
      }

      // create the new preload data map
      Map preloadDataMap = new HashMap();

      // create new soft reference
      ref = new SoftReference(preloadDataMap);

      // store the reference
      manager.putEntityTxData(preloadKey, ref);

      // return the new preload data map
      return preloadDataMap;
   }

   private class ListCache extends LRUCachePolicy {
      public ListCache(int max) {
         super(2, max);
      }
      public void add(FinderResults r) {
         insert(r, r);
      }
      public void promote(FinderResults r) {
         get(r);
      }
      protected void ageOut(LRUCacheEntry entry) {
         removeFinderResult((FinderResults)entry.m_key);
      }
   }
   
   /**
    * Wraps an entity primary key, so it does not collide with other
    * data stored in the entityTxDataMap.
    */
   private class PreloadKey {
      private final Object entityPrimaryKey;

      public PreloadKey(Object entityPrimaryKey) {
         if(entityPrimaryKey == null) {
            throw new IllegalArgumentException("Entity primary key is null");
         }
         this.entityPrimaryKey = entityPrimaryKey;
      }

      public boolean equals(Object object) {
         if(object instanceof PreloadKey) {
            PreloadKey preloadKey = (PreloadKey)object;
            return preloadKey.entityPrimaryKey.equals(entityPrimaryKey);
         }
         return false;
      }

      public int hashCode() {
         return entityPrimaryKey.hashCode();
      }

      public String toString() {
         return "PreloadKey: entityId="+entityPrimaryKey;
      }
   }

   private class EntityMapEntry {
      public final int index;
      public final FinderResults finderResults;

      private EntityMapEntry(int index, FinderResults finderResults) {
         this.index = index;
         this.finderResults = finderResults;
      }
   }
   public class EntityReadAheadInfo {
      private final List loadKeys;
      private EntityReadAheadInfo(List loadKeys) {
         this.loadKeys = loadKeys;
      }
      public List getLoadKeys() {
         return loadKeys;
      }
   }
}
