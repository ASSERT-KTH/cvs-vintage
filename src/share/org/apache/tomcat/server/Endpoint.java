/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/server/Attic/Endpoint.java,v 1.1 1999/10/09 00:20:48 duncan Exp $
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


package org.apache.tomcat.server;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

class Endpoint implements Runnable {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private EndpointManager manager = EndpointManager.getManager();
    private boolean running = true;
    private InetAddress inet;
    private int port;
    private ServerSocket serverSocket;
    
    Endpoint(InetAddress inet, int port) {
	this.inet = inet;
	this.port = port;
    }

    int getPort() {
	return port;
    }

    InetAddress getAddress() {
	return inet;
    }

    void setServerSocket(ServerSocket ss) {
	serverSocket = ss;
    }

    void shutdown() {
	running = false;
    }

    public void run() {
	while (running) {
	    try {
		Socket socket = serverSocket.accept();
		if (running == false) {
		    socket.close();  // rude, but unlikely!
		    break;
		}
		ConnectionHandler handler = manager.getHandler();
		handler.setEndpoint(this);
		handler.setSocket(socket);
		handler.start();
	    } catch (InterruptedIOException iioe) {
		// normal part -- should happen regularly so
		// that the endpoint can release if the server
		// is shutdown.
		// you know, i really wish that there was a
		// way for the socket to timeout without
		// tripping an exception. Exceptions are so
		// 'spensive.
	    } catch (Exception e) {
		running = false;
		String msg = sm.getString("endpoint.err.fatal",
                    serverSocket, e);
		System.err.println(msg);
	    }
	}
	manager.notifyEndpointDown(this);
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	if (inet != null) {
	    buf.append(inet);
	}
	buf.append(":" + port);
	return buf.toString();
    }
}
