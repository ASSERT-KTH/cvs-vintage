/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/http/Attic/HttpResponseAdapter.java,v 1.2 1999/11/22 18:36:44 costin Exp $
 * $Revision: 1.2 $
 * $Date: 1999/11/22 18:36:44 $
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

// no "buffering" - we send the status and headers as soon as
// the method is called.

// this method is _not_ thread-safe. ( if 2 threads call ServletOutputStream.out

/**
 */
public class HttpResponseAdapter implements  ResponseAdapter {
    protected OutputStream sout;

    // no need to create new objects/request,
    // avoid extra String creation
    protected StringBuffer statusSB;
    protected StringBuffer headersSB;

    // XXX Temporary fix for encoding - it should be at a higher level
    // ( i.e. connector)
    // Also, we need to resolve few other problems in this are - header
    // encoding != body encoding, default should _not_ be platform def., etc.
    // Any reason this should be a soft setting?
    final static String encoding = "ISO-8859-1";  // as called for by HTTP standard?
    // final static String encoding = "UTF8";     // more useful?

    public HttpResponseAdapter() {
        super();
	statusSB=new StringBuffer();
	headersSB=new StringBuffer();
    }

    public void recycle() {
	sout=null;
	statusSB.setLength(0);
	headersSB.setLength(0);
    }

    public void setStatus( int status, String message ) throws IOException {
	// statusSB.reset();
	statusSB.append("HTTP/1.0 ").append(status);
	if(message!=null) statusSB.append(" ").append(message);
	statusSB.append("\r\n");
	sout.write(statusSB.toString().getBytes(encoding));
	statusSB.setLength(0);
    }
    
    public void setOutputStream(OutputStream os) {
	sout = os;
    }

    public void addHeader(String name, String value) throws IOException{
	headersSB.setLength(0);
	headersSB.append(name).append(": ").append(value).append("\r\n");
	sout.write( headersSB.toString().getBytes(encoding) );
    }
    
    public void addMimeHeaders(MimeHeaders headers) throws IOException {
	headersSB.setLength(0);
	int size = headers.size();
        for (int i = 0; i < size; i++) {
            MimeHeaderField h = headers.getField(i);
            headersSB.append(h).append("\r\n");
        }
	sout.write( headersSB.toString().getBytes(encoding) );
    }

    static final byte CRLF[]= { (byte)'\r', (byte)'\n' };
    
    public void endHeaders()  throws IOException {
	sout.write( CRLF, 0, 2 );
    }

    public void endResponse() throws IOException {
	sout.flush();
    }

    public ServletOutputStream getServletOutputStream() throws IOException {
	return null; // use default
    }

    public void doWrite( byte buffer[], int pos, int count) throws IOException {
	sout.write( buffer, pos, count);
    }
}
