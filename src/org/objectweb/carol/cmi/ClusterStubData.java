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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//TODO Rewrite comments, they are completely wrong
/**
 * Stubs to clustered objects extend this class. They may contain several
 * stubs to regular objects.
 * @author Simon Nieuviarts
 */
public class ClusterStubData {
    /**
     * Cluster configuration of this stub.
     */
    private ClusterConfig cfg;

    /**
     * A secure random seed, written at the ClusterStub creation.
     */
    private long randomSeed;

    // Updates on these variables have to be done with the lock on this
    private HashMap idMap;
    private HashMap stubMap;

    private ClusterStub cs;

    //private Map lbList = Collections.synchronizedMap(new WeakHashMap());
    private WeakList lbList = new WeakList();

    /**
     * for read(ObjectInput)
     */
    private ClusterStubData() {
    }

    /**
     * Construct a new cluster stub, containing a regular stub.
     * @param serverId the cluster id of the server where the remote object is
     * running.
     * @param stub the regular stub.
     */
    public ClusterStubData(ClusterId serverId, Remote stub, int factor)
        throws RemoteException {
        StubData sd = new StubData(serverId, stub, factor);
        idMap = new HashMap();
        idMap.put(serverId, sd);
        stubMap = new HashMap();
        stubMap.put(stub, sd);
        randomSeed = SecureRandom.getLong();
        buildClusterStub(stub.getClass());
    }

    /**
     * This must be called only for stubs without server id and load factor.
     * Can be used only for the cluster registry.
     * @param stub a stub to the registry.
     */
    public ClusterStubData(Remote stub) throws RemoteException {
        //first = stub;
        StubData sd = new StubData(null, stub, Config.DEFAULT_LOAD_FACTOR);
        stubMap = new HashMap();
        stubMap.put(stub, sd);
        randomSeed = SecureRandom.getLong();
        buildClusterStub(stub.getClass());
    }

    private static Class[] cnstr_params = new Class[] { ClusterStubData.class };

    private void buildClusterStub(Class cl) throws RemoteException {
        Class clusterStubClass;
        try {
            clusterStubClass = ClusterObject.getClusterStubClass(cl);
        } catch (ClassNotFoundException e1) {
            throw new RemoteException("No valid cluster stub class", e1);
        }

        Constructor cnstr;
        try {
            cnstr = clusterStubClass.getConstructor(cnstr_params);
            cs = (ClusterStub) cnstr.newInstance(new Object[] { this });
        } catch (Exception e) {
            throw new RemoteException("Can not instanciate cluster stub", e);
        }
    }

    public ClusterStub getClusterStub() {
        return cs;
    }

    public boolean isValidRemote(Remote r) {
        if (cs == null)
            return false;
        try {
            return cs.getClass().getName().equals(
                ClusterObject.getClusterStubClass(r.getClass()).getName());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Add a regular stub in this cluster stub.
     * @param serverId the cluster id of the server where the remote object
     * is running.
     * @param stub the regular stub.
     * @return false if the class of the stub is not the same the other objects
     * in the stub, or if factor is < 1.
     */
    public boolean setStub(ClusterId serverId, Remote stub, int factor) {
        if (idMap == null) {
            return false;
        }
        if (!isValidRemote(stub)) {
            return false;
        }
        StubData sd;
        try {
            sd = new StubData(serverId, stub, factor);
        } catch (RemoteException e) {
            return false;
        }
        synchronized (this) {
            idMap.put(serverId, sd);
            stubMap.put(stub, sd);
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
    public boolean setStub(Remote stub) {
        if (idMap != null) {
            return false;
        }
        if (!isValidRemote(stub)) {
            return false;
        }
        StubData sd;
        try {
            sd = new StubData(null, stub, Config.DEFAULT_LOAD_FACTOR);
        } catch (RemoteException e) {
            return false;
        }
        synchronized (this) {
            stubMap.put(stub, sd);
        }
        return true;
    }

    /**
     * Serialize this object. Used by the encapsulating ClusterStub.
     * @param out the output stream 
     * @throws IOException
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeObject(cfg);
        synchronized (this) {
            Iterator it = stubMap.entrySet().iterator();
            int l = stubMap.size();
            if (idMap == null) {
                out.writeInt(-l);
                for (int i = 0; i < l; i++) {
                    Map.Entry e = (Entry) it.next();
                    StubData sd = (StubData) e.getValue();
                    out.writeInt(sd.getFactor());
                    out.writeObject(sd.getStub());
                }
            } else {
                out.writeInt(l);
                for (int i = 0; i < l; i++) {
                    Map.Entry e = (Entry) it.next();
                    StubData sd = (StubData) e.getValue();
                    sd.getId().write(out);
                    out.writeInt(sd.getFactor());
                    out.writeObject(sd.getStub());
                }
            }
        }
        out.writeLong(randomSeed);
    }

    /**
     * Deserialize a ClusterStubData.
     * @param in input stream
     * @return the object
     * @throws IOException
     */
    public static ClusterStubData read(ObjectInput in, ClusterStub cs)
        throws IOException, ClassNotFoundException {
        ClusterStubData csd = new ClusterStubData();
        try {
            csd.cfg = (ClusterConfig) in.readObject();
            int l = in.readInt();
            if (l == 0) {
                throw new IOException("invalid serialized stub : 0 stubs");
            }
            HashMap idm = null;
            HashMap stubm = new HashMap();
            if (l < 0) {
                l = -l;
                for (int i = 0; i < l; i++) {
                    int f = in.readInt();
                    Remote r = (Remote) in.readObject();
                    StubData sd = new StubData(null, r, f);
                    stubm.put(r, sd);
                }
            } else {
                idm = new HashMap(l);
                for (int i = 0; i < l; i++) {
                    ClusterId id = ClusterId.read(in);
                    int f = in.readInt();
                    Remote r = (Remote) in.readObject();
                    StubData sd = new StubData(id, r, f);
                    stubm.put(r, sd);
                    idm.put(id, sd);
                }
            }
            csd.idMap = idm;
            csd.stubMap = stubm;
            SecureRandom.setSeed(
                csd.randomSeed = in.readLong() ^ System.currentTimeMillis());
        } catch (ClassCastException ce) {
            throw new IOException("invalid serialized stub" + ce.toString());
        }
        csd.cs = cs;
        return csd;
    }

    /*
        public void printStub() {
            System.out.println("Stub inside : ");
            java.util.Iterator i = stubMap.keySet().iterator();
            while (i.hasNext()) {
                System.out.println(i.next());
            }
        }
    */
    /**
     * This function fails if and only if the stub to remove is the last one.
     */
    public boolean removeStub(ClusterId serverId) {
        StubData sd;
        synchronized (this) {
            sd = (StubData) idMap.remove(serverId);
            if (sd == null) {
                return true;
            }
            if (idMap.size() == 0) {
                idMap.put(serverId, sd);
                return false;
            }
            stubMap.remove(sd.getStub());
            removeFromLB(sd);
        }
        return true;
    }

    /**
     * This function fails if and only if the stub to remove is the last one.
     */
    public boolean removeStub(Remote stub) {
        StubData sd; 
        synchronized (this) {
            sd = (StubData) stubMap.remove(stub);
            if (sd == null) {
                return true;
            }
            if (stubMap.size() == 0) {
                stubMap.put(stub, sd);
                return false;
            }
            idMap.remove(sd.getId());
            removeFromLB(sd);
        }
        return true;
    }

    //TODO Why not an addInLB ?
    private void removeFromLB(StubData sd) {
        Iterator it = lbList.iterator();
        while (it.hasNext()) {
            StubLB lb = (StubLB) it.next();
            lb.remove(sd);
        }
    }

    //TODO Who calls this ?
    public void setClusterConfig(ClusterConfig cfg) {
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

    public StubLB getRoundRobin() {
        StubLB lb;
        // Synchronize to avoid Exceptions for concurrent modifications
        synchronized (this) {
            lb = new RoundRobin(this, stubMap.values());
        }
        lbList.put(lb);
        return lb;
    }

    public StubLB getRandom() {
        StubLB lb;
        // Synchronize to avoid Exceptions for concurrent modifications
        synchronized (this) {
            lb = new Random(this, stubMap.values());
        }
        lbList.put(lb);
        return lb;
    }
}
