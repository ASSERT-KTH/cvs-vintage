/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/SimpleRequestSecurityProviderImpl.java,v 1.1 2000/03/29 09:19:06 bergsten Exp $
 * $Revision: 1.1 $
 * $Date: 2000/03/29 09:19:06 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.tomcat.core;

import java.util.Hashtable;
import java.util.Vector;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;

/**
 * Experimental implementation of a RequestSecurityProvider, based
 * on the org.apache.tomcat.request.SecurityCheck.MemoryRealm class.
 * An instance of this class is set as the RequestSecurityProvider for
 * a Context by the SecurityCheck interceptor if the Context doesn't
 * have a RequestSecurityProvider.
 * <p>
 * Since the whole security implementation is still experimental,
 * and the RequestSecurityProvider interface has been deprecated, this 
 * class will likely be replaced soon.
 *
 * @author Hans Bergsten <hans@gefionsoftware.com>
 */

public class SimpleRequestSecurityProviderImpl implements RequestSecurityProvider {
    static private int base64[]= {
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 62, 64, 64, 64, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64, 64,
        64,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 64, 64, 64, 64, 64,
        64, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64
    };
    private Hashtable roles = new Hashtable();

    public SimpleRequestSecurityProviderImpl(Hashtable roles) {
        this.roles = roles;
    }

    /**
     * Returns a boolean indicating whether the authenticated user
     * is included in the specified logical "role". Roles and role
     * membership can be defined using deployment descriptors. If
     * the user has not been authenticated, the method returns false.
     *
     * @param context not used. One instance of this class is associated
     *    with one Context. The parameter is left over from a J2EE impl.
     * @param req the request to get the current user from
     * @param role the role name to check
     * @return true if the user is in the role, false if not or if the
     *   request user is not authenticated
     */
    public boolean isUserInRole(Context context, HttpServletRequest req,
        String role) {
        boolean inRole = false;
        String remoteUser = getUserName(req);
        if (remoteUser != null) {
            Vector users = (Vector) roles.get(role);
            if (users != null) {
                inRole = users.indexOf(remoteUser) >=0;
            }
        }
        return inRole;
    }

    /**
     * Returns a java.security.Principal object containing the name of
     * the current authenticated user.
     *
     * @param context not used. One instance of this class is associated
     *    with one Context. The parameter is left over from a J2EE impl.
     * @param req the request to get the current user from
     * @return a java.security.Principal containing the name of the
     *    user making this request; null if the user has not been
     *    authenticated
     */
    public Principal getUserPrincipal(Context context, HttpServletRequest req) {
        Principal principal = null;
        String remoteUser = getUserName(req);
        if (remoteUser != null) {
            principal = new SimplePrincipal(remoteUser);
        }
        return principal;
    }

    /**
     * Returns a boolean indicating whether this request was made
     * using a secure channel, such as HTTPS
     *
     * @param context not used. One instance of this class is associated
     *    with one Context. The parameter is left over from a J2EE impl.
     * @param req the request
     * @return a boolean indicating if the request was made using
     *   a secure channel
     */
    public boolean isSecure(Context context, HttpServletRequest req) {
        return req.getProtocol().startsWith("HTTPS");
    }

    /**
     * Returns the user name from the Authorization header if any.
     *
     * @param req the request
     * @return the user name, or null if not authenticated
     */
    private String getUserName(HttpServletRequest req) {
        String userName =  null;
        String authorization = req.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic ")) {
            authorization = authorization.substring(6).trim();
            String unencoded = base64Decode(authorization);
            int colon = unencoded.indexOf(':');
            if (colon > 0) {
                userName = unencoded.substring(0, colon);
            }
        }
        return userName;
    }

    private String base64Decode(String orig) {
        char chars[]=orig.toCharArray();
        StringBuffer sb=new StringBuffer();
        int i=0;

        int shift = 0;   // # of excess bits stored in accum
        int acc = 0;

        for (i = 0; i <chars.length; i++) {
            int v = base64[chars[i] & 0xFF];

            if (v >= 64) {
                if (chars[i] != '=') {
                    System.out.println("Wrong char in base64: " + chars[i]);
                }
            }
            else {
                acc = (acc << 6) | v;
                shift += 6;
                if (shift >= 8) {
                    shift -= 8;
                    sb.append((char) ((acc >> shift) & 0xff));
                }
            }
        }
        return sb.toString();
    }

    public class SimplePrincipal implements Principal {
        private String name;

        public SimplePrincipal(String name) {
            this.name = name;
        }

        /**
         * Returns true if the specified Object represents the
         * same principal (i.e. a Principal with the same name)
         *
         * @param another Another Principal instance
         * @return true if another is a Principal with the same name
         */
        public boolean equals(Object another) {
            return another instanceof Principal &&
                ((Principal) another).getName().equals(getName());
        }

        /**
         * Returns the principal's name.
         *
         * @return The principal's name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the principal's name.
         *
         * @return The principal's name
         */
        public String toString() {
            return getName();
        }
    }
}
