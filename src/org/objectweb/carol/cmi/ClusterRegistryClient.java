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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Client code for a cluster registry
 */
public class ClusterRegistryClient implements ClusterRegistry {
    private ClusterRegistryInternal cr;

    // constructor
    public ClusterRegistryClient(ClusterRegistryInternal cr)
        throws RemoteException {
        this.cr = cr;
    }

    public String[] list() throws RemoteException {
        return cr.list();
    }

    public void test() throws RemoteException {
        cr.test();
    }

    public Remote lookup(String name)
        throws NotBoundException, RemoteException {
        Object obj = cr.lookup(name);
        if (obj instanceof Remote) {
            return (Remote)obj;
        }
        byte[] buf = (byte[])obj;
        ByteArrayInputStream inStream = new ByteArrayInputStream(buf);
        int type = inStream.read(); 
        try {
            CmiInputStream in = new CmiInputStream(inStream);
            if (type == ClusterRegistryInternal.CLUSTERED) {
                return ClusterStubData.read(in, null).getClusterStub();
            } else {
                return (Remote) in.readObject();
            }
        } catch (IOException e) {
            throw new RemoteException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new RemoteException(e.toString());
        }
    }

    private byte[] serialize(Remote obj) throws RemoteException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CmiOutputStream out;
        try {
            out = new CmiOutputStream(outStream);
            out.writeObject(obj);
            out.flush();
            return outStream.toByteArray();
        } catch (IOException e1) {
            throw new RemoteException(e1.toString());
        }
    }

    public void bind(String name, Remote obj)
        throws AlreadyBoundException, RemoteException {
            ClusterConfig cc = null;
            try {
                cc = ClusterObject.getClusterConfig(obj);
                if (TraceCarol.isDebugCmiRegistry())
                    TraceCarol.debugCmiRegistry("Global bind of " + name);
            } catch (Exception e) {
                if (TraceCarol.isDebugCmiRegistry())
                    TraceCarol.debugCmiRegistry("Local bind of " + name);
            }
            if (cc == null) {
                cr.bindSingle(name, obj);
            } else {
                if (!cc.isGlobalAtBind()) {
                    throw new RemoteException("not implemented");
                }
                obj = LowerOrb.toStub(obj);
                byte[] ser = serialize(obj);
                cr.bindCluster(name, ser);
            }
    }

    public void rebind(String name, Remote obj) throws RemoteException {
        ClusterConfig cc = null;
        try {
            cc = ClusterObject.getClusterConfig(obj);
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Global bind of " + name);
        } catch (Exception e) {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Local bind of " + name);
        }
        if (cc == null) {
            cr.rebindSingle(name, obj);
        } else {
            if (!cc.isGlobalAtBind()) {
                throw new RemoteException("not implemented");
            }
            obj = LowerOrb.toStub(obj);
            byte[] ser = serialize(obj);
            cr.rebindCluster(name, ser);
        }
    }

    public void unbind(String name) throws NotBoundException, RemoteException {
        cr.unbind(name);
    }
}
