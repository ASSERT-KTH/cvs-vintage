package org.jboss.ejb;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of keys carrying additional information about the query from
 * which they came. this is used to optimize (or maybe un-deoptimize)
 * multi-finders in the CMPPersistenceManager.
 *
 * @author <a href="mailto:danch@nvisia.com">danch</a>
 */
public class FinderResults implements Collection {

   private static long nextListId = 0;

   private Collection keys;
   /** hold arbitrary data from the query. This is an object rather than a
    *  string so that we can better support non-relational/non-jdbc storages */
   private Object queryData;

   private Object finder;

   private Object[] queryArgs;

   /**
    * List id is used only in the case of read ahead on load.
    */
   private long listId;

   /** Constructor taking the collection of keys to hold and the query data.
    */
   public FinderResults(Collection keys, Object queryData, Object finder, Object[] args) {
      this.keys = keys;
      this.queryData = queryData;
      this.finder = finder;
      this.queryArgs = args;
   }
   public Collection getAllKeys() {
      return keys;
   }
   public void setKeys(Collection newKeys) {
      this.keys = newKeys;
   }
   public Object getQueryData() {
      return queryData;
   }
   public Object getFinder() {
      return finder;
   }
   public Object[] getQueryArgs() {
      return queryArgs;
   }
   public boolean isReadAheadOnLoadUsed() {
      return false;
   }
   public long getListId() {
      return listId;
   }

   public int size() {
      return keys.size();
   }
   public boolean isEmpty() {
      return keys.isEmpty();
   }
   public boolean contains(Object o) {
      return keys.contains(o);
   }
   public Iterator iterator() {
      return keys.iterator();
   }
   public Object[] toArray() {
      return keys.toArray();
   }
   public Object[] toArray(Object[] array) {
      return keys.toArray(array);
   }
   public boolean add(Object o) {
      return keys.add(o);
   }
   public boolean remove(Object o) {
      return keys.remove(o);
   }
   public boolean containsAll(Collection otherCollection) {
      return keys.containsAll(otherCollection);
   }
   public boolean addAll(Collection otherCollection) {
      return keys.addAll(otherCollection);
   }
   public boolean removeAll(Collection otherCollection) {
      return keys.removeAll(otherCollection);
   }
   public boolean retainAll(Collection otherCollection) {
      return keys.retainAll(otherCollection);
   }
   public void clear() {
      keys.clear();
   }
}
