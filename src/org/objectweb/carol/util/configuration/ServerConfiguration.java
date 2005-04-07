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
 * $Id: ServerConfiguration.java,v 1.1 2005/04/07 15:07:07 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;

/**
 * This class handle the configuration of carol (independent of protocols
 * configuration) It doesn't implement ProtocolConfiguration interface which is
 * dedicated to protocols configuration
 * @author Florent Benoit
 */
public class ServerConfiguration {

    /**
     * Properties used for this server
     */
    private Properties properties = null;

    /**
     * Start nameservice of protocols ?
     */
    private boolean startNS = false;

    /**
     * Set MultiORB InitialContext factory ?
     */
    private boolean startJNDI = false;

    /**
     * Set MultiProDelegate class ?
     */
    private boolean startRMI = false;

    /**
     * Build a server configuration object with the given properties
     * @param properties the properties need to construct the configuration
     * @throws ConfigurationException if properties are missing
     */
    protected ServerConfiguration(Properties properties) throws ConfigurationException {
        this.properties = properties;
        if (properties == null) {
            throw new ConfigurationException("Cannot build a server configuration withotu properties");
        }

        // Init values with properties object
        startNS = getBooleanValue(CarolDefaultValues.START_NS_KEY);
        startRMI = getBooleanValue(CarolDefaultValues.START_RMI_KEY);
        startJNDI = getBooleanValue(CarolDefaultValues.START_JNDI_KEY);

        Properties jvmProperties = new Properties();
        if (startRMI) {
            jvmProperties.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", CarolDefaultValues.MULTI_PROD);
        }

        if (startJNDI) {
            jvmProperties.setProperty(Context.INITIAL_CONTEXT_FACTORY, CarolDefaultValues.MULTI_JNDI);
        }

        String protocols = properties.getProperty(CarolDefaultValues.PROTOCOLS_KEY);
        boolean isMultiProtocols = false;
        if (protocols != null) {
            isMultiProtocols = (protocols.split(",").length > 1);
        }



        String jndiPrefix = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.JNDI_PREFIX;
        String multiJvmPrefix = CarolDefaultValues.MULTI_RMI_PREFIX +  "." + CarolDefaultValues.CAROL_PREFIX + "." + "jvm";
        String singleJvmPrefix = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.JVM_PREFIX;
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            // JNDI JVM properties
            if (key.startsWith(jndiPrefix)) {
                jvmProperties.setProperty(key.substring(jndiPrefix.length() + 1), properties.getProperty(key));
            }

            // Interceptor multi (only in multi protocol)
            if (key.startsWith(multiJvmPrefix) && isMultiProtocols) {
                // extract key
                String newKey = key.substring(multiJvmPrefix.length() + 1);
                jvmProperties.setProperty(newKey, "");
            }

            // JVM properties
            if (key.startsWith(singleJvmPrefix)) {
                // extract key
                String newKey = key.substring(singleJvmPrefix.length() + 1);
                jvmProperties.setProperty(newKey, properties.getProperty(key));
            }

        }


        // Set jvm Properties previously defined
        for (Enumeration e = jvmProperties.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = jvmProperties.getProperty(key);
            System.setProperty(key, value);
            if (TraceCarol.isDebugCarol()) {
                TraceCarol.debugCarol("Set the JVM property '" + key + "' with the value '" + value + "'.");
            }
        }

    }

    /**
     * Gets value of properties object
     * @param key the key of the properties
     * @return value stored in a property object
     * @throws ConfigurationException if properties are missing
     */
    protected boolean getBooleanValue(String key) throws ConfigurationException {
        // properties cannot be null, check in constructor
        String s = properties.getProperty(key);
        if (s == null) {
            throw new ConfigurationException("Property '" + key
                    + "' was not found in the properties object of the protocol, properties are :'" + properties + "'");
        }
        return new Boolean(s.trim()).booleanValue();
    }

    /**
     * @return true if JNDI has to be started (set of MultiORB ICTX factory)
     */
    public boolean isStartingJNDI() {
        return startJNDI;
    }

    /**
     * @return true if name services have to be launched)
     */
    public boolean isStartingNS() {
        return startNS;
    }

    /**
     * @return true if ProdelegateClass has to be set to MultiProDelegate
     */
    public boolean isStartingRMI() {
        return startRMI;
    }

}
