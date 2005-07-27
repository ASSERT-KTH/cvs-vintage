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
 * $Id: LowerOrb.java,v 1.5 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.rmi.multi.JrmpPRODelegate;

/**
 * @author Simon Nieuviarts
 */
class SunLowerOrb {

    /**
     * sun.rmi.transport.LiveRef
     */
    private static Class liveref;

    /**
     * sun.rmi.transport.LiveRef constructor
     */
    private static Constructor liveref_cons;

    /**
     * sun.rmi.server.UnicastServerRef
     */
    private static Class usref;

    /**
     * sun.rmi.server.UnicastServerRef constructor
     */
    private static Constructor usref_cons;

    /**
     * sun.rmi.server.UnicastServerRef exportObject
     */
    private static Method usref_export;

    /**
     * sun.rmi.transport.tcp.TCPEndpoint
     */
    private static Class tcpep;

    /**
     * sun.rmi.transport.tcp.TCPEndpoint constructor
     */
    private static Constructor tcpep_cons;

    /**
     * sun.rmi.transport.LiveRef constructor
     */
    private static Constructor liveref_cons2;

    /**
     * sun.rmi.server.UnicastRef
     */
    private static Class uref;

    /**
     * sun.rmi.server.UnicastRef constructor
     */
    private static Constructor uref_cons;

    /**
     * True if SUN ORB is identified
     */
    private static boolean init = false;

    static {
        try {
            liveref = Class.forName("sun.rmi.transport.LiveRef");
            Class[] p0 = {ObjID.class, int.class};
            liveref_cons = liveref.getConstructor(p0);
            usref = Class.forName("sun.rmi.server.UnicastServerRef");
            Class[] p1 = {liveref};
            usref_cons = usref.getConstructor(p1);
            Class[] p2 = {Remote.class, Object.class, boolean.class};
            usref_export = usref.getMethod("exportObject", p2);
            tcpep = Class.forName("sun.rmi.transport.tcp.TCPEndpoint");
            Class[] p3 = {String.class, int.class};
            tcpep_cons = tcpep.getConstructor(p3);
            Class ep = Class.forName("sun.rmi.transport.Endpoint");
            Class[] p4 = {ObjID.class, ep, boolean.class};
            liveref_cons2 = liveref.getConstructor(p4);
            uref = Class.forName("sun.rmi.server.UnicastRef");
            Class[] p5 = {liveref };
            uref_cons = uref.getConstructor(p5);
            init = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // Init failed
        } catch (SecurityException e) {
            e.printStackTrace();
            // Init failed
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            // Init failed
        }
    }

    /**
     *
     * @return True if SUN ORB is identified
     */
    public static boolean isValid() {
        return init;
    }

    /**
     * Export thru Sun ORB
     * @param obj object to export
     * @param port port
     * @param id server id
     * @return remote object
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static Remote export(Remote obj, int port, ObjID id) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object[] p0 = {id, new Integer(port)};
        Object lr = liveref_cons.newInstance(p0);
        Object[] p1 = {lr};
        Object usr = usref_cons.newInstance(p1);
        Object[] p2 = {obj, null, new Boolean(true)};
        Object ret = usref_export.invoke(usr, p2);
        return (Remote) ret;
    }

    /**
     * Get a remote ref thru Sun Orb
     * @param host host
     * @param port port
     * @param id server id
     * @return remote object
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static RemoteRef getRemoteRef(String host, int port, ObjID id) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object[] p0 = {host, new Integer(port)};
        Object ep = tcpep_cons.newInstance(p0);
        Object[] p1 = {id, ep, new Boolean(false)};
        Object ref = liveref_cons2.newInstance(p1);
        Object[] p2 = {ref};
        Object rr = uref_cons.newInstance(p2);
        return (RemoteRef)rr;
    }
}

/**
 * @author Simon Nieuviarts
 */
class GnuLowerOrb {

    /**
     * gnu.java.rmi.server.UnicastRef
     */
    private static Class usref;

    /**
     * gnu.java.rmi.server.UnicastRef constructor
     */
    private static Constructor usrefCons;

    /**
     * True if SUN ORB is identified
     */
    private static boolean init = false;

    static {
        try {
            usref = Class.forName("gnu.java.rmi.server.UnicastRef");
            Class[] p0 = {ObjID.class, String.class, int.class, RMIClientSocketFactory.class};
            usrefCons = usref.getConstructor(p0);
            init = true;
        } catch (SecurityException e) {
            // Init failed
        } catch (ClassNotFoundException e) {
            // Init failed
        } catch (NoSuchMethodException e) {
            // Init failed
        }
    }

    /**
    *
    * @return True if GNU ORB is identified
    */
    public static boolean isValid() {
        return init;
    }

    /**
     * Export thru Gnu ORB
     * @param obj object to export
     * @param port port
     * @param id server id
     * @return remote object
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     * @throws RemoteException RemoteException
     */
     public static Remote export(Remote obj, int port, ObjID id) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, RemoteException {
        throw new RemoteException("not implemented");
    }

     /**
      * Get a remote ref thru Gnu Orb
      * @param host host
      * @param port port
      * @param id server id
      * @return remote object
      * @throws IllegalArgumentException IllegalArgumentException
      * @throws InstantiationException InstantiationException
      * @throws IllegalAccessException IllegalAccessException
      * @throws InvocationTargetException InvocationTargetException
      */
    public static RemoteRef getRemoteRef(String host, int port, ObjID id) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object[] p0 = {id, host, new Integer(port), RMISocketFactory.getSocketFactory()};
        Object rr = usrefCons.newInstance(p0);
        return (RemoteRef) rr;
    }
}


/**
 * Exports the internal methods of the underlying ORB.
 *
 * @author Simon Nieuviarts
 */
public class LowerOrb {

    /**
     * Rmi protocols prefix
     */
    private static String prefix = "rmi:";

    /**
     * Default port number
     */
    public static final int DEFAULT_CREG_PORT = 1099;

    /**
     * Reg ID
     */
    public static final int REG_ID = 0xC2C91901;

    /**
     * ObjID
     */
    private static ObjID id = new ObjID(REG_ID);

    /**
     * PortableRemoteObjectDelegate
     */
    private static PortableRemoteObjectDelegate rmi = new JrmpPRODelegate(true);

    /**
     * @see java.rmi.Remote
     */
    public static Remote toStub(Remote obj) throws NoSuchObjectException {
        return rmi.toStub(obj);
    }

    /**
     * @see java.rmi.Remote
     */
    public static void exportObject(Remote obj) throws RemoteException {
        rmi.exportObject(obj);
    }

    /**
     * @see java.rmi.Remote
     */
    public static void unexportObject(Remote obj) throws NoSuchObjectException {
        rmi.unexportObject(obj);
    }

    /**
     * @see java.rmi.Remote
     */
    public static PortableRemoteObjectDelegate getPRODelegate() {
        return rmi;
    }

    /**
     * Export an object thru lower Orb
     * @param obj object to export
     * @param port port
     * @return Remote object
     * @throws RemoteException if an error is encountered
     */
    public static Remote exportRegistry(Remote obj, int port)
        throws RemoteException {
        /*
        LiveRef lref = new LiveRef(id, port);
        UnicastServerRef uref = new UnicastServerRef(lref);
        return uref.exportObject(obj, null, true);
        */
        try {
            if (SunLowerOrb.isValid()) {
                return SunLowerOrb.export(obj, port, id);
            } else if (GnuLowerOrb.isValid()) {
                return GnuLowerOrb.export(obj, port, id);
            } else {
                throw new RemoteException("Don't know how to export the registry : JVM specific");
            }
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RemoteException) {
                throw (RemoteException)t;
            } else {
                throw new RemoteException("Unexpected exception", t);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Unexpected exception", e);
        }
    }

    /**
     * java.rmi.server.RemoteRef
     */
    private static Class[] stubConsParamTypes = { RemoteRef.class };

    /**
     * Get a remote ref thru lower Orb
     * @param className class name
     * @param host host
     * @param port port
     * @return Remote ref
     * @throws RemoteException in an error occurs
     */
    public static Remote getRegistryStub(
        String className,
        String host,
        int port)
        throws RemoteException {
        if (port <= 0) {
            throw new RemoteException("Invalid port no " + port);
        }
        RemoteRef rr;
        try {
            /*
            Endpoint ep = new TCPEndpoint(host, port);
            LiveRef ref = new LiveRef(id, ep, false);
            RemoteRef rr = new UnicastRef(ref);
            */
            if (SunLowerOrb.isValid()) {
                rr = SunLowerOrb.getRemoteRef(host, port, id);
            } else if (GnuLowerOrb.isValid()) {
                rr = GnuLowerOrb.getRemoteRef(host, port, id);
            } else {
                throw new RemoteException("Don't know how to build a stub to the registry : JVM specific");
            }
            Class stubcl = Class.forName(className + "_Stub");
            Object[] p0 = {rr};
            Constructor cons = stubcl.getConstructor(stubConsParamTypes);
            return (RemoteStub) cons.newInstance(p0);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RemoteException) {
                throw (RemoteException) t;
            } else {
                throw new RemoteException("Unexpected exception", t);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Unexpected exception", e);
        }
    }
}
