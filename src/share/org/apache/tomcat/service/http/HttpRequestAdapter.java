/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/http/Attic/HttpRequestAdapter.java,v 1.11 2000/04/25 17:54:26 costin Exp $
 * $Revision: 1.11 $
 * $Date: 2000/04/25 17:54:26 $
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


package org.apache.tomcat.service.http;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class HttpRequestAdapter extends RequestImpl {
    private Socket socket;
    private boolean moreRequests = false;
    InputStream sin;
    byte[] buf;
    
    public HttpRequestAdapter() {
        super();
	buf=new byte[Constants.RequestBufferSize];
    }

    public void setSocket(Socket socket) throws IOException {
	sin = new BufferedInputStream ( socket.getInputStream());
	in = new BufferedServletInputStream(this);
        this.socket = socket;
    	moreRequests = true;
    }

    public void recycle() {
	super.recycle();
    }
    
    public Socket getSocket() {
        return this.socket;
    }

    public boolean hasMoreRequests() {
        return moreRequests;
    }
    
    public int doRead() throws IOException {
	return sin.read();
    }

    public int doRead(byte[] b, int off, int len) throws IOException {
	return sin.read(b, off, len);
    }

    public void readNextRequest(Response response) throws IOException {
	int count = in.readLine(buf, 0, buf.length);
	if (count < 0 ) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    return;
	}
	
	processRequestLine(response, buf, 0, count );

	// XXX
	//    return if an error was detected in processing the
	//    request line

	// read headers if at least we have a protocol >=1.0, or the
	// error will be reported to early
	//         if (response.getStatus() >=
	// 	    HttpServletResponse.SC_BAD_REQUEST) {
	//             return;
	// 	}

	// for 0.9, we don't have headers!
	if ((protocol!=null) &&
            !protocol.toLowerCase().startsWith("http/0."))
	    headers.read(in);

	// XXX
	// detect for real whether or not we have more requests
	// coming
	moreRequests = false;	
    }    
    
    public int getServerPort() {
        return socket.getLocalPort();
    }

    public String getServerName() {
	if(serverName!=null) return serverName;
	
	// XXX Move to interceptor!!!
	String hostHeader = this.getHeader("host");
	if (hostHeader != null) {
	    int i = hostHeader.indexOf(':');
	    if (i > -1) {
		hostHeader = hostHeader.substring(0,i);
	    }
	    serverName=hostHeader;
	    return serverName;
	}

	if (hostHeader == null) {
		// XXX
		// we need a better solution here
		InetAddress localAddress = socket.getLocalAddress();
		serverName = localAddress.getHostName();
	}
	return serverName;
    }
    
    
    public String getRemoteAddr() {
        return socket.getInetAddress().getHostAddress();
    }
    
    public String getRemoteHost() {
	return socket.getInetAddress().getHostName();
    }    
    
    public void processRequestLine(Response response, byte buf[], int start, int count)
	throws IOException
    {

	String line=new String(buf, 0, count, Constants.CharacterEncoding.Default);
        String buffer = line.trim();

	int firstDelim = buffer.indexOf(' ');
	int lastDelim = buffer.lastIndexOf(' ');
	// default - set it to HTTP/0.9 or null if we can parse the request
	//protocol = "HTTP/1.0";

	if (firstDelim == -1 && lastDelim == -1) {
	    if (buffer.trim().length() > 0) {
	        firstDelim = buffer.trim().length();
		lastDelim = buffer.trim().length();
	    }
	}

	if (firstDelim != lastDelim) {
	    String s = buffer.substring(firstDelim, lastDelim);

	    if (s.trim().length() == 0) {
	        firstDelim = lastDelim;
	    }
	}

	String requestString=null;
	
	if (firstDelim != lastDelim) {
	    method = buffer.substring(0, firstDelim).trim();
	    protocol = buffer.substring(lastDelim + 1).trim();
	    requestString = buffer.substring(firstDelim + 1, lastDelim).trim();
	} else if (firstDelim != -1 && lastDelim != -1) {
	    method = buffer.substring(0, firstDelim).trim();
	    protocol = null;
	    if (lastDelim < buffer.length()) {
	        requestString = buffer.substring(lastDelim + 1).trim();
	    }
	}

	if (protocol != null &&
	    ! protocol.toLowerCase().startsWith("http/")) {
	    requestString += " " + protocol;
	    protocol = null;
	}

        int requestErrorCode = 0; 

	// see if request looks right

	try {
	    int len = line.length();

	    if (len < 2) {
	        requestErrorCode = HttpServletResponse.SC_BAD_REQUEST;
	    } else if (/* line.charAt(len - 2) != '\r' || Correct, but will break C clients */
                line.charAt(len - 1) != '\n') {
	        requestErrorCode =
		    HttpServletResponse.SC_REQUEST_URI_TOO_LONG;
		// XXX
		// For simplicity we assume there's an HTTP/1.0 on the end
		// We should check to be sure.
		protocol = "HTTP/1.0";
	    }
	} catch (StringIndexOutOfBoundsException siobe) {
	}

	// see if uri is well formed

	String msg="";
	if (requestErrorCode == 0 &&
	    (requestString == null || requestString.indexOf(' ') > -1 ||
	        requestString.indexOf('/') != 0)) {
	    requestErrorCode = HttpServletResponse.SC_BAD_REQUEST;
	    msg="Bad request: " + requestString + " " + requestErrorCode;
	}

	if (requestErrorCode != 0) {
	    response.setStatus(requestErrorCode);
	    return;
	}

	int indexQ=requestString.indexOf("?");
	int rLen=requestString.length();
	if ( (indexQ >-1) && ( indexQ  < rLen) ) {
	    queryString = requestString.substring(indexQ + 1, requestString.length());
	    requestURI = requestString.substring(0, indexQ);
	} else {
	    requestURI= requestString;
	}
    }

    
}
