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

import java.util.StringTokenizer;

import org.apache.tools.ant.Project;

/**
   Part of the GTest application
*/
public class Matcher {
    protected HttpResponse response;
    protected HttpRequest request;
    protected HttpClient client;
    protected boolean result=false;
    protected boolean magnitude=true; // expectedResult
    protected int debug=0;
    
    // If the matching fails, a description of what failed
    StringBuffer messageSB=new StringBuffer();

    String ifProp=null;
    String unlessProp=null;
        
    public Matcher() {
    }

    // ----------------- Ant Properties -----------------

    public void setIf(String prop) {
        ifProp=prop;
    }

    public void setUnless(String prop) {
        unlessProp=prop;
    }

    // -------------------- General Properties --------------------

    /** Test description ( text representation of the test )
     */
    public String getTestDescription() {
	return "";
    }

    public void setExpectedResult( boolean b ) {
	magnitude=b;
    }

    public void setMagnitude( boolean b ) {
	magnitude=b;
    }

    /** Display debug info
     */
    public void setDebug( int d ) {
	debug=d;
    }

    /** Return a message describing the reason of the failure
     *  or the test log
     */
    public String getMessage() {
	return messageSB.toString();
    }

    /** Add a message to the test log
     */
    protected void log(String s ) {
	messageSB.append( s ).append("\r\n");
    }

    /** Result of the test
     */
    public boolean getResult() {
	return result;
    }
    
    
    // -------------------- Client, request, response --------------------
    /** The test case
     */
    public void setHttpClient( HttpClient req ) {
	client=req;
    }

    public HttpClient getHttpClient() {
	return client;
    }

    /** The request that generated the response
     */
    public void setHttpRequest( HttpRequest req ) {
	request=req;
    }

    
    public HttpRequest getHttpRequest() {
	return request;
    }

    /** The response we'll match against
     */
    public void setHttpResponse( HttpResponse resp ) {
	response=resp;
    }

    public HttpResponse getHttpResponse() {
	return response;
    }

    // --------------------

    /** Execute the test
     */
    public void execute() {
    }

    /** Check if test should be skipped
     */
    public boolean skipTest() {
        if( client != null ) {
            Project project = client.getProject();
            if( project != null ) {
                if( ifProp != null && project.getProperty(ifProp) == null) {
                    // skip if "if" property is not set
                    result = true;
                    return true;
                }
                // Allow a comma separated list of properties for "unless"
                // so after using a sequence of properties, each in an "if",
                // you can include the list in an "unless" to implement the
                // default.
                if( unlessProp != null) {
                    StringTokenizer st = new StringTokenizer(unlessProp,",");
                    while( st.hasMoreElements() ) {
                        String prop = (String)st.nextElement();
                        if( project.getProperty(prop) != null ) {
                            // skip if an "unless" property is set
                            result = true;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }    
    
}
