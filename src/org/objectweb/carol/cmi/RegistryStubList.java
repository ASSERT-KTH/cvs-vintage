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
 * $Id: RegistryStubList.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A container of regular stubs to CMI registries.
 *
 * @author Simon Nieuviarts
 */
public class RegistryStubList {

    /**
     * Debug flag
     */
    private boolean stubDebug;

    /**
     * Stubs list
     * Accesses to stubs have to be done with lock on this (RegistryStubList)
     */
    private HashSet stubs;

    /**
     * Initial set of stubs
     * Accesses to constSetOfStubs have to be done with lock on this (RegistryStubList)
     */
    private Set constSetOfStubs = null;

    /**
     * Dump
     * Accesses to fullDump have to be done with lock on this (RegistryStubList)
     */
    private byte[] fullDump = null;

    /**
     * Constructor
     * @param stubs stubs list
     * @param stubDebug enable debug mode
     */
    private RegistryStubList(HashSet stubs, boolean stubDebug) {
        this.stubDebug = stubDebug;
        this.stubs = stubs;
    }

    /**
     * Write stubs to output
     * @param out output stream
     * @throws IOException if exception occurs
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeObject(getSerialized());
    }

    /**
     * Read stubs from input
     * @param in input stream
     * @return stubs list
     * @throws IOException if I/O error is encountered
     * @throws ClassNotFoundException if the class INF
     */
    public static RegistryStubList read(ObjectInput in) throws IOException, ClassNotFoundException {
        byte[] ser = (byte[]) in.readObject();
        return deserialize(ser);
    }

    /**
     * Get the serialized form of the stubs list
     * @return binary form of the stubs list
     * @throws IOException if an I/O error occurs
     */
    private synchronized byte[] getSerialized() throws IOException {
        byte[] dump = fullDump;
        if (dump != null) {
            return dump;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CmiOutputStream cos = new CmiOutputStream(bos);
        Iterator it;
        int l = stubs.size();
        cos.writeInt(l);
        it = stubs.iterator();
        for (int i = 0; i < l; i++) {
            StubData sd = (StubData) it.next();
            cos.writeInt(sd.getFactor());
            cos.writeObject(sd.getStub());
        }
        cos.writeBoolean(stubDebug);
        cos.flush();
        dump = bos.toByteArray();
        fullDump = dump;
        return dump;
    }

    /**
     * Deserializes a byte array to a stubs list
     * @param serialized byte array of the stubs
     * @return RegistryStubList
     * @throws IOException if I/O error occurs
     * @throws ClassNotFoundException if the targeted class is not found
     */
    private static RegistryStubList deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        CmiInputStream in = new CmiInputStream(bis);
        try {
            int l = in.readInt();
            if (l > 0) {
                HashSet stubs = new HashSet();
                for (int i = 0; i < l; i++) {
                    int f = in.readInt();
                    RegistryImpl ri = (RegistryImpl) in.readObject();
                    StubData sd = new StubData(null, ri, f);
                    stubs.add(sd);
                }
                return new RegistryStubList(stubs, in.readBoolean());
            } else { // l <= 0
                throw new NoServerException();
            }
        } catch (ClassCastException ce) {
            throw new IOException("invalid serialized stub : " + ce);
        }
    }

    /**
     * Remove a stub
     * This function fails if and only if the stub to remove is the last one.
     * XXX No consistent with the way it works in ServerStubList.
     * @param sd stub
     * @return true if the stub has been removed, false otherwise
     */
    public synchronized boolean removeStub(StubData sd) {
        if (stubs.size() <= 1) {
            return false;
        }
        fullDump = null;
        stubs.remove(sd);
        constSetOfStubs = null;
        return true;
    }

    /**
     * Get a reference to the Cluster registry
     * @param hp host and port of the naming context
     * @return registry reference
     * @throws RemoteException if an exception is encountered
     */
    public static RegistryInternal getClusterStub(NamingContextHostPort[] hp) throws RemoteException {
        int n = hp.length;
        RegistryInternal r = getRegistry(hp[0].host, hp[0].port);
        if (n == 1) {
            return r;
        }
        HashSet stubs = new HashSet();
        stubs.add(new StubData(r, ServerConfig.DEFAULT_LOAD_FACTOR));
        for (int i = 1; i < n; i++) {
            stubs.add(new StubData(getRegistry(hp[i].host, hp[i].port), ServerConfig.DEFAULT_LOAD_FACTOR));
        }
        RegistryStubList sl = new RegistryStubList(stubs, ServerConfig.isStubDebug());
        return (RegistryInternal) new RegistryImplCluster(sl);
    }

    /**
     * Get a reference to the client registry
     * @param host host
     * @param port port number
     * @return registry reference
     * @throws RemoteException if an exception is encountered
     */
    private static RegistryInternal getRegistry(String host, int port) throws RemoteException {
        RegistryInternal r = (RegistryInternal) LowerOrb.getRegistryStub("org.objectweb.carol.cmi.RegistryImpl", host,
                port);
        return r;
    }

    /**
     * Get the set of stubs
     * @return set of stubs
     */
    public synchronized Set getSetOfStubs() {
        if (constSetOfStubs != null) {
            return constSetOfStubs;
        }
        return constSetOfStubs = Collections.unmodifiableSet(stubs);
    }

    /**
     * Test is the debug mode is enabled
     * @return true is enable, false otherwise
     */
    public boolean isStubDebug() {
        return stubDebug;
    }

    /**
     * Print a bug message on the console
     * @param mesg message to print
     */
    public static void debug(String mesg) {
        System.out.println("ClusterStub: " + mesg);
    }

    /**
     * Pretty print for the stubs list
     * @return String stubs list
     */
    public synchronized String toContentsString() {
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

    /**
     * Get the String form of this
     */
    public String toString() {
        return this.getClass().getName() + toContentsString();
    }
}