/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClusterRegistryInternal extends Remote {
    public static final byte CLUSTERED = 1;
    public static final byte NOT_CLUSTERED = 0;
    public void test() throws RemoteException;
    public String[] list() throws RemoteException;
    public Object lookup(String name)
        throws NotBoundException, RemoteException;
    public void bindSingle(String name, Remote obj)
        throws AlreadyBoundException, RemoteException;
    public void rebindSingle(String name, Remote obj) throws RemoteException;
    public void bindCluster(String name, byte[] obj)
        throws AlreadyBoundException, RemoteException;
    public void rebindCluster(String name, byte[] obj) throws RemoteException;
    public void unbind(String name) throws NotBoundException, RemoteException;
}
