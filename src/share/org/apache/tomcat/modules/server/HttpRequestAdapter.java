/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/modules/server/Attic/HttpRequestAdapter.java,v 1.1 2000/09/17 06:37:52 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2000/09/17 06:37:52 $
 *
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


package org.apache.tomcat.modules.server;

import org.apache.tomcat.core.*;
import org.apache.tomcat.helper.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.logging.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class HttpRequestAdapter extends Request {
    static StringManager sm = StringManager.getManager("org.apache.tomcat.resources");
    private Socket socket;
    private boolean moreRequests = false;
    RecycleBufferedInputStream sin;
    byte[] buf;
    int bufSize=2048; // default
    int off=0;
    int count=0;
    public static final String DEFAULT_CHARACTER_ENCODING = "8859_1";
    
    Logger.Helper loghelper = new Logger.Helper("tc_log", this);
    
    public HttpRequestAdapter() {
        super();
	buf=new byte[bufSize];
    }

    public void setSocket(Socket socket) throws IOException {
	if( sin==null)
	    sin = new RecycleBufferedInputStream ( socket.getInputStream());
	else
	    sin.setInputStream( socket.getInputStream());
        this.socket = socket;
    	moreRequests = true;
    }

    public void recycle() {
	super.recycle();
	off=0;
	count=0;
	if( sin!=null )  sin.recycle();
    }
    
    public Socket getSocket() {
        return this.socket;
    }

    public boolean hasMoreRequests() {
        return moreRequests;
    }
    
    public int doRead() throws IOException {
	return sin.read();
    }

    public int doRead(byte[] b, int off, int len) throws IOException {
	return sin.read(b, off, len);
    }

    // cut&paste from ServletInputStream - but it's as inefficient as before
    public int readLine(InputStream in, byte[] b, int off, int len) throws IOException {

	if (len <= 0) {
	    return 0;
	}
	int count = 0, c;

	while ((c = in.read()) != -1) {
	    b[off++] = (byte)c;
	    count++;
	    if (c == '\n' || count == len) {
		break;
	    }
	}
	return count > 0 ? count : -1;
    }
    

    
    public void readNextRequest(Response response) throws IOException {

	count = readLine(sin,buf, 0, buf.length);

	if (count < 0 ) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    return;
	}
	
	processRequestLine(response  );

	// for 0.9, we don't have headers!
	if (protocol!=null) { // all HTTP versions with protocol also have headers ( 0.9 has no HTTP/0.9 !)
	    readHeaders( headers, in  );
	}

	// XXX
	// detect for real whether or not we have more requests
	// coming
	moreRequests = false;	
    }


    /**
     * Reads header fields from the specified servlet input stream until
     * a blank line is encountered.
     * @param in the servlet input stream
     * @exception IllegalArgumentException if the header format was invalid 
     * @exception IOException if an I/O error has occurred
     */
    public void readHeaders( MimeHeaders headers, ServletInputStream in )  throws IOException {
	// use pre-allocated buffer if possible
	off = count; // where the request line ended
	
	while (true) {
	    int start = off;

	    while (true) {
		int len = buf.length - off;

		if (len > 0) {
		    len = readLine(sin,buf, off, len);

		    if (len == -1) {
                        String msg =
                            sm.getString("mimeHeader.connection.ioe");

			throw new IOException (msg);
		    }
		}

		off += len;

		if (len == 0 || buf[off-1] == '\n') {
		    break;
		}

		// overflowed buffer, so temporarily expand and continue

		// XXX DOS - if the length is too big - stop and throw exception
		byte[] tmp = new byte[buf.length * 2];

		System.arraycopy(buf, 0, tmp, 0, buf.length);
		buf = tmp;
	    }

	    // strip off trailing "\r\n"
	    if (--off > start && buf[off-1] == '\r') {
		--off;
	    }

	    if (off == start) {
		break;
	    }
	    
	    // XXX this does not currently handle headers which
	    // are folded to take more than one line.
	    if( ! parseHeaderFiled(headers, buf, start, off - start) ) {
		// error parsing header
		return;
	    }
	}
    }

    /**
     * Parses a header field from a subarray of bytes.
     * @param b the bytes to parse
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     * @exception IllegalArgumentException if the header format was invalid
     */
    public boolean parseHeaderFiled(MimeHeaders headers, byte[] b, int off,
				    int len)
    {
	int start = off;
	byte c;

	while ((c = b[off++]) != ':' && c != ' ') {
	    if (c == '\n') {
		loghelper.log("Parse error, empty line: " +
			      new String( b, off, len ), Logger.ERROR);
		return false;
	    }
	}

	int nS=start;
	int nE=off - start - 1;

	while (c == ' ') {
	    c = b[off++];
	}

	if (c != ':') {
	    loghelper.log("Parse error, missing : in  " +
			  new String( b, off, len ), Logger.ERROR);
	    loghelper.log("Full  " + new String( b, 0, b.length ),
			  Logger.ERROR);
	    return false;
	}

	while ((c = b[off++]) == ' ');

	headers.addValue( b, nS, nE).
	    setBytes(b, off-1, len - (off - start - 1));
	return true;
    }

    public int getServerPort() {
        return socket.getLocalPort();
    }

    public String getServerName() {
	if(serverName!=null) return serverName;
	
	// XXX Move to interceptor!!!
	String hostHeader = this.getHeader("host");
	if (hostHeader != null) {
	    int i = hostHeader.indexOf(':');
	    if (i > -1) {
		hostHeader = hostHeader.substring(0,i);
	    }
	    serverName=hostHeader;
	    return serverName;
	}

	if (hostHeader == null) {
		// XXX
		// we need a better solution here
		InetAddress localAddress = socket.getLocalAddress();
		serverName = localAddress.getHostName();
	}
	return serverName;
    }
    
    
    public String getRemoteAddr() {
        return socket.getInetAddress().getHostAddress();
    }
    
    public String getRemoteHost() {
	return socket.getInetAddress().getHostName();
    }    

    /** Advance to first non-whitespace
     */
    private  final int skipSpaces() {
	while (off < count) {
	    if ((buf[off] != (byte) ' ') 
		&& (buf[off] != (byte) '\t')
		&& (buf[off] != (byte) '\r')
		&& (buf[off] != (byte) '\n')) {
		return off;
	    }
	    off++;
	}
	return -1;
    }

    /** Advance to the first whitespace character
     */
    private  int findSpace() {
	while (off < count) {
	    if ((buf[off] == (byte) ' ') 
		|| (buf[off] == (byte) '\t')
		|| (buf[off] == (byte) '\r')
		|| (buf[off] == (byte) '\n')) {
		return off;
	    }
	    off++;
	}
	return -1;
    }

    /** Find a character, no side effects
     */
    private  int findChar( char c, int start, int end ) {
	byte b=(byte)c;
	int offset = start;
	while (offset < end) {
	    if (buf[offset] == b) {
		return offset;
	    }
	    offset++;
	}
	return -1;
    }

    
    private void processRequestLine(Response response)
	throws IOException
    {
	off=0;

	// if end of line is reached before we scan all 3 components -
	// we're fine, off=count and remain unchanged
	
	if( buf[count-1]!= '\r' && buf[count-1]!= '\n' ) {
	    response.setStatus(HttpServletResponse.SC_REQUEST_URI_TOO_LONG);
	    return;
	}	    
	
	int startMethod=skipSpaces();
	int endMethod=findSpace();

	int startReq=skipSpaces();
	int endReq=findSpace();

	int startProto=skipSpaces();
	int endProto=findSpace();

	if( startReq < 0   ) {
	    // we don't have 2 "words", probably only method
	    // startReq>0 => method is fine, request has at least one char
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    return;
	}

	methodMB.setBytes( buf, startMethod, endMethod - startMethod );
	method=null;
	if( Ascii.toLower( buf[startMethod]) == 'g' ) {
	    if( methodMB.equalsIgnoreCase( "get" ))
		method="GET";
	}
	if( Ascii.toLower( buf[startMethod]) == 'p' ) {
	    if( methodMB.equalsIgnoreCase( "post" ))
		method="POST";
	    if( methodMB.equalsIgnoreCase( "put" ))
		method="PUT";
	}

	if( method==null )
	    method= new String( buf, startMethod, endMethod - startMethod );

	protocol=null;
	if( endReq < 0 ) {
	    endReq=count;
	} else {
	    if( startProto > 0 ) {
		if( endProto < 0 ) endProto = count;
		protoMB.setBytes( buf, startProto, endProto-startProto);
		if( protoMB.equalsIgnoreCase( "http/1.0" ))
		    protocol="HTTP/1.0";
		if( protoMB.equalsIgnoreCase( "http/1.1" ))
		    protocol="HTTP/1.1";
		
		if( protocol==null) 
		    protocol=new String( buf, startProto, endProto-startProto );
	    }
	}

	int qryIdx= findChar( '?', startReq, endReq );
	if( qryIdx <0 ) {
	    uriMB.setBytes(buf, startReq, endReq - startReq );
	    //= new String( buf, startReq, endReq - startReq );
	} else {
	    uriMB.setBytes( buf, startReq, qryIdx - startReq );
	    queryMB.setBytes( buf, qryIdx+1, endReq - qryIdx -1 );
	}

	// temp. fix until the rest of the code is changed
	requestURI=uriMB.toString();
	queryString=queryMB.toString();

	// Perform URL decoding only if necessary
	if ((uriMB.indexOf('%') >= 0) || (uriMB.indexOf('+') >= 0)) {

	    try {
		// XXX rewrite URLDecode to avoid allocation
		requestURI = uriMB.toString();
		requestURI = RequestUtil.URLDecode(requestURI);
	    } catch (Exception e) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return;
	    }
	}

	//	loghelper.log("XXX " + method + " " + requestURI + " " + queryString + " " + protocol );

    }

    
}
