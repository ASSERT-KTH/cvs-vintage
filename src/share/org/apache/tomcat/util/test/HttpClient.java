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

import org.apache.tomcat.util.test.matchers.*;
import org.apache.tools.ant.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;


/**
 *  HttpClient can send requests and execute matchers against the request.
 *  This is the main tool that is used to test tomcat's web applications.
 *  Typical use:
 *  <pre>
 *      <httpClient>
 *        <request/>
 *        <matcher/>
 *      </httpClient>
 *  </pre>
 *  Part of GTest - send a Http request. 
 */
public class HttpClient {
    static Report defaultReport=new Report();

    Project project=null;
    String ifProp=null;
    String unlessProp=null;
    HttpRequest firstRequest=null;
    Vector actions=new Vector();
    String id;
    int debug=0;
    Body comment=null;
    boolean success=true;

    PrintWriter out=null;
    String outType=null;
    
    public HttpClient() {
    }

    public void setProject(Project p ) {
        project=p;
    }

    public Project getProject() {
        return project;
    }

    public void setIf(String prop) {
        ifProp=prop;
    }

    public void setUnless(String prop) {
        unlessProp=prop;
    }

    /** Set an unique id to this request. This allows it to be
     *  referenced later, for complex tests/matchers that look
     * 	at multiple requests.
     */
    public void setId(String id) {
	this.id=id;
    }

    /** Display debug info
     */
    public void setDebug( int d ) {
	debug=d;
    }

    public int getDebug() {
        return debug;
    }
    
    /** Add a request that will be executed.
     */
    public void addHttpRequest( HttpRequest b ) {
	b.setHttpClient( this );
	if( firstRequest == null ) firstRequest=b;
	actions.addElement( b );
    }

    public Body createComment() {
	comment=new Body();
	return comment;
    }

    public String getComment() {
	if(comment==null) return "";
	return comment.getText();
    }

    public void setDescription( String s ) {
	comment=new Body( s );
    }

    public void setWriter( PrintWriter pw ) {
        out=pw;
    }

    public void setOutput( String t ) {
        outType=t;
    }
    
    // -------------------- Various matchers --------------------

    /** Add a matcher.
     */
    public void addMatcher( Matcher m ) {
	m.setHttpClient( this );
	actions.addElement( m );
    }

    // XXX Ant is not able to handle generic addXXX, we need to add
    // individual methods for each matcher

    public void addGoldenMatch( GoldenMatch m ) {
	addMatcher( m );
    }
    public void addHeaderMatch( HeaderMatch m ) {
	addMatcher( m );
    }
    public void addHttpStatusMatch( HttpStatusMatch m ) {
	addMatcher( m );
    }
    public void addResponseMatch( ResponseMatch m ) {
	addMatcher( m );
    }
    public void addResponseMatchFile( ResponseMatchFile m ) {
	addMatcher( m );
    }
    public void addSessionMatch( SessionMatch m ) {
        addMatcher( m );
    }
    
    
    // -------------------- Access to the actions --------------------

    public HttpRequest getFirstRequest() {
	return firstRequest;
    }

    // -------------------- Result --------------------
    Matcher failingMatcher=null;

    public Matcher getFailingMatch() {
	return failingMatcher;
    }

    public String getFailureMessage() {
	if( failingMatcher==null ) return "";
	return failingMatcher.getMessage();
    }

    public boolean getResult() {
	return success;
    }
    
    // -------------------- Execute the request --------------------

    public void execute() {
        if( project != null ) {
            if( ifProp != null && project.getProperty(ifProp) == null)
                // skip if "if" property is not set
                return;
            if( unlessProp != null && project.getProperty(unlessProp) != null )
                // skip if "unless" property is set
                return;
        }
        HttpRequest lastRequest=null;
	try {
	    Enumeration aE=actions.elements();
	    while( aE.hasMoreElements() ) {
		Object action=aE.nextElement();
		if( action instanceof HttpRequest ) {
		    lastRequest=(HttpRequest)action;
		    dispatch(lastRequest);
		} else if( action instanceof Matcher ) {
		    Matcher matcher=(Matcher)action;
		    matcher.setHttpRequest( lastRequest );
		    matcher.setHttpResponse( lastRequest.getHttpResponse() );
		    matcher.execute();
		    boolean testResult=matcher.getResult();
		    if( ! testResult ) {
			success=false;
			failingMatcher=matcher;
			break;
		    }
		}
	    }
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}
	if( id!=null )
	    clients.put( id, this );

	// after execute() is done, add the test result to the list
	testResults.addElement( this );
	if( !success )
	    testFailures.addElement( this.getFailingMatch() );
	else
	    testSuccess.addElement( this );

        if( lastRequest != null) {
            defaultReport.setHttpClient(this);
            defaultReport.setHttpRequest(lastRequest);
            defaultReport.setWriter(out);
            defaultReport.setOutput(outType);
            defaultReport.writeReport();
        }
    }

    /** Invoke a request, set headers, responseLine, body
     *  We use plain socket ( instead of the more convenient URLConnection)
     *  because we want to check bad http, special strings, etc.
     */
    private void dispatch(HttpRequest req)
	throws Exception
    {
	// connect
	Socket s = new Socket( req.getHost(), req.getPort());
	s.setSoLinger( true, 1000);

	InputStream is=	s.getInputStream();
	OutputStream os=s.getOutputStream();
	OutputStreamWriter out=new OutputStreamWriter(os);
	PrintWriter pw = new PrintWriter(out);

	HttpResponse response=new HttpResponse();
	req.setHttpResponse( response );

	req.prepareRequest();
	String fullRequest=req.getFullRequest();
	if( debug > 5 ) {
	    System.out.println("--------------------Sending " );
	    System.out.println(fullRequest);
	    System.out.println("----------" );
	}
	// Write the request
	try {
	    os.write(fullRequest.getBytes()); // XXX encoding !
	    os.flush();
	} catch (Exception ex1 ) {
	    response.setThrowable( ex1 );
	    return;
	}
	
	try {
	    // http 1.0 +
	    if( fullRequest.indexOf( "HTTP/1." ) > -1) {
		if( debug > 5 )
		    System.out.println("Reading response " );
		String responseLine = read( is );
		if( debug > 5 )
		    System.out.println("Got: " + responseLine );
		response.setResponseLine( responseLine );
		
		Hashtable headers=Header.parseHeaders( is );
		if( debug > 5 )
		    System.out.println("Got headers: " + headers );
		response.setHeaders( headers );
	    }

	    StringBuffer result = readBody( is );
	    if(result!=null)
		response.setResponseBody( result.toString() );
	    if( debug > 5 )
		System.out.println("Got body: " + result );
	    
	} catch( SocketException ex ) {
	    response.setThrowable( ex );
	}
	try {
	    s.close();
	} catch(IOException ex ) {
	}
    }


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

    /**
     * Read a line from the specified servlet input stream, and strip off
     * the trailing carriage return and newline (if any).  Return the remaining
     * characters that were read as a string.
     *
     * @returns The line that was read, or <code>null</code> if end of file
     *  was encountered
     *
     * @exception IOException if an input/output error occurred
     */
    public static String read(InputStream input) throws IOException {
	// Read the next line from the input stream
	StringBuffer sb = new StringBuffer();
	while (true) {
	    try {
		int ch = input.read();
		// System.out.println("XXX " + (char)ch );
		if (ch < 0) {
		    if (sb.length() == 0) {
			//  if(debug>0) log("Error reading line " + ch + " " +
			// 			sb.toString() );
			return "";
		    } else {
			break;
		    }
		} else if (ch == '\n') {
		    break;
		}
		sb.append((char) ch);
	    } catch( IOException ex ) {
		System.out.println("Error reading : " + ex );
		// debug=1;
		// if(debug>0) log("Partial read: " + sb.toString());
		ex.printStackTrace();
		//break;
	    }
	}

	// Strip any trailing carriage return
	int n = sb.length();
	if ((n > 0) && (sb.charAt(n - 1) == '\r')) {
	    sb.setLength(n - 1);
	}

	// Convert the line to a String and return it
	return (sb.toString());
    }

    // -------------------- Client registry --------------------
    // all test results will be available
    static Vector testResults=new Vector();
    static Vector testFailures=new Vector();
    static Vector testSuccess=new Vector();


    static Hashtable clients=new Hashtable();

    /** Return one of the "named" clients that have been executed so far.
     */
    public static Hashtable getHttpClients() {
	return clients;
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


    // --------------------

    public String getMatchDescription() {
	return "";
    }
}
