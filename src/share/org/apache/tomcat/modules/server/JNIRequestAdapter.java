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

import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;

public class JNIRequestAdapter extends Request {
    JNIConnectionHandler h;
    long s;
    long l;

    public JNIRequestAdapter(ContextManager cm,
                             JNIConnectionHandler h) {
    	this.contextM = cm;
    	this.h = h;
    }

    public  int doRead(byte b[], int off, int len) throws IOException {
	if( available <= 0 )
	    return 0;
        int rc = 0;

        while(0 == rc) {
	        rc = h.read(s, l, b, off, len);
	        if(0 == rc) {
	            Thread.currentThread().yield();
	        }
	    }
	available -= rc;
	return rc;
    }

    protected void readNextRequest(long s, long l) throws IOException {
        String []env = new String[15];
        int i = 0;

    	this.s = s;
    	this.l = l;

        for(i = 0 ; i < 12 ; i++) {
            env[i] = null;
        }

        /*
         * Read the environment
         */
        if(h.readEnvironment(s, l, env) > 0) {
    		methodMB.setString( env[0] );
    		uriMB.setString( env[1] );
    		queryMB.setString( env[2] );
    		remoteAddrMB.setString( env[3] );
    		remoteHostMB.setString( env[4] );
    		serverNameMB.setString( env[5] );
            serverPort  = Integer.parseInt(env[6]);
            authType    = env[7];
            remoteUser  = env[8];
            schemeMB.setString(env[9]);
            protoMB.setString( env[10]);
            // response.setServerHeader(env[11]);
            
            if(schemeMB.equalsIgnoreCase("https")) {
                if(null != env[12]) {
		            attributes.put("javax.servlet.request.X509Certificate",
	                               env[12]);
	            }
	            
                if(null != env[13]) {
		            attributes.put("javax.servlet.request.cipher_suite",
	                               env[13]);
	            }
	            
                if(null != env[14]) {
		            attributes.put("javax.servlet.request.ssl_session",
	                               env[14]);
	            }
            }
            
            
        } else {
            throw new IOException("Error: JNI implementation error");
        }

        /*
         * Read the headers
         */
        int nheaders = h.getNumberOfHeaders(s, l);
        if(nheaders > 0) {
            String []names = new String[nheaders];
            String []values = new String[nheaders];
            if(h.readHeaders(s, l, names, values) > 0) {
                for(i = 0 ; i < nheaders ; i++) {
                    headers.addValue(names[i]).setString(values[i]);
                }
            } else {
                throw new IOException("Error: JNI implementation error");
            }
        }

	// REQUEST_URI may include a query string
	String requestURI=uriMB.toString();
	int indexQ=requestURI.indexOf("?");
	int rLen=requestURI.length();
	if ( (indexQ >-1) && ( indexQ  < rLen) ) {
	    queryMB.setString( requestURI.substring(indexQ + 1, requestURI.length()));
	    uriMB.setString( requestURI.substring(0, indexQ));
	} 
    }
}

