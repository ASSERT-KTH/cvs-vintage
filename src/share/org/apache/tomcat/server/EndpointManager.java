/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/server/Attic/EndpointManager.java,v 1.2 1999/10/15 03:20:31 harishp Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/15 03:20:31 $
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
import org.apache.tomcat.net.*;
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
 * @author Harish Prabandham
 */

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
public class EndpointManager {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private static final int BACKLOG = 50;
    private static final int TIMEOUT = 1000;

    private static EndpointManager manager = new EndpointManager();
    
    public static EndpointManager getManager() {
	return manager;
    }

    private Vector endpoints = new Vector(); // {endpoints}
    private Hashtable endpointMaps = new Hashtable(); // endpoint -> {server}
    private Hashtable serverMaps = new Hashtable(); // server -> {endpoints}

    private ServerSocketFactory defaultFactory =
        ServerSocketFactory.getDefault();
    private int backlog = BACKLOG;
    private int timeout = TIMEOUT;
    
    private EndpointManager() {
	// XXX
	// protocol handler gets mapped in here.
    }

    
    /**
     * Gets a connection handler thread to handle a connection.
     */
    
    ConnectionHandler getHandler() {
	return new ConnectionHandler(this);
    }


    
    /**
     * Return a connection handler to the manager.
     */
    
    void returnHandler(ConnectionHandler handler) {
    	handler.recycle();
    	// XXX
	// eventually we'll want to recycle these.
    }
    
    
    /**
     * Allows the server developer to specify the backlog that
     * should be used for server sockets. By default, this value
     * is 50.
     */
    
    public void setBacklog(int backlog) {
	this.backlog = backlog;
    }

    /**
     * Sets the timeout in ms of the server sockets created by this
     * server. This method allows the developer to make servers
     * more or less responsive to having their server sockets
     * shut down.
     *
     * <p>By default this value is 1000ms.
     */

    public void setTimeout(int timeout) {
	this.timeout = timeout;
    }
    
    void startServer(HttpServer server) throws HttpServerException {
	Vector v = (Vector) serverMaps.get(server);

	for(Enumeration e = v.elements(); e.hasMoreElements(); ) {
	    Endpoint endpnt = (Endpoint) e.nextElement();
	    Thread thread = new Thread(endpnt);
	    thread.start();
	}
    }

    void stopServer(HttpServer server) throws HttpServerException {
	Vector v = (Vector) serverMaps.get(server);

	for(Enumeration e = v.elements(); e.hasMoreElements(); ) {
	    Endpoint endpnt = (Endpoint) e.nextElement();
	    System.out.println("Taking down endpoint: " + endpnt);
	    endpnt.shutdown();
	    endpointMaps.remove(endpnt);
	}

	// v.removeElement(server);
	// 
	// if (v.size() > 1) {
	// this endpoint needs to be decommissioned
	//}
    }

    HttpServer resolveServer(Endpoint endpoint, String hostname) {
	HttpServer server = null;
	Vector v = (Vector)endpointMaps.get(endpoint);

	if (v == null) {
	    // should be impossible,
	    System.out.println("NO SERVER FOR CONNECTION ON: " + endpoint);
	    System.exit(1);
	}

	if (v.size() == 1) {
	    server = (HttpServer)v.firstElement();
	} else {
	    // look it up the hard way -- and the server with a null
            // host name is the default one!

            // XXX
            // not yet working
            System.out.println("CAN'T DO VIRTUAL HOSTS YET");
	    System.exit(1);
	}

	return server;
    }
    
    public synchronized void addEndpoint(HttpServer server, int port,
					 InetAddress inet, ServerSocketFactory factory)
    throws HttpServerException {
	//	int port = server.getPort();
	//	InetAddress inet = server.getAddress();

	// Check if we have an existing endpoint...
	Enumeration enum = endpoints.elements();
	
	while (enum.hasMoreElements()) {
	    Endpoint endpnt = (Endpoint)enum.nextElement();
	    
	    if ((endpnt.getPort() == port) &&
		(endpnt.getAddress() == inet)) {
		return;
	    }
	}

	// so, if we make it here, we have to create a new endpoint
	Endpoint endpnt = new Endpoint(inet, port);
	// endpoints.addElement(endpnt);

	try {
	    ServerSocket ss = null;

	    if (inet != null) {
		ss = ((factory!= null)?factory:defaultFactory).createSocket(port, backlog);
	    } else {
		ss = ((factory!= null)?factory:defaultFactory).createSocket(port, backlog, inet);
	    }

	    //ss.setSoTimeout(timeout);
	    endpnt.setServerSocket(ss);

            String msg = sm.getString("endptmgr.created", endpnt);

	    System.out.println(msg);
	} catch (InstantiationException ie) {
	    System.out.println("FOO: " + ie);
	} catch (IOException ioe) {
	    String msg = sm.getString("endptmgr.getendpt.ioe", inet,
                Integer.toString(port), ioe);

	    throw new HttpServerException(msg);
	}

	endpoints.addElement(endpnt);

	// Stick it in the serverMaps table...
	if(serverMaps.get(server) == null) {
	    serverMaps.put(server, new Vector());
	}

	((Vector) serverMaps.get(server)).addElement(endpnt);

	// now stick it in the endpointMaps...
	if(endpointMaps.get(endpnt) == null) {
	    endpointMaps.put(endpnt, new Vector());
	}

	((Vector) endpointMaps.get(endpnt)).addElement(server);

	/*
	   Thread thread = new Thread(endpnt);
	   
	   thread.start();
	*/

	// return endpnt;
    }

    void notifyEndpointDown(Endpoint endpnt) {
	System.out.println("endpoint down: " + endpnt);
    }

}
