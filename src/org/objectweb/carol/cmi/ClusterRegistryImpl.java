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

// XXX RMIClientSocketFactory & RMIServerSocketFactory ?
public final class ClusterRegistryImpl implements ClusterRegistry {
    /**
     * The registry. Maps names to regular stubs or to <code>clusterobj</code>
     * when the object is in the cluster registry.
     */
    private HashMap lreg = new HashMap();

    private static Object clusterobj = new Object();

    private ClusterRegistryImpl() {
    }

    public static ClusterRegistryKiller start(int port)
        throws RemoteException {
        if (Trace.CREG)
            Trace.out("CREG: starting on port " + port);
        ClusterRegistryImpl creg = new ClusterRegistryImpl();
        ClusterRegistry stub =
            (ClusterRegistry) LowerOrb.exportRegistry(creg, port);
        ClusterRegistryKiller k = new ClusterRegistryKiller(creg, port);
        SecureRandom.doSlowSeed();
        return k;
    }

    public Remote lookup(String n) throws NotBoundException, RemoteException {
        Object obj;
        synchronized (lreg) {
            obj = lreg.get(n);
        }
        if ((obj != null) && (obj != clusterobj)) {
            if (Trace.CREG)
                Trace.out("CREG: local lookup of " + n);
            return (Remote) obj;
        }
        try {
            Remote cs = null;
            if (Trace.CREG)
                Trace.out("CREG: global lookup of " + n);
            //XXX
            try {
                cs = (Remote) DistributedEquiv.getGlobal(n);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (ConfigException e) {
                throw e;
            }
            if (cs != null) {
                return cs;
            }
        } catch (ConfigException e) {
            throw new RemoteException(e.getMessage());
        }
        throw new NotBoundException(n);
    }

    public void bind(String n, Remote obj)
        throws AlreadyBoundException, RemoteException {
        Object cur;
        synchronized (lreg) {
            cur = lreg.get(n);
            if (cur != null)
                throw new AlreadyBoundException(n);
            ClusterConfig cc;
            try {
                cc = ClusterObject.getClusterConfig(obj);
            } catch (Exception e) {
                if (Trace.CREG)
                    Trace.out("CREG: Local bind of " + n);
                lreg.put(n, obj);
                return;
            }
            if (cc.isGlobalAtBind()) {
                if (Trace.CREG)
                    Trace.out("CREG: Global bind of " + n);
                lreg.put(n, clusterobj);
                try {
                    DistributedEquiv.exportObject(n, obj);
                } catch (ConfigException e) {
                    throw new RemoteException(e.getMessage());
                }
            } else {
                throw new RemoteException("not implemented");
            }
        }
    }

    public void unbind(String n) throws NotBoundException, RemoteException {
        Object obj;
        synchronized (lreg) {
            obj = lreg.get(n);
            if (obj == null)
                throw new NotBoundException(n);
            lreg.remove(n);
            if (obj == clusterobj) {
                if (Trace.CREG)
                    Trace.out("CREG: Global unbind of " + n);
                try {
                    DistributedEquiv.unexportObject(n);
                } catch (ConfigException e) {
                    throw new RemoteException(e.getMessage());
                }
            } else if (Trace.CREG)
                Trace.out("CREG: Local unbind of " + n);
        }
    }

    public void rebind(String n, Remote obj) throws RemoteException {
        Object cur;
        synchronized (lreg) {
            cur = lreg.get(n);
            if (cur == clusterobj) {
                if (Trace.CREG)
                    Trace.out("CREG: Rebind: global unbind of " + n);
                try {
                    DistributedEquiv.unexportObject(n);
                } catch (ConfigException e) {
                    throw new RemoteException(e.getMessage());
                }
            }
            ClusterConfig cc;
            try {
                cc = ClusterObject.getClusterConfig(obj);
            } catch (Exception e) {
                if (Trace.CREG)
                    Trace.out("CREG: Local rebind of " + n);
                lreg.put(n, obj);
                return;
            }
            if (cc.isGlobalAtBind()) {
                if (Trace.CREG)
                    Trace.out("CREG: Global rebind of " + n);
                lreg.put(n, clusterobj);
                try {
                    DistributedEquiv.exportObject(n, obj);
                } catch (ConfigException e) {
                    throw new RemoteException(e.getMessage());
                }
            } else {
                throw new RemoteException("not implemented");
            }
        }
    }

    public String[] list() throws RemoteException {
        /*
                DistributedRegistryImpl reg = dreg;
                if (reg == null) reg = waitStartEnd();
                String[] lst1 = reg.list();
                synchronized (lreg) {
                    int n1 = lst1.length;
                    int n2 = lreg.size();
                    String[] lst2 = new String[n1 + n2];
                    for (int i=0; i<n1; i++) lst2[i] = lst1[i];
                    Iterator it = lreg.entrySet().iterator();
                    for (int i=0; i<n2; i++) {
                        Map.Entry e = (Map.Entry)it.next();
                        if (e.getValue() != clusterobj) lst2[n1++] = (String)e.getKey();
                    }
                    String[] lst = new String[n1];
                    for (int i=0; i<n1; i++) lst[i] = lst2[i];
                    return lst;
                }
        */
        throw new RemoteException("not supported");
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
