/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2005 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: ProtocolConfiguration.java,v 1.1 2005/04/07 15:07:07 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * This interface defines an rmi configuration that is used by Carol.
 * @author Florent Benoit
 */
public interface ProtocolConfiguration {

    /**
     * @return the protocol used by this configuration.
     */
    Protocol getProtocol();

    /**
     * @return the name of this configuration
     */
    String getName();


    /**
     * Build an initial context with the given environment using our
     * configuration
     * @param env parameters for the initial context
     * @return an InitialContext
     * @throws NamingException if the context is not created
     */
    Context getInitialContext(Hashtable env) throws NamingException;

    /**
     * @return properties of this configuration
     */
    Properties getProperties();

    /**
     * @return the host.
     */
    String getHost();

    /**
     * @return the port for this protocol name service
     */
    int getPort();

    /**
     * @return the Provider URL attribute
     */
    String getProviderURL();

    /**
     * Configure this configuration with a given properties object
     * @param properties given properties
     * @throws ConfigurationException if the given config is invalid
     */
    void configure(Properties properties) throws ConfigurationException;
}
