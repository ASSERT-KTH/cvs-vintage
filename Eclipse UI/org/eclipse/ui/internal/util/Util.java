/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Util {

	public final static SortedMap EMPTY_SORTED_MAP = Collections.unmodifiableSortedMap(new TreeMap());
	public final static SortedSet EMPTY_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet());
	public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	public static void assertInstance(Object object, Class c) {
		if (object == null || c == null)
			throw new NullPointerException();
		else if (!c.isInstance(object))
			throw new IllegalArgumentException();
	}
	
	public static int compare(Comparable left, Comparable right) {
		if (left == null && right == null)
			return 0;	
		else if (left == null)
			return -1;	
		else if (right == null)
			return 1;
		else
			return left.compareTo(right);
	}

	public static int compare(Comparable[] left, Comparable[] right) {
		if (left == null && right == null)
			return 0;	
		else if (left == null)
			return -1;	
		else if (right == null)
			return 1;
		else {
			int l = left.length;
			int r = right.length;

			if (l != r)
				return l - r;
			else {
				for (int i = 0; i < l; i++) {
					int compareTo = compare(left[i], right[i]);

					if (compareTo != 0)
						return compareTo;
				}

				return 0;
			}
		}
	}

	public static int compare(List left, List right) {
		if (left == null && right == null)
			return 0;
		else if (left == null)
			return -1;
		else if (right == null)
			return 1;
		else {
			int l = left.size();
			int r = right.size();

			if (l != r)
				return l - r;
			else {
				for (int i = 0; i < l; i++) {
					int compareTo = compare((Comparable) left.get(i), (Comparable) right.get(i)); 
					
					if (compareTo != 0)
						return compareTo;
				}

				return 0;
			}
		}
	}

	public static void diff(Set left, Set right, Set leftOnly, Set rightOnly) {
		if (left == null || right == null || leftOnly == null || rightOnly == null)
			throw new NullPointerException();
	
		Iterator iterator = left.iterator();
			
		while (iterator.hasNext()) {
			Object object = iterator.next();
				
			if (!right.contains(object))
				leftOnly.add(object);
		}
	
		iterator = right.iterator();
			
		while (iterator.hasNext()) {
			Object object = iterator.next();
				
			if (!left.contains(object))
				rightOnly.add(object);		
		}
	}

	public static void diff(Map left, Map right, Set leftOnly, Set different, Set rightOnly) {
		if (left == null || right == null || leftOnly == null || different == null || rightOnly == null)
			throw new NullPointerException();
	
		Iterator iterator = left.keySet().iterator();
			
		while (iterator.hasNext()) {
			Object key = iterator.next();
				
			if (!right.containsKey(key))
				leftOnly.add(key);
			else if (!Util.equals(left.get(key), right.get(key)))
				different.add(key);								
		}
	
		iterator = right.keySet().iterator();
			
		while (iterator.hasNext()) {
			Object key = iterator.next();
				
			if (!left.containsKey(key))
				rightOnly.add(key);		
		}
	}

	public static boolean equals(Object left, Object right) {
		return left == null ? right == null : left.equals(right);
	}

	public static boolean isChildOf(Object[] left, Object[] right, boolean equals) {
		if (left == null || right == null)
			return false;
		else {
			int l = left.length;
			int r = right.length;

			if (r > l || !equals && r == l)
				return false;

			for (int i = 0; i < r; i++)
				if (!equals(left[i], right[i]))
					return false;

			return true;
		}
	}

	public static boolean isChildOf(List left, List right, boolean equals) {
		if (left == null || right == null)
			return false;
		else {
			int l = left.size();
			int r = right.size();

			if (r > l || !equals && r == l)
				return false;

			for (int i = 0; i < r; i++)
				if (!equals(left.get(i), right.get(i)))
					return false;

			return true;
		}
	}

	public static int hashCode(Object object) {
		return object != null ? object.hashCode() : 0;	
	}
	
	public static List safeCopy(List list, Class c) {
		if (list == null || c == null)
			throw new NullPointerException();
			
		list = Collections.unmodifiableList(new ArrayList(list));
		Iterator iterator = list.iterator();
	
		while (iterator.hasNext())
			assertInstance(iterator.next(), c);

		return list;
	}

	public static Map safeCopy(Map map, Class keyClass, Class valueClass) {
		if (map == null || keyClass == null || valueClass == null)
			throw new NullPointerException();
			
		map = Collections.unmodifiableMap(new HashMap(map));
		Iterator iterator = map.entrySet().iterator();
	
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertInstance(entry.getKey(), keyClass);
			assertInstance(entry.getValue(), valueClass);
		}
		
		return map;
	}

	public static Set safeCopy(Set set, Class c) {
		if (set == null || c == null)
			throw new NullPointerException();
			
		set = Collections.unmodifiableSet(new HashSet(set));
		Iterator iterator = set.iterator();
	
		while (iterator.hasNext())
			assertInstance(iterator.next(), c);

		return set;
	}

	public static SortedMap safeCopy(SortedMap sortedMap, Class keyClass, Class valueClass) {
		if (sortedMap == null || keyClass == null || valueClass == null)
			throw new NullPointerException();
			
		sortedMap = Collections.unmodifiableSortedMap(new TreeMap(sortedMap));
		Iterator iterator = sortedMap.entrySet().iterator();
	
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertInstance(entry.getKey(), keyClass);
			assertInstance(entry.getValue(), valueClass);
		}
		
		return sortedMap;
	}
	
	public static SortedSet safeCopy(SortedSet sortedSet, Class c) {
		if (sortedSet == null || c == null)
			throw new NullPointerException();
			
		sortedSet = Collections.unmodifiableSortedSet(new TreeSet(sortedSet));
		Iterator iterator = sortedSet.iterator();
	
		while (iterator.hasNext())
			assertInstance(iterator.next(), c);

		return sortedSet;
	}	

	public static String translateString(ResourceBundle resourceBundle, String key) {
		return Util.translateString(resourceBundle, key, key, true, true);
	}

	public static String translateString(ResourceBundle resourceBundle, String key, String string, boolean signal, boolean trim) {
		if (resourceBundle != null && key != null)
			try {
				final String translatedString = resourceBundle.getString(key);
				
				if (translatedString != null)
					return trim ? translatedString.trim() : translatedString;
			} catch (MissingResourceException eMissingResource) {
				if (signal)
					System.err.println(eMissingResource);
			}

		return trim ? string.trim() : string;
	}

	private Util() {
	}
}
