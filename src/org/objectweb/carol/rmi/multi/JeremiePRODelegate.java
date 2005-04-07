/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * $Id: JeremiePRODelegate.java,v 1.18 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.rmi.exception.NoSuchObjectExceptionHelper;
import org.objectweb.carol.rmi.exception.RemoteExceptionHelper;
import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * class <code>JeremiePRODelegate</code> for the mapping between Jeremie
 * UnicastRemoteObject and PortableRemoteObject
 * @author Guillaume Riviere
 * @author Florent Benoit (exception rethrow)
 */
public class JeremiePRODelegate implements PortableRemoteObjectDelegate {

    /**
     * Extension of Jeremie Stubs
     */
    private static final String JEREMIE_STUB_EXTENSION = "_OWStub";

    /**
     * port number
     */
    private int port;

    /**
     * UnicastRemoteObjectClass
     */
    private static final String JEREMIE_UNICAST_CLASS = "org.objectweb.jeremie.binding.moa.UnicastRemoteObject";

    /**
     * Instance object for this UnicastRemoteObject
     */
    private static Class unicastClass = null;

    /**
     * Number of protocols
     */
    private static int nbProtocols = 0;

    /**
     * Empty constructor for instanciate this class
     * @throws ClassNotFoundException if the class JEREMIE_UNICAST_CLASS cannot be loaded
     */
    public JeremiePRODelegate() throws ClassNotFoundException {
        // class for name
        unicastClass = Thread.currentThread().getContextClassLoader().loadClass(JEREMIE_UNICAST_CLASS);
        Properties prop = ConfigurationRepository.getProperties();
        if (prop != null) {
            String propertyName = CarolDefaultValues.SERVER_JEREMIE_PORT;
            this.port = PortNumber.strToint(prop.getProperty(propertyName, "0"), propertyName);
        }
    }

    /**
     * Makes a server object ready to receive remote calls. Note
     * that subclasses of PortableRemoteObject do not need to call this
     * method, as it is called by the constructor.
     * @param obj the server object to export.
     * @exception RemoteException if export fails.
     */
    public void exportObject(Remote obj) throws RemoteException {
        if (!containsJeremieStub(obj)) {
            return;
        }
        try {
            Method exportO = unicastClass.getMethod("exportObject", new Class[] {Remote.class, Integer.TYPE});
            exportO.invoke(unicastClass, (new Object[] {obj, new Integer(port)}));
        } catch (InvocationTargetException e) {
            throw RemoteExceptionHelper.create(e.getTargetException());
        } catch (Exception e) {
            throw new RemoteException("exportObject() method fails on object '" + obj + "'", e);
        }
    }

    /**
     * Deregisters a server object from the runtime, allowing the object to become
     * available for garbage collection.
     * @param obj the object to unexport.
     * @exception NoSuchObjectException if the remote object is not
     * currently exported.
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        if (!containsJeremieStub(obj)) {
            return;
        }
        try {
            Method unexportO = unicastClass.getMethod("unexportObject", new Class[] {Remote.class, Boolean.TYPE});
            unexportO.invoke(unicastClass, (new Object[] {obj, Boolean.TRUE}));
        } catch (InvocationTargetException e) {
            throw NoSuchObjectExceptionHelper.create(e.getTargetException());
        } catch (Exception e) {
            throw NoSuchObjectExceptionHelper.create("unexportObject() method fails on object '" + obj + "'", e);
        }
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
        // do nothing
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
        if (narrowTo.isAssignableFrom(narrowFrom.getClass())) {
            return narrowFrom;
        } else {
            throw new ClassCastException("Cannot cast '" + narrowFrom.getClass().getName() + "' in '" + narrowTo.getName() + "'.");
        }
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
        try {
            Method exportO = unicastClass.getMethod("toStub", new Class[] {Remote.class});
            return (Remote) exportO.invoke(unicastClass, (new Object[] {obj}));
        } catch (InvocationTargetException e) {
            throw NoSuchObjectExceptionHelper.create(e.getTargetException());
        } catch (Exception e) {
            throw NoSuchObjectExceptionHelper.create("toStub() method fails on object '" + obj + "'", e);
        }
    }

    /**
     * Check if the remote object contains the Jeremie stub in the case of
     * multiprotocols. It can happen that a remote object calls the methods of
     * this delegate object, For example if we try to bind a iiop object in an
     * iiop context, this delegate object is called anyway.
     * @param r remote object
     * @return true if the jeremie stubs are found, else false
     */
    private boolean containsJeremieStub(Remote r) {

        if (nbProtocols == 0) {
            // need to init number of protocols
            nbProtocols = ConfigurationRepository.getActiveConfigurationsNumber();
        }

        // No check on single protocol
        if (nbProtocols == 1) {
            return true;
        }

        // Construct name of a jeremie stub of a remote object
        String stubName = r.getClass().getName() + JEREMIE_STUB_EXTENSION;

        // Convert . into / as we check the resource name
        String resourceName = stubName.replace('.', '/');

        // suffix it by the classname
        resourceName += ".class";

        // return true if the resource is found
        return (Thread.currentThread().getContextClassLoader().getResource(resourceName) != null);

    }

}