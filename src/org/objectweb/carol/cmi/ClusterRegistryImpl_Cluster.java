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

import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.RemoteException;

/**
 * Cluster standard stub for ClusterRegistryImpl
 */
public class ClusterRegistryImpl_Cluster
    implements ClusterStub, ClusterRegistry {
    private ClusterStubData csd;
    private StubLB lb;

    // constructor
    public ClusterRegistryImpl_Cluster(ClusterStubData csd)
        throws RemoteException {
        this.csd = csd;
        lb = csd.getRandom();
    }

    //TODO remove
    private void setLB() {
        if (lb == null) {
            lb = csd.getRandom();
        }
    }

    public java.lang.String[] list() throws java.rmi.RemoteException {
        setLB();
        ClusterRegistry stub = (ClusterRegistry) lb.get();
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
                filter.add(stub);
                stub = (ClusterRegistry) lb.get(filter);
            }
        } while (true);
    }

    /* Used only to test if a registry is started.
     */
    public void test() throws java.rmi.RemoteException {
        setLB();
        ClusterRegistry stub = (ClusterRegistry) lb.get();
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
                filter.add(stub);
                stub = (ClusterRegistry) lb.get(filter);
            }
        } while (true);
    }

    public java.rmi.Remote lookup(java.lang.String name)
        throws java.rmi.NotBoundException, java.rmi.RemoteException {
        setLB();
        ClusterRegistry stub = (ClusterRegistry) lb.get();
        StubLBFilter filter = null;
        do {
            try {
                java.rmi.Remote result = stub.lookup(name);
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
                filter.add(stub);
                stub = (ClusterRegistry) lb.get(filter);
            }
        } while (true);
    }

    public void bind(java.lang.String name, java.rmi.Remote obj)
        throws java.rmi.AlreadyBoundException, java.rmi.RemoteException {
        throw new RemoteException("Can't bind into multiple servers");
        //        ClusterRegistry stub = (ClusterRegistry) getFirst();
        //        stub.bind(name, obj);
    }

    public void rebind(java.lang.String name, java.rmi.Remote obj)
        throws java.rmi.RemoteException {
        throw new RemoteException("Can't rebind into multiple servers");
        //        ClusterRegistry stub = (ClusterRegistry) getFirst();
        //        stub.rebind(name, obj);
    }

    public void unbind(java.lang.String name)
        throws java.rmi.NotBoundException, java.rmi.RemoteException {
        throw new RemoteException("Can't unbind from multiple servers");
        //        ClusterRegistry stub = (ClusterRegistry) getFirst();
        //        stub.unbind(name);
    }
}
