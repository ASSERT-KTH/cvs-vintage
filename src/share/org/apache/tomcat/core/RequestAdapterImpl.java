/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/RequestAdapterImpl.java,v 1.1 1999/10/24 17:21:20 costin Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/24 17:21:20 $
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


package org.apache.tomcat.core;

import org.apache.tomcat.util.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * "Default" implementationf for RequstAdapter - use setters to put the information
 *  in.
 */
public class RequestAdapterImpl implements  RequestAdapter {
    protected String scheme;
    protected String method;
    protected String requestURI;
    protected String protocol;
    protected MimeHeaders headers;
    ServletInputStream in;
    
    String serverName;
    int serverPort;
    protected String remoteAddr;
    protected String remoteHost;

    
    public RequestAdapterImpl() {
	headers = new MimeHeaders();
	recycle(); // XXX need better placement-super()
	// will call it too many times

	// XXX a good place to add more 1.2 security
	// here we can check whe creates the Request -
	// it shouldn't be permited for untrusted servlets
	// ( little performance impact - requestAdapter is reused)
    }
    
    // Required - basic request fields

    public String getScheme() {
        return scheme;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getRequestURI() {
        return requestURI;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    public Enumeration getHeaderNames() {
        return headers.names();
    }
    
    public ServletInputStream getInputStream()
    throws IOException {
    	return in;    
    }

    public String getServerName() {
	return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public String getRemoteHost() {
	return remoteHost;
    }    
    
    // -------------------- "cooked" info --------------------
    // Hints = return null if you don't know,
    // and Tom will find the value. You can also use the static
    // methods in RequestImpl

    /** Return the parsed Cookies
     */
    public String[] getCookieHeaders() {
	return null;
    }

    // server may have it pre-calculated - return null if
    // it doesn't
    public String getContextPath() {
	return null;
    }

    // What's between context path and servlet name ( /servlet )
    // A smart server may use arbitrary prefixes and rewriting
    public String getServletPrefix() {
	return null;
    }

    // Servlet name ( a smart server may use aliases and rewriting !!! )
    public String getServletName() {
	return null;
    }

    // What's after Servlet name, before "?"
    public String getPathInfo() {
	return null;
    }

    public String getQueryString() {
	return null;
    }

    // Parameters - if the server can parse parameters faster
    // Note: there is a tricky requirement in Servlet API
    // regarding POST parameters ( you can't read the body until you are
    // asked for the first parameter ). It's easy if you use callbacks (
    // this method will be called when needed, but don't read until you are
    // asked for params )
    public Enumeration getParameterNames() {
	return null;
    }

    public String[] getParameterValues(String name) {
	return null;
    }

    // You probably know the session ID if you are in a distributed engine, or
    // if you use a non-standard session identifier ( to integrate with "legacy"
    // apps ?)
    public String getRequestedSessionId() {
	return null;
    }

    // Authentication - carefull :-)
    public String getAuthType() {
	return null;
    }

    public String getRemoteUser() {
	return null;
    }

    // Headers and special Headers. You may want to use a "fast" route for those,
    // since it's almost sure will be needed ( for example get "other" headers in
    // a callback.). Again - if you don't know, return null.
    
    public String getCharacterEncoding() {
	return null;
    }

    public int getContentLength() {
	return -1;
    }
    
    public String getContentType() {
	return null;
    }

    // You're no longer needed, go away
    public  void recycle() {
	scheme = "http";// no need to use Constants
	method = "GET";
	requestURI="/";
	protocol="HTTP/1.0";
	headers.clear(); // XXX use recycle pattern
	serverName="localhost";
	serverPort=8080;

	// XXX a request need to override those if it cares
	// about security
	remoteAddr="127.0.0.1";
	remoteHost="localhost";
    }


    // -------------------- Setters - not part of RequestAdapter interface

    
    
}
