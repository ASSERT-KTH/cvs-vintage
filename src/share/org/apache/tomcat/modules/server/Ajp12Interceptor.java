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

/*
  Based on Ajp11ConnectionHandler and Ajp12 implementation of JServ
*/
package org.apache.tomcat.modules.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.net.TcpConnection;
import org.apache.tomcat.util.net.TcpConnectionHandler;

/* 
 */
public class Ajp12Interceptor extends PoolTcpConnector
    implements  TcpConnectionHandler{
    private boolean tomcatAuthentication=true;
    String secret;
    File ajpidFile=null;
    
    public Ajp12Interceptor() {
	super();
    }
    // -------------------- PoolTcpConnector --------------------

    protected void localInit() throws Exception {
	ep.setConnectionHandler( this );
    }

    /** Enable the use of a stop secret. The secret will be
     *  randomly generated.
     */
    public void setUseSecret(boolean b ) {
	secret=Double.toString(Math.random());
    }

    /** Explicitely set the stop secret
     */
    public void setSecret( String s ) {
	secret=s;
    }

    /** Specify ajpid file used when shutting down tomcat
     */
    public void setAjpidFile( String path ) {
        ajpidFile=( path==null?null:new File(path));
    }
    
    public void engineInit( ContextManager cm )
	throws TomcatException
    {
        super.engineInit( cm );
        String ajpid12 = cm.getProperty("ajpid12");
        if( ajpid12 != null ) {
            if( ajpidFile != null ) {
                log( "Overriding ajpidFile with " + ajpid12 );
            }
            ajpidFile = new File(ajpid12);
        }
    }

    public void engineState(ContextManager cm, int state )
	throws TomcatException
    {
	if( state!=ContextManager.STATE_START )
	    return;
	// the engine is now started, create the ajp12.id
	// file that will allow us to stop the server and
	// know that the server is started ok.
	Ajp12Interceptor tcpCon=this;
	int portInt=tcpCon.getPort();
	InetAddress address=tcpCon.getAddress();
        File sf=FileUtil.getConfigFile(ajpidFile, new File(cm.getHome()),
                        "conf/ajp12.id");
        if( ajpidFile != null || debug > 0)
            log( "Using stop file: "+sf);
	try {
	    PrintWriter stopF=new PrintWriter
		(new FileWriter(sf));
	    stopF.println( portInt );
	    if( address==null )
		stopF.println( "" );
	    else
		stopF.println( address.getHostAddress() );
	    if( secret !=null )
		stopF.println( secret );
	    else
		stopF.println();
	    stopF.close();
	} catch( IOException ex ) {
	    log( "Can't create stop file: "+sf, ex );
	}
    }

    // -------------------- Handler implementation --------------------

    public Object[] init() {
	Object thData[]=new Object[2];
	AJP12Request reqA=new AJP12Request();
	reqA.setSecret( secret );
	reqA.setTomcatAuthentication(isTomcatAuthentication());
	AJP12Response resA=new AJP12Response();
	cm.initRequest( reqA, resA );
	thData[0]=reqA;
	thData[1]=resA;

	return  thData;
    }

    public void setServer( Object cm ) {
	this.cm=(ContextManager )cm;
    }

    public void processConnection(TcpConnection connection, Object[] thData) {
        try {
	    // XXX - Add workarounds for the fact that the underlying
	    // serverSocket.accept() call can now time out.  This whole
	    // architecture needs some serious review.
	    if (connection == null)
		return;
	    Socket socket=connection.getSocket();
	    if (socket == null)
		return;

	    socket.setSoLinger( true, 100);
	    //	    socket.setSoTimeout( 1000); // or what ?

	    AJP12Request reqA=null;
	    AJP12Response resA=null;

	    if( thData != null ) {
		reqA=(AJP12Request)thData[0];
		resA=(AJP12Response)thData[1];
		if( reqA!=null ) reqA.recycle();
		if( resA!=null ) resA.recycle();
//XXX is needed to revert the tomcat auth state? if yes put it into recycle and
//    and uncomment here                
//                ((AJP12Request)reqA).setTomcatAuthentication(isTomcatAuthtentication());
	    }

	    if( reqA==null || resA==null ) {
		reqA = new AJP12Request();
		reqA.setSecret( secret );
                ((AJP12Request)reqA).setTomcatAuthentication(
                                        isTomcatAuthentication());
		resA=new AJP12Response();
		cm.initRequest( reqA, resA );
	    }

	    reqA.setSocket( socket );
	    resA.setSocket( socket );

	    reqA.readNextRequest();

	    if( reqA.internalAjp() )
		return;

	    cm.service( reqA, resA );
	    socket.close();
	} catch (Exception e) {
	    log("HANDLER THREAD PROBLEM", e);
	}
    }

    public boolean isTomcatAuthentication() {
        return tomcatAuthentication;
    }

    public void setTomcatAuthentication(boolean newTomcatAuthentication) {
        tomcatAuthentication = newTomcatAuthentication;
    }
}

class AJP12Request extends Request {
    Ajp12 ajp12=new Ajp12();

    public AJP12Request() {
    }

    void setSecret( String s ) {
	ajp12.setSecret( s );
    }
    
    public boolean internalAjp() {
	return ajp12.isPing ||
	    ajp12.shutdown;
    }

    public void readNextRequest() throws IOException {
	ajp12.readNextRequest( this );
    }

    public void setSocket( Socket s ) throws IOException {
	ajp12.setSocket( s );
    }

    public int doRead() throws IOException {
	if( available <= 0 )
	    return -1;
	available--;
	return ajp12.doRead();
    }

    public  int doRead( byte b[], int off, int len ) throws IOException {
	if( available <= 0 )
	    return -1;
	int rd=ajp12.doRead( b,off,len);
	available -= rd;
	return rd;
    }

    public boolean isTomcatAuthentication() {
        return ajp12.isTomcatAuthentication();
    }

    public void setTomcatAuthentication(boolean newTomcatAuthentication) {
        ajp12.setTomcatAuthentication(newTomcatAuthentication);
    }
}


// Ajp use Status: instead of Status
class AJP12Response extends Response {
    Http10 http=new Http10();

    public void recycle() {
        super.recycle();
        http.recycle();
    }

    public void setSocket( Socket s ) throws IOException {
	http.setSocket( s );
    }

    public void endHeaders()  throws IOException {
	super.endHeaders();
	sendStatus( status, HttpMessages.getMessage( status ));
	http.sendHeaders( getMimeHeaders() );
    }

    public void doWrite( byte buffer[], int pos, int count)
	throws IOException
    {
	http.doWrite( buffer, pos, count);
    }

    /** Override setStatus
     */
    protected void sendStatus( int status, String message)  throws IOException {
	http.printHead("Status: " );
	http.printHead( String.valueOf( status ));
	http.printHead( " " );
	http.printHead( message );
	http.printHead("\r\n");

	// Servlet Engine header will be set per/adapter - smarter adapters will
	// not send it every time ( have it in C side ), and we may also want
	// to add informations about the adapter used 
	if( request.getContext() != null)
	    setHeader("Servlet-Engine", request.getContext().getEngineHeader());

    }
}
