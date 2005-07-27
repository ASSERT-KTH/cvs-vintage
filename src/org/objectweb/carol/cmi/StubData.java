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
 * $Id: StubData.java,v 1.4 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Contains a stub to a single instance in the cluster of a clustered object
 * and related data.
 *
 * @author Simon Nieuviarts
 */
public class StubData {

    /**
     * ObjectId of the remote object
     */
    private ObjectId objectId;

    /**
     * Serialized form of the stub
     */
    private byte[] serializedStub;

    /**
     * Stub
     */
    private Object stub;

    /**
     * Factor used for Distibuting
     */
    private int factor;

    /**
     * Load increment used for Distributing
     */
    private double loadIncr; // Is lower or equal to 1.0

    /**
     * Creates a new stub
     * @param objectId object identifier
     * @param serStub serialized stub
     * @param factor factor
     * @throws RemoteException if prm are not allowed
     */
    public StubData(ObjectId objectId, byte[] serStub, int factor)
            throws RemoteException {
        if (factor < 1) {
            throw new RemoteException("bad load factor : " + factor);
        }
        this.objectId = objectId;
        this.serializedStub = serStub;
        this.stub = null;
        this.factor = factor;
        this.loadIncr = 1.0 / (double) factor;
    }

    /**
     * Creates a new stub
     * @param objectId object identifier
     * @param stub Remote object stub
     * @param factor factor
     * @throws RemoteException if prm are not allowed
     */
    public StubData(ObjectId objectId, Remote stub, int factor)
            throws RemoteException {
        if (factor < 1) {
            throw new RemoteException("bad load factor : " + factor);
        }
        this.objectId = objectId;
        this.serializedStub = null;
        this.stub = stub;
        this.factor = factor;
        this.loadIncr = 1.0 / (double) factor;
    }

    /**
     * Creates a new stub without object id
     * Used only by RegistryStubList, to create a list of stubs to a registry
     * @param serStub serialized stub
     * @param factor factor
     * @throws RemoteException if prm are not allowed
     */
    StubData(Remote stub, int factor) throws RemoteException {
        this(null, stub, factor);
    }

    /**
     * Get the server id associated with the stub
     * @return cluster id
     */
    public ClusterId getServerId() {
        return objectId.getServerId();
    }

    /**
     * Get the ObjectId ref of the stub
     * @return objectId
     */
    public ObjectId getObjectId() {
        return objectId;
    }

    /**
     * Get the serialized stub
     * @return serialized buffer
     */
    public byte[] getSerializedStub() {
        if (serializedStub == null) {
            // XXX throw an exception
            /*ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             CmiOutputStream out = new CmiOutputStream(outStream);
             out.writeObject(stub);
             out.flush();
             serializedStub = outStream.toByteArray();*/
            TraceCarol
                    .error("StubData.getSerializedStub() called when serializedStub == null");
        }
        return serializedStub;
    }

    /**
     * Get the Remote stub
     * @return Remote object
     * @throws RemoteException, if stub can't be retrieved or not Remote object
     */
    public Remote getStub() throws RemoteException {
        Object s = getStubOrException();
        if (s instanceof Remote) {
            return (Remote) s;
        }
        throw (RemoteException) s;
    }

    /**
     * Get the stub
     * @return Remote object
     * @throws RemoteException
     */
    public Object getStubOrException() {
        if (stub == null) {
            ByteArrayInputStream inStream = new ByteArrayInputStream(
                    serializedStub);
            try {
                CmiInputStream in = new CmiInputStream(inStream);
                stub = (Remote) in.readObject();
            } catch (IOException e) {
                stub = new RemoteException(e.toString());
            } catch (ClassNotFoundException e) {
                stub = new RemoteException(e.toString());
            }
        }
        return stub;
    }

    /**
     * @return load incr
     */
    public double getLoadIncr() {
        return loadIncr;
    }

    /**
     * @return factor
     */
    public int getFactor() {
        return factor;
    }

    /**
     * @return readable form of the stub
     */
    public String toString() {
        String str = "[objectId:" + objectId + ",stub:";
        Object o = getStubOrException();
        if (o instanceof Remote) {
            return str + o.toString() + "]";
        } else {
            return str + "serialized]";
        }
    }

    /**
     * Compute a hashcode for this
     * @return hashcode
     */
    public int hashCode() {
        if (objectId != null) {
            return objectId.hashCode();
        }
        return System.identityHashCode(this);
    }

    /**
     * Compares an object with this one
     * @param obj object to compare
     * @return true if equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (objectId == null) {
            return false;
        }
        if (!(obj instanceof StubData)) {
            return false;
        }
        StubData sd = (StubData) obj;
        return objectId.equals(sd.objectId);
    }
}