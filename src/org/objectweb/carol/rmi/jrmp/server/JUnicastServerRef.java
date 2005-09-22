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
 * $Id: JUnicastServerRef.java,v 1.8 2005/09/22 17:46:43 el-vadimo Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.server;

// sun import
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteRef;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JInterceptorStore;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerInterceptorHelper;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;

import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.LiveRef;

/**
 * Class <code>JUnicastServerRef</code> implements the remote reference layer
 * server-side behavior for remote objects exported with the JUnicastRef
 * reference type.
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JUnicastServerRef extends UnicastServerRef {

    /**
     * ServerRequestInterceptor array
     */
    protected JServerRequestInterceptor[] sis = null;

    /**
     * ClientRequestInterceptor array
     */
    protected JClientRequestInterceptor[] cis = null;

    private int localId = -2;

    /**
     * constructor
     */
    public JUnicastServerRef() {
    }

    /**
     * Constructor with interceptor
     * @param ref the live reference
     * @param sis the server interceptor array
     * @param cis the client interceptor array
     */
    public JUnicastServerRef(LiveRef ref, JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis) {
        super(ref);
        this.sis = sis;
        this.cis = cis;
    }

    /**
     * Constructor with interceptor
     * @param port the port reference
     * @param sis the server interceptor array
     * @param cis the client interceptor array
     */
    public JUnicastServerRef(int port, JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis) {
        super(new LiveRef(port));
        this.sis = sis;
        this.cis = cis;
    }

    /**
     * get the ref class name
     * @return String the class name
     */
    public String getRefClass(ObjectOutput out) {
        super.getRefClass(out);
        return "org.objectweb.carol.rmi.jrmp.server.JUnicastServerRef";
    }

    /**
     * use a different kind of RemoteRef instance. This method is used by the
     * remote client to get the Client reference
     * @return remote Ref the remote reference
     */
    protected RemoteRef getClientRef() {
        return new JUnicastRef(ref, cis, JInterceptorStore.getJRMPInitializers(), localId);
    }

    /**
     * @param obj
     * @param localId
     * @param object
     * @return
     */
    public Remote exportObject(Remote obj, Object object, int localId) throws RemoteException {
        this.localId = localId;
        return super.exportObject(obj, object);
    }

    /**
     * override unmarshalCustomCallData to receive and establish contexts sent
     * by the client
     * @param in the object input
     */
    protected void unmarshalCustomCallData(ObjectInput in) throws IOException, ClassNotFoundException {
        JServerInterceptorHelper.receive_request(in, sis);
        super.unmarshalCustomCallData(in);
    }

    /**
     * override dispatch to use a specific thread factory
     * @param obj the remote object
     * @param call the remote call on this object
     */
    public void dispatch(Remote obj, RemoteCall call) throws IOException {
        super.dispatch(obj, new JRemoteServerCall(call, sis));
    }
}
