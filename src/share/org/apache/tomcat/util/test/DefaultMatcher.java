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

import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

// use regexp ? No, it's better to allow other matchers to be used - RegexpMatch
// will be a separate tool

/**
   Part of the GTest application
*/
public class DefaultMatcher {

    // Expected response
    boolean magnitude=true;
    boolean exactMatch=false;
    // Match the body against a golden file
    String goldenFile;
    // Match the body against a string
    String responseMatch;
    // the response should include the following headers
    Hashtable expectHeaders;
    // Match request line
    String returnCode="";
    String description;
    int debug;

    Response response;
    
    // Results of matching
    boolean result=false;
    StringBuffer messageSB;
    
    public DefaultMatcher() {
    }

    // -------------------- 
    
    public boolean getResult() {
	return result;
    }

    public String getMessage() {
	return messageSB.toString();
    }

    // -------------------- 

    /** The response we'll match against
     */
    public void setResponse( Response resp ) {
	response=resp;
    }

    // --------------------
    
    public void setExactMatch(String exact) {
	exactMatch=Boolean.valueOf( exact ).booleanValue();
    }

    /** Description should be in <test description=""/>
     */
    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description=description;
    }

    /** Display debug info
     */
    public void setDebug( String debugS ) {
	debug=Integer.valueOf( debugS).intValue();
    }

    /** True if this is a positive test, false for negative
     */
    public void setMagnitude( String magnitudeS ) {
        magnitude = Boolean.valueOf(magnitudeS).booleanValue();   
    }

    /** Compare with the golden file
     */
    public void setGoldenFile( String s ) {
	this.goldenFile=s;
    }


    public void addHeader( Header rh ) {
	expectHeaders.put( rh.getName(), rh );
    }
    
    /** Verify that response includes the expected headers.
     *  The value is a "|" separated list of headers to expect.
     */
    public void setExpectHeaders( String s ) {
       expectHeaders=new Hashtable();
       Header.parseHeadersAsString( s, expectHeaders );
    }

    /** Verify that response match the string
     */
    public void setResponseMatch( String s ) {
	this.responseMatch=s;
    }

    /** Verify the response code
     */
    public void setReturnCode( String s ) {
	this.returnCode=s;
    }

    // -------------------- Execute the request --------------------

    public void execute() {
	try {
	    result=checkResponse( magnitude );
	} catch(Exception ex ) {
	    result=false;
	}
    }

    void log(String s ) {
	messageSB.append( s ).append("\r\n");
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
		log("ERROR");
		log("    Expecting: " + returnCode );
		log("    Got      : " + responseLine);
	    }
	}

	if( expectHeaders != null ) {
	    // Check if we got the expected headers
	    if(headers==null) {
		log("ERROR no response header, expecting header");
	    }
	    Enumeration e=expectHeaders.keys();
	    while( e.hasMoreElements()) {
		String key=(String)e.nextElement();
		String value=(String)expectHeaders.get(key);
		String respValue=(String)headers.get(key);
		if( respValue==null || respValue.indexOf( value ) <0 ) {
		    log("ERROR expecting header " + key + ":" +
				       value + " GOT: " + respValue+ " HEADERS(" + headers + ")");
		    
		    return false;
		}
	    }

	}

	String responseBody=response.getResponseBody();
	    
	if( responseMatch != null ) {
	    // check if we got the string we wanted
	    if( responseBody == null ) {
		log("ERROR: got no response, expecting " + responseMatch);
		return false;
	    }
	    if( responseBody.indexOf( responseMatch ) < 0) {
		responseStatus = false;
		log("ERROR: expecting match on " + responseMatch);
		log("GOT: " );
		log(responseBody );
	    }
	}

	// compare the body
	if( goldenFile==null) return responseStatus;
	// Get the expected result from the "golden" file.
	StringBuffer expResult = getExpectedResult();
	
	// Compare the results and set the status
	boolean cmp=true;
	
	if(exactMatch)
	    cmp=compare(responseBody, expResult.toString() );
	else
	    cmp=compareWeek( responseBody, expResult.toString());
	
	if( cmp  != testCondition ) {
	    responseStatus = false;
	    log("ERROR (" + cmp + "," + testCondition + ")");
	    log("====================Expecting: ");
	    log(expResult.toString());
	    log("====================Got:");
	    log(responseBody);
	    log("====================");
	}	    
	
	return responseStatus;
    }
    
    // Parse a file into a String.
    private StringBuffer getExpectedResult()
	throws IOException
    {
        StringBuffer expResult = new StringBuffer("NONE");

        try {
	    //	    InputStream in = this.getClass().getResourceAsStream(goldenFile);
	    InputStream in = new FileInputStream( goldenFile );
	    return readBody ( in );
        } catch (Exception ex) {
            log("\tGolden file not found: " + goldenFile);
            return expResult;
        }
    }


    // Compare the actual result and the expected result.
    private boolean compare(String str1, String str2) {
	if ( str1==null || str2==null) return false;
	if ( str1.length() != str2.length() ) {
	    log("Wrong size " + str1.length() +" " + str2.length() );
	    return false;
	}
	
        for(int i=0; i<str1.length() ; i++ ) {
            if (str1.charAt( i ) != str2.charAt( i ) ) {
		log("Error at " + i  + " " + str1.charAt(1) +
				   str2.charAt(i));
                return false;
            }
        }
	return true;
    }

    // Compare the actual result and the expected result.
    // Original compare - ignores spaces ( because most
    // golden files are wrong !)
    private boolean compareWeek(String str1, String str2) {
 	if ( str1==null || str2==null) return false;
	
        StringTokenizer st1=new StringTokenizer(str1);
        StringTokenizer st2=new StringTokenizer(str2);

        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            String tok1 = st1.nextToken();
            String tok2 = st2.nextToken();
            if (!tok1.equals(tok2)) {
		log("\tFAIL*** : Rtok1 = " + tok1 
                        + ", Etok2 = " + tok2);
                return false;
            }
        }

        if (st1.hasMoreTokens() || st2.hasMoreTokens()) {
            return false;
        } else {
            return true;
        }
    }


    // XXX return byte [], fix the reading !!!!!
    StringBuffer readBody( InputStream input )
    {
	StringBuffer sb = new StringBuffer();
	while (true) {
	    try {
		int ch = input.read();
		if (ch < 0) {
		    if (sb.length() == 0) {
			return (null);
		    } else {
			break;
		    }
		}
		sb.append((char) ch);
	    } catch(IOException ex ) {
		return sb;
	    }
	}
        return sb;
    }

}
