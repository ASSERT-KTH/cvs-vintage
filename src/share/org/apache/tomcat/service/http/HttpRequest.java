/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/http/Attic/HttpRequest.java,v 1.1 1999/10/09 00:20:49 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:20:49 $
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

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
//import org.apache.tomcat.server.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class HttpRequest extends RequestImpl {

    public HttpRequest() {
	super();
    }
    
    public void recycle() {
	super.recycle();
    }

    protected int readRequest(InputStream is) throws IOException {

 	HttpServletIS in = new HttpServletIS(is);
 	this.in=in;

	String line=in.readLine();
	if( line == null ) {
	    return -1;
	}
	processRequestLine( line );
	headers.read( in );

	return 0;

    }    


    public void processRequestLine(String line) {
        String buffer = line.trim();
	int firstDelim = buffer.indexOf(' ');
	int lastDelim = buffer.lastIndexOf(' ');

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

	if (firstDelim != lastDelim) {
	    method = buffer.substring(0, firstDelim).trim();
	    protocol = buffer.substring(lastDelim + 1).trim();
	    requestURI = buffer.substring(firstDelim + 1, lastDelim).trim();
	} else if (firstDelim != -1 && lastDelim != -1) {
	    method = buffer.substring(0, firstDelim).trim();
	    protocol = null;

	    if (lastDelim < buffer.length()) {
	        requestURI = buffer.substring(lastDelim + 1).trim();
	    }
	}

	if (protocol != null &&
	    ! protocol.toLowerCase().startsWith("http/")) {
	    requestURI += " " + protocol;
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

        if (requestErrorCode == 0 &&
	    (requestURI == null || requestURI.indexOf(' ') > -1 ||
	        requestURI.indexOf('/') != 0)) {
	    requestErrorCode = HttpServletResponse.SC_BAD_REQUEST;
	}

	if (requestErrorCode != 0) {
            try {
	        response.sendError(requestErrorCode);
	    } catch (IOException ioe) {
            }

	    return;
	}

        // get query string and
        // parse out the request line parameters if possible
        
        if (requestURI.indexOf("?") > -1) {
            queryString = requestURI.substring(
                requestURI.indexOf("?") + 1, requestURI.length());
            processFormData(queryString);
	    requestURI = requestURI.substring(0, requestURI.indexOf("?"));
        }
    }

}

