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
package org.objectweb.carol.irmi.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TraceSocketFactory
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class TraceSocketFactory extends RMISocketFactory {

    private RMISocketFactory delegate;
    private List clients = Collections.synchronizedList(new ArrayList());
    private List servers = Collections.synchronizedList(new ArrayList());

    TraceSocketFactory(RMISocketFactory delegate) {
        this.delegate = delegate;
    }

    public List getClientTraces() {
        return clients;
    }

    public List getServerTraces() {
        return servers;
    }

    public Socket createSocket(String host, int port) throws IOException {
        clients.add(new Trace());
        return delegate.createSocket(host, port);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        servers.add(new Trace());
        return delegate.createServerSocket(port);
    }

}
