/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/http/Attic/HttpAdapter.java,v 1.5 2000/02/09 23:26:29 costin Exp $
 * $Revision: 1.5 $
 * $Date: 2000/02/09 23:26:29 $
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

import org.apache.tomcat.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.service.*;
import org.apache.tomcat.net.*;
import java.io.*;
import java.net.*;
import java.util.*;

//import org.apache.tomcat.server.HttpServer;

/**
 * @author costin@eng.sun.com
 */
public class HttpAdapter  implements ServerConnector {
    // Attributes we accept ( to support the old model of
    // configuration, will be deprecated )
    public static final String VHOST_PORT="vhost_port";
    public static final String VHOST_NAME="vhost_name";

    // needed only as a "hack" to HttpServerConnector
    public static final String SERVER="server";
    public static final String VHOST_ADDRESS="vhost_address";
    public static final String SOCKET_FACTORY="socketFactory";

    String handlerClassName;
    TcpEndpoint ep;
    HttpConnectionHandler con;

    ContextManager cm;
    
    private InetAddress address;
    // default is 8080
    int vport=8080;

    private ServerSocketFactory socketFactory;
    private ServerSocket serverSocket;

    boolean running = true;
    
    public HttpAdapter() {
	ep=new TcpEndpoint();
	con=new HttpConnectionHandler();
	ep.setConnectionHandler( con );
    }

    public void start() throws Exception {
	if( con==null) throw new Exception( "Invalid ConnectionHandler");
	ep.setPort(vport);
	if( socketFactory != null) {
	    ep.setServerSocketFactory( socketFactory );
	}
	ep.startEndpoint();
    }

    public void stop() throws Exception {
	ep.stopEndpoint();
    }

    public void setContextManager( ContextManager ctx ) {
	this.cm=ctx;
	con.setContextManager( ctx );
    }

    public void setPort( String s ) {
	vport=string2Int( s );
    }

    public void setProperty( String prop, String value) {
	if(VHOST_PORT.equals(prop) ) {
	    //	    System.out.println("XXX");
	    vport=string2Int(value);
	}
    }

    // XXX use constants, remove dep on HttpServer
    public void setAttribute( String prop, Object value) {
	if(VHOST_NAME.equals(prop) ) {
	    //vhost=(String)value;
	}
	if(VHOST_PORT.equals(prop) ) {
	    vport=((Integer)value).intValue();
	}

	if(VHOST_ADDRESS.equals(prop)) {
	    address=(InetAddress)value;
	}
	if(SERVER.equals(prop)) {
	    //server=(HttpServer)value;
	}
	if(SOCKET_FACTORY.equals(prop)) {
	    socketFactory=(ServerSocketFactory)value;
	}
    }

    public Object getAttribute( String prop ) {
	return null;
    }

    private int string2Int( String val) {
	try {
	    return Integer.parseInt(val);
	} catch (NumberFormatException nfe) {
	    return 0;
	}
    }


    
}
    

