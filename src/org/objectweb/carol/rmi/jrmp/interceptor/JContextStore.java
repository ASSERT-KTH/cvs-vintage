/*
 * @(#) JContextStore.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
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
 *
 */
package org.objectweb.carol.rmi.jrmp.interceptor;

import java.util.ArrayList;

/**
 * Class <code>JContextObjectStore</code> is the CAROL JRMP Client Interceptor Contexts
 * Storage System
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 10/03/2003
 */
public class JContextStore {


    private static int counter = 0;

    public static ArrayList lists = new ArrayList();

    // The number of arraylists MAX must be less that MASK
    private static final int MAX = 100;
    // MASK is used to divide the key into the two indexes.
    private static final int MASK = 256;

    static {
        int i = 0;
        while (i != MAX) {
            lists.add(new ArrayList());
            i++;
        }

    }

    /**
     * Stote an object context  
     */
    public static int storeObject(Object ob) {

        // The context is often null so return a key that can be decoded
        // quickly. This coresponding to a "no context send"
        if (ob == null) {
            return -2;
        }

        int i = 0;
        ArrayList ar;

        // pick the next array list to use
        synchronized (lists) {
            counter++;
            if (counter == MAX) {
                counter = 0;
            }
            i = counter;
        }

        ar = (ArrayList) lists.get(i);

        // add the object at position j.
        int j;
        synchronized (ar) {
            ar.add(ob);
            j = ar.size() - 1;
        }

        i = j * MASK + i;
        return i;
    }


    /**
     * Get an object from the store and remove it from the arrayList. Mark
     * empty slots in the arrayList with Boolean.FALSE.
     *
     */

    public static Object getObject(int key) {

        if (key < 0) {
            return null;
        }
        Object ob;
        int i = key % MASK;
        int j = key / MASK;
        ArrayList ar = (ArrayList) lists.get(i);

        synchronized (ar) {
            ob = ar.get(j);
            ar.set(j, Boolean.FALSE);
            int k = ar.size() - 1;

            // only remove keys from the end so as not to alter
            // the index of other keys.
            while (k != -1 && (ar.get(k) == Boolean.FALSE)) {
                ar.remove(k);
                k--;
            }
        }
        return ob;
    }


}


