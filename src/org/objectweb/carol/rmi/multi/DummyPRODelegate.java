/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * $Id: DummyPRODelegate.java,v 1.1 2005/03/10 16:58:30 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

/**
 * Class <code>DummyPRODelegate</code> is a fake PortableRemoteObject for local
 * methods call
 */
public class DummyPRODelegate implements PortableRemoteObjectDelegate {

    /**
     * Export a Remote Object
     * @param obj to export
     * @throws RemoteException exporting remote object problem
     */
    public void exportObject(Remote obj) throws RemoteException {

    }

    /**
     * Method for unexport object
     * @param obj object to unexport
     * @throws NoSuchObjectException if the object is not currently exported
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {

    }

    /**
     * Connection method
     * @param target a remote object;
     * @param source another remote object;
     * @throws RemoteException if the connection fail
     */
    public void connect(Remote target, Remote source) throws RemoteException {

    }

    /**
     * Narrow method
     * @param obj the object to narrow
     * @param newClass the expected type of the result
     * @return an object of type newClass
     * @throws ClassCastException if the obj class is not compatible with a
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
     * @param obj to unexport
     * @throws NoSuchObjectException if the object is not currently exported
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
        return obj;
    }
}