/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 * $Id: JLocalObjectStore.java,v 1.3 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.server;

import java.util.ArrayList;

/**
 * The class is a naming context allocating integer identifier. This integer
 * value is divided in two parts in order to reduce the synchronizaion
 * conflicts. The 8 (MAX_SIZE constant) right bits are used to hash identifiers.
 * The null value are stored with a special identifier: -1
 * @author riviereg, sebastien chassande-barrioz
 */
public class JLocalObjectStore {

    private static int counter = 0;

    // The number of arraylists MAX must be less that MASK
    private static final int MAX = 100;

    // MASK is used to divide the key into the two indexes.
    private static final int MASK = 255;

    private static final int MASK_SIZE = 8;

    private static final Object EMPTY_SLOT = Boolean.FALSE;

    public static ArrayList[] lists = new ArrayList[MAX];

    static {
        for (int i = 0; i < MAX; i++) {
            lists[i] = new ArrayList();
        }

    }

    /**
     * Exports an object and allocates an integer identifier.
     */
    public static int storeObject(Object ob) {
        // The context is often null so return a key that can be decoded
        // quickly. This coresponding to a "no context send"
        if (ob == null) {
            return -1;
        }

        // pick the next array list to use
        int i = 0;
        synchronized (lists) {
            counter++;
            if (counter == MAX) {
                counter = 0;
            }
            i = counter;
        }
        ArrayList ar = lists[i];

        int j;
        synchronized (ar) {
            j = ar.indexOf(EMPTY_SLOT);
            if (j == -1) {
                // add the object at the end of the list
                j = ar.size();
                ar.add(ob);
            } else {
                // reuse an empty slot in order reduce the
                // memory cost
                ar.set(j, ob);
            }
        }
        return i + (j << MASK_SIZE);
    }

    /**
     * lookup an object by its integer identifier.
     * @param key is the object identifier
     * @return the Object associated to the identifier, or a null value if no
     *         object was found.
     */
    public static Object getObject(int key) {
        if (key == -1) {
            return null;
        }
        int j = key >> MASK_SIZE;
        ArrayList ar = lists[key & MASK];
        try {
            //First attemp without synchronization in order to
            // optimize the lookup
            return ar.get(j);
        } catch (RuntimeException e) {
            //When new elements are stored, the ArrayList can be
            // resized and then produces an Exception. With a
            // synchronized, access this bad case is avoided.
            synchronized (ar) {
                return (ar.size() > j ? ar.get(j) : null);
            }
        }
    }

    /**
     * Unexport an object from the NamingContext. Empty slots are full with
     * EMPTY_SLOT.
     */
    public static Object removeObject(int key) {
        if (key < 0) {
            return null;
        }
        Object ob;
        ArrayList ar = lists[key & MASK];
        int j = key >> MASK_SIZE;
        synchronized (ar) {
            ob = ar.get(j);
            ar.set(j, EMPTY_SLOT);
            int k = ar.size() - 1;

            // clean up last elements if possible
            while (k != -1 && (ar.get(k) == EMPTY_SLOT)) {
                ar.remove(k);
                k--;
            }
        }
        return ob;
    }
}