/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
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
 * $Id: RegistryInternal.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface Registry Internal
 * @author Simon Nieuvarts
 */
interface RegistryInternal extends Remote {

    /**
     * Test method
     * @throws RemoteException if exception is encountered
     */
    public void test() throws RemoteException;

    /**
     * Get the entries list
     * @return entries
     * @throws RemoteException if Exception is encountered
     */
    public String[] list() throws RemoteException;

    /**
     * Retrieve an object by a name
     * @param name name to search
     * @return object associated
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
    public Object lookup(String name)
        throws NotBoundException, RemoteException;

    /**
     * Bind a single entry
     * @param name name
     * @param obj object
     * @throws AlreadyBoundException if the entry is already bound
     * @throws RemoteException if an exception is encountered
     */
    public void bindSingle(String name, Remote obj)
        throws AlreadyBoundException, RemoteException;

    /**
     * Rebind a single entry
     * @param name name
     * @param obj object
     * @throws RemoteException if an exception is encountered
     */
    public void rebindSingle(String name, Remote obj) throws RemoteException;

    /**
     * Bind a cluster entry
     * @param name cluster name
     * @param obj array of objects
     * @throws AlreadyBoundException if the entry is already bound
     * @throws RemoteException if an exception is encountered
     */
    public void bindCluster(String name, byte[] obj)
        throws AlreadyBoundException, RemoteException;

    /**
     * Rebind a cluster entry
     * @param name cluster name
     * @param obj array of objects
     * @throws RemoteException if an exception is encountered
     */
    public void rebindCluster(String name, byte[] obj) throws RemoteException;

    /**
     * Unbind an entry
     * @param name entry to unregister
     * @throws NotBoundException if entry is not found
     * @throws RemoteException if an exception is encountered
     */
    public void unbind(String name) throws NotBoundException, RemoteException;
}
