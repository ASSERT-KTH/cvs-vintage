/**
 * Copyright (C) 2004-2005 - Bull S.A.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
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
 * $Id: JacORBCosNaming.java,v 1.12 2005/04/11 13:39:40 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Allow to start the nameservice of JacORB within Carol
 * @author Florent Benoit
 */
public class JacORBCosNaming extends AbsRegistry implements NameService {

    /**
     * JacORB nameserver
     */
    private static final String JACORB_NAMESERVER_CLASS = "org.jacorb.naming.NameServer";

    /**
     * Sleep time to wait
     */
    private static final int SLEEP_TIME = 2000;

    /**
     * Default port
     */
    private static final int DEFAULT_PORT_NUMBER = 38693;

    /**
     * process of JacORB
     */
    private Process jacORBNameServerProcess = null;

    /**
     * ORB instance (should be unique in the JVM)
     */
    private static ORB orb = null;

    /**
     * Default constructor
     */
    public JacORBCosNaming() {
        super(DEFAULT_PORT_NUMBER);
    }

    /**
     * Start a new NameService or do nothing if the name service is already
     * started
     * @throws NameServiceException if a problem occurs
     */
    public void start() throws NameServiceException {

        // Don't start again
        if (isStarted()) {
            throw new IllegalStateException("Cannot start the server as the service is already running.");
        }
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("start() on port : '" + getPort() + "'");
        }
        String ipAddr = null;
        String hostCorbaLoc = CarolDefaultValues.DEFAULT_HOST;
        // Ip of the host is not the default host (localhost)
        if (!getHost().equalsIgnoreCase(CarolDefaultValues.DEFAULT_HOST)) {
            try {
                ipAddr = InetAddress.getByName(getHost()).getHostAddress();
                // Set the ip which was set in carol.properties (or if
                // localhost, listen on all interfaces).
                System.setProperty("OAIAddr", ipAddr);
            } catch (UnknownHostException uhe) {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("Could net get ip address from host '" + getHost() + "' : "
                            + uhe.getMessage());
                    uhe.printStackTrace();
                }
            }
        }

        // Fix iiop port if running inside a server
        if (System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
            if (getConfigProperties() != null) {
                String propertyName = CarolDefaultValues.SERVER_IIOP_PORT;
                int iiopPort = PortNumber.strToint(getConfigProperties().getProperty(propertyName, "0"), propertyName);
                if (iiopPort > 0) {
                    TraceCarol.infoCarol("Using IIOP fixed server port number '" + iiopPort + "'.");
                    System.setProperty("OAPort", String.valueOf(iiopPort));
                }
            } else {
                TraceCarol.debugCarol("No properties '" + CarolDefaultValues.SERVER_IIOP_PORT
                        + "' defined in carol.properties file.");
            }
        }

        // Set SSL Port
        if (System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
            if (getConfigProperties() != null) {
                String propertyName = CarolDefaultValues.SERVER_SSL_IIOP_PORT;
                int iiopSslPort = PortNumber.strToint(getConfigProperties().getProperty(propertyName,
                        String.valueOf(CarolDefaultValues.DEFAULT_SSL_PORT)), propertyName);
                if (iiopSslPort > 0) {
                    TraceCarol.debugCarol("Using SSL IIOP port number '" + iiopSslPort + "'.");
                    System.setProperty("OASSLPort", String.valueOf(iiopSslPort));
                }
            } else {
                TraceCarol.debugCarol("No properties '" + CarolDefaultValues.SERVER_SSL_IIOP_PORT
                        + "' defined in carol.properties file.");
            }
        }

        try {
            if (!isRemoteNameServiceStarted()) {
                // start the registry
                String jvmProperties = "-Djava.endorsed.dirs=" + System.getProperty("java.endorsed.dirs") + " "
                        + "-Djacorb.orb.print_version=off " + "-Djacorb.log.default.verbosity=0 "
                        + "-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB "
                        + "-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton " + "-DOAPort=";

                jvmProperties += Integer.toString(getPort());
                jvmProperties += " -DORBInitRef.NameService=corbaloc:iiop:" + hostCorbaLoc + ":"
                        + Integer.toString(getPort()) + "/NameService";

                if (ipAddr != null) {
                    jvmProperties += " -DOAIAddr=" + ipAddr;
                }

                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("Launching NS with JVM properties: '" + jvmProperties + "'");
                }

                // Launch JVM
                jacORBNameServerProcess = Runtime.getRuntime().exec(
                        System.getProperty("java.home") + File.separator + "bin" + File.separator + "java "
                                + jvmProperties + " " + JACORB_NAMESERVER_CLASS);
                // wait for starting
                Thread.sleep(SLEEP_TIME);

                // trace the start execution
                InputStream cosError = jacORBNameServerProcess.getErrorStream();
                InputStream cosOut = jacORBNameServerProcess.getInputStream();
                Thread err = new Thread(new CosReader(cosError, true));
                Thread out = new Thread(new CosReader(cosOut, false));
                out.start();
                err.start();

                // add a shudown hook for this process
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    public void run() {
                        try {
                            if (JacORBCosNaming.this.isStarted()) {
                                JacORBCosNaming.this.stop();
                            }
                        } catch (Exception e) {
                            TraceCarol.error("JacORBCosNaming ShutdownHook problem", e);
                        }
                    }
                });
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("JacORBCosNaming is already start on port : '" + getPort() + "'.");
                }
            }
        } catch (Exception e) {
            TraceCarol.error("Cannot start JacORBCosNaming for an unknown reason", e);
            throw new NameServiceException("cannot start cosnaming daemon: " + e);
        }
        setStarted();
    }

    /**
     * Stop a NameService or do nothing if the name service is already stopped
     * @throws NameServiceException if a problem occurs
     */
    public void stop() throws NameServiceException {
        if (!isStarted()) {
            throw new IllegalStateException("Cannot stop the server as the service is not running.");
        }
        try {

            if (jacORBNameServerProcess != null) {
                jacORBNameServerProcess.destroy();
            }
            jacORBNameServerProcess = null;
        } catch (Exception e) {
            TraceCarol.error("Cannot stop JacORBCosNaming for an unknown reason", e);
            throw new NameServiceException("cannot start cosnaming daemon: " + e);
        }
        resetStarted();
    }

    /**
     * Check if a remote NS was started before
     * @return true if a remote NS was started
     */
    private boolean isRemoteNameServiceStarted() {

        Properties prop = new Properties();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        prop.put(Context.PROVIDER_URL, "corbaloc:iiop:localhost:" + Integer.toString(getPort())
                + "/StandardNS/NameServer-POA/_root");

        if (orb == null) {
            initORB();
        }

        prop.put("java.naming.corba.orb", orb);

        try {
            new InitialContext(prop);
        } catch (javax.naming.CommunicationException jcm) {
            return false;
        } catch (org.omg.CORBA.TRANSIENT ct) {
            return false;
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    /**
     * @return the orb.
     */
    public static ORB getOrb() {
        if (orb == null) {
            initORB();
        }
        return orb;
    }

    /**
     * Initialize the ORB
     * @return
     */
    private static void initORB() {
        orb = ORB.init(new String[0], null);
    }

    /**
     * Allow to trace errors/output of a process
     */
    class CosReader implements Runnable {

        /**
         * Input stream containing information
         */
        private InputStream is;

        /**
         * Should send as error or debug message ?
         */
        private boolean isErrorMessage = false;

        /**
         * Constructor
         * @param is given input stream
         * @param isErrorMessage Should send as error or debug message
         */
        public CosReader(InputStream is, boolean isErrorMessage) {
            this.is = is;
            this.isErrorMessage = isErrorMessage;
        }

        /**
         * Thread execution printing information received
         */
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String str = null;
                while ((str = br.readLine()) != null) {
                    if (isErrorMessage) {
                        if (TraceCarol.isDebugJndiCarol()) {
                            TraceCarol.debugJndiCarol("JacORBCosNaming error :");
                            TraceCarol.debugJndiCarol(str);
                        }
                    } else {
                        if (TraceCarol.isDebugJndiCarol()) {
                            TraceCarol.debugJndiCarol("JacORBCosNaming:");
                            TraceCarol.debugJndiCarol(str);
                        }
                    }
                }
                // close input stream
                is.close();
            } catch (Exception e) {
                TraceCarol.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}