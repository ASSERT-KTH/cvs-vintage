/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/HttpServletResponseFacade.java,v 1.3 2000/02/01 21:39:38 costin Exp $
 * $Revision: 1.3 $
 * $Date: 2000/02/01 21:39:38 $
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
import java.util.*;
import java.lang.IllegalArgumentException;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

public class HttpServletResponseFacade
implements HttpServletResponse {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private Response response;

    Response getRealResponse() {
	return response;
    }
    
    public HttpServletResponseFacade(Response response) {
        this.response = response;
    }

    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    public boolean containsHeader(String name) {
	return response.containsHeader(name);
    }

    public String encodeRedirectURL(String url) {
        // XXX
        // we don't support url rewriting yet!
        return url;
    }
    
    /**
     * @deprecated
     */
    
    public String encodeRedirectUrl(String location) {
	//try {
	//    URL url = new URL(location);
	//} catch (MalformedURLException e) {
	//    String msg = sm.getString("hsrf.redirect.iae")
        //
	//    throw new IllegalArgumentException(msg);
	//}
	
	return encodeRedirectURL(location);
    }

    public String encodeURL(String url) {
        // XXX
        // we don't support url rewriting yet!        
        return url;
    }
    
    /**
     * @deprecated
     */
    
    public String encodeUrl(String url) {
	return encodeURL(url);
    }
    
    public String getCharacterEncoding() {
	return response.getCharacterEncoding();
    }
    
    public ServletOutputStream getOutputStream() {
	return response.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
	return response.getWriter();
    }

    public void sendError(int sc) throws IOException {
	sendError(sc, "No detailed message");
    }
    
    public void sendError(int sc, String msg) throws IOException {
	setStatus( sc );
	Request request=response.getRequest();
	request.setAttribute("javax.servlet.error.status_code",
			     String.valueOf(sc));
	request.setAttribute("javax.servlet.error.message", msg);

	// XXX need to customize it
	Servlet errorP=new org.apache.tomcat.servlets.DefaultErrorPage();
	try {
	    errorP.service(request.getFacade(),this);
	} catch (ServletException ex ) {
	    // shouldn't happen!
	    ex.printStackTrace();
	}
    }

    public void sendRedirect(String location)
	throws IOException, IllegalArgumentException
    {
        if (location == null) {
            String msg = sm.getString("hsrf.redirect.iae");
            throw new IllegalArgumentException(msg);
	}
	sendError(HttpServletResponse.SC_MOVED_TEMPORARILY,
		  location);
    }
    
    public void setContentLength(int len) {
	response.setContentLength(len);
    }

    public void setContentType(String type) {
	response.setContentType(type);
    }

    public void setDateHeader(String name, long date) {
	response.setHeader(name, new HttpDate(date).toString());
    }

    public void addDateHeader(String name, long value) {
	response.addHeader(name, new HttpDate(value).toString());
    }
    
    public void setHeader(String name, String value) {
	response.setHeader(name, value);
    }

    public void addHeader(String name, String value) {
	response.addHeader(name, value);
    }
    
    public void setIntHeader(String name, int value) {
	response.setHeader(name, Integer.toString(value));
    }

    public void addIntHeader(String name, int value) {
        response.addHeader(name, Integer.toString(value));
    }
    
    public void setStatus(int sc) {
	response.setStatus(sc);
    }
    
    public void setBufferSize(int size) throws IllegalStateException {
	response.setBufferSize(size);
    }
    
    public int getBufferSize() {
	return response.getBufferSize();
    }
    
    public void reset() throws IllegalStateException {
	response.reset();
    }
    
    public boolean isCommitted() {
	return response.isBufferCommitted();
    }
    
    public void flushBuffer() throws IOException {
	response.flushBuffer();
    }
    
    public void setLocale(Locale loc) {
        response.setLocale(loc);
    }
    
    public Locale getLocale() {
	return response.getLocale();
    }

    /**
     *
     * @deprecated
     */
    public void setStatus(int sc, String msg) {
	response.setStatus(sc);
    }    
    
}
