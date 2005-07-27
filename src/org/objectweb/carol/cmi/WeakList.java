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
 * $Id: WeakList.java,v 1.3 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Register objects in a list via weak references. It is possible to get an
 * iterator to walk through alive objects.
 *
 * @author Simon Nieuviarts
 */
public class WeakList {

    /**
     * Head of the list
     */
    private final Object listHead = new Object();

    /**
     * Start of the list
     */
    private WeakLink listStart;

    /**
     * Wrap class to link list item
     * @author Simon Nieuviarts
     *
     */
    private class WeakLink extends WeakRef {

        /**
         * Next element
         */
        public WeakLink next;

        /**
         * Previous element
         */
        public WeakLink prev;

        /**
         * New element
         * @param o object embedded in the wrapper
         */
        WeakLink(Object o) {
            super(o);
        }

        /**
         * Remove element form the list
         */
        protected void remove() {
            synchronized (listHead) {
                WeakLink prev = this.prev;
                WeakLink next = this.next;
                prev.next = next;
                next.prev = prev;
                this.prev = this;
                this.next = this;
            }
        }
    }

    /**
     * Creates a new linked list
     */
    public WeakList() {
        listStart = new WeakLink(listHead);
        listStart.next = listStart;
        listStart.prev = listStart;
    }

    /**
     * Add an element to the list
     * @param o object to insert
     */
    public void add(Object o) {
        WeakLink n = new WeakLink(o);
        synchronized (listHead) {
            WeakLink next = listStart;
            WeakLink prev = listStart.prev;
            n.prev = prev;
            n.next = next;
            prev.next = n;
            next.prev = n;
        }
    }

    /**
     * Iterator to walk thru the WeakLink list
     * @author Simon Nieuviarts
     *
     */
    private class ListIterator implements Iterator {

        /**
         * Current element
         */
        private WeakLink link = listStart;

        /**
         * Object embedded in the cel
         */
        private Object obj;

        /**
         * End of the list ?
         */
        private boolean isNext = false;

        /**
         * Default Construtor
         *
         */
        public ListIterator() {
            //pinNext();
        }

        /**
         * Extract next element
         */
        private void pinNext() {
            WeakLink l;
            Object o;
            synchronized (listHead) {
                l = link.next;
                o = l.get();
                while (o == null) {
                    l = l.next;
                    o = l.get();
                }
                link.next = l;
                l.prev = link;
            }
            link = l;
            obj = o;
            isNext = true;
        }

        /**
         * @return true if the end of the list is reached, false otherwise
         */
        public boolean hasNext() {
            if (!isNext) {
                pinNext();
            }
            return obj != listHead;
        }

        /**
         * @return get the next element
         */
        public Object next() {
            if (!isNext) {
                pinNext();
            }
            if (obj == listHead) {
                throw new NoSuchElementException();
            }
            isNext = false;
            return obj;
        }

        /**
         * Remove current element
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Get iterator for the linked list
     * @return iterator
     */
    public Iterator iterator() {
        return new ListIterator();
    }

    /**
     * Count the number of elements in the linked lists
     * @param l
     */
    private static void count(WeakList l) {
        System.out.println("Walk through the list");
        long t1 = System.currentTimeMillis();
        int count = 0;
        Iterator it = l.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        long t2 = System.currentTimeMillis();
        System.out.println(count + " objects in " + (t2 - t1) + " ms");
    }

    /**
     * For test purposes
     * @param argv program arg
     */
    public static void main(String[] argv) {
        long t0 = System.currentTimeMillis();
        WeakList l = new WeakList();
        java.util.LinkedList l2 = new java.util.LinkedList();
        java.util.LinkedList l3 = new java.util.LinkedList();
        System.out.println("Fill in");
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            l.add(new Object());
        }
        for (int i=0; i<11111; i++) {
            Object o = new Object();
            l.add(o);
            l2.add(o);
        }
        for (int i = 0; i < 12345; i++) {
            Object o = new Object();
            l.add(o);
            l3.add(o);
        }
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) + " ms");
        count(l);
        System.gc();
        count(l);
        l2 = null;
        System.gc();
        count(l);
        System.gc();
        count(l);
        System.out.println("total time " + (System.currentTimeMillis() - t0) + " ms");
    }
}
