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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Cluster standard stub for ClusterRegistryImpl
 */
public class ClusterRegistryImpl_Cluster
    extends ClusterStub
    implements ClusterRegistry {
    // constructors
    public ClusterRegistryImpl_Cluster(Remote stub) throws RemoteException {
        super(stub);
    }

    public java.lang.String[] list() throws java.rmi.RemoteException {
        StubListRandomChooser chooser = getRandomChooser();
        do {
            ClusterRegistry stub = (ClusterRegistry) chooser.next();
            try {
                java.lang.String[] result = stub.list();
                return result;
            } catch (java.rmi.ConnectException e) {
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Connection to registry refused, retry");
            } catch (java.rmi.ConnectIOException e) {
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Connection to registry refused, retry");
            }
        } while (true);
    }

    /* Used only to test if a registry is started. Could also be used to test
     * which registries are started.
     */
    public void test() throws java.rmi.RemoteException {
        StubListRandomChooser chooser = getRandomChooser();
        do {
            ClusterRegistry stub = (ClusterRegistry) chooser.next();
            try {
                stub.test();
            } catch (java.rmi.ConnectException e) {
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Connection to registry refused, retry");
            } catch (java.rmi.ConnectIOException e) {
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Connection to registry refused, retry");
            }
        } while (true);
    }

    public java.rmi.Remote lookup(java.lang.String name)
        throws java.rmi.NotBoundException, java.rmi.RemoteException {
        StubListRandomChooser chooser = getRandomChooser();
        do {
            ClusterRegistry stub = (ClusterRegistry) chooser.next();
            try {
                java.rmi.Remote result = stub.lookup(name);
                return result;
            } catch (java.rmi.ConnectException e) {
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Connection to registry refused, retry");
            } catch (java.rmi.ConnectIOException e) {
                if (Trace.CSTUB)
                    Trace.out("CSTUB: Connection to registry refused, retry");
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
