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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.tomcat.util.test.Matcher;

/**
   Check if the response matches a response file
*/
public class ResponseMatchFile extends Matcher {
    // Match the body against a list of strings in a file
    String responseMatchFile;

    public ResponseMatchFile() {
    }

    // -------------------- 

    /** Verify that response matches a list of strings in a file
     */
    public void setFile( String s ) {
	this.responseMatchFile=s;
    }
    public void setResponseMatchFile( String s ) {
	this.responseMatchFile=s;
    }

    /** A test description of the test beeing made
     */
    public String getTestDescription() {
	StringBuffer desc=new StringBuffer();
	boolean needAND=false;

	// if match file is specified
	if( responseMatchFile != null ) {
	    if( needAND ) desc.append( " && " );
	    needAND=true;

	    desc.append("( responseBody matches lines in '"+
			responseMatchFile + "') ");
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
	
	String responseBody=response.getResponseBody();
	    
	// if match file is specified
	if( responseMatchFile != null ) {
	    try {
		boolean desiredResult = true;
		FileReader fr=new FileReader( responseMatchFile);
		BufferedReader br = new BufferedReader( fr );

		String expected = br.readLine();
		while (expected != null) {
		    if ( "!=".equals(expected) )
			desiredResult = false;
		    else {
			boolean result = responseBody.indexOf( expected ) >= 0;
			if( result != desiredResult ) {
			    responseStatus = false;
			    if ( desiredResult )
				log("ERROR: expecting match on " + expected);
			    else
				log("ERROR: expecting no match on " + expected);
			    log("In match file: " + responseMatchFile);
			    log("====================Got:");
			    log(responseBody );
			    log("====================");
			}
		    }
		    expected = br.readLine();
		}
		br.close();
	    } catch (FileNotFoundException ex) {
        	log("\tMatch file not found: " + responseMatchFile);
		log("====================Got:");
		log(responseBody );
		log("====================");
		responseStatus = false;
	    } catch ( IOException ex ) {
        	log("\tError reading match file: " + responseMatchFile);
        	log(ex.toString());
		responseStatus = false;
	    }
	}
	
	return responseStatus;
    }
}
