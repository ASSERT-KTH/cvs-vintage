/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/security/file/Attic/FileRealmConnector.java,v 1.1 1999/10/18 03:34:04 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/18 03:34:04 $
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


package org.apache.tomcat.security.file;


import java.security.Principal;
import java.util.Enumeration;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.security.RealmConnector;


/**
 * Implementation of <code>RealmConnector</code> that uses a
 * <code>FileRealmDatabase</code> object as the cache to an underlying
 * XML-stored database of users and their associated roles.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 1999/10/18 03:34:04 $
 */

public final class FileRealmConnector
    implements RealmConnector {


    /**
     * The Context with which we are associated.
     */
    private Context context = null;


    /**
     * The cache object containing our database information.
     */
    private FileRealmDatabase database = null;


    /**
     * No-arguments constructor so that an instance of this class can be
     * instantiated dynamically.
     */
    public FileRealmConnector() {
    }


    /**
     * Returns the Principal associated with the specified username and
     * credentials, if there is one, or <code>null</code> otherwise.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *	authenticating this username (XXX: Is a String sufficient
     *  for all possible cases?)
     *
     * @exception IllegalStateException if called before <code>start()</code>
     *  has been called, or after <code>stop()</code> has been called
     */
    public Principal authenticate(String username, String credentials) {

	if (context == null) {
	    // XXX Internationalization
	    throw new IllegalStateException("FileRealmConnector:  " +
					    "Not started");
	}

	FileRealmUser user = database.getUser(username);
	if (user == null)
	    return (null);
	if (user.authenticate(credentials))
	    return ((Principal) user);
	else
	    return (null);

    }


    /**
     * Returns <code>true</code> if the specified Principal has been
     * granted the specified role in this realm, or <code>false</code>
     * otherwise.
     *
     * @param principal Principal whose access rights are to be tested
     * @param role Role to test for
     *
     * @exception IllegalArgumentException if the specified principal
     *  is not associated with this realm
     * @exception IllegalStateException if called before <code>start()</code>
     *  has been called, or after <code>stop()</code> has been called
     */
    public boolean hasRole(Principal principal, String role) {

	if (context == null) {
	    // XXX Internationalization
	    throw new IllegalStateException("FileRealmConnector:  " +
					    "Not started");
	}

	FileRealmUser user = database.getUser(principal.getName());
	if (user == null)
	    return (false);
	if (user.hasRole(role))
	    return (true);
	Enumeration groups = user.getGroups();
	while (groups.hasMoreElements()) {
	    FileRealmGroup group = (FileRealmGroup) groups.nextElement();
	    if (group.hasRole(role))
		return (true);
	}
	return (false);
    }


    /**
     * Prepares this RealmConnector for use in association with the specified
     * Context.  This method must be called prior to calling any of the
     * information lookup methods.
     *
     * @param context The Context with which this RealmConnector is associated
     *
     * XXX Should we support some formal exception to report initialization
     * problems?
     */
    public void start(Context context) {

	this.context = context;
	// XXX create and initialize the database object

    }


    /**
     * Tells this RealmConnector that it will no longer be used for information
     * lookup, so it can release any resources that were allocated in the
     * <code>start()</code> method.
     */
    public void stop() {

	this.context = null;
	this.database = null;

    }


}
