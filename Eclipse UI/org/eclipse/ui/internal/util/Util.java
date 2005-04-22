/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.util;

import java.util.ArrayList;
import java.util.Collection;
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Util {

    public final static SortedMap EMPTY_SORTED_MAP = Collections
            .unmodifiableSortedMap(new TreeMap());

    public final static SortedSet EMPTY_SORTED_SET = Collections
            .unmodifiableSortedSet(new TreeSet());

    public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

    /**
     * Ensures that a string is not null. Converts null strings into empty
     * strings, and leaves any other string unmodified. Use this to help
     * wrap calls to methods that return null instead of the empty string.
     * Can also help protect against implementation errors in methods that
     * are not supposed to return null. 
     * 
     * @param input input string (may be null)
     * @return input if not null, or the empty string if input is null
     */
    public static String safeString(String input) {
        if (input != null) {
            return input;
        }

        return ZERO_LENGTH_STRING;
    }

    public static void assertInstance(Object object, Class c) {
        assertInstance(object, c, false);
    }

    public static void assertInstance(Object object, Class c, boolean allowNull) {
        if (object == null && allowNull)
            return;

        if (object == null || c == null)
            throw new NullPointerException();
        else if (!c.isInstance(object))
            throw new IllegalArgumentException();
    }

    public static int compare(boolean left, boolean right) {
        return left == false ? (right == true ? -1 : 0) : 1;
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

    public static int compare(int left, int right) {
        return left - right;
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
                    int compareTo = compare((Comparable) left.get(i),
                            (Comparable) right.get(i));

                    if (compareTo != 0)
                        return compareTo;
                }

                return 0;
            }
        }
    }

    public static int compare(Object left, Object right) {
        if (left == null && right == null)
            return 0;
        else if (left == null)
            return -1;
        else if (right == null)
            return 1;
        else if (left == right)
            return 0;
        else
            return compare(System.identityHashCode(left), System
                    .identityHashCode(right));
    }

    /**
     * An optimized comparison that uses identity hash codes to perform the
     * comparison between non- <code>null</code> objects.
     * 
     * @param left
     *            The left-hand side of the comparison; may be <code>null</code>.
     * @param right
     *            The right-hand side of the comparison; may be
     *            <code>null</code>.
     * @return <code>0</code> if they are the same, <code>-1</code> if left
     *         is <code>null</code>;<code>1</code> if right is
     *         <code>null</code>. Otherwise, the left identity hash code
     *         minus the right identity hash code.
     */
    public static final int compareIdentity(Object left, Object right) {
        if (left == null && right == null)
            return 0;
        else if (left == null)
            return -1;
        else if (right == null)
            return 1;
        else
            return System.identityHashCode(left)
                    - System.identityHashCode(right);
    }

    public static void diff(Map left, Map right, Set leftOnly, Set different,
            Set rightOnly) {
        if (left == null || right == null || leftOnly == null
                || different == null || rightOnly == null)
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

    public static void diff(Set left, Set right, Set leftOnly, Set rightOnly) {
        if (left == null || right == null || leftOnly == null
                || rightOnly == null)
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

    public static boolean endsWith(List left, List right, boolean equals) {
        if (left == null || right == null)
            return false;
        else {
            int l = left.size();
            int r = right.size();

            if (r > l || !equals && r == l)
                return false;

            for (int i = 0; i < r; i++)
                if (!equals(left.get(l - i - 1), right.get(r - i - 1)))
                    return false;

            return true;
        }
    }

    public static boolean endsWith(Object[] left, Object[] right, boolean equals) {
        if (left == null || right == null)
            return false;
        else {
            int l = left.length;
            int r = right.length;

            if (r > l || !equals && r == l)
                return false;

            for (int i = 0; i < r; i++)
                if (!equals(left[l - i - 1], right[r - i - 1]))
                    return false;

            return true;
        }
    }

    public static boolean equals(boolean left, boolean right) {
        return left == right;
    }

    public static boolean equals(int left, int right) {
        return left == right;
    }

    public static boolean equals(Object left, Object right) {
        return left == null ? right == null : ((right != null) && left
                .equals(right));
    }

    public static int hashCode(boolean b) {
        return b ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
    }

    public static int hashCode(int i) {
        return i;
    }

    public static int hashCode(Object object) {
        return object != null ? object.hashCode() : 0;
    }

    public static Collection safeCopy(Collection collection, Class c) {
        return safeCopy(collection, c, false);
    }

    public static Collection safeCopy(Collection collection, Class c,
            boolean allowNullElements) {
        if (collection == null || c == null)
            throw new NullPointerException();

        collection = Collections.unmodifiableCollection(new ArrayList(
                collection));
        Iterator iterator = collection.iterator();

        while (iterator.hasNext())
            assertInstance(iterator.next(), c, allowNullElements);

        return collection;
    }

    public static List safeCopy(List list, Class c) {
        return safeCopy(list, c, false);
    }

    public static List safeCopy(List list, Class c, boolean allowNullElements) {
        if (list == null || c == null)
            throw new NullPointerException();

        list = Collections.unmodifiableList(new ArrayList(list));
        Iterator iterator = list.iterator();

        while (iterator.hasNext())
            assertInstance(iterator.next(), c, allowNullElements);

        return list;
    }

    public static Map safeCopy(Map map, Class keyClass, Class valueClass) {
        return safeCopy(map, keyClass, valueClass, false, false);
    }

    public static Map safeCopy(Map map, Class keyClass, Class valueClass,
            boolean allowNullKeys, boolean allowNullValues) {
        if (map == null || keyClass == null || valueClass == null)
            throw new NullPointerException();

        map = Collections.unmodifiableMap(new HashMap(map));
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            assertInstance(entry.getKey(), keyClass, allowNullKeys);
            assertInstance(entry.getValue(), valueClass, allowNullValues);
        }

        return map;
    }

    public static Set safeCopy(Set set, Class c) {
        return safeCopy(set, c, false);
    }

    public static Set safeCopy(Set set, Class c, boolean allowNullElements) {
        if (set == null || c == null)
            throw new NullPointerException();

        set = Collections.unmodifiableSet(new HashSet(set));
        Iterator iterator = set.iterator();

        while (iterator.hasNext())
            assertInstance(iterator.next(), c, allowNullElements);

        return set;
    }

    public static SortedMap safeCopy(SortedMap sortedMap, Class keyClass,
            Class valueClass) {
        return safeCopy(sortedMap, keyClass, valueClass, false, false);
    }

    public static SortedMap safeCopy(SortedMap sortedMap, Class keyClass,
            Class valueClass, boolean allowNullKeys, boolean allowNullValues) {
        if (sortedMap == null || keyClass == null || valueClass == null)
            throw new NullPointerException();

        sortedMap = Collections.unmodifiableSortedMap(new TreeMap(sortedMap));
        Iterator iterator = sortedMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            assertInstance(entry.getKey(), keyClass, allowNullKeys);
            assertInstance(entry.getValue(), valueClass, allowNullValues);
        }

        return sortedMap;
    }

    public static SortedSet safeCopy(SortedSet sortedSet, Class c) {
        return safeCopy(sortedSet, c, false);
    }

    public static SortedSet safeCopy(SortedSet sortedSet, Class c,
            boolean allowNullElements) {
        if (sortedSet == null || c == null)
            throw new NullPointerException();

        sortedSet = Collections.unmodifiableSortedSet(new TreeSet(sortedSet));
        Iterator iterator = sortedSet.iterator();

        while (iterator.hasNext())
            assertInstance(iterator.next(), c, allowNullElements);

        return sortedSet;
    }

    public static boolean startsWith(List left, List right, boolean equals) {
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

    public static boolean startsWith(Object[] left, Object[] right,
            boolean equals) {
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

    public static String translateString(ResourceBundle resourceBundle,
            String key) {
        return Util.translateString(resourceBundle, key, key, true, true);
    }

    public static String translateString(ResourceBundle resourceBundle,
            String key, String string, boolean signal, boolean trim) {
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
    
    public static void arrayCopyWithRemoval(Object [] src, Object [] dst, int idxToRemove) {
    	if (src == null || dst == null || src.length - 1 != dst.length || idxToRemove < 0 || idxToRemove >= src.length)
    		throw new IllegalArgumentException();
    	
    	if (idxToRemove == 0) {
    		System.arraycopy(src, 1, dst, 0, src.length - 1);    		
    	}
    	else if (idxToRemove == src.length - 1) {
    		System.arraycopy(src, 0, dst, 0, src.length - 1);
    	}
    	else {
    		System.arraycopy(src, 0, dst, 0, idxToRemove);
    		System.arraycopy(src, idxToRemove + 1, dst, idxToRemove, src.length - idxToRemove - 1);
    	}    	
    }

    /**
     * Appends array2 to the end of array1 and returns the result
     * 
     * @param array1
     * @param array2
     * @return
     * @since 3.1
     */
    public static Object[] appendArray(Object[] array1, Object[] array2) {        
        Object[] result = new Object[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;               
    }
    
    private Util() {
    }

	/**
	 * Returns an interned representation of the given string
	 * @param string The string to intern
	 * @return The interned string
	 */
	public static String intern(String string) {
		return string == null ? null : string.intern();
	}
	
	/**
	 * Returns the result of converting a list of comma-separated tokens into an array.
	 * Used as a replacement for <code>String.split(String)</code>, to allow compilation
	 * against JCL Foundation (bug 80053).
	 * 
	 * @param prop the initial comma-separated string
	 * @param separator the separator characters
	 * @return the array of string tokens
	 * @since 3.1
	 */
	public static String[] getArrayFromList(String prop, String separator) {
		if (prop == null || prop.trim().equals("")) //$NON-NLS-1$
			return new String[0];
		ArrayList list = new ArrayList();
		StringTokenizer tokens = new StringTokenizer(prop, separator); 
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) //$NON-NLS-1$
				list.add(token);
		}
		return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[list.size()]);
	}
	
}
