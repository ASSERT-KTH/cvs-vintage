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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author nieuviar
 *
 */
public class StubData {
    private ClusterId id;
    private byte[] serializedStub;
    private Object stub;
    private int factor;
    private double loadIncr; // Is lower or equal to 1.0

    public StubData(ClusterId id, byte[] serStub, int factor) throws RemoteException {
        if (factor < 1) {
            throw new RemoteException("bad load factor : " + factor);
        }
        this.id = id;
        this.serializedStub = serStub;
        this.stub = null;
        this.factor = factor;
        this.loadIncr = 1.0 / (double)factor;
    }

    public StubData(ClusterId id, Remote stub, int factor) throws RemoteException {
        if (factor < 1) {
            throw new RemoteException("bad load factor : " + factor);
        }
        this.id = id;
        this.serializedStub = null;
        this.stub = stub;
        this.factor = factor;
        this.loadIncr = 1.0 / (double)factor;
    }

    StubData(Remote stub) {
        this.id = null;
        this.serializedStub = null;
        this.stub = stub;
        this.factor = Config.DEFAULT_RR_FACTOR;
        this.loadIncr = 1.0 / (double)Config.DEFAULT_RR_FACTOR;
    }

    public ClusterId getId() {
        return id;
    }

    public byte[] getSerializedStub() throws IOException {
        if (serializedStub == null) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            CmiOutputStream out = new CmiOutputStream(outStream);
            out.writeObject(stub);
            out.flush();
            serializedStub = outStream.toByteArray();
        }
        return serializedStub;
    }

    public Remote getStub() throws RemoteException {
        Object s = getStubOrException();
        if (s instanceof Remote) {
            return (Remote)s;
        }
        throw (RemoteException)s;
    }

    public Object getStubOrException() {
        if (stub == null) {
            ByteArrayInputStream inStream = new ByteArrayInputStream(serializedStub);
            try {
                CmiInputStream in = new CmiInputStream(inStream);
                stub = (Remote)in.readObject();
            } catch (IOException e) {
                stub = new RemoteException(e.toString());
            } catch (ClassNotFoundException e) {
                stub = new RemoteException(e.toString());
            }
        }
        return stub;
    }

    public double getLoadIncr() {
        return loadIncr;
    }

    public int getFactor() {
        return factor;
    }

    public String toString() {
        String str = "[id:" + id + ",stub:";
        Object o = getStubOrException();
        if (o instanceof Remote) {
            return str + o.toString() + "]";
        } else {
            return str + "serialized]";
        }
    }

    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return System.identityHashCode(this);
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (id == null) return false;
        if (!(obj instanceof StubData)) return false; 
        StubData sd = (StubData)obj;
        return id.equals(sd.id);
    }
}
