/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Response.java,v 1.1 1999/10/09 00:30:16 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:30:16 $
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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */

public abstract class Response {
    
    protected Request request;
    protected HttpServletResponseFacade responseFacade;
    protected Vector userCookies = new Vector();
    protected Vector systemCookies = new Vector();
    protected String contentType = Constants.ContentType.Default;
    protected String contentLanguage = null;
    protected String characterEncoding =
        System.getProperty("file.encoding",
            Constants.CharacterEncoding.Default);
    protected int contentLength = -1;
    protected int status = 200; 
    private Locale locale = new Locale(Constants.Locale.Default, "");
    
    public Response() {
        responseFacade = new HttpServletResponseFacade(this);
    }
    
    public HttpServletResponseFacade getFacade() {
	return responseFacade;
    }

    // XXX - public so this can be called from ConnectionHandler.java

    public void setRequest(Request request) {
	this.request = request;
    }

    public void reset() throws IllegalStateException {
	userCookies.removeAllElements();  // keep system (session) cookies
	contentType = Constants.ContentType.Default;
        locale = new Locale(Constants.Locale.Default, "");
	characterEncoding = System.getProperty("file.encoding",
            Constants.CharacterEncoding.Default);
	contentLength = -1;
	status = 200;
    }

    public void recycle() {
	userCookies.removeAllElements();
	systemCookies.removeAllElements();
	contentType = Constants.ContentType.Default;
        locale = new Locale(Constants.Locale.Default, "");
	characterEncoding = System.getProperty("file.encoding",
            Constants.CharacterEncoding.Default);
	contentLength = -1;
	status = 200;
    }

    public void addCookie(Cookie cookie) {
	userCookies.addElement(cookie);
    }

    public void addSystemCookie(Cookie cookie) {
	systemCookies.addElement(cookie);
    }

    public abstract boolean containsHeader(String name);

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            return;  // throw an exception?
        }

        // Save the locale for use by getLocale()
        this.locale = locale;

        // Set the contentLanguage for header output
        contentLanguage = locale.getLanguage();

        // Set the contentType for header output
        // Use the setContentType() method so encoding is set properly
        String newType = constructLocalizedContentType(contentType, locale);
        setContentType(newType);
    }

    String constructLocalizedContentType(String type, Locale loc) {
        // Cut off everything after the semicolon
        int semi = type.indexOf(";");
        if (semi != -1) {
            type = type.substring(0, semi);
        }

        // Append the appropriate charset, based on the locale
        String charset = LocaleToCharsetMap.getCharset(loc);
        if (charset != null) {
            type = type + "; charset=" + charset;
        }

        return type;
    }

    public String getCharacterEncoding() {
	return characterEncoding;
    }

    public abstract boolean isStarted();
    public abstract boolean isCommitted();
    public abstract ServletOutputStream getOutputStream();
    public abstract PrintWriter getWriter() throws IOException;

    public void setContentType(String contentType) {
        this.contentType = contentType;
        setCharacterEncodingFromContentType(contentType);
    }

    public void setContentLength(int contentLength) {
	this.contentLength = contentLength;
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void sendError(int sc) throws IOException {
	sendError(sc, "No detailed message");
    }
    
    public void sendError(int sc, String msg) throws IOException {
	this.status = sc;

	Context context = request.getContext();
	
	if (context == null) {
	    sendPrivateError(sc, msg);

	    return;
	}

	ServletContextFacade contextFacade = context.getFacade();
	String path = context.getErrorPage(String.valueOf(sc));

	if (path != null) {
	    RequestDispatcher rd = contextFacade.getRequestDispatcher(path);
	    request.setAttribute("javax.servlet.error.status_code",
                String.valueOf(sc));
	    request.setAttribute("javax.servlet.error.message", msg);

	    try {
		reset();
		rd.forward(request.getFacade(), this.getFacade());
	    } catch (IllegalStateException ise) {
		// too late for a forward
		try {
		    rd.include(request.getFacade(), this.getFacade());
		} catch (ServletException se) {
		    sendPrivateError(sc, msg);
		}
	    } catch (ServletException se) {
		sendPrivateError(sc, msg);
	    }
	} else {
	    sendPrivateError(sc, msg);
	}

	// XXX
	// we only should set this if we are the head, not in an include
	
	close();
    }

    private void sendPrivateError(int sc, String msg) throws IOException {
	setContentType("text/html");
	
	StringBuffer buf = new StringBuffer();
	buf.append("<h1>Error: " + sc + "</h1>\r\n");
	buf.append(msg + "\r\n");
	
	// XXX
	// need to figure out if we are in an include better. The subclass
	// knows whether or not we are in an include!
	
	sendBodyText(buf.toString());
    }
    
    public void sendRedirect(String location) throws IOException {
	setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
	//mimeType = null;
	setContentType(Constants.ContentType.HTML);	// ISO-8859-1 default

        location = makeAbsolute(location);

	setHeader("Location", location);

	StringBuffer buf = new StringBuffer();
	buf.append("<head><title>Document moved</title></head>\r\n");
	buf.append("<body><h1>Document moved</h1>\r\n");
	buf.append("This document has moved <a href=\"");
	buf.append(location);
	buf.append("\">here</a>.<p>\r\n");
	buf.append("</body>\r\n");

	String body = buf.toString();

	setContentLength(body.length());

	sendBodyText(body);

	close();
    }

    // XXX This method is duplicated in server/ServerRequest.java
    private String getCharsetFromContentType(String type) {
        // Basically return everything after ";charset="
        if (type == null) {
            return null;
        }
        int semi = type.indexOf(";");
        if (semi == -1) {
            return null;
        }
        String afterSemi = type.substring(semi + 1);
        int charsetLocation = afterSemi.indexOf("charset=");
        if (charsetLocation == -1) {
            return null;
        }
        String afterCharset = afterSemi.substring(charsetLocation + 8);
        String encoding = afterCharset.trim();
        return encoding;
    }

    private void setCharacterEncodingFromContentType(String type) {
        String encoding = getCharsetFromContentType(type);
        if (encoding != null) {
           setCharacterEncoding(encoding);
        }
    }

    private void setCharacterEncoding(String encoding) {
        characterEncoding = encoding;
    }

    private String makeAbsolute(String location) {
        URL url = null;
        try {
	    // Try making a URL out of the location
	    // Throws an exception if the location is relative
            url = new URL(location);
	}
	catch (MalformedURLException e) {
	    String requrl = HttpUtils.getRequestURL(
                                request.getFacade()).toString();
	    try {
	        url = new URL(new URL(requrl), location);
	    }
	    catch (MalformedURLException ignored) {
	        // Give up
	        return location;
	    }
	}
        return url.toString();
    }

    public void sendBodyText(String s) throws IOException {
	try {
	    PrintWriter out = getWriter();
	    out.print(s);
	} catch (IllegalStateException ise) {
	    ServletOutputStream out = getOutputStream();
	    out.print(s);
	}
    }
    
    private void close() throws IOException {
	try {
	    PrintWriter out = getWriter();
	    out.close();
	} catch (IllegalStateException ise) {
	    ServletOutputStream out = getOutputStream();
	    out.close();
	}
    }
    
    public abstract void setDateHeader(String name, long date);    
    public abstract void setHeader(String name, String value);
    public abstract void setIntHeader(String name, int value);
    public abstract void addDateHeader(String name, long date);    
    public abstract void addHeader(String name, String value);
    public abstract void addIntHeader(String name, int value);

    public abstract int getBufferSize();
    public abstract void setBufferSize(int size) throws IllegalStateException;
    public abstract boolean isBufferCommitted();
    // reset() implemented above
    public abstract void flushBuffer() throws IOException;
}
