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
 * $Id: JNDIResourceWrapper.java,v 1.3 2005/02/08 09:43:11 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.wrapping;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Class <code> JNDIResourceWrapper </code> is the CAROL Remote Resource
 * implementation. This implementation make the serializable resource wrapping
 * to/from a remote object
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 */
public class JNDIResourceWrapper implements JNDIRemoteResource {

    /**
     * <code>Serializable</code> resource to wrap
     */
    private Serializable resource;

    /**
     * constructor, export this object
     * @param resource the <code>Serializable</code> resource to wrap
     */
    public JNDIResourceWrapper(Serializable resource) {
        super();
        this.resource = resource;
    }

    /**
     * Get the <code>Serializable</code> resource
     * @return the <code>Serializable</code> resource
     * @throws RemoteException if the resource cannot be returned
     */
    public Serializable getResource() throws RemoteException {
        return resource;
    }
}