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
 * $Id: JeremieRegistry.java,v 1.9 2005/03/10 12:21:46 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.objectweb.jeremie.binding.moa.UnicastRemoteObject;
import org.objectweb.jeremie.services.registry.LocateRegistry;

import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> JeremieRegistry </code>
 * @author Guillaume Riviere
 * @author Florent Benoit (Refactoring)
 */
public class JeremieRegistry extends AbsRegistry implements NameService {

    /**
     * Default port
     */
    private static final int DEFAULT_PORT_NUMBER = 12340;

    /**
     * Jeremie registry
     */
    private Registry registry = null;

    /**
     * Default constructor
     */
    public JeremieRegistry() {
        super(DEFAULT_PORT_NUMBER);
    }

    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occure
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("JeremieRegistry.start() on port:" + getPort());
        }

        // Fix jeremie port if running inside a server
        if (System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
            if (getConfigProperties() != null) {
                String propertyName = CarolDefaultValues.SERVER_JEREMIE_PORT;
                int jeremiePort = PortNumber.strToint(getConfigProperties().getProperty(propertyName, "0"),
                        propertyName);
                if (jeremiePort > 0) {
                    TraceCarol.infoCarol("Using Jeremie fixed server port number '" + jeremiePort + "'.");
                    System.setProperty("org.objectweb.jeremie.stub_factories.defaultport", String.valueOf(jeremiePort));
                }
            } else {
                TraceCarol.debugCarol("No properties '" + CarolDefaultValues.SERVER_IIOP_PORT
                        + "' defined in carol.properties file.");
            }
        }

        try {
            if (!isStarted()) {
                if (getPort() >= 0) {
                    registry = LocateRegistry.createRegistry(getPort());
                    // add a shudown hook for this process
                    Runtime.getRuntime().addShutdownHook(new Thread() {

                        public void run() {
                            try {
                                JeremieRegistry.this.stop();
                            } catch (Exception e) {
                                TraceCarol.error("JeremieRegistry ShutdownHook problem", e);
                            }
                        }
                    });
                } else {
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol("Can't start JeremieRegistry, port=" + getPort() + " is < 0");
                    }
                }

            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("JeremieRegistry is already start on port:" + getPort());
                }
            }
        } catch (Exception e) {
            throw new NameServiceException("can not start jeremie registry: " + e);
        }
    }

    /**
     * stop Method, Stop a NameService or do nothing if the name service is all
     * ready stop
     * @throws NameServiceException if a problem occure
     */
    public void stop() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("JeremieRegistry.stop()");
        }
        try {
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
            }
            registry = null;
        } catch (Exception e) {
            throw new NameServiceException("can not stop jeremie registry: " + e);
        }
    }

    /**
     * isStarted Method, check if a name service is started
     * @return boolean true if the name service is started
     */
    public boolean isStarted() {
        if (registry != null) {
            return true;
        }
        try {
            LocateRegistry.getRegistry(getPort()).list();
        } catch (RemoteException re) {
            return false;
        }
        return true;
    }
}