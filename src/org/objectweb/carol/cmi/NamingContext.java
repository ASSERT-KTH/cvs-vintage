/***
 * Jonathan: an Open Distributed Processing Environment 
 * Copyright (C) 1999 France Telecom R&D
 * Copyright (C) 2002, Simon Nieuviarts
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Release: 2.0
 *
 * Contact: jonathan@objectweb.org
 *
 * Author: Kathleen Milsted
 *
 * with contributions from:
 *   Bruno Dumant
 * 
 */
package org.objectweb.carol.cmi;

import java.net.MalformedURLException;

public class NamingContext {
    public String scheme = "";
    public NamingContextHostPort[] hp;
    public String name = "";

    public NamingContext(String input) throws MalformedURLException {
        if (input == null || input.length() == 0) {
            throw new MalformedURLException("null or empty registry URL");
        }
        if (input.indexOf("//") == -1) {
            throw new MalformedURLException(
                "badly formed registry URL " + input);
        }
        try {
            this.parseScheme(input.substring(0, input.indexOf("//")));
            this.parseName(input.substring(input.indexOf("//")));
        } catch (MalformedURLException e) {
            throw new MalformedURLException(
                "badly formed registry URL " + input + " - " + e.getMessage());
        } catch (Exception e) {
            throw new MalformedURLException(
                "badly formed registry URL " + input);
        }
    }

    private void parseScheme(String inputscheme) throws MalformedURLException {
        if (inputscheme.length() == 0) { // scheme can be empty
            this.scheme = "";
        } else {
            if (inputscheme.length() > 1 && inputscheme.endsWith(":")) {
                // non-empty scheme must contain at least one character and end with :
                this.scheme =
                    inputscheme.substring(0, inputscheme.length() - 1);
            } else {
                throw new MalformedURLException("badly formed protocol");
            }
            if (!scheme.equals("cmi")) {
                throw new MalformedURLException("Invalid protocol : " + scheme);
            }
        }
    }

    private void parseName(String inputurl) throws MalformedURLException {
        // inputurl is expected to start with //
        String inputname = "";
        String inputhostport = "";
        int n = inputurl.indexOf("/", 2); // find third / if any
        if (n == -1) { // no name
            if (inputurl.length() > 2) { // host and/or port specified
                inputhostport = inputurl.substring(2);
            }
        }
        if (n == 2) { // no host, no port
            if (inputurl.length() > 3) { // non-empty name preceded by /
                inputname = inputurl.substring(3);
            } else { // empty name preceded by / ; URL consists of ///
                throw new MalformedURLException("non-empty name expected after third /");
            }
        }
        if (n > 2) {
            // possibly non-empty host, non-empty port, non-empty name
            if (inputurl.length() > n + 1) {
                // non-empty name preceded by /
                inputname = inputurl.substring(n + 1);
            } else { // empty name preceded by / ; URL consists of //hostport/
                throw new MalformedURLException("non-empty name expected after third /");
            }
            inputhostport = inputurl.substring(2, n);
        }
        this.name = inputname;
        this.parseHostsPorts(inputhostport);
    }

    private void parseHostPort(String inputhostport, java.util.ArrayList hp)
        throws MalformedURLException {
        String inputhost = "";
        String inputport = "";
        int m = inputhostport.indexOf(':');
        if (m == -1) { // no port
            inputhost = inputhostport;
        } else {
            if (m == 0) { // no host
                if (inputhostport.length() > 1) {
                    // non-empty port preceded by /
                    inputport = inputhostport.substring(1);
                } else { // empty port preceded by : ; URL consists of //:
                    throw new MalformedURLException("non-empty port expected after :");
                }
            } else { // non-empty host, maybe non-empty port
                inputhost = inputhostport.substring(0, m);
                if (inputhostport.length() > m + 1) { // port specified
                    inputport = inputhostport.substring(m + 1);
                } else { // empty port preceded by : ; URL consists of //host:/
                    throw new MalformedURLException("non-empty port expected after :");
                }
            }
        }
        NamingContextHostPort nchp = new NamingContextHostPort();
        if (!inputhost.equals("")) {
            nchp.host = inputhost;
        }
        if (!inputport.equals("")) {
            try {
                nchp.port = Integer.parseInt(inputport);
            } catch (NumberFormatException e) {
                throw new MalformedURLException("port must be a number");
            }
        }
        hp.add(nchp);
    }

    private void parseHostsPorts(String inputhostsports)
        throws MalformedURLException {
        int start = 0;
        java.util.ArrayList hostsports = new java.util.ArrayList();
        do {
            int end = inputhostsports.indexOf(',', start);
            if (end < 0)
                parseHostPort(inputhostsports.substring(start), hostsports);
            else
                parseHostPort(
                    inputhostsports.substring(start, end),
                    hostsports);
            start = end + 1;
        } while (start > 0);
        int n = hostsports.size();
        hp = new NamingContextHostPort[n];
        for (int i = 0; i < n; i++)
            hp[i] = (NamingContextHostPort) hostsports.get(i);
    }
}
