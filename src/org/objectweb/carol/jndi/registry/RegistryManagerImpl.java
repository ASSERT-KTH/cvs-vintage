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
 * $Id: RegistryManagerImpl.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author riviereg To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RegistryManagerImpl extends UnicastRemoteObject implements RegistryManager {

    private ManageableRegistry registry = null;

    /**
     * @throws RemoteException
     */
    protected RegistryManagerImpl() throws RemoteException {
        super();
    }

    /**
     * @throws RemoteException
     */
    protected RegistryManagerImpl(ManageableRegistry reg) throws RemoteException {
        super();
        registry = reg;
    }

    /*
     * (non-Javadoc)
     * @see org.objectweb.carol.jndi.registry.RegistryManager#Stop()
     */
    public void stop() throws RemoteException {
        registry.purge();
        UnicastRemoteObject.unexportObject(this, true);
    }

    /*
     * (non-Javadoc)
     * @see org.objectweb.carol.jndi.registry.RegistryManager#setVerbose(boolean)
     */
    public void setVerbose(boolean verbose) throws RemoteException {
        registry.setVerbose(verbose);
    }

    /*
     * just a ping method
     */
    public void ping() throws RemoteException {
    }

    /**
     * @author Guillaume Riviere purge the registry
     */
    public void purge() throws RemoteException {
        registry.purge();
    }

    /**
     * @author Guillaume Riviere purge the registry
     */
    public String[] list() throws RemoteException {
        return registry.list();
    }

    // write firewall methods
    /**
     * Allow everybody write
     */
    public void allowWriteAll() throws RemoteException {
        registry.allowWriteAll();
    }

    /**
     * Forbid everybody write
     */
    public void forbidWriteAll() throws RemoteException {
        registry.forbidWriteAll();
    }

    /**
     * add a write forbiden address
     * @param i
     */
    public void addWriteForbidenAddress(InetAddress i) throws RemoteException {
        registry.addWriteForbidenAddress(i);
    }

    /**
     * remove a write forbiden adress
     * @param i
     */
    public void addWriteAllowAddress(InetAddress i) throws RemoteException {
        registry.addWriteAllowAddress(i);
    }

    /**
     * list write forbiden adress
     * @return
     */
    public InetAddress[] listWriteForbidenAddress() throws RemoteException {
        return registry.listWriteForbidenAddress();
    }

    /**
     * list write Allowed Adress
     * @return
     */
    public InetAddress[] listWriteAllowedAddress() throws RemoteException {
        return registry.listWriteAllowedAddress();
    }

    /**
     * is allow for all writer
     * @return
     */
    public boolean isWriteAllowAll() throws RemoteException {
        return registry.isWriteAllowAll();
    }

    /**
     * Tets if a InetAdress is allow for writting
     * @param i
     * @return
     */
    public boolean isWriteAllow(InetAddress i) throws RemoteException {
        return registry.isWriteAllow(i);
    }

    // read firewall method
    /**
     * Allow everybody read
     */
    public void allowReadAll() throws RemoteException {
        registry.allowReadAll();
    }

    /**
     * Forbid everybody read
     */
    public void forbidReadAll() throws RemoteException {
        registry.forbidReadAll();
    }

    /**
     * add a read forbiden address
     * @param i
     */
    public void addReadForbidenAddress(InetAddress i) throws RemoteException {
        registry.addReadForbidenAddress(i);
    }

    /**
     * remove a read forbiden adress
     * @param i
     */
    public void addReadAllowAddress(InetAddress i) throws RemoteException {
        registry.addReadAllowAddress(i);
    }

    /**
     * list read forbiden adress
     * @return
     */
    public InetAddress[] listReadForbidenAddress() throws RemoteException {
        return registry.listReadForbidenAddress();
    }

    /**
     * list read Allowed Adress
     * @return
     */
    public InetAddress[] listReadAllowedAddress() throws RemoteException {
        return registry.listReadAllowedAddress();
    }

    /**
     * is read allow for all
     * @return
     */
    public boolean isReadAllowAll() throws RemoteException {
        return registry.isReadAllowAll();
    }

    /**
     * Tets if a InetAdress is allow to read
     * @param i
     * @return
     */
    public boolean isReadAllow(InetAddress i) throws RemoteException {
        return registry.isReadAllow(i);
    }

}