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
 * $Id: RMIFixedPortFirewallSocketFactory.java,v 1.1 2005/03/03 16:11:03 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;

/**
 * Socket factory allowing to use a fixed port instead of a random port (when
 * it's 0). This is useful for firewall issues.
 * @author Florent Benoit
 */
public class RMIFixedPortFirewallSocketFactory extends RMISocketFactory {

    /**
     * Rmi socket factory instance
     */
    private static RMISocketFactory factory = null;

    /**
     * Server socket used for exported objects
     */
    private static ServerSocket exportedObjectfixedSocket = null;

    /**
     * port number for exporting Objects
     */
    private int exportedObjectsPort;

    /**
     * Constructor
     * @param port exported objects port number
     */
    private RMIFixedPortFirewallSocketFactory(int port) {
        super();
        this.exportedObjectsPort = port;
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
        if (port == 0 && exportedObjectfixedSocket != null) {
            return exportedObjectfixedSocket;
        }
        ServerSocket ss = new ServerSocket(port);
        // Keep the socket for the exported object port
        if (port == exportedObjectsPort) {
            exportedObjectfixedSocket = ss;
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
        // When asking for a new random port, use the port defined by the user
        if (port == 0) {
            port = exportedObjectsPort;
        }
        return new Socket(host, port);
    }

    /**
     * Register the factory
     * @param port given port number for exporting objects
     * @throws RemoteException if the registration is not possible
     */
    public static void register(int port) throws RemoteException {
        if (factory == null) {
            factory = new RMIFixedPortFirewallSocketFactory(port);

            // Registring as default socket factory
            try {
                RMISocketFactory.setSocketFactory(factory);

            } catch (IOException ioe) {
                throw new RemoteException("Cannot set the default registry factory :", ioe);
            }
        }
    }

}
