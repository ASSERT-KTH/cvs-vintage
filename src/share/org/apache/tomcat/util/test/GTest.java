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

package org.apache.tomcat.util.test;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.util.test.matchers.GoldenMatch;
import org.apache.tomcat.util.test.matchers.HeaderMatch;
import org.apache.tomcat.util.test.matchers.HttpStatusMatch;
import org.apache.tomcat.util.test.matchers.ResponseMatch;
import org.apache.tomcat.util.test.matchers.ResponseMatchFile;
import org.apache.tools.ant.Project;

/** Original tester for a web application. Will send a http request and
    verify the response code, compare the response with a golden
    file or find strings.

    This class is using the well-known ant patterns.

    @deprecated Use HttpClient instead. This class has very limited
                support for multiple matchers, requests, etc.
*/
public class GTest  {
    // Defaults
    static int defaultDebug=0;
    static boolean failureOnly=false;

    // all test results will be available
    static Hashtable testProperties=new Hashtable();

    // Instance variables

    Project project=null;
    String ifProp=null;
    String unlessProp=null;

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
	Report.setDefaultWriter(pw);
    }

    /** @deprecated. Output will be text or none, with external
	formater ( or event ? - it would be nice, but too serious, it's
	still a simple test runner )
    */
    public static void setDefaultOutput( String s ) {
	Report.setDefaultOutput(s);
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
    
    // ----------------- Ant Properties -----------------

    public void setProject(Project p ) {
        project=p;
    }

    public void setIf(String prop) {
        ifProp=prop;
    }

    public void setUnless(String prop) {
        unlessProp=prop;
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
	    if( debug==-1) debug=defaultDebug;

            httpClient.setProject(project);
            httpClient.setIf(ifProp);
            httpClient.setUnless(unlessProp);
	    initMatchers();
            httpClient.setWriter(out);
            httpClient.setOutput(outType);
            httpClient.setDebug(debug);
	    httpClient.execute();
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
    
}
