/**
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
 * $Id: RegistryManager.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author riviereg Remote interface to Manage Registry
 */
public interface RegistryManager extends Remote {

    /**
     * @author riviereg Stop Method
     */
    public void stop() throws RemoteException;

    /**
     * @author Guillaume Riviere Set the Flag Verbose
     */
    public void setVerbose(boolean verbose) throws RemoteException;

    /**
     * @author Guillaume Riviere
     */
    public void ping() throws RemoteException;

    /**
     * @author Guillaume Riviere purge the registry
     */
    public void purge() throws RemoteException;

    /**
     * @author Guillaume Riviere purge the registry
     */
    public String[] list() throws RemoteException;

    // write firewall methods
    /**
     * Allow everybody write
     */
    public void allowWriteAll() throws RemoteException;

    /**
     * Forbid everybody write
     */
    public void forbidWriteAll() throws RemoteException;

    /**
     * add a write forbiden address
     * @param i
     */
    public void addWriteForbidenAddress(InetAddress i) throws RemoteException;

    /**
     * remove a write forbiden adress
     * @param i
     */
    public void addWriteAllowAddress(InetAddress i) throws RemoteException;

    /**
     * list write forbiden adress
     * @return
     */
    public InetAddress[] listWriteForbidenAddress() throws RemoteException;

    /**
     * list write Allowed Adress
     * @return
     */
    public InetAddress[] listWriteAllowedAddress() throws RemoteException;

    /**
     * is allow for all writer
     * @return
     */
    public boolean isWriteAllowAll() throws RemoteException;

    /**
     * Tets if a InetAdress is allow for writting
     * @param i
     * @return
     */
    public boolean isWriteAllow(InetAddress i) throws RemoteException;

    // read firewall method
    /**
     * Allow everybody read
     */
    public void allowReadAll() throws RemoteException;

    /**
     * Forbid everybody read
     */
    public void forbidReadAll() throws RemoteException;

    /**
     * add a read forbiden address
     * @param i
     */
    public void addReadForbidenAddress(InetAddress i) throws RemoteException;

    /**
     * remove a read forbiden adress
     * @param i
     */
    public void addReadAllowAddress(InetAddress i) throws RemoteException;

    /**
     * list read forbiden adress
     * @return
     */
    public InetAddress[] listReadForbidenAddress() throws RemoteException;

    /**
     * list read Allowed Adress
     * @return
     */
    public InetAddress[] listReadAllowedAddress() throws RemoteException;

    /**
     * is read allow for all
     * @return
     */
    public boolean isReadAllowAll() throws RemoteException;

    /**
     * Tets if a InetAdress is allow to read
     * @param i
     * @return
     */
    public boolean isReadAllow(InetAddress i) throws RemoteException;
}