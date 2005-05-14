/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
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
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi;

/**
 * The Constants interface defines the protocol constants used by this
 * RMI implementation.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public interface Constants {

    /**
     * Protocol constant used by the client to request a remote
     * invocation on the server.
     */
    byte METHOD_CALL = 0;

    /**
     * Protocol constant used by the server to return the normal
     * result of a remote method invocation.
     */
    byte METHOD_RESULT = 1;

    /**
     * Protocol constant used by the server to return an exceptional
     * result of a remote method invocation.
     */
    byte METHOD_ERROR = 2;

    /**
     * Protocol constant used by the server to indicate that a system
     * error occured at some point during an attempt to service a
     * remote invocation request.
     */
    byte SYSTEM_ERROR = 3;

    /**
     * Protocol constant used by the client to indicate the presence
     * of a remote reference to an object.
     */
    byte DGC_PING = 4;

}
