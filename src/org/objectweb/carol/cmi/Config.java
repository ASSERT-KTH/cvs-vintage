/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.objectweb.carol.util.configuration.RMIConfigurationException;

public class Config {
    private static boolean configured = false;
    private static String multicastAddress = null;
    private static String multicastItf = null;
    private static int multicastPort = -1;
    private static String multicastGroupName = null;
    //private static Vector membersIp = null;
    private static String localHost = null;
    public static final String MULTICAST_ADDRESS_PROPERTY =
        "carol.cmi.multicast.address";
    public static final String MULTICAST_ITF_PROPERTY =
        "carol.cmi.multicast.itf";
    public static final String MULTICAST_GROUPNAME_PROPERTY =
        "carol.cmi.multicast.groupname";

    /**
     * Set properties
     * @param pr
     */
    public static synchronized void setProperties(Properties pr)
        throws RMIConfigurationException {
        if (configured) {
            throw new RMIConfigurationException("Cmi already configured");
        }
        Iterator i = pr.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String s = (String) e.getValue();
            if (e.getKey().equals(MULTICAST_ADDRESS_PROPERTY)) {
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
                    throw new RMIConfigurationException(
                        "Invalid multicast address (" + s + ")");
                }
            } else if (e.getKey().equals(MULTICAST_GROUPNAME_PROPERTY)) {
                multicastGroupName = s.trim();
            } else if (e.getKey().equals(MULTICAST_ITF_PROPERTY)) {
                multicastItf = s.trim();
            }
        }
        configured = true;
    }

    public static String getMulticastGroupName() throws ConfigException {
        if (multicastGroupName == null)
            throw new ConfigException(
                "Property " + MULTICAST_GROUPNAME_PROPERTY + " not defined");
        return multicastGroupName;
    }

    public static String getMulticastAddress() throws ConfigException {
        if (multicastAddress == null)
            throw new ConfigException(
                "Property " + MULTICAST_ADDRESS_PROPERTY + " not defined");
        return multicastAddress;
    }

    public static int getMulticastPort() {
        return multicastPort;
    }

    public static String getMulticastItf() {
        return multicastItf;
    }
}
