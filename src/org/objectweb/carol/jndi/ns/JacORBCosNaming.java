/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004-2005 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: JacORBCosNaming.java,v 1.5 2005/02/17 16:48:44 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.RMIConfiguration;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Allow to start the nameservice of JacORB within Carol
 * @author Florent Benoit
 */
public class JacORBCosNaming implements NameService {

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
     * port number ( 38693 as default)
     */
    private int port = DEFAULT_PORT_NUMBER;

    /**
     * Hostname to use
     */
    private String host = null;

    /**
     * registry is started ?
     */
    private boolean started = false;

    /**
     * process of JacORB
     */
    private Process jacORBNameServerProcess = null;

    /**
     * ORB instance (should be unique in the JVM)
     */
    private static ORB orb = null;

    /**
     * Configuration properties (of carol.properties)
     */
    private Properties configurationProperties = null;

    /**
     * Default constructor
     */
    public JacORBCosNaming() {

    }

    /**
     * Start a new NameService or do nothing if the name service is already
     * started
     * @throws NameServiceException if a problem occurs
     */
    public void start() throws NameServiceException {

        if (isStarted()) {
            throw new IllegalStateException("Cannot start the server as the service is already running.");
        }
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("start() on port : '" + port + "'");
        }
        String ipAddr = null;
        String hostCorbaLoc = RMIConfiguration.DEFAULT_HOST;
        // Ip of the host is not the default host (localhost)
        if (!host.equalsIgnoreCase(RMIConfiguration.DEFAULT_HOST)) {
            try {
                ipAddr = InetAddress.getByName(host).getHostAddress();
                // Set the ip which was set in carol.properties (or if localhost, listen on all interfaces).
                System.setProperty("OAIAddr" , ipAddr);
            } catch (UnknownHostException uhe) {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("Could net get ip address from host '" + host + "' : " + uhe.getMessage());
                    uhe.printStackTrace();
                }
            }
        }

        // Fix iiop port if running inside a server
        if (System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
            String iiopPortStr = null;
            if (configurationProperties != null) {
                iiopPortStr = configurationProperties.getProperty(CarolDefaultValues.SERVER_IIOP_PORT, "0");
                int iiopPort = 0;
                try {
                    iiopPort = Integer.parseInt(iiopPortStr);
                } catch (NumberFormatException nfe) {
                    TraceCarol.error("Invalid port number for property '" + CarolDefaultValues.SERVER_IIOP_PORT + "'. Value set was '" + iiopPortStr + "'. It should be 0(random) or greater than 0. Error : " + nfe.getMessage());
                }
                if (iiopPort > 0) {
                    TraceCarol.infoCarol("Using fixed IIOP server port number '" + iiopPort + "'.");
                    System.setProperty("OAPort", iiopPortStr);
                } else if (iiopPort < 0) {
                    TraceCarol.error("Invalid port number for property '" + CarolDefaultValues.SERVER_IIOP_PORT + "'. It should be 0(random) or greater than 0.");
                }
                if (iiopPort == 0) {
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugCarol("IIOP port was 0, will use a random port");
                    }
                }
            } else {
                TraceCarol.debugCarol("No properties '" + CarolDefaultValues.SERVER_IIOP_PORT + "' defined in carol.properties file.");
            }
        }


        try {
            if (!isRemoteNameServiceStarted()) {
                // start the registry
                String jvmProperties = "-Djava.endorsed.dirs=" + System.getProperty("java.endorsed.dirs") + " "
                        + "-Djacorb.orb.print_version=on " + "-Djacorb.log.default.verbosity=4 "
                        + "-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB "
                        + "-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton " + "-DOAPort=";

                jvmProperties += Integer.toString(port);
                jvmProperties += " -DORBInitRef.NameService=corbaloc:iiop:" + hostCorbaLoc + ":"
                        + Integer.toString(port) + "/NameService";

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
                if (cosError.available() != 0) {
                    byte[] b = new byte[cosError.available()];
                    cosError.read(b);
                    cosError.close();
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol("JacORBCosNaming:");
                        TraceCarol.debugJndiCarol(new String(b));
                    }
                }

                InputStream cosOut = jacORBNameServerProcess.getInputStream();
                if (cosOut.available() != 0) {
                    byte[] b = new byte[cosOut.available()];
                    cosOut.read(b);
                    cosOut.close();
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol("JacORBCosNaming:");
                        TraceCarol.debugJndiCarol(new String(b));
                    }
                }

                // add a shudown hook for this process
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    public void run() {
                        try {
                            JacORBCosNaming.this.stop();
                        } catch (Exception e) {
                            TraceCarol.error("JacORBCosNaming ShutdownHook problem", e);
                        }
                    }
                });
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("JacORBCosNaming is already start on port : '" + port + "'.");
                }
            }
        } catch (Exception e) {
            TraceCarol.error("Cannot start JacORBCosNaming for an unknown reason", e);
            throw new NameServiceException("cannot start cosnaming daemon: " + e);
        }
        started = true;
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

    }

    /**
     * isStarted Method, check if a name service is started
     * @return boolean true if the name service is started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * set port method, set the port for the name service
     * @param p port number
     */
    public void setPort(int p) {
        if (isStarted()) {
            throw new IllegalStateException("The port cannot be changed as the server is running.");
        }
        if (p <= 0) {
            throw new IllegalArgumentException(
                    "The number for the port is incorrect. It must be a value > 0. Value was '" + port + "'");
        }
        this.port = p;
    }

    /**
     * Set the address to use for bind
     * @param host hostname/ip address
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return hostname/ip to use
     */
    public String getHost() {
        return host;
    }

    /**
     * get port method, get the port for the name service
     * @return int port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Check if a remote NS was started before
     * @return true if a remote NS was started
     */
    private boolean isRemoteNameServiceStarted() {

        Properties prop = new Properties();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        prop.put(Context.PROVIDER_URL, "corbaloc:iiop:localhost:" + Integer.toString(port)
                + "/StandardNS/NameServer-POA/_root");

        if (orb == null) {
            // Properties p = new Properties();
            // p.put("jacorb.log.default.verbosity", "4");
            orb = ORB.init(new String[0], null);
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
        return orb;
    }

    /**
     * Set the configuration properties of the protocol
     * @param p configuration properties
     */
    public void setConfigProperties(Properties p) {
        this.configurationProperties = p;
    }
}