/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/modules/aaa/RealmBase.java,v 1.3 2003/09/29 07:41:51 hgomez Exp $
 * $Revision: 1.3 $
 * $Date: 2003/09/29 07:41:51 $
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

package org.apache.tomcat.modules.aaa;

import java.security.MessageDigest;
import java.security.Principal;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.res.StringManager;

/*
*  Abstract Base class for Realms.
*  This class is intented to be a helper for defining Realms, it contains basic
*  utils for Digesting passwords, and create associated notes.
*  There are 3 abstract methods in this class, every Realm that inherits from
*  RealmBase needs to define them to make a basic working Realm..they are:
*
*    protected abstract String getCredentials(String username);
*    protected abstract String[] getUserRoles(String username);
*    protected abstract Principal getPrincipal(String username);
*
*  Defining this methods and if needed contextInit and contextShutdown from
*  BaseInterceptor are the only methods a Realm Writer needs
*  to take into account to construct a functional Realm for Tomcat 3.3
*
*  A Complex Realm that need more control over the auth process can already
*  inherit directly from BaseInterceptor.
*
*/

public abstract class RealmBase extends BaseInterceptor {

    int reqRolesNote=-1;
    int userNote=-1;
    int passwordNote=-1;

    /** The string manager for this package. */
    protected static StringManager sm = StringManager.getManager("org.apache.tomcat.resources");

    /**
     * Digest algorithm used in passwords.  Should be a value accepted by MessageDigest for algorithm
     * or "No" ( no encode ).  "No" is the default.
     */
    protected String digest = "No";

    /**
     * Gets the digest algorithm used for credentials in the database.
     * Should be a value that MessageDigest accepts for algorithm or "No".
     * "No" is the Default.
     * @return the digest algorithm being used, or "No" if no encoding
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Sets the digest algorithm used for credentials in the database.
     * Should be a value that MessageDigest accepts for algorithm or "No".
     * "No" is the Default.
     * @param algorithm the Encode type
     */
    public void setDigest(String algorithm) {
        digest = algorithm;
    }

    /**
     * Digest password using the algorithm especificied and
     * convert the result to a corresponding hex string.
     * If exception, the plain credentials string is returned
     * @param credentials Password or other credentials to use in authenticating this username
     * @param algorithm Algorithm used to do the digest
     */
    public static final String digest(String credentials,String algorithm ) {
        try {
            // Obtain a new message digest with MD5 encryption
            MessageDigest md = (MessageDigest)MessageDigest.getInstance(algorithm).clone();
            // encode the credentials
            md.update(credentials.getBytes());
            // obtain the byte array from the digest
            byte[] dig = md.digest();
            // convert the byte array to hex string
            //            Base64 enc=new Base64();
            //            return new String(enc.encode(HexUtils.convert(dig).getBytes()));
            return org.apache.tomcat.util.buf.HexUtils.convert(dig);
        } catch (Exception ex) {
            ex.printStackTrace();
            return credentials;
        }
    }

    /**
     * RealmBase can be used as a standalone tool for offline password digest
     * @param args
     */
    public static void main(String[] args) {
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("-a")) {
                for (int i = 2; i < args.length; i++) {
                    System.out.print(args[i] + ":");
                    System.out.println(digest(args[i], args[1]));
                }
            }
        }
    }
    protected abstract String getCredentials(String username);
    protected abstract String[] getUserRoles(String username);
    protected abstract Principal getPrincipal(String username);


    String digest(String credentials) {
        if( digest.equals("") || digest.equalsIgnoreCase("No")){
            return credentials;
        } else {
            return digest(credentials,digest);
        }
    }

    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	reqRolesNote = cm.getNoteId( ContextManager.REQUEST_NOTE,
				     "required.roles");
	userNote=cm.getNoteId( ContextManager.REQUEST_NOTE,
			       "credentials.user");
	passwordNote=cm.getNoteId( ContextManager.REQUEST_NOTE,
				   "credentials.password");
    }


    public int authenticate(Request req, Response response) {
        String user = (String)req.getNote(userNote);
        String password = (String)req.getNote(passwordNote);
        if (user == null) return DECLINED;
        if (checkPassword(user, password)) {
            if (debug > 0) log("Auth ok, user=" + user);
            Context ctx = req.getContext();
            if (ctx != null)
                req.setAuthType(ctx.getAuthMethod());
            if (user != null) {
                req.setRemoteUser(user);
                req.setUserPrincipal( getPrincipal( user ));
                String userRoles[] = getUserRoles(user);
                req.setUserRoles(userRoles);
                return OK;
            }
        }
        return DECLINED;
    }

    private boolean checkPassword(String username,String credentials) {
        // Create the authentication search prepared statement if necessary
        // Perform the authentication search
        if (digest(credentials).equals(getCredentials(username))) {
            if (debug >= 2)
                log(sm.getString("jdbcRealm.authenticateSuccess", username));
            return true;
        }
        if (debug >= 2)
            log(sm.getString("jdbcRealm.authenticateFailure", username));
        return false;
    }
}
