/**
 * Copyright (C) 2002,2006 - INRIA (www.inria.fr)
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
 * $Id: JUnicastRef.java,v 1.13 2006/03/24 15:01:12 sauthieg Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.server;

//sun import
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.UID;
import java.util.Arrays;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientInterceptorHelper;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JInterceptorHelper;
import org.objectweb.carol.rmi.jrmp.interceptor.JInterceptorStore;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.Connection;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.StreamRemoteCall;

/**
 * Class <code>JUnicastRef</code> is the CAROL JRMP UnicastRef with context
 * propagation
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002 Unicast Reference ensuring context propagation
 */
public class JUnicastRef extends UnicastRef {

    /**
     * flag for local server
     */
    private transient boolean localRef = false;

    /**
     * flag for InetAddress
     */
    private transient byte[] raddr = null;

    /**
     * flag for UID
     */
    private transient UID ruid = null;

    /**
     * Client Interceptor for context propagation
     */
    protected transient JClientRequestInterceptor[] cis = null;

    /**
     * Interceptors initialisers for this References
     */
    protected transient String[] initializers = null;

    private transient int localId = -2;

    /**
     * empty constructor
     */
    public JUnicastRef() {
    }

    /**
     * Constructor without Interceptor
     * @param liveRef the live reference
     */
    public JUnicastRef(LiveRef liveRef) {
        super(liveRef);
    }

    /**
     * Constructor with interceptor
     * @param liveRef the live reference
     * @param cis the client interceptor array
     * @param int localId
     */
    public JUnicastRef(LiveRef liveRef, JClientRequestInterceptor[] cis, String[] initial, int local) {
        super(liveRef);
        this.initializers = initial;
        this.cis = cis;
        this.raddr = JInterceptorHelper.getInetAddress();
        this.ruid = JInterceptorHelper.getSpaceID();
        this.localId = local;
    }

    /**
     * get the ref class always return null
     * @param out the output stream
     */
    public String getRefClass(ObjectOutput out) {
        super.getRefClass(out);
        return null;
    }

    /**
     * wrap the call to send the contexts
     * @param obj the remote object to invoke
     * @param method the method to invoke
     * @param params the methid parametters
     * @param opnum the operation number
     */
    public Object invoke(Remote obj, java.lang.reflect.Method method, Object[] params, long opnum) throws Exception {
        if ((localRef) && (localId != -2)) {
            //System.out.println("local call on object id:"+localId);

            // local call on the object
            try {
                return method.invoke(JLocalObjectStore.getObject(localId), params);
            } catch (InvocationTargetException e) {
                // The application exceptions are embedded into a, InvocationTargetException
                // The code below aims to unwrap them before returning to the upper layer
                Throwable t = e.getCause();
                if (t instanceof Exception) {
                    throw (Exception) e.getCause();
                } else {
                    throw new Exception("Unsupported exception", t);
                }
            } catch (IllegalArgumentException iae) {
                // Usually, this is the Exception returned when method's ClassLoader
                // and locally stored Object does not match.
                // So in this case we fall back to the Remote invokation, using the Remote Object
                return performRemoteCall(method, params, opnum);
            }
        } else {
            return performRemoteCall(method, params, opnum);
        }
    }

    /**
     * Perform a Remote Call.
     * @param method the method to invoke
     * @param params Method parmeters
     * @param opnum operation number
     * @return operation return value.
     * @throws RemoteException
     * @throws Exception
     * @throws Error
     */
    private Object performRemoteCall(java.lang.reflect.Method method, Object[] params, long opnum) throws RemoteException, Exception, Error {
        //System.out.println("remote (local ref="+localRef+") call on
        // object id:"+localId);
        Connection conn = ref.getChannel().newConnection();
        java.rmi.server.RemoteCall call = null;
        boolean reuse = true;
        boolean alreadyFreed = false;

        try {
            call = new JRemoteCall(conn, ref.getObjID(), -1, opnum, cis);
            try {
                ObjectOutput out = call.getOutputStream();
                marshalCustomCallData(out);
                Class[] types = method.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    marshalValue(types[i], params[i], out);
                }
            } catch (IOException e) {
                throw new MarshalException("error marshalling arguments" + e);
            }

            // unmarshal return
            call.executeCall();

            try {
                Class rtype = method.getReturnType();
                if (rtype == void.class) {
                    return null;
                }
                ObjectInput in = call.getInputStream();
                Object returnValue = unmarshalValue(rtype, in);
                alreadyFreed = true;
                ref.getChannel().free(conn, true);

                return returnValue;

            } catch (IOException e) {
                throw new UnmarshalException("IOException unmarshalling return" + e);
            } catch (ClassNotFoundException e) {
                throw new UnmarshalException("ClassNotFoundException unmarshalling return" + e);
            } finally {
                try {
                    call.done();
                } catch (IOException e) {
                    reuse = false;
                }
            }

        } catch (RuntimeException e) {
            if ((call == null) || (((StreamRemoteCall) call).getServerException() != e)) {
                reuse = false;
            }
            throw e;

        } catch (RemoteException e) {
            reuse = false;
            throw e;

        } catch (Error e) {
            reuse = false;
            throw e;

        } finally {
            if (!alreadyFreed) {
                ref.getChannel().free(conn, reuse);
            }
        }
    }

    /**
     * v1.1 style of Stubs call this invoke
     * @param call the remote call
     * @deprecated
     */
    public void invoke(java.rmi.server.RemoteCall call) throws Exception {
        super.invoke(call);
    }

    /**
     * override v1.1 RemoteCall instanciation to wrap the call and send the
     * context
     * @param obj the remote object
     * @param ops the operation
     * @param opnum the operation number
     * @param hash the hash code
     * @deprecated
     */
    public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) throws RemoteException {

        Connection conn = ref.getChannel().newConnection();
        try {
            RemoteCall call = new JRemoteCall(conn, ref.getObjID(), opnum, hash, cis);
            try {
                marshalCustomCallData(call.getOutputStream());
            } catch (IOException e) {
                throw new MarshalException("error marshaling " + "custom call data");
            }
            return call;
        } catch (RemoteException e) {
            ref.getChannel().free(conn, false);
            throw e;
        }
    }

    /**
     * override marshalCustomCallData to pass and disociate contexts on the
     * client
     * @param out the ObjectOutput for the call marchalling
     */
    protected void marshalCustomCallData(ObjectOutput out) throws IOException {
        JClientInterceptorHelper.send_request(out, cis, localRef);
        super.marshalCustomCallData(out);
    }

    /**
     * override readExternal to initialise localRef We could actually receive
     * anything from the server on lookup
     * @param in the ObjectInput
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternal(in, false);
    }

    /**
     * override writeExternal to send spaceID We could actually send anything to
     * the client on lookup
     * @param out the object output stream
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternal(out, false);
    }

    /**
     * override readExternal to initialise localRef We could actually receive
     * anything from the server on lookup
     * @param in the object input
     * @param newFormat the new format boolean
     */
    public void readExternal(ObjectInput in, boolean newFormat) throws IOException, ClassNotFoundException {
        raddr = new byte[in.readInt()];
        in.read(raddr);
        ruid = UID.read(in);
        localRef = ((Arrays.equals(raddr, JInterceptorHelper.getInetAddress())) && (ruid.equals(JInterceptorHelper
                .getSpaceID())));
        // write initializers array in UTF
        String[] ia = new String[in.readInt()];
        for (int i = 0; i < ia.length; i++) {
            ia[i] = in.readUTF();
        }
        this.initializers = ia;
        cis = JInterceptorStore.setRemoteInterceptors(raddr, ruid, ia);
        localId = in.readInt();
        ref = LiveRef.read(in, newFormat);
    }

    /**
     * override writeExternal to send spaceID And the interceptor We could
     * actually send anything to the client on lookup
     * @param out the object output
     * @param newFormat the boolean new format
     */
    public void writeExternal(ObjectOutput out, boolean newFormat) throws IOException {
        out.writeInt(raddr.length);
        out.write(raddr);
        ruid.write(out);
        // write initializers array in UTF
        String[] ia = this.initializers;
        out.writeInt(ia.length);
        for (int i = 0; i < ia.length; i++) {
            out.writeUTF(ia[i]);
        }
        // write the local identificator
        out.writeInt(getLocalId());
        ref.write(out, newFormat);
        out.flush();
    }

    /**
     * @return
     */
    public int getLocalId() {
        // TODO Auto-generated method stub
        return localId;
    }
}