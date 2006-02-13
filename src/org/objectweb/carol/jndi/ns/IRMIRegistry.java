/**
 * Copyright (C) 2006 - INRIA
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
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
 * $Id: IRMIRegistry.java,v 1.4 2006/02/13 15:18:33 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.objectweb.carol.jndi.registry.RegistryCreator;
import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.ConfigurationRepository;
import org.objectweb.carol.util.configuration.ConfigurationUtil;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * IRMIRegistry
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class IRMIRegistry extends AbsRegistry implements NameService {

    /**
     * Default port
     */
    private static final int DEFAULT_PORT_NUMBER = 1098;

    /**
     * Instance port number (firewall)
     */
    private static int objectPort = 0;


    /**
     * InetAddress to use for creating registry (by default use all interfaces)
     */
    private InetAddress registryInetAddress = null;

    /**
     * registry
     */
    private static Registry registry = null;

    /**
     * Default constructor
     */
    public IRMIRegistry() {
        super(DEFAULT_PORT_NUMBER);
    }

    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occure
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("IRMIRegistry.start() on port:" + getPort());
        }
        try {
            // Set factory which allow to fix rmi port if defined and if running inside a server
            if (System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
                if (getConfigProperties() != null) {
                    String propertyName = CarolDefaultValues.SERVER_IRMI_PORT;
                    objectPort = PortNumber.strToint(getConfigProperties().getProperty(propertyName, "0"),
                            propertyName);

                    // Read if regstry should use a single interface
                    propertyName = CarolDefaultValues.SERVER_IRMI_SINGLE_ITF;
                    boolean useSingleItf = Boolean.valueOf(getConfigProperties().getProperty(propertyName, "false")).booleanValue();
                    if (useSingleItf) {
                        String url = getConfigProperties().getProperty(CarolDefaultValues.CAROL_PREFIX + ".irmi." + CarolDefaultValues.URL_PREFIX);
                        registryInetAddress = InetAddress.getByName(ConfigurationUtil.getHostOfUrl(url));
                    }
                } else {
                    TraceCarol.debugCarol("No properties '" + CarolDefaultValues.SERVER_IRMI_PORT
                            + "' defined in carol.properties file.");
                }
            }

            if (!isStarted()) {

                if (objectPort > 0) {
                    TraceCarol.infoCarol("Using IRMI fixed server port number '" + objectPort + "'.");
                }

                if (registryInetAddress != null) {
                    TraceCarol.infoCarol("Using Specific address to bind registry '" + registryInetAddress + "'.");
                }

                if (getPort() >= 0) {
                    String protocol = null;
                    try {
                        protocol = ConfigurationRepository.getCurrentConfiguration().getProtocol().getName();
                    } catch (Exception e) {
                        protocol = null;
                    }
                    if (protocol == null || protocol.equals("")) {
                        TraceCarol.infoCarol("Unknown protocol - setting irmi");
                        protocol = "irmi";
                    }
                    registry = RegistryCreator.createRegistry(getPort(), objectPort, registryInetAddress, protocol);
                    // add a shudown hook for this process
                    Runtime.getRuntime().addShutdownHook(new Thread() {

                        public void run() {
                            try {
                                IRMIRegistry.this.stop();
                            } catch (Exception e) {
                                TraceCarol.error("IRMIRegistry ShutdownHook problem", e);
                            }
                        }
                    });
                } else {
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol("Can't start IRMIRegistry, port=" + getPort() + " is < 0");
                    }
                }
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("IRMIRegistry is already start on port:" + getPort());
                }
            }
        } catch (Exception e) {
            throw new NameServiceException("can not start rmi registry: " + e);
        }
    }

    /**
     * stop Method, Stop a NameService or do nothing if the name service is all
     * ready stop
     * @throws NameServiceException if a problem occure
     */
    public void stop() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("IRMIRegistry.stop()");
        }
        try {
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
            }
            registry = null;
        } catch (Exception e) {
            throw new NameServiceException("can not stop rmi registry: " + e);
        }
    }

    /**
     * isStarted Method, check if a name service is local
     * @return boolean true if the name service is local
     */
    public static boolean isLocal() {
        return (registry != null);
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
            if (registryInetAddress != null) {
                LocateRegistry.getRegistry(registryInetAddress.getHostAddress(), getPort()).list();
            } else {
                LocateRegistry.getRegistry(getPort()).list();
            }
        } catch (RemoteException re) {
            return false;
        }
        return true;
    }

    /**
     * @return the registry.
     */
    public static Registry getRegistry() {
        return registry;
    }

}
