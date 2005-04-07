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
 * $Id: ConfigurationRepository.java,v 1.1 2005/04/07 15:07:07 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handle all rmi configuration available at runtime for carol.<br>
 * Configurations could be added/removed after the startup of carol
 * @author Florent Benoit
 */
public class ConfigurationRepository {

    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(ConfigurationRepository.class);

    /**
     * Default properties (content of carol-default.properties file)
     */
    private static Properties defaultProperties = null;

    /**
     * Configuration of the server (not the protocols)
     */
    private static ServerConfiguration serverConfiguration = null;

    /**
     * List of protocols configured (and that Carol can manage)
     */
    private static Map managedProtocols = null;

    /**
     * List of protocols used at runtime
     */
    private static Map managedConfigurations = null;

    /**
     * Thread Local for protocol context propagation
     */
    private static InheritableThreadLocal threadLocal = null;

    /**
     * Default configuration to fallback when there is a missing config in thread
     */
   private static ProtocolConfiguration defaultConfiguration = null;

   /**
    * Properties of the carol configuration
    */
   private static Properties properties = null;

   /**
    * Initialization is done
    */
   private static boolean initDone = false;

    /**
     * No public constructor, singleton
     */
    private ConfigurationRepository() {
    }

    /**
     * Check that the configuration is done
     */
    protected static void checkInitialized() {
        if (!initDone) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Do the configuration as the configuration was not yet done!");
                }
                init();
            } catch (ConfigurationException ce) {
                IllegalStateException ise = new IllegalStateException("Configuration of carol was not done and when trying to initialize it, it fails.");
                ise.initCause(ce);
                throw ise;
            }
        }
    }


    /**
     * Checks that carol is initialized
     */
    protected static void checkConfigured() {
        // check that init is done.
        checkInitialized();
        if (managedConfigurations == null) {
            throw new IllegalStateException("Cannot find a configuration, carol was not configured");
        }
    }

    /**
     * @return a list of current configurations
     */
    public static ProtocolConfiguration[] getConfigurations() {
        checkConfigured();
        Set set = managedConfigurations.keySet();
        ProtocolConfiguration[] configs = new ProtocolConfiguration[set.size()];
        int c = 0;
        for (Iterator it = set.iterator(); it.hasNext();) {
            String key = (String) it.next();
            configs[c] = (ProtocolConfiguration) managedConfigurations.get(key);
            c++;
        }
        return configs;

    }



    /**
     * Gets a configuration with the given name
     * @param configName name of the configuration
     * @return configuration object associated to the given name
     */
    public static ProtocolConfiguration getConfiguration(String configName) {
        checkConfigured();
        return (ProtocolConfiguration) managedConfigurations.get(configName);
    }

    /**
     * Gets a protocol with the given name
     * @param protocolName name of the protocol
     * @return protocol object associated to the given name
     */
    public static Protocol getProtocol(String protocolName) {
        checkConfigured();
        return (Protocol) managedProtocols.get(protocolName);
    }



    /**
     * Build a new configuration for a given protocol
     * @param configurationName the name of the configuration
     * @param protocolName name of the protocol
     * @return a new configuration object for a given protocol
     * @throws ConfigurationException if no configuration can be built
     */
    public static ProtocolConfiguration newConfiguration(String configurationName, String protocolName) throws ConfigurationException {
        checkConfigured();
        Protocol p = null;

        // Check that there is no configuration existing with the same name
        if (managedConfigurations.get(configurationName) != null) {
            throw new ConfigurationException("There is an existing configuration with the name '" + configurationName + "'. Use another name.");
        }

        // Get configured protocol
        if (managedProtocols != null) {
            p = (Protocol) managedProtocols.get(protocolName);
        }
        if (p == null) {
            throw new ConfigurationException("Protocol '" + protocolName + "' doesn't exists in carol. Cannot build");
        }

        return new ProtocolConfigurationImpl(configurationName, p, new Properties());
    }

    /**
     * Set the current configuration object
     * @param config the configuration to set as current configuration
     * @return previous value of the configuration which was set
     */
    public static ProtocolConfiguration setCurrentConfiguration(ProtocolConfiguration config) {
        checkConfigured();
        ProtocolConfiguration old = getCurrentConfiguration();
        threadLocal.set(config);
        return old;
    }

    /**
     * @return current carol configuration
     */
    public static ProtocolConfiguration getCurrentConfiguration() {
        checkConfigured();
        Object o = threadLocal.get();
        if (o != null) {
                return (ProtocolConfiguration) o;
        } else {
            return defaultConfiguration;
        }
    }

    /**
     * Initialize Carol configurations with the carol.properties URL
     * @param carolPropertiesFileURL URL rerencing the configuration file
     * @throws ConfigurationException if no properties can be loaded
     */
    public static void init(URL carolPropertiesFileURL) throws ConfigurationException {
        if (initDone) {
            return;
        }
        Properties carolDefaultProperties = getDefaultProperties();
        Properties carolProperties = getPropertiesFromURL(carolPropertiesFileURL);

        /*
         * Merge two properties file in following order : carol properties
         * overwrite default values
         */
        properties = mergeProperties(carolDefaultProperties, carolProperties);

        // Extract general configuration
        serverConfiguration = new ServerConfiguration(properties);

        // Reinit protocols
        managedProtocols = new HashMap();
        managedConfigurations = new HashMap();

        // Reset thread local
        threadLocal = new InheritableThreadLocal();

        // Now build protocols objects by extracting them
        int propertyBeginLength = CarolDefaultValues.CAROL_PREFIX.length();
        int propertyEndLength = CarolDefaultValues.FACTORY_PREFIX.length();
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            // Detect protocol name on all matching carol.XXXX.context.factory
            // properties
            if (key.startsWith(CarolDefaultValues.CAROL_PREFIX) && key.endsWith(CarolDefaultValues.FACTORY_PREFIX)) {
                // Extract protocol name
                String protocolName = key.substring(propertyBeginLength + 1, key.length() - propertyEndLength - 1);
                // Build protocol
                if (logger.isDebugEnabled()) {
                    logger.debug("Build protocol object for protocol name found '" + protocolName + "'.");
                }
                Protocol protocol = new Protocol(protocolName, properties);
                managedProtocols.put(protocolName, protocol);

                // if protocol is cmi, configure it
                if (protocolName.equals("cmi")) {
                    try {
                        org.objectweb.carol.cmi.Config.setProperties(properties);
                    } catch (NoClassDefFoundError ncdfe) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Cmi is not available, don't configure it.");
                        }
                    } catch (Exception ex) {
                        TraceCarol.error("Cannot set the cmi configuration.", ex);
                        throw new ConfigurationException("Cannot set the cmi configuration.", ex);
                    }
                }
            }
        }

        /*
         * ... and finish with building configurations This is done by using
         * protocols choosen by user
         */

        // read property in carol properties file
        String protocols = properties.getProperty(CarolDefaultValues.PROTOCOLS_KEY);
        String defaultProtocol = properties.getProperty(CarolDefaultValues.DEFAULT_PROTOCOLS_KEY);
        if (defaultProtocol == null) {
            throw new ConfigurationException("No default protocol defined with property '"
                    + CarolDefaultValues.DEFAULT_PROTOCOLS_KEY + "', check your carol configuration.");
        }
        if (protocols == null) {
            logger.info("No protocols were defined for property '" + CarolDefaultValues.PROTOCOLS_KEY
                    + "', trying with default protocol = '" + defaultProtocol + "'.");
            protocols = defaultProtocol;
        }

        // for each protocol, build a configuration object
        String[] protocolsArray = protocols.split(",");

        for (int p = 0; p < protocolsArray.length; p++) {
            // protocol name
            String pName = protocolsArray[p];

            // is it present ?
            Protocol protocol = (Protocol) managedProtocols.get(pName);
            if (protocol == null) {
                throw new ConfigurationException("Cannot find a protocol with name '" + pName
                        + "' in the list of available protocols.");
            }
            ProtocolConfiguration protoConfig = new ProtocolConfigurationImpl(pName, protocol, properties);
            managedConfigurations.put(pName, protoConfig);
        }

        // set the default configuration to the first available protocol
        if (protocolsArray[0] != null) {
            defaultConfiguration =  (ProtocolConfiguration) managedConfigurations.get(protocolsArray[0]);
        }

        initDone = true;
    }

    /**
     * Gets server configuration (made with carol-default.properties and carol.properties file)
     * @return server configuration
     */
    public static ServerConfiguration getServerConfiguration() {
        checkConfigured();
        return serverConfiguration;
    }

    /**
     * Merge content of two properties object (second overwrite first values)
     * @param defaultValues default values
     * @param values new values
     * @return properties object with merge done
     */
    protected static Properties mergeProperties(Properties defaultValues, Properties values) {
        Properties p = new Properties();
        p.putAll(defaultValues);
        // overwrite some
        p.putAll(values);
        return p;
    }

    /**
     * Initialize Carol configurations with an URL of carol properties file
     * found with Classloader
     * @throws ConfigurationException if no properties can be loaded
     */
    public static void init() throws ConfigurationException {
        init(Thread.currentThread().getContextClassLoader().getResource(CarolDefaultValues.CAROL_CONFIGURATION_FILE));
    }

    /**
     * Initialize carol with default configuration file found in jar of carol
     * @return properties object corresponding to carol-default.properties file
     * @throws ConfigurationException if the properties file cannot be get
     */
    protected static Properties getDefaultProperties() throws ConfigurationException {
        if (defaultProperties == null) {
            // First, found the URL of this file
            URL defaultConfigurationFile = Thread.currentThread().getContextClassLoader().getResource(
                    CarolDefaultValues.CAROL_DEFAULT_CONFIGURATION_FILE);

            defaultProperties = getPropertiesFromURL(defaultConfigurationFile);
        }
        return defaultProperties;

    }

    /**
     * Gets a properties object based on given URL
     * @param url URL from where build properties object
     * @return properties object with data from the given URL
     * @throws ConfigurationException if properties cannot be built
     */
    protected static Properties getPropertiesFromURL(URL url) throws ConfigurationException {
        if (url == null) {
            throw new ConfigurationException("Cannot use null URL.");
        }

        // Get inputStream + invalid cache
        InputStream is = null;
        try {
            URLConnection urlConnect = null;
            urlConnect = url.openConnection();
            // disable cache
            urlConnect.setDefaultUseCaches(false);

            is = urlConnect.getInputStream();
        } catch (IOException ioe) {
            throw new ConfigurationException("Invalid URL '" + url + "' : " + ioe.getMessage(), ioe);
        }

        // Check inpustream
        if (is == null) {
            throw new ConfigurationException("No inputstream for URL '" + url + "'.");
        }

        // load properties from URL
        Properties p = new Properties();
        try {
            p.load(is);
        } catch (IOException ioe) {
            throw new ConfigurationException("Could not load input stream of  URL '" + url + "' : " + ioe.getMessage());
        }

        // close inputstream
        try {
            is.close();
        } catch (IOException ioe) {
            throw new ConfigurationException("Cannot close inputStream", ioe);
        }

        return p;
    }


    /**
     * @return the default Configuration when no thread local is set.
     */
    public static ProtocolConfiguration getDefaultConfiguration() {
        checkConfigured();
        return defaultConfiguration;
    }


    /**
     * @return the properties used by Carol configuration.
     */
    public static Properties getProperties() {
        checkConfigured();
        return properties;
    }

    /**
     * @return the number of active protocols
     */
    public static int getActiveConfigurationsNumber() {
        checkConfigured();
        if (managedConfigurations != null) {
            return managedConfigurations.size();
        } else {
            return 0;
        }
    }


    /**
     * Add interceptor at runtime for a given protocol
     * @param protocolName protocol name
     * @param interceptorInitializer Interceptor Intializer class name
     * @throws ConfigurationException if interceptor cannot be added
     */
    public static void addInterceptors(String protocolName, String interceptorInitializer)
            throws ConfigurationException {
        checkConfigured();
        Protocol protocol = getProtocol(protocolName);
        if (protocol == null) {
            throw new ConfigurationException("Cannot add interceptor on an unknown protocol '" + protocolName + "'.");
        }
        protocol.addInterceptor(interceptorInitializer);
    }

}
