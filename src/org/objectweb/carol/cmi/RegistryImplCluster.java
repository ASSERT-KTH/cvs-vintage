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
 * $Id: RegistryImplCluster.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

// import java.io.IOException;
// import java.io.ObjectInput;
// import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Cluster stub for RegistryImpl
 * @author Simon Nieuvarts
 */
class RegistryImplCluster implements RegistryInternal {

    /**
     * Debug mode
     */
    private boolean stubDebug;

    /**
     * Stub list
     */
    private RegistryStubList sl;

    /**
     * Random
     */
    private Random rnd;

    /**
     * Constructor with stub list
     * @param sl initial stub list
     */
    public RegistryImplCluster(RegistryStubList sl) {
        stubDebug = sl.isStubDebug();
        this.sl = sl;
        this.rnd = new Random();
    }

    /**
     * Constructor used only by Externalizable
     */
    public RegistryImplCluster() {
    }

    /**
     * Write a stub list
     * @param out ouput
     * @throws IOException if an I/O error occurs
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        RegistryStubList sl;
        synchronized (this) {
            sl = this.sl;
        }
        sl.write(out);
    }

    /**
     * Read a stub list
     * @param in input
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the stub list doesn't match with the
     *         expected class
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // It is like a part of a constructor : no need to synchronize
        if (sl != null) {
            throw new IOException("ClusterStub.readExternal() called at the wrong place.");
        }
        sl = RegistryStubList.read(in);
    }

    /**
     * Choose randomly a stub XXX Here we could let the user specify a custom
     * algorithm (by configuration).
     * @return stub
     * @throws NoServerException if an exception occurs
     */
    private StubData choose() throws NoServerException {
        rnd.update(sl.getSetOfStubs());
        return rnd.get();
    }

    /**
     * Remove a stub
     * @param sd stub
     */
    private void remove(StubData sd) {
    }

    /**
     * Get the entries list
     * @return entries
     * @throws RemoteException if Exception is encountered
     */
    public String[] list() throws RemoteException {
        do {
            StubData sd = choose();
            RegistryInternal stub = (RegistryInternal) sd.getStub();
            String exMesg;
            try {
                java.lang.String[] result = stub.list();
                return result;
            } catch (ConnectException e) {
                exMesg = e.getMessage();
            } catch (ConnectIOException e) {
                exMesg = e.getMessage();
            } catch (NoSuchObjectException e) {
                exMesg = e.getMessage();
            }
            if (stubDebug) {
                RegistryStubList.debug("Connection to registry failed, retry : " + exMesg);
            }
            remove(sd);
        } while (true);
    }

    /**
     * Test method
     * @throws RemoteException if exception is encountered
     */
    public void test() throws RemoteException {
        do {
            StubData sd = choose();
            RegistryInternal stub = (RegistryInternal) sd.getStub();
            String exMesg;
            try {
                stub.test();
                return;
            } catch (ConnectException e) {
                exMesg = e.getMessage();
            } catch (ConnectIOException e) {
                exMesg = e.getMessage();
            } catch (NoSuchObjectException e) {
                exMesg = e.getMessage();
            }
            if (stubDebug) {
                RegistryStubList.debug("Connection to registry failed, retry : " + exMesg);
            }
            remove(sd);
        } while (true);
    }

    /**
     * Retrieve an object by a name
     * @param name name to search
     * @return object associated
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
    public Object lookup(String name) throws NotBoundException, RemoteException {
        do {
            StubData sd = choose();
            RegistryInternal stub = (RegistryInternal) sd.getStub();
            String exMesg;
            try {
                return stub.lookup(name);
            } catch (ConnectException e) {
                exMesg = e.getMessage();
            } catch (ConnectIOException e) {
                exMesg = e.getMessage();
            } catch (NoSuchObjectException e) {
                exMesg = e.getMessage();
            }
            if (stubDebug) {
                RegistryStubList.debug("Connection to registry failed, retry : " + exMesg);
            }
            remove(sd);
        } while (true);
    }

    /**
     * Bind a single entry
     * @param name name
     * @param obj object
     * @throws AlreadyBoundException if the entry is already bound
     * @throws RemoteException if an exception is encountered
     */
    public void bindSingle(String name, Remote obj) throws AlreadyBoundException, RemoteException {
        throw new RemoteException("Can't bind into multiple servers");
    }

    /**
     * Rebind a single entry
     * @param name name
     * @param obj object
     * @throws RemoteException if an exception is encountered
     */
    public void rebindSingle(String name, Remote obj) throws RemoteException {
        throw new RemoteException("Can't rebind into multiple servers");
    }

    /**
     * Bind a cluster entry
     * @param name cluster name
     * @param obj array of objects
     * @throws AlreadyBoundException if the entry is already bound
     * @throws RemoteException if an exception is encountered
     */
    public void bindCluster(String name, byte[] obj) throws AlreadyBoundException, RemoteException {
        throw new RemoteException("Can't bind into multiple servers");
    }

    /**
     * Rebind a cluster entry
     * @param name cluster name
     * @param obj array of objects
     * @throws RemoteException if an exception is encountered
     */
    public void rebindCluster(String name, byte[] obj) throws RemoteException {
        throw new RemoteException("Can't rebind into multiple servers");
    }

    /**
     * Unbind an entry
     * @param name entry to unregister
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
    public void unbind(java.lang.String name) throws NotBoundException, RemoteException {
        throw new RemoteException("Can't unbind from multiple servers");
    }
}