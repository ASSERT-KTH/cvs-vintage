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
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.carol.util.configuration.TraceCarol;

public final class ClusterRegistryImpl implements ClusterRegistry {
    /**
     * The non-clustered objects registry. Maps names to regular stubs for non
     * clustered objects.
     */
    private HashMap lreg = new HashMap();
    public static final String REG_PREFIX = "REG_";

    private ClusterRegistryImpl() {
    }

    public static ClusterRegistryKiller start(int port)
        throws RemoteException {
        if (TraceCarol.isDebugCmiRegistry())
            TraceCarol.debugCmiRegistry("registry starting on port " + port);
        ClusterRegistryImpl creg = new ClusterRegistryImpl();
        ClusterRegistry stub =
            (ClusterRegistry) LowerOrb.exportRegistry(creg, port);
        ClusterRegistryKiller k = new ClusterRegistryKiller(creg, port);
        return k;
    }

    public Remote lookup(String n) throws NotBoundException, RemoteException {
        Object obj;
        synchronized (lreg) {
            obj = lreg.get(n);
        }
        if (obj != null) {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("local lookup of " + n);
            return (Remote) obj;
        }
        try {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("global lookup of " + n);
            Remote cs = (Remote) DistributedEquiv.getGlobal(REG_PREFIX + n);
            if (cs != null) {
                if (TraceCarol.isDebugCmiRegistry())
                    TraceCarol.debugCmiRegistry("returned a cluster stub");
                return cs;
            }
        } catch (ConfigException e) {
            throw new RemoteException(e.toString());
        }
        throw new NotBoundException(n);
    }

    public void bind(String n, Remote obj)
        throws AlreadyBoundException, RemoteException {
        Object cur;
        ClusterConfig cc = null;
        try {
            cc = ClusterObject.getClusterConfig(obj);
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Global bind of " + n);
        } catch (Exception e) {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Local bind of " + n);
        }

        synchronized (lreg) {
            if (cc == null) {
                cur = lreg.get(n);
                if (cur != null)
                    throw new AlreadyBoundException(n);
                lreg.put(n, obj);
            } else {
                if (!cc.isGlobalAtBind()) {
                    throw new RemoteException("not implemented");
                }
                try {
                    if (!DistributedEquiv.exportObject(REG_PREFIX + n, obj)) {
                        throw new AlreadyBoundException(n);
                    }
                } catch (ConfigException e) {
                    throw new RemoteException(e.toString());
                }
            }
        }
    }

    public void unbind(String n) throws NotBoundException, RemoteException {
        Object obj;
        synchronized (lreg) {
            obj = lreg.remove(n);
            if (obj != null) {
                if (TraceCarol.isDebugCmiRegistry()) {
                    TraceCarol.debugCmiRegistry("Local unbind of " + n);
                }
                return;
            }
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Global unbind of " + n);
            try {
                if (!DistributedEquiv.unexportObject(REG_PREFIX + n)) {
                    throw new NotBoundException(n);
                }
            } catch (ConfigException e) {
                throw new RemoteException(e.toString());
            }
        }
    }

    public void rebind(String n, Remote obj) throws RemoteException {
        Object cur;
        ClusterConfig cc = null;
        try {
            cc = ClusterObject.getClusterConfig(obj);
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Global rebind of " + n);
        } catch (Exception e) {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("Local rebind of " + n);
        }

        synchronized (lreg) {
            if (cc == null) {
                lreg.put(n, obj);
            } else {
                if (!cc.isGlobalAtBind()) {
                    throw new RemoteException("not implemented");
                }
                try {
                    String name = REG_PREFIX + n;
                    DistributedEquiv.unexportObject(name);
                    DistributedEquiv.exportObject(name, obj);
                } catch (ConfigException e) {
                    throw new RemoteException(e.toString());
                }
            }
        }
    }

    public String[] list() throws RemoteException {
        Set s = new TreeSet();
        try {
            Set global = DistributedEquiv.keySet();
            Iterator it = global.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof String) {
                    String str = (String)o;
                    if (str.startsWith(REG_PREFIX)) {
                        s.add(str.substring(4));
                    }
                }
            }
        } catch (ConfigException e) {
            throw new RemoteException(e.toString());
        }
        synchronized (lreg) {
            Set local = lreg.keySet();
            Iterator it = local.iterator();
            while (it.hasNext()) {
                s.add(it.next());
            }
        }
        int n = s.size();
        Iterator it = s.iterator();
        String[] tab = new String[n];
        for (int i=0; i<n; i++) {
            tab[i] = (String)it.next();
        }
        return tab;
    }

    public void test() throws RemoteException {
    }

    public static void main(String args[]) throws Exception {
        DistributedEquiv.start();
        start(Integer.parseInt(args[0]));
        System.out.println("Cluster service started");
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
            }
        }
    }
}
