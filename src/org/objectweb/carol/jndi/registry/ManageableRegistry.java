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
 * $Id: ManageableRegistry.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import sun.rmi.registry.RegistryImpl;

/**
 * @author riviereg Manageable Registry for Carol
 */
public class ManageableRegistry extends RegistryImpl {

    public Hashtable remoteObjectTable = new Hashtable(101);

    public static ManageableRegistry manageableRegistry;

    public static ObjID id = new ObjID(ObjID.REGISTRY_ID);

    public static RegistryFireWall writeFirewall = new RegistryFireWall();

    public static RegistryFireWall readFirewall = new RegistryFireWall();

    // The registry properties
    public static String REGISTRY_MANAGER_NAME = "carolregistryadministator";

    public Properties regProps = new Properties();

    // The registry manager
    RegistryManager manager = null;

    public static boolean verbose = false;

    /**
     *
     */
    public ManageableRegistry(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
        try {
            regProps.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("registry.properties"));
        } catch (Exception e) {
            //there is no registry propertie use defaults
        }
        setManager();
    }

    /**
     *
     */
    public ManageableRegistry(int port) throws RemoteException {
        super(port);
        try {
            regProps.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("registry.properties"));
        } catch (Exception e) {
            //there is no registry propertie use defaults
        }
        setManager();
    }

    /**
     * Create a Remote Manager Object and bind it
     */
    private void setManager() throws RemoteException {
        manager = new RegistryManagerImpl(this);
        try {
            bind(REGISTRY_MANAGER_NAME, manager);
        } catch (Exception e) {
            //nothing to do
        }
    }

    /**
     * Set verbose
     */
    public void setVerbose(boolean v) {
        System.out.println("RegistryManager.setVerbose(" + v + ")");
        verbose = v;
    }

    /**
     *
     */
    public Remote lookup(String name) throws RemoteException, NotBoundException {
        checkReadAccess(name);
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.lookup(" + name + ") from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        synchronized (remoteObjectTable) {
            Remote obj = (Remote) remoteObjectTable.get(name);
            if (obj == null) throw new NotBoundException(name);
            return obj;
        }
    }

    /**
     *
     */
    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
        checkWriteAccess();
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.bind(" + name + ", obj)" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        synchronized (remoteObjectTable) {
            Remote curr = (Remote) remoteObjectTable.get(name);
            if (curr != null) throw new AlreadyBoundException(name);
            remoteObjectTable.put(name, obj);
        }
    }

    /**
     *
     */
    public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
        checkWriteAccess();
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.unbind(" + name + ")" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        synchronized (remoteObjectTable) {
            // Do not Unbind the manager
            if (REGISTRY_MANAGER_NAME.equals(name))
                    throw new NotBoundException("Can not unbind the Registry Manager use the Stop() method");
            Remote obj = (Remote) remoteObjectTable.get(name);
            if (obj == null) throw new NotBoundException(name);
            remoteObjectTable.remove(name);
        }
    }

    /**
     *
     */
    public void rebind(String name, Remote obj) throws RemoteException, AccessException {
        checkWriteAccess();
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.rebind(" + name + ", obj)" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
        }
        remoteObjectTable.put(name, obj);
    }

    /**
     *
     */
    public String[] list() throws RemoteException {
        checkReadAccess("");
        if (verbose) {
            try {
                System.out.println("ManageableRegistry.list()" + " from client: " + getClientHost());
            } catch (ServerNotActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String[] names;
        synchronized (remoteObjectTable) {
            int i = remoteObjectTable.size();
            names = new String[i];
            Enumeration enum = remoteObjectTable.keys();
            while ((--i) >= 0)
                names[i] = (String) enum.nextElement();
        }
        return names;
    }

    public static ObjID getID() {
        return id;
    }

    public static Registry createManagableRegistry(int port) throws RemoteException {
        return new ManageableRegistry(port);
    }

    /**
     *
     */
    public void purge() {
        RegistryManager reg = (RegistryManager) remoteObjectTable.get(REGISTRY_MANAGER_NAME);
        remoteObjectTable.clear();
        remoteObjectTable.put(REGISTRY_MANAGER_NAME, reg);
    }

    /**
     *
     */
    public static void main(String args[]) {
        try {
            int regPort = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                regPort = Integer.parseInt(args[0]);
            }
            manageableRegistry = new ManageableRegistry(regPort);
            System.out.println("ManageableRegistry started on port " + regPort);
            // The registry should not exiting because of the Manager binded

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     *
     */
    public static void checkWriteAccess() throws AccessException {

        try {
            String clientHostName = getClientHost();
            InetAddress clientHost = InetAddress.getByName(clientHostName);
            if (!(writeFirewall.isAllow(clientHost))) {
                throw new AccessException("Write in registry disallowed; from " + clientHost + " host");
            }
        } catch (ServerNotActiveException ex) {
            // local jvm registry alway allow
        } catch (java.net.UnknownHostException ex) {
            throw new AccessException("Write in the registry disallowed; from unknown host");
        }
    }

    /**
     *
     */
    public static void checkReadAccess(String name) throws AccessException {

        try {
            String clientHostName = getClientHost();
            InetAddress clientHost = InetAddress.getByName(clientHostName);
            if ((REGISTRY_MANAGER_NAME.equals(name)) && (InetAddress.getLocalHost().equals(clientHost))) {
                // always allow to lookup Manager from local host
            } else {
                if (!(readFirewall.isAllow(clientHost))) {
                    throw new AccessException("Read in registry disallowed; from " + clientHost + " host");
                }
            }
        } catch (ServerNotActiveException ex) {
            // local jvm registry alway allow
        } catch (java.net.UnknownHostException ex) {
            throw new AccessException("Read in the registry disallowed; from unknown host");
        }
    }

    // write firewall methods
    /**
     * Allow everybody write
     */
    public synchronized void allowWriteAll() {
        writeFirewall.allowAll();
    }

    /**
     * Forbid everybody write
     */
    public synchronized void forbidWriteAll() {
        writeFirewall.forbidAll();
    }

    /**
     * add a write forbiden address
     * @param i
     */
    public synchronized void addWriteForbidenAddress(InetAddress i) {
        writeFirewall.addForbidenAddress(i);
    }

    /**
     * remove a write forbiden adress
     * @param i
     */
    public synchronized void addWriteAllowAddress(InetAddress i) {
        writeFirewall.addAllowedAddress(i);
    }

    /**
     * list write forbiden adress
     * @return
     */
    public InetAddress[] listWriteForbidenAddress() {
        return writeFirewall.listForbidenAddress();
    }

    /**
     * list write Allowed Adress
     * @return
     */
    public InetAddress[] listWriteAllowedAddress() {
        return writeFirewall.listAllowedAddress();
    }

    /**
     * is allow for all writer
     * @return
     */
    public boolean isWriteAllowAll() {
        return writeFirewall.isAllowAll();
    }

    /**
     * Tets if a InetAdress is allow for writting
     * @param i
     * @return
     */
    public boolean isWriteAllow(InetAddress i) {
        return writeFirewall.isAllow(i);
    }

    // read firewall method
    /**
     * Allow everybody read
     */
    public synchronized void allowReadAll() {
        readFirewall.allowAll();
    }

    /**
     * Forbid everybody read
     */
    public synchronized void forbidReadAll() {
        readFirewall.forbidAll();
    }

    /**
     * add a read forbiden address
     * @param i
     */
    public synchronized void addReadForbidenAddress(InetAddress i) {
        readFirewall.addForbidenAddress(i);
    }

    /**
     * remove a read forbiden adress
     * @param i
     */
    public synchronized void addReadAllowAddress(InetAddress i) {
        readFirewall.addAllowedAddress(i);
    }

    /**
     * list read forbiden adress
     * @return
     */
    public InetAddress[] listReadForbidenAddress() {
        return readFirewall.listForbidenAddress();
    }

    /**
     * list read Allowed Adress
     * @return
     */
    public InetAddress[] listReadAllowedAddress() {
        return readFirewall.listAllowedAddress();
    }

    /**
     * is read allow for all
     * @return
     */
    public boolean isReadAllowAll() {
        return readFirewall.isAllowAll();
    }

    /**
     * Tets if a InetAdress is allow to read
     * @param i
     * @return
     */
    public boolean isReadAllow(InetAddress i) {
        return readFirewall.isAllow(i);
    }
}