/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: NameServiceManager.java,v 1.11 2005/04/07 15:07:07 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.carol.util.configuration.ConfigurationRepository;
import org.objectweb.carol.util.configuration.Protocol;
import org.objectweb.carol.util.configuration.ProtocolConfiguration;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> NameServicemanager </code> is the CAROL Name Service manager.
 * This is the carol API for Nme services management
 * @author Guillaume Riviere
 */
public class NameServiceManager {

    /**
     * Default sleep value
     */
    private static final int SLEEP_VALUE = 10000;

    /**
     * Name Service Hashtable
     */
    private static Hashtable nsTable;

    /**
     * private constructor for singleton
     */
    private static NameServiceManager current = new NameServiceManager();

    /**
     * private constructor for unicicity
     */
    private NameServiceManager() {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.NameServiceManager()");
        }

        // List of current name server
        nsTable = new Hashtable();
        ProtocolConfiguration[] protocolConfigurations = ConfigurationRepository.getConfigurations();
        for (int c = 0; c < protocolConfigurations.length; c++) {
            ProtocolConfiguration protocolConfiguration = protocolConfigurations[c];
            String configurationName = protocolConfiguration.getName();
            Protocol protocol = protocolConfiguration.getProtocol();
            NameService nsC = null;
            try {
                nsC = (NameService) Class.forName(protocol.getRegistryClassName()).newInstance();
            } catch (Exception e) {
                String msg = "Cannot instantiate registry class '" + protocol.getRegistryClassName() + "'";
                TraceCarol.error(msg, e);
            }
            nsC.setPort(protocolConfiguration.getPort());
            nsC.setHost(protocolConfiguration.getHost());
            nsC.setConfigProperties(protocolConfiguration.getProperties());
            // Add the NameService in the list of configurations
            nsTable.put(configurationName, nsC);
        }

    }

    /**
     * Method getCurrent
     * @return NameServiceManager return the current
     */
    public static NameServiceManager getNSManagerCurrent() {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.getNSManagerCurrent()");
        }
        return current;
    }

    /**
     * Start all names service
     * @throws NameServiceException if one of the name services is already start
     */
    public static void startNS() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.startNS()");
        }
        // test if one of the ns is allready started
        for (Enumeration e = nsTable.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            NameService currentNS = (NameService) nsTable.get(k);
            if (currentNS.isStarted()) {
                throw new NameServiceException("The " + k + " name service is already started");
            }
        }
        // Start all name services
        startNonStartedNS();
    }

    /**
     * Start all non-started names service
     */
    public static void startNonStartedNS() {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.startNonStartedNS()");
        }
        // start name services
        for (Enumeration e = nsTable.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            NameService currentNS = (NameService) nsTable.get(k);

            try {
                currentNS.start();
                if (TraceCarol.isInfoCarol()) {
                    TraceCarol.infoCarol("Name service for " + k + " is started on port " + currentNS.getPort());
                }
            } catch (NameServiceException nse) {
                // do nothing, just trace
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol
                            .debugJndiCarol("NameServiceManager.startNonStartedNS() can not start name service: " + k);
                }
            }
        }
    }

    /**
     * Stop all name services
     * @throws NameServiceException if an exception occure at stoping time
     */
    public static void stopNS() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.stopNS()");
        }
        // stop name services
        for (Enumeration e = nsTable.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            NameService currentNS = (NameService) nsTable.get(k);
            currentNS.stop();
        }
    }

    /**
     * Main function: start all registry and wait for control C function
     * @param args arguments
     */
    public static void main(String[] args) {

        // configure logging
        TraceCarol.configure();

        try {
            NameServiceManager.startNonStartedNS();
            // add a shudown hook for this process
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        NameServiceManager.stopNS();
                    } catch (Exception e) {
                        TraceCarol.error("Carol Naming ShutdownHook problem", e);
                    }
                }
            });
            while (true) {
                Thread.sleep(SLEEP_VALUE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}