/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
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
 * Initial developer: Florent BENOIT
 * --------------------------------------------------------------------------
 * $Id: JacORBCosNaming.java,v 1.2 2005/02/08 09:45:46 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Allow to start the nameservice of JacORB within Carol
 * @author Florent Benoit
 */
public class JacORBCosNaming implements NameService {

    /**
     * Sleep time to wait
     */
    private static final int SLEEP_TIME = 2000;

    /**
     * Default port
     */
    private static final int DEFAULT_PORT_NUMBER = 38693;

    /**
     * JacORB properties file
     */
    private static final String JACORB_PROPERTIES_FILE = "jacorb.properties";

    /**
     * SSL property
     */
    private static final String SSL_PROPERTY = "jacorb.security.support_ssl";

    /**
     * port number ( 38693 as default)
     */
    private int port = DEFAULT_PORT_NUMBER;

    /**
     * registry is started ?
     */
    private boolean started = false;

    /**
     * SSL mode ?
     */
    private boolean isSsl = false;

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

        // Try to see if ssl should be on or off (read classpath
        // jacorb.properties)
        try {
            InputStream fInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    JACORB_PROPERTIES_FILE);
            if (fInputStream != null) {
                Properties jacorbProps = new Properties();
                jacorbProps.load(fInputStream);
                String sslProp = jacorbProps.getProperty(SSL_PROPERTY);
                if (sslProp.equalsIgnoreCase("on")) {
                    isSsl = true;
                }
            }

        } catch (IOException ioe) {
            if (TraceCarol.isDebugJndiCarol()) {
                TraceCarol.debugJndiCarol("Cannot load '" + JACORB_PROPERTIES_FILE + "' file from classpath :"
                        + ioe.getMessage());
            }
        }

        try {
            if (!isRemoteNameServiceStarted()) {
                // start the registry
                String jvmProperties = "-Djava.endorsed.dirs=" + System.getProperty("java.endorsed.dirs") + " "
                        + "-Djacorb.orb.print_version=on " + "-Djacorb.log.default.verbosity=4 "
                        + "-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB "
                        + "-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton " + "-DOAPort=";

                if (isSsl) {
                    jvmProperties += Integer.toString(port);
                    jvmProperties += " -DORBInitRef.NameService=corbaloc:iiop:localhost" + Integer.toString(port + 1) + "/NameService";
                    jvmProperties += " -DOASSLPort=" + Integer.toString(port + 1) + " ";
                } else {
                    jvmProperties += Integer.toString(port);
                    jvmProperties += " -DORBInitRef.NameService=corbaloc:iiop:localhost:" + Integer.toString(port) + "/NameService ";
                }

                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("Launching NS with JVM properties: '" + jvmProperties + "'");
                }

                // Launch JVM
                jacORBNameServerProcess = Runtime.getRuntime().exec(System.getProperty("java.home") + File.separator + "bin" + File.separator
                        + "java " + jvmProperties + "org.jacorb.naming.NameServer");
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
        prop.put("java.naming.factory.initial", "com.sun.jndi.cosnaming.CNCtxFactory");
        prop.put("java.naming.provider.url", "corbaloc:iiop:localhost:" + Integer.toString(port)
                + "/StandardNS/NameServer-POA/_root");

        if (orb == null) {
            //Properties p = new Properties();
            //p.put("jacorb.log.default.verbosity", "4");
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
}