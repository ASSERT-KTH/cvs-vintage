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
import org.apache.tomcat.util.test.matchers.*;

/** Original tester for a web application. Will send a http request and
    verify the response code, compare the response with a golden
    file or find strings.

    This class is using the well-known ant patterns.

    @deprecated Use HttpClient instead. This class has very limited
                support for multiple matchers, requests, etc.
*/
public class GTest  {
    // Defaults
    static PrintWriter defaultOutput=new PrintWriter(System.out);
    static String defaultOutType="text";
    static int defaultDebug=0;
    static boolean failureOnly=false;

    // all test results will be available
    static Hashtable testProperties=new Hashtable();

    // Instance variables

    // The "real" thing.
    // GTest is here to support the old ( and simpler ) syntax .
    // The real work is done in HttpClient, which is a lot more
    // powerfull. For example it can handle multiple requests and
    // matches, etc
    HttpClient httpClient=new HttpClient();

    // Gtest supports only one request. 
    HttpRequest httpRequest=new HttpRequest();

    String failMessage="";
    
    PrintWriter out=null;
    String outType=null;
    int debug=-1;
    
    boolean result=false;
    
    public GTest() {
	httpClient.setDebug( debug );
	httpClient.addHttpRequest( httpRequest );
	//	httpClient.addMatcher( matcher );
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
	return HttpClient.getTestResults();
    }

    /** Vector of GTest elements, containing all test instances
     *  that were run and failed.
     */
    public static Vector getTestFailures() {
	return HttpClient.getTestFailures();
    }

    /** Vector of GTest elements, containing all test instances
     *  that were run and failed.
     */
    public static Vector getTestSuccess() {
	return HttpClient.getTestSuccess();
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

    // -------------------- Getters --------------------

    public HttpClient getHttpClient() {
	return httpClient;
    }

    public String getComment() {
	return httpClient.getComment();
    }

    // -------------------- Local properties --------------------
    /** Description should be in <test description=""/>
     */
    public String getDescription() {
	return httpClient.getComment();
    }

    public void setDescription(String description) {
	httpClient.setDescription(description);
    }

    public String getMatchDescription() {
	Matcher m=httpClient.getFailingMatch();
	if( m==null ) return "";
	return m.getTestDescription();
    }

    public String getFailureMessage() {
	return httpClient.getFailureMessage();
    }
    
    /** Display debug info
     */
    public void setDebug( String debugS ) {
	debug=Integer.valueOf( debugS).intValue();
	//matcher.setDebug( debug );
	httpClient.setDebug( debug );
    }

    // -------------------- Client properties --------------------
    public void setHost(String h) {
	httpRequest.setHost(h);
    }
    
    public void setPort(String portS) {
	httpRequest.setPort( portS );
    }

    /** Set the port as int - different name to avoid confusing ant
     */
    public void setPortInt(int i) {
	httpRequest.setPortInt(i);
    }

    /** Do a POST with the specified content
     */
    public void setContent(String s) {
	httpRequest.setContent(s);
    }

    /** Request line ( will have the host and context path prefix)
     */
    public void setRequest( String s ) {
	httpRequest.setRequestLine(s);
    }
    
    /** Send additional headers
     *  The value is a "|" separated list of headers to send
     */
    public void setHeaders( String s ) {
	httpRequest.setHeaders( s );
    }

    // -------------------- Matcher properties --------------------

    // @deprecated Use defaultMatcher childs, this allow only one test !!!

    // GTest supports 5 different matches in a single element.

    boolean exactMatch=false;
    boolean magnitude=true;
    String goldenFile;
    String expectedHeader;
    String responseMatch;
    String responseMatchFile;
    String returnCode;
    
    public void setExactMatch(boolean exact) {
	exactMatch=exact;
    }

    /** True if this is a positive test, false for negative
     */
    public void setMagnitude( boolean m ) {
	magnitude=m;
    }

    /** Compare with the golden file
     */
    public void setGoldenFile( String s ) {
	goldenFile=s;
    }

    /** Verify that response includes the expected headers
     *  The value is a "|" separated list of headers to expect.
     */
    public void setExpectHeaders( String s ) {
	expectedHeader=s;
    }

    /** Verify that response match the string
     */
    public void setResponseMatch( String s ) {
	responseMatch=s;
    }

    /** Verify that response matches a list of strings in a file
     */
    public void setResponseMatchFile( String s ) {
	responseMatchFile=s;
    }

    /** Verify the response code
     */
    public void setReturnCode( String s ) {
	returnCode=s;
    }

    // -------------------- Execute the request --------------------

    public void execute() {
	try {
	    //	 System.out.println("XXX " + outType + " " + defaultOutType);
	    if( out==null) out=defaultOutput;
	    if( outType==null) outType=defaultOutType;
	    if( debug==-1) debug=defaultDebug;

	    initMatchers();
	    httpClient.execute();
	    HttpResponse resp=httpRequest.getHttpResponse();

	    result=httpClient.getResult();

	    if( "text".equals(outType) )
		textReport();
	    if( "html".equals(outType) )
		htmlReport();
	} catch(Exception ex ) {
	    // no exception should be thrown in normal operation
	    ex.printStackTrace();
	}

    }

    
    // -------------------- Internal methods --------------------

    private void initMatchers( ) {
	if( goldenFile != null ) {
	    GoldenMatch gm=new GoldenMatch();
	    gm.setFile( goldenFile );
	    gm.setExactMatch( exactMatch );
	    gm.setExpectedResult( magnitude );
	    httpClient.addMatcher( gm );
	}
	if( expectedHeader != null ) {
	    HeaderMatch hm=new HeaderMatch();
	    hm.setExpectHeaders( expectedHeader );
	    hm.setExpectedResult( magnitude );
	    httpClient.addMatcher( hm );
	}

	if( responseMatch != null ) {
	    ResponseMatch rm=new ResponseMatch();
	    rm.setMatch( responseMatch );
	    rm.setExpectedResult( magnitude );
	    httpClient.addMatcher( rm );
	}
	
	if( responseMatchFile != null ) {
	    ResponseMatchFile rf=new ResponseMatchFile();
	    rf.setFile( responseMatchFile );
	    rf.setExpectedResult( magnitude );
	    httpClient.addMatcher( rf );
	}
	if( returnCode != null ) {
	    HttpStatusMatch sm=new HttpStatusMatch();
	    sm.setMatch( returnCode );
	    sm.setExpectedResult( magnitude );
	    httpClient.addMatcher(sm );
	}
    }
    
    private void textReport() {
	String msg=null;
	if(  "".equals( getDescription() )) 
	    msg=" (" + httpRequest.getRequestLine() + ")";
	else
	    msg=getDescription() + " (" + httpRequest.getRequestLine() + ")";

	if( result ) 
	    out.println("OK " +  msg );
	else {
	    out.println("FAIL " + msg );
	    out.println("Message: " + failMessage);
	}
	out.flush();
    }

    private void htmlReport() {
	String uri=httpRequest.getURI();
	if( uri!=null )
	    out.println("<a href='" + uri + "'>");
	if( result )
	    out.println( "OK " );
	else
	    out.println("<font color='red'>FAIL ");
	if( uri!=null )
	    out.println("</a>");

	String msg=null;
	if(  "".equals( getDescription() )) 
	    msg=" (" + httpRequest.getRequestLine() + ")";
	else
	    msg=getDescription() + " (" + httpRequest.getRequestLine() + ")";

	out.println( msg );
	
	if( ! result )
	    out.println("</font>");
	
	out.println("<br>");

	if( ! result ) {
	    out.println("<b>Message:</b><pre>");
	    out.println( failMessage);
	    out.println("</pre>");
	}

	if( ! result && debug > 0 ) {
	    out.println("<b>Request: </b><pre>" + httpRequest.getFullRequest());
	    out.println("</pre><b>Response:</b> " +
			httpRequest.getHttpResponse().getResponseLine());
	    out.println("<br><b>Response headers:</b><br>");
	    Hashtable headerH=httpRequest.getHttpResponse().getHeaders();
	    Enumeration hE=headerH.elements();
	    while( hE.hasMoreElements() ) {
		Header h=(Header) hE.nextElement();
		out.println("<b>" + h.getName() + ":</b>" +
			    h.getValue() + "<br>");
	    }
	    out.println("<b>Response body:</b><pre> ");
	    out.println(httpRequest.getHttpResponse().getResponseBody());
	    out.println("</pre>");
	}

	Throwable ex=httpRequest.getHttpResponse().getThrowable();
	if( ex!=null) {
	    out.println("<b>Exception</b><pre>");
	    ex.printStackTrace(out);
	    out.println("</pre><br>");
	}
	out.flush();
    }

    
}
