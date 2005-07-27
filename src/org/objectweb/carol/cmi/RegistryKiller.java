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
 * $Id: RegistryKiller.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.NoSuchObjectException;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Handle returned when starting a registry. Use to kill it.
 * @author Simon Nieuviarts
 */
public final class RegistryKiller {
    /**
     * Stub, to unexport this registry
     */
    private RegistryImpl impl;

    /**
     * Port on which the registry has been exported.
     */
    private int port;

    /**
     * Constructor
     * @param impl the registry that will be killed by this killer.
     * @param port the port it is exported on.
     */
    RegistryKiller(RegistryImpl impl, int port) {
        this.impl = impl;
        this.port = port;
    }

    /**
     * Stop the registry. After the call, this object is useless.
     * @throws NoSuchObjectException if the registry has not been exported.
     */
    public synchronized void stop() throws NoSuchObjectException {
        if (TraceCarol.isDebugCmiRegistry()) {
            TraceCarol.debugCmiRegistry("killer is stopping registry on port " + port);
        }
        LowerOrb.unexportObject(impl);
        impl = null;
    }
}
