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

import java.util.Collection;
import java.util.Iterator;

//TODO synchronization ?
public class Random extends StubLB {
    private ClusterStubData csd;
    private int len;
    private StubData[] sd;

    /**
     * Builds a random algorithm on a Collection of StubData objects.
     * @param c a Collection of StubData objects.
     */
    public Random(ClusterStubData csd, Collection c) {
        this.csd = csd;
        len = c.size();
        sd = new StubData[len];
        Iterator it = c.iterator();
        for (int i = 0; i < len; i++) {
            StubData s = (StubData) it.next();
            sd[i] = s;
        }
    }

    private synchronized void ensureCapacity(int minCapacity) {
        int old = sd.length;
        if (old >= minCapacity)
            return;
        int l = (old * 3) / 2 + 1;
        if (l < minCapacity)
            l = minCapacity;
        StubData[] nsd = new StubData[l];
        System.arraycopy(sd, 0, nsd, 0, old);
        sd = nsd;
    }

    /**
     * This method must be called only by the ClusterStubData to ensure integrity
     * between this load balancer and the cluster stub.
     * @see org.objectweb.carol.cmi.lb.StubLB#add(org.objectweb.carol.cmi.StubData)
     */
    void add(StubData sd) {
        ensureCapacity(len + 1);
        this.sd[len] = sd;
        len++;
    }

    /**
     * This method must be called only by the ClusterStubData to ensure integrity
     * between this load balancer and the cluster stub.
     * @see org.objectweb.carol.cmi.lb.StubLB#remove(org.objectweb.carol.cmi.StubData)
     */
    void removeCallback(StubData s) {
        for (int i = 0; i < len; i++) {
            if (sd[i] == s) {
                len--;
                sd[i] = sd[len];
                sd[len] = null;
                return;
            }
        }
    }

    public synchronized StubData get() throws NoMoreStubException {
        if (len <= 0) {
            throw new NoMoreStubException();
        }
        int choice = SecureRandom.getInt(len);
        return sd[choice];
    }

    public synchronized StubData get(StubLBFilter f) throws NoMoreStubException {
        int n = SecureRandom.getInt(len);
        for (int i=0; i<len; i++) {
            StubData s = sd[n];
            if (!f.contains(s)) {
                return s;
            }
            n = (n + 1) % len;
        }
        throw new NoMoreStubException();
    }

    /**
     * @see org.objectweb.carol.cmi.StubLB#remove(java.rmi.Remote)
     */
    public void remove(StubData s) {
        csd.removeStubData(s);
    }
}
