/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: WeakValueHashtable.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.util.HashMap;

/**
 * A Map that remove its entries when the corresponding values are no
 * more referenced elsewhere. The key and the value can not be the same
 * object : they would never be removed.
 * It is synchronized.
 *
 * @author Simon Nieuviarts
 */
public class WeakValueHashtable {

    /**
     * The Hasmap
     */
    private HashMap table = new HashMap();

    /**
     * A value (which is removed when are no more referenced)
     * @author Simon Nieuviarts
     *
     */
    private class Value extends WeakRef {

        /**
         * The key of the object
         */
        Object key;

        /**
         * Constructor
         * @param key key
         * @param obj Value object
         */
        public Value(Object key, Object obj) {
            super(obj);
            this.key = key;
        }
        /**
         * Remove the value when no more reference
         */
        protected void remove() {
            synchronized (WeakValueHashtable.this) {
                Value v = (Value) table.remove(key);
                if ((v == null) || (v == this)) return;
                table.put(v.key, v);
            }
        }
    }

    /**
     * Put an entry in the hashmap
     * @param key key object
     * @param value value object
     * @return value if ok, otherwise null
     */
    public Object put(Object key, Object value) {
        if (key == value) {
            throw new UnsupportedOperationException();
        }
        if (value == null) {
            throw new NullPointerException();
        }
        Value v = new Value(key, value);
        synchronized (this) {
            v = (Value) table.put(key, v);
        }
        if (v == null) {
            return null;
        }
        return v.get();
    }

    /**
     * Get (extract) an entry in the hashmap
     * @param key key
     * @return Value object or null if not found
     */
    public Object get(Object key) {
        synchronized (this) {
            Value v = (Value) table.get(key);
            if (v == null) {
                return null;
            }
            Object o = v.get();
            if (o != null) {
                return o;
            }
            table.remove(key);
        }
        return null;
    }

    /**
     * Remove an entry in the hashmap
     * @param key key object
     * @return object removed or null if not found
     */
    public Object remove(Object key) {
        Value v;
        synchronized (this) {
            v = (Value) table.remove(key);
        }
        if (v == null) {
            return null;
        }
        return v.get();
    }

    /**
     * Count the number of entries in the hashmap
     * @param t hashtable
     */
    private static void count(WeakValueHashtable t) {
        System.out.println("Walk through the table");
        long t1 = System.currentTimeMillis();
        int count = 0;
        synchronized (t) {
            java.util.Iterator it = t.table.keySet().iterator();
            while (it.hasNext()) {
                it.next();
                count++;
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println(count + " objects in " + (t2 - t1) + " ms");
    }

    /**
     * Test purposes
     * @param argv program arg
     */
    public static void main(String[] argv) {
        long t0 = System.currentTimeMillis();
        WeakValueHashtable t = new WeakValueHashtable();
        java.util.LinkedList l2 = new java.util.LinkedList();
        System.out.println("Fill in");
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            Object o = new Object();
            if (i < 12345) {
                o = new Integer(i);
                l2.add(o);
            }
            t.put(new Integer(i), o);
        }
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) + " ms");
        count(t);
        System.gc();
        count(t);
        System.gc();
        count(t);
        System.gc();
        count(t);
        System.out.println("total time " + (System.currentTimeMillis() - t0) + " ms");
        System.out.println("obj[345] : " + t.get(new Integer(345)));
    }
}
