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
 * $Id: JeremiePRODelegate.java,v 1.15 2005/03/04 10:01:36 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolConfiguration;
import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.RMIConfiguration;
import org.objectweb.carol.util.configuration.RMIConfigurationException;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * class <code>JeremiePRODelegate</code> for the mapping between Jeremie
 * UnicastRemoteObject and PortableRemoteObject
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
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
    private static String className = "org.objectweb.jeremie.binding.moa.UnicastRemoteObject";

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
     */
    public JeremiePRODelegate() throws Exception {
        // class for name
        unicastClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        try {
            RMIConfiguration rmiConfig = CarolConfiguration.getRMIConfiguration("jeremie");
            String propertyName = CarolDefaultValues.SERVER_JEREMIE_PORT;
            Properties p = rmiConfig.getConfigProperties();
            if (p != null) {
                this.port = PortNumber.strToint(p.getProperty(propertyName, "0"), propertyName);
            }
        } catch (RMIConfigurationException rmice) {
            TraceCarol.error("Could not get current carol configuration, rmi port will use random port.");
            this.port = 0;
        }
    }

    /**
     * Export a Remote Object
     * @param Remote object to export
     * @exception RemoteException exporting remote object problem
     */
    public void exportObject(Remote obj) throws RemoteException {
        if (!containsJeremieStub(obj)) {
            return;
        }
        try {
            Method exportO = unicastClass.getMethod("exportObject", new Class[] {Remote.class, Integer.TYPE});
            exportO.invoke(unicastClass, (new Object[] {obj, new Integer(port)}));
        } catch (Exception e) {
            throw new RemoteException(e.toString(), e);
        }
    }

    /**
     * Method for unexport object
     * @param Remote obj object to unexport
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        if (!containsJeremieStub(obj)) {
            return;
        }
        try {
            Method unexportO = unicastClass.getMethod("unexportObject", new Class[] {Remote.class, Boolean.TYPE});
            unexportO.invoke(unicastClass, (new Object[] {obj, Boolean.TRUE}));
        } catch (Exception e) {
            throw new NoSuchObjectException(e.toString());
        }
    }

    /**
     * Connection method
     * @param target a remote object;
     * @param source another remote object;
     * @exception RemoteException if the connection fail
     */
    public void connect(Remote target, Remote source) throws RemoteException {
        // do nothing
    }

    /**
     * Narrow method
     * @param Remote obj the object to narrow
     * @param Class newClass the expected type of the result
     * @return an object of type newClass
     * @exception ClassCastException if the obj class is not compatible with a
     *            newClass cast
     */
    public Object narrow(Object obj, Class newClass) throws ClassCastException {
        if (newClass.isAssignableFrom(obj.getClass())) {
            return obj;
        } else {
            throw new ClassCastException("Can't cast " + obj.getClass().getName() + " in " + newClass.getName());
        }
    }

    /**
     * To stub method
     * @return the stub object
     * @param Remote object to unexport
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
        try {
            Method exportO = unicastClass.getMethod("toStub", new Class[] {Remote.class});
            return (Remote) exportO.invoke(unicastClass, (new Object[] {obj}));
        } catch (Exception e) {
            throw new NoSuchObjectException(e.toString());
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
            nbProtocols = CarolCurrentConfiguration.getCurrent().getPortableRemoteObjectHashtable().size();
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