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
import java.rmi.RemoteException;

/**
 * @author nieuviar
 *
 */
public class StubData {
    private ClusterId id;
    private Remote stub;
    private int factor;
    private double loadIncr; // Is lower or equal to 1.0

    public StubData(ClusterId id, Remote stub, int factor) throws RemoteException {
        if (factor < 1) {
            throw new RemoteException("bad load factor : " + factor);
        }
        this.id = id;
        this.stub = stub;
        this.factor = factor;
        this.loadIncr = 1.0 / (double)factor;
    }

    public ClusterId getId() {
        return id;
    }

    public Remote getStub() {
        return stub;
    }

    public double getLoadIncr() {
        return loadIncr;
    }

    public int getFactor() {
        return factor;
    }
}
