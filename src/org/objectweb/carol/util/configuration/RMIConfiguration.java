/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * $Id: RMIConfiguration.java,v 1.14 2005/02/17 16:48:44 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Class <code> RmiConfiguration </code> implement the Properties way
 * representing the rmi configuration
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @author Florent Benoit
 */
public class RMIConfiguration {

    /**
     * Default hostname
     */
    public static final String DEFAULT_HOST = "localhost";

    /**
     * RMI Architecture name
     */
    private String rmiName = null;

    /**
     * Portable Remote Delegate class for this protocol
     */
    private String pro = null;

    /**
     * Host for this protocol name service
     */
    private String host = null;

    /**
     * name service for this protocol
     */
    private String nameServiceName = null;

    /**
     * port number for this protocol name servce
     */
    private int port = 0;

    /**
     * Interceptor prefix for System configuration
     */
    private String interPref = null;

    /**
     * extra system properties
     */
    private Properties jndiProperties = null;

    /**
     * configuration properties
     */
    private Properties confProperties = null;

    /**
     * Constructor, This constructor make a validation of the properties
     * @param name the RMI architecture name
     * @param carolProperties all properties
     * @throws RMIConfigurationException if one of the properties below missing: - -
     *         to be set (see the carol specifications) -
     */
    public RMIConfiguration(String name, Properties carolProperties) throws RMIConfigurationException {
        this.confProperties = new Properties();

        String rmiPref = CarolDefaultValues.CAROL_PREFIX + "." + name;
        String urlPref = CarolDefaultValues.CAROL_PREFIX + "." + name + "." + CarolDefaultValues.URL_PREFIX;
        String factoryPref = CarolDefaultValues.CAROL_PREFIX + "." + name + "." + CarolDefaultValues.FACTORY_PREFIX;

        if (name.equals("cmi")) {
            org.objectweb.carol.cmi.Config.setProperties(carolProperties);
        }

        // RMI Properties
        rmiName = name;

        // PortableRemoteObjectClass flag
        pro = carolProperties.getProperty(rmiPref + "." + CarolDefaultValues.PRO_PREFIX).trim();

        // NameServiceClass flag (not mandatory)
        if (carolProperties.getProperty(rmiPref + "." + CarolDefaultValues.NS_PREFIX) != null) {
            nameServiceName = carolProperties.getProperty(rmiPref + "." + CarolDefaultValues.NS_PREFIX).trim();
        }

        //interceptors simplifications
        interPref = carolProperties.getProperty(CarolDefaultValues.CAROL_PREFIX + "." + name + "."
                + CarolDefaultValues.INTERCEPTOR_PKGS_PREFIX);
        String interValues = carolProperties.getProperty(CarolDefaultValues.CAROL_PREFIX + "." + name + "."
                + CarolDefaultValues.INTERCEPTOR_VALUES_PREFIX);

        //set the jvm interceptors flag
        if ((interPref != null) && (interValues != null)) {
            //Parse jvm the properties
            Properties jvmProps = new Properties();
            jvmProps.putAll(System.getProperties());
            String current;
            StringTokenizer st = new StringTokenizer(interValues, ",");
            while (st.hasMoreTokens()) {
                current = st.nextToken().trim();
                jvmProps.setProperty(interPref + "." + current, "");
            }
            System.setProperties(jvmProps);
        }

        this.jndiProperties = new Properties();
        for (Enumeration e = carolProperties.propertyNames(); e.hasMoreElements();) {
            String key = ((String) e.nextElement()).trim();

            // Add in confProperties all matching properties for the protocol
            if (key.startsWith(rmiPref)) {
                confProperties.setProperty(key, carolProperties.getProperty(key));
            }

            // jndi configuration
            if (key.startsWith(urlPref)) {
                jndiProperties.setProperty(CarolDefaultValues.JNDI_URL_PREFIX, carolProperties.getProperty(key));
            } else if (key.startsWith(factoryPref)) {
                jndiProperties
                        .setProperty(CarolDefaultValues.JNDI_FACTORY_PREFIX, carolProperties.getProperty(key));
            }
        }

        port = getPortOfUrl(this.jndiProperties.getProperty(CarolDefaultValues.JNDI_URL_PREFIX));
        host = getHostOfUrl(this.jndiProperties.getProperty(CarolDefaultValues.JNDI_URL_PREFIX));
    }

    /**
     * @return name
     */
    public String getName() {
        return rmiName;
    }

    /**
     * @return Portable Remote Delegate for this protocol
     */
    public String getPro() {
        return pro;
    }

    /**
     * @return the jndi properties for this protocol
     */
    public Properties getJndiProperties() {
        return jndiProperties;
    }

    /**
     * @return the jndi properties port for this protocol name service -1 if the
     *         port is not configured
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the name service class name
     */
    public String getNameService() {
        return nameServiceName;
    }

    /**
     * @return the interceptor prefix, "" if there is no prefix
     */
    public String getInterceptorPrefix() {
        return interPref;
    }

    /**
     * Parses the given url, and returns the port number. 0 is given in error
     * case)
     * @param url given url on which extract port number
     * @return port number of the url
     */
    private static int getPortOfUrl(String url) {
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
            return -1;
        }
    }

    /**
     * Parses the given url, and returns the hostname
     * If not found, returns localhost
     * @param url given url on which extract hostname
     * @return hostname of the url
     */
    private static String getHostOfUrl(String url) {
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
            return DEFAULT_HOST;
        }
        return host;
    }



    /**
     * @return the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the configuration Properties.
     */
    public Properties getConfigProperties() {
        return confProperties;
    }
}