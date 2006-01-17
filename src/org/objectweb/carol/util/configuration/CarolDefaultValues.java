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
 * $Id: CarolDefaultValues.java,v 1.24 2006/01/17 16:14:45 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Class <code> DefaultCarolValues </code> get default carol value for the
 * properties file and get carol properties with defaults from jndi Standard
 * properties
 */
public class CarolDefaultValues {

    /**
     * Carol default configuration file
     */
    public static final String CAROL_DEFAULT_CONFIGURATION_FILE = "carol-defaults.properties";

    /**
     * Carol configuration file
     */
    public static final String CAROL_CONFIGURATION_FILE = "carol.properties";

    /**
     * Carol prefix
     */
    public static final String CAROL_PREFIX = "carol";

    /**
     * Server mode property (if true, this means that carol is running in a server which export objects)
     */
    public static final String SERVER_MODE = CAROL_PREFIX + ".server.mode";

    /**
     * Setter class property name (specifies the protocol class used to set the properties in this protocol)
     */
    public static final String SETTER_CLASS = "setter.class";

    /**
     * Setter method properties property name (specifies the method of the class given by the
     * SETTER_CLASS_PROPERTIES used to set the properties in this protocol)
     */
    public static final String SETTER_METHOD_PROPERTIES = "setter.methodProperties";

    /**
     * Setter method MBean property name (specifies the method of the class given by the
     * SETTER_CLASS_PROPERTIES used to set the properties in this protocol)
     */
    public static final String SETTER_METHOD_MBEAN = "setter.methodMBean";

    /**
     * Port number to use in server mode case (iiop)
     */
    public static final String  SERVER_IIOP_PORT = CAROL_PREFIX + ".iiop.server.port";

    /**
     * Port number to use in server mode case (irmi)
     */
    public static final String  SERVER_IRMI_PORT = CAROL_PREFIX + ".irmi.server.port";

    /**
     * Use a single interface for creating the registry in server mode case (irmi)
     */
    public static final String  SERVER_IRMI_SINGLE_ITF = CAROL_PREFIX + ".irmi.interfaces.bind.single";

    /**
     * Port number to use in server mode case (jrmp)
     */
    public static final String  SERVER_JRMP_PORT = CAROL_PREFIX + ".jrmp.server.port";

    /**
     * Use a single interface for creating the registry in server mode case (jrmp)
     */
    public static final String  SERVER_JRMP_SINGLE_ITF = CAROL_PREFIX + ".jrmp.interfaces.bind.single";


    /**
     * Port number to use in server mode case (jeremie)
     */
    public static final String  SERVER_JEREMIE_PORT = CAROL_PREFIX + ".jeremie.server.port";

    /**
     * Ssl Port number to use in server mode case (iiop)
     */
    public static final String  SERVER_SSL_IIOP_PORT = CAROL_PREFIX + ".iiop.server.sslport";

    /**
     * Default ssl port value
     */
    public static final int DEFAULT_SSL_PORT = 2003;

    /**
     * Default hostname
     */
    public static final String DEFAULT_HOST = "localhost";

    /**
     * JNDI Prefix
     */
    public static final String JNDI_PREFIX = "jndi";

    /**
     * JVM Prefix
     */
    public static final String JVM_PREFIX = "jvm";

    /**
     * name service class prefix
     */
    public static final String NS_PREFIX = "NameServiceClass";

    /**
     * portable remote object Prefix
     */
    public static final String PRO_PREFIX = "PortableRemoteObjectClass";

    /**
     * carol url Prefix
     */
    public static final String URL_PREFIX = "url";

    /**
     * carol jrmp local call optimization
     */
    public static final String LOCAL_JRMP_PROPERTY = "rmi.local.call";

    /**
     * carol jrmp registry local flag
     */
    public static final String LOCALREG_JRMP_PROPERTY = "rmi.local.registry";
    /**
     * carol factory Prefix
     */
    public static final String FACTORY_PREFIX = "context.factory";

    /**
     * start name service Prefix
     */
    public static final String START_NS_PREFIX = "start.ns";

    /**
     * start ns key
     */
    public static final String START_NS_KEY = "carol.start.ns";

    /**
     * start rmi key
     */
    public static final String START_RMI_KEY = "carol.start.rmi";

    /**
     * start jndi key
     */
    public static final String START_JNDI_KEY = "carol.start.jndi";

    /**
     * default activation key
     */
    public static final String DEFAULT_PROTOCOLS_KEY = "carol.protocols.default";

    /**
     * acativation key
     */
    public static final String PROTOCOLS_KEY = "carol.protocols";

    /**
     * start ns key
     */
    public static final String MULTI_RMI_PREFIX = "multi";

    /**
     * start prod key
     */
    public static final String MULTI_PROD = "org.objectweb.carol.rmi.multi.MultiPRODelegate";

    /**
     * start jndi key
     */
    public static final String MULTI_JNDI = "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory";

    /**
     * interceptor prefix
     */
    public static final String INTERCEPTOR_PKGS_PREFIX = "interceptor.pkgs";

    /**
     * interceptor prefix
     */
    public static final String INTERCEPTOR_VALUES_PREFIX = "interceptors";


    /**
     * Hashtable mapping between default en rmi name
     */
    private static Properties mapping = new Properties();

    static {
        mapping.setProperty("rmi", "jrmp");
        mapping.setProperty("iiop", "iiop");
        mapping.setProperty("jrmi", "jeremie");
        mapping.setProperty("cmi", "cmi");
    }

    /**
     * return protocol name from url
     * @return protocol name
     * @param url protocol jndi url
     */
    public static String getRMIProtocol(String url) {
        if (url != null) {
            StringTokenizer st = new StringTokenizer(url, "://");
            if (!st.hasMoreTokens()) {
                throw new IllegalArgumentException("The given url '" + url + "' is not on the format protocol://<something>.");
            }
            String pref = st.nextToken().trim();
            return mapping.getProperty(pref, pref);
        } else {
            return null;
        }
    }

    /**
     *  Utility class, no constructor
     */
    private CarolDefaultValues() {

    }
}
