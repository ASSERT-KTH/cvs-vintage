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
 * $Id: MultiPRODelegate.java,v 1.14 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.util.configuration.ConfigurationRepository;
import org.objectweb.carol.util.configuration.ProtocolConfiguration;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> MultiPRODelegate </code><br>
 * This is a proxy for multi orb portable remote object delegate reference this
 * class with the systeme property : java
 * -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.objectweb.carol.rmi.multi.MultiPRODelegate
 * ... for the moment this class is only for one orb
 * @author Guillaume Riviere
 * @author Florent Benoit (Refactoring : this class has no static methods,
 *         remove all static methods and init is done only once in constructor)
 */
public class MultiPRODelegate implements PortableRemoteObjectDelegate {

    /**
     * exported HashTable
     */
    private Hashtable exported = new Hashtable();

    /**
     * Prodelegate object available
     */
    private List proDelegates = null;

    /**
     * constructor for this PortableRemoteObjectDelegateProxy
     */
    public MultiPRODelegate() {

        // Build a PortableRemoteObjectDelegate object for each protocol configuration
        ProtocolConfiguration[] protocolConfigurations = ConfigurationRepository.getConfigurations();
        proDelegates = new ArrayList();
        for (int i = 0; i < protocolConfigurations.length; i++) {
            PortableRemoteObjectDelegate proDelegate = protocolConfigurations[i].getProtocol().getPortableRemoteObject();
            proDelegates.add(proDelegate);
        }
    }

    /**
     * Makes a server object ready to receive remote calls. Note that subclasses
     * of PortableRemoteObject do not need to call this method, as it is called
     * by the constructor.
     * @param obj the server object to export.
     * @exception RemoteException if export fails.
     */
    public void exportObject(Remote obj) throws RemoteException {
        for (Iterator it = proDelegates.iterator(); it.hasNext();) {
            ((PortableRemoteObjectDelegate) it.next()).exportObject(obj);
        }
        if (TraceCarol.isDebugExportCarol()) {
            TraceCarol.debugExportCarol("Export object " + obj.getClass().getName());
            addObject(obj.getClass().getName());
        }
    }

    /**
     * Deregisters a server object from the runtime, allowing the object to
     * become available for garbage collection.
     * @param obj the object to unexport.
     * @exception NoSuchObjectException if the remote object is not currently
     *            exported.
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
        for (Iterator it = proDelegates.iterator(); it.hasNext();) {
            ((PortableRemoteObjectDelegate) it.next()).unexportObject(obj);
        }
        if (TraceCarol.isDebugExportCarol()) {
            TraceCarol.debugExportCarol("Unexport object " + obj.getClass().getName());
            TraceCarol.debugExportCarol("UnExported objects list:\n" + getExportedObjects());
            removeObject(obj.getClass().getName());
        }
    }

    /**
     * Makes a Remote object ready for remote communication. This normally
     * happens implicitly when the object is sent or received as an argument on
     * a remote method call, but in some circumstances it is useful to perform
     * this action by making an explicit call.
     * @param target the object to connect.
     * @param source a previously connected object.
     * @throws RemoteException if <code>source</code> is not connected or if
     *         <code>target</code> is already connected to a different ORB
     *         than <code>source</code>.
     */
    public void connect(Remote target, Remote source) throws RemoteException {
        for (Iterator it = proDelegates.iterator(); it.hasNext();) {
            ((PortableRemoteObjectDelegate) it.next()).connect(target, source);
        }
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
        return ConfigurationRepository.getCurrentConfiguration().getProtocol().getPortableRemoteObject().narrow(narrowFrom, narrowTo);
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
        return ConfigurationRepository.getCurrentConfiguration().getProtocol().getPortableRemoteObject().toStub(obj);
    }

    /**
     * Used only in debug mode
     * @return list of unexported objects
     */
    private String getExportedObjects() {
        String result = "Exported Objects:\n";
        int resultInt = 0;
        for (Enumeration e = exported.keys(); e.hasMoreElements();) {
            String ck = (String) e.nextElement();
            int on = ((Integer) exported.get(ck)).intValue();
            result += "" + on + " instances of  " + ck + "\n";
            resultInt += on;
        }
        result += "Total number of exported objects=" + resultInt;
        return result;
    }

    /**
     * This method is used only in debug mode Removes an exported object
     * @param className of exported object
     */
    private void removeObject(String className) {
        if (exported.containsKey(className)) {
            if (((Integer) exported.get(className)).intValue() != 1) {
                exported.put(className, new Integer(((Integer) exported.get(className)).intValue() - 1));
            } else {
                exported.remove(className);
            }
        }
    }

    /**
     * This method is used only in debug mode Add exported object to a the list
     * @param className of exported object
     */
    private void addObject(String className) {
        if (exported.containsKey(className)) {
            exported.put(className, new Integer(((Integer) exported.get(className)).intValue() + 1));
        } else {
            exported.put(className, new Integer(1));
        }
    }

}