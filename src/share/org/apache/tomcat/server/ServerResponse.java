/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/server/Attic/ServerResponse.java,v 1.1 1999/10/09 00:20:48 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:20:48 $
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


package org.apache.tomcat.server;

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

public class ServerResponse extends ResponseImpl {
    protected StringManager sm =
        StringManager.getManager(Constants.Package);
    
    Socket socket;
    
    public ServerResponse() {
        super();
    }

    void setSocket(Socket socket) throws IOException {
        this.socket = socket;
	OutputStream sout = socket.getOutputStream();
	super.setBufferedServeletOutputStream( new ServletOutputStreamImpl(this, sout) );
    }

    // XXX
    // need to rethink this - hackary

    public void setOutputStream(OutputStream os) {
        super.setBufferedServeletOutputStream( new ServletOutputStreamImpl(this, os) );
    }
    
    public Socket getSocket() {
        return this.socket;
    }
    
    void writeHeaders(OutputStream sout) throws IOException {
        if (omitHeaders) {
            return;
        }

	// XXX
	// shouldn't this logic be in the servlet output stream
	// and it should get the headers from here?
	
	StringBuffer buf = new StringBuffer();

        // XXX
        // ok, we really need to know what HTTP/1.x header to write
        // here...

        // XXX
        // need to add nice name to end of first line

	String statusPhrase = sm.getString("sc." + status);	
        buf.append("HTTP/1.0 " + status);
	if (statusPhrase != null) {
	    buf.append(" " + statusPhrase);
	}
	buf.append("\r\n");

	HttpDate date = new HttpDate(System.currentTimeMillis());
	buf.append("Date: " + date + "\r\n");
	
	
        // XXX
        // need to suck this from a prop file -- also need to make it
        // customizable at the HttpServer level...
        
        buf.append("Server: " + getServerHeader() + "\r\n");

	// context is null if we are in a error handler before the context is
	// set ( i.e. 414, wrong request )
	if( request.getContext() != null) 
	    buf.append("Servlet-Engine: " + 
		       request.getContext().getEngineHeader() + "\r\n");

        // XXX
        // do something with connnection header here if any

        // XXX
        // do something with transfer encoding header here if any

        buf.append("Content-Type: " + contentType + "\r\n");

        if (contentLanguage != null) {
            buf.append("Content-Language: " + contentLanguage + "\r\n");
        }

        if (contentLength != -1) {
            buf.append("Content-Length: " + contentLength + "\r\n");
        }
	
        // write system and user cookies
        Enumeration cookieEnum = null;
        cookieEnum = systemCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            buf.append(CookieUtils.getCookieHeader(c) + "\r\n");
        }
        cookieEnum = userCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            buf.append(CookieUtils.getCookieHeader(c) + "\r\n");
        }
        
        // XXX
        // do something with content encoding here

        // write out rest of headers here.
        
        // XXX
        // we shouldn't be getting an enum here -- we should just
        // have the mime headers shove themselves out the outputstream
        // however -- we've got to rewrite all the parts of the MimeHeaderXXX
        // classes to grok outputstreams and not servletoutputstream...

        int size = headers.size();
        for (int i = 0; i < size; i++) {
            MimeHeaderField h = headers.getField(i);
            buf.append(h + "\r\n");
        }
        buf.append("\r\n");
        
        sout.write(buf.toString().getBytes());        
    }
    
}
