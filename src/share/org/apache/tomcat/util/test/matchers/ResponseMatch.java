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
package org.apache.tomcat.util.test.matchers;

import java.util.Hashtable;

import org.apache.tomcat.util.test.Matcher;

/**
   Check if the response contains a substring
*/
public class ResponseMatch extends Matcher {
    // Match the body against a string
    String responseMatch;

    public ResponseMatch() {
    }

    // -------------------- 

    /** Verify that response match the string
     */
    public void setMatch( String s ) {
	this.responseMatch=s;
    }

    /** Verify that response match the string
     */
    public void setResponseMatch( String s ) {
	this.responseMatch=s;
    }

    /** A test description of the test beeing made
     */
    public String getTestDescription() {
	StringBuffer desc=new StringBuffer();

	desc.append("( responseBody matches '"+ responseMatch + "') ");
	return desc.toString();
    }

    // -------------------- Execute the request --------------------

    public void execute() {
        if( skipTest() )
           return;
	try {
	    result=checkResponse( magnitude );
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    result=false;
	}
    }

    private boolean checkResponse(boolean testCondition)
	throws Exception
    {
	String responseLine=response.getResponseLine();
	Hashtable headers=response.getHeaders();
	
        boolean responseStatus = true;
	
	String responseBody=response.getResponseBody();
	    
	if( responseMatch != null ) {
	    // check if we got the string we wanted
	    if( responseBody == null ) {
		log("ERROR: got no response, expecting " + responseMatch);
		return false;
	    }
	    boolean match=responseBody.indexOf( responseMatch ) >= 0; 
	    if( match != testCondition ) {
		responseStatus = false;
		if( testCondition )
		    log("ERROR: expecting match on " + responseMatch);
		else
		    log("ERROR: expecting no match on " + responseMatch);
		log("GOT: " );
		log(responseBody );
	    }
	}

	
	return responseStatus;
    }
}
