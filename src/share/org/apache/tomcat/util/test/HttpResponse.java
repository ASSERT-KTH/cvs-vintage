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

package org.apache.tomcat.util.test;

import java.util.Hashtable;


/**
 *  Part of GTest.
 * 
 */
public class HttpResponse {

    String responseLine;
    String responseBody;
    Hashtable responseHeaders=new Hashtable();
    
    Throwable exception;

    public HttpResponse() {}

    /** Exception thrown during request execution
     */
    public void setThrowable( Throwable t ) {
	exception=t;
    }

    public Throwable getThrowable() {
	return exception;
    }

    
    /**
     * Response headers 
     */
    public Hashtable getHeaders() {
	return responseHeaders;
    }

    public void setHeaders(Hashtable  v) {
	this.responseHeaders = v;
    }
    
    
    /**
     * Get the value of responseBody - the content
     */
    public String getResponseBody() {
	return responseBody;
    }
    
    public void setResponseBody(String  v) {
	this.responseBody = v;
    }
    
    
    /**
     * Get the value of responseLine - the first line of the response
     */
    public String getResponseLine() {
	return responseLine;
    }
    
    public void setResponseLine(String  v) {
	this.responseLine = v;
    }
    
    

}
