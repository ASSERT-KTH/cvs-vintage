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

