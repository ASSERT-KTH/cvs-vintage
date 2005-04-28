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
 * $Id: ProtocolConfigurationImpl.java,v 1.3 2005/04/28 11:37:26 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * This class manage a rmi configuration used by carol.<br>
 * The configuration is based on a protocol which contains required values and non modified values
 */
public class ProtocolConfigurationImpl implements ProtocolConfiguration, ProtocolConfigurationImplMBean {

    /**
     * Name of this configuration
     */
    private String name = null;

    /**
     * Protocol used by this configuration
     */
    private Protocol protocol = null;

    /**
     * Properties of this configuration
     */
    private Properties properties = null;

    /**
     * JNDI env
     */
    private Hashtable jndiEnv = null;

    /**
     * Host for this protocol name service
     */
    private String host = null;

    /**
     * port number for this protocol name servce
     */
    private int port = 0;

    /**
     * Object name
     */
    private String objectName = null;

    /**
     * Build a new configuration with given parameters
     * @param name the name of this protocol configuration
     * @param protocol the protocol object used by this configuration
     * @param properties for this object
     * @throws ConfigurationException if configuration is not correct
     */
    public ProtocolConfigurationImpl(String name, Protocol protocol, Properties properties) throws ConfigurationException {
        this.name = name;
        this.protocol = protocol;

        configure(properties);
    }


    /**
     * Configure this configuration with a given properties object
     * @param properties given properties
     * @throws ConfigurationException if the given config is invalid
     */
    public void configure(Properties properties) throws ConfigurationException {
        if (properties == null) {
            throw new ConfigurationException("Cannot build a configuration with a null properties object");
        }
        this.properties = properties;


        // extract JNDI env for InitialContext() creation
        extractJNDIProperties();

        // extract host and port of URL
        parseURL();
    }

    /**
     * Extract JNDI properties of properties
     * @throws ConfigurationException if properties are missing
     */
    protected void extractJNDIProperties() throws ConfigurationException {
        jndiEnv = new Hashtable();
        String prefixProtocol = CarolDefaultValues.CAROL_PREFIX + "." + protocol.getName() + ".";

        jndiEnv.put(Context.PROVIDER_URL, getValue(prefixProtocol + CarolDefaultValues.URL_PREFIX));
        jndiEnv.put(Context.INITIAL_CONTEXT_FACTORY, protocol.getInitialContextFactoryClassName());
    }


    /**
     * Parse URL to extract host and port
     * @throws ConfigurationException if URL is not correct
     */
    protected void parseURL() throws ConfigurationException {
        String url = getProviderURL();
        port = getPortOfUrl(url);
        host = getHostOfUrl(url);
    }

    /**
     * Build an initial context with the given environment using our configuration
     * @param env parameters for the initial context
     * @return an InitialContext
     * @throws NamingException if  the context is not created
     */
    public Context getInitialContext(Hashtable env) throws NamingException {
        Hashtable newEnv = (Hashtable) env.clone();
        newEnv.putAll(jndiEnv);
        return new InitialContext(newEnv);
    }

    /**
     * @return the protocol used by this configuration.
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * @return the name of this configuration
     */
    public String getName() {
        return name;
    }

    /**
     * @return a copy of the properties of this configuration
     */
    public Properties getProperties() {
        return (Properties) properties.clone();
    }

    /**
     * @return the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port for this protocol name service
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the Provider URL attribute
     */
    public String getProviderURL() {
        return (String) jndiEnv.get(Context.PROVIDER_URL);
    }

    /**
     * Parses the given url, and returns the port number. 0 is given in error
     * case)
     * @param url given url on which extract port number
     * @return port number of the url
     * @throws ConfigurationException if URL is invalid
     */
    protected int getPortOfUrl(String url) throws ConfigurationException {
        int portNumber = 0;
        try {
            StringTokenizer st = new StringTokenizer(url, ":");
            st.nextToken();
            st.nextToken();
            if (st.hasMoreTokens()) {
                StringTokenizer lastst = new StringTokenizer(st.nextToken(), "/");
                String pts = lastst.nextToken().trim();
                int i = pts.indexOf(',');
                if (i > 0) {
                    pts = pts.substring(0, i);
                }
                portNumber = new Integer(pts).intValue();
            }
            return portNumber;
        } catch (Exception e) {
            // don't rethrow original exception. only URL name is important
            throw new ConfigurationException("Invalid URL '" + url + "'. It should be on the format <protocol>://<hostname>:<port>");
        }
    }

    /**
     * Parses the given url, and returns the hostname
     * If not found, returns localhost
     * @param url given url on which extract hostname
     * @return hostname of the url
     * @throws ConfigurationException if URL is invalid
     */
    protected  String getHostOfUrl(String url) throws ConfigurationException {
        String host = null;
        // this would be simpler with a regexp :)
        try {
            // url is of the form protocol://<hostname>:<port>
            String[] tmpSplitStr = url.split(":");

            // array should be of length = 3
            // get 2nd element (should be //<hostname>)
            String tmpHost = tmpSplitStr[1];

            // remove //
            String[] tmpSplitHost = tmpHost.split("/");

            // Get last element of the array to get hostname
            host = tmpSplitHost[tmpSplitHost.length - 1];
        } catch (Exception e) {
            // don't rethrow original exception. only URL name is important
            throw new ConfigurationException("Invalid URL '" + url + "'. It should be on the format <protocol>://<hostname>:<port>");
        }
        return host;
    }


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
     * @return the jndiEnv (used for InitialContext).
     */
    public Hashtable getJndiEnv() {
        return jndiEnv;
    }

    /**
     * MBean method : Gets the InitialContextFactory classname (Context.INITIAL_CONTEXT_FACTORY)
     * @return the InitialContextFactory classname
     */
    public String getInitialContextFactoryClassName() {
        return protocol.getInitialContextFactoryClassName();
    }

    /**
     * Gets JNDI names of the context with this configuration
     * @return JNDI names
     * @throws NamingException if the names cannot be listed
     */
    public List getNames() throws NamingException {
        Context ctx = getInitialContext(getJndiEnv());

        List names = new ArrayList();
        NamingEnumeration ne = ctx.list("");
        String n = null;
        while (ne.hasMore()) {
            NameClassPair ncp = (NameClassPair) ne.next();
            n = ncp.getName();
            names.add(n);
        }
        return names;
    }

    /**
     * @return Object Name
     */
    public String getobjectName() {
        return objectName;
    }

    /**
     * Sets the object name of this mbean
     * @param name the Object Name
     */
    public void setobjectName(String name) {
        this.objectName = name;
    }

    /**
     * @return true if it is an event provider
     */
    public boolean iseventProvider() {
        return false;
    }

    /**
     * @return true if this managed object implements J2EE State Management
     *         Model
     */
    public boolean isstateManageable() {
        return false;
    }

    /**
     * @return true if this managed object implements the J2EE StatisticProvider
     *         Model
     */
    public boolean isstatisticsProvider() {
        return false;
    }

}
