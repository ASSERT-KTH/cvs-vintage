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
 * $Id: ServerConfig.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * The <code>ServerConfig</code> provides a method to give configuration
 * information to the CMI runtime.
 *
 * @author Simon Nieuviarts
 */
public class ServerConfig {

    /**
     * isConfigured flag
     */
    private static boolean configured = false;

    /**
     * Multicast address
     */
    private static String multicastAddress = null;

    /**
     * Multicast interface
     */
    private static String multicastItf = null;

    /**
     * Multicast port number
     */
    private static int multicastPort = -1;

    /**
     * Multicast group name
     */
    private static String multicastGroupName = null;
    //private static Vector membersIp = null;

    /**
     * loalhost
     */
    private static String localHost = null;

    /**
     * Default values for the load
     */
    public final static int DEFAULT_LOAD_FACTOR = 100;

    /**
     * Load factor
     */
    private static int loadFactor = DEFAULT_LOAD_FACTOR;

    /**
     * Debug mode
     */
    private static boolean stubDebug = false;

    /**
     * Multicast address property name
     */
    public static final String MULTICAST_ADDRESS_PROPERTY = "carol.cmi.multicast.address";

    /**
     * Multicast interface property name
     */
    public static final String MULTICAST_ITF_PROPERTY = "carol.cmi.multicast.itf";

    /**
     * Multicast group name property name
     */
    public static final String MULTICAST_GROUPNAME_PROPERTY = "carol.cmi.multicast.groupname";

    /**
     * Multicast round robin factor property name
     */
    public static final String RR_FACTOR_PROPERTY = "carol.cmi.rr.factor";

    /**
     * Multicast debug property name
     */
    public static final String STUB_DEBUG_PROPERTY = "carol.cmi.stub.debug";

    /**
     * Intializes the CMI runtime configuration.
     * Can be called succesfully only one time.
     *
     * @param pr                        the configuration properties
     * @throws ServerConfigException    if an invalid configuration is specified
     */
    public static synchronized void setProperties(Properties pr)
        throws ServerConfigException {

        if (configured) {
            throw new ServerConfigException("Cmi already configured");
        }
        Iterator i = pr.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String s = (String) e.getValue();
            Object k = e.getKey();
            if (k.equals(MULTICAST_ADDRESS_PROPERTY)) {
                s = s.trim();
                try {
                    int l = s.indexOf(':');
                    s.substring(0, l);
                    String a =
                        InetAddress
                            .getByName(s.substring(0, l))
                            .getHostAddress();
                    int p = new Integer(s.substring(l + 1)).intValue();
                    multicastAddress = a;
                    multicastPort = p;
                } catch (Exception ex) {
                    throw new ServerConfigException(
                        "Invalid multicast address (" + s + ")", ex);
                }
            } else if (k.equals(MULTICAST_GROUPNAME_PROPERTY)) {
                multicastGroupName = s.trim();
            } else if (k.equals(MULTICAST_ITF_PROPERTY)) {
                multicastItf = s.trim();
            } else if (k.equals(RR_FACTOR_PROPERTY)) {
                loadFactor = new Integer(s.trim()).intValue();
            } else if (k.equals(STUB_DEBUG_PROPERTY)) {
                stubDebug = new Boolean(s.trim()).booleanValue();
            }
        }
        configured = true;
    }

    /**
     * Get the multicast group name
     * @return group name
     * @throws ServerConfigException if the config item is not defined
     */
    static String getMulticastGroupName() throws ServerConfigException {
        if (multicastGroupName == null)
            throw new ServerConfigException(
                "Property " + MULTICAST_GROUPNAME_PROPERTY + " not defined");
        return multicastGroupName;
    }

    /**
     * Get the multicast addr
     * @return addr
     * @throws ServerConfigException if the config item is not defined
     */
    static String getMulticastAddress() throws ServerConfigException {
        if (multicastAddress == null)
            throw new ServerConfigException(
                "Property " + MULTICAST_ADDRESS_PROPERTY + " not defined");
        return multicastAddress;
    }

    /**
     * Get the multicast port
     * @return multicast port numer
     */
    static int getMulticastPort() {
        return multicastPort;
    }

    /**
     * Get the multicast interface
     * @return multicast interface
     */
    static String getMulticastItf() {
        return multicastItf;
    }

    /**
     * Get the load balancing factor
     * @return factor
     */
    static int getLoadFactor() {
        return loadFactor;
    }

    /**
     * Test if the debug mode is enabled
     * @return true if enabled, false otherwise
     */
    static boolean isStubDebug() {
        return stubDebug;
    }
}
