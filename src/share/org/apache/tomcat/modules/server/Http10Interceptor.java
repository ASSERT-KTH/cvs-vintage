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

package org.apache.tomcat.modules.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.util.buf.DateTool;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.TcpConnection;
import org.apache.tomcat.util.net.TcpConnectionHandler;

/** Standalone http.
 *
 *  Connector properties:
 *  - secure - will load a SSL socket factory and act as https server
 *
 *  Properties passed to the net layer:
 *  - timeout
 *  - backlog
 *  - address
 *  - port
 * Thread pool properties:
 *  - minSpareThreads
 *  - maxSpareThreads
 *  - maxThreads
 *  - poolOn
 * Properties for HTTPS:
 *  - keystore - certificates - default to ~/.keystore
 *  - keypass - password
 *  - clientauth - true if the server should authenticate the client using certs
 * Properties for HTTP:
 *  - reportedname - name of server sent back to browser (security purposes)
 */
public class Http10Interceptor extends PoolTcpConnector
    implements  TcpConnectionHandler
{
    private int	timeout = 300000;	// 5 minutes as in Apache HTTPD server
    private String reportedname;
    private int socketCloseDelay=-1;

    public Http10Interceptor() {
	super();
        super.setSoLinger( 100 );
	// defaults:
	this.setPort( 8080 );
    }

    // -------------------- PoolTcpConnector --------------------

    protected void localInit() throws Exception {
	ep.setConnectionHandler( this );
    }

    // -------------------- Attributes --------------------
    public void setTimeout( int timeouts ) {
	timeout = timeouts * 1000;
    }
    public void setReportedname( String reportedName) {
    reportedname = reportedName;
    }

    public void setSocketCloseDelay( int d ) {
        socketCloseDelay=d;
    }

    public void setProperty( String prop, String value ) {
        setAttribute( prop, value );
    }

    // -------------------- Handler implementation --------------------
    public void setServer( Object o ) {
	this.cm=(ContextManager)o;
    }
    
    public Object[] init() {
	Object thData[]=new Object[3];
	HttpRequest reqA=new HttpRequest();
	HttpResponse resA=new HttpResponse();
	if (reportedname != null)
	    resA.setReported(reportedname);
	cm.initRequest( reqA, resA );
	thData[0]=reqA;
	thData[1]=resA;
	thData[2]=null;
	return  thData;
    }

    public void processConnection(TcpConnection connection, Object thData[]) {
	Socket socket=null;
	HttpRequest reqA=null;
	HttpResponse resA=null;

	try {
	    reqA=(HttpRequest)thData[0];
	    resA=(HttpResponse)thData[1];

	    socket=connection.getSocket();
		socket.setSoTimeout(timeout);

	    reqA.setSocket( socket );
	    resA.setSocket( socket );

	    reqA.readNextRequest(resA);
	    if( secure ) {
		reqA.scheme().setString( "https" );
 
 		// Load up the SSLSupport class
		if(sslImplementation != null)
		    reqA.setSSLSupport(sslImplementation.getSSLSupport(socket));
	    }
	    
	    cm.service( reqA, resA );

            // If unread input arrives after the shutdownInput() call
            // below and before or during the socket.close(), an error
            // may be reported to the client.  To help troubleshoot this
            // type of error, provide a configurable delay to give the
            // unread input time to arrive so it can be successfully read
            // and discarded by shutdownInput().
            if( socketCloseDelay >= 0 ) {
                try {
                    Thread.sleep(socketCloseDelay);
                } catch (InterruptedException ie) { /* ignore */ }
            }

        // XXX didn't honor HTTP/1.0 KeepAlive, should be fixed
	    TcpConnection.shutdownInput( socket );
	}
	catch(java.net.SocketException e) {
	    // SocketExceptions are normal
	    log( "SocketException reading request, ignored", null,
		 Log.INFORMATION);
	    log( "SocketException reading request:", e, Log.DEBUG);
	}
	catch (java.io.InterruptedIOException ioe) {
		// We have armed a timeout on read as does apache httpd server.
		// Just to avoid staying with inactive connection
		// BUG#1006
		ioe.printStackTrace();
		log( "Timeout reading request, aborting", ioe, Log.ERROR);
	}
	catch (java.io.IOException e) {
	    // IOExceptions are normal 
	    log( "IOException reading request, ignored", null,
		 Log.INFORMATION);
	    log( "IOException reading request:", e, Log.DEBUG);
	}
	// Future developers: if you discover any other
	// rare-but-nonfatal exceptions, catch them here, and log as
	// above.
	catch (Throwable e) {
	    // any other exception or error is odd. Here we log it
	    // with "ERROR" level, so it will show up even on
	    // less-than-verbose logs.
	    e.printStackTrace();
	    log( "Error reading request, ignored", e, Log.ERROR);
	} 
	finally {
	    // recycle kernel sockets ASAP
        // XXX didn't honor HTTP/1.0 KeepAlive, should be fixed
	    try { if (socket != null) socket.close (); }
	    catch (IOException e) { /* ignore */ }
        }
    }

    /** Internal constants for getInfo */ 
    private static final int GET_OTHER = 0;
    private static final int GET_CIPHER_SUITE = 1;
    private static final int GET_PEER_CERTIFICATE_CHAIN = 2;

     /**
       getInfo calls for SSL data
 
       @return the requested data
     */
     public Object getInfo( Context ctx, Request request,
 			   int id, String key ) {
       // The following code explicitly assumes that the only
       // attributes hand;ed here are HTTP. If you change that
       // you MUST change the test for sslSupport==null --EKR
 
       if (key != null) {
         int infoRequested = GET_OTHER;
         if(key.equals("javax.servlet.request.cipher_suite"))
           infoRequested = GET_CIPHER_SUITE;
         else if(key.equals("javax.servlet.request.X509Certificate"))
           infoRequested = GET_PEER_CERTIFICATE_CHAIN;

         if(infoRequested != GET_OTHER) {
           HttpRequest httpReq;

           try {
             httpReq=(HttpRequest)request;
           } catch (ClassCastException e){
             return null;
           }
 
           if (httpReq!=null && httpReq.sslSupport!=null){
             try {
               switch (infoRequested) {
                 case GET_CIPHER_SUITE:
                   return httpReq.sslSupport.getCipherSuite();
                 case GET_PEER_CERTIFICATE_CHAIN:
                   return httpReq.sslSupport.getPeerCertificateChain();
               }
             } catch (Exception e){
               log("Exception getting SSL attribute " + key,e,Log.WARNING);
               return null;
             }
           } // if req != null
         } // if asking for ssl attribute
       } // if key != null
       return super.getInfo(ctx,request,id,key);
     } // getInfo
}

class HttpRequest extends Request {
    Http10 http=new Http10();
    private boolean moreRequests = false;
    Socket socket;
    SSLSupport sslSupport=null;
    
    public HttpRequest() {
        super();

        // recycle these to remove the defaults
        remoteAddrMB.recycle();
        remoteHostMB.recycle();
    }

    public void recycle() {
	super.recycle();
	if( http!=null) http.recycle();
        // recycle these to remove the defaults
        remoteAddrMB.recycle();
        remoteHostMB.recycle();
	sslSupport=null;
    }

    public void setSocket(Socket socket) throws IOException {
	http.setSocket( socket );
	this.socket=socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public int doRead() throws IOException {
	if( available == 0 ) 
	    return -1;
	// #3745
	// if available == -1: unknown length, we'll read until end of stream.
	if( available!= -1 )
	    available--;
	return http.doRead();
    }

    public int doRead(byte[] b, int off, int len) throws IOException {
	if( available == 0 )
	    return -1;
	// if available == -1: unknown length, we'll read until end of stream.
	int rd=http.doRead( b, off, len );
	if( rd==-1) {
	    available=0;
	    return -1;
	}
	if( available!= -1 )
	    available -= rd;
	return rd;
    }
    

    public void readNextRequest(Response response) throws IOException {
	int status=http.processRequestLine( methodMB, uriMB,queryMB, protoMB );
	// XXX remove this after we swich to MB
	// 	method=methodMB.toString();
	// 	requestURI=uriMB.toString();
	// 	queryString=queryMB.toString();
	// 	protocol=protoMB.toString();
	
	if( status > 200 ) {
	    response.setStatus( status );
	    return;
	}

	// for 0.9, we don't have headers!
	if (! protoMB.equals("")) {
	    // all HTTP versions with protocol also have headers
	    // ( 0.9 has no HTTP/0.9 !)
	    status=http.readHeaders( headers  );
	    if( status >200 ) {
		response.setStatus( status );
		return;
	    }
	}

	// XXX detect for real whether or not we have more requests
	// coming
	moreRequests = false;
    }

    // -------------------- override special methods

    public MessageBytes remoteAddr() {
	// WARNING: On some linux configurations, this call may get you in
	// trubles... Big trubles...
	if( remoteAddrMB.isNull() ) {
	    remoteAddrMB.setString(socket.getInetAddress().getHostAddress());
	}
	return remoteAddrMB;
    }

    public MessageBytes remoteHost() {
	if( remoteHostMB.isNull() ) {
	    remoteHostMB.setString( socket.getInetAddress().getHostName() );
	}
	return remoteHostMB;
    }

    public String getLocalHost() {
	InetAddress localAddress = socket.getLocalAddress();
	localHost = localAddress.getHostName();
	return localHost;
    }

    public MessageBytes serverName(){
        if(! serverNameMB.isNull()) return serverNameMB;
        parseHostHeader();
        return serverNameMB;
    }

    public int getServerPort(){
        if(serverPort!=-1) return serverPort;
        parseHostHeader();
        return serverPort;
    }

    protected void parseHostHeader() {
	MessageBytes hH=getMimeHeaders().getValue("host");
        if (sslSupport != null){
            serverPort = 443;
        } else {
            serverPort = 80;
        }           
	if (hH != null) {
	    // XXX use MessageBytes
	    String hostHeader = hH.toString();
	    int i = hostHeader.indexOf(':');
	    if (i > -1) {
		serverNameMB.setString( hostHeader.substring(0,i));
                hostHeader = hostHeader.substring(i+1);
                try{
                    serverPort=Integer.parseInt(hostHeader);
                }catch(NumberFormatException  nfe){
                }
	    }else serverNameMB.setString( hostHeader);
            return;
	}
        serverPort = socket.getLocalPort();
	if( localHost != null ) {
	    serverNameMB.setString( localHost );
	}
	// default to localhost - and warn
	//	log("No server name, defaulting to localhost");
        serverNameMB.setString( getLocalHost() );
    }
 
    void setSSLSupport(SSLSupport s){
        sslSupport=s;
    }
 
}


class HttpResponse extends  Response {
    Http10 http;
    String reportedname;
    DateFormat dateFormat;
    
    public HttpResponse() {
        super();
    }

    public void init() {
	super.init();
	dateFormat=new SimpleDateFormat(DateTool.RFC1123_PATTERN,
					Locale.US);
	dateFormat.setTimeZone(DateTool.GMT_ZONE);
    }
    
    public void setSocket( Socket s ) {
	http=((HttpRequest)request).http;
    }

    public void recycle() {
	super.recycle();
    }

    public void setReported(String reported) {
        reportedname = reported;
    }

    public void endHeaders()  throws IOException {
	super.endHeaders();
	if(request.protocol().isNull() ||
	   request.protocol().equals("") ) // HTTP/0.9 
	    return;

	http.sendStatus( status, HttpMessages.getMessage( status ));

	// Check if a Date is to be added
	MessageBytes dateH=getMimeHeaders().getValue("Date");
	if( dateH == null ) {
	    // no date header set by user
	    MessageBytes dateHeader=getMimeHeaders().setValue(  "Date" );
	    dateHeader.setTime( System.currentTimeMillis(), dateFormat);
	}

	// return server name (or the reported one)
	if (reportedname == null) {
	    Context ctx = request.getContext();
	    String server = ctx != null ? ctx.getEngineHeader() : 
                ContextManager.TOMCAT_NAME + "/" + ContextManager.TOMCAT_VERSION;    
	    getMimeHeaders().setValue(  "Server" ).setString(server);
	} else {
	    if (reportedname.length() != 0)
		getMimeHeaders().setValue(  "Server" ).setString(reportedname);
	}
	
	http.sendHeaders( getMimeHeaders() );
    }

    public void doWrite( byte buffer[], int pos, int count)
	throws IOException
    {
	http.doWrite( buffer, pos, count);
    }
}
