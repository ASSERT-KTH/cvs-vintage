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

import java.rmi.NoSuchObjectException;

/**
 * Handle returned when starting a registry. Use to kill it.
 * @author Simon Nieuviarts
 */
public final class ClusterRegistryKiller {
    /**
     * Stub, to unexport this registry
     */
    private ClusterRegistryImpl impl;

    /**
     * Port on which the registry has been exported.
     */
    private int port;

    /**
     * @param impl the registry that will be killed by this killer.
     * @param port the port it is exported on.
     */
    ClusterRegistryKiller(
        ClusterRegistryImpl impl,
        int port) {
        this.impl = impl;
        this.port = port;
    }

    /**
     * Stop the registry. After the call, this object is useless.
     * @throws NoSuchObjectException if the registry has not been exported.
     */
    public synchronized void stop() throws NoSuchObjectException {
        if (Trace.CREG) {
            Trace.out("CREG: killer: stopping registry on port " + port);
        }
        LowerOrb.unexportObject(impl);
        impl = null;
    }
}
