/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/server/Attic/ConnectionHandler.java,v 1.2 1999/10/28 05:15:28 costin Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/28 05:15:28 $
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


package org.apache.tomcat.server;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 */

class ConnectionHandler extends Thread {
    
    protected StringManager sm =
        StringManager.getManager(Constants.Package);
    protected EndpointManager manager;
    protected Request request = new Request();
    protected HttpRequestAdapter reqA= new HttpRequestAdapter();
    protected ServerResponse response = new ServerResponse();
    protected Endpoint endpoint;
    protected Socket socket;
    
    ConnectionHandler() {
    }
    
    ConnectionHandler(EndpointManager manager) {
	this.manager = manager;
    }
    
    void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
    
    
    void setEndpointManager( EndpointManager manager ) {
	this.manager = manager;
    }
    
    void setSocket(Socket socket) {
        this.socket = socket;
    }
        
    void recycle() {
        request.recycle();
	reqA.recycle();
        response.recycle();
        endpoint = null;
        socket = null;
    }
    
    public void run() {
        // first make sure that we've got a socket and endpoint to work with
        if (endpoint == null || socket == null) {
            String msg = sm.getString("conhandler.run.ise");

            throw new IllegalStateException(msg);
        }
        
        try {
	    int count = 1;
	    // XXX should be in init or main ?
	    request.setRequestAdapter( reqA );
	    
            reqA.setSocket(socket);
            response.setSocket(socket);

            while(reqA.hasMoreRequests()) {

	        // XXX
                //    bind response to request, and vice versa

	        request.setResponse(response);
	        response.setRequest(request);
		reqA.readNextRequest(response);

		// XXX
                //    don't do headers if request protocol is http/0.9

		if (request.getProtocol() == null) {
		    response.setOmitHeaders(true);
		}

                // XXX
                //    return if an error was detected in processing the
                //    request line

		if (response.getStatus() >= 400) {
		    response.finish();

		    request.recycle();
		    response.recycle();

		    break;
		}

		// resolve the server that we are for

		String hostHeader = request.getHeader("host");

		if (hostHeader != null) {
		    // shave off the port part of the host header if
		    // it exists

		    int i = hostHeader.indexOf(':');

		    if (i > -1) {
			hostHeader = hostHeader.substring(0,i);
		    }

		    request.setServerName(hostHeader);
		} else {
		    // XXX
		    // we need a better solution here
		    InetAddress localAddress = socket.getLocalAddress();
		    request.setServerName(localAddress.getHostName());
		}

		int contentLength = request.getIntHeader("content-length");

		if (contentLength != -1) {
		    ServletInputStreamImpl sis =
			(ServletInputStreamImpl)request.getInputStream();
		    sis.setLimit(contentLength+2);
		}

		HttpServer server = manager.resolveServer(endpoint,hostHeader);
		String path = request.getRequestURI();
		int index = path.indexOf("?");

		response.setServerHeader(server.getServerHeader());

		if (index > 0 ) {
		    path = path.substring(0, index);
		}

		Context ctx = server.getContextByPath(path);
		String ctxPath = ctx.getPath();
		String pathInfo =
		    path.substring(ctxPath.length(), path.length());
		
		ctx.handleRequest(request, response);
		response.finish();

		request.recycle();
		response.recycle();
            }

	    // Some browsers send an extra CRLF after the POST data.
	    // This is wrong - but NN does it, so we have to deal with it.
	    
	    // If we don't read the final CRLF, some TCP/IP implementations
	    // will send a SIGPIPE to the client ( Linux for example ), to signal
	    // that not all data was consumed ( RST packet = connection abort). 
	    
	    // Apache deals with the problem by using a sort of "soft" SO_LINGER.

	    // There is no way to know if after the POST data the client will send CRLF
	    // or will just wait.
	    // I have no better ideea about how to fix it -  if the client do not close the
	    // connection after sending a correct POST, we'll hang.. 
	    // it is a bug in the browser that the final CRLF is sent anyway

	try{
	    InputStream is=socket.getInputStream();
	    if(is.available() >1 ) {
		// read the bogus 2 bytes
		is.read();
		is.read();
	    }
	    socket.close();
	   }catch(NullPointerException npe) {
	   // Added to make the Thread quite on Solaris.. - Costin talk to me 
	   // before you change this code - Harish/AKV
	   }
	} catch (Exception e) {
            // XXX
	    // this isn't what we want, we want to log the problem somehow
	    System.out.println("HANDLER THREAD PROBLEM: " + e);
	    e.printStackTrace();
	}

	// recycle ourselves
	manager.returnHandler(this);
    }
}
