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


/** Test a web application. Will send a http request and
    verify the response code, compare the response with a golden
    file or find strings.

    This class is using the well-known ant patterns.
*/
public class GTest  {

    HttpClient httpClient=new HttpClient();
    DefaultMatcher matcher=new DefaultMatcher();
    
    int debug=0;
    String description="No description";

    static PrintWriter defaultOutput=new PrintWriter(System.out);
    static String defaultOutType="text";
    
    PrintWriter out=defaultOutput;
    String outType=defaultOutType;
    boolean failureOnly=false;
    
    public GTest() {
    }

    // -------------------- GTest behavior --------------------

    public void setWriter( PrintWriter pw ) {
	out=pw;
    }

    public static void setDefaultWriter( PrintWriter pw ) {
	defaultOutput=pw;
    }

    public static void setDefaultOutput( String s ) {
	defaultOutType=s;
    }

    /** text, xml, html
     */
    public void setOutput( String t ) {
	outType=t;
    }

    /** Report only tests that fail
     */
    public void setFailureOnly( String e ) {
	failureOnly=Boolean.valueOf(e).booleanValue();   
    }

    // -------------------- Ant patterns --------------------

    public HttpClient createHttpClient() {
	return httpClient;
    }

    public void addHttpClient(HttpClient c) {
	httpClient=c;
    }

    public void addDefaultMatcher( DefaultMatcher m ) {
	matcher=m;
    }
    
    // -------------------- Local properties --------------------
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
	matcher.setDebug( debugS );
	httpClient.setDebug( debugS );
    }

    // -------------------- Client properties --------------------
    public void setHost(String h) {
	httpClient.setHost(h);
    }
    
    public void setPort(String portS) {
	httpClient.setPort( portS );
    }

    /** Set the port as int - different name to avoid confusing ant
     */
    public void setPortInt(int i) {
	httpClient.setPortInt(i);
    }

    /** Do a POST with the specified content
     */
    public void setContent(String s) {
	httpClient.setContent(s);
    }

    /** Request line ( will have the host and context path prefix)
     */
    public void setRequest( String s ) {
	httpClient.setRequestLine(s);
    }
    
    /** Send additional headers
     *  The value is a "|" separated list of headers to send
     */
    public void setHeaders( String s ) {
	httpClient.setHeaders( s );
    }

    // -------------------- Matcher properties --------------------
    
    public void setExactMatch(String exact) {
	matcher.setExactMatch(exact);
    }

    /** True if this is a positive test, false for negative
     */
    public void setMagnitude( String magnitudeS ) {
        matcher.setMagnitude( magnitudeS );
    }

    /** Compare with the golden file
     */
    public void setGoldenFile( String s ) {
	matcher.setGoldenFile(s);
    }

    /** Verify that response includes the expected headers
     *  The value is a "|" separated list of headers to expect.
     */
    public void setExpectHeaders( String s ) {
	matcher.setExpectHeaders( s );
    }

    /** Verify that response match the string
     */
    public void setResponseMatch( String s ) {
	matcher.setResponseMatch( s );
    }

    /** Verify the response code
     */
    public void setReturnCode( String s ) {
	matcher.setReturnCode( s );
    }

    // -------------------- Execute the request --------------------

    public void execute() {
	
	try {
	    httpClient.execute();
	    Response resp=httpClient.getResponse();

	    matcher.setResponse( resp );
	    matcher.execute();
	    boolean result=matcher.getResult();

	    // don't print OKs
	    if( result && failureOnly ) return;
	    
	    if( "text".equals(outType) )
		textReport( result , null );
	    if( "html".equals(outType) )
		htmlReport( result , null );
	    if( "xml".equals(outType) )
		xmlReport( result , null );
	} catch(Exception ex ) {
	    textReport( false, ex );
	}
    }

    
    // -------------------- Internal methods --------------------

    private void textReport( boolean result, Exception ex ) {
	String msg=null;
	if(  "No description".equals( description )) 
	    msg=" (" + httpClient.getRequestLine() + ")";
	else
	    msg=description + " (" + httpClient.getRequestLine() + ")";

	if(result) 
	    out.println("OK " + msg );
	else 
	    out.println("FAIL " + msg );

	if( ex!=null)
	    ex.printStackTrace(out);
    }

    private void htmlReport( boolean result, Exception ex ) {
	if( result )
	    out.println( "OK " );
	else
	    out.println("<font color='red'>FAIL ");

	String msg=null;
	if(  "No description".equals( description )) 
	    msg=" (" + httpClient.getRequestLine() + ")";
	else
	    msg=description + " (" + httpClient.getRequestLine() + ")";

	out.println( msg );
	
	if( ! result )
	    out.println("</font>");

	out.println("<br>");

	if( ex!=null) {
	    out.println("<b>Exception</b><pre>");
	    ex.printStackTrace(out);
	    out.println("</pre><br>");
	}
    }

    private void xmlReport( boolean result, Exception ex ) {
	String msg=null;
	if(  "No description".equals( description )) 
	    msg=" (" + httpClient.getRequestLine() + ")";
	else
	    msg=description + " (" + httpClient.getRequestLine() + ")";

	if(result) 
	    out.println("OK " + msg );
	else 
	    out.println("FAIL " + msg );

	if( ex!=null)
	    ex.printStackTrace(out);
    }

    
}
