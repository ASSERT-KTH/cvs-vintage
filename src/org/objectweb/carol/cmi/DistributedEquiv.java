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
 * $Id: DistributedEquiv.java,v 1.5 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.RemoteException;
import java.util.Set;
import java.io.Serializable;

/**
 * Manage equivalences between objects in the cluster. Two objects are equivalent if
 * their keys have the same value (key1.equals(key2)).
 * The keys prefixed with "REG_" are reserved by the RegistryImpl. No other
 * module should generate such keys.
 *
 * @author Simon Nieuviarts
 */
public final class DistributedEquiv {

    /**
     * lock
     */
    private static Object lock = new Object();

    /**
     * Distributed Equivalent System
     */
    private static DistributedEquivSystem des = null;

    /**
     * The first one which calls this method starts the DistributedEquiv system.
     * It can be the only one to stop it with the ref it gets.
     * @return Distributed Equivalent System
     * @throws ServerConfigException if exception is encountered
     */
    public static DistributedEquiv start() throws ServerConfigException {
        synchronized (lock) {
            if (des != null) {
                throw new ServerConfigException("DistributedEquiv already started");
            }
            try {
                des = new DistributedEquivSystem();
            } catch (ServerConfigException e) {
                throw e;
            } catch (Exception e) {
                throw new ServerConfigException(
                    e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return new DistributedEquiv();
    }

    /**
     * Terminate the Distributed Equivalent System
     * @throws ServerConfigException, if error during stopping
     */
    public void stop() throws ServerConfigException {
        synchronized (lock) {
            des.terminate();
            des = null;
        }
    }

    /**
     * Export object thru the Distributed Equivalent System
     * @param key object key
     * @param obj object
     * @return true if succesfully exported
     * @throws ServerConfigException if des is null
     * @throws RemoteException if remote exception is encountered
     */
    static boolean exportObject(Serializable key, byte[] obj)
        throws ServerConfigException, RemoteException {
        DistributedEquivSystem d = des;
        if (d == null) {
            throw new ServerConfigException("DistributedEquiv not started");
        }
        return d.exportObject(key, obj);
    }

    /**
     * Unexport object thru the Distributed Equivalent System
     * @param key object key
     * @return true if succesfully unexported
     * @throws ServerConfigException if des is null
     */
    static boolean unexportObject(Serializable key) throws ServerConfigException {
        DistributedEquivSystem d = des;
        if (d == null) {
            throw new ServerConfigException("DistributedEquiv not started");
        }
        return d.unexportObject(key);
    }

    /**
     * Gt the server stub list
     * @param key key
     * @return stub list and <code>null<code> if not exported
     * @throws ServerConfigException if des is null
     * @throws RemoteException if remote exception is encountered
     */
    static ServerStubList getGlobal(Serializable key)
        throws ServerConfigException, RemoteException {
        DistributedEquivSystem d = des;
        if (d == null) {
            throw new ServerConfigException("DistributedEquiv not started");
        }
        return d.getGlobal(key);
    }

    /**
     * Get the key set for the des
     * @return key set
     * @throws ServerConfigException if des is null
     */
    static Set keySet() throws ServerConfigException {
        DistributedEquivSystem d = des;
        if (d == null) {
            throw new ServerConfigException("DistributedEquiv not started");
        }
        return d.keySet();
    }
}
