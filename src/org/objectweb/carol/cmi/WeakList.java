/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Register objects in a list via weak references. It is possible to get an iterator to know
 * objects still alive. 
 * @author nieuviar
 *
 */
public class WeakList {
    private final Object listHead = new Object();
    private WeakLink listStart;

    private static class WeakLink extends WeakReference {
        public WeakLink next;
        public WeakLink prev;
        WeakLink(Object o) {
            super(o);
        }
    }

    public WeakList() {
        listStart = new WeakLink(listHead);
        listStart.next = listStart;
        listStart.prev = listStart;
    }

    public void put(Object o) {
        WeakLink n = new WeakLink(o);
        synchronized (listHead) {
            WeakLink prev = listStart;
            WeakLink next = listStart.next;
            n.prev = prev;
            n.next = next;
            prev.next = n;
            next.prev = n;
        }
    }

    private class ListIterator implements Iterator {
        private WeakLink link = listStart;
        private Object obj;
        private boolean isNext = false;

        public ListIterator() {
            //pinNext();
        }

        private void pinNext() {
            WeakLink l = link.next;
            Object o = l.get();
            if (o == null) {
                // Remove links with null objects
                synchronized (listHead) {
                    l = link;
                    do {
                        l = l.next;
                        o = l.get();
                    } while (o == null);
                    link.next = l;
                    l.prev = link;
                }
            }
            link = l;
            obj = o;
            isNext = true;
        }

        public boolean hasNext() {
            if (!isNext) {
                pinNext();
            }
            return obj != listHead;
        }

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

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator iterator() {
        return new ListIterator();
    }
}
