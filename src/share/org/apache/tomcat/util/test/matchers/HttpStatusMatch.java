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
 *  Test if the HTTP response has a certain status code 
 */
public class HttpStatusMatch extends Matcher {

    // Match request line
    String returnCode=null;

    public HttpStatusMatch() {
    }

    // -------------------- 
    
    /** Verify the response code
     */
    public void setMatch( String s ) {
	this.returnCode=s;
    }

    public void setReturnCode( String s ) {
	this.returnCode=s;
    }

    /** A test description of the test beeing made
     */
    public String getTestDescription() {
	StringBuffer desc=new StringBuffer();
	if( returnCode != null ) {
	    desc.append("( returnCode matches '" + returnCode + "') ");
	}
	desc.append( " == " ).append( magnitude );
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
	
	// you can't check return code on http 0.9
	if( returnCode != null ) {
	    boolean match= ( responseLine!=null &&
			     responseLine.indexOf(returnCode) > -1);
	    if( match != testCondition ) {
		responseStatus = false;
		if( testCondition )
		    log("    Expecting    : " + returnCode );
		else
		    log("    Not Expecting: " + returnCode );
		log("    Got          : " + responseLine);
	    }
	}

	return responseStatus;
    }
}
