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

import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Cluster standard stub for ClusterRegistryImpl
 */
public class ClusterRegistryImpl_Cluster
    implements ClusterStub, ClusterRegistryInternal {
    private ClusterStubData csd;
    private StubLB lb;

    // constructor
    public ClusterRegistryImpl_Cluster(ClusterStubData csd)
        throws RemoteException {
        this.csd = csd;
        lb = csd.getRandom();
    }

    private void setLB() {
        if (lb == null) {
            lb = csd.getRandom();
        }
    }

    public String[] list() throws RemoteException {
        setLB();
        StubData sd = lb.get();
        ClusterRegistryInternal stub = (ClusterRegistryInternal) sd.getStub();
        StubLBFilter filter = null;
        do {
            try {
                java.lang.String[] result = stub.list();
                return result;
            } catch (RemoteException e) {
                if (!(e instanceof ConnectException)
                    && !(e instanceof ConnectIOException)) {
                    throw e;
                }
                if (csd.isStubDebug()) {
                    csd.debug("Connection to registry refused, retry");
                }
                if (filter == null) {
                    filter = new StubLBFilter();
                }
                filter.add(sd);
                sd = lb.get(filter);
                stub = (ClusterRegistryInternal) sd.getStub();
            }
        } while (true);
    }

    /* Used only to test if a registry is started.
     */
    public void test() throws RemoteException {
        setLB();
        StubData sd = lb.get();
        ClusterRegistryInternal stub = (ClusterRegistryInternal) sd.getStub();
        StubLBFilter filter = null;
        do {
            try {
                stub.test();
            } catch (RemoteException e) {
                if (!(e instanceof ConnectException)
                    && !(e instanceof ConnectIOException)) {
                    throw e;
                }
                if (csd.isStubDebug()) {
                    csd.debug("Connection to registry refused, retry");
                }
                if (filter == null) {
                    filter = new StubLBFilter();
                }
                filter.add(sd);
                sd = lb.get(filter);
                stub = (ClusterRegistryInternal) sd.getStub();
            }
        } while (true);
    }

    public Object lookup(String name)
        throws NotBoundException, RemoteException {
        setLB();
        StubData sd = lb.get();
        ClusterRegistryInternal stub = (ClusterRegistryInternal) sd.getStub();
        StubLBFilter filter = null;
        do {
            try {
                return stub.lookup(name);
            } catch (RemoteException e) {
                if (!(e instanceof ConnectException)
                    && !(e instanceof ConnectIOException)) {
                    throw e;
                }
                if (csd.isStubDebug()) {
                    csd.debug("Connection to registry refused, retry");
                }
                if (filter == null) {
                    filter = new StubLBFilter();
                }
                filter.add(sd);
                sd = lb.get(filter);
                stub = (ClusterRegistryInternal) sd.getStub();
            }
        } while (true);
    }

    public void bindSingle(String name, Remote obj)
        throws AlreadyBoundException, RemoteException {
        throw new RemoteException("Can't bind into multiple servers");
    }

    public void rebindSingle(String name, Remote obj)
        throws RemoteException {
        throw new RemoteException("Can't rebind into multiple servers");
    }

    public void bindCluster(String name, byte[] obj)
        throws AlreadyBoundException, RemoteException {
        csd.getLocal();
        throw new RemoteException("Can't bind into multiple servers");
        //        ClusterRegistryInternal stub = (ClusterRegistryInternal) getFirst();
        //        stub.bind(name, obj);
    }

    public void rebindCluster(String name, byte[] obj)
        throws RemoteException {
        csd.getLocal();
        throw new RemoteException("Can't rebind into multiple servers");
        //        ClusterRegistryInternal stub = (ClusterRegistryInternal) getFirst();
        //        stub.rebind(name, obj);
    }

    public void unbind(java.lang.String name)
        throws NotBoundException, RemoteException {
        csd.getLocal();
        throw new RemoteException("Can't unbind from multiple servers");
        //        ClusterRegistryInternal stub = (ClusterRegistryInternal) getFirst();
        //        stub.unbind(name);
    }
}
