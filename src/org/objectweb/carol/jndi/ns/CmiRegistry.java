/**
 * IIOPCosNaming.java 1.0 02/07/15 Copyright (C) 2002 - INRIA
 * * Copyright (C) 2003 - Simon Nieuviarts
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
 */
package org.objectweb.carol.jndi.ns;

import org.objectweb.carol.cmi.Registry;
import org.objectweb.carol.cmi.RegistryImpl;
import org.objectweb.carol.cmi.RegistryKiller;
import org.objectweb.carol.cmi.DistributedEquiv;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code>CmiRegistry</code>
 * @author Simon Nieuviarts (Simon.Nieuviarts@inrialpes.fr)
 * @author Florent Benoit (Refactoring)
 */
public class CmiRegistry extends AbsRegistry implements NameService {

    /**
     * Cluster equivalence system
     */
    private DistributedEquiv de = null;

    /**
     * To kill the registry server
     */
    private RegistryKiller cregk = null;

    /**
     * Default constructor
     */
    public CmiRegistry() {
        super(Registry.DEFAULT_PORT);
    }


    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occure
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("CmiRegistry.start() on port:" + getPort());
        }
        try {
            if (!isStarted()) {
                if (getPort() >= 0) {
                    de = DistributedEquiv.start();
                    cregk = RegistryImpl.start(getPort());
                    // add a shudown hook for this process
                    Runtime.getRuntime().addShutdownHook(new Thread() {

                        public void run() {
                            try {
                                CmiRegistry.this.stop();
                            } catch (Exception e) {
                                TraceCarol.error("CmiRegistry ShutdownHook problem", e);
                            }
                        }
                    });
                } else {
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol("Can't start CmiRegistry, port=" + getPort() + " is < 0");
                    }
                }
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("CmiRegistry is already start on port:" + getPort());
                }
            }
        } catch (Exception e) {
            String msg = "can not start cluster registry: " + e;
            TraceCarol.error(msg);
            throw new NameServiceException(msg);
        }
    }

    /**
     * stop Method, Stop a NameService or do nothing if the name service is
     * already stop
     * @throws NameServiceException if a problem occure
     */
    public void stop() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("CmiRegistry.stop()");
        }
        try {
            if (cregk != null) {
                cregk.stop();
                de.stop();
                cregk = null;
            }
        } catch (Exception e) {
            throw new NameServiceException("can not stop cluster registry: " + e);
        }
    }

    /**
     * isStarted Method, check if a name service is started
     * @return boolean true if the name service is started
     */
    public boolean isStarted() {
        if (cregk != null) {
            return true;
        }
        return false;
    }

}