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
 * $Id: JUnicastRemoteObject.java,v 1.7 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.server;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import java.rmi.server.RemoteStub;
import java.rmi.server.ServerCloneException;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;
import org.objectweb.carol.util.configuration.CarolDefaultValues;

import sun.rmi.transport.ObjectTable;

/**
 * Class Extension of <code>UnicastRemoteObject</code> CAROL class ensuring
 * the JRMP context propagation Unicast Reference ensuring context propagation
 * with custom sockets
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JUnicastRemoteObject extends RemoteServer {

    protected RMIClientSocketFactory csf = null;

    protected RMIServerSocketFactory ssf = null;

    private static JUnicastThreadFactory defaultThreadFactory = null;

    //static boolean for local call optimization
    private static boolean localO;

    static {
        localO = new Boolean(System.getProperty(CarolDefaultValues.LOCAL_JRMP_PROPERTY, "false")).booleanValue();
    }

    protected JUnicastRemoteObject(JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis)
            throws RemoteException {
        // search in the jvm properties if the port number properties exist
        this(0, sis, cis);
    }

    protected JUnicastRemoteObject(int p, JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis)
            throws RemoteException {
        JUnicastRemoteObject.exportObject(this, p, sis, cis);
    }

    protected JUnicastRemoteObject(int p, RMIClientSocketFactory csf, RMIServerSocketFactory ssf,
            JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis) throws RemoteException {
        this.csf = csf;
        this.ssf = ssf;
        JUnicastRemoteObject.exportObject(this, p, csf, ssf, sis, cis);
    }

    // exports methods
    protected void exportObject(JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis)
            throws RemoteException {
        if (csf == null && ssf == null) {
            JUnicastRemoteObject.exportObject(this, 0, sis, cis);
        } else {
            JUnicastRemoteObject.exportObject(this, 0, csf, ssf, sis, cis);
        }
    }

    public static RemoteStub exportObject(Remote obj, JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis)
            throws RemoteException {
        return (RemoteStub) JUnicastRemoteObject.exportObject(obj, 0, sis, cis);
    }

    public static Remote exportObject(Remote obj, int p, JServerRequestInterceptor[] sis,
            JClientRequestInterceptor[] cis) throws RemoteException {

        return JUnicastRemoteObject.exportObjectR(obj, new JUnicastServerRef(p, sis, cis));
    }

    public static Remote exportObject(Remote obj, int p, RMIClientSocketFactory csf, RMIServerSocketFactory ssf,
            JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis) throws RemoteException {

        return JUnicastRemoteObject.exportObjectR(obj, new JUnicastServerRefSf(p, csf, ssf, sis, cis));
    }

    /**
     * Real export object (localy and remotly)
     * @param obj
     * @param serverRef
     * @param params
     * @param args
     * @return @throws RemoteException
     */
    protected static Remote exportObjectR(Remote obj, JUnicastServerRef serverRef) throws RemoteException {
        int localId = -2;
        if (localO) {
            localId = JLocalObjectStore.storeObject(obj);
        }
        if (obj instanceof JUnicastRemoteObject) ((JUnicastRemoteObject) obj).ref = serverRef;
        Remote rob = serverRef.exportObject(obj, null, localId);
        return rob;
    }

    /**
     * Real unexport Object (localy and remotly)
     * @param obj
     * @param force
     * @return @throws NoSuchObjectException
     */
    public static boolean unexportObject(Remote obj, boolean force) throws NoSuchObjectException {
        if (localO) {
            JUnicastRef remoteref = (JUnicastRef) ObjectTable.getStub(obj).getRef();
            JLocalObjectStore.removeObject(remoteref.getLocalId());
        }
        return ObjectTable.unexportObject(obj, force);
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        exportObject(null, null);
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            JUnicastRemoteObject cloned = (JUnicastRemoteObject) super.clone();
            cloned.exportObject(null, null);
            return cloned;
        } catch (RemoteException e) {
            throw new ServerCloneException("Clone failed", e);
        }
    }

    /**
     * set the default thread factory to to used when dispatching the call. No
     * new thread is created when the factory is <code>null</code>
     */
    public static void setDefaultThreadFactory(JUnicastThreadFactory factory) {
        defaultThreadFactory = factory;
    }

    /**
     * get the current default thread factory
     */
    public static JUnicastThreadFactory getDefaultThreadFactory() {
        return defaultThreadFactory;
    }
}

