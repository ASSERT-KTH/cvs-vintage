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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.carol.util.configuration.TraceCarol;

public final class ClusterRegistryImpl implements ClusterRegistryInternal {
    /**
     * The non-clustered objects registry. Maps names to serialized regular
     * stubs for non clustered objects.
     */
    private HashMap lreg = new HashMap();
    public static final String REG_PREFIX = "REG_";

    // Protect the registry against multiple starts in the same JVM
    private static Object lock = new Object();
    private boolean started = false;

    private ClusterRegistryImpl() throws RemoteException {
        synchronized (lock) {
            //TODO Could be defeated by several class loaders ? Check in JOnAS
            if (started) throw new RemoteException("Can't start multiple cluster registries in the same JVM");
            started = true;
        }
    }

    public static ClusterRegistryKiller start(int port)
        throws RemoteException {
        if (TraceCarol.isDebugCmiRegistry())
            TraceCarol.debugCmiRegistry("registry starting on port " + port);
        ClusterRegistryImpl creg = new ClusterRegistryImpl();
        ClusterRegistryInternal stub =
            (ClusterRegistryInternal) LowerOrb.exportRegistry(creg, port);
        ClusterRegistryKiller k = new ClusterRegistryKiller(creg, port);
        return k;
    }

    public Object lookup(String n) throws NotBoundException, RemoteException {
        Object obj;
        synchronized (lreg) {
            obj = lreg.get(n);
        }
        if (obj != null) {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("local lookup of " + n);
            return obj;
        }
        try {
            if (TraceCarol.isDebugCmiRegistry())
                TraceCarol.debugCmiRegistry("global lookup of " + n);
            ClusterStubData csd = DistributedEquiv.getGlobal(REG_PREFIX + n);
            if (csd != null) {
                if (TraceCarol.isDebugCmiRegistry())
                    TraceCarol.debugCmiRegistry("returned a cluster stub");
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                b.write(CLUSTERED);
                CmiOutputStream os = new CmiOutputStream(b);
                csd.write(os);
                os.flush();
                return b.toByteArray();
            }
        } catch (ConfigException e) {
            throw new RemoteException(e.toString());
        } catch (IOException e) {
            throw new RemoteException(e.toString());
        }
        throw new NotBoundException(n);
    }

    public void bindSingle(String n, Remote obj)
        throws AlreadyBoundException, RemoteException {
/*
            byte[] prefix = new byte[obj.length + 1];
            prefix[0] = NOT_CLUSTERED;
            System.arraycopy(obj, 0, prefix, 1, obj.length);
*/
        Object cur;
        if (TraceCarol.isDebugCmiRegistry())
            TraceCarol.debugCmiRegistry("Local bind of " + n);
        synchronized (lreg) {
            cur = lreg.get(n);
            if (cur != null)
                throw new AlreadyBoundException(n);
            lreg.put(n, obj);
        }
    }

    public void rebindSingle(String n, Remote obj) throws RemoteException {
/*
        byte[] prefix = new byte[obj.length + 1];
        prefix[0] = NOT_CLUSTERED;
        System.arraycopy(obj, 0, prefix, 1, obj.length);
*/
        if (TraceCarol.isDebugCmiRegistry())
            TraceCarol.debugCmiRegistry("Local rebind of " + n);
        synchronized (lreg) {
            lreg.put(n, obj);
        }
    }

    public void bindCluster(String n, byte[] obj)
        throws AlreadyBoundException, RemoteException {
        Object cur;
        if (TraceCarol.isDebugCmiRegistry())
            TraceCarol.debugCmiRegistry("Global bind of " + n);
        synchronized (lreg) {
            try {
                if (!DistributedEquiv.exportObject(REG_PREFIX + n, obj)) {
                    throw new AlreadyBoundException(n);
                }
            } catch (ConfigException e) {
                throw new RemoteException(e.toString());
            }
        }
    }

    public void unbind(String n)
        throws NotBoundException, RemoteException {
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

    public void rebindCluster(String n, byte[] obj) throws RemoteException {
        Object cur;
        ClusterConfig cc = null;
        if (TraceCarol.isDebugCmiRegistry())
            TraceCarol.debugCmiRegistry("Global rebind of " + n);

        synchronized (lreg) {
            try {
                String name = REG_PREFIX + n;
                DistributedEquiv.unexportObject(name);
                DistributedEquiv.exportObject(name, obj);
            } catch (ConfigException e) {
                throw new RemoteException(e.toString());
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
                    String str = (String) o;
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
        for (int i = 0; i < n; i++) {
            tab[i] = (String) it.next();
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
