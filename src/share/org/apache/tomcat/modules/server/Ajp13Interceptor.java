/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/modules/server/Ajp13Interceptor.java,v 1.8 2001/03/02 04:49:23 costin Exp $
 * $Revision: 1.8 $
 * $Date: 2001/03/02 04:49:23 $
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

public class Ajp13Interceptor extends PoolTcpConnector
    implements  TcpConnectionHandler
{
    public Ajp13Interceptor()
    {
        super();
    }

    // -------------------- PoolTcpConnector --------------------

    protected void localInit() throws Exception {
	ep.setConnectionHandler( this );
    }

    // -------------------- Handler implementation --------------------
    
    public Object[] init()
    {
        Object thData[]=new Object[3];
        Ajp13Request req=new Ajp13Request();
        Ajp13Response res=new Ajp13Response();
        cm.initRequest(req, res);
        thData[0]=req;
        thData[1]=res;
        thData[2]=new Ajp13();

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

            socket.setSoLinger( true, 100);

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
                cm.initRequest( req, res );
            }
	    // XXX
	    req.ajp13=con;
	    res.ajp13=con;
	    
            con.setSocket(socket);

            boolean moreRequests = true;
            while(moreRequests) {
		int status=req.receiveNextRequest();

		if( status==-2) {
		    // special case - shutdown
		    // XXX need better communication, refactor it
		    if( !doShutdown(socket.getLocalAddress(),
				    socket.getInetAddress())) {
			moreRequests = false;
			continue;
		    }                        
		}
		if( status != 200 )
		    break;
		
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
    
    protected boolean doShutdown(InetAddress serverAddr,
                                 InetAddress clientAddr)
    {
        try {
	    // close the socket connection before handling any signal
	    // but get the addresses first so they are not corrupted			
            if(Ajp12.isSameAddress(serverAddr, clientAddr)) {
		cm.stop();
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
    
}

class Ajp13Request extends Request 
{
    Ajp13 ajp13=new Ajp13();
    
    public Ajp13Request() 
    {
        super();
    }
    
    protected int receiveNextRequest() throws IOException 
    {
	return ajp13.receiveNextRequest( this );
    }
    
    public int doRead() throws IOException 
    {
	if( available <= 0 )
	    return -1;
	available--;
	return ajp13.doRead();
    }
    
    public int doRead(byte[] b, int off, int len) throws IOException 
    {
	if( available <= 0 )
	    return -1;
	int rd=ajp13.doRead( b,off, len );
	available -= rd;
	return rd;
    }
    
    public void recycle() 
    {
        super.recycle();
	if( ajp13!=null) ajp13.recycle();
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
