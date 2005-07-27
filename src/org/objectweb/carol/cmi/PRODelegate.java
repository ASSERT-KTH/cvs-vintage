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
 * $Id: PRODelegate.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

// rmi import
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

/**
 * Class <code>PRODelegate</code> for use by
 * <code>PortableRemoteObject</code>.
 *
 * @author Simon Nieuviarts
 */
public class PRODelegate implements PortableRemoteObjectDelegate {

    /**
     * ORB to export clustered objects.
     */
    private PortableRemoteObjectDelegate rmi;

    /**
     * Equiv prefix
     */
    public static final String EQUIV_PREFIX = "EXPORT_";

    /**
     * Get the lower ORB delegate to export objects.
     */
    public PRODelegate() {
        rmi = LowerOrb.getPRODelegate();
    }

    /**
     * Export a Remote Object
     * @param obj Remote object to export
     * @exception RemoteException exporting remote object problem
     */
    public void exportObject(Remote obj) throws RemoteException {
        rmi.exportObject(obj);
        String equiv = StubConfig.clusterEquivAtExport(obj);
        if (equiv != null) {
            byte[] ser = CmiOutputStream.serialize(rmi.toStub(obj));
            try {
                if (!DistributedEquiv.exportObject(EQUIV_PREFIX + equiv, ser)) {
                    throw new RemoteException(equiv + " : already exported");
                }
            } catch (ServerConfigException e) {
                throw new RemoteException("", e);
            }
        }
    }

    /**
     * Method for unexport object
     * @param obj Remote obj object to unexport
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        String equiv = null;
        try {
            equiv = StubConfig.clusterEquivAtExport(obj);
        } catch (StubConfigException e) {
        }
        if (equiv != null) {
            //XXX
            throw new NoSuchObjectException("Not supported");
        }
        rmi.unexportObject(obj);
    }

    /**
     * Connection method
     * @param target a remote object;
     * @param source another remote object;
     * @exception RemoteException if the connection fail
     */
    public void connect(Remote target, Remote source) throws RemoteException {
        throw new RemoteException("not supported");
    }

    /**
     * Narrow method
     * @param obj Remote obj the object to narrow
     * @param newClass Class newClass the expected type of the result
     * @return an object of type newClass
     * @exception ClassCastException if the obj class is not compatible with a
     *            newClass cast
     */
    public Object narrow(Object obj, Class newClass) throws ClassCastException {
        if (newClass.isAssignableFrom(obj.getClass())) {
            return obj;
        } else {
            throw new ClassCastException("Can't cast " + obj.getClass().getName() + " in " + newClass.getName());
        }
    }

    /**
     * To stub method
     * @return the stub object
     * @param obj Remote object to unexport
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
        String equiv = null;
        try {
            equiv = StubConfig.clusterEquivAtExport(obj);
        } catch (StubConfigException e) {
        }
        if (equiv != null) {
            try {
                ServerStubList sl = DistributedEquiv.getGlobal(EQUIV_PREFIX + equiv);
                return sl.getClusterStub();
            } catch (ServerConfigException e) {
                NoSuchObjectException e0 = new NoSuchObjectException(equiv);
                e0.initCause(e);
                throw e0;
            } catch (RemoteException e) {
                NoSuchObjectException e0 = new NoSuchObjectException(equiv);
                e0.initCause(e);
                throw e0;
            }
        }
        return rmi.toStub(obj);
    }
}
