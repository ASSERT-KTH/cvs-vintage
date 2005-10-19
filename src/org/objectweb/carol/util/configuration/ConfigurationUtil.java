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
 * $Id: ConfigurationUtil.java,v 1.1 2005/10/19 13:40:36 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.StringTokenizer;

/**
 * Utility class to analyze some configuration properties
 * @author Florent Benoit
 */
public class ConfigurationUtil {

    /**
     * Utility class, no public constructor
     */
    private ConfigurationUtil() {

    }


    /**
     * Parses the given url, and returns the port number. 0 is given in error
     * case)
     * @param url given url on which extract port number
     * @return port number of the url
     * @throws ConfigurationException if URL is invalid
     */
    public static int getPortOfUrl(String url) throws ConfigurationException {
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
    public static String getHostOfUrl(String url) throws ConfigurationException {
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
}
