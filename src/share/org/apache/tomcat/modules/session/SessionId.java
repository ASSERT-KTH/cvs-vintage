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

package org.apache.tomcat.modules.session;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.http.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Extract the session ID from the request using cookies and
 * session rewriting.
 * 
 * Will process the request and determine the session Id, and set it
 * in the Request. It doesn't marks the session as accessed, and it
 * doesn't retrieve or set the HttpSession - the storage and management
 * of sessions is implemented in a separate module. 
 *
 * This interceptor doesn't deal with any of the Session internals -
 * it just works with the sessionID. A pluggable session manager 
 *  ( or user-space manager !) will deal with marking the session
 * as accessed or setting the session implementation and maintaining
 * lifecycles.
 *
 * This implementation only handles Cookies and URL rewriting sessions,
 * please extend or add new interceptors for other methods.
 * 
 * You can set this interceptor to not use cookies, but only rewriting.
 *
 * @author costin@eng.sun.com
 * @author Shai Fultheim [shai@brm.com]
 */
public class SessionId extends  BaseInterceptor
{
    // GS, separates the session id from the jvm route
    static final char SESSIONID_ROUTE_SEP = '.';
    ContextManager cm;
    boolean noCookies=false;
    boolean cookiesFirst=true;
    
    public SessionId() {
    }

    public void setCookiesFirst( boolean b ) {
	cookiesFirst=b;
    }

    public void setNoCookies(boolean noCookies) {
        this.noCookies = noCookies;
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

	// quick test: if no extra path, no url rewriting.
	if( request.requestURI().indexOf( ';' ) < 0 )
	    return 0;
	
	// In case URI rewriting is used, extract the uri and fix
	// the request.
	String sig=";jsessionid=";
	int foundAt=-1;
	String uri=request.requestURI().toString();
	String sessionId;
	
	if ((foundAt=uri.indexOf(sig))!=-1){
	    sessionId=uri.substring(foundAt+sig.length());
	    // rewrite URL, do I need to do anything more?
	    request.requestURI().setString(uri.substring(0, foundAt));

	    // No validate now - we just note that this is what the user
	    // requested. 
	    request.setSessionIdSource( Request.SESSIONID_FROM_URL);
	    request.setRequestedSessionId( sessionId );
	}
	return 0;
    }

    /**
     *  Extract and set the session id and ServerSession.
     *  We know the Context - and all local interceptors can be used
     *  ( like session managers that are set per context ).
     *
     *  This module knows about URI and cookies. It will validate the
     *  session id ( and set it only if valid ), and "touch" the 
     *  session.
     */
    public int requestMap(Request request ) {
	String sessionId = null;
	Context ctx=request.getContext();
	if( ctx==null ) {
	    log( "Configuration error in StandardSessionInterceptor " +
		 " - no context " + request );
	    return 0;
	}

	int count=request.getCookies().getCookieCount();

	ServerSession sess=null;

	if( ! cookiesFirst  ) {
	    // try the information from URL rewriting
	    sessionId= request.getRequestedSessionId();
	    sess=processSession( request, sessionId,
			      request.getSessionIdSource() );
	    if( sess!=null )
		return 0;
	}
	
	// Give priority to cookies. I don't know if that's part
	// of the spec - XXX
	for( int i=0; i<count; i++ ) {
	    ServerCookie cookie = request.getCookies().getCookie(i);
	    
	    if (cookie.getName().equals("JSESSIONID")) {
		sessionId = cookie.getValue().toString();
		if (debug > 0) log("Found session id cookie " +
				   sessionId);
		// 3.2 - PF ( pfrieden@dChain.com ): check the session id from
		// cookies for validity
		sess=processSession( request, sessionId,
				  Request.SESSIONID_FROM_COOKIE );
		if (sess != null) {
		    break;
		}
	    }
	}

	if( sess==null ) {
	    // try the information from URL rewriting
	    sessionId= request.getRequestedSessionId();
	    sess=processSession( request, sessionId,
			      request.getSessionIdSource() );
	    if( sess!=null )
		return 0;
	}

 	return 0;
    }


    /** Find the session, set session id, source, mark it as accessed.
     */
    private ServerSession processSession(Request request,
			     String sessionId, String source )
    {
	BaseInterceptor reqI[]= request.getContainer().
	    getInterceptors(Container.H_findSession);
	
	ServerSession sess=null;
	for( int i=0; i< reqI.length; i++ ) {
	    sess=reqI[i].findSession( request,
				      sessionId,  false );
	    if( sess!=null ) break;
	}

	if (sess != null) {
	    request.setRequestedSessionId( sessionId );
	    request.setSessionIdSource( source );
	    // since we verified this sessionID, we can also set
	    // it and adjust the session
	    request.setSession( sess );
	    request.setSessionId( sessionId );
	    
	    sess.touch( System.currentTimeMillis() );

	    // if the session was NEW ( never accessed - change it's state )
	    if( sess.getState() == ServerSession.STATE_NEW ) {
		sess.setState( ServerSession.STATE_ACCESSED, request);
	    }
	}
	return sess;
    }
    
//     /** Fix the session id. If the session is not valid return null.
//      *  It will also clean up the session from load-balancing strings.
//      * @return sessionId, or null if not valid
//      */
//     private String fixSessionId(Request request, String sessionId){
// 	// GS, We piggyback the JVM id on top of the session cookie
// 	// Separate them ...

// 	if( debug>0 ) cm.log(" Orig sessionId  " + sessionId );
// 	if (null != sessionId) {
// 	    int idex = sessionId.lastIndexOf(SESSIONID_ROUTE_SEP);
// 	    if(idex > 0) {
// 		sessionId = sessionId.substring(0, idex);
// 	    }
// 	}
// 	return sessionId;
//     }

    public int beforeBody( Request rrequest, Response response ) {
    	String reqSessionId = rrequest.getSessionId();
	if( debug>0 ) cm.log("Before Body " + reqSessionId );
	if( reqSessionId==null)
	    return 0;
        if (noCookies)
            return 0;
	if( reqSessionId.equals( rrequest.getRequestedSessionId() )) {
	    // we are already in a session - no need to
	    // send the Set-Cookie again ( plus it's annoying if
	    // the user doesn't want sessions but rewriting )
	    //	    log( "We are in a session already ");
	    return 0;
	}
	
        // GS, set the path attribute to the cookie. This way
        // multiple session cookies can be used, one for each
        // context.
        String sessionPath = rrequest.getContext().getPath();
        if(sessionPath.length() == 0) {
            sessionPath = "/";
        }

//         // GS, piggyback the jvm route on the session id.
//  //        if(!sessionPath.equals("/")) {
//             String jvmRoute = rrequest.getJvmRoute();
//             if(null != jvmRoute) {
//                 reqSessionId = reqSessionId + SESSIONID_ROUTE_SEP + jvmRoute;
//             }
//  //     }

	// We'll use a Netscape cookie for sessions - it's
	// the only one supported by all browsers
	StringBuffer buf = new StringBuffer();
	buf.append( "JSESSIONID=" ).append( reqSessionId );
	buf.append( ";Path=" ).append(  sessionPath  );
	response.addHeader( "Set-Cookie",
			    buf.toString());
	
    	return 0;
    }

}
