/*
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

package org.apache.tomcat.modules.server;

import java.io.IOException;

import org.apache.tomcat.core.Response;
import org.apache.tomcat.util.http.HttpMessages;


public class JNIResponseAdapter extends Response {
    // Ajp use Status: instead of Status
    JNIConnectionHandler h;
    long s;
    long l;

    public JNIResponseAdapter(JNIConnectionHandler h) {
    	this.h = h;
    }

    protected void setRequestAttr(long s, long l) throws IOException {
    	this.s = s;
    	this.l = l;
    }

    public void endHeaders() throws IOException {

    	if(request.protocol().isNull()) // HTTP/0.9 
	        return;

        super.endHeaders();
        
	// Servlet Engine header will be set per/adapter - smarter adapters
	// will not send it every time ( have it in C side ), and we may also
	// want to add informations about the adapter used 
	// 	if( request.getContext() != null)
	// 	    setHeader("Servlet-Engine",
	// 		      request.getContext().getEngineHeader());

        int    hcnt = 0;
        String []headerNames = null;
        String []headerValues = null;
	// Shouldn't be set - it's a bug if it is
        // headers.removeHeader("Status");
        hcnt = headers.size();
        headerNames = new String[hcnt];
        headerValues = new String[hcnt];

        for(int i = 0; i < hcnt; i++) {
            headerNames[i] = headers.getName(i).toString();
            headerValues[i] = headers.getValue(i).toString();
        }

        if(h.startReasponse(s, l, status,
			    HttpMessages.getMessage(status),
			    headerNames, headerValues, hcnt) <= 0) {
            throw new IOException("JNI startReasponse implementation error");
        }
    }

    public void doWrite(byte buf[], int pos, int count) throws IOException {
        if(h.write(s, l, buf, pos, count) <= 0) {
            throw new IOException("JNI implementation error");
        }
    }
}

