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
 * $Id: WeakRef.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

/**
 * This <code>WeakRef</code> is a <code>WeakReference</code> has its method
 * <code>remove()</code> called when the corresponding object has been
 * garbage collected.
 *
 * @author Simon Nieuviarts
 */
public abstract class WeakRef extends WeakReference {

    /**
     * Creates a new WeakRef instance
     * @param obj object
     */
    public WeakRef(Object obj) {
        super(obj, rQueue);
    }

    /**
     * Remove the current instance
     */
    protected abstract void remove();

    /**
     * ReferenceQueue ref
     */
    private static ReferenceQueue rQueue = new ReferenceQueue();

    /**
     * Thread processing the
     */
    private static Thread rQueueThread = new RefQueueFlush();

    /**
     * Start the ref queue flush
     */
    static {
        rQueueThread.setDaemon(true);
        rQueueThread.start();
    }

    /**
     * Daemon which is in charge to remove the entries in the ReferenceQueue
     * @author Simon Nieuviarts
     *
     */
    private static class RefQueueFlush extends Thread {

        /**
         * Default constructor
         */
        public RefQueueFlush() {
        }

        /**
         * Thread body
         */
        public void run() {
            while (true) {
                try {
                    WeakRef wr = (WeakRef) rQueue.remove();
                    wr.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Ignored
                }
            }
        }
    }
}
