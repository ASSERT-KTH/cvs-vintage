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
 * $Id: Random.java,v 1.4 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.util.Iterator;
import java.util.Set;

/**
 * You can use this helper class to get a randomized load balancing
 * algorithm between clustered servers.
 *
 * @author Simon Nieuviarts
 */
public class Random {

    /**
     * Stub size
     */
    private int len;

    /**
     * Array of stub
     */
    private StubData[] sd;

    /**
     * Last update
     */
    private Set previousSet;

    /**
     * Constructor
     *
     */
    public Random() {
        len = 0;
    }

    /**
     * Builds a random algorithm on a Collection of StubData objects.
     * @param c a Collection of StubData objects.
     */
    public Random(Set c) {
        reset(c);
    }

    /**
     * Reset the set of stub
     * @param stubDatas set of stub
     */
    private void reset(Set stubDatas) {
        len = stubDatas.size();
        sd = new StubData[len];
        Iterator it = stubDatas.iterator();
        for (int i = 0; i < len; i++) {
            StubData s = (StubData) it.next();
            sd[i] = s;
        }
    }

    /**
     * Update the set of stub only if different from the last update
     * @param stubs set of stub
     */
    public synchronized void update(Set stubs) {
        if (previousSet == stubs) {
            return;
        }
        reset(stubs);
    }

    /**
     * Get a stub randomly
     * @return stub
     * @throws NoServerException if no stub available
     */
    public synchronized StubData get()
            throws NoServerException {
        if (len <= 0) {
            throw new NoServerException();
        }
        int choice = SecureRandom.getInt(len);
        return sd[choice];
    }
}