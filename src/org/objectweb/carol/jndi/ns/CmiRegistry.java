/*
 * CmiRegistry.java
 *
 * Copyright (C) 2003 - Simon Nieuviarts
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

import org.objectweb.carol.cmi.ClusterRegistry;
import org.objectweb.carol.cmi.ClusterRegistryImpl;
import org.objectweb.carol.cmi.ClusterRegistryKiller;
import org.objectweb.carol.cmi.DistributedEquiv;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code>CmiRegistry</code>
 *
 * @author  Simon Nieuviarts (Simon.Nieuviarts@inrialpes.fr)
 */
public class CmiRegistry implements NameService {

    /**
     * URL
     */
    private int port = ClusterRegistry.DEFAULT_PORT;

    /**
     * registry 
     */
    //    public Registry registry = null;

    /**
     * Cluster equivalence system
     */
    private DistributedEquiv de = null;

    /**
     * To kill the registry server
     */
    private ClusterRegistryKiller cregk = null;

    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occure 
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("CmiRegistry.start() on port:" + port);
        }
        try {
            if (!isStarted()) {
                if (port >= 0) {
                    de = DistributedEquiv.start();
                    cregk = ClusterRegistryImpl.start(port);
                    // add a shudown hook for this process
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            try {
                                CmiRegistry.this.stop();
                            } catch (Exception e) {
                                TraceCarol.error(
                                    "CmiRegistry ShutdownHook problem",
                                    e);
                            }
                        }
                    });
                } else {
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol(
                            "Can't start CmiRegistry, port="
                                + port
                                + " is < 0");
                    }
                }
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol(
                        "CmiRegistry is already start on port:" + port);
                }
            }
        } catch (Exception e) {
            throw new NameServiceException(
                "can not start cluster registry: " + e);
        }
    }

    /**
     * stop Method, Stop a NameService or do nothing if the name service
     * is already stop
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
            throw new NameServiceException(
                "can not stop cluster registry: " + e);
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
/*
        try {
            LocateRegistry.getRegistry(port).list();
        } catch (RemoteException re) {
            return false;
        }
        return true;
*/
        return false;
    }

    /**
     * set port method, set the port for the name service
     * @param int port
     */
    public void setPort(int p) {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("CmiRegistry.setPort(" + p + ")");
        }
        if (p != 0) {
            this.port = p;
        }
    }
}
