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
public class SecurityCheck extends  BaseInterceptor {
    
    public SecurityCheck() {
    }
	
    public int authenticate( Request req, Response response )
    {
	Context ctx=req.getContext();
	if( req.getRemoteUser() != null) return 0; // already authenticated

	String authMethod=ctx.getAuthMethod();
	if( authMethod==null || "BASIC".equals(authMethod) ) {
	    String authorization = req.getHeader("Authorization");
	    // XXX we may have multiple headers ?
	    if (authorization != null  &&   authorization.startsWith("Basic ")) {
		authorization = authorization.substring(6).trim();
		String unencoded=base64Decode( authorization );
		int colon = unencoded.indexOf(':');
		if( ctx.getDebug() > 0 ) ctx.log( "BASIC auth " + authorization + " " + unencoded );
		if (colon < 0)
		    return 0;
		String username = unencoded.substring(0, colon);
		String password = unencoded.substring(colon + 1);
		if( checkPassword( username, password ) ) {
		    req.setRemoteUser( username );
		    if( ctx.getDebug() > 0 ) ctx.log( "BASEIC Auth:  " + username );
		} else {
		    // wrong password
		    errorPage( req, response );
		}
	    }
	}

	if( "DIGEST".equals( authMethod ) ) {

	}

	if( "CLIENT-CERT".equals( authMethod ) ) {

	}
	if( "FORM".equals( authMethod ) ) {
	    HttpSession session=req.getSession( false );
	    if( session == null )
		return 0; // not authenticated
	    String username=(String)session.getAttribute( "j_username" );
	    String password=(String)session.getAttribute( "j_password" );
	    if( checkPassword( username, password ) ) {
		req.setRemoteUser( username );
		if( ctx.getDebug() > 0 ) ctx.log( "Form Auth:  " + username );
	    } else {
		// wrong password and user
		errorPage( req, response );
	    }
	    if( ctx.getDebug() > 0 ) ctx.log( "FORM auth " + username + " " + password );
	}

	return 0;
    }

    /** Wrong user/password
     */
    private int errorPage( Request req, Response response ) {
	System.out.println("Wrong user/password");
	return 0;
    }
    
    public int authorize( Request req, Response response )
    {
	Context ctx=req.getContext();

	String roles[]=req.getContainer().getRoles();
	if( roles==null ) {
	    return 0;
	}

	String user=req.getRemoteUser();
	if( user!=null ) {
	    if( ctx.getDebug() > 0 ) ctx.log( "Controled access for " + user );
	    for( int i=0; i< roles.length; i++ ) {
		if( userInRole( user, roles[i] ) )
		    return 0;
	    }
	}

	if( ctx.getDebug() > 0 ) ctx.log( "Unauthorized " );
	// redirect to the right servlet 
	// XXX hardcoded
	ServletWrapper authWrapper=ctx.getServletByName( "authServlet" );
	req.setWrapper( authWrapper );
	
 	return HttpServletResponse.SC_UNAUTHORIZED;
	// XXX check transport
    }

    static int base64[]= {
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 62, 64, 64, 64, 63,
	    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64, 64,
	    64,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
	    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 64, 64, 64, 64, 64,
	    64, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
	    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
	    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64
    };
    
    private String base64Decode( String orig ) {
	char chars[]=orig.toCharArray();
	StringBuffer sb=new StringBuffer();
	int i=0;

	int shift = 0;   // # of excess bits stored in accum
	int acc = 0;
	
	for (i=0; i<chars.length; i++) {
	    int v = base64[ chars[i] & 0xFF ];
	    
	    if ( v >= 64 ) {
		if( chars[i] != '=' )
		    System.out.println("Wrong char in base64: " + chars[i]);
	    } else {
		acc= ( acc << 6 ) | v;
		shift += 6;
		if ( shift >= 8 ) shift -= 8;
		sb.append( (char) ((acc >> shift) & 0xff));
	    }
	}
	return sb.toString();
    }

    private boolean checkPassword( String user, String pass ) {
	System.out.println("Checking " + user + " " + pass );
	return true;
    }

    private boolean userInRole( String user, String role ) {
	System.out.println("Checking role " + user + " " + role );
	return true;
    }
}
