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
 * $Id: BasicServer.java,v 1.8 2005/02/08 10:03:48 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.basic.server;

import org.objectweb.carol.util.configuration.RMIConfigurationException;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Arrays;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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

    private Context ic;

    private BasicObjectItf ba;

    private BasicMultiObjectItf bma;

    private BasicObjectRef bref;

    private final int port;

    private boolean startedSuccessfully;

    /**
     * This method binds all the names in the registry.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("expected the port number, but got: " + Arrays.asList(args));
        }
        new BasicServer(Integer.parseInt(args[0])).advertiseReadiness();
    }

    private BasicServer(int port) {
        this.port = port;
        startedSuccessfully = true;

        try {
            org.objectweb.carol.util.configuration.CarolConfiguration.init();
        } catch (RMIConfigurationException ex) {
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