/*
 * @(#) JUnicastRemoteObject.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
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
 *
 */
package org.objectweb.carol.rmi.jrmp.server;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import java.rmi.server.RemoteStub;
import java.rmi.server.ServerCloneException;
import java.rmi.server.ServerRef;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;

import sun.rmi.transport.ObjectTable;

/**
 * Class Extension of <code>UnicastRemoteObject</code> CAROL class ensuring the JRMP context propagation
 * Unicast Reference ensuring context propagation with custom sockets
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002 
 */
public class JUnicastRemoteObject extends RemoteServer {

	
    protected RMIClientSocketFactory csf = null;
    protected RMIServerSocketFactory ssf = null;
    private static JUnicastThreadFactory defaultThreadFactory = null;

    // parameter for generic export method
    protected static final Class[] exportObjectParamType = new Class[]{int.class,  
								       JServerRequestInterceptor[].class,  
								       JClientRequestInterceptor[].class};
    protected static final Class[] exportObjectWithFactoryParamType = new Class[]{int.class, RMIClientSocketFactory.class,
										  RMIServerSocketFactory.class,  
										  JServerRequestInterceptor[].class, 
										  JClientRequestInterceptor[].class};
										  
    protected JUnicastRemoteObject(JServerRequestInterceptor [] sis, JClientRequestInterceptor [] cis) throws RemoteException {
	// search in the jvm properties if the port number properties exist
        this(0, sis, cis);
    }

    protected JUnicastRemoteObject(int p, JServerRequestInterceptor [] sis, JClientRequestInterceptor [] cis) throws RemoteException {
        JUnicastRemoteObject.exportObject((Remote) this, p, sis, cis);
    }

    protected JUnicastRemoteObject(int p,
				   RMIClientSocketFactory csf,
				   RMIServerSocketFactory ssf, 
				   JServerRequestInterceptor [] sis,
				   JClientRequestInterceptor [] cis)
            throws RemoteException {
        this.csf = csf;
        this.ssf = ssf;
        JUnicastRemoteObject.exportObject((Remote) this, p, csf, ssf, sis, cis);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
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


    protected void exportObject( JServerRequestInterceptor [] sis, JClientRequestInterceptor [] cis) throws RemoteException {
        if (csf == null && ssf == null) {
            JUnicastRemoteObject.exportObject((Remote) this, 0, sis, cis);
        } else {
            JUnicastRemoteObject.exportObject((Remote) this, 0, csf, ssf, sis, cis);
        }
    }

    public static RemoteStub exportObject(Remote obj,  JServerRequestInterceptor [] sis, JClientRequestInterceptor [] cis)
            throws RemoteException {
        return (RemoteStub) JUnicastRemoteObject.exportObject(obj, 0, sis, cis);
    }

    public static Remote exportObject(Remote obj, int p,  JServerRequestInterceptor [] sis, JClientRequestInterceptor [] cis)
            throws RemoteException {

        return JUnicastRemoteObject.exportObject(obj, "org.objectweb.carol.rmi.jrmp.server.JUnicastServerRef",
                exportObjectParamType,
                new Object[]{ new Integer(p), sis, cis });
    }


    public static Remote exportObject(Remote obj, int p,
                                      RMIClientSocketFactory csf,
                                      RMIServerSocketFactory ssf, 
				      JServerRequestInterceptor [] sis, 
				      JClientRequestInterceptor [] cis)
            throws RemoteException {

        return JUnicastRemoteObject.exportObject(obj, "org.objectweb.carol.rmi.jrmp.server.JUnicastServerRefSf",
                exportObjectWithFactoryParamType,
                new Object[]{ new Integer(p), csf, ssf, sis, cis});
    }

    public static boolean unexportObject(Remote obj, boolean force)
            throws NoSuchObjectException {
        return ObjectTable.unexportObject(obj, force);
    }

    protected static Remote exportObject(Remote obj, String refClassName,  Class[] params, Object[] args)  throws RemoteException {
        Class refClass;
        try {
            refClass = Class.forName(refClassName);
        } catch (ClassNotFoundException e) {
            throw new ExportException("Class " + refClassName + " not found");
        }

        if (!ServerRef.class.isAssignableFrom(refClass)) {
            throw new ExportException("Class " + refClassName + " not instance of " + ServerRef.class.getName());
        }

        // create server ref instance using given constructor and arguments
        ServerRef serverRef;
        try {
            java.lang.reflect.Constructor cons = refClass.getConstructor(params);
            serverRef = (ServerRef) cons.newInstance(args);
            // if impl does extends JUnicastRemoteObject set its ref
            if (obj instanceof JUnicastRemoteObject)
                ((JUnicastRemoteObject) obj).ref = serverRef;

        } catch (Exception e) {
            throw new ExportException("Exception creating instance of " + refClassName, e);
        }
        return serverRef.exportObject(obj, null);
    }

    /**
     * set the default thread factory to to used when dispatching the call.
     * No new thread is created when the factory is <code>null</code>
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

