/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/server/Attic/HttpServer.java,v 1.2 2000/02/22 21:06:35 costin Exp $
 * $Revision: 1.2 $
 * $Date: 2000/02/22 21:06:35 $
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
import org.apache.tomcat.service.http.*;
import org.apache.tomcat.service.*;
import org.apache.tomcat.net.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;


/* Old comment:
 * A server that implements the HTTP protocol and supports the servlet
 * processing engine of the org.apache.tomcat.core package.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham 
 */


/** 
 *  You can use HttpServer to start the servlet engine and one connector.
 *  If you want more flexibility you need to use the admin interface.( for example to
 *  have the same Context shared between multiple connectors, etc ).
 * 
 *  HttpServer is the simplest way to add Tomcat to an application, but has
 *  some limitations ( only one connector / engine ).
 *
 *  XXX Right now HttpServerConnector is loaded unless a "connector.properties" file is
 *  found in classpath. This is a hack to allow the old startup model to work ( i.e. use
 *  the old HttpServer, and no options to specify a connector ).
 *  The properties file specify what connector to use for a certain host/port combination
 *  and options.
 *  
 */

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
public class HttpServer {
    // XXX move to Constants
    public static final String VHOST_PORT="vhost_port";
    public static final String VHOST_NAME="vhost_name";

    // needed only as a "hack" to HttpServerConnector
    public static final String SERVER="server";
    public static final String VHOST_ADDRESS="vhost_address";
    public static final String SOCKET_FACTORY="socketFactory";
    
    public static final String CONNECTOR_PROP="/connector.properties";
    
    /**
     * The context manager with which this server is associated.
     */

    ContextManager contextM;

    /**
     * The current number of server connectors attached to this server.
     */
    int connector_count=0;
    
    // set to connector before start
    /**
     * The IP address this server should listen on, or <code>null</code>
     * for all IP addresses associated with this server.
     */

    InetAddress address;

    /**
     * The socket factory associated with this server.
     */

    ServerSocketFactory factory;
    
    Properties props;
    private StringManager sm =
        StringManager.getManager("org.apache.tomcat.server");

    private String hostname = "";
    private String serverHeader = null;

    String workDir;
    /**
     * Creates a new server with the default configuration of port=80,
     * address=null, and hostname=null.
     */

    public HttpServer() {
        //this(80, null, null);
    }

    
    /**
     * Creates a new server with the given properties
     *
     * @param port The TCP port number on which to listen for connections
     * @param address The IP address on which to listen for connections, or
     *  <code>null</code> to listen on all interfaces on this server
     * @param hostname The virtual host name of this server, or
     *  <code>null</code> for no defined host name
     */

    public HttpServer(int port, InetAddress address, String hostname) {
	contextM= new ContextManager(); 
	init(port, address, hostname, contextM);
    }


    /**
     * Creates a new server with the given properties
     *
     * @param port The TCP port number on which to listen for connections
     * @param address The IP address on which to listen for connections, or
     *  <code>null</code> to listen on all interfaces on this server
     * @param hostname The virtual host name of this server, or
     *  <code>null</code> for no defined host name
     * @param contextM The context manager with which this server is associated
     */

    public HttpServer(int port, InetAddress address, String hostname,
        ContextManager contextM) {
	init(port, address, hostname, contextM);
    }


    /**
     * Initialize the properties of this server.
     *
     * @param port The TCP port number on which to listen for connections
     * @param address The IP address on which to listen for connections, or
     *  <code>null</code> to listen on all interfaces on this server
     * @param hostname The virtual host name of this server, or
     *  <code>null</code> for no defined host name
     * @param contextM The context manager with which this server is associated
     */

    public void init(int port, InetAddress address, String hostname,
		     ContextManager contextM) {
	this.contextM=contextM;
	if( (hostname==null) || "".equals(hostname) )
	    hostname="localhost"; // XXX 
	setPort( port );
	setAddress( address );
	setHostName( hostname );

	// No need to worry about root context - someone may add it later,
	// or an exception will be thrown 
    }


    public void setWorkDir( String wd ) {
	workDir=wd;
    }
    
    /**
     * Return the context manager with which this server is associated.
     */
    public ContextManager getContextManager() {
	return contextM;
    }
    

    /**
     * Sets the context manager with which this server is associated.
     *
     * @param cm The new context manager
     */

    public void setContextManager(ContextManager cm) {
	this.contextM=cm;
	cm.setWorkDir( workDir );
    }
    

    /**
     * Add the specified server connector to the those attached to this server.
     *
     * @param con The new server connector
     */

    public synchronized void addConnector( ServerConnector connector ) {
	connector.setAttribute(SERVER, this);
	connector.setContextManager( contextM );
	    
	connector.setAttribute(VHOST_PORT, new Integer( getPort() ));
	connector.setAttribute(VHOST_ADDRESS, address);	
	connector.setAttribute(SOCKET_FACTORY, factory);
	connector.setAttribute( VHOST_NAME, getHostName() );

	contextM.addServerConnector( connector );

	connector_count++;
    }

    public synchronized void addConnector( String classN ) {
	try {
	    Class c=Class.forName( classN );
	    ServerConnector conn=(ServerConnector)c.newInstance();
	    addConnector( conn );
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
    }



    /**
     * Gets the port that the server is listening to requests on.
     */

    public int getPort() {
	//        Integer port=(Integer)connector.getAttribute(VHOSTPORT);
	//return port.intValue();
	return contextM.getPort();
    }


    /**
     * Sets the port of the server to the given port.
     *
     * @param port The TCP port number this server should listen on
     * @throws IllegalStateException if the server is running
     */

    public void setPort(int port) {
	contextM.setPort(port);
    }


    /**
     * Sets the SocketFactory for this server.....
     *
     * @param ServerSocketFactory The new socket factory
     * @throws IllegalStateException if the server is running
     */

    public void setDefaultSocketFactory(ServerSocketFactory factory) {
	this.factory=factory;
    }
    

    /**
     * Gets the address, if any, that the server is listening to.
     */

    public InetAddress getAddress() {
        return address;
    }


    /**
     * Sets the address that the server is listening on.
     *
     * @param address The address that this server should listen on,
     *  or <code>null</code> to listen to all addresses for this server
     * @throws IllegalStateException if the server is running
     */
    
    public void setAddress(InetAddress address) {
	this.address=address;
    }


    /**
     * Gets the hostname of the server.
     */
    
    public String getHostName() {
        return contextM.getHostName();
	//return hostname;
    }


    /**
     * Sets the hostname of the server. This is used for HTTP/1.1
     * style virtual hosting.
     *
     * @param hostname The host name for this server
     * @throws IllegalStateException if the server is running
     */
    
    public void setHostName(String hostname) {
	//        this.hostname = hostname;
	contextM.setHostName( hostname );
    }


    /**
     * Gets the server header for this server.
     */
    public String getServerHeader() {
	// XXX XXX Ugly, but we want to remove the properties stuff
	// which is worse
	if(serverHeader==null) serverHeader="Tomcat Web Server 3.1M1";
        return serverHeader;
    }


    /**
     * Starts the server.
     */
    public void start() throws HttpServerException {
	try {
	    contextM.start();
	} catch(Exception ex) {
	    ex.printStackTrace();
	    throw new HttpServerException(ex);
	}
    }


    /**
     * Stops the server.
     */
  
    public void stop() throws HttpServerException{
	try {
	    contextM.stop();
	} catch(Exception ex) {
	    throw new HttpServerException(ex);
	}
    }

    /** Called before starting the connector
     */
    private void initConnector() {
    	if(connector_count==0) {
	    // find a connector for the vhost:port combination
	    // Use props and CONNECTOR_PROP to load configuration info
	    // default is HttpServerConnector
	    SimpleTcpConnector sc=new SimpleTcpConnector();
	    sc.setTcpConnectionHandler( new HttpConnectionHandler());
	    addConnector(  sc );
	}
    }
}
