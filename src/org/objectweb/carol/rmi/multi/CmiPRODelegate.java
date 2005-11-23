/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 * $Id: CmiPRODelegate.java,v 1.5 2005/11/23 21:35:40 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.cmi.PRODelegate;

/**
 * Class <code>CmiPRODelegate</code> for the mapping between cmi
 * UnicastRemoteObject and PortableRemoteObject
 * @author Simon Nieuviarts
 */
public class CmiPRODelegate implements PortableRemoteObjectDelegate {

    /**
     * ORB to export clustered objects.
     */
    private PortableRemoteObjectDelegate cmi;

    /**
     * Get the lower ORB delegate to export objects.
     */
    public CmiPRODelegate() {
        PortableRemoteObjectDelegate rmi = new JrmpPRODelegate() ;
        cmi = new PRODelegate(rmi);
    }

    /**
     * Export a Remote Object
     * @param obj Remote object to export
     * @exception RemoteException exporting remote object problem
     */
    public void exportObject(Remote obj) throws RemoteException {
        cmi.exportObject(obj);
    }

    /**
     * Method for unexport object
     * @param obj Remote obj object to unexport
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        cmi.unexportObject(obj);
    }

    /**
     * Connection method
     * @param target a remote object;
     * @param source another remote object;
     * @exception RemoteException if the connection fail
     */
    public void connect(Remote target, Remote source) throws RemoteException {
        cmi.connect(target, source);
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
        return cmi.narrow(obj, newClass);
    }

    /**
     * To stub method
     * @return the stub object
     * @param obj Remote object to unexport
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
        return cmi.toStub(obj);
    }
}