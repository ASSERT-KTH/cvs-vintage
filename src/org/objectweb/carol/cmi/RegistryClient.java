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
 * $Id: RegistryClient.java,v 1.2 2005/10/21 14:33:27 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Client code for a cluster registry
 *
 * @author Simon Nieuvarts
 */
public class RegistryClient implements Registry {

    /**
     * Registry
     */
    private RegistryInternal cr;

    /**
     * Constructor
     * @param cr client registry
     * @throws RemoteException if exception
     */
    public RegistryClient(RegistryInternal cr)
        throws RemoteException {
        this.cr = cr;
    }

    /**
     * Get the entries list
     * @return entries
     * @throws RemoteException if Exception is encountered
     */
    public String[] list() throws RemoteException {
        return cr.list();
    }

    /**
     * Test
     * @throws RemoteException if Exception is encountered
     */
    public void test() throws RemoteException {
        cr.test();
    }

    /**
     * Retrieve an object by a name
     * @param name name to search
     * @return object associated
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
     public Remote lookup(String name)throws NotBoundException, RemoteException {

		URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		URL[] urls = cl.getURLs();

        Object obj = cr.lookup(name,urls);

        if (obj instanceof Remote) {
            return (Remote) obj;
        }
        // If not Remote, then it is a cluster stub as a serialized stub list.
        try {
            return ServerStubList.deserialize((byte[]) obj);
        } catch (Exception e) {
            throw new RemoteException("Can't deserialize stub", e);
        }
    }

     /**
      * Bind an entry
      * @param name name
      * @param obj object
      * @throws AlreadyBoundException if the entry is already bound
      * @throws RemoteException if an exception is encountered
      */
    public void bind(String name, Remote obj) throws AlreadyBoundException, RemoteException {

        Boolean clusterEquivAtBind = StubConfig.clusterEquivAtBind(obj);
        if (clusterEquivAtBind == null) {
            if (TraceCarol.isDebugCmiRegistry()) {
                TraceCarol.debugCmiRegistry("Local bind of " + name);
            }
            cr.bindSingle(name, obj);
            return;
        } else if (!clusterEquivAtBind.booleanValue()) {
            throw new RemoteException("not supported");
        }
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("Global bind of " + name);
        }
        obj = LowerOrb.toStub(obj);
        byte[] ser = CmiOutputStream.serialize(obj);
        cr.bindCluster(name, ser);
    }

    /**
     * Rebind an entry
     * @param name name
     * @param obj object
     * @throws RemoteException if an exception is encountered
     */
    public void rebind(String name, Remote obj) throws RemoteException {
        Boolean clusterEquivAtBind = StubConfig.clusterEquivAtBind(obj);
        if (clusterEquivAtBind == null) {
            if (TraceCarol.isDebugCmiRegistry()) {
                TraceCarol.debugCmiRegistry("Local bind of " + name);
            }
            cr.rebindSingle(name, obj);
            return;
        } else if (!clusterEquivAtBind.booleanValue()) {
            throw new RemoteException("not supported");
        }
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("Global bind of " + name);
        }
        obj = LowerOrb.toStub(obj);
        byte[] ser = CmiOutputStream.serialize(obj);
        cr.rebindCluster(name, ser);
    }

    /**
     * Unbind an entry
     * @param name entry to unregister
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
     public void unbind(String name) throws NotBoundException, RemoteException {
        cr.unbind(name);
    }
}
