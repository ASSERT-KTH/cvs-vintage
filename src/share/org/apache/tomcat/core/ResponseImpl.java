/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ResponseImpl.java,v 1.3 1999/10/22 01:47:07 costin Exp $
 * $Revision: 1.3 $
 * $Date: 1999/10/22 01:47:07 $
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

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 */

public class ResponseImpl extends Response {

    protected StringManager sm =
        StringManager.getManager(Constants.Package);

    protected MimeHeaders headers = new MimeHeaders();

    protected BufferedServletOutputStream out;
    protected PrintWriter writer;

    protected boolean usingStream = false;
    protected boolean usingWriter = false;
    protected boolean started = false;
    protected boolean committed = false;
    protected boolean omitHeaders = false;
    protected String serverHeader = null;
    
    public ResponseImpl() {
        super();
    }

    public boolean isStarted() {
	return started;
    }

    public boolean isCommitted() {
	return committed;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public void setOmitHeaders(boolean omitHeaders) {
	this.omitHeaders = omitHeaders;
    }
    
    public void setBufferedServeletOutputStream(
        BufferedServletOutputStream out) {
	this.out=out;
    }
    
    public void recycle() {
        super.recycle();
	headers.clear();
	usingWriter = false;
	usingStream = false;
	writer=null;
	out = null;
	started = false;
	committed = false;
	omitHeaders=false;
    }
    
    public void finish() throws IOException {
	try {
	    if (usingWriter && (writer != null)) {
	        writer.flush();
	    }
	    out.reallyFlush();
	} catch (SocketException e) {
	    return;  // munch
	}
    }
 
    public boolean containsHeader(String name) {
	return headers.containsHeader(name);
    }

    // XXX
    // mark whether or not we are being used as a stream our writer
    
    public ServletOutputStream getOutputStream() {
	started = true;

	if (usingWriter) {
	    String msg = sm.getString("serverResponse.outputStream.ise");

	    throw new IllegalStateException(msg);
	}

	usingStream = true;

	return out;
    }

    public PrintWriter getWriter() throws IOException {
	started = true;

	if (usingStream) {
	    String msg = sm.getString("serverResponse.writer.ise");

	    throw new IllegalStateException(msg);
	}

	usingWriter = true;

	if (writer == null) {
	    String encoding = getCharacterEncoding();

	    if ((encoding == null) || "Default".equals(encoding) )
	        writer = new PrintWriter(new OutputStreamWriter(out));
	    else
		try {
		    writer = new PrintWriter(new OutputStreamWriter(out, encoding));
		} catch (java.io.UnsupportedEncodingException ex) {
		    // if we don't do that, the runtime exception will propagate
		    // and we'll try to send an error page - but surprise, we
		    // still can't get the Writer to send the error page...
		    writer = new PrintWriter( new OutputStreamWriter(out));

		    // Deal with strange encodings - webmaster should see a message
		    // and install encoding classes - n new, unknown language was discovered,
		    // and they read our site!
		    System.out.println("Unsuported encoding: " + encoding );
		}
	}

	out.setUsingWriter (usingWriter);
	
	return writer;
    }
    
    public void setDateHeader(String name, long date) {
	headers.putDateHeader(name, date);
    }

    public void addDateHeader(String name, long date) {
        headers.addDateHeader(name, date);
    }
    
    public void setHeader(String name, String value) {
	headers.putHeader(name, value);
    }

    public void addHeader(String name, String value) {
        headers.addHeader(name, value);
    }
    
    public void setIntHeader(String name, int value) {
	headers.putIntHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        headers.addIntHeader(name, value);
    }
    
    public int getBufferSize() {
	return out.getBufferSize();
    }
    
    public void setBufferSize(int size) throws IllegalStateException {

	// Force the PrintWriter to flush the data to the OutputStream.
	if (usingWriter == true) writer.flush();
	
	if (out.isContentWritten() == true) {
	    String msg = sm.getString("servletOutputStreamImpl.setbuffer.ise");
	    throw new IllegalStateException (msg); 
	}
	out.setBufferSize(size);
    }
    
    /*
     * Methodname "isCommitted" already taken by Response class.
     */
    public boolean isBufferCommitted() {
	return out.isCommitted();
    }
    
    public void reset() throws IllegalStateException {
	// Force the PrintWriter to flush its data to the output
        // stream before resetting the output stream
        // 
	if (usingWriter == true) 
	    writer.flush();
	
	// Reset the stream
	out.reset();

        // Clear the cookies and such
        super.reset();

        // Clear the headers
        headers.clear();
    }
    
    public void flushBuffer() throws IOException {
	if (usingWriter == true)
	    writer.flush();
	
	out.reallyFlush();
    }

    
    /** Set server-specific headers */
    protected void fixHeaders() throws IOException {
	//	System.out.println( "Fixing headers" );
	headers.putIntHeader("Status", status);
        headers.putHeader("Content-Type", contentType);

	// Generated by Server!!!
	//headers.putDateHeader("Date",System.currentTimeMillis());
        //headers.putHeader("Server",getServerHeader());

        if (contentLength != -1) {
            headers.putIntHeader("Content-Length", contentLength);
        }
	
        // write cookies
        Enumeration cookieEnum = null;
        cookieEnum = systemCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            headers.putHeader( CookieTools.getCookieHeaderName(c),
			       CookieTools.getCookieHeaderValue(c));
	    if( c.getVersion() == 1 ) {
		// add a version 0 header too.
		// XXX what if the user set both headers??
		Cookie c0 = (Cookie)c.clone();
		c0.setVersion(0);            
		headers.putHeader( CookieTools.getCookieHeaderName(c0),
				   CookieTools.getCookieHeaderValue(c0));
	    }
        }
	// XXX duplicated code, ugly
        cookieEnum = userCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            headers.putHeader( CookieTools.getCookieHeaderName(c),
			       CookieTools.getCookieHeaderValue(c));
	    if( c.getVersion() == 1 ) {
		// add a version 0 header too.
		// XXX what if the user set both headers??
		Cookie c0 = (Cookie)c.clone();
		c0.setVersion(0);            
		headers.putHeader( CookieTools.getCookieHeaderName(c0),
				   CookieTools.getCookieHeaderValue(c0));
	    }
        }
	// XXX
        // do something with content encoding here
    }

    // XXX should be abstract
    public void endResponse() throws IOException {
    }

    // XXX should be abstract
    public void writeHeaders() throws IOException {
    }
    
}
