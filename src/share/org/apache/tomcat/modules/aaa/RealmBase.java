/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/modules/aaa/RealmBase.java,v 1.5 2004/10/08 02:53:38 billbarker Exp $
 * $Revision: 1.5 $
 * $Date: 2004/10/08 02:53:38 $
 *
 *   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.aaa;

import java.io.UnsupportedEncodingException;
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
     * The encoding to use for password digesting.
     */
    protected String digestEncoding=null;

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
     * Get the encoding to use for digesting passwords.
     * If <code>null</code> then the System encoding is used.
     */
    public String getDigestEncoding() {
	return digestEncoding;
    }

    /**
     * Set the encoding to use for digesting passwords.
     * if <code>null</code> then the System encoding is used.
     */
    public void setDigestEncoding(String de) {
	digestEncoding = de;
    }

    /**
     * Digest password using the algorithm especificied and
     * convert the result to a corresponding hex string.
     * If exception, the plain credentials string is returned
     * @param credentials Password or other credentials to use in authenticating this username
     * @param algorithm Algorithm used to do the digest
     */
    public static final String digest(String credentials,String algorithm, String encoding ) {
        try {
            // Obtain a new message digest with MD5 encryption
            MessageDigest md = (MessageDigest)MessageDigest.getInstance(algorithm).clone();
            // encode the credentials
	    byte [] credBytes = null;
	    if(encoding != null) {
		credBytes = credentials.getBytes(encoding);
	    } else {
		credBytes = credentials.getBytes();
	    }
            md.update(credBytes);
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
                    System.out.println(digest(args[i], args[1], null));
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
            return digest(credentials,digest, digestEncoding);
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
