/**
 * Copyright (C) 2005 - INRIA (www.inria.fr)
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
 * $Id: UnicastJNDIReferenceWrapper.java,v 1.2 2005/08/02 22:02:54 ashah Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.wrapping;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.naming.Reference;

/**
 * Class <code> JNDIReferenceWrapper </code> is the CAROL Remote Reference
 * implementation. This implementation make the reference wrapping to/from a
 * remote object
 * @author Florent Benoit
 */
public class UnicastJNDIReferenceWrapper extends UnicastRemoteObject implements Remote, RemoteReference {

    /**
     * <code>Reference</code> reference to wrap
     */
    private Reference reference;

    /**
     * constructor, export this object
     * @param reference the <code>Reference</code> reference to wrap
     * @param objectPort the port on which export objects
     * @throws RemoteException when super class try to export the object
     */
    public UnicastJNDIReferenceWrapper(Reference reference, int objectPort) throws RemoteException {
        super(objectPort);
        this.reference = reference;
    }

    /**
     * Get the <code>Reference</code> reference
     * @return the <code>Reference</code> reference
     * @throws RemoteException if the reference cannot be returned
     */
    public Reference getReference() throws RemoteException {
        return reference;
    }
}
