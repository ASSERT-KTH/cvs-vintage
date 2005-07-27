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
 * $Id: ReferenceImpl.java,v 1.2 2005/07/27 11:49:23 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi.jndi;

import javax.naming.Reference;

/**
 * Class <code> ReferenceImpl </code> is an implementation of the RemoteReference interface
 * @author Simon Nieuviarts
 */
public class ReferenceImpl
    extends org.objectweb.carol.cmi.UnicastRemoteObject
    implements RemoteReference {

    /**
     * wrapped reference
     */
    protected Reference ref;

    /**
     * Create a new Wrapper for Reference
     * @param ref reference
     * @throws java.rmi.RemoteException if exception is encountered
     */
    public ReferenceImpl(Reference ref) throws java.rmi.RemoteException {
        this.ref = ref;
    }
    /**
     * Get the wrapped object
     * @return reference to the object
     */
    public Reference getReference() {
        return ref;
    }
}
