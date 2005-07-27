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
 * $Id: ServerStubList.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A container of regular stubs to RMI-exported clustered objects. It is used by
 * cluster stubs to choose to which clustered object redirect a request. Regular
 * stubs have to be the same class.
 *
 * @author Simon Nieuviarts
 */
public class ServerStubList {

    /**
     * Debug mode ?
     */
    private boolean stubDebug;

    /**
     * Stubs list
     *
     * Accesses to the fields of the stub lists have to be done with a lock
     * on this object as soon as their are recorded in this list. This is to
     * ensure a correct sharing of stub lists between clients in the same JVM.
     */
    private static WeakValueHashtable slCache = new WeakValueHashtable();

    /**
     * Is in cache
     * Never goes from false to true. A ServerStubList out of the cache can
     * no more go back into it.
     */
    private boolean inCache;

    /**
     * Stubs have to be of this same class.
     */
    private final Class stubClass;

    /**
     * Maps server ids (ClusterId) to ObjectId.
     */
    private Map srvMap = new TreeMap();

    /**
     * Maps ObjectId to StubData.
     */
    private Map objMap = new TreeMap();

    /**
     * Unmodifiable set of stubs
     */
    private Set constSetOfStubs = null;

    /**
     * Signature
     *
     * Option : compress it ? MD5 ?
     */
    private byte[] signature;

    /**
     * Full dump
     */
    private byte[] fullDump;

    /**
     * This is the stub to return to clients using this stub list
     * Set to null when no more in the slCache. The object is not allowed
     * to go back in the cache or it may break some functions
     */
    private ClusterStub clusterStub = null;

    /**
     * There may be other cluster stubs which share the same list in case
     * of group merging in the cluster. Typically happens at cluster startup
     * when several servers start at the same time
     */
    private WeakList clusterStubs = null;

    /**
     * Creates a new server stub list
     * @param first initial stub data
     * @throws RemoteException if an exception occurs
     */
    ServerStubList(StubData first) throws RemoteException {
        stubDebug = ServerConfig.isStubDebug();
        inCache = true;
        stubClass = first.getStub().getClass();
        ObjectId objId = first.getObjectId();
        srvMap.put(objId.getServerId(), objId);
        objMap.put(objId, first);
    }

    /**
     * Instanciate a ServerStubList that is not in the cache and can not be
     * modified. It is used in the registry
     * @param first initial stub data
     * @throws RemoteException if an exception occurs
     */
    ServerStubList(Collection sdList) throws RemoteException {
        stubDebug = ServerConfig.isStubDebug();
        inCache = false;
        Iterator it = sdList.iterator();
        StubData first = (StubData) it.next();
        stubClass = first.getStub().getClass();
        ObjectId objId = first.getObjectId();
        srvMap.put(objId.getServerId(), objId);
        objMap.put(objId, first);
        while (it.hasNext()) {
            StubData sd = (StubData) it.next();
            ObjectId objectId = sd.getObjectId();
            checkCompatibleStub(sd.getStub());
            srvMap.put(objectId.getServerId(), objectId);
            objMap.put(objectId, sd);
        }
        srvMap = Collections.unmodifiableMap(srvMap);
        objMap = Collections.unmodifiableMap(objMap);
        clusterStub = StubConfig.instanciateClusterStub(stubClass, this);
    }

    /**
     * @return a dump of the object Ids of the stubs in this list. Two lists
     *         that contain the same stubs return the same signature.
     * @throws IOException if an exception occurs
     */
    private byte[] getSignature() throws IOException {
        synchronized (slCache) {
            byte[] sign = signature;
            if (sign != null) {
                return sign;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream oos = new DataOutputStream(bos);
            int l = objMap.size();
            oos.writeInt(l);
            Iterator it = objMap.keySet().iterator();
            for (int i = 0; i < l; i++) {
                ObjectId oid = (ObjectId) it.next();
                oid.write(oos);
            }
            if (it.hasNext()) {
                throw new NullPointerException();
            }
            oos.flush();
            return signature = bos.toByteArray();
        }
    }

    /**
     * @return the serialized  of the server stubs list
     * @throws IOException if an exception occurs
     */
    public byte[] getSerialized() throws IOException {
        synchronized (slCache) {
            byte[] dump = fullDump;
            if (dump != null) {
                return dump;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            Iterator it;
            int l = objMap.size();
            dos.writeInt(l);
            it = objMap.keySet().iterator();
            for (int i = 0; i < l; i++) {
                ObjectId oid = (ObjectId) it.next();
                oid.write(dos);
            }
            if (it.hasNext()) {
                throw new NullPointerException();
            }
            it = objMap.values().iterator();
            dos.flush();
            CmiOutputStream cos = new CmiOutputStream(bos);
            for (int i = 0; i < l; i++) {
                StubData sd = (StubData) it.next();
                cos.writeInt(sd.getFactor());
                Object o = sd.getStubOrException();
                if (o instanceof Remote) {
                    cos.writeObject(o);
                } else {
                    cos.writeObject(sd.getSerializedStub());
                }
            }
            if (it.hasNext()) {
                throw new NullPointerException();
            }
            cos.writeBoolean(stubDebug);
            cos.flush();
            return fullDump = bos.toByteArray();
        }
    }

    /**
     * Write the server stubs list
     * @param out output
     * @throws IOException if an I/O error occurs
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeObject(getSerialized());
    }

    /**
     * Read the input and builds a server stubs list
     * @param in input
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the expected class can not be created
     */
    public static ClusterStub read(ObjectInput in) throws IOException,
            ClassNotFoundException {
        byte[] ser = (byte[]) in.readObject();
        return deserialize(ser);
    }

    /**
     * Check the compatibility of stubs
     * @param stub stub to check
     * @throws RemoteException if the stubs aren't compliant
     */
    private void checkCompatibleStub(Remote stub) throws RemoteException {
        if (stub.getClass().getClassLoader().equals(stubClass.getClassLoader())) {
            if (!stub.getClass().equals(stubClass)) {
                throw new RemoteException(
                "Incompatible stubs in cluster stub : two different versions of the application are deployed in the cluster.");
            }
        }
        else {
            // When the stubs aren't loaded from the same classloader as it's the case in JOnAS,
            // we are just able to check the name of the class
            if (!stub.getClass().getName().equals(stubClass.getName())) {
                throw new RemoteException(
                "Incompatible stubs in cluster stub : two different versions of the application are deployed in the cluster.");
            }
        }
    }

    /**
     * Get the cluster stub
     * @return cluster stub
     */
    ClusterStub getClusterStub() {
        return clusterStub;
    }

    /**
     * Add a cluster stub to the list
     * Must be called only with the lock on slCache
     * @param cs stub to add
     */
    private void addClusterStub(ClusterStub cs) {
        if (cs == null) {
            throw new NullPointerException(); // for debug only
        }
        if (clusterStub == null) {
            clusterStub = cs;
        } else {
            if (clusterStubs == null) {
                clusterStubs = new WeakList();
            }
            clusterStubs.add(cs);
        }
        cs._distrib.setStubList(this);
    }

    /**
     * Read a stub from the CmiInputStream
     * @param in input stream
     * @param objectId define the class to use for create the instance
     * @return Stub to a single instance
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the objectid class can not be instanciated
     */
    private static StubData readStub(CmiInputStream in, ObjectId objectId)
            throws IOException, ClassNotFoundException {
        int f = in.readInt();
        Object obj = in.readObject();
        StubData sd;
        if (obj instanceof Remote) {
            return new StubData(objectId, (Remote) obj, f);
        } else {
            return new StubData(objectId, (byte[]) obj, f);
        }
    }

    /**
     * Deserialize a byte array to create a cluster stub
     * @param serialized but to deserialize
     * @return Stub to clustered object
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the expected class can not be instanciated
     */
    public static ClusterStub deserialize(byte[] serialized)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        DataInputStream dis = new DataInputStream(bis);
        ServerStubList stubList;
        ArrayList oidl = new ArrayList();
        try {
            int l = dis.readInt();
            if (l <= 0) {
                throw new NoServerException();
            }
            ObjectId objectId = ObjectId.read(dis);
            ServerStubList sl;
            synchronized (slCache) {
                sl = (ServerStubList) slCache.get(objectId);
                // If another stub list with the same objects exists in the JVM,
                // use it
                if (sl != null) {
                    byte[] sign = sl.getSignature();
                    int l2 = sign.length;
                    if (l2 <= serialized.length) {
                        int j = 0;
                        while ((j < l2) && (sign[j] == serialized[j])) {
                            j++;
                        }
                        if (j == l2) {
                            return sl.clusterStub;
                        }
                    }
                }
            }
            // Here, we know that we have a new stub list in the JVM
            oidl.add(objectId);
            for (int i = 1; i < l; i++) {
                objectId = ObjectId.read(dis);
                oidl.add(objectId);
            }

            CmiInputStream in = new CmiInputStream(bis);
            objectId = (ObjectId) oidl.get(0);
            StubData sd = readStub(in, objectId);
            stubList = new ServerStubList(sd);
            for (int i = 1; i < l; i++) {
                objectId = (ObjectId) oidl.get(i);
                sd = readStub(in, objectId);
                Remote stub = sd.getStub();
                stubList.checkCompatibleStub(stub);
                stubList.srvMap.put(objectId.getServerId(), objectId);
                stubList.objMap.put(objectId, sd);
            }
            stubList.stubDebug = in.readBoolean();
        } catch (ClassCastException ce) {
            throw new IOException("invalid serialized stub : " + ce);
        }
        // stubList is the full new list we have read

        synchronized (slCache) {
            // if at least one non-replaceable stub list shares an object id
            // with our new stub list, discard the new one and return the
            // non-replaceable one
            // else
            // memorize the association of the new stub list to its object
            // ids and remove each stub list that share at least an object id
            // with it
            int l = oidl.size();
            HashSet stubLists = new HashSet();
            int newObjIDs = 0;
            for (int i = 0; i < l; i++) {
                ServerStubList sl = (ServerStubList) slCache.put(oidl.get(i),
                        stubList);
                if (sl != null) {
                    stubLists.add(sl);
                } else {
                    newObjIDs++;
                }
            }
            Iterator it0 = stubLists.iterator();
            HashSet ids = new HashSet();
            while (it0.hasNext()) {
                ServerStubList sl = (ServerStubList) it0.next();
                ids.addAll(sl.objMap.keySet());
                sl.inCache = false;
                stubList.addClusterStub(sl.clusterStub);
                sl.clusterStub = null;
                WeakList wl = sl.clusterStubs;
                if (wl != null) {
                    sl.clusterStubs = null;
                    Iterator it = wl.iterator();
                    while (it.hasNext()) {
                        stubList.addClusterStub((ClusterStub) it.next());
                    }
                }
            }
            ids.removeAll(oidl);
            Iterator it = ids.iterator();
            while (it.hasNext()) {
                slCache.remove(it.next());
            }
            if (stubList.clusterStub == null) {
                stubList.clusterStub = StubConfig.instanciateClusterStub(
                        stubList.stubClass, stubList);
            }
            return stubList.clusterStub;
        }
    }

    /**
     * Remove a stub in the clustered stub
     * @param serverId stub identifier to remove
     */
    private void doRemoveStub(ClusterId serverId) {
        ObjectId oid = (ObjectId) srvMap.remove(serverId);
        if (oid == null) {
            return;
        }
        signature = null;
        fullDump = null;
        constSetOfStubs = null;
        objMap.remove(oid);
        if (inCache) {
            ServerStubList sl = (ServerStubList) slCache.remove(oid);
            if (sl != this) {
                throw new NullPointerException(); // debug
            }
            if (objMap.isEmpty()) {
                inCache = false;
            }
        }
    }

    /**
     * Remove a stub in the clustered stub (Synchronized)
     * @param serverId stub identifier to remove
     */
    public void removeStub(ClusterId serverId) {
        synchronized (slCache) {
            if (inCache) {
                doRemoveStub(serverId);
                return;
            }
        }
        synchronized (this) {
            doRemoveStub(serverId);
        }
    }

    /**
     * Remove a stub in the clustered stub (Synchronized)
     * @param sd stub data
     */
    public void removeStub(StubData sd) {
        synchronized (slCache) {
            if (inCache) {
                doRemoveStub(sd.getServerId());
                return;
            }
        }
        synchronized (this) {
            doRemoveStub(sd.getServerId());
        }
    }

    /**
     * Get the set of stubs
     * @return collection of stubs
     */
    private Set doGetSetOfStubs() {
        if (constSetOfStubs != null) {
            return constSetOfStubs;
        }
        HashSet a = new HashSet();
        a.addAll(objMap.values());
        return constSetOfStubs = Collections.unmodifiableSet(a);
    }

    /**
     * Get the set of stubs (synchronized)
     * @return collection of stubs
     */
    public Set getSetOfStubs() {
        synchronized (slCache) {
            if (inCache) {
                return doGetSetOfStubs();
            }
        }
        synchronized (this) {
            return doGetSetOfStubs();
        }
    }

    /**
     * Test is debug mode is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isStubDebug() {
        return stubDebug;
    }

    /**
     * Print a debug message on the console
     * @param mesg message
     */
    public static void debug(String mesg) {
        System.out.println("ClusterStub: " + mesg);
    }

    /**
     * Convert the stubs list to a readable string
     * @return String form of the stubs list
     */
    public String toContentsString() {
        synchronized (slCache) {
            Iterator it = objMap.values().iterator();
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
    /**
     * @return String form of the stubs list
     */
    public String toString() {
        return this.getClass().getName() + toContentsString();
    }
}