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
package org.apache.tomcat.util.test;

import org.apache.tools.ant.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

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
