/**
 * Copyright (C) 2005-2006 - Bull S.A.
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
 * $Id: RMIManageableSocketFactory.java,v 1.3 2006/03/09 07:31:28 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Socket factory allowing to :
 *    - use a fixed port instead of a random port (when port is 0).
 *        (This is useful for firewall issues)
 *    - Bind on a specific IP address
 *    - Supporting multiprotocol with a multi entries socket cache
 * @author Florent Benoit
 */
public class RMIManageableSocketFactory extends RMISocketFactory {

    /**
     * Rmi socket factory instance
     */
    private static RMISocketFactory factory = null;

    /**
     * Server socket used for exported objects [one entry per protocol]
     */
    private Map exportedObjectfixedSocket = new HashMap();

    /**
     * port number for exporting Objects [one entry per protocol]
     */
    private Map exportedObjectsPort = new HashMap();

    /**
     * Get the protocol from the port number
     */
    private Map mapPortProtocol = new HashMap();

    /**
     * InetAddress used for bind on a specific IP [one entry per protocol]
     */
    private Map inetAddress = new HashMap();

    /**
     * Constructor
     * @param port exported objects port number
     * @param inetAddress ip to use for the bind (instead of all),
     *                    if null take default 0.0.0.0 listener
     */
    private RMIManageableSocketFactory(int port, InetAddress inetAddress) {
        super();
    }

    /**
     * Create a server socket on the specified port (port 0 indicates an
     * anonymous port).
     * @param port the port number
     * @return the server socket on the specified port
     * @exception IOException if an I/O error occurs during server socket
     *            creation
     * @see java.rmi.server.RMISocketFactory#createServerSocket(int)
     */
    public ServerSocket createServerSocket(int port) throws IOException {

        // get the protocol from the port number
        String protocol = (String) this.mapPortProtocol.get(Integer.toString(port));

        // the Socket cache is indexed thru the key <protocol><port>
        String key = null;
        if (protocol != null && port > 0) {
            key = protocol + port;
            ServerSocket expss = (ServerSocket) exportedObjectfixedSocket.get(key);
            if (expss != null) {
                return expss;
            }
        }

        ServerSocket ss = null;

        if (inetAddress.get(protocol) == null) {
            ss = new ServerSocket(port);
        } else {
            // 0 value will be replaced by a default value
            ss = new ServerSocket(port, 0, (InetAddress) inetAddress.get(protocol));
        }
        String strExpPort = (String) exportedObjectsPort.get(protocol);
        int expPort = 0;
        if (strExpPort != null) {
            expPort = Integer.parseInt(strExpPort);
        }
        // Keep the socket in the cache for the exported object port
        if (protocol != null && port > 0 && expPort == port) {
            exportedObjectfixedSocket.put(key, ss);
        }
        return ss;
    }

    /**
     * Creates a client socket connected to the specified host and port.
     * @param host the host name
     * @param port the port number
     * @return a socket connected to the specified host and port.
     * @exception IOException if an I/O error occurs during socket creation
     * @see java.rmi.server.RMISocketFactory#createSocket(java.lang.String, int)
     */
    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port);
    }

    /**
     * Register the factory
     * @return the factory which was created
     * @param port given port number for registry
     * @param objectPort given port number for exporting objects
     * @param inetAddress ip to use for the bind (instead of all),
     *                    if null take default 0.0.0.0 listener
     * @param protocol protocol associated with the [port, inetAddress]
     * @throws RemoteException if the registration is not possible
     */
    public static RMISocketFactory register(int port, int objectPort, InetAddress inetAddress, String protocol) throws RemoteException {
        // The factory is a singleton and supports multiprotocol by being able to register
        // parameters for each protocol
        if (factory == null) {
            factory = new RMIManageableSocketFactory(port, inetAddress);
            // Registring as default socket factory
            try {
                RMISocketFactory.setSocketFactory(factory);

            } catch (IOException ioe) {
                throw new RemoteException("Cannot set the default registry factory :", ioe);
            }
        }
        ((RMIManageableSocketFactory) factory).setPort(protocol, port, objectPort);
        ((RMIManageableSocketFactory) factory).setInetAddress(protocol, inetAddress);
        return factory;
    }

    /**
     * Get the factory instance, null if not created
     * @return the factory
     */
    public static RMISocketFactory getFactory() {
        return factory;
    }

    /**
     * Set the port for exporting the object
     * @param protocol protocol under RMI
     * @param port port number for the registry
     * @param objectPort port number for exporting object
     * @throws RemoteException if the port is already set
     */
    private void setPort(String protocol, int  port, int objectPort) throws RemoteException  {
        if (this.exportedObjectsPort.get(protocol) != null) {
            throw new RemoteException("Port already set for the protocol : " + objectPort + " " + protocol);
        }
        this.exportedObjectsPort.put(protocol, Integer.toString(objectPort));
        if (port > 0) {
            this.mapPortProtocol.put(Integer.toString(port), protocol);
        }
        if (objectPort > 0) {
            this.mapPortProtocol.put(Integer.toString(objectPort), protocol);
        }
    }

    /**
     * Set the InetAddress for exporting the object
     * @param protocol protocol under RMI
     * @param inetAddress inetAddress
     * @throws RemoteException if the inetAddress is already set
     */
    private void setInetAddress(String protocol, InetAddress inetAddress) throws RemoteException  {
        if (this.inetAddress.get(protocol) != null) {
            throw new RemoteException("Address already set for the protocol : " + inetAddress + " " + protocol);
        }
        this.inetAddress.put(protocol, inetAddress);
    }



}
