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


package org.apache.tomcat.core;

import org.apache.tomcat.core.*;
import org.apache.tomcat.net.*;
import org.apache.tomcat.context.*;
import org.apache.tomcat.request.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.logging.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * A collection class representing the Contexts associated with a particular
 * Server.  The managed Contexts can be accessed by path.
 *
 * It also store global default properties - the server name and port ( returned by
 * getServerName(), etc) and workdir.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */
public class ContextManager {
    /**
     * The string constants for this ContextManager.
     */
    private StringManager sm = StringManager.getManager("org.apache.tomcat.core");

    private Vector requestInterceptors = new Vector();
    private Vector contextInterceptors = new Vector();
    
    // cache - faster access
    ContextInterceptor cInterceptors[];
    RequestInterceptor rInterceptors[];
    
    /**
     * The set of Contexts associated with this ContextManager,
     * keyed by context paths.
     */
    private Hashtable contexts = new Hashtable();

    public static final String DEFAULT_HOSTNAME="localhost";
    public static final int DEFAULT_PORT=8080;
    public static final String DEFAULT_WORK_DIR="work";
    
    /**
     * The virtual host name for the Server this ContextManager
     * is associated with.
     */
    String hostname;

    /**
     * The port number being listed to by the Server this ContextManager
     * is associated with.
     */
    int port;

    int debug=0;
    String workDir;

    // Instalation directory  
    String home;
    
    Vector connectors=new Vector();

    
    /**
     * Construct a new ContextManager instance with default values.
     */
    public ContextManager() {
    }

    // -------------------- Context repository --------------------

    /** Set default settings ( interceptors, connectors, loader, manager )
     *  It is called from init if no connector is set up  - note that we
     *  try to avoid any "magic" - you either set up everything ( using
     *  server.xml or alternatives) or you don't set up and then defaults
     *  will be used.
     * 
     *  Set interceptors or call setDefaults before adding contexts.
     */
    public void setDefaults() {
	if(connectors.size()==0) {
	    log("Setting default adapter");
	    addServerConnector(  new org.apache.tomcat.service.http.HttpAdapter() );
	}
	
	if( contextInterceptors.size()==0) {
	    log("Setting default context interceptors");
	    addContextInterceptor(new LogEvents());
	    addContextInterceptor(new AutoSetup());
	    addContextInterceptor(new DefaultCMSetter());
	    addContextInterceptor(new WorkDirInterceptor());
	    addContextInterceptor( new WebXmlReader());
	    addContextInterceptor(new LoadOnStartupInterceptor());
	}
	
	if( requestInterceptors.size()==0) {
	    log("Setting default request interceptors");
	    SimpleMapper smap=new SimpleMapper();
	    smap.setContextManager( this );
	    addRequestInterceptor(smap);
	    addRequestInterceptor(new SessionInterceptor());
	}
    }
     
    
    /**
     * Get the names of all the contexts in this server.
     */
    public Enumeration getContextNames() {
        return contexts.keys();
    }

    /** Init() is called after the context manager is set up
     *  and configured. 
     */
    public void init()  throws TomcatException {
	//	long time=System.currentTimeMillis();
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineInit( this );
	}
	
    	// init contexts
	Enumeration enum = getContextNames();
	while (enum.hasMoreElements()) {
            Context context = getContext((String)enum.nextElement());
            initContext( context );
	}
	//	log("Time to initialize: "+ (System.currentTimeMillis()-time), Logger.INFORMATION);
    }

    
    /**
     * Initializes this context to take on requests. This action
     * will cause the context to load it's configuration information
     * from the webapp directory in the docbase.
     *
     * <p>This method may only be called once and must be called
     * before any requests are handled by this context and after setContextManager()
     * is called.
     */
    public void initContext( Context ctx ) throws TomcatException {
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].contextInit( ctx );
	}
    }
    
    public void shutdownContext( Context ctx ) throws TomcatException {
	// shut down and servlet
	Enumeration enum = ctx.getServletNames();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper wrapper = ctx.getServletByName( key );
	    ctx.removeServletByName( key );
	    wrapper.destroy();
	}
	
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].contextShutdown( ctx );
	}
    }
    
    /** Will start the connectors and begin serving requests
     */
    public void start() throws Exception {//TomcatException {
	Enumeration connE=getConnectors();
	while( connE.hasMoreElements() ) {
	    ((ServerConnector)connE.nextElement()).start();
	}
    }

    public void stop() throws Exception {//TomcatException {
	log("Stopping context manager ");
	Enumeration connE=getConnectors();
	while( connE.hasMoreElements() ) {
	    ((ServerConnector)connE.nextElement()).stop();
	}

	ContextInterceptor cI[]=getContextInterceptors();
	Enumeration enum = getContextNames();
	while (enum.hasMoreElements()) {
	    removeContext((String)enum.nextElement());
	}
    }

    /**
     * Gets a context by it's name, or <code>null</code> if there is
     * no such context.
     *
     * @param name Name of the requested context
     */
    public Context getContext(String name) {
	return (Context)contexts.get(name);
    }
    
    /**
     * Adds a new Context to the set managed by this ContextManager.
     *
     * @param ctx context to be added.
     */
    public void addContext( Context ctx ) throws TomcatException {
	// Make sure context knows about its manager.
	ctx.setContextManager( this );

	// it will replace existing context - it's better than  IllegalStateException.
	String path=ctx.getPath();
	if( getContext( path ) != null ) {
	    log("Warning: replacing context for " + path, Logger.WARNING);
	    removeContext(path);
	}

	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addContext( this, ctx );
	}
	
	log(" adding " + ctx + " " + ctx.getPath() + " " +  ctx.getDocBase(), Logger.INFORMATION);

	contexts.put( path, ctx );
    }
    
    /**
     * Shut down and removes a context from service.
     *
     * @param name Name of the Context to be removed
     */
    public void removeContext(String name) throws TomcatException {
	if (name.equals("")){
	    throw new IllegalArgumentException(name);
	}

	Context context = (Context)contexts.get(name);

	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeContext( this, context );
	}

	if(context != null) {
	    shutdownContext( context );
	    contexts.remove(name);
	}
    }

    public void removeServlet( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeServlet( ctx, sw );
	}

    }

    public void addServlet( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addServlet( ctx, sw );
	}
    }

    public void addMapping( Context ctx ,String path, ServletWrapper sw )
    	throws TomcatException
    {
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addMapping( ctx, path, sw );
	}
    }

    public void removeMapping( Context ctx, String path )
    	throws TomcatException
    {
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeMapping( ctx, path );
	}
    }

    public void addSecurityConstraint( Context ctx, String path[], String methods[],
				       String transport, String roles[] )
	throws TomcatException
    {
	ContextInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addSecurityConstraint( ctx, path, methods, transport, roles );
	}
    }


    // -------------------- Connectors and Interceptors --------------------

    /**
     * Add the specified server connector to the those attached to this server.
     *
     * @param con The new server connector
     */
    public synchronized void addServerConnector( ServerConnector con ) {
	log(" adding connector " + con.getClass().getName(), Logger.INFORMATION);
	con.setContextManager( this );
	connectors.addElement( con );
    }

    public Enumeration getConnectors() {
	return connectors.elements();
    }
    
    public void addRequestInterceptor( RequestInterceptor ri ) {
	log(" adding request intereptor " + ri.getClass().getName(), Logger.INFORMATION);
	requestInterceptors.addElement( ri );
	if( ri instanceof ContextInterceptor )
	    contextInterceptors.addElement( ri );
	// XXX XXX use getMethods() to find what notifications are needed by interceptor
	// ( instead of calling all interceptors )
	// No API change - can be done later.
    }

    /** Return the context interceptors as an array.
	For performance reasons we use an array instead of
	returning the vector - the interceptors will not change at
	runtime and array access is faster and easier than vector
	access
    */
    public RequestInterceptor[] getRequestInterceptors() {
	if( rInterceptors == null || rInterceptors.length != requestInterceptors.size()) {
	    rInterceptors=new RequestInterceptor[requestInterceptors.size()];
	    for( int i=0; i<rInterceptors.length; i++ ) {
		rInterceptors[i]=(RequestInterceptor)requestInterceptors.elementAt(i);
	    }
	}
	return rInterceptors;
    }

    public void addContextInterceptor( ContextInterceptor ci) {
	contextInterceptors.addElement( ci );
    }


    /** Return the context interceptors as an array.
	For performance reasons we use an array instead of
	returning the vector - the interceptors will not change at
	runtime and array access is faster and easier than vector
	access
    */
    public ContextInterceptor[] getContextInterceptors() {
	if( contextInterceptors.size() == 0 ) {
	    setDefaults();
	}
	if( cInterceptors == null || cInterceptors.length != contextInterceptors.size()) {
	    cInterceptors=new ContextInterceptor[contextInterceptors.size()];
	    for( int i=0; i<cInterceptors.length; i++ ) {
		cInterceptors[i]=(ContextInterceptor)contextInterceptors.elementAt(i);
	    }
	}
	return cInterceptors;
    }

    public void addLogger(Logger logger) {
	// Will use this later once I feel more sure what I want to do here.
	// -akv
    }


    // -------------------- Defaults for all contexts --------------------
    /** The root directory of tomcat
     */
    public String getHome() {
	if( home != null ) return home;
	// XXX check if TOMCAT_HOME env is set 

	// Convert "." to absolute path
	home=new File("").getAbsolutePath();
	return home;
    }
    
    /** Set instalation directory
     */
    public void setHome(String home) {
	this.home=home;
    }
    
    /**
     * Sets the port number on which this server listens.
     *
     * @param port The new port number
     * @deprecated 
     */
    public void setPort(int port) {
	this.port=port;
    }

    /**
     * Gets the port number on which this server listens.
     * @deprecated 
     */
    public int getPort() {
	if(port==0) port=DEFAULT_PORT;
	return port;
    }

    /**
     * Sets the virtual host name of this server.
     *
     * @param host The new virtual host name
     * @deprecated 
     */
    public void setHostName( String host) {
	this.hostname=host;
    }

    /**
     * Gets the virtual host name of this server.
     * @deprecated 
     */
    public String getHostName() {
	if(hostname==null)
	    hostname=DEFAULT_HOSTNAME;
	return hostname;
    }

    /**
     * WorkDir property - where all temporary files will be created
     */ 
    public void setWorkDir( String wd ) {
	log("set work dir " + wd, Logger.INFORMATION);
	this.workDir=wd;
    }

    public String getWorkDir() {
	if( workDir==null)
	    workDir=DEFAULT_WORK_DIR;
	return workDir;
    }
    
    // -------------------- Request processing / subRequest --------------------
    
    /** Common for all connectors, needs to be shared in order to avoid
	code duplication
    */
    public void service( Request rrequest, Response rresponse ) {
	try {
	    rrequest.setResponse(rresponse);
	    rresponse.setRequest(rrequest);

	    // XXX
	    //    return if an error was detected in processing the
	    //    request line
	    if (rresponse.getStatus() >= 400) {
		rresponse.finish();
		rrequest.recycle();
		rresponse.recycle();
		return;
	    }

	    // XXX Hardcoded - it will be changed in the next step.( costin )

	    processRequest( rrequest );

	    if( rrequest.getWrapper() == null ) {
		log("ERROR: mapper returned no wrapper ");
		log(rrequest.toString() );
		// XXX send an error - it shouldn't happen, mapper is broken
	    } else {
		// do it
		rrequest.getWrapper().handleRequest(rrequest.getFacade(),
						    rresponse.getFacade());
	    }
	    
	    // finish and clean up
	    rresponse.finish();
	    
	} catch (Exception e) {
	    if(e instanceof IOException && "Broken pipe".equals(e.getMessage()) ) {
	    }
	    // XXX
	    // this isn't what we want, we want to log the problem somehow
	    log("HANDLER THREAD PROBLEM: " + e);
	    log("Request: " + rrequest);
	    e.printStackTrace();
	}
    }

    // XXX need to be changed to use a full sub-request model (costin)
    
    /** Will find the ServletWrapper for a servlet, assuming we already have
     *  the Context. This is used by Dispatcher and getResource - where the Context
     *  is already known.
     */
    int processRequest( Request req ) {

	log("ProcessRequest: "+req.toString(), Logger.DEBUG);

	for( int i=0; i< requestInterceptors.size(); i++ ) {
	    ((RequestInterceptor)requestInterceptors.elementAt(i)).contextMap( req );
	}

	for( int i=0; i< requestInterceptors.size(); i++ ) {
	    ((RequestInterceptor)requestInterceptors.elementAt(i)).requestMap( req );
	}

	log("After processing: "+req.toString(), Logger.DEBUG);

	return 0;
    }

    int doBeforeBody( Request req, Response res ) {
	for( int i=0; i< requestInterceptors.size(); i++ ) {
	    ((RequestInterceptor)requestInterceptors.elementAt(i)).beforeBody( req, res );
	}
	return 0;
    }
    
    // -------------------- Sub-Request mechanism --------------------

    /** Create a new sub-request in a given context, set the context "hint"
     *  This is a particular case of sub-request that can't get out of
     *  a context ( and we know the context before - so no need to compute it again)
     *
     *  Note that session and all stuff will still be computed.
     */
    Request createRequest( Context ctx, String urlPath ) {
	// assert urlPath!=null

	// deal with paths with parameters in it
	String queryString=null;
	int i = urlPath.indexOf("?");
	int len=urlPath.length();
	if (i>-1) {
	    if(i<len)
		queryString =urlPath.substring(i + 1, urlPath.length());
	    urlPath = urlPath.substring(0, i);
	}

	/** Creates an "internal" request
	 */
	RequestImpl lr = new RequestImpl();
	//	RequestAdapterImpl reqA=new RequestAdapterImpl();
	//lr.setRequestAdapter( reqA);
	lr.setLookupPath( urlPath );
	lr.setQueryString( queryString );
	//	lr.processQueryString();

	lr.setContext( ctx );
	
	// XXX set query string too 
	return lr;
    }

    public void setDebug( int level ) {
	
	if( level != 0 ) System.out.println( "Setting level to " + level);
	debug=level;
    }

    boolean firstLog = true;
    Logger cmLog = null;
    
    public final void log(String msg) {
	if (firstLog == true) {
	    cmLog = Logger.getLogger("CTXMGR_LOG");
	    firstLog = false;
	}

	if (cmLog != null)
	    cmLog.log(msg, Logger.DEBUG);
    }

    public final void log(String msg, int level) {
	if (firstLog == true) {
	    cmLog = Logger.getLogger("CTXMGR_LOG");
	    firstLog = false;
	}

	if (cmLog != null)
	    cmLog.log(msg, level);
    }
    
}
