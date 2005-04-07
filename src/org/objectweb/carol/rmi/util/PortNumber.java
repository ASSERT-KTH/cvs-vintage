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
 * $Id: PortNumber.java,v 1.2 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.util;

import org.objectweb.carol.util.configuration.TraceCarol;


/**
 * Convert String port number to int and do some check
 * @author Florent Benoit
 *  */
public class PortNumber {


    /**
     * Utility class, no constructor
     */
    private PortNumber() {

    }

    /**
     * Return port number based on given string
     * @param portStr port number in stringified format
     * @param propertyName name of the property of the port number in carol.properties file (for error messages)
     * @return port number (or 0)
     */
    public static int strToint(String portStr, String propertyName) {
            int port = 0;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException nfe) {
                TraceCarol.error("Invalid port number for property '" + propertyName + "'. Value set was '" + portStr + "'. It should be 0(random) or greater than 0. Error : " + nfe.getMessage());
            }
            if (port < 0) {
                String errMsg = "Invalid port number for property '" + propertyName + "'. It should be 0(random) or greater than 0.";
                TraceCarol.error(errMsg);
                throw new IllegalArgumentException(errMsg);
            }
            if (port == 0) {
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugCarol("Port was 0, will use a random port");
                }
            }
            return port;
    }
}
