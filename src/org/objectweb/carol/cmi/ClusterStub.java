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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stubs to clustered objects extend this class. They may contain several
 * stubs to regular objects.
 * @author Simon Nieuviarts
 */
public class ClusterStub implements Externalizable {
    /**
     * Cluster configuration of this stub.
     */
    private transient ClusterConfig cfg = null;

    /**
     * Table of regular stubs registered in this cluster stub. The keys are
     * server IDs.
     */
    private transient HashMap stubMap;

    /**
     * A secure random seed, written at the ClusterStub creation.
     */
    private transient long randomSeed;

    /**
     * The class of the regular stubs stored in this ClusterStub.
     * It is transient but always defined thanks to readResolve()
     */
    private transient Class regularStubClass = null;

    /**
     * An ArrayList, for random choices. Zeroed when stubMap is changed. It is rebuild
     * when accessed (through getList()).
     */
    private transient ArrayList list = null;

    /**
     * The first stub registered. Only useful for a cluster registry : it
     * is the stub to the registry to bind into.
     */
    private transient Remote first = null;

    /**
     * for Externalizable
     * TODO check where it is used
     */
    public ClusterStub() {
    }

    /**
     * Construct a new cluster stub, containing a regular stub.
     * @param serverId the cluster id of the server where the remote object is
     * running.
     * @param stub the regular stub.
     */
    protected ClusterStub(ClusterId serverId, Remote stub) {
        stubMap = new HashMap();
        stubMap.put(serverId, stub);
        randomSeed = SecureRandom.getLong();
        regularStubClass = stub.getClass();
    }

    /**
     * This must be called only for stubs without server id.
     * Can be used only for the cluster registry.
     * @param stub a stub to the registry.
     */
    protected ClusterStub(Remote stub) {
        first = stub;
        stubMap = new HashMap();
        stubMap.put(new Object(), stub);
        randomSeed = SecureRandom.getLong(); // XXX Loose time creating a random seed ?
        regularStubClass = stub.getClass();
    }

    /**
     * Add a regular stub in this cluster stub.
     * @param serverId the cluster id of the server where the remote object
     * is running.
     * @param stub the regular stub.
     * @return false if the class of the stub is not the same the other objects
     * in the stub.
     */
    protected boolean setStub(ClusterId serverId, Remote stub) {
        //TODO is it right ? Should not check classes instead ?
        if (!stub.getClass().getName().equals(regularStubClass.getName())) {
            return false;
        }
        synchronized (this) {
            stubMap.put(serverId, stub);
            list = null;
        }
        return true;
    }

    /**
     * Add a regular stub in this cluster stub.
     * Can be used only for the cluster registry.
     * @param stub a stub to a registry.
     * @return false if the class of the stub is not the same the other objects
     * in the stub.
     */
    protected boolean setStub(Remote stub) {
        if (!stub.getClass().getName().equals(regularStubClass.getName())) {
            return false;
        }
        synchronized (this) {
            stubMap.put(new Object(), stub);
            list = null;
        }
        return true;
    }

    public void printStub() {
        System.out.println("Stub inside : ");
        java.util.Iterator i = stubMap.keySet().iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }

    /**
     * This function fails if and only if the stub to remove is the last one.
     */
    public boolean removeStub(ClusterId serverId) {
        synchronized (this) {
            Object o = stubMap.remove(serverId);
            if (stubMap.size() == 0) {
                stubMap.put(serverId, o);
                return false;
            }
            if (o != null) {
                list = null;
            }
        }
        return true;
    }

    /**
     * This function fails if and only if the stub to remove is the last one.
     */
    public boolean removeStub(Remote stub) {
        synchronized (this) {
            if (stubMap.size() == 1)
                return false;
            if (Trace.CSTUB)
                Trace.out("CSTUB: Removing a stub");
            if (stubMap.values().remove(stub)) {
                list = null;
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Now " + stubMap.size() + " stub");
            }
        }
        return true;
    }

    /**
     * Called when a StubList is deserialized.
     * Setup the transient fields.
     * TODO remove, and merge in readObject.
     */
    private Object readResolve() throws ObjectStreamException {
        try {
            if (Trace.CSTUB)
                Trace.out("CSTUB: readResolve()");
            getList();
        } catch (RemoteException e) {
            throw new java.io.InvalidObjectException(e.getMessage());
        }
        SecureRandom.setSeed(randomSeed ^ System.currentTimeMillis());
        return this;
    }

    /**
     * Always get the list through this function : thread safe.
     */
    private ArrayList getList() throws RemoteException {
        if (list != null)
            return list;
        synchronized (this) {
            if (list != null)
                return list;
            ArrayList l = new ArrayList(stubMap.values());
            regularStubClass = l.get(0).getClass();
            return list = l;
        }
    }

    public Class getRegularStubClass() {
        return regularStubClass;
    }

    public StubListRandomChooser getRandomChooser() throws RemoteException {
        return new StubListRandomChooser(getList());
    }

    /**
     * Should be called by the constructor of class inheriting this one.
     */
    protected void setClusterConfig(ClusterConfig cfg) {
        this.cfg = cfg;
    }

    /**
     * You can safely assume it returns a non null structure if it is not
     * a stub to a cluster registry. cfg should have been initialized by
     * the constructor
     */
    public ClusterConfig getClusterConfig() {
        return cfg;
    }

    /**
     * May be called only on a fresh new stub to a cluster registry.
     * @return stub to the first registry listed in URL.
     * @throws RemoteException
     */
    protected Remote getFirst() throws RemoteException {
        if (first == null) {
            throw new RemoteException("not supported");
        }
        return first;
    }

    /** (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(cfg);
        synchronized (this) {
            Iterator it = stubMap.entrySet().iterator();
            int l = stubMap.size();
            out.writeInt(l);
            for (int i=0; i<l; i++) {
                Map.Entry e = (Entry) it.next();
                ClusterId id = (ClusterId) e.getKey();
                id.write(out);
                out.writeObject(e.getValue());
            }
        }
        out.writeLong(randomSeed);
    }

    /** (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            cfg = (ClusterConfig) in.readObject();
            int l = in.readInt(); 
            HashMap m = new HashMap(l);
            for (int i=0; i<l; i++) {
                ClusterId k = ClusterId.read(in);
                Object o = in.readObject();
                m.put(k, o);
            }
            stubMap = m;
            randomSeed = in.readLong();
        } catch (ClassCastException ce) {
            throw new IOException("invalid serialized stub : " + ce.toString());
        }
        
    }
}
