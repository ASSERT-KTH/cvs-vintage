/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public final class UtilCollectionsCopier implements Copier
{
   private final Copier objectCopier;
   
   public UtilCollectionsCopier(Copier objectCopier)
   {
      this.objectCopier = objectCopier;
   }

   public Object copy(Object source, IdentityHashMap referenceMap)
   {
      // nothing returns nothing
      if(source == null)
      {
         return null;
      }
      
      // check the reference map first
      Object copy = referenceMap.get(source);
      if(copy != null)
      {
         return copy;
      }
 
      if(source instanceof List)
      {
         return copyList((List)source, referenceMap);
      }
      else if(source instanceof Map)
      {
         return copyMap((Map)source, referenceMap);
      }
      else if(source instanceof Set)
      {
         return copySet((Set)source, referenceMap);
      }
      else
      {
         throw new CopyException("Source is not a List, Map or Set: " + 
               source.getClass());
      }
   }

   private List copyList(List source, IdentityHashMap referenceMap)
   {
      // Create new list
      List list = null;
      if(source instanceof ArrayList)
      {
         list = new ArrayList(source.size());
      }
      else if (source instanceof LinkedList)
      {
         list = new LinkedList();
      }
      else if (source instanceof Stack)
      {
         list = new Stack();
      }
      else if (source instanceof Vector)
      {
         list = new Vector(source.size());
      }
      else
      {
         throw new CopyException("Source list is an unknown type: " + 
               source.getClass());
      }
      
      // put the new list into the reference map befrore deligating back to
      // the object copier (avoid infinite loops).
      referenceMap.put(source, list);

      // Copy the list elements to the new list
      for(Iterator iterator = source.iterator(); iterator.hasNext(); )
      {
         list.add(objectCopier.copy(iterator.next(), referenceMap));
      }
      return list;
   }

   private Map copyMap(Map source, IdentityHashMap referenceMap)
   {
      // Create new mapo
      Map map = null;
      if(source instanceof HashMap)
      {
         map = new HashMap(source.size());
      }
      else if (source instanceof Hashtable)
      {
         map = new Hashtable(source.size());
      }
      else if (source instanceof TreeMap)
      {
         Comparator comparator = ((TreeMap)source).comparator();
         comparator = (Comparator)objectCopier.copy(comparator, referenceMap);
         map = new TreeMap(comparator);
      }
      else if (source instanceof WeakHashMap)
      {
         map = new WeakHashMap(source.size());
      }
      else
      {
         throw new CopyException("Source map is an unknown type: " + 
               source.getClass());
      }

      // put the new list into the reference map befrore deligating back to
      // the object copier (avoid infinite loops).
      referenceMap.put(source, map);

      // Copy the list elements to the new list
      for(Iterator iterator = source.entrySet().iterator(); iterator.hasNext();)
      {
         Map.Entry entry = (Map.Entry)iterator.next();
         map.put(objectCopier.copy(entry.getKey(), referenceMap),
               objectCopier.copy(entry.getValue(), referenceMap));
      }
      return map;
   }

   private Set copySet(Set source, IdentityHashMap referenceMap)
   {
      // Create new mapo
      Set set = null;
      if(source instanceof HashSet)
      {
         set = new HashSet(source.size());
      }
      else if (source instanceof TreeSet)
      {
         Comparator comparator = ((TreeSet)source).comparator();
         comparator = (Comparator)objectCopier.copy(comparator, referenceMap);
         set = new TreeSet(comparator);
      }
      else
      {
         throw new CopyException("Source set is an unknown type: " + 
               source.getClass());
      }

      // put the new list into the reference map befrore deligating back to
      // the object copier (avoid infinite loops).
      referenceMap.put(source, set);

      // Copy the list elements to the new list
      for(Iterator iterator = source.iterator(); iterator.hasNext(); )
      {
         set.add(objectCopier.copy(iterator.next(), referenceMap));
      }
      return set;
   }
}

