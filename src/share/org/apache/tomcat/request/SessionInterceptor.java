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


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Will process the request and determine the session Id, and set it
 * in the Request.
 * It also marks the session as accessed.
 *
 * This implementation only handles Cookies sessions, please extend or
 * add new interceptors for other methods.
 *
 */
public class SessionInterceptor extends  BaseInterceptor implements RequestInterceptor {

    // GS, separates the session id from the jvm route
    static final char SESSIONID_ROUTE_SEP = '.';
    int debug=0;
    ContextManager cm;
    
    public SessionInterceptor() {
    }

    public void setDebug( int i ) {
	System.out.println("Set debug to " + i);
	debug=i;
    }
    
    public void setContextManager( ContextManager cm ) {
	this.cm=cm;
    }

    /** Extract the session id from the request.
     * SessionInterceptor will have to be called _before_ mapper,
     * to avoid coding session stuff inside the mapper.
     *
     * When we fix the interceptors we'll have to specify something
     * similar with the priority in apache hooks, right now it's just
     * a config issue.
     */
    public int contextMap(Request request ) {
	if( request.getRequestedSessionId() != null ) {
	    // probably Apache already did that for us
	    return 0;
	}

	// fix URL rewriting
	String sig=";jsessionid=";
	int foundAt=-1;
	String uri=request.getRequestURI();
	String sessionId;
	
	if ((foundAt=uri.indexOf(sig))!=-1){
	    sessionId=uri.substring(foundAt+sig.length()); // I hope the optimizer does it's job:-)
	    sessionId = fixSessionId( request, sessionId );
	    
	    // rewrite URL, do I need to do anything more?
	    request.setRequestURI(uri.substring(0, foundAt));

	    // No validate now - we just note that this is what the user
	    // requested. 
	    request.setSessionIdSource( Request.SESSIONID_FROM_URL);
	    request.setRequestedSessionId( sessionId );
	}
	return 0;
    }

    /** This happens after context map, so we know the context.
     *  We can probably do it later too.
     */
    public int requestMap(Request request ) {
	String sessionId = null;

	Cookie cookies[]=request.getCookies(); // assert !=null
	
	// Give priority to cookies. I don't know if that's part
	// of the spec - XXX
	for( int i=0; i<cookies.length; i++ ) {
	    Cookie cookie = cookies[i];
	    
	    if (cookie.getName().equals("JSESSIONID")) {
		sessionId = cookie.getValue();
		sessionId = fixSessionId( request, sessionId );

		// XXX what if we have multiple session cookies ?
		// right now only the first is used
		request.setRequestedSessionId( sessionId );
		request.setSessionIdSource( Request.SESSIONID_FROM_COOKIE);
		break;
	    }
	}

	// "access" it and set HttpSession if valid
	sessionId=request.getRequestedSessionId();
	validateSessionId(request, sessionId);

	return 0;
    }

    /** Fix the session id. If the session is not valid return null.
     *  It will also clean up the session from load-balancing strings.
     * @return sessionId, or null if not valid
     */
    private String fixSessionId(Request request, String sessionId){
	// GS, We piggyback the JVM id on top of the session cookie
	// Separate them ...

	if( debug>0 ) cm.log(" Orig sessionId  " + sessionId );
	if (null != sessionId) {
	    int idex = sessionId.lastIndexOf(SESSIONID_ROUTE_SEP);
	    if(idex > 0) {
		sessionId = sessionId.substring(0, idex);
	    }
	}
	return sessionId;
    }

    /** Validate and fix the session id. If the session is not valid return null.
     *  It will also clean up the session from load-balancing strings.
     * @return sessionId, or null if not valid
     */
    private void validateSessionId(Request request, String sessionId){

	if (sessionId != null && sessionId.length()!=0) {
	    // GS, We are in a problem here, we may actually get
	    // multiple Session cookies (one for the root
	    // context and one for the real context... or old session
	    // cookie. We must check for validity in the current context.
	    Context ctx=request.getContext();
	    SessionManager sM = ctx.getSessionManager();    
	    HttpSession sess= sM.findSession( sessionId );
	    if(null != sess) {
		sM.access( sess );
		request.setSession( sess );
	    }
	}
    }

    public int postService(  Request rrequest, Response response ) {
	Context ctx=rrequest.getContext();
	if( ctx==null ) return 0;
	SessionManager sm=ctx.getSessionManager();
	HttpSession sess=rrequest.getSession(false);
	if( sess == null ) return 0;
	sm.release( sess );
	return 0;
    }

    public int beforeBody( Request rrequest, Response response ) {
    	String reqSessionId = response.getSessionId();
	if( debug>0 ) cm.log("Before Body " + reqSessionId );
	if( reqSessionId==null)
	    return 0;

	
        // GS, set the path attribute to the cookie. This way
        // multiple session cookies can be used, one for each
        // context.
        String sessionPath = rrequest.getContext().getPath();
        if(sessionPath.length() == 0) {
            sessionPath = "/";
        }

        // GS, piggyback the jvm route on the session id.
        if(!sessionPath.equals("/")) {
            String jvmRoute = rrequest.getJvmRoute();
            if(null != jvmRoute) {
                reqSessionId = reqSessionId + SESSIONID_ROUTE_SEP + jvmRoute;
            }
        }

	Cookie cookie = new Cookie("JSESSIONID",
				   reqSessionId);
    	cookie.setMaxAge(-1);
        cookie.setPath(sessionPath);
    	cookie.setVersion(1);

	response.addHeader( CookieTools.getCookieHeaderName(cookie),
			    CookieTools.getCookieHeaderValue(cookie));
    	cookie.setVersion(0);
	response.addHeader( CookieTools.getCookieHeaderName(cookie),
			    CookieTools.getCookieHeaderValue(cookie));
	
    	return 0;
    }

    /** Init session management stuff for this context
     */
    public void contextInit(Context ctx) throws TomcatException {
	// Defaults !!
	if( ctx.getSessionManager() == null ) {
	    SessionManager sm=new org.apache.tomcat.session.StandardManager();
	    ctx.setSessionManager(sm);
	}

	SessionManager sm=ctx.getSessionManager();
	try {
	    sm.start();
	} catch(IllegalStateException ex ) {
	    throw new TomcatException( ex );
	}
    }
    
    /** Notification of context shutdown
     */
    public void contextShutdown( Context ctx )
	throws TomcatException
    {
	if( ctx.getDebug() > 0 ) ctx.log("Removing sessions from " + ctx );
	SessionManager sm=ctx.getSessionManager();
	try {
	    sm.stop();
	} catch(IllegalStateException ex ) {
	    throw new TomcatException( ex );
	}
    }

}
