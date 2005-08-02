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
 * $Id: IIOPCosNaming.java,v 1.12 2005/08/02 21:28:41 ashah Exp $
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
 * @author Guillaume Riviere
 * @author Florent Benoit (add POA model / Refactoring)
 */
public class IIOPCosNaming extends AbsRegistry implements NameService {

    /**
     * Default port number ( 12350 for default)
     */
    private static final int DEFAULT_PORT_NUMBER = 12350;

    /**
     * Sleep time to wait
     */
    private static final int SLEEP_TIME = 2000;

    /**
     * process of the cosnaming
     */
    private Process cosNamingProcess = null;

    /**
     * Unique instance of the ORB running in the JVM
     */
    private static ORB orb = null;

    /**
     * Default constructor
     */
    public IIOPCosNaming() {
        super(DEFAULT_PORT_NUMBER);
    }

    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occurs
     */
    public void start() throws NameServiceException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("IIOPCosNaming.start() on port:" + getPort());
        }
        try {
            if (!isStarted()) {
                // start a new orbd procees
                if (getPort() >= 0) {
                    cosNamingProcess = Runtime.getRuntime().exec(
                            System.getProperty("java.home") + System.getProperty("file.separator") + "bin"
                                    + System.getProperty("file.separator") + "tnameserv -ORBInitialPort " + getPort());
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
                        TraceCarol.debugJndiCarol("Can't start IIOPCosNaming, port=" + getPort() + " is < 0");
                    }
                }
            } else {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("IIOPCosNaming is already start on port:" + getPort());
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
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "org.objectweb.carol.jndi.spi.URLInitialContextFactory");
        prop.put(Context.PROVIDER_URL, "iiop://localhost:" + getPort());

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

}
