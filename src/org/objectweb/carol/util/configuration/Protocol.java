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
 * $Id: Protocol.java,v 1.2 2005/11/23 21:35:39 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.apache.commons.logging.Log;


/**
 * This class defines commons attributes of a protocol for Carol.<br>
 * An rmi configuration relies on a protocol by specifying properties. For
 * example a protocol is composed of a Prodelegate Implementation class, a
 * registry class, etc.<br>
 * But the PROVIDER_URL could be different. This is done in Configuration
 * object. (one protocol could be associated to different configurations)<br>
 * ie : JRMP --> jrmp1 with localhost:1099, jrmp2 with localhost:1100
 * @author Florent Benoit
 */
public class Protocol {

    /**
     * Name of this protocol
     */
    private String name = null;

    /**
     * Properties of this protocol
     */
    private Properties properties = null;

    /**
     * PortableRemoteObject object
     */
    private PortableRemoteObjectDelegate portableRemoteObjectDelegate = null;

    /**
     * Name of the class of PortableRemoteObject
     */
    private String portableRemoteObjectClassName = null;

    /**
     * Context.INITIAL_CONTEXT_FACTORY class implementation
     */
    private String initialContextFactoryClassName = null;

    /**
     * Registry class (implementing org.objectweb.carol.jndi.ns.NameService
     * class)
     */
    private String registryClassName = null;

    /**
     * Prefix for interceptors
     */
    private String interceptorNamePrefix = null;

    /**
     * Gets value of properties object
     * @param key the key of the properties
     * @return value stored in a property object
     * @throws ConfigurationException if properties are missing
     */
    protected String getValue(String key) throws ConfigurationException {
        // properties cannot be null, check in constructor
        String s = properties.getProperty(key);
        if (s == null) {
            throw new ConfigurationException("Property '" + key + "' was not found in the properties object of the protocol, properties are :'" + properties + "'");
        }
        return s;
    }


    /**
     * Build a new protocol object with given parameters
     * @param name the name of this protocol
     * @param properties properties of this protocol
     * @param logger logger
     * @throws ConfigurationException if properties are missing
     */
    public Protocol(String name, Properties properties, Log logger) throws ConfigurationException {
        if (name == null || "".equals(name)) {
            throw new ConfigurationException("Cannot build a protocol with null or empty name");
        }
        this.name = name;

        if (properties == null) {
            throw new ConfigurationException("Cannot build a new protocol without properties");
        }
        this.properties = properties;

        String prefixProtocol = CarolDefaultValues.CAROL_PREFIX + "." + name + ".";

        // PRODelegate
        portableRemoteObjectClassName = getValue(prefixProtocol + CarolDefaultValues.PRO_PREFIX);

        // Initial Context factory
        this.initialContextFactoryClassName = getValue(prefixProtocol + CarolDefaultValues.FACTORY_PREFIX);

        // Registry class
        this.registryClassName = getValue(prefixProtocol + CarolDefaultValues.NS_PREFIX);

        // JVM interceptors
        interceptorNamePrefix = properties.getProperty(CarolDefaultValues.CAROL_PREFIX + "." + name + "."
                + CarolDefaultValues.INTERCEPTOR_PKGS_PREFIX);

        String interceptorValues = properties.getProperty(CarolDefaultValues.CAROL_PREFIX + "." + name + "."
                + CarolDefaultValues.INTERCEPTOR_VALUES_PREFIX);

        // set the jvm interceptors flag
        if ((interceptorNamePrefix != null) && (interceptorValues != null)) {
            //Parse jvm the properties
            String[] values = interceptorValues.split(",");
            for (int s = 0; s < values.length; s++) {
                String value = values[s];
                addInterceptor(value);
            }
        }

        // set the properties in the underlying protocol if needed
        String setterClass = properties.getProperty(CarolDefaultValues.CAROL_PREFIX + "." + name + "."
                + CarolDefaultValues.SETTER_CLASS_PROPERTIES);

        if (setterClass != null) {
            String setterMethod = getValue(CarolDefaultValues.CAROL_PREFIX + "." + name + "."
                + CarolDefaultValues.SETTER_METHOD_PROPERTIES);
            try {
                //org.objectweb.carol.cmi.ServerConfig.setProperties(properties);
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(setterClass);
                Method m = clazz.getMethod(setterMethod, new Class[] {Properties.class});
                m.invoke(null, new Object[] {properties});

            } catch (NoClassDefFoundError ncdfe) {
                if (logger.isDebugEnabled()) {
                    logger.debug(name + "is not available, don't configure it.");
                }
            } catch (Exception ex) {
                TraceCarol.error("Cannot set the " + name + " configuration.", ex);
                throw new ConfigurationException("Cannot set the " + name + " configuration.", ex);
            }
        }
    }

    /**
     * Add an interceptor for the given protocol
     * @param interceptorInitializer the class of the interceptor initializer
     */
    public void addInterceptor(String interceptorInitializer) {
        System.setProperty(interceptorNamePrefix + "." + interceptorInitializer, "");
        if (TraceCarol.isDebugCarol()) {
            TraceCarol.debugCarol("Setting interceptor " + interceptorNamePrefix + "." + interceptorInitializer + "/");
        }
    }


    /**
     * @return the initialContextFactory ClassName.
     */
    public String getInitialContextFactoryClassName() {
        return initialContextFactoryClassName;
    }

    /**
     * @return the registry ClassName.
     */
    public String getRegistryClassName() {
        return registryClassName;
    }


    /**
     * @return the portableRemoteObject delegate.
     */
    public PortableRemoteObjectDelegate getPortableRemoteObject() {
        if (portableRemoteObjectDelegate != null) {
            return portableRemoteObjectDelegate;
        }
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(portableRemoteObjectClassName);
            portableRemoteObjectDelegate = (PortableRemoteObjectDelegate) clazz.newInstance();
        } catch (Exception e) {
            IllegalStateException newEx = new IllegalStateException("Cannot build PortableRemoteObjectDelegate class '" + portableRemoteObjectClassName + "'");
            newEx.initCause(e);
            throw newEx;
        }

        return portableRemoteObjectDelegate;
    }


    /**
     * @return the name of this protocol.
     */
    public String getName() {
        return name;
    }


}
