/*
 * $Header: /tmp/cvs-vintage/tomcat/proposals/tomcat.next/Attic/ManagerBase.java,v 1.1 2000/01/08 03:54:03 craigmcc Exp $
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


// package org.apache.tomcat.session.standard;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;


/**
 * Minimal implementation of the <b>Manager</b> interface that supports
 * no session persistence or distributable capabilities.  This class may
 * be subclassed to create more sophisticated Manager implementations.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/08 03:54:03 $
 */

public class ManagerBase implements Manager {


    // ----------------------------------------------------- Instance Variables


    /**
     * The name of the cookie used for session identifier maintenance.  This
     * value is required by the Java Servlet Specification, version 2.2.
     */
    protected static final String COOKIE_NAME = "JSESSIONID";


    /**
     * The name of the URL parameter used for session identifier encoding.
     * This value is required by the Java Servlet Specification, version 2.2.
     */
    protected static final String PARAMETER_NAME = "jsessionid";


    /**
     * The Container with which this Manager is associated.
     */
    protected Container container;


    /**
     * Private counter used in generating session identifiers.
     */
    protected int counter = 1010;


    /**
     * The distributable flag for Sessions created by this Manager.  If this
     * flag is set to <code>true</code>, any user attributes added to a
     * session must be Serializable.
     */
    protected boolean distributable;


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info = "ManagerBase/1.0";


    /**
     * The default maximum inactive interval for Sessions created by
     * this Manager.
     */
    protected int maxInactiveInterval = -1;


    /**
     * The set of previously recycled Sessions for this Manager.
     */
    protected Vector recycled = new Vector();


    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected Hashtable sessions = new Hashtable();


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer() {

	return (this.container);

    }


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container) {

	this.container = container;

    }


    /**
     * Return the distributable flag for the sessions supported by
     * this Manager.
     */
    public boolean getDistributable() {

	return (this.distributable);

    }


    /**
     * Set the distributable flag for the sessions supported by this
     * Manager.  If this flag is set, all user data objects added to
     * sessions associated with this manager must implement Serializable.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {

	this.distributable = distributable;

    }


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (this.info);

    }


    /**
     * Return the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     */
    public int getMaxInactiveInterval() {

	return (this.maxInactiveInterval);

    }


    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    public void setMaxInactiveInterval(int interval) {

	this.maxInactiveInterval = interval;

    }


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
    public Cookie createCookie(Request req, String id) {

	Cookie cookie = new Cookie(COOKIE_NAME, id);
	String serverName = req.getRequest().getServerName();
	if (serverName != null)
	    cookie.setDomain(serverName);
	String contextPath = req.getRequest().getContextPath();
	if ((contextPath != null) && (contextPath.length() > 0))
	    cookie.setPath(contextPath);
	cookie.setMaxAge(-1);
	return (cookie);

    }


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
    public Session createSession() {

	// Recycle or create a Session instance
	StandardSession session = null;
	synchronized (recycled) {
	    int size = recycled.size();
	    if (size > 0) {
		session = (StandardSession) recycled.elementAt(size - 1);
		recycled.removeElementAt(size - 1);
	    }
	}
	if (session == null)
	    session = new StandardSession(this);

	// Initialize the properties of the new session and return it
	session.setNew(true);
	session.setValid(true);
	session.setCreationTime(System.currentTimeMillis());
	session.setLastAccessedTime(session.getCreationTime());
	session.setMaxInactiveInterval(this.maxInactiveInterval);
	session.setId(generate());

	return (session);

    }


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
    public String encodeRedirectURL(String id, String url, String hostname) {

	// FIXME: Are the rules really the same?
	return (encodeURL(id, url, hostname));

    }


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
    public String encodeURL(String id, String url, String hostname) {

	// Encode all relative URLs unless they start with a hash
	if (!url.startsWith("http:")) {
	    if (!url.startsWith("#"))
		return (encode(id, url));
	    else
		return (url);
	}

	// Encode all absolute URLs that return to this hostname
	String match = "http://" + hostname;
	if (url.startsWith("http://" + hostname))
	    return (encode(id, url));
	else
	    return (url);

    }


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
    public Session findSession(String id) throws IOException {

	return ((Session) sessions.get(id));

    }


    /**
     * Return the set of active Sessions associated with this Manager.
     * If this Manager has no active Sessions, a zero-length array is returned.
     */
    public Session[] findSessions() {

	Session sessions[] = null;
	synchronized (sessions) {
	    Vector keys = new Vector();
	    Enumeration ids = this.sessions.keys();
	    while (ids.hasMoreElements()) {
		String id = (String) ids.nextElement();
		keys.addElement(id);
	    }
	    sessions = new Session[keys.size()];
	    for (int i = 0; i < sessions.length; i++) {
		String key = (String) keys.elementAt(i);
		sessions[i] = (Session) this.sessions.get(key);
	    }
	}
	return (sessions);

    }


    /**
     * Return the session id from the specified array of cookies,
     * where the session id cookie was presumably created by the
     * <code>createCookie()</code> method of this Manager.
     * If there is no session id cookie included, return <code>null</code>.
     *
     * @param cookies Array of cookies from which to extract the session id
     */
    public String parseSessionId(Cookie cookies[]) {

	if (cookies == null)
	    return (null);
	for (int i = 0; i < cookies.length; i++) {
	    if (COOKIE_NAME.equals(cookies[i].getName()))
		return (cookies[i].getValue());
	}
	return (null);

    }


    /**
     * Return the session id from the specified request URI, where
     * it was presumably encoded via the <code>encodeRedirectURL()</code> or
     * <code>encodeURL()</code> method of this Manager.
     * If there is no session id included, return <code>null</code>.
     *
     * @param uri The request URI from which to extract the session id
     */
    public String parseSessionId(String uri) {

	// Search for the required match string in the URI
	String match = ";" + PARAMETER_NAME + "=";
	int m = uri.indexOf(match);
	if (m < 0)
	    return (null);

	// Parse the session identifier
	String temp = uri.substring(m + match.length());
	int s = temp.indexOf(';');
	int q = temp.indexOf('?');
	if (s < 0) {
	    if (q < 0)
		return (temp);
	    else
		return (temp.substring(0, q));
	} else {
	    if (q < 0)
		return (temp.substring(0, s));
	    else if (q < s)
		return (temp.substring(0, q));
	    else
		return (temp.substring(0, s));
	}

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Add this Session to the set of active Sessions for this Manager.
     *
     * @param session Session to be added
     */
    void add(StandardSession session) {

	sessions.put(session.getId(), session);

    }


    /**
     * Add this Session to the recycle collection for this Manager.
     *
     * @param session Session to be recycled
     */
    void recycle(StandardSession session) {

	recycled.addElement(session);

    }


    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    void remove(StandardSession session) {

	sessions.remove(session.getId());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Encode the specified session identifier into the specified URL,
     * and return the resulting string.
     *
     * @param id The session id to be encoded
     * @param url The URL to be encoded with the session id
     */
    protected String encode(String id, String url) {

	if ((id == null) || (url == null))
	    return (url);

	// Locate the beginning of the query string (if any)
	int question = url.indexOf('?');

	// Encode the session identifier appropriately (before any query)
	StringBuffer buf = new StringBuffer();
	if (question < 0)
	    buf.append(url);
	else
	    buf.append(url.substring(0, question));
	buf.append(';');
	buf.append(PARAMETER_NAME);
	buf.append('=');
	buf.append(id);
	if (question >= 0)
	    buf.append(url.substring(question));
	return (buf.toString());

    }


    /**
     * Generate and return a new session identifier.
     *
     * <b>IMPLEMENTATION NOTE</b>:  Copied from the original code in
     * org.apache.tomcat.util.SessionIdGenerator.  This implementation
     * is not at all sophisticated or secure.
     */
    protected synchronized String generate() {

	Integer i = new Integer(counter++);
	StringBuffer buf = new StringBuffer();
	String dString = Double.toString(Math.abs(Math.random()));

	buf.append("To");
	buf.append(i);
	buf.append("mC");
	buf.append(dString.substring(2));
	buf.append("At");

	return (buf.toString());

    }


}
