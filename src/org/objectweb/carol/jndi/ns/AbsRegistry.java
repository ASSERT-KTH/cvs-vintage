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
 * $Id: AbsRegistry.java,v 1.1 2005/03/10 12:21:46 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.ns;

import java.util.Properties;


/**
 * This abstract class implements common methods of a NameService
 * Registry should extend this class.
 * @author Florent Benoit
 */
public abstract class AbsRegistry implements NameService {

    /**
     * port number
     */
    private int port = 0;

    /**
     * Hostname to use
     */
    private String host = null;


    /**
     * registry is started ?
     */
    private boolean isStarted = false;

    /**
     * Configuration properties (of carol.properties)
     */
    private Properties configurationProperties = null;


    /**
     * Build a new Registry
     */
    protected AbsRegistry() {

    }

    /**
     * Build a new Registry with a given default port number
     * @param defaultPortNumber the default port number
     */
    protected AbsRegistry(int defaultPortNumber) {
        this.port = defaultPortNumber;
    }


    /**
     * start Method, Start a new NameService or do nothing if the name service
     * is all ready start
     * @throws NameServiceException if a problem occure
     */
    public abstract void start() throws NameServiceException;

    /**
     * stop Method, Stop a NameService or do nothing if the name service is all
     * ready stop
     * @throws NameServiceException if a problem occure
     */
    public abstract void stop() throws NameServiceException;



    /**
     * isStarted Method, check if a name service is started
     * @return boolean true if the name service is started
     */
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * set port method, set the port for the name service
     * @param p port number
     */
    public void setPort(int p) {
        if (p <= 0) {
            throw new IllegalArgumentException(
                    "The number for the port is incorrect. It must be a value > 0. Value was '" + port + "'");
        }
        this.port = p;
    }

    /**
     * Set the address to use for bind
     * @param host hostname/ip address
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return hostname/ip to use
     */
    public String getHost() {
        return host;
    }

    /**
     * get port method, get the port for the name service
     * @return int port number
     */
    public int getPort() {
        return port;
    }


    /**
     * Set the configuration properties of the protocol
     * @param p configuration properties
     */
    public void setConfigProperties(Properties p) {
        this.configurationProperties = p;
    }


    /**
     * Registry is started
     */
    protected void setStarted() {
        this.isStarted = true;
    }

    /**
     * Registry is stopped
     */
    protected void resetStarted() {
        this.isStarted = false;
    }


    /**
     * @return the configuration properties.
     */
    protected Properties getConfigProperties() {
        return configurationProperties;
    }
}
