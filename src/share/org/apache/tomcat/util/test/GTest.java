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
    // Defaults
    static PrintWriter defaultOutput=new PrintWriter(System.out);
    static String defaultOutType="text";
    static int defaultDebug=0;
    static boolean failureOnly=false;

    // all test results will be available
    static Vector testResults=new Vector();
    static Vector testFailures=new Vector();
    static Vector testSuccess=new Vector();
    static Hashtable testProperties=new Hashtable();

    // Instance variables
    
    HttpClient httpClient=new HttpClient();
    DefaultMatcher matcher=new DefaultMatcher();
    Body comment=null;
    
    String description="No description";

    PrintWriter out=null;
    String outType=null;
    int debug=-1;
    
    boolean result=false;
    
    public GTest() {
	matcher.setDebug( debug );
	httpClient.setDebug( debug );
    }

    // -------------------- Defaults --------------------

    public static void setDefaultDebug( int d ) {
	defaultDebug=d;
    }

    public static void setDefaultWriter( PrintWriter pw ) {
	defaultOutput=pw;
    }

    /** @deprecated. Output will be text or none, with external
	formater ( or event ? - it would be nice, but too serious, it's
	still a simple test runner )
    */
    public static void setDefaultOutput( String s ) {
	defaultOutType=s;
    }

    /** Vector of GTest elements, containing all test instances
     *  that were run.
     */
    public static Vector getTestResults() {
	return testResults;
    }

    /** Vector of GTest elements, containing all test instances
     *  that were run and failed.
     */
    public static Vector getTestFailures() {
	return testFailures;
    }

    /** Vector of GTest elements, containing all test instances
     *  that were run and failed.
     */
    public static Vector getTestSuccess() {
	return testSuccess;
    }

    /** Various global test propertis
     */
    public static Hashtable getTestProperties() {
	return testProperties;
    }

    public static void resetGTest() {
	GTest.getTestResults().setSize(0);
	GTest.getTestFailures().setSize(0);
	GTest.getTestSuccess().setSize(0);
	GTest.getTestProperties().clear();
	HttpClient.getHttpClients().clear();
    }
    
    // -------------------- GTest behavior --------------------
    public void setWriter( PrintWriter pw ) {
	out=pw;
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

    public Body createComment() {
	comment=new Body();
	return comment;
    }
    // -------------------- Getters --------------------

    public HttpClient getHttpClient() {
	return httpClient;
    }
    
    public DefaultMatcher getMatcher() {
	return matcher;
    }

    public String getComment() {
	if(comment==null) return "";
	return comment.getText();
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
	matcher.setDebug( debug );
	httpClient.setDebug( debug );
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

    /** Verify that response matches a list of strings in a file
     */
    public void setResponseMatchFile( String s ) {
	matcher.setResponseMatchFile( s );
    }

    /** Verify the response code
     */
    public void setReturnCode( String s ) {
	matcher.setReturnCode( s );
    }

    // -------------------- Execute the request --------------------

    public void execute() {
	try {
	    //	 System.out.println("XXX " + outType + " " + defaultOutType);
	    if( out==null) out=defaultOutput;
	    if( outType==null) outType=defaultOutType;
	    if( debug==-1) debug=defaultDebug;

	    httpClient.execute();
	    Response resp=httpClient.getResponse();

	    matcher.setResponse( resp );
	    matcher.execute();
	    result=matcher.getResult();

	    // don't print OKs
	    if( result && failureOnly ) return;

	    if( "text".equals(outType) )
		textReport();
	    if( "html".equals(outType) )
		htmlReport();
	    if( "xml".equals(outType) )
		xmlReport();
	} catch(Exception ex ) {
	    // no exception should be thrown in normal operation
	    ex.printStackTrace();
	}

	// after execute() is done, add the test result to the list
	testResults.addElement( this );
	if( !result )
	    testFailures.addElement( this );
	else
	    testSuccess.addElement( this );
    }

    
    // -------------------- Internal methods --------------------

    private void textReport() {
	String msg=null;
	if(  "No description".equals( description )) 
	    msg=" (" + httpClient.getRequestLine() + ")";
	else
	    msg=description + " (" + httpClient.getRequestLine() + ")";

	if(matcher.getResult()) 
	    out.println("OK " +  msg );
	else {
	    out.println("FAIL " + msg );
	    out.println("Message: " + matcher.getMessage());
	}
	out.flush();
    }

    private void htmlReport() {
	boolean result=matcher.getResult();
	String uri=httpClient.getURI();
	if( uri!=null )
	    out.println("<a href='" + uri + "'>");
	if( result )
	    out.println( "OK " );
	else
	    out.println("<font color='red'>FAIL ");
	if( uri!=null )
	    out.println("</a>");

	String msg=null;
	if(  "No description".equals( description )) 
	    msg=" (" + httpClient.getRequestLine() + ")";
	else
	    msg=description + " (" + httpClient.getRequestLine() + ")";

	out.println( msg );
	
	if( ! result )
	    out.println("</font>");
	
	out.println("<br>");

	if( ! result ) {
	    out.println("<b>Message:</b><pre>");
	    out.println( matcher.getMessage());
	    out.println("</pre>");
	}

	if( ! result && debug > 0 ) {
	    out.println("<b>Request: </b><pre>" + httpClient.getFullRequest());
	    out.println("</pre><b>Response:</b> " +
			httpClient.getResponse().getResponseLine());
	    out.println("<br><b>Response headers:</b><br>");
	    Hashtable headerH=httpClient.getResponse().getHeaders();
	    Enumeration hE=headerH.elements();
	    while( hE.hasMoreElements() ) {
		Header h=(Header) hE.nextElement();
		out.println("<b>" + h.getName() + ":</b>" +
			    h.getValue() + "<br>");
	    }
	    out.println("<b>Response body:</b><pre> ");
	    out.println(httpClient.getResponse().getResponseBody());
	    out.println("</pre>");
	}

	Throwable ex=httpClient.getResponse().getThrowable();
	if( ex!=null) {
	    out.println("<b>Exception</b><pre>");
	    ex.printStackTrace(out);
	    out.println("</pre><br>");
	}
	out.flush();
    }

    private void xmlReport() {
	boolean result=matcher.getResult();
	String msg=null;
	if(  "No description".equals( description )) 
	    msg=" (" + httpClient.getRequestLine() + ")";
	else
	    msg=description + " (" + httpClient.getRequestLine() + ")";

	if(result) 
	    out.println("OK " + msg );
	else 
	    out.println("FAIL " + msg );

	Throwable ex=httpClient.getResponse().getThrowable();
	if( ex!=null) {
	    out.println("<b>Exception</b><pre>");
	    ex.printStackTrace(out);
	    out.println("</pre><br>");
	}
	out.flush();
    }

    
}
