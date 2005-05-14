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

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

/**
 * <p>The PRO class implements the {@link
 * PortableRemoteObjectDelegate} interfaced provided by the {@link
 * javax.rmi.CORBA} package as a plugin point for RMI implementations.
 * In order to use this RMI implementation simply set the system
 * property javax.rmi.CORBA.PortableRemoteObjectClass to the name of
 * this class.</p>
 *
 * <p>By default this class uses a {@link Server} instance on a random
 * port with no {@link Interceptor}s. If this behavior needs to be
 * customized, this class may be subclassed by another class with a
 * noargs constructor that invokes the {@link PRO#PRO(Server)}
 * constructor with a {@link Server} instance that has been
 * initialized with the desired {@link Interceptor} implementations
 * and/or port. For example:</p>
 *
 * <code><pre>
 *   public class MyPRO extends PRO {
 *       public MyPRO() {
 *           super(new Server(new MyClientInterceptor(), new MyServerInterceptor()));
 *       }
 *   }
 * </pre></code>
 *
 * <code><pre>
 *   java -Djavax.rmi.CORBA.PortableRemoteObjectClass=MyPRO MyApp
 * </pre></code>
 *
 * @see Server#Server(ClientInterceptor, Interceptor)
 * @see Server#Server(int, ClientInterceptor, Interceptor)
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class PRO implements PortableRemoteObjectDelegate {

    /**
     * The server used by this PRO implementation.
     */
    private Server server;

    /**
     * True iff the server has been started.
     */
    private boolean started = false;

    /**
     * Constructs a new PRO instance with the given {@link Server}.
     * This constructor is for use by subclasses wishing to use a
     * customized Server instance.
     *
     * @param server the Server used by this PRO instance
     */

    protected PRO(Server server) {
        this.server = server;
    }

    /**
     * This is the public noargs constructor used by the RMI runtime
     * to create an instance of this class when it is used directly.
     */

    public PRO() {
        this(new Server());
    }

    public void connect(Remote target, Remote source) throws RemoteException {
        RemoteStub tgt = (RemoteStub) target;
        RemoteStub src = (RemoteStub) source;
        Ref tgtRef = (Ref) tgt.getRef();
        Ref srcRef = (Ref) src.getRef();
        tgtRef.connect(srcRef);
    }

    public synchronized void exportObject(Remote obj) throws RemoteException {
        if (!started) {
            server.start();
            started = true;
        }
        server.export(obj);
    }

    public Object narrow(Object narrowFrom, Class narrowTo) {
        return narrowFrom;
    }

    public Remote toStub(Remote obj) throws NoSuchObjectException {
        if (!started) {
            throw new NoSuchObjectException("" + obj);
        }
        Remote result = server.getStub(obj);
        if (result == null) {
            throw new NoSuchObjectException("" + obj);
        } else {
            return result;
        }
    }

    public void unexportObject(Remote obj) throws NoSuchObjectException {
        if (!server.unexport(obj)) {
            throw new NoSuchObjectException("" + obj);
        }
    }

}
