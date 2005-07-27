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
 * $Id: DistributedEquivStubs.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.RemoteException;
import java.util.TreeMap;

/**
 * A list of stubs to equivalent objects in the cluster. It is used by the
 * equivalence system to create cluster stubs on user requests. This object may
 * contain incompatible stubs while the application is being redeployed on the
 * cluster. At that time, clients receive errors when they want to use them. The
 * stubs are not deserialized because the application may not be in the
 * classpath.
 *
 * @author Simon Nieuviarts
 */
class DistributedEquivStubs {
    /**
     * Maps server ids (ClusterId) to ObjectId.
     */
    private TreeMap srvMap = new TreeMap();

    /**
     * Maps ObjectId to StubData.
     */
    private TreeMap objMap = new TreeMap();

    /**
     * Server stub list
     */
    private ServerStubList stubList = null;

    /**
     * Creates a object instance
     * @throws RemoteException if exception is encountered
     */
    DistributedEquivStubs() throws RemoteException {
    }

    /**
     * Get the server stub list
     * @return stub list
     * @throws RemoteException if exception is encountered
     */
    public synchronized ServerStubList getServerStubList()
            throws RemoteException {
        if (stubList == null) {
            stubList = new ServerStubList(objMap.values());
        }
        return stubList;
    }

    /**
     * Set a stub in the map
     * @param stub stub to set
     */
    public synchronized void setStub(StubData stub) {
        ObjectId objId = stub.getObjectId();
        ClusterId srvId = objId.getServerId();
        Object prevOid = srvMap.put(srvId, objId);
        // If already in the list, do nothing
        if (prevOid != null) {
            if (prevOid.equals(objId)) {
                return;
            }
            objMap.remove(prevOid);
        }
        objMap.put(objId, stub);
        stubList = null;
    }

    /**
     * Remove a stub in the map
     * @param serverId stub to remove
     * @return true is the map is empty
     */
    public synchronized boolean removeStub(ClusterId serverId) {
        Object objId = srvMap.remove(serverId);
        if (objId != null) {
            objMap.remove(objId);
            stubList = null;
        }
        return srvMap.isEmpty();
    }
}