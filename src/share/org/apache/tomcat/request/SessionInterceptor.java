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

    public SessionInterceptor() {
    }

    public int requestMap(Request request ) {
	String sessionId = null;
	
	Cookie cookies[]=request.getCookies(); // assert !=null
	
	for( int i=0; i<cookies.length; i++ ) {
	    Cookie cookie = cookies[i];
	    
	    if (cookie.getName().equals("JSESSIONID")) {
		sessionId = cookie.getValue();
		sessionId=validateSessionId(request, sessionId);
		if (sessionId!=null){
		    request.setRequestedSessionIdFromCookie(true);
		}
	    }
	}
	
	String sig=";jsessionid=";
	int foundAt=-1;
	if ((foundAt=request.getRequestURI().indexOf(sig))!=-1){
	    sessionId=request.getRequestURI().substring(foundAt+sig.length());
	    // rewrite URL, do I need to do anything more?
	    request.setRequestURI(request.getRequestURI().substring(0, foundAt));
	    sessionId=validateSessionId(request, sessionId);
	    if (sessionId!=null){
		request.setRequestedSessionIdFromURL(true);
	    }
	}
	return 0;
    }

    // XXX what is the correct behavior if the session is invalid ?
    // We may still set it and just return session invalid.
    
    /** Validate and fix the session id. If the session is not valid return null.
     *  It will also clean up the session from load-balancing strings.
     * @return sessionId, or null if not valid
     */
    private String validateSessionId(Request request, String sessionId){
      // GS, We piggyback the JVM id on top of the session cookie
      // Separate them ...
      if (null != sessionId) {
        int idex = sessionId.lastIndexOf(SESSIONID_ROUTE_SEP);
        if(idex > 0) {
         sessionId = sessionId.substring(0, idex);
       }
      }
      
      if (sessionId != null && sessionId.length()!=0) {
       // GS, We are in a problem here, we may actually get
       // multiple Session cookies (one for the root
       // context and one for the real context... or old session
       // cookie. We must check for validity in the current context.
       Context ctx=request.getContext();
       SessionManager sM = ctx.getSessionManager();    
       if(null != sM.findSession(ctx, sessionId)) {
         sM.accessed(ctx, request, sessionId );
         request.setRequestedSessionId(sessionId);
         return sessionId;
       }
      }
      return null;
    }
  


    public int beforeBody( Request rrequest, Response response ) {
    	String reqSessionId = response.getSessionId();
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


    /** Notification of context shutdown
     */
    public void contextShutdown( Context ctx )
	throws TomcatException
    {
	if( ctx.getDebug() > 0 ) ctx.log("Removing sessions from " + ctx );
	ctx.getSessionManager().removeSessions(ctx);
    }


}
