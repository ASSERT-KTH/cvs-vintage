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

import org.apache.tomcat.util.test.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

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
