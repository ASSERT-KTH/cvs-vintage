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
import java.lang.ref.ReferenceQueue;

/**
 * An object cache, to reduce memory usage by returning always the same instance for
 * equal objects.
 */
public class WeakCache {
    private java.util.HashMap table = new java.util.HashMap();

    private class WeakRef extends WeakReference {
        private int hash;

        public WeakRef(Object obj) {
            super(obj);
            hash = obj.hashCode();
        }

        public WeakRef(Object obj, ReferenceQueue rq) {
            super(obj, rq);
            hash = obj.hashCode();
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object obj) {
            // This first check is necessary to remove empty WeakRef from the table
            if (obj == this) {
                return true;
            }

            if (obj instanceof WeakRef) {
                Object o = this.get();
                return (o != null) && o.equals(((WeakRef) obj).get());
            } else
                return false;
        }

        public void remove() {
            synchronized (table) {
                table.remove(this);
            }
        }
    }

    private static ReferenceQueue rQueue = new ReferenceQueue();
    private static Thread rQueueThread = new RefQueueFlush();
    static {
        rQueueThread.setDaemon(true);
        rQueueThread.start();
    }

    //TODO Thread end ?
    private static class RefQueueFlush extends Thread {
        public RefQueueFlush() {
        }
        public void run() {
            while (true) {
                try {
                    WeakRef wr = (WeakRef) rQueue.remove();
                    wr.remove();
                } catch (InterruptedException e) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Registers an object in the cache or return an already registered equal object.
     * @param obj Object to register.
     * @return the copy to use.
     */
    public Object getCached(Object obj) {
        WeakRef wr = new WeakRef(obj, rQueue);
        synchronized (table) {
            WeakRef cached = (WeakRef) table.get(wr);
            if (cached == null) {
                table.put(wr, wr);
                return obj;
            }
            Object c = cached.get();
            if (c == null) {
                table.remove(cached);
                table.put(wr, wr);
                return obj;
            }
            return c;
        }
    }
}
