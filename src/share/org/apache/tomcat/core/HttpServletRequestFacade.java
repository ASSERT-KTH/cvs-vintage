/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/HttpServletRequestFacade.java,v 1.7 2000/02/18 18:14:50 costin Exp $
 * $Revision: 1.7 $
 * $Date: 2000/02/18 18:14:50 $
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
import java.net.*;
import java.security.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * The facade to the request that a servlet will see.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */
public class HttpServletRequestFacade implements HttpServletRequest {

    private StringManager sm = StringManager.getManager(Constants.Package);
    private Request request;

    private boolean usingStream = false;
    private boolean usingReader = false;

    public Request getRealRequest() {
	// XXX In JDK1.2, call a security class to see if the code has
	// the right permission !!!
	return request;
    }
    
    public HttpServletRequestFacade(Request request) {
	// XXX In JDK1.2, call a security class to see if the code has
	// the right permission !!!
        this.request = request;
    }

    public Object getAttribute(String name) {
	return request.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
	return request.getAttributeNames();
    }

    public void setAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
	request.removeAttribute(name);
    }
    
    public String getAuthType() {
	return request.getAuthType();
    }
    
    public String getCharacterEncoding() {
	return request.getCharacterEncoding();
    }

    public int getContentLength() {
        return request.getContentLength();
    }

    public String getContentType() {
	return request.getContentType();
    }

    public Cookie[] getCookies() {
	return request.getCookies();
    }

    public long getDateHeader(String name) {
	String value=request.getHeader( name );
	if( value==null) return -1;
	
	long date=RequestUtil.toDate(value);
	if( date==-1) {
	    String msg = sm.getString("httpDate.pe", value);
	    throw new IllegalArgumentException(msg);
	}
	return date;
    }
    
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    public Enumeration getHeaders(String name) {
        return request.getHeaders(name);
    }

    public Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }
    
    public ServletInputStream getInputStream() throws IOException {
	if (usingReader) {
	    String msg = sm.getString("reqfac.getinstream.ise");
	    throw new IllegalStateException(msg);
	}

	usingStream = true;
	return request.getInputStream();
    }

    public int getIntHeader(String name)
	throws  NumberFormatException
    {
	String value=request.getHeader( name );
	if( value==null) return -1;
	int valueInt=Integer.parseInt(value);
	return valueInt;
    }
    
    public String getMethod() {
        return request.getMethod();
    }

    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null) {
            return values[0];
        } else {
	    return null;
        }
    }

    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    public Enumeration getParameterNames() {
        return request.getParameterNames();
    }
    
    public String getPathInfo() {
        return request.getPathInfo();
    }

    public String getPathTranslated() {
        return request.getPathTranslated();
    }
    
    public String getProtocol() {
        return request.getProtocol();
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public String getRemoteUser() {
	return request.getRemoteUser();
    }

    public String getScheme() {
        return request.getScheme();
    }

    public String getServerName() {
	return request.getServerName();
    }

    public int getServerPort() {
        return request.getServerPort();
    }

    public HttpSession getSession() {
        return request.getSession(true);
    }
    
    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    public BufferedReader getReader() throws IOException {
	if (usingStream) {
	    String msg = sm.getString("reqfac.getreader.ise");
	    throw new IllegalStateException(msg);
	}

	usingReader = true;
	return request.getReader();
    }
    
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }
    
    public String getRequestURI() {
        return request.getRequestURI();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        if (path == null)
	    return null;

	if (! path.startsWith("/")) {
	    path= FileUtil.catPath( request.getLookupPath(), path );
	    if( path==null) return null;
	}

	return request.getContext().getRequestDispatcher(path);
    }

    public boolean isSecure() {
	return request.isSecure();
    }

    public Locale getLocale() {
	return (Locale)getLocales().nextElement();
    }

    public Enumeration getLocales() {
        return RequestUtil.getLocales(this);
    }

    public String getContextPath() {
        return request.getContext().getPath();
    }

    public boolean isUserInRole(String role) {
	return request.isUserInRole(role);
    }

    public Principal getUserPrincipal() {
	return request.getUserPrincipal();
    }

    public String getServletPath() {
        return request.getServletPath();
    }

    /**
     * @deprecated
     */
    public String getRealPath(String name) {
        return request.getContext().getRealPath(name);
    }

    public boolean isRequestedSessionIdValid() {
	return request.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
	return request.isRequestedSessionIdFromCookie();
    }

    /**
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl() {
	return isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromURL() {
	return request.isRequestedSessionIdFromURL();
    }

}
