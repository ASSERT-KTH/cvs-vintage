/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
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
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi.test;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * Server
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Server {

    public static final Collection export(String[] args) throws Exception {
        int index = 0;
        if (args[index].equals("--port")) {
            index++;
            int port = Integer.parseInt(args[index]);
            LocateRegistry.createRegistry(port);
            index++;
        }
        List result = new ArrayList();
        String registry = args[index];
        Context ctx = new InitialContext();
        for (index++; index < args.length; index++) {
            String name = args[index];
            Class remote = Class.forName(name + "Impl");
            Remote obj = (Remote) remote.newInstance();
            if (!PortableRemoteObject.class.isAssignableFrom(remote)) {
                PortableRemoteObject.exportObject(obj);
            }
            ctx.bind(registry + "/" + name, obj);
            result.add(obj);
        }
        return result;
    }

    public static final void unexport(Collection objects) throws Exception {
        for (Iterator it = objects.iterator(); it.hasNext(); ) {
            try {
                PortableRemoteObject.unexportObject((Remote) it.next());
            } catch (NoSuchObjectException e) {
                // continue
            }
        }
    }

    public static final void main(String[] args) throws Exception {
        export(args);
        System.out.println("started");
        System.out.flush();
        System.in.read();
        System.exit(0);
    }

}
