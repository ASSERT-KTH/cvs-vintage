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
 * $Id: ManageableRegistry.java,v 1.1 2005/10/19 13:40:36 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry.jrmp;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.carol.jndi.registry.RMIManageableSocketFactory;

import sun.rmi.registry.RegistryImpl;

/**
 * JRMP Registry without checks for bind
 * This class do some invalid import on Sun classes and won't compile
 * on free JVM.
 * @author Guillaume Rivière
 */
public class ManageableRegistry extends RegistryImpl {

    /**
     * Initial capacity of the hashtable
     */
    private static final int INITIAL_CAPACITY = 101;

    /**
     * Hashtable containing objects of the registry
     */
    private Hashtable registryObjects = new Hashtable(INITIAL_CAPACITY);

    /**
     * Verbosity
     */
    private static boolean verbose = false;

    /**
     * Build a new registry on a given port
     * @param port given port number
     * @param csf client socket factory
     * @param ssf server socket factory
     * @throws RemoteException if the registry cannot be built
     */
    private ManageableRegistry(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
    }

    /**
     * Build a new registry on a given port
     * @param port given port number
     * @throws RemoteException if the registry cannot be built
     */
    private ManageableRegistry(int port) throws RemoteException {
        this(port, RMISocketFactory.getSocketFactory(), RMISocketFactory.getSocketFactory());
    }

    /**
     * Set verbosity
     * @param v true/false
     */
    public void setVerbose(boolean v) {
        System.out.println("RegistryManager.setVerbose(" + v + ")");
        verbose = v;
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws RemoteException if a naming exception is encountered
     * @throws NotBoundException if object is not bound
     */
    public Remote lookup(String name) throws RemoteException, NotBoundException {
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.lookup(" + name + ") from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        synchronized (registryObjects) {
            Remote obj = (Remote) registryObjects.get(name);
            if (obj == null) {
                throw new NotBoundException(name);
            }
            return obj;
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding. All
     * intermediate contexts and the target context (that named by all but
     * terminal atomic component of the name) must already exist.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws RemoteException if a bind cannot be done exception is encountered
     * @throws AlreadyBoundException if object is already bound
     * @throws AccessException if cannot bind
     */
    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.bind(" + name + ", obj)" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        synchronized (registryObjects) {
            Remote curr = (Remote) registryObjects.get(name);
            if (curr != null) {
                throw new AlreadyBoundException(name);
            }
            registryObjects.put(name, obj);
        }
    }

    /**
     * Unbinds the named object. Removes the terminal atomic name in
     * <code>name</code> from the target context--that named by all but the
     * terminal atomic part of <code>name</code>.
     * @param name the name to unbind; may not be empty
     * @throws RemoteException if a naming exception is encountered
     * @throws NotBoundException if object was not bound
     * @throws AccessException if unbind is not authorized
     */
    public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.unbind(" + name + ")" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        synchronized (registryObjects) {
            Remote obj = (Remote) registryObjects.get(name);
            if (obj == null) {
                throw new NotBoundException(name);
            }
            registryObjects.remove(name);
        }
    }

    /**
     * ReBinds a name to an object, overwriting any existing binding. All
     * intermediate contexts and the target context (that named by all but
     * terminal atomic component of the name) must already exist.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws RemoteException if a bind cannot be done exception is encountered
     * @throws AccessException if cannot bind
     */
    public void rebind(String name, Remote obj) throws RemoteException, AccessException {
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.rebind(" + name + ", obj)" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        registryObjects.put(name, obj);
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are not
     * included.
     * @return the names in this context.
     * @throws RemoteException if a naming exception is encountered
     */
    public String[] list() throws RemoteException {

        if (verbose) {
            try {
                System.out.println("ManageableRegistry.list()" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String[] names;
        synchronized (registryObjects) {
            int i = registryObjects.size();
            names = new String[i];
            Enumeration e = registryObjects.keys();
            while ((--i) >= 0) {
                names[i] = (String) e.nextElement();
            }
        }
        return names;
    }

    /**
     * Create a new registry on given port and use exported object port given
     * @param port registry port
     * @param objectPort exported objects port
     * @param inetAddress ip to use for the bind (instead of using all interfaces)
     * @return a new Registry object
     * @throws RemoteException if registry cannot be built
     */
    public static Registry createManagableRegistry(int port, int objectPort, InetAddress inetAddress) throws RemoteException {
        // used fixed port factory only if user want set the port
        if (objectPort > 0 || inetAddress != null) {
            RMISocketFactory socketFactory = RMIManageableSocketFactory.register(objectPort, inetAddress);
            return new ManageableRegistry(port, socketFactory, socketFactory);
        } else {
            return new ManageableRegistry(port);
        }
    }

    /**
     * Remove objects of the registry
     */
    public void purge() {
        registryObjects.clear();
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
            createManagableRegistry(regPort, 0, null);
            System.out.println("ManageableRegistry started on port " + regPort);
            // The registry should not exiting because of the Manager binded

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}