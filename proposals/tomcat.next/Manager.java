/*
 * $Header: /tmp/cvs-vintage/tomcat/proposals/tomcat.next/Attic/Manager.java,v 1.1 2000/01/08 03:54:03 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/08 03:54:03 $
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


// package org.apache.tomcat;


import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;


/**
 * A <b>Manager</b> manages the pool of Sessions that are associated with a
 * particular Container.  Different Manager implementations may support
 * value-added features such as the persistent storage of session data,
 * as well as migrating sessions for distributable web applications.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/08 03:54:03 $
 */

public interface Manager {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer();


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container);


    /**
     * Return the distributable flag for the sessions supported by
     * this Manager.
     */
    public boolean getDistributable();


    /**
     * Set the distributable flag for the sessions supported by this
     * Manager.  If this flag is set, all user data objects added to
     * sessions associated with this manager must implement Serializable.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable);


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     */
    public int getMaxInactiveInterval();


    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    public void setMaxInactiveInterval(int interval);


    // --------------------------------------------------------- Public Methods


    /**
     * Construct and return an HTTP Cookie object that contains the specified
     * session id.  The characteristics of this cookie are based on the
     * cookie-related properties of this Manager.  If a cookie
     * cannot be created for any reason, return <code>null</code>.
     *
     * @param req The servlet request asking for this cookie to be created
     * @param id The session id for which a cookie should be constructed
     */
    public Cookie createCookie(Request req, String id);


    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession();


    /**
     * Encode the specified session id into the specified redirect URL,
     * if it is an absolute URL that returns to the specified host name
     * (presumably the host name on which this request was received).
     * If URL rewriting is disabled or unnecessary, the specified URL
     * will be returned unchanged.
     *
     * @param id The session id to be encoded
     * @param url The URL to be encoded with the session id
     * @param hostname The host name on which the associated request
     *	was received
     *
     * @exception IllegalArgumentException if the specified URL is
     *	not absolute
     */
    public String encodeRedirectURL(String id, String url, String hostname);


    /**
     * Encode the specified session id into the specified URL,
     * if it is a relative URL or an absolute URL that returns to the
     * specified host name (presumably the host name on which this request
     * was received).  If URL rewriting is disabled or unnecessary,
     * the specified URL will be returned unchanged.
     *
     * @param id The session id to be encoded
     * @param url The URL to be encoded with the session id
     * @param hostname The host name on which the associated request
     *  was received
     */
    public String encodeURL(String id, String url, String hostname);


    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     *  while processing this request
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id) throws IOException;


    /**
     * Return the set of active Sessions associated with this Manager.
     * If this Manager has no active Sessions, a zero-length array is returned.
     */
    public Session[] findSessions();


    /**
     * Return the session id from the specified array of cookies,
     * where the session id cookie was presumably created by the
     * <code>createCookie()</code> method of this Manager.
     * If there is no session id cookie included, return <code>null</code>.
     *
     * @param cookies Array of cookies from which to extract the session id
     */
    public String parseSessionId(Cookie cookies[]);


    /**
     * Return the session id from the specified request URI, where
     * it was presumably encoded via the <code>encodeRedirectURL()</code> or
     * <code>encodeURL()</code> method of this Manager.
     * If there is no session id included, return <code>null</code>.
     *
     * @param uri The request URI from which to extract the session id
     */
    public String parseSessionId(String uri);


}
