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
 * $Id: UnicastRemoteObject.java,v 1.2 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * To export objects in the cluster. Prefer <code>PRODelegate</code>.
 *
 * @author Simon Nieuviarts
 */
public class UnicastRemoteObject implements Remote {

    /**
     * @see java.rmi.Remote#UnicastRemoteObject
     */
    protected UnicastRemoteObject() throws RemoteException {
        exportObject((Remote) this);
    }

    /**
     * @see java.rmi.Remote#clone
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Not implemented");
    }

    /**
     * @see java.rmi.Remote#exportObject
     */
    public static void exportObject(Remote obj)
        throws RemoteException {
        LowerOrb.exportObject(obj);
    }

    /**
     * @see java.rmi.Remote#unexportObject
     */
    public static void unexportObject(Remote obj)
        throws NoSuchObjectException {
        LowerOrb.unexportObject(obj);
    }
}
