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
 * $Id: JrmpPRODelegate.java,v 1.16 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Properties;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JInterceptorStore;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.server.JUnicastRemoteObject;
import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * Class <code>JrmpPRODelegate</code> for the mapping between Rmi jrmp
 * UnicastRemoteObject and PortableRemoteObject
 */
public class JrmpPRODelegate implements PortableRemoteObjectDelegate {

    /**
     * private port number
     */
    private int port;

    /**
     * private Interceptor for Context propagation
     */
    private JServerRequestInterceptor[] sis = null;

    /**
     * private Interceptor for Context propagation
     */
    private JClientRequestInterceptor[] cis = null;

    /**
     * Constructor
     * @param usingCmi this prodelegate will be used for CMI protocol
     */
    public JrmpPRODelegate(boolean usingCmi) {
        sis = JInterceptorStore.getLocalServerInterceptors();
        cis = JInterceptorStore.getLocalClientInterceptors();
        Properties prop = ConfigurationRepository.getProperties();
        if (!usingCmi && prop != null) {
            String propertyName = CarolDefaultValues.SERVER_JRMP_PORT;
            this.port = PortNumber.strToint(prop.getProperty(propertyName, "0"), propertyName);
        }
    }

    /**
     * By default, this class is not used for Cmi. Cmi has to instantiated the other constructor
     */
    public JrmpPRODelegate() {
        this(false);
    }

    /**
     * Makes a server object ready to receive remote calls. Note that subclasses
     * of PortableRemoteObject do not need to call this method, as it is called
     * by the constructor.
     * @param obj the server object to export.
     * @exception RemoteException if export fails.
     */
    public void exportObject(Remote obj) throws RemoteException {
        JUnicastRemoteObject.exportObject(obj, port, sis, cis);
    }

    /**
     * Deregisters a server object from the runtime, allowing the object to
     * become available for garbage collection.
     * @param obj the object to unexport.
     * @exception NoSuchObjectException if the remote object is not currently
     *            exported.
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        JUnicastRemoteObject.unexportObject(obj, true);
    }

    /**
     * Makes a Remote object ready for remote communication. This normally
     * happens implicitly when the object is sent or received as an argument on
     * a remote method call, but in some circumstances it is useful to perform
     * this action by making an explicit call. See the {@link Stub#connect}
     * method for more information.
     * @param target the object to connect.
     * @param source a previously connected object.
     * @throws RemoteException if <code>source</code> is not connected or if
     *         <code>target</code> is already connected to a different ORB
     *         than <code>source</code>.
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
            throw new ClassCastException("Cannot cast '" + narrowFrom.getClass().getName() + "' in '"
                    + narrowTo.getName() + "'.");
        }
    }

    /**
     * Returns a stub for the given server object.
     * @param obj the server object for which a stub is required. Must either be
     *        a subclass of PortableRemoteObject or have been previously the
     *        target of a call to {@link #exportObject}.
     * @return the most derived stub for the object.
     * @exception NoSuchObjectException if a stub cannot be located for the
     *            given server object.
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
        return RemoteObject.toStub(obj);
    }
}