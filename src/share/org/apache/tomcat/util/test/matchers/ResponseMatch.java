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
