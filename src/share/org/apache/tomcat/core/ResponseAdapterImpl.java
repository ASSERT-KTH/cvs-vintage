/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ResponseAdapterImpl.java,v 1.1 1999/11/01 20:09:22 costin Exp $
 * $Revision: 1.1 $
 * $Date: 1999/11/01 20:09:22 $
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
 * Base implementation for ResponseAdapter
 * 
 * @author costin@eng.sun.com
 */
public class ResponseAdapterImpl implements ResponseAdapter {
    int status;
    String message;
    Hashtable headers=new Hashtable();
    BufferedServletOutputStream sos=new BufferedServletOutputStream(this);
    StringBuffer body=new StringBuffer();

    public ResponseAdapterImpl() {
    }
    
    /** Set the response status and message. 
     *	@param message null will set the "default" message, "" will send no message
     */ 
    public void setStatus( int status, String message) throws IOException {
	this.status=status;
	this.message=message;
    }

    
    public void addHeader( String name, String value ) throws IOException {
	headers.put( name, value );
    }

    // XXX This one or multiple addHeader?
    // Probably not a big deal - but an adapter may have
    // an optimized version for this one ( one round-trip only )
    public void addMimeHeaders(MimeHeaders headers) throws IOException {
	int size = headers.size();
        for (int i = 0; i < size; i++) {
            MimeHeaderField h = headers.getField(i);
            addHeader( h.getName(), h.getValue());
        }
    }

    /** Signal that we're done with a particular request, the
	server can go on and read more requests or close the socket
    */
    public void endResponse() throws IOException {
    }

    /** Signal that we're done with the headers, and body will follow.
	The adapter doesn't have to maintain state, it's done inside the engine
    */
    public void endHeaders() throws IOException {

    }

    /** Either implement ServletOutputStream or return BufferedServletOutputStream(this)
	and implement doWrite();
     */
    public ServletOutputStream getServletOutputStream() throws IOException {
	return sos;
    }
	
    
    /** Write a chunk of bytes. Should be called only from ServletOutputStream implementations,
     *	No need to implement it if your adapter implements ServletOutputStream.
     *  Headers and status will be written before this method is exceuted.
     */
    public void doWrite( byte buffer[], int pos, int count) throws IOException {
	body.append(new String(buffer, pos, count) );
    }

    public void recycle() {
	sos.recycle();
	headers.clear();
	status=-1;
	message=null;
	body.setLength(0);
    }


    // -------------------- Extract response information --------------------
    public int getStatus() {
	return status;
    }

    public String getMessage() {
	return message;
    }

    public Hashtable getHeaders() {
	return headers;
    }

    public StringBuffer getBody() {
	return body;
    }
}
