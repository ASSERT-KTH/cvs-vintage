/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/http/Attic/HttpConnectionHandler.java,v 1.13 2000/02/17 23:37:51 costin Exp $
 * $Revision: 1.13 $
 * $Date: 2000/02/17 23:37:51 $
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


package org.apache.tomcat.service.http;

import org.apache.tomcat.service.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class HttpConnectionHandler  implements  TcpConnectionHandler {
    
    ContextManager contextM;
    
    public HttpConnectionHandler() {
	super();
    }

    public void setAttribute(String name, Object value ) {
	if("context.manager".equals(name) ) {
	    contextM=(ContextManager)value;
	}
    }
    
    public void setContextManager( ContextManager contextM ) {
	this.contextM=contextM;
    }

    public Object[] init( ) {
	return null;
    }

    // XXX
    //    Nothing overriden, right now AJPRequest implment AJP and read everything.
    //    "Shortcuts" to be added here ( Vhost and context set by Apache, etc)
    // XXX handleEndpoint( Endpoint x )
    public void processConnection(TcpConnection connection, Object thData[]) {
	Socket socket=null;

	//	System.out.println("New Connection");
	try {
	    socket=connection.getSocket();
	    InputStream in=socket.getInputStream();
	    OutputStream out=socket.getOutputStream();

	    HttpRequestAdapter reqA=new HttpRequestAdapter();
	    reqA.setContextManager( contextM );

	    HttpResponseAdapter resA=new HttpResponseAdapter();
	    
	    resA.setRequest(reqA);
	    reqA.setResponse( resA );
	    
	    reqA.setSocket( socket );
	    resA.setOutputStream( out );

	    reqA.readNextRequest(resA);

	    // XXX temporary fix for getServerName
	    String hostHeader = reqA.getHeader("host");
	    //  if it's not null, Request.getServerName() will take care
	    if (hostHeader == null) {
		// XXX
		// we need a better solution here
		InetAddress localAddress = socket.getLocalAddress();
		reqA.setServerName(localAddress.getHostName());
	    } else {
		// strip out the port information
		int i = hostHeader.indexOf(":");
		
		if (i > -1)
		    hostHeader = hostHeader.substring(0, i);
		
		reqA.setServerName(hostHeader);
             }
    
	    contextM.service( reqA, resA );
	    try {
               InputStream is = socket.getInputStream();
               int available = is.available ();
	       
               // XXX on JDK 1.3 just socket.shutdownInput () which
               // was added just to deal with such issues.

               // skip any unread (bogus) bytes
               if (available > 1) {
                   is.skip (available);
               }
	   }catch(NullPointerException npe) {
	       // do nothing - we are just cleaning up, this is
	       // a workaround for Netscape \n\r in POST - it is supposed
	       // to be ignored
	   } catch(java.net.SocketException ex) {
	       // do nothing - same
	   }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    // recycle kernel sockets ASAP
	    try { socket.close (); }
	    catch (IOException e) { /* ignore */ }
        }
    }


}
