/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/modules/server/Ajp13Interceptor.java,v 1.20 2002/02/08 20:23:48 larryi Exp $
 * $Revision: 1.20 $
 * $Date: 2002/02/08 20:23:48 $
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

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.io.FileUtil;

/* Frozen, bug fixes only: all active development goes in
     jakarta-tomcat-connectors/jk/org/apache/ajp/Ajp14*
*/


public class Ajp13Interceptor extends PoolTcpConnector
    implements  TcpConnectionHandler
{
    private boolean tomcatAuthentication=true;
    private boolean shutDownEnable=false;
    // true if the incloming uri is encoded.
    private boolean decoded=true;

    private int decodedNote;
    private String secret=null;
    private File ajpidFile=null;
    private boolean authenticateConnection=false;
    
    public Ajp13Interceptor()
    {
        super();
 	super.setSoLinger( 100 );
	super.setTcpNoDelay( true );
    }

    // -------------------- PoolTcpConnector --------------------

    /** Enable shutdown command. By default it is disabled, since
     *	ajp12 has an improved version with password checking.
     *
     *	In future we'll enable shutdown in ajp13/14 and deprecate ajp12,
     *	and merge various improvements from ajp12.
     *
     *  Note that this you can use ajp13 for communication with the server
     *	and ajp12 only for shutdown - that would allow some extra flexibility,
     *	especially if you use firewall rules.
    */
    public void setShutDownEnable(boolean b ) {
	shutDownEnable=b;
    }

    /** Enable the use of a secret. The secret will be
     *  randomly generated. mod_jk must read the secret to
     *  communicate with tomcat. 
     *
     *  Note that we don't use the secret only for shutdown, but
     *  for normal request processing. A 'bad' request may forge
     *  auth, etc.
     */
    public void setUseSecret(boolean b ) {
	secret=Double.toString(Math.random());
        shutDownEnable=true;
    }

    /** Set the 'secret'. If this is set, all sensitive operations
     *   will be disabled unless the request includes a password.
     *
     *  This requires a recent version of mod_jk and the
     *    worker.NAME.secret property in workers.properties.
     */
    public void setSecret( String s ) {
        secret=s;
        shutDownEnable=true;
    }

    /** Specify ajpid file used when shutting down tomcat
     */
    public void setAjpidFile( String path ) {
        ajpidFile=( path==null?null:new File(path));
    }

    /** Specify if Ajp13 requests must be authenticated
     */
    public void setAuthenticateConnection( boolean b ) {
        authenticateConnection=b;
    }

    public void setDecodedUri( boolean b ) {
	decoded=b;
    }
    
    protected void localInit() throws Exception {
	ep.setConnectionHandler( this );
    }

    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	super.engineInit( cm );
	decodedNote=cm.getNoteId(ContextManager.REQUEST_NOTE,
				  "req.decoded" );
    }

    public void engineState(ContextManager cm, int state )
	throws TomcatException
    {

        if( state==ContextManager.STATE_START ) {
            // the engine is now started, create the ajp13.id
            // file that will allow us to stop the server and
            // know that the server is started ok.
            Ajp13Interceptor tcpCon=this;
            int portInt=tcpCon.getPort();
            InetAddress address=tcpCon.getAddress();
            File sf=FileUtil.getConfigFile(ajpidFile, new File(cm.getHome()),
                                           "conf/ajp13.id");
            Properties props=new Properties();
            
            if( ajpidFile != null || debug > 0)
                log( "Using stop file: "+sf);
            try {
                //  PrintWriter stopF=new PrintWriter
                //                     (new FileWriter(sf));
                FileOutputStream stopF=new FileOutputStream( sf );
                props.put( "port", Integer.toString( portInt ));
                // stopF.println( portInt );
                if( address==null ) {
                    // stopF.println( "" );
                } else {
                    //stopF.println( address.getHostAddress() );
                    props.put( "address", address.getHostAddress() );
                }
                if( secret !=null ) {
                    //stopF.println( secret );
                    props.put( "secret", secret );
                } else {
                    // stopF.println();
                }
                if( shutDownEnable )
                    props.put( "shutdown", "enabled" );
                //            stopF.close();
                props.save( stopF, "Automatically generated, don't edit" );
            } catch( IOException ex ) {
                log( "Can't create stop file: "+sf, ex );
            }
        }

    }

    
    // -------------------- Handler implementation --------------------
    
    public Object[] init()
    {
        Object thData[]=new Object[3];
        Ajp13Request req=new Ajp13Request();
        Ajp13Response res=new Ajp13Response();
        Ajp13 con=new Ajp13();
        con.setDebug(debug);
        req.setDebug(debug);
        con.setTomcatAuthentication(isTomcatAuthentication());
        cm.initRequest(req, res);
        thData[0]=req;
        thData[1]=res;
        thData[2]=con;

        return  thData;
    }

    // XXX
    //    Nothing overriden, right now AJPRequest implment AJP and read
    // everything.
    //    "Shortcuts" to be added here ( Vhost and context set by Apache, etc)
    // XXX handleEndpoint( Endpoint x )
    public void processConnection(TcpConnection connection, Object thData[])
    {
        try {
            if(connection == null) {
                return;
            }
            Socket socket = connection.getSocket();
            if(socket == null) {
                return;
            }

            Ajp13 con=null;
            Ajp13Request req=null;
            Ajp13Response res=null;

            if(thData != null) {
                req = (Ajp13Request)thData[0];
                res = (Ajp13Response)thData[1];
                con = (Ajp13)thData[2];
                if(req != null) req.recycle();
                if(res != null) res.recycle();
                if(con != null) con.recycle();
            }

            if(req == null || res == null || con == null) {
                req = new Ajp13Request();
                res = new Ajp13Response();
                con = new Ajp13();
                con.setTomcatAuthentication(isTomcatAuthentication());
                cm.initRequest( req, res );
            }
	    // XXX
	    req.ajp13=con;
	    res.ajp13=con;

            con.setSocket(socket);

            boolean moreRequests = true;
            boolean authenticated = false;
            // If we are not configured with a secret or we are
            // not authenticating the connection, assume
            // we trust the remote party ( as we did before )
            if( secret == null || !authenticateConnection )
                authenticated=true;
            
            while(moreRequests) {
		int status=req.receiveNextRequest();

                if( !authenticated ) {
                    // we need to authenticate - the user set a
                    // secret and expects the web server to send it
                    String conSecret=con.getSecret();
                    if(  conSecret == null ) {
                        log("Unauthenticated server");
                        break;
                    }
                    if( ! secret.equals( conSecret )) {
                        log("Bad server secret");
                        break;
                    }
                    // allow further requests without checking
                    authenticated=true;
                }
                
		if( status==-2) {
                    // check secret if set
                    if( secret != null && ! secret.equals(con.getSecret())) {
                        log("Shutdown command ignored. Secret didn't match.");
                        continue;
                    }
		    // special case - shutdown
		    // XXX need better communication, refactor it
		    if( !doShutdown(con,
                                    socket.getLocalAddress(),
				    socket.getInetAddress())) {
			moreRequests = false;
			continue;
		    }
		}

		// special case - invalid AJP13 packet, error 
		// decoding packet ...
		// we drop the connection rigth now
		if( status != 200 )
		    break;

		if( decoded )
		    req.setNote( decodedNote, this );
		
		cm.service(req, res);

		req.recycle();
		res.recycle();
            }
            log("Closing connection", Log.DEBUG);
            con.close();
	    socket.close();
        } catch (Exception e) {
	    log("Processing connection " + connection, e);
        }
    }

    public void setServer(Object contextM)
    {
        this.cm=(ContextManager)contextM;
    }

    protected boolean doShutdown(Ajp13 con,
                                 InetAddress serverAddr,
                                 InetAddress clientAddr)
    {
        try {
            // continue with the other checks. XXX We may allow shutdown
            // with the right secret from a different address.
	    // close the socket connection before handling any signal
	    // but get the addresses first so they are not corrupted
            if(shutDownEnable && Ajp12.isSameAddress(serverAddr, clientAddr)) {
		cm.shutdown();
                log( "Exiting" );
		// same behavior as in past, because it seems that
		// stopping everything doesn't work - need to figure
		// out what happens with the threads ( XXX )
		System.exit(0);
	    }
	} catch(Exception ignored) {
	    log("Ignored " + ignored);
	}
	log("Shutdown command ignored");
	return false;
    }

    public boolean isTomcatAuthentication() {
        return tomcatAuthentication;
    }

    public void setTomcatAuthentication(boolean newTomcatAuthentication) {
        tomcatAuthentication = newTomcatAuthentication;
    }

}

class Ajp13Request extends Request 
{
    Ajp13 ajp13 = new Ajp13();
    int   dL    = 0;
 
    public Ajp13Request() 
    {
        super();
    }
    
    public void setDebug(int level) {
        dL = level;
    }

    protected int receiveNextRequest() throws IOException 
    {
	return ajp13.receiveNextRequest( this );
    }
    
    public int doRead() throws IOException 
    {
	if( contentLength == -1 ) {
	    return ajp13.doRead();
	}
	if( available <= 0 )
	    return -1;
	available--;
	return ajp13.doRead();
    }
    
    public int doRead(byte[] b, int off, int len) throws IOException 
    {
	int rd=-1;
	if( contentLength == -1 ) {
	    rd=ajp13.doRead(b,off,len);
	    return rd;
	}
	if( available <= 0 )
	    return -1;
	rd=ajp13.doRead( b,off, len );
	available -= rd;
	if( dL > 0 ) d("Read: " + new String( b,off, len ));
	return rd;
    }
    
    public void recycle() 
    {
        super.recycle();
	if( ajp13!=null) ajp13.recycle();
    }

    private void d(String s ) {
	System.err.println( "Ajp13Request: " + s );
    }
}

class Ajp13Response extends Response 
{
    Ajp13 ajp13;
    boolean finished=false;
    
    public Ajp13Response() 
    {
	super();
    }

    public void recycle() {
	super.recycle();
	finished=false;
    }

    public void setSocket( Socket s ) {
	ajp13=((Ajp13Request)request).ajp13;
    }

    // XXX if more headers that MAX_SIZE, send 2 packets!   
    public void endHeaders() throws IOException 
    {
        super.endHeaders();
    
        if (request.protocol().isNull()) {
            return;
        }

	ajp13.sendHeaders(getStatus(), getMimeHeaders());
    } 
         
    public void finish() throws IOException 
    {
	if(!finished) {
	    super.finish();
		finished = true; // Avoid END_OF_RESPONSE sent 2 times
	    ajp13.finish();
	}
    }
    
    public void doWrite(  byte b[], int off, int len) throws IOException 
    {
	ajp13.doWrite(b, off, len );
    }
    
}
