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

import java.rmi.Remote;
import java.util.ArrayList;

public class StubListRandomChooser {
    private Object[] stubs;
    private Object[] chooser;
    private int first;
    private int returned;

    public StubListRandomChooser(ArrayList stublist) {
        stubs = stublist.toArray();
        chooser = null;
        returned = 0;
    }

    public void restart() {
        returned = 0;
    }

    public Remote next() {
        int len = stubs.length;
        switch (returned) {
            case 0 :
                first = SecureRandom.getInt(len);
                returned = 1;
                return (Remote) stubs[first];
            case 1 :
                if (chooser == null)
                    chooser = new Object[len];
                for (int i = 0; i < len; i++)
                    chooser[i] = stubs[i];
                chooser[first] = stubs[0];
            default :
                if (returned >= len)
                    return null;
                int c = returned + SecureRandom.getInt(len - returned);
                Object obj = chooser[c];
                chooser[c] = chooser[returned];
                returned++;
                return (Remote) obj;
        }
    }

    public Remote get() {
        returned = 0;
        return next();
    }
}
