/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/server/Attic/ConnectionHandler.java,v 1.7 2000/01/07 19:14:15 costin Exp $
 * $Revision: 1.7 $
 * $Date: 2000/01/07 19:14:15 $
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
    protected HttpResponseAdapter resA= new HttpResponseAdapter();
    protected Response response = new Response();
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
	    response.setResponseAdapter( resA );
            reqA.setSocket(socket);
            resA.setOutputStream( socket.getOutputStream() );

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
		    BufferedServletInputStream sis =
			(BufferedServletInputStream)request.getInputStream();
		    sis.setLimit(contentLength);
		}

		HttpServer server = manager.resolveServer(endpoint,hostHeader);
		response.setServerHeader(server.getServerHeader());

		ContextManager cm=server.getContextManager();
		cm.service( request, response );

		request.recycle();
		response.recycle();
		reqA.recycle();
		resA.recycle();
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

	    // NOTE interactions with connection keepalive

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
	} catch (SocketException e) {
	    // XXX is this the right exception for abort by client?
	    System.out.println("Connection aborted by client");
	} catch (Exception e) {
            // XXX
	    // this isn't what we want, we want to log the problem somehow
	    //
	    // ... unless it's out of our hands and routine, like a broken
	    // pipe IOException.  That is routine, caused by a client going
	    // away before the response is completely written ... too bad
	    // there's nothing like an error code we can switch on !!
	    System.out.println("HANDLER THREAD PROBLEM: " + e);
 	    e.printStackTrace();
	} finally {
	    // recycle kernel sockets ASAP
	    try { socket.close (); }
	    catch (IOException e) { /* ignore */ }
        }

	// recycle ourselves
	manager.returnHandler(this);
    }
}
