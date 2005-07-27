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
 * $Id: Naming.java,v 1.5 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Similar to <code>java.rmi.Naming</code> class.
 *
 * @author Simon Nieuviarts
 */
public final class Naming {
    /**
     * @see java.rmi.Naming
     *
     */
    private Naming() {
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static Registry getRegistry(NamingContextHostPort[] hp)
        throws MalformedURLException, RemoteException {
        if (hp.length < 1) {
            throw new MalformedURLException("No host specified !");
        }
        return new RegistryClient(RegistryStubList.getClusterStub(hp));
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static Registry getLocalRegistry(NamingContextHostPort[] hp)
        throws MalformedURLException, RemoteException {
        if (hp.length != 1) {
            throw new MalformedURLException("Must bind or unbind in one host (only)");
        }
        Registry creg = getRegistry(hp);
        return creg;
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static Object lookup(String name)
        throws MalformedURLException, NotBoundException, RemoteException {
        NamingContext nc = new NamingContext(name);
        Registry reg = getRegistry(nc.hp);
        if (nc.name.length() == 0) {
            return reg;
        }
        return reg.lookup(nc.name);
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static void bind(String name, Remote obj)
        throws MalformedURLException, AlreadyBoundException, RemoteException {
        NamingContext nc = new NamingContext(name);
        Registry reg = getLocalRegistry(nc.hp);
        if (obj == null) {
            throw new NullPointerException("cannot bind null object");
        }
        reg.bind(nc.name, obj);
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static void rebind(String name, Remote obj)
        throws MalformedURLException, RemoteException {
        NamingContext nc = new NamingContext(name);
        Registry reg = getLocalRegistry(nc.hp);
        if (obj == null) {
            throw new NullPointerException("cannot bind null object");
        }
        reg.rebind(nc.name, obj);
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static void unbind(String name)
        throws MalformedURLException, NotBoundException, RemoteException {
        NamingContext nc = new NamingContext(name);
        Registry reg = getLocalRegistry(nc.hp);
        reg.unbind(nc.name);
    }

    /**
     * @see java.rmi.Naming
     *
     */
    public static String[] list(String name)
        throws MalformedURLException, RemoteException {
        NamingContext nc = new NamingContext(name);
        Registry reg = getRegistry(nc.hp);

        String prefix = nc.scheme.equals("") ? "" : nc.scheme + ":";
        prefix += "//";
        int i = 0;
        while (i < nc.hp.length) {
            prefix += nc.hp[i].host;
            if (nc.hp[i].port != LowerOrb.DEFAULT_CREG_PORT) {
                prefix += ":" + nc.hp[i].port;
            }
            i++;
            if (i < nc.hp.length) {
                prefix += ",";
            }
        }
        prefix += "/";

        String lst[] = reg.list();
        for (i = 0; i < lst.length; i++) {
            lst[i] = prefix + lst[i];
        }
        return lst;
    }
}
