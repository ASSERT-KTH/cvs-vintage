/**
 * Copyright (C) 2004,2005 - INRIA (www.inria.fr)
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
 * $Id: RegistryCreator.java,v 1.1 2005/10/19 13:40:36 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;

/**
 * Create registry with options like fixing exported objects port number
 * @author Florent Benoit
 */
public class RegistryCreator {

    /**
     * Utility class, no public constructor
     */
    private RegistryCreator() {

    }


    /**
     * Create a new registry on given port and use exported object port given
     * @param port registry port
     * @param objectPort exported objects port
     * @param inetAddress ip to use for the bind (instead of using all interfaces)
     * @return a new Registry object
     * @throws RemoteException if registry cannot be built
     */
    public static Registry createRegistry(int port, int objectPort, InetAddress inetAddress) throws RemoteException {
        // used fixed port factory only if user want set the port
        if (objectPort > 0 || inetAddress != null) {
            RMISocketFactory socketFactory = RMIManageableSocketFactory.register(objectPort, inetAddress);
            return LocateRegistry.createRegistry(port, socketFactory, socketFactory);
        } else {
            return LocateRegistry.createRegistry(port);
        }
    }

    /**
     * Start a new Registry
     * @param args arguments for starting registry
     */
    public static void main(String[] args) {
        try {
            int regPort = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                regPort = Integer.parseInt(args[0]);
            }
            createRegistry(regPort, 0, null);
            System.out.println("Registry started on port " + regPort);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}