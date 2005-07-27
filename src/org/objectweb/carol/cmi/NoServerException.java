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
 * $Id: NoServerException.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.RemoteException;

/**
 * Thrown when no more remote object is available in the cluster for a
 * remote procedure call.
 *
 * @author Simon Nieuviarts
 */
public class NoServerException extends RemoteException {
    /**
     * Creates an instance without argument
     *
     */
    public NoServerException() {
        super();
    }
    /**
     * Creates an instance with a message
     * @param message message
     */
    public NoServerException(String message) {
        super(message);
    }

    /**
     * Creates an instance with a message and an exception
     * @param message message
     * @param ex exception
     */
    public NoServerException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
