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
 * $Id: RegistryImpl.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

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

/**
 * Server part of the CMI registry.
 *
 * @author Simon Nieuviarts
 */
public final class RegistryImpl implements RegistryInternal {

    /**
     * The non-clustered objects registry. Maps names to serialized regular
     * stubs for non clustered objects.
     */
    private HashMap lreg = new HashMap();

    /**
     * Registry prefix
     */
    public static final String REG_PREFIX = "REG_";

    /**
     * Protect the registry against multiple starts in the same JVM
     */
    private static Object lock = new Object();

    /**
     * Flag set if Registry is started
     */
    private boolean started = false;

    /**
     * Constructor
     * @throws RemoteException if an exception is encountered
     */
    private RegistryImpl() throws RemoteException {
        synchronized (lock) {
            if (started) {
                throw new RemoteException("Can't start multiple cluster registries in the same JVM");
            }
            started = true;
        }
    }

    /**
     * Start the registry
     * @param port port number
     * @return Handle to allow the registry killing
     * @throws RemoteException if an exception is encountered
     */
    public static RegistryKiller start(int port)
        throws RemoteException {
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("registry starting on port " + port);
        }
        RegistryImpl creg = new RegistryImpl();
        RegistryInternal stub =
            (RegistryInternal) LowerOrb.exportRegistry(creg, port);
        RegistryKiller k = new RegistryKiller(creg, port);
        return k;
    }

    /**
     * Retrieve an object by a name
     * @param n name to search
     * @return object associated
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
    public Object lookup(String n) throws NotBoundException, RemoteException {
        Object obj;
        synchronized (lreg) {
            obj = lreg.get(n);
        }
        if (obj != null) {
            if (TraceCarol.isDebugCmiRegistry()) {
                TraceCarol.debugCmiRegistry("lookup found local object for " + n);
            }
            return obj;
        }
        try {
            if (TraceCarol.isDebugCmiRegistry()) {
                TraceCarol.debugCmiRegistry("lookup found global object for " + n);
            }
            ServerStubList sl = DistributedEquiv.getGlobal(REG_PREFIX + n);
            if (sl != null) {
                if (TraceCarol.isDebugCmiRegistry()) {
                    TraceCarol.debugCmiRegistry("returned a cluster stub");
                }
                return sl.getSerialized();
            }
        } catch (ServerConfigException e) {
            throw new RemoteException(e.toString());
        } catch (IOException e) {
            throw new RemoteException(e.toString());
        }
        throw new NotBoundException(n);
    }

    /**
     * Bind a single entry
     * @param n name
     * @param obj object
     * @throws AlreadyBoundException if the entry is already bound
     * @throws RemoteException if an exception is encountered
     */
    public void bindSingle(String n, Remote obj)
        throws AlreadyBoundException, RemoteException {
/*
            byte[] prefix = new byte[obj.length + 1];
            prefix[0] = NOT_CLUSTERED;
            System.arraycopy(obj, 0, prefix, 1, obj.length);
*/
        Object cur;
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("Local bind of " + n);
        }
        synchronized (lreg) {
            cur = lreg.get(n);
            if (cur != null) {
                throw new AlreadyBoundException(n);
            }
            lreg.put(n, obj);
        }
    }

    /**
     * Rebind a single entry
     * @param n name
     * @param obj object
     * @throws RemoteException if an exception is encountered
     */
    public void rebindSingle(String n, Remote obj) throws RemoteException {
/*
        byte[] prefix = new byte[obj.length + 1];
        prefix[0] = NOT_CLUSTERED;
        System.arraycopy(obj, 0, prefix, 1, obj.length);
*/
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("Local rebind of " + n);
        }
        synchronized (lreg) {
            lreg.put(n, obj);
        }
    }

    /**
     * Bind a cluster entry
     * @param n cluster name
     * @param obj array of objects
     * @throws AlreadyBoundException if the entry is already bound
     * @throws RemoteException if an exception is encountered
     */
    public void bindCluster(String n, byte[] obj)
        throws AlreadyBoundException, RemoteException {
        Object cur;
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("Global bind of " + n);
        }
        synchronized (lreg) {
            try {
                if (!DistributedEquiv.exportObject(REG_PREFIX + n, obj)) {
                    throw new AlreadyBoundException(n);
                }
            } catch (ServerConfigException e) {
                throw new RemoteException(e.toString());
            }
        }
    }

    /**
     * Unbind an entry
     * @param n entry to unregister
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
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
                if (TraceCarol.isDebugCmiRegistry()) {
                    TraceCarol.debugCmiRegistry("Global unbind of " + n);
                }
                try {
                    if (!DistributedEquiv.unexportObject(REG_PREFIX + n)) {
                        throw new NotBoundException(n);
                    }
                } catch (ServerConfigException e) {
                    throw new RemoteException(e.toString());
                }
            }
    }

    /**
     * Rebind a cluster entry
     * @param n cluster name
     * @param obj array of objects
     * @throws RemoteException if an exception is encountered
     */
    public void rebindCluster(String n, byte[] obj) throws RemoteException {
        Object cur;
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("Global rebind of " + n);
        }

        synchronized (lreg) {
            try {
                String name = REG_PREFIX + n;
                DistributedEquiv.unexportObject(name);
                DistributedEquiv.exportObject(name, obj);
            } catch (ServerConfigException e) {
                throw new RemoteException(e.toString());
            }
        }
    }

    /**
     * Get the entries list
     * @return entries
     * @throws RemoteException if Exception is encountered
     */
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
        } catch (ServerConfigException e) {
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

    /**
     * Test method
     * @throws RemoteException if exception is encountered
     */
    public void test() throws RemoteException {
    }

    /**
     * Test purposes
     * @param args program args
     * @throws Exception if error
     */
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
