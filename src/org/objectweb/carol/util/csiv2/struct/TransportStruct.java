/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
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
 * $Id: TransportStruct.java,v 1.5 2005/01/28 09:17:23 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2.struct;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.omg.CSIIOP.TransportAddress;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * @author Florent Benoit
 */
public class TransportStruct implements Serializable {

    /**
     * Default localhost ip address
     */
    private static final String LOCAL_IP = "127.0.0.1";

    /**
     * Default ssl port
     */
    public static final int DEFAULT_SSL_PORT = 2003;

    /**
     * TransportAddress
     */
    private TransportAddress[] transportAdresses = null;

    /**
     * Target supports for this mech
     */
    private short targetSupports = 0;

    /**
     * Target requires for this mech
     */
    private short targetRequires = 0;


    /**
     * Gets current SSL port
     * @return the ssl port
     */
    public int getSslPort() {
        // TODO : change return value
        return DEFAULT_SSL_PORT;
    }

    /**
     * @return TransportAddress[] object for SSL connection
     */
    public TransportAddress[] getTransportAddress() {

        int sslPort = getSslPort();

        String ipAddr = null;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            ipAddr = addr.getHostAddress();
        } catch (UnknownHostException uhe) {
            TraceCarol.error("Cannot get current hostname", uhe);
            ipAddr = LOCAL_IP;
        }
        transportAdresses = new TransportAddress[1];
        transportAdresses[0] = new TransportAddress(ipAddr, (short) sslPort);
        return transportAdresses;
    }

    /**
     * @param targetRequires The targetRequires to set.
     */
    public void setTargetRequires(int targetRequires) {
        this.targetRequires = (short) targetRequires;
    }

    /**
     * @param targetSupports The targetSupports to set.
     */
    public void setTargetSupports(int targetSupports) {
        this.targetSupports = (short) targetSupports;
    }

    /**
     * @return Returns the targetRequires.
     */
    public short getTargetRequires() {
        return targetRequires;
    }

    /**
     * @return Returns the targetSupports.
     */
    public short getTargetSupports() {
        return targetSupports;
    }

}