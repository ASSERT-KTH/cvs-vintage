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


/**
 *  Part of GTest - send a Http request. This tool gives a lot 
 *  of control over the request, and is usable with ant ( testing
 *  is also a part of the build process :-) or other xml-tools
 *  using similar patterns.
 *
 *  
 */
public class HttpClient {
    static String CRLF="\r\n";
    String host="localhost";
    int port=8080;

    int debug=0;

    String requestLine;
    Hashtable requestHeaders;
    Body body;
    
    String requestBody;
    
    // Response resulted from this request
    Response response;

    public HttpClient() {
    }

    /** Server that will receive the request
     */
    public void setHost(String h) {
	this.host=h;
    }

    /** The port used to send the request
     */
    public void setPort(String portS) {
	this.port=Integer.valueOf( portS).intValue();
    }

    /** Set the port as int - different name to avoid confusing introspection
     */
    public void setPortInt(int i) {
	this.port=i;
    }

    /** Do a POST with the specified content.
     */
    public void setContent(String s) {
	body=new Body( s );
    }

    /** Add content to the request, for POST ( alternate method )
     */
    public void addBody( Body b ) {
	body=b;
    }
    

    public void addHeader( String n, String v ) {
	requestHeaders.put(n, new Header( n, v) );
    }

    /** Add a header to the request
     */
    public void addHeader( Header rh ) {
	requestHeaders.put( rh.getName(), rh );
    }

    /** Add headers - string representation, will be parsed
     *  The value is a "|" separated list of headers to expect.
     *  It's preferable to use the other 2 methods.
     */
    public void setHeaders( String s ) {
       requestHeaders=new Hashtable();
       Header.parseHeadersAsString( s, requestHeaders );
    }


    /** Add a parameter to the request
     *  XXX not implemented
     */
    public void addParameter( Parameter rp ) {
    }

    /** Display debug info
     */
    public void setDebug( String debugS ) {
	debug=Integer.valueOf( debugS).intValue();
    }

    /** Verbose request line - including method and protocol
     */
    public void setRequestLine( String s ) {
	this.requestLine=s;
    }
    
    public String getRequestLine( ) {
	return requestLine;
    }
    
    /** Allow sending a verbose request
     */
    public void setVerboseRequest( String s ) {
	requestBody=s;
    }

    /** Alternate method for sending a verbose request
     */
    public void addText(String s ) {
	requestBody=s;
    }

    // -------------------- Access the response --------------------

    public Response getResponse() {
	return response;
    }

    // -------------------- Execute the request --------------------

    public void execute() {
	try {
	    dispatch();
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}
    }

    /** 
     */
    private void prepareRequest() 
	throws Exception
    {
	// explicitely set
	if( requestBody != null ) return;

	// use the existing info to compose what will be sent to the
	// server
	StringBuffer sb=new StringBuffer();
	sb.append(requestLine).append(CRLF);

	// We may test HTTP0.9 behavior. If it's post 1.0, it needs
	// a LF
	if( requestLine.indexOf( "HTTP/1." ) <0 ) {
	    requestBody=sb.toString();
	    return; // nothing to add
	}

	String contentL=null;
	
	// headers
	Enumeration headersE=requestHeaders.elements();
	while( headersE.hasMoreElements() ) {
	    Header h=(Header)headersE.nextElement();
	    sb.append(h.toString()).append( CRLF );
	    if( "Content-Length".equals( h.getName() )) {
		contentL=h.getValue();
	    }
	}

	// If we have a body
	if( body != null) {
	    // If set explicitely ( maybe we're testing bad POSTs )
	    if( contentL==null ) {
		sb.append("Content-Length: ").append( body.getBody().length());
		sb.append(CRLF);
	    }
	    
	    sb.append(body.getBody());
	    // no /n at the end -see HTTP specs!
	    // If we want to test bad POST - set Content-Length
	    // explicitely.
	}

	// set the requestBody
	requestBody=sb.toString();
    }

    /** Invoke a request, set headers, responseLine, body
     *  We use plain socket ( instead of the more convenient URLConnection)
     *  because we want to check bad http, special strings, etc.
     */
    private void dispatch()
	throws Exception
    {
	// connect
	Socket s = new Socket( host, port);
	s.setSoLinger( true, 1000);

	InputStream is=	s.getInputStream();
	OutputStream os=s.getOutputStream();
	OutputStreamWriter out=new OutputStreamWriter(os);
	PrintWriter pw = new PrintWriter(out);

	// Write the request
	try {
	    os.write(requestBody.getBytes()); // XXX encoding !
	    os.flush();
	} catch (Exception ex1 ) {
	    response.setThrowable( ex1 );
	    return;
	}
	
	try {
	    // http 1.0 +
	    if( requestBody.indexOf( "HTTP/1." ) > -1) {
		String responseLine = read( is );
		response.setResponseLine( responseLine );
		
		Hashtable headers=Header.parseHeaders( is );
		response.setHeaders( headers );
	    }

	    StringBuffer result = readBody( is );
	    if(result!=null)
		response.setResponseBody( result.toString() );
	    
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
		//		log("XXX " + (char)ch );
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

}
