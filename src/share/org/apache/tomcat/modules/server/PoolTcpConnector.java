/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.server;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.net.PoolTcpEndpoint;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.ServerSocketFactory;


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
    protected SSLImplementation sslImplementation;
    // socket factory attriubtes ( XXX replace with normal setters ) 
    protected Hashtable attributes = new Hashtable();
    protected String socketFactoryName=null;
    protected String sslImplementationName=null;
    protected boolean secure=false;

    public PoolTcpConnector() {
    	ep = new PoolTcpEndpoint();
    }

    // -------------------- Start/stop --------------------

    /** Called when the ContextManger is started
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	super.engineInit( cm );
	checkSocketFactory();

	try {
	    localInit();
	} catch( Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    /** Called when the ContextManger is started
     */
    public void engineStart(ContextManager cm) throws TomcatException {
	try {
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

    /** Sanity check and socketFactory setup.
     *  IMHO it is better to stop the show on a broken connector,
     *  then leave Tomcat running and broken.
     *  @exception TomcatException Unable to resolve classes
     */
    private void checkSocketFactory() throws TomcatException {
	if(secure) {
 	    try {
 		// The SSL setup code has been moved into
 		// SSLImplementation since SocketFactory doesn't
 		// provide a wide enough interface
 		sslImplementation=SSLImplementation.getInstance
 		    (sslImplementationName);
                socketFactory = 
                        sslImplementation.getServerSocketFactory();
		ep.setServerSocketFactory(socketFactory);
 	    } catch (ClassNotFoundException e){
 		throw new TomcatException("Error loading SSLImplementation ",
 					  e);
  	    }
  	}
 	else {
 	    if (socketFactoryName != null) {
 		try {
 		    socketFactory = string2SocketFactory(socketFactoryName);
 		    ep.setServerSocketFactory(socketFactory);
 		} catch(Exception sfex) {
 		    throw new TomcatException("Error Loading Socket Factory " +
 					      socketFactoryName,
 					      sfex);
 		}
	    }
	}
    }
    public void setSocketFactory( String valueS ) {
	socketFactoryName = valueS;
    }
    public void setSSLImplementation( String valueS) {
 	sslImplementationName=valueS;
    }
 	

    // -------------------- Socket options --------------------

    public void setTcpNoDelay( boolean b ) {
	ep.setTcpNoDelay( b );
    }

    public void setSoLinger( int i ) {
	ep.setSoLinger( i );
    }

    public void setSoTimeout( int i ) {
	ep.setSoTimeout(i);
    }
    
    public void setServerSoTimeout( int i ) {
	ep.setServerSoTimeout( i );
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

    // -------------------- SocketFactory attributes --------------------
    public void setKeystore( String k ) {
	attributes.put( "keystore", k);
    }

    public void setKeypass( String k ) {
	attributes.put( "keypass", k);
    }

    public void setClientauth( String k ) {
	attributes.put( "clientauth", k);
    }

    public boolean isKeystoreSet() {
        return (attributes.get("keystore") != null);
    }

    public boolean isKeypassSet() {
        return (attributes.get("keypass") != null);
    }

    public boolean isClientauthSet() {
        return (attributes.get("clientauth") != null);
    }

    public boolean isAttributeSet( String attr ) {
        return (attributes.get(attr) != null);
    }

    public void setSecure( boolean b ) {
    	secure=b;
    }

    public boolean isSecure() {
        return secure;
    }
    
    public void setAttribute( String prop, Object value) {
	attributes.put( prop, value );
    }

    private static ServerSocketFactory string2SocketFactory( String val)
	throws ClassNotFoundException, IllegalAccessException,
	InstantiationException
    {
	Class chC=Class.forName( val );
	return (ServerSocketFactory)chC.newInstance();
    }

    /**
     * Return <code>true</code> if the specified client and server addresses
     * are the same.  This method works around a bug in the IBM 1.1.8 JVM on
     * Linux, where the address bytes are returned reversed in some
     * circumstances.
     *
     * @param server The server's InetAddress
     * @param client The client's InetAddress
     */
    public static boolean isSameAddress(InetAddress server, InetAddress client) {
	// Compare the byte array versions of the two addresses
	byte serverAddr[] = server.getAddress();
	byte clientAddr[] = client.getAddress();
	if (serverAddr.length != clientAddr.length)
	    return (false);
	boolean match = true;
	for (int i = 0; i < serverAddr.length; i++) {
	    if (serverAddr[i] != clientAddr[i]) {
		match = false;
		break;
	    }
	}
	if (match)
	    return (true);
	
	// Compare the reversed form of the two addresses
	for (int i = 0; i < serverAddr.length; i++) {
	    if (serverAddr[i] != clientAddr[(serverAddr.length-1)-i])
		return (false);
	}
	return (true);
    }

//     public static String getExtension( String classN ) {
// 	int lidot=classN.lastIndexOf( "." );
// 	if( lidot >0 ) classN=classN.substring( lidot + 1 );
// 	return classN;
//     }

}
