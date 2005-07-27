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
 * $Id: RoundRobin.java,v 1.4 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.util.Iterator;
import java.util.Set;

/**
 * You can use this helper class to get a round robin load balacing
 * algorithm between clustered servers.
 *
 * @author Simon Nieuviarts
 */
public class RoundRobin {
    /**
     * Nb of stubs
     */
    private int len;

    /**
     * Array of stubs
     */
    private StubData[] sd;

    /**
     * Array of Stub's load
     */
    private double[] load;

    /**
     * Minimum load
     */
    private double minLoad;

    /**
     * Last set of stubs
     */
    private Set previousSet = null;

    /**
     * Constructor
     *
     */
    public RoundRobin() {
        len = 0;
    }

    /**
     * Builds a round robin algorithm on a Collection of StubData objects.
     * @param c a Collection of StubData objects.
     */
    public RoundRobin(Set c) {
        reset(c);
    }

    /**
     * Reset the set of stubs
     * @param stubs set of stubs
     */
    private void reset(Set stubs) {
        previousSet = stubs;
        len = stubs.size();
        sd = new StubData[len];
        load = new double[len];
        Iterator it = stubs.iterator();
        for (int i = 0; i < len; i++) {
            StubData s = (StubData) it.next();
            sd[i] = s;
        }

        /* a random start choice
         */
        for (int i = 0; i < SecureRandom.getInt(len); i++) {
            load[i] = sd[i].getLoadIncr();
        }
    }

    /**
     * Ensure a minimum nb of entries in the stubs list
     * @param minCapacity the minimum nb of entries
     */
    private synchronized void ensureCapacity(int minCapacity) {
        int old = sd.length;
        if (old >= minCapacity) {
            return;
        }
        int l = (old * 3) / 2 + 1;
        if (l < minCapacity) {
            l = minCapacity;
        }
        StubData[] nsd = new StubData[l];
        double[] nload = new double[l];
        System.arraycopy(sd, 0, nsd, 0, old);
        System.arraycopy(load, 0, nload, 0, old);
        sd = nsd;
        load = nload;
    }

/*
    public synchronized void add(StubData sd) {
        ensureCapacity(len + 1);
        this.sd[len] = sd;
        load[len] = minLoad;
        len++;
    }

    public synchronized void remove(StubData sd) {
        for (int i = 0; i < len; i++) {
            if (this.sd[i] == sd) {
                len--;
                this.sd[i] = this.sd[len];
                this.sd[len] = null;
                load[i] = load[len];
                return;
            }
        }
    }
*/

    /**
     * Update the set of stubs
     * @param stubs new set of stubs
     */
    public synchronized void update(Set stubs) {
        if (previousSet == stubs) {
            return;
        }
        // Should only insert and remove new objects and not change state for others
        reset(stubs);
    }

    /**
     * Get a stub according to the RR algorithm
     * @return stub data
     * @throws NoServerException if no stub available
     */
    public synchronized StubData get()
            throws NoServerException {
        double min = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < len; i++) {
            double l = load[i];
            if (l < min) {
                min = l;
                index = i;
            }
        }

        if (index < 0) {
            throw new NoServerException();
        }

        // to avoid overflow, restart values when the min is relatively high
        if (min >= 100.0) {
            for (int i = 0; i < len; i++) {
                load[i] -= min;
            }
            min = 0;
        }

        StubData s = sd[index];
        load[index] += s.getLoadIncr();
        return s;
    }
}