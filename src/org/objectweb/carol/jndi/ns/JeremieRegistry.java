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
 * $Id: JeremieRegistry.java,v 1.8 2005/03/03 16:10:32 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Properties;

import org.objectweb.jeremie.binding.moa.UnicastRemoteObject;
import org.objectweb.jeremie.services.registry.LocateRegistry;

import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> JeremieRegistry </code>
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/01/2003
 */
public class JeremieRegistry implements NameService {

    /**
     * Hostname to use
     */
    private String host = null;

    /**
     * port number ( 12340 for default)
     */
    public int port = 12340;

    /**
     * registry
     */
    public Registry registry = null;

    /**
     * Configuration properties (of carol.properties)
     */
    private Properties configurationProperties = null;

    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @param int port is port number
     * @throws NameServiceException if a problem occure
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("JeremieRegistry.start() on port:" + port);
        }

        // Fix jeremie port if running inside a server
        if (System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
            if (configurationProperties != null) {
                String propertyName = CarolDefaultValues.SERVER_JEREMIE_PORT;
                int jeremiePort = PortNumber.strToint(configurationProperties.getProperty(propertyName, "0"), propertyName);
                if (jeremiePort > 0) {
                    TraceCarol.infoCarol("Using Jeremie fixed server port number '" + jeremiePort + "'.");
                    System.setProperty("org.objectweb.jeremie.stub_factories.defaultport", String.valueOf(jeremiePort));
                }
            } else {
                TraceCarol.debugCarol("No properties '" + CarolDefaultValues.SERVER_IIOP_PORT + "' defined in carol.properties file.");
            }
        }





        try {
            if (!isStarted()) {
                if (port >= 0) {
                    registry = LocateRegistry.createRegistry(port);
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
                        TraceCarol.debugJndiCarol("Can't start JeremieRegistry, port=" + port + " is < 0");
                    }
                }

            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("JeremieRegistry is already start on port:" + port);
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
            if (registry != null) UnicastRemoteObject.unexportObject(registry, true);
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

        if (registry != null) return true;
        try {
            LocateRegistry.getRegistry(port).list();
        } catch (RemoteException re) {
            return false;
        }
        return true;
    }

    /**
     * set port method, set the port for the name service
     * @param int port number
     */
    public void setPort(int p) {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("JeremieRegistry.setPort(" + p + ")");
        }
        if (p != 0) {
            port = p;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.objectweb.carol.jndi.ns.NameService#getPort()
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the address to use for bind
     * @param host hostname/ip address
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return hostname/ip to use
     */
     public String getHost() {
         return host;
     }

     /**
      * Set the configuration properties of the protocol
      * @param p configuration properties
      */
     public void setConfigProperties(Properties p) {
         this.configurationProperties = p;
     }
}