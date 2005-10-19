/**
 * Copyright (C) 2005 - Bull S.A.
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
 * $Id: RMIManageableSocketFactory.java,v 1.1 2005/10/19 13:40:36 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;

/**
 * Socket factory allowing to :
 *    - use a fixed port instead of a random port (when port is 0).
 *        (This is useful for firewall issues)
 *    - Bind on a specific IP address
 * @author Florent Benoit
 */
public class RMIManageableSocketFactory extends RMISocketFactory {

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
     * InetAddress used for bind on a specific IP
     */
    private InetAddress inetAddress = null;

    /**
     * Constructor
     * @param port exported objects port number
     * @param inetAddress ip to use for the bind (instead of all),
     *                    if null take default 0.0.0.0 listener
     */
    private RMIManageableSocketFactory(int port, InetAddress inetAddress) {
        super();
        this.exportedObjectsPort = port;
        this.inetAddress = inetAddress;
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
        ServerSocket ss = null;

        if (inetAddress == null) {
            ss = new ServerSocket(port);
        } else {
            // 0 value will be replaced by a default value
            ss = new ServerSocket(port, 0, inetAddress);
        }
        // Keep the socket for the exported object port
        if (port == exportedObjectsPort && exportedObjectsPort > 0) {
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
        return new Socket(host, port);
    }

    /**
     * Register the factory
     * @return the factory which was created
     * @param port given port number for exporting objects
     * @param inetAddress ip to use for the bind (instead of all),
     *                    if null take default 0.0.0.0 listener
     * @throws RemoteException if the registration is not possible
     */
    public static RMISocketFactory register(int port, InetAddress inetAddress) throws RemoteException {
        if (factory == null) {
            factory = new RMIManageableSocketFactory(port, inetAddress);

            // Registring as default socket factory
            try {
                RMISocketFactory.setSocketFactory(factory);

            } catch (IOException ioe) {
                throw new RemoteException("Cannot set the default registry factory :", ioe);
            }


        }
        return factory;
    }

}
