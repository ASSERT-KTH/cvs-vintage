/*
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

import org.apache.tomcat.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.net.*;
import org.apache.tomcat.logging.*;
import java.io.*;
import java.net.*;
import java.util.*;


/* Similar with MPM module in Apache2.0. Handles all the details related with
   "tcp server" functionality - thread management, accept policy, etc.
   It should do nothing more - as soon as it get a socket (
   and all socket options are set, etc), it just handle the stream
   to ConnectionHandler.processConnection. (costin)
*/



/**
 * Connector for a TCP-based connector using the API in tomcat.service.
 * You need to set a "connection.handler" property with the class name of
 * the TCP connection handler
 *
 * @author costin@eng.sun.com
 * @author Gal Shachor [shachor@il.ibm.com]
 */
public abstract class PoolTcpConnector extends BaseInterceptor
{
    protected PoolTcpEndpoint ep;
    protected ServerSocketFactory socketFactory;
    // socket factory attriubtes ( XXX replace with normal setters ) 
    protected Hashtable attributes = new Hashtable();
    protected boolean enabled=true;
    protected boolean secure=false;
    
    public PoolTcpConnector() {
    	ep = new PoolTcpEndpoint();
    }

    // -------------------- Start/stop --------------------

    /** Called when the ContextManger is started
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	super.engineInit( cm );

	try {
	    localInit();
	    if( socketFactory!=null ) {
		Enumeration attE=attributes.keys();
		while( attE.hasMoreElements() ) {
		    String key=(String)attE.nextElement();
		    Object v=attributes.get( key );
		    socketFactory.setAttribute( key, v );
		}
	    }
	    ep.startEndpoint();
	    log( "Starting on " + ep.getPort() );
	} catch( Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    public void engineShutdown(ContextManager cm) throws TomcatException {
	try {
	    ep.stopEndpoint();
	} catch( Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    
    protected abstract void localInit() throws Exception;
    
// 	//    	setTcpHandler();

// 	// Pass attributes to the socket factory
// 	attE=attributes.keys();
// 	while( attE.hasMoreElements() ) {
// 	    String key=(String)attE.nextElement();
// 	    Object v=attributes.get( key );
// 	    socketFactory.setAttribute( key, v );
// 	}

// 	ep.startEndpoint();

// 	loghelper.log("Starting " + getExtension(con.getClass().getName())
// 		      + " on " + ep.getPort());
//     }

//     public void setTcpHandler() throws Exception {
//     	if(con==null)
//     	    throw new Exception( "Invalid ConnectionHandler");
// 	con.setServer( cm );
// 	con.setAttribute("context.manager",cm ); // old mechanism


// 	// Pass properties to the handler
// 	Enumeration attE=attributes.keys();
// 	while( attE.hasMoreElements() ) {
// 	    String key=(String)attE.nextElement();
// 	    Object v=attributes.get( key );
// 	    con.setAttribute( key, v );
// 	}
// 	ep.setConnectionHandler( con );
//     }

    // -------------------- Pool setup --------------------

    public void setPools( boolean t ) {
	ep.setPoolOn(t);
    }

    public void setMaxThreads( int maxThreads ) {
	ep.setMaxThreads(maxThreads);
    }

    public void setMaxSpareThreads( int maxThreads ) {
	ep.setMaxSpareThreads(maxThreads);
    }

    public void setMinSpareThreads( int minSpareThreads ) {
	ep.setMinSpareThreads(minSpareThreads);
    }

    // -------------------- Tcp setup --------------------

    public void setBacklog( int i ) {
	ep.setBacklog(i);
    }
    
    public void setPort( int port ) {
	ep.setPort(port);
    	//this.port=port;
    }

    public void setAddress(InetAddress ia) {
	ep.setAddress( ia );
    }

    public void setHostName( String name ) {
	// ??? Doesn't seem to be used in existing or prev code
	// vhost=name;
    }

    public void setSocketFactory( String valueS ) {
	try {
	    socketFactory= string2SocketFactory( valueS );
	    ep.setServerSocketFactory( socketFactory );
	}catch (Exception ex ) {
	    ex.printStackTrace();
	}
    }

    // -------------------- Getters --------------------
    
    public PoolTcpEndpoint getEndpoint() {
	return ep;
    }
    
    public int getPort() {
    	return ep.getPort();
    }

    public InetAddress getAddress() {
	return ep.getAddress();
    }

    // -------------------- SocketFactory attriubtes --------------------
    public void setKeystore( String k ) {
	attributes.put( "keystore", k);
    }

    public void setKeyspass( String k ) {
	attributes.put( "keypass", k);
    }

    public static final String SSL_CHECK=
	"javax.net.ssl.SSLServerSocketFactory";
    public static final String SSL_FACT=
	"org.apache.tomcat.util.net.SSLSocketFactory";
    
    
    public void setSecure( boolean b ) {
	enabled=false;
	secure=false;
	if( b == true ) {
	    // 	    if( keystore!=null && ! new File( keystore ).exists() ) {
	    // 		log("Can't find keystore " + keystore );
	    // 		return;
	    // 	    }
	    try {
		Class c1=Class.forName( SSL_CHECK );
	    } catch( Exception ex ) {
		log( "Can't find JSSE, HTTPS will not be enabled");
		return;
	    }
	    try {
		Class chC=Class.forName( SSL_FACT );
		socketFactory=(ServerSocketFactory)chC.newInstance();
		ep.setServerSocketFactory( socketFactory );
		log( "Setting ssl socket factory ");
	    } catch(Exception ex ) {
		log( "Error loading SSL socket factory ", ex);
		return;
	    }
	}
    	secure=b;
	enabled=true;
    }
    
    public void setAttribute( String prop, Object value) {
	attributes.put( prop, value );
    }


    // -------------------- Handler ( deprecated ) --------------------
//     String handlerClassName;
//     TcpConnectionHandler con;

//     public void setTcpConnectionHandler( TcpConnectionHandler handler) {
//     	this.con=handler;
//     }

//     public void setTcpConnectionHandler( String s) {
//     	this.con=string2ConnectionHandler( s );
//     }

//     public TcpConnectionHandler getTcpConnectionHandler() {
// 	    return con;
//     }

    // -------------------- Implementation methods --------------------

    // now they just throw exceptions, which are caught and logged by
    // the caller

//     private static TcpConnectionHandler string2ConnectionHandler( String val)
// 	throws ClassNotFoundException, IllegalAccessException,
// 	InstantiationException
//     {
// 	Class chC=Class.forName( val );
// 	return (TcpConnectionHandler)chC.newInstance();
//     }

    private static ServerSocketFactory string2SocketFactory( String val)
	throws ClassNotFoundException, IllegalAccessException,
	InstantiationException
    {
	Class chC=Class.forName( val );
	return (ServerSocketFactory)chC.newInstance();
    }

    private static InetAddress string2Inet( String val)
	throws UnknownHostException
    {
	return InetAddress.getByName( val );
    }
    
    private static int string2Int( String val) {
	return Integer.parseInt(val);
    }


    public static String getExtension( String classN ) {
	int lidot=classN.lastIndexOf( "." );
	if( lidot >0 ) classN=classN.substring( lidot + 1 );
	return classN;
    }

}










// -- Removed code ( put it back if something goes wrong, remove it later)
/*
  // Attributes we accept ( to support the old model of
    // configuration, will be deprecated )
    public static final String VHOST_PORT="vhost_port";
    public static final String VHOST_NAME="vhost_name";
    public static final String VHOST_ADDRESS="vhost_address";
    public static final String SOCKET_FACTORY="socketFactory";


    public static final String INET = "inet";
    public static final String PORT = "port";
    public static final String HANDLER = "handler";


    // Threading and mod_mpm style properties.
    public static final String THREAD_POOL = "thread_pool";
    public static final String MAX_THREADS = "max_threads";
    public static final String MAX_SPARE_THREADS = "max_spare_threads";
    public static final String MIN_SPARE_THREADS = "min_spare_threads";
    public static final String BACKLOG = "backlog";

    Object cm;

    ** Generic configure system - this allows Connector
     * 	configuration using name/value.
     *
     *  The "prefered" configuration is to call setters,
     * 	and tomcat using server.xml will do that, but
     *	this allows (minimal) integration with simpler
     *	systems.
     *
     *  Only a minimal number of properties can be set
     *  this way. This mechanism may be deprecated
     *  after we improve the startup system.
     *
     *  Supported attributes:
     *  "vhost_port" - port ( will act as a virtual host )
     *  "vhost_name" - virtual host name 
     *  "vhost_address" - virtual host binding address
     *  "socketFactory" - socket factory - for ssl.
     *  XXX add the others
     * 
     *  Note that the attributes are passed to the Endpoint.
     *
     public void setAttribute( String prop, Object value) {
	if( debug > 0 ) 
	    loghelper.log( "setAttribute( " + prop + " , " + value + ")");

	try {
	if( value instanceof String ) {
	    String valueS=(String)value;
	    
	    if( PORT.equals(prop) ) {
		setPort( valueS );
	    } else if(HANDLER.equals(prop)) {
		con=string2ConnectionHandler( valueS );
	    } else if(THREAD_POOL.equals(prop)) {
		usePools = ! valueS.equalsIgnoreCase("off");
	    } else if(INET.equals(prop)) {
		address=string2Inet( valueS );
	    } else if( MAX_THREADS.equals(prop)) {
		maxThreads = string2Int(valueS);
	    } else if( MAX_SPARE_THREADS.equals(prop)) {
		maxSpareThreads = string2Int(valueS);
	    } else if( MIN_SPARE_THREADS.equals(prop)) {
		minSpareThreads = string2Int(valueS);
	    } else if(VHOST_NAME.equals(prop) ) {
		vhost=valueS;
	    } else if( BACKLOG.equals(prop)) {
		backlog = string2Int(valueS);
	    } else if(VHOST_PORT.equals(prop) ) {
		port= string2Int( valueS );
	    } else if(SOCKET_FACTORY.equals(prop)) {
		socketFactory= string2SocketFactory( valueS );
	    } else if(VHOST_ADDRESS.equals(prop)) {
		address= string2Inet(valueS);
	    } else {
		if( valueS!=null)
		    attributes.put( prop, valueS );
	    }
	} else {
	    // Objects - avoids String-based "serialization" 
	    if(VHOST_PORT.equals(prop) ) {
		port=((Integer)value).intValue();
	    } else if(VHOST_ADDRESS.equals(prop)) {
		address=(InetAddress)value;
	    } else if(SOCKET_FACTORY.equals(prop)) {
		socketFactory=(ServerSocketFactory)value;
	    } else {
		if( value!=null)
		    attributes.put( prop, value );
	    }
	}
	}
	catch (Exception e) {
	    loghelper.log("setAttribute: " +prop + "=" + value, e, Logger.ERROR);
	}
	}

	public void setProperty( String prop, String value) {
	setAttribute( prop, value );
	}


	    // -------------------- Implementation methods --------------------

    public void setPort(  String portS ) {
	this.port=string2Int( portS );
    }

    public void setAddress(String addressS) {
	address= string2Inet( addressS );	
    }



*/
