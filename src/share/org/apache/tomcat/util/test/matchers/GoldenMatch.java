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
 *  Check if the Resposne body matches a golden file.
 */
public class GoldenMatch extends Matcher {

    // Match the body against a golden file
    String goldenFile;

    // ignore spaces ?
    boolean exactMatch=false;


    public GoldenMatch() {
    }

    // -------------------- 

    
    public void setExactMatch(boolean ex) {
	exactMatch=ex;
    }

    /** Compare with the golden file
     */
    public void setFile( String s ) {
	this.goldenFile=s;
    }

    public void setGoldenFile( String s ) {
	this.goldenFile=s;
    }

    public String getTestDescription() {
	StringBuffer desc=new StringBuffer();
	desc.append("( responseBody " );
	if( exactMatch )
	    desc.append( "equals file '" );
	else
	    desc.append( "like file '");
	int idx=goldenFile.lastIndexOf("/");
	String g=(idx>0) ? goldenFile.substring(idx) : goldenFile;
	desc.append( goldenFile + "') ");

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
