/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/security/Attic/RealmConnector.java,v 1.2 1999/10/29 06:13:15 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/29 06:13:15 $
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


package org.apache.tomcat.security;

import java.security.Principal;

import org.apache.tomcat.core.Context;


/**
 * Generalized interface for a connector between a web application's
 * Context object and the security domain (realm) used to authenticate
 * users and identify their associated roles for access control purposes.
 * <p>
 * A specific implementation of this interface will be selected when
 * the Context is configured (usually by passing the name of the desired
 * implementation class as a configuration parameter).  The Context should
 * obey the following rules when dealing with a RealmConnector:
 * <ul>
 * <li>A single RealmConnector object should be associated with one,
 *     and only one, Context object.
 * <li>When the Context itself is being initialized, call the
 *     <code>start()</code> method of the RealmConnector.
 * <li>While the Context is processing requests, it can call the information
 *     lookup methods (<code>authenticate()</code> and <code>hasRole()</code>)
 *     as often as required, from multiple simultaneous threads if necessary.
 *     Any required synchronization of access to the underlying Realm is the
 *     responsibility of the RealmConnector implementation.
 * <li>When the Context itself is being finalized, call the
 *     <code>stop()</code> method of the RealmConnector.
 * </ul>
 *
 * This interface is based on the <code>RequestSecurityProvider</code>
 * interface that was present in the Tomcat code originally released
 * to the Jakarta project.  NOTE:  Because a RealmConnector is no longer
 * sensitive to which authentication mechanism is being used, the
 * <code>isSecure()</code> method has been removed, and an HttpServletRequest
 * is no longer passed as an argument.
 * 
 * @author Harish Prabandham
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 1999/10/29 06:13:15 $
 */

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//

public interface RealmConnector {


    /**
     * Returns the Principal associated with the specified username and
     * credentials, if there is one, or <code>null</code> otherwise.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *	authenticating this username
     *
     * @exception IllegalStateException if called before <code>start()</code>
     *  has been called, or after <code>stop()</code> has been called
     */
    public Principal authenticate(String username, String credentials);


    /**
     * Returns the Principal associated with the specified username and
     * credentials, if there is one, or <code>null</code> otherwise.
     *
     * @param username Username of the Principal to look up
     * @param credentials Credentials to use in authenticating this username
     *
     * @exception IllegalStateException if called before <code>start()</code>
     *  has been called, or after <code>stop()</code> has been called
     */
    public Principal authenticate(String username, byte[] credentials);


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
    public boolean hasRole(Principal principal, String role);


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
    public void start(Context context);


    /**
     * Tells this RealmConnector that it will no longer be used for information
     * lookup, so it can release any resources that were allocated in the
     * <code>start()</code> method.
     */
    public void stop();


}
