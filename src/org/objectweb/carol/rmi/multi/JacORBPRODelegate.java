/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2005 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: JacORBPRODelegate.java,v 1.4 2005/02/04 13:55:35 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.util.configuration.TraceCarol;


/**
 * TODO : Use the same class for all JVM (maybe Classpath implementation ?)
 * @author Florent Benoit
 */
public class JacORBPRODelegate implements PortableRemoteObjectDelegate {

    /**
     * Used by Sun 1.4 and Bea 1.4
     */
    private static final String SUN_JDK14_CLASS = "com.sun.corba.se.internal.javax.rmi.PortableRemoteObject";

    /**
     * Used by IBM 1.4 JDK
     */
    private static final String IBM_JDK14_CLASS = "com.ibm.CORBA.iiop.PortableRemoteObject";

    /**
     * Used by Sun 5.0 JDK
     */
    private static final String SUN_JDK50_CLASS = "com.sun.corba.se.impl.javax.rmi.PortableRemoteObject";

    /**
     * Default JacORB class is the other classes cannot be loaded
     */
    private static final String DEFAULT_JACORB_CLASS = "org.jacorb.orb.rmi.PortableRemoteObjectDelegateImpl";


    /**
     * List of classes by order (try first, if not present, try next and etc.)
     */
    private static final String[] DELEGATE_CLASSES = new String[] {SUN_JDK14_CLASS, IBM_JDK14_CLASS, SUN_JDK50_CLASS };


    /**
     * internal delegate object
     */
    private static PortableRemoteObjectDelegate delegate = null;

    /**
     * Makes a server object ready to receive remote calls. Note
     * that subclasses of PortableRemoteObject do not need to call this
     * method, as it is called by the constructor.
     * @param obj the server object to export.
     * @exception RemoteException if export fails.
     */
    public void exportObject(Remote obj) throws RemoteException {
        getDelegate().exportObject(obj);

    }

    /**
     * Deregisters a server object from the runtime, allowing the object to become
     * available for garbage collection.
     * @param obj the object to unexport.
     * @exception NoSuchObjectException if the remote object is not
     * currently exported.
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        getDelegate().unexportObject(obj);

    }

    /**
     * Returns a stub for the given server object.
     * @param obj the server object for which a stub is required. Must either be a subclass
     * of PortableRemoteObject or have been previously the target of a call to
     * {@link #exportObject}.
     * @return the most derived stub for the object.
     * @exception NoSuchObjectException if a stub cannot be located for the given server object.
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
        return getDelegate().toStub(obj);
    }

    /**
     * Makes a Remote object ready for remote communication. This normally
     * happens implicitly when the object is sent or received as an argument
     * on a remote method call, but in some circumstances it is useful to
     * perform this action by making an explicit call.  See the
     * {@link Stub#connect} method for more information.
     * @param target the object to connect.
     * @param source a previously connected object.
     * @throws RemoteException if <code>source</code> is not connected
     * or if <code>target</code> is already connected to a different ORB than
     * <code>source</code>.
     */
    public void connect(Remote target, Remote source) throws RemoteException {
        getDelegate().connect(target, source);

    }

    /**
     * Checks to ensure that an object of a remote or abstract interface type
     * can be cast to a desired type.
     * @param narrowFrom the object to check.
     * @param narrowTo the desired type.
     * @return an object which can be cast to the desired type.
     * @throws ClassCastException if narrowFrom cannot be cast to narrowTo.
     */
    public Object narrow(Object narrowFrom, Class narrowTo) throws ClassCastException {
        return getDelegate().narrow(narrowFrom, narrowTo);
    }


    /**
     * Instantiate the delegate object by trying the different JVM classes
     * or use the default class if none is found.
     * Once the class is loaded, return always the same object.
     * @return the delegate portable remote object
     */
    private PortableRemoteObjectDelegate getDelegate() {
        // delegate already loaded
        if (delegate != null) {
            return delegate;
        }

        Class clazz = null;
        // use thread classloader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // Need to find one class present in current JVM

        boolean classFound = false;
        int i = 0;
        while (!classFound && i < DELEGATE_CLASSES.length) {
            // get the classname
            String cls = DELEGATE_CLASSES[i];

            // debug trace
            if (TraceCarol.isDebugCarol()) {
                TraceCarol.debugCarol("Trying with class '" + cls + "'.");
            }

            try {
                // Try to load the class
                clazz = cl.loadClass(cls);
                if (TraceCarol.isDebugCarol()) {
                    TraceCarol.debugCarol("Class found, Use as prodelegate class : '" + cls + "'.");
                }
                // class is available, the class is found
                classFound = true;
            } catch (ClassNotFoundException cnfesun) {
                // class not available, try with next one if available
                if (TraceCarol.isDebugCarol()) {
                    TraceCarol.debugCarol("Class '" + cls + "' not available.");
                }
            }

            // increment item
            i++;
        }

        // No class was found, use default one
        if (!classFound) {
            try {
                clazz = cl.loadClass(DEFAULT_JACORB_CLASS);
            } catch (ClassNotFoundException cnfejacorb) {
                throw new IllegalArgumentException("Could not load default class '" + DEFAULT_JACORB_CLASS + "' :" + cnfejacorb.getMessage());
            }
            TraceCarol.infoCarol("Using default Jacorb delegate class and not the JVM class as JVM class was not found. It may fail in some cases.");
        }


        // new instance of the object
        Object o = null;
        try {
            o = clazz.newInstance();
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("Cannot make instance of class : '" + clazz + "' : " + iae.getMessage());
        } catch (InstantiationException ie) {
            throw new IllegalArgumentException("Cannot make instance of class : '" + clazz + "' : " + ie.getMessage());
        }

        // then cast as PortableRemoteObjectDelegate
        if (o instanceof PortableRemoteObjectDelegate) {
            delegate = (PortableRemoteObjectDelegate) o;
            return delegate;
        } else {
            throw new IllegalArgumentException("Object '" + o + "' is not an instance of PortableRemoteObjectDelegate");
        }

    }


}