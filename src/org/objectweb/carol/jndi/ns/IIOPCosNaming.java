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
 * $Id: IIOPCosNaming.java,v 1.10 2005/02/18 08:50:15 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> IIOPCosNaming </code> Start in a separated process (see the sun
 * orbd documentation)
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @author Florent Benoit (add POA model)
 */
public class IIOPCosNaming implements NameService {

    /**
     * Default port number ( 12350 for default)
     */
    private static final int DEFAUL_PORT = 12350;

    /**
     * Sleep time to wait
     */
    private static final int SLEEP_TIME = 2000;

    /**
     * port number
     */
    private int port = DEFAUL_PORT;

    /**
     * Hostname to use
     */
    private String host = null;

    /**
     * process of the cosnaming
     */
    private Process cosNamingProcess = null;

    /**
     * Unique instance of the ORB running in the JVM
     */
    private static ORB orb = null;

    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occurs
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("IIOPCosNaming.start() on port:" + port);
        }
        try {
            if (!isStarted()) {
                // start a new orbd procees
                if (port >= 0) {
                    cosNamingProcess = Runtime.getRuntime().exec(
                            System.getProperty("java.home") + System.getProperty("file.separator") + "bin"
                                    + System.getProperty("file.separator") + "tnameserv -ORBInitialPort " + port);
                    // wait for starting
                    Thread.sleep(SLEEP_TIME);

                    // trace the start execution
                    InputStream cosError = cosNamingProcess.getErrorStream();
                    if (cosError.available() != 0) {
                        byte[] b = new byte[cosError.available()];
                        cosError.read(b);
                        cosError.close();
                        throw new NameServiceException("can not start cosnaming daemon:" + new String(b));
                    }

                    InputStream cosOut = cosNamingProcess.getInputStream();
                    if (cosOut.available() != 0) {
                        byte[] b = new byte[cosOut.available()];
                        cosOut.read(b);
                        cosOut.close();
                        if (TraceCarol.isDebugJndiCarol()) {
                            TraceCarol.debugJndiCarol("IIOPCosNaming:");
                            TraceCarol.debugJndiCarol(new String(b));
                        }
                    }

                    // add a shudown hook for this process
                    Runtime.getRuntime().addShutdownHook(new Thread() {

                        public void run() {
                            try {
                                IIOPCosNaming.this.stop();
                            } catch (Exception e) {
                                TraceCarol.error("IIOPCosNaming ShutdownHook problem", e);
                            }
                        }
                    });
                } else {
                    if (TraceCarol.isDebugJndiCarol()) {
                        TraceCarol.debugJndiCarol("Can't start IIOPCosNaming, port=" + port + " is < 0");
                    }
                }
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("IIOPCosNaming is already start on port:" + port);
                }
            }
        } catch (Exception e) {
            TraceCarol.error("Can not start IIOPCosNaming for an unknow Reason", e);
            throw new NameServiceException("can not start cosnaming daemon: " + e);
        }
    }

    /**
     * stop Method, Stop a NameService or do nothing if the name service is all
     * ready stop
     * @throws NameServiceException if a problem occure
     */
    public void stop() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("IIOPCosNaming.stop()");
        }
        try {
            // stop orbd procees
            if (cosNamingProcess != null) {
                cosNamingProcess.destroy();
            }
            cosNamingProcess = null;
        } catch (Exception e) {
            throw new NameServiceException("can not stop cosnaming daemon: " + e);
        }
    }

    /**
     * isStarted Method, check if a name service is started
     * @return boolean true if the name service is started
     */
    public boolean isStarted() {
        if (cosNamingProcess != null) {
            return true;
        }
        Properties prop = new Properties();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        prop.put(Context.PROVIDER_URL, "iiop://localhost:" + port);

        if (orb == null) {
            initORB();
        }

        prop.put("java.naming.corba.orb", orb);

        try {
            new InitialContext(prop);
        } catch (javax.naming.NamingException ex) {
            return false;
        }
        return true;
    }

    /**
     * set port method, set the port for the name service
     * @param p port number
     */
    public void setPort(int p) {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("IIOPCosNaming.setPort(" + p + ")");
        }
        if (p != 0) {
            port = p;
        }
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
     * @return the port number
     */
    public int getPort() {
        return port;
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
     * Set the configuration properties of the protocol
     * @param p configuration properties
     */
    public void setConfigProperties(Properties p) {

    }
}
