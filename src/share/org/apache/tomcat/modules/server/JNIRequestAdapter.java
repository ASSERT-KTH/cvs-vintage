/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

