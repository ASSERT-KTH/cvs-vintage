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

import java.lang.reflect.Constructor;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.rmi.multi.JrmpPRODelegate;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.Endpoint;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

public class LowerOrb {
    private static String prefix = "rmi:";
    public static final int DEFAULT_CREG_PORT = 1099;
    public static final int REG_ID = 0xC2C91901;
    private static ObjID id = new ObjID(REG_ID);
    private static PortableRemoteObjectDelegate rmi = new JrmpPRODelegate();

    public static void exportObject(Remote obj) throws RemoteException {
        rmi.exportObject(obj);
    }

    public static void unexportObject(Remote obj) throws NoSuchObjectException {
        rmi.unexportObject(obj);
    }

    public static PortableRemoteObjectDelegate getPRODelegate() {
        return rmi;
    }

    public static Remote exportRegistry(Remote obj, int port)
        throws RemoteException {
        LiveRef lref = new LiveRef(id, port);
        UnicastServerRef uref = new UnicastServerRef(lref);
        return uref.exportObject(obj, null, true);
    }

    private static Class[] stubConsParamTypes = { RemoteRef.class };

    public static Remote getRegistryStub(
        String className,
        String host,
        int port)
        throws RemoteException {
        if (port <= 0)
            throw new RemoteException("Invalid port no " + port);
        try {
            Endpoint ep = new TCPEndpoint(host, port);
            LiveRef ref = new LiveRef(id, ep, false);
            Class stubcl = Class.forName(className + "_Stub");
            Constructor cons = stubcl.getConstructor(stubConsParamTypes);
            return (RemoteStub) cons.newInstance(
                new Object[] { new UnicastRef(ref)});
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }
}
