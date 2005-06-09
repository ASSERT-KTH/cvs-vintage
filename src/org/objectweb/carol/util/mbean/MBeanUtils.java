/**
 * Copyright (C) 2005 - Bull S.A.
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
 * $Id: MBeanUtils.java,v 1.3 2005/06/09 11:42:31 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.mbean;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;

import org.objectweb.carol.util.configuration.ConfigurationException;
import org.objectweb.carol.util.configuration.ProtocolConfigurationImplMBean;

/**
 * This class is used to manage mbean registration.
 * All MBean stuff should go here, all imports on javax.management are in this class.
 * So if carol is not initialized with MBeanServer, JMX jars are not required at runtime.
 * @author Florent Benoit
 */
public class MBeanUtils {

    /**
     * MBeanServer
     */
    private static MBeanServer mbeanServer = null;

    /**
     * Utility class, no constructor
     */
    private MBeanUtils() {

    }

    /**
     * Init MBeanServer
     * @param logger the logger to use to log messages
     * @throws ConfigurationException if the registration failed
     */
    protected static void initMBeanServer(Log logger) throws ConfigurationException {
        List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        if (mbeanServers.size() == 0) {
            throw new ConfigurationException("No MBean Servers were found.");
        }
        if (mbeanServers.size() > 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Take first MBeanServer of the list");
            }
        }
        mbeanServer = (MBeanServer) mbeanServers.get(0);
    }


    /**
     * Register a ProtocolConfiguration object in MBeanServer
     * @param protocolConfiguration the configuration to register
     * @param logger the logger to use to log messages
     * @param domainName the name of the JOnAS domain
     * @param serverName the name of the server (for ObjectName)
     * @throws ConfigurationException if the registration failed
     */
    public static void registerProtocolConfigurationMBean(ProtocolConfigurationImplMBean protocolConfiguration, Log logger, String domainName, String serverName) throws ConfigurationException {

        // get MBeanServer if not present
        if (mbeanServer == null) {
            initMBeanServer(logger);
        }
        StringBuffer sb = new StringBuffer(domainName);
        sb.append(":j2eeType=JNDIResource");
        sb.append(",name=");
        sb.append(protocolConfiguration.getName());
        sb.append(",J2EEServer=");
        sb.append(serverName);
        ObjectName on = null;
        try {
            on = new ObjectName(sb.toString());
        } catch (MalformedObjectNameException e) {
            throw new ConfigurationException("Cannot build ObjectName for configuration '" + protocolConfiguration.getName() + "'", e);
        }

        // Set the objectname
        protocolConfiguration.setobjectName(on.toString());

        try {
            mbeanServer.registerMBean(protocolConfiguration, on);
        } catch (Exception e) {
            throw new ConfigurationException("Cannot register MBean '" + on + "' in MBeanServer", e);
        }

    }

}
