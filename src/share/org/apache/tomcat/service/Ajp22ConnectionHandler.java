/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/Attic/Ajp22ConnectionHandler.java,v 1.1 1999/10/09 00:20:48 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:20:48 $
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


package org.apache.tomcat.service;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
//import org.apache.tomcat.server.*;
import javax.servlet.*;
import javax.servlet.http.*;

/* Deprecated - must be rewriten to the connector model.  
 */
public class Ajp22ConnectionHandler  implements  TcpConnectionHandler {
    ContextManager contextM;

    public Ajp22ConnectionHandler() {
    }

    public void init( ) {
    }

    public void setAttribute(String name, Object value ) {
	if("context.manager".equals(name) ) {
	    contextM=(ContextManager)value;
	}
    }
    
    
    public void setContextManager( ContextManager contextM ) {
	this.contextM=contextM;
    }
    
    // XXX
    //    Nothing overriden, right now AJPRequest implment AJP and read everything.
    //    "Shortcuts" to be added here ( Vhost and context set by Apache, etc)
    // XXX handleEndpoint( Endpoint x )
    public void processConnection(TcpConnection connection) {
	Ajp22Response rresponse=null;
	Ajp22Request  rrequest=null;
    
	Socket socket;

	try {
	    MessageConnector msg=null;
	    boolean recycle_is_broken=false;
	    
	    socket=connection.getSocket();
	    
	    if( ! recycle_is_broken ) {
		msg=new MessageConnector(socket);
		rresponse = new Ajp22Response(msg);
		//		rresponse.setProtocol( this );
		rrequest = new Ajp22Request(msg);
		rrequest.setResponse(rresponse);
		rresponse.setRequest(rrequest);
	    }
	    boolean moreRequests=true;
            while( moreRequests ) { // XXX how to exit ? // request.hasMoreRequests()) {
		if(recycle_is_broken) {
		    // create new objects for GC until reuse is fixed
		    msg=new MessageConnector(socket);
		    rresponse = new Ajp22Response(msg);
		    //		    rresponse.setProtocol( this );
		    rrequest = new Ajp22Request(msg);
		    rrequest.setResponse(rresponse);
		    rresponse.setRequest(rrequest);
		}
		
		// XXX this should be implemented here!
		try {
		    int err=rrequest.readNextRequest();
		    if( err<0 ) {
			moreRequests=false;
			break;
		    }
		} catch( IOException ex ) {
		    break;
		}

		// XXX
                //    return if an error was detected in processing the
                //    request line
		if (rresponse.getStatus() >= 400) {
		    rresponse.finish();
		    rrequest.recycle();
		    rresponse.recycle();
		    break;
		}

		
		// resolve the server that we are for
		String path = rrequest.getRequestURI();
	
		Context ctx= contextM.getContextByPath(path);

		// final fix on response & request
		//		rresponse.setServerHeader(server.getServerHeader());

		String ctxPath = ctx.getPath();
		String pathInfo =path.substring(ctxPath.length(),
						path.length());
                //    don't do headers if request protocol is http/0.9
		if (rrequest.getProtocol() == null) {
		    rresponse.setOmitHeaders(true);
		}

		// do it
		//		System.out.println( request + " " + rresponse );
		ctx.handleRequest(rrequest, rresponse);

		// finish and clean up
		rresponse.finish();

		// protocol notification
		msg.endResponse();
		
		rrequest.recycle();
		rresponse.recycle();
            }

	    //	    System.out.println("Closing socket");
	    System.out.println("Closing connection");
	    socket.close();
	} catch (Exception e) {
            // XXX
	    // this isn't what we want, we want to log the problem somehow
	    System.out.println("HANDLER THREAD PROBLEM: " + e);
	    e.printStackTrace();
	}

	// recycle ourselves
	//manager.returnHandler(this);
    }

}

interface Ajp22Constants {
    public static final int END_RESPONSE=5;
    public static final int SEND_BODY_CHUNK=3;
    public static final int MAX_PACKET_SIZE=4096;
    public static final int SEND_HEADERS=2;
    
    static final int H_SIZE=4;
    static final int CONTEXT='C';
    static final int SERVLET='S';
    static final int HOSTNAME='N';
    static final int ENV='E';
    static final int HEADER='H';
    static final int END_REQUEST='Z';
    
    
    static final int MAX_REQUEST_SIZE=4096;

}

// XXX bad code !!!
// It's a sort of struct, so we don't pass in/out around + few methods
class MessageConnector implements Ajp22Constants {
    OutputStream out;
    InputStream in;
    
    byte buff[]=new byte[MAX_PACKET_SIZE];
    int len;
 
    public MessageConnector ( Socket socket ) throws IOException {
	socket.setSoLinger( true, 100);
	
	out = socket.getOutputStream();
	in = socket.getInputStream();
    }    

    // Connection Handler knows the protocol - it should be the only thing to change
     
    // XXX rewrite the whole thing, only one write per buff
    // ( pre-set header )
    public void endResponse( ) throws IOException {
	// reuse buffer - the protocol is used by only one thread ( it should :-)
	//	 System.out.println("End Response");
	buff[0]=(byte)'A';
	buff[1]=(byte)'B';
	BuffTool.addInt( buff, 2, 2 ); 
	BuffTool.addInt( buff, 4, END_RESPONSE );
	out.write( buff, 0 , 6 );
	out.flush();
	
	 //	 BuffTool.dump( buff, 6);
    }

    
    public int readPacket() throws IOException {
	// Read Packet
	     
	int rd=in.read( buff, 0, H_SIZE );
	if( rd<=0 ) return -1;
	
	int mark=BuffTool.getInt( buff,0 );
	
	//  BuffTool.dump( buff, 4 );
	if( mark != 0x1234 ) {
	    System.out.println("BAD packet " + mark + " " + rd);
	    throw new IOException("BAD Packet ");
	}
	
	int pos=2;
	len=BuffTool.getInt( buff, 2);
	if(len > MAX_REQUEST_SIZE ) {
	    // XXX
	    //		System.out.println("New Buffer");
	    buff=new byte[len];
	    pos=0;
	}
	
	// XXX check if enough space - it's assert()-ed !!!
	// Can we have only one read ( with unblocking, it can read all at once - but maybe more ) ? 
	rd=in.read( buff, 0, len );
	return rd;
	//BuffTool.dump( buff, len  );
	//    System.out.println( "Incoming Packet len=" + len);
    }
}


class Ajp22Request extends RequestImpl {
    MessageConnector proto;
    Hashtable env_vars;

    public Ajp22Request( MessageConnector proto ) {
	super();
	this.proto=proto;
    }
    
    public void recycle() {
	super.recycle();
    }

    // Called only from ConnectionHandler - should be part of CH, since it is specific to CH
    // XXX Use setXXX instead!!!
    protected int readNextRequest() throws IOException {

	Ajp22ServletIS in = new Ajp22ServletIS(proto);
	this.in=in;

	env_vars=new Hashtable();

	int rd=proto.readPacket();
	if( rd<=0 ) {
	    return -1;
	}

	// Unmarshal
	// XXX We can just keep the buff, and unmarshal on demand
	// That will reduce a lot GC.
	// We need a data structure that is efficient for such use, O(1) for frequent used info
	byte buff[]=proto.buff;
	int pos=0;
	
	int reqType = BuffTool.getInt( buff, pos );
	pos+=2;
	//	System.out.println( "Request type: " + Integer.toHexString(reqType) + " pos=" + pos);
	
	int envCount = BuffTool.getInt ( buff, pos );
	// System.out.println( "Table count: " + envCount);
	pos+=2;
	
	for( int i=0; i<envCount; i++ ) {
	    int nlen = BuffTool.getInt( buff, pos );
	    String n=BuffTool.getString( buff, pos+2, nlen );
	    pos+=3 + nlen;
	    int vlen = BuffTool.getInt( buff, pos );
	    String v=BuffTool.getString( buff, pos+2, vlen );
	    pos+=3 + vlen;
	    
	    env_vars.put( n , v );
	    // System.out.println( "Env: " + n + "=" + v);
	}
	
	int hCount = BuffTool.getInt ( buff, pos );
	//System.out.println( "Header count: " + hCount);
	pos+=2;
	     
	     
	for( int i=0; i<hCount; i++ ) {
	    int nlen = BuffTool.getInt( buff, pos );
	    String n= BuffTool.getString( buff, pos+2, nlen );
	    pos+=3 + nlen;
	    int vlen = BuffTool.getInt( buff, pos );
	    String v= BuffTool.getString( buff, pos+2, vlen );
	    pos+=3 + vlen;
		 
	    headers.putHeader( n.toLowerCase() , v );
	    //System.out.println( "Head: " + n + "=" + v);
	}
	
	setInternalVars();

	processCookies();
	
	contentLength = headers.getIntHeader("content-length");
	contentType = headers.getHeader("content-type");
	charEncoding = getCharsetFromContentType(contentType);
	return 0;
    }    

    // -------------------- Get Request info from AJP server --------------------
    public int getServerPort() {
        String sport=(String)env_vars.get("SERVER_PORT");
	if(sport==null) sport="80";
	return new Integer(sport).intValue();
    }
    
    public String getRemoteAddr() {
        return (String)env_vars.get("REMOTE_ADDR");
    }
    
    public String getRemoteHost() {
	// todo: bug, fix it
	return (String)env_vars.get("REMOTE_ADDR");
    }    

    private void setInternalVars() {
	method= (String)env_vars.get("REQUEST_METHOD");
	protocol=(String)env_vars.get("SERVER_PROTOCOL");
	requestURI=(String)env_vars.get("REQUEST_URI");
	queryString=(String)env_vars.get("QUERY_STRING");
	if ((queryString != null ) && ! "".equals(queryString)) {
            processFormData(queryString);
        }
	if(requestURI==null) requestURI="xxx"; //XXX
	// XXX: fix it!
	if (requestURI.indexOf("?") > -1) {
	    //            queryString = requestURI.substring(
	    //			       requestURI.indexOf("?") + 1, requestURI.length());
            //processFormData(queryString);
	    requestURI = requestURI.substring(0, requestURI.indexOf("?"));
        }
	//	servletzone = (String )env_vars.get("CONTEXT");
	// servletname = (String )env_vars.get("HANDLER");
	String hostHeader = this.getHeader("host");
		
	if (hostHeader != null) {
	    // shave off the port part of the host header if
	    // it exists
	    
	    int i = hostHeader.indexOf(':');
	    
	    if (i > -1) {
		hostHeader = hostHeader.substring(0,i);
	    }
	    
	    this.setServerName(hostHeader);
	} else {
	    // XXX
	    // this is crap having to do this lookup -- we
	    // need a better solution
	    //    InetAddress localAddress = socket.getLocalAddress();
	    //rrequest.setServerName(localAddress.getHostName());
	    this.setServerName("localhost");
	}

		
    }
}


class Ajp22Response extends ResponseImpl implements Ajp22Constants {
    MessageConnector proto;
    Ajp22ServletOS rout;

    public Ajp22Response(MessageConnector proto) {
        super();
	this.proto=proto;
	rout=new Ajp22ServletOS(proto);
	rout.setResponse( this );
	this.out=rout;
    }

    public void recycle() {
	super.recycle();
	rout.recycle();
	out=rout;
    }

    protected void fixHeaders() throws IOException {
	//	System.out.println( "Fixing headers" );
	headers.putIntHeader("Status", status);
        headers.putHeader("Content-Type", contentType);

	// Generated by Server!!!
	//headers.putDateHeader("Date",System.currentTimeMillis());
        //headers.putHeader("Server",getServerHeader());

        if (contentLength != -1) {
            headers.putIntHeader("Content-Length", contentLength);
        }
	
        // write cookies
        Enumeration cookieEnum = null;
        cookieEnum = systemCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            headers.putHeader( CookieTools.getCookieHeaderName(c),
			       CookieTools.getCookieHeaderValue(c));
        }
        cookieEnum = userCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            headers.putHeader( CookieTools.getCookieHeaderName(c),
			       CookieTools.getCookieHeaderValue(c));
        }
	// XXX
        // do something with content encoding here
    }

    // XXX if more headers that MAX_SIZE, send 2 packets!   
    public void writeHeaders() throws IOException {
	//	System.out.println("Writing headers");
        if (omitHeaders) {
            return;
        }
	fixHeaders();
	
	byte buff[]=proto.buff; // use the cached buff
	buff[0]=(byte)'A';
	buff[1]=(byte)'B';
	int pos=0;
	int count=headers.size();
	
	// Marshal headers
	BuffTool.addInt( buff, 4, SEND_HEADERS);
	BuffTool.addInt( buff, 6, count );
	pos=8;
	
	Enumeration e = headers.names();
	while (e.hasMoreElements()) {
	    String headerName = (String)e.nextElement();
	    String headerValue = headers.getHeader(headerName);
	    pos=BuffTool.addString(buff, pos, headerName);
	    pos=BuffTool.addString(buff, pos, headerValue);
	}
	
	//	System.out.println("Total size=" + pos );
	BuffTool.addInt( buff, 2, pos-4); // fix packet length
	//	 BuffTool.dump ( buff, pos ); // length + header
	proto.out.write( buff, 0, pos );
     }
}

class Ajp22ServletIS extends BufferedServletInputStream {
    private InputStream in;
    MessageConnector proto;
    
    Ajp22ServletIS( MessageConnector proto) {
	super();
	this.proto=proto;
	this.in = null;
    }

    public int doRead() throws IOException {
	return in.read();
    }

    public int doRead(byte[] b, int off, int len) throws IOException {
	return in.read(b, off, len);
    }
}

class Ajp22ServletOS extends BufferedServletOutputStream implements Ajp22Constants { 
    protected Ajp22Response response;
    MessageConnector proto;
    
    // XXX clean up
    Ajp22ServletOS(MessageConnector proto) {
	this.proto=proto;
    }

    public void setResponse( Ajp22Response response ) {
	this.response=response;
    }

    public void doWrite(  byte b[], int off, int len) throws IOException {
	// XXX check if len > MAX_PACKET_SIZE !
	byte buff[]=proto.buff;
	buff[0]=(byte)'A';
	buff[1]=(byte)'B';
	BuffTool.addInt( buff, 2, len + 4 ); // !! No trailing 0, it's byte[]
	BuffTool.addInt( buff, 4, SEND_BODY_CHUNK);
	BuffTool.addInt( buff, 6, len );
	proto.out.write( buff, 0, 8 );
	//	BuffTool.dump( buff, 8);
	//	System.out.println("Writing body chunk " +off + " " + len );
	//	BuffTool.dump( b, len);
	proto.out.write( b, off, len );
	proto.out.flush();
    }

    protected void endResponse() throws IOException {
	//proto.endResponse();
    }

    protected void sendHeaders() throws IOException {
	//	System.out.println("SH");
	response.writeHeaders();
    }

}
