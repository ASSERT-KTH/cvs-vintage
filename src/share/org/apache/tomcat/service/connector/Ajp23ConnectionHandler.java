/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/connector/Attic/Ajp23ConnectionHandler.java,v 1.6 2000/01/15 23:30:24 costin Exp $
 * $Revision: 1.6 $
 * $Date: 2000/01/15 23:30:24 $
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


package org.apache.tomcat.service.connector;

import org.apache.tomcat.service.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
//import org.apache.tomcat.server.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class Ajp23ConnectionHandler implements  TcpConnectionHandler {
    ContextManager contextM;

    public Ajp23ConnectionHandler() {
	super();
    }

    public void setContextManager( ContextManager contextM ) {
	this.contextM=contextM;
    }
    public void setAttribute(String name, Object value ) {
	if("context.manager".equals(name) ) {
	    contextM=(ContextManager)value;
	}
    }
    
    public Object[] init( ) {
	return null;
    }

    // XXX
    //    Nothing overriden, right now AJPRequest implment AJP and read everything.
    //    "Shortcuts" to be added here ( Vhost and context set by Apache, etc)
    // XXX handleEndpoint( Endpoint x )
    public void processConnection(TcpConnection connection, Object thData[]) {
	Socket socket;

	try {
	    socket=connection.getSocket();
	    TcpConnector con=new TcpConnector( socket );
	    ConnectorResponse rresponse=new ConnectorResponse(con);
	    //	    RequestImpl  rrequest=new RequestImpl();
	    ConnectorRequest  reqA=new ConnectorRequest(con);
	    //rrequest.setRequestAdapter( reqA ); 

	    boolean moreRequests=true;
            while( moreRequests ) { // XXX how to exit ? // request.hasMoreRequests()) {
		MsgBuffer msg=con.getMsgBuffer();
		int err=con.receive( msg );
		if( err<0 ) {
		    //System.out.println("ERR rec " + err );
		    moreRequests=false;
		    break;
		}
		// XXX right now the only incoming packet is "new request"
		// We need to deal with arbitrary calls
		int type=msg.getInt();
		//		msg.dump("Received: ");
		
		err=reqA.decodeRequest(msg);

		contextM.service( reqA, rresponse);

		reqA.recycle();
		rresponse.recycle();

		// XXX
		//		rresponse=new ConnectorResponse(con);
            }

	    //System.out.println("Closing connection");
	    socket.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


}

class TcpConnector implements MsgConnector {
    public static final int MAX_PACKET_SIZE=8192;
    public static final int H_SIZE=4;
    OutputStream out;
    InputStream in;
    
    MsgBuffer msg;
    
    public TcpConnector( Socket socket ) throws IOException {
	socket.setSoLinger( true, 100);
	
	out = socket.getOutputStream();
	in = socket.getInputStream();
	msg= new MsgBuffer( MAX_PACKET_SIZE );
    }

    public MsgBuffer getMsgBuffer() {
	msg.reset();
	return msg;
    }
    
    public int receive(MsgBuffer msg) throws IOException {
	// Read Packet

	byte b[]=msg.getBuff();
	
	int rd=in.read( b, 0, H_SIZE );
	if( rd<=0 ) {
	    //	    System.out.println("Rd header returned: " + rd );
	    return rd;
	}

	int len=msg.checkIn();
	
	// XXX check if enough space - it's assert()-ed !!!
	// Can we have only one read ( with unblocking, it can read all at once - but maybe more ) ?
	//???	len-=4; // header

	rd=in.read( b, 4, len );
	if( rd != len ) {
	    System.out.println( "Incomplete read, deal with it " + len + " " + rd);
	}
	// 	msg.dump( "Incoming");
	return rd;
	//    System.out.println( "Incoming Packet len=" + len);
    }

    public void send( MsgBuffer msg ) throws IOException {
	msg.end();
	byte b[]=msg.getBuff();
	int len=msg.getLen();
	//	msg.dump("SEND");
	out.write( b, 0, len );
    }
}
