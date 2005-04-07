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
 * $Id: BasicServer.java,v 1.10 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.basic.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.carol.util.configuration.ConfigurationException;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * Class <code>BasicServer</code> is a Server for Junit tests Test The
 * InitialContext and the PortableRemoteObject situation with remote object
 */
public class BasicServer {

    /**
     * Name of the basic remote object (in all name services)
     */
    private static String basicObjectName = "basicname";

    /**
     * Name of the basic multi remote object (in all name services)
     */
    private static String basicMultiObjectName = "basicmultiname";

    /**
     * Name of the basic object ref
     */
    private static String basicObjectRefName = "basicrefname";

    /**
     * Initial context
     */
    private Context ic = null;

    /**
     * Instance of basic object
     */
    private BasicObjectItf ba;

    /**
     * Instance of basic multi object
     */
    private BasicMultiObjectItf bma;

    /**
     * Instance of basic object ref
     */
    private BasicObjectRef bref;

    /**
     * Port number
     */
    private int port;

    /**
     * Start was successful ?
     */
    private boolean startedSuccessfully;

    /**
     * This method binds all the names in the registry.
     * @param args arguments for launching the test
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("expected the port number, but got: " + Arrays.asList(args));
        }
        new BasicServer(Integer.parseInt(args[0])).advertiseReadiness();
    }

    /**
     * Constructor
     * @param port port number to use
     */
    private BasicServer(int port) {
        this.port = port;
        startedSuccessfully = true;

        try {
            ConfigurationRepository.init();
        } catch (ConfigurationException ex) {
            System.err.println("carol is misconfigured");
            ex.printStackTrace();
            startedSuccessfully = false;
        }

        if (startedSuccessfully) {
            try {
                ba = new BasicObject();
                bma = new BasicMultiObject();
                bref = new BasicObjectRef("string");
            } catch (Exception ex) {
                System.err.println("error creating basic objects");
                ex.printStackTrace();
                startedSuccessfully = false;
            }
        }
        if (startedSuccessfully) {
            try {
                ic = new InitialContext();
                ic.rebind(basicObjectName, ba);
                ic.rebind(basicMultiObjectName, bma);
                ic.rebind(basicObjectRefName, bref);
            } catch (NamingException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Advertise readiness to the external world by binding to the specified TCP
     * port.
     * <p>
     * The purpose of this method is to indicate to Ant's <wait>task that this
     * BasicServer has registered all the necessary objects in the registry and
     * it is ok for the clients to start making remote calls.
     * </p>
     */
    private void advertiseReadiness() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't bind to " + port);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException("Error accepting a connection", ex);
            }
        }
    }
}