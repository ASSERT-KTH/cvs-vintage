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
import java.util.HashSet;
import java.util.Iterator;

/**
 * Stubs to clustered objects use this class. They may contain several
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

    private boolean stubDebug = false;

    private StubData firstSD;

    // Updates on these variables have to be done with the lock on this
    private HashMap idMap;
    private HashSet stubs;
    private volatile ClusterStub clusterStub;

    private WeakList lbList = new WeakList();


    /**
     * for read(ObjectInput)
     */
    private ClusterStubData() {
    }

    /**
     * Construct a new cluster stub data, containing a regular stub.
     * @param serverId the cluster id of the server where the remote object is
     * running.
     * @param stubSer the regular stub, serialized.
     * @param factor factor for round robin load lalancing.
     */
    public ClusterStubData(ClusterId serverId, byte[] stubSer, int factor)
        throws RemoteException {
        StubData sd = new StubData(serverId, stubSer, factor);
        firstSD = sd;
        idMap = new HashMap();
        idMap.put(serverId, sd);
        stubs = new HashSet();
        stubs.add(sd);
        randomSeed = SecureRandom.getLong();
        stubDebug = Config.isStubDebug();
    }

    /**
     * This must be called only for stubs without server id and load factor.
     * Can be used only for the cluster registry.
     * @param stub a stub to the registry.
     */
    ClusterStubData(ClusterRegistryInternal stub) throws RemoteException {
        StubData sd = new StubData(stub);
        firstSD = sd;
        idMap = null;
        stubs = new HashSet();
        stubs.add(sd);
        randomSeed = SecureRandom.getLong();
        stubDebug = Config.isStubDebug();
    }

    private static Class[] cnstrParams = new Class[] { ClusterStubData.class };

    public ClusterStub getClusterStub() throws RemoteException {
        ClusterStub cs = clusterStub;
        if (cs == null) {
            Remote stub;
            stub = firstSD.getStub();
            Class clusterStubClass;
            try {
                clusterStubClass =
                    ClusterObject.getClusterStubClass(stub.getClass());
            } catch (ClassNotFoundException e1) {
                throw new RemoteException("No valid cluster stub class for " + stub.getClass().getName());
            }
            Constructor cnstr;
            try {
                cnstr = clusterStubClass.getConstructor(cnstrParams);
                cs = (ClusterStub) cnstr.newInstance(new Object[] { this });
            } catch (Exception e) {
                throw new RemoteException("Can not instanciate cluster stub" + e.toString());
            }
            clusterStub = cs;
        }
        return cs;
    }

/*
    public boolean isValidRemote(Remote r) {
        try {
            return clusterStubClass.getName().equals(
                ClusterObject.getClusterStubClass(r.getClass()).getName());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
*/

    /**
     * Add a regular stub in this cluster stub.
     * @param serverId the cluster id of the server where the remote object
     * is running.
     * @param stub the regular stub.
     * @return false if the class of the stub is not the same the other objects
     * in the stub, or if factor is < 1.
     */
    public boolean setStub(ClusterId serverId, byte[] stubSer, int factor) {
        if (idMap == null) {
            return false;
        }
/*
        if (!isValidRemote(stub)) {
            return false;
        }
*/
                StubData sd;
        try {
            sd = new StubData(serverId, stubSer, factor);
        } catch (RemoteException e) {
            return false;
        }
        synchronized (this) {
            idMap.put(serverId, sd);
            stubs.add(sd);
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
    public boolean setStub(ClusterRegistryInternal stub) {
        if (idMap != null) {
            return false;
        }
        StubData sd = new StubData(stub);
        synchronized (this) {
            stubs.add(sd);
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
            Iterator it = stubs.iterator();
            int l = stubs.size();
            if (idMap == null) {
                out.writeInt(-l);
                for (int i = 0; i < l; i++) {
                    StubData sd = (StubData) it.next();
                    out.writeInt(sd.getFactor());
                    Object o = sd.getStubOrException();
                    if (o instanceof Remote) {
                        out.writeObject(o);
                    } else {
                        out.writeObject(sd.getSerializedStub());
                    }
                }
            } else {
                out.writeInt(l);
                for (int i = 0; i < l; i++) {
                    StubData sd = (StubData) it.next();
                    sd.getId().write(out);
                    out.writeInt(sd.getFactor());
                    Object o = sd.getStubOrException();
                    if (o instanceof Remote) {
                        out.writeObject(o);
                    } else {
                        out.writeObject(sd.getSerializedStub());
                    }
                }
            }
        }
        out.writeLong(randomSeed);
        out.writeBoolean(stubDebug);
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
        StubData first = null;
        try {
            csd.cfg = (ClusterConfig) in.readObject();
            int l = in.readInt();
            if (l == 0) {
                throw new IOException("invalid serialized stub : 0 stubs");
            }
            HashMap idm = null;
            HashSet stubs = new HashSet();
            if (l < 0) {
                l = -l;
                for (int i = 0; i < l; i++) {
                    int f = in.readInt();
                    Object obj = in.readObject();
                    System.err.println(obj.getClass().getName());
                    StubData sd;
                    if (obj instanceof Remote) {
                        sd = new StubData(null, (Remote) obj, f);
                    } else {
                        sd = new StubData(null, (byte[]) obj, f);
                    }
                    stubs.add(sd);
                    if (first == null) {
                        first = sd;
                    }
                }
            } else {
                idm = new HashMap(l);
                for (int i = 0; i < l; i++) {
                    ClusterId id = ClusterId.read(in);
                    int f = in.readInt();

                    Object obj = in.readObject();
                    StubData sd;
                    if (obj instanceof Remote) {
                        sd = new StubData(id, (Remote) obj, f);
                    } else {
                        sd = new StubData(id, (byte[]) obj, f);
                    }
                    stubs.add(sd);
                    idm.put(id, sd);
                    if (first == null) {
                        first = sd;
                    }
                }
            }
            csd.idMap = idm;
            csd.stubs = stubs;
            SecureRandom.setSeed(
                csd.randomSeed = in.readLong() ^ System.currentTimeMillis());
            csd.stubDebug = in.readBoolean();
        } catch (ClassCastException ce) {
            ce.printStackTrace();
            throw new IOException("invalid serialized stub " + ce.toString());
        }
        csd.clusterStub = cs;
        csd.firstSD = first;
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
            stubs.remove(sd);
            removeFromLB(sd);
        }
        return true;
    }

    /**
     * This function fails if and only if the stub to remove is the last one.
     */
    public boolean removeStubData(StubData sd) {
        synchronized (this) {
            if (!stubs.remove(sd)) {
                return true;
            }
            if (stubs.size() == 0) {
                stubs.add(sd);
                return false;
            }
            idMap.remove(sd.getId());
            removeFromLB(sd);
        }
        return true;
    }

    private void removeFromLB(StubData sd) {
        Iterator it = lbList.iterator();
        while (it.hasNext()) {
            StubLB lb = (StubLB) it.next();
            lb.removeCallback(sd);
        }
    }

    public void setClusterConfig(ClusterConfig cfg) {
        this.cfg = cfg;
    }

    /**
     * You can assume it returns a non null structure if it is not
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
            lb = new RoundRobin(this, stubs);
        }
        lbList.put(lb);
        return lb;
    }

    public StubLB getRandom() {
        StubLB lb;
        // Synchronize to avoid Exceptions for concurrent modifications
        synchronized (this) {
            lb = new Random(this, stubs);
        }
        lbList.put(lb);
        return lb;
    }

    public StubData getLocal() throws NoLocalStubException {
        synchronized (this) {
            Iterator it = stubs.iterator();
            while (it.hasNext()) {
                StubData sd = (StubData)it.next();
                System.out.println(sd.getStubOrException());
            }
        }
        throw new NoLocalStubException();
    }

    public boolean isStubDebug() {
        return stubDebug;
    }

    public void debug(String mesg) {
        System.out.println("ClusterStub: " + mesg);
    }

    public String toContentsString() {
        synchronized (this) {
            Iterator it = stubs.iterator();
            if (!it.hasNext()) {
                return "[]";
            }
            String str = "[ " + it.next().toString();
            while (it.hasNext()) {
                str = str + ", " + it.next().toString();
            }
            str = str + " ]";
            return str;
        }
    }

    public String toString() {
        return this.getClass().getName() + toContentsString();
    }
}
