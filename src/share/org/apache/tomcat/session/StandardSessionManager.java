/*
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


package org.apache.tomcat.session;

import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.catalina.*;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.SessionManager;
import org.apache.tomcat.util.SessionUtil;


/**
 * Specialized implementation of org.apache.tomcat.core.SessionManager
 * that adapts to the new component-based Manager implementation.
 * <p>
 * XXX - At present, use of <code>StandardManager</code> is hard coded,
 * and lifecycle configuration is not supported.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  Once we commit to the new Manager/Session
 * paradigm, I would suggest moving the logic implemented here back into
 * the core level.  The Tomcat.Next "Manager" interface acts more like a
 * collection class, and has minimal knowledge of the detailed request
 * processing semantics of handling sessions.
 * <p>
 * XXX - At present, there is no way (via the SessionManager interface) for
 * a Context to tell the Manager that we create what the default session
 * timeout for this web application (specified in the deployment descriptor)
 * should be.
 *
 * @author Craig R. McClanahan
 */

public final class StandardSessionManager
    implements SessionManager {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new SessionManager that adapts to the corresponding Manager
     * implementation.
     */
    public StandardSessionManager() {

	manager = new StandardManager();
	if (manager instanceof Lifecycle) {
	    try {
		((Lifecycle) manager).configure(null);
		((Lifecycle) manager).start();
	    } catch (LifecycleException e) {
		throw new IllegalStateException("" + e);
	    }
	}

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Manager implementation we are actually using.
     */
    private Manager manager = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Mark the specified session's last accessed time.  This should be
     * called for each request by a RequestInterceptor.
     *
     * @param session The session to be marked
     */
    public void accessed(Context ctx, Request req, String id) {
	HttpSession session=findSession(ctx, id);
	if( session == null) return;
	if (session instanceof Session)
	    ((Session) session).access();

	// cache the HttpSession - avoid another find
	req.setSession( session );
    }

    // XXX should we throw exception or just return null ??
    public HttpSession findSession( Context ctx, String id ) {
	try {
	    Session session = manager.findSession(id);
	    if(session!=null)
		return session.getSession();
	} catch (IOException e) {
	}
	return (null);
    }

    public HttpSession createSession(Context ctx) {
	return  manager.createSession().getSession();
    }

    /**
     * Remove all sessions because our associated Context is being shut down.
     *
     * @param ctx The context that is being shut down
     */
    public void removeSessions(Context ctx) {

	// XXX XXX a manager may be shared by multiple
	// contexts, we just want to remove the sessions of ctx!
	// The manager will still run after that ( i.e. keep database
	// connection open
	if (manager instanceof Lifecycle) {
	    try {
		((Lifecycle) manager).stop();
	    } catch (LifecycleException e) {
		throw new IllegalStateException("" + e);
	    }
	}

    }

    /**
     * Used by context to configure the session manager's inactivity timeout.
     *
     * The SessionManager may have some default session time out, the
     * Context on the other hand has it's timeout set by the deployment
     * descriptor (web.xml). This method lets the Context conforgure the
     * session manager according to this value.
     *
     * @param minutes The session inactivity timeout in minutes.
     */
    public void setSessionTimeOut(int minutes) {
        if(-1 != minutes) {
            // The manager works with seconds...
            manager.setMaxInactiveInterval(minutes * 60);
        }
    }
}
