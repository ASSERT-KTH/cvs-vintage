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
import org.apache.tomcat.context.*;
import org.apache.tomcat.request.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ContextManager is the entry point and "controler" of the servlet execution.
 * It maintains a list of WebApplications and a list of global event
 * interceptors that are set up to handle the actual execution.
 *
 * The ContextManager will direct the request processing flow
 * from its arrival from the server/protocl adapter ( in service() ).
 * It will do that by calling a number of hooks implemented by Interceptors.
 *
 * Hooks are provided for request parsing and mapping, auth, autorization,
 * pre/post service, actual invocation and logging.
 *
 * ContextManager will also store properties that are global to the servlet
 * container - like root directory, install dir, work dir.
 *
 * The extension mechanism for tomcat is the Interceptor.
 * This class is final - if you need to change certain functionality
 * you should add a new hook.
 *
 * ContextManager is not a singleton - it represent a servlet container
 * instance ( with all associated ports and configurations ).
 * One application may try to embed multiple ( distinct ) servlet containers -
 * this feature hasn't been tested or used
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 * @author Hans Bergsten [hans@gefionsoftware.com]
 */
public final class ContextManager implements LogAware{
    /** Official name and version
     */
    public static final String TOMCAT_VERSION = "3.3 dev";
    public static final String TOMCAT_NAME = "Tomcat Web Server";
    
    /** Contexts managed by this server
     */
    private Vector contextsV=new Vector();

    int debug=0;

    // Global properties for this tomcat instance:

    /** Private workspace for this server
     */
    String workDir;

    /** The base directory where this instance runs.
     *  It can be different from the install directory to
     *  allow one install per system and multiple users
     */
    String home;

    /** The directory where tomcat is installed
     */
    String installDir;

    /** Property used to set the random number generator
     */
    public static final String RANDOM_CLASS_PROPERTY=
	"tomcat.sessionid.randomclass";
    
    /** Default work dir, relative to home
     */
    public static final String DEFAULT_WORK_DIR="work";

    Container defaultContainer;

    // the application loader. ContextManager is loaded with
    // a class loader containing tomcat-specific classes,
    // use parent loader to avoid polution
    ClassLoader parentLoader;
    // tomcat classes ( used to load tomcat)
    URL serverClassPath[];
    
    /**
     * Construct a new ContextManager instance with default values.
     */
    public ContextManager() {
        defaultContainer=new Container();
        defaultContainer.setContext( null );
	defaultContainer.setContextManager( this );
        defaultContainer.setPath( null ); // default container
    }

    // -------------------- setable properties: tomcat directories  ---
    /**
     *  The home of the tomcat instance - you can have multiple
     *  users running tomcat, with a shared install directory.
     *  Every instance will have its own logs, webapps directory
     *  and local config, all relative to this directory.
     */
    public void setHome(String home) {
	this.home=FileUtil.getCanonicalPath( home );
	log( "Setting home to " + this.home );
    }

    /**
     *  The home of the tomcat instance - you can have multiple
     *  users running tomcat, with a shared install directory.
     *  Every instance will have its own logs, webapps directory
     *  and local config, all relative to this directory.
     *
     *  If no home is configured we'll try the install dir
     *  XXX clean up the order and process of guessing - maybe we can
     *  just throw error instead of guessing wrong.
     */
    public String getHome() {
	if(home!=null) return home;

	// If none defined, assume tomcat.home is used as base.
	if( installDir != null )
	    home=FileUtil.getCanonicalPath( installDir );

	if(home!=null) return home;

	// try at least the system property
	home=FileUtil.getCanonicalPath( System.getProperty("tomcat.home") );
	if(home!=null) return home;

	home=FileUtil.getCanonicalPath( "." );
	// try current dir - we should throw an exception
	return home;
    }

    /** Get installation directory, where libraries and default files
     *	are located.  If path specified is relative,
     *  evaluate it relative to the current working directory.
     */
    public String getInstallDir() {
	if(installDir!= null) return installDir;

	installDir=System.getProperty("tomcat.home");
	if(installDir!= null) return installDir;

	// If the property is not set ( for example JNI worker ) assume
	// at least home is set up corectly.
	installDir=getHome();
	return installDir;
    }

    /** Set installation directory, where libraries and default files
     *	are located.  If path specified is relative,
     *  evaluate it relative to the current working directory.
     */
    public void setInstallDir( String tH ) {
	installDir=tH;
    }

    /**
     * WorkDir property - where all working files will be created
     */
    public void setWorkDir( String wd ) {
	if(debug>0) log("set work dir " + wd);
	this.workDir=FileUtil.getCanonicalPath( getHome() + File.separator + wd );
    }

    /**
     * WorkDir property - where all working files will be created
     */
    public String getWorkDir() {
	if( workDir==null)
	    workDir=FileUtil.getCanonicalPath(getHome() + File.separator + DEFAULT_WORK_DIR);
	return workDir;
    }

    /** Parent loader is the "base" class loader of the
     *	application that starts tomcat, and includes no
     *	tomcat classes. All servlet loaders will have it as
     *  a parent loader, as if the webapps would be loaded
     *  by the embeding app ( using parentLoader ).
     *
     *  Tomcat will add servlet.jar and any other extension
     *  it is configured to - for example trusted webapps
     *  may have tomcat internal classes in classpath. 
     */
    public void setParentLoader( ClassLoader cl ) {
	parentLoader=cl;
    }

    public ClassLoader getParentLoader() {
	return parentLoader;
    }

    public URL[] getServerClassPath() {
	if( serverClassPath==null ) return new URL[0];
	return serverClassPath;
    }
    
    public void setServerClassPath( URL urls[] ) {
	serverClassPath=urls;
    }

    /** Get the name of the class to be used for generating random numbers by
     * the session id generator. By default this is
     *  <code>java.security.SecureRandom</code>.
     **/
    public String getRandomClass() {
        String randomClass = System.getProperty(RANDOM_CLASS_PROPERTY);
        return randomClass == null ? "java.security.SecureRandom" : randomClass;
    }
    
    /** Sets the name of the class used for generating random numbers by the
     *  session id generator. 
     */
    public void setRandomClass(String randomClass) {
        System.setProperty(RANDOM_CLASS_PROPERTY, randomClass);
    }


    // -------------------- Support functions --------------------

    /**
     *  Init() is called after the context manager is set up
     *  and configured ( all setFoo methods are called, all initial
     *  interceptors are added and their setters are called ).
     *
     *  CM will call the following hooks:
     *   - Interceptor.engineInit()
     *
     *  It will tehn call initContext() for all the contexts (
     *  including those added by interceptors )
     */
    public void init()  throws TomcatException {
	if(debug>0 ) log( "Tomcat classpath = " +
			     System.getProperty( "java.class.path" ));

	cntr.touchCounter( ACC_INIT_START );

	BaseInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineInit( this );
	}

    	// init contexts
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    Context context = (Context)enum.nextElement();
	    try {
		initContext( context );
	    } catch (TomcatException ex ) {
		if( context!=null ) {
		    log( "ERROR initializing " + context.toString(), ex );
		    removeContext( context  );
		}
	    }
	}
	cntr.touchCounter( ACC_INIT_END);
    }

    /** Remove all contexts.
     *  Call Intereptor.engineShutdown hooks.
     */
    public void shutdown() throws TomcatException {
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    removeContext((Context)enum.nextElement());
	}

	BaseInterceptor cI[]=getContextInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineShutdown( this );
	}
    }

    /**
     * Initializes this context to be able to accept requests. This action
     * will cause the context to load it's configuration information
     * from the webapp directory in the docbase.
     *
     * <p>This method must be called
     * before any requests are handled by this context. It will be called
     * after the context was added, typically when the engine starts
     * or after the admin adds a new context.
     */
    public void initContext( Context ctx ) throws TomcatException {
	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].contextInit( ctx );
	}
    }

    /** Stop the context and release all resources.
     */
    public void shutdownContext( Context ctx ) throws TomcatException {
	// XXX This is here by accident, it should be moved as part
	// of a normal context interceptor that will handle all standard
	// start/stop actions

	// shut down and servlets
	Enumeration enum = ctx.getServletNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    Handler wrapper = ctx.getServletByName( key );
	    ctx.removeServletByName( key );
	    try {
		wrapper.destroy();
	    } catch(Exception ex ) {
		ctx.log( "Error in destroy ", ex);
	    }
	}

	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].contextShutdown( ctx );
	}
    }

    /** Will start the connectors and begin serving requests.
     *  It must be called after init.
     *  XXX Do nothing ?? Tomcat is started up by calling the init
     *  hooks, there is no need for special code here - in the worst
     *  case we should add a new hook
     */
    public void start() throws Exception {
    }

    /** Will stop all connectors
     */
    public void stop() throws Exception {
	shutdown();
    }

    // -------------------- Contexts --------------------
    /** Return the list of contexts managed by this server
     */
    public Enumeration getContexts() {
	return contextsV.elements();
    }

    /**
     * Adds a new Context to the set managed by this ContextManager.
     *
     * @param ctx context to be added.
     */
    public void addContext( Context ctx ) throws TomcatException {
	log("Adding context " +  ctx.toString());

	// Make sure context knows about its manager.
	ctx.setContextManager( this );

	// If the context already exist - the interceptors need
	// to deal with that ( either replace or throw an exception ).

	// The mapping alghoritm may use more than path and host -
	// if not now, then in future.

	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addContext( this, ctx );
	}

	String vhost=ctx.getHost();

	// XXX temporary workaround for the old SimpleMapper -
	// This code will be removed as soon as the new mapper is stable.
	if( vhost ==null ) // the old mapper will support only "default" server
	    contexts.put( ctx.getPath(), ctx );
	contextsV.addElement( ctx );
    }

    /** Shut down and removes a context from service
     */
    public void removeContext( Context context ) throws TomcatException {
	if( context==null ) return;

	log( "Removing context " + context.toString());
	
	BaseInterceptor cI[]=getContextInterceptors(context);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeContext( this, context );
	}

	shutdownContext( context );
	contextsV.removeElement(context);
	contexts.remove(context.getPath());
    }

    public void doReload( Request req, Context context )
	throws TomcatException
    {
	if( context==null ) return;

	if( debug>0 ) log( "Reloading context " + context.toString());

	BaseInterceptor cI[]=getContextInterceptors(context);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].reload(  req, context );
	}
    }


    /** Notify interceptors that a new container was added.
     */
    public void addContainer( Container container )
    	throws TomcatException
    {
	BaseInterceptor cI[]=getContextInterceptors(container);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addContainer( container);
	}
    }

    /** Notify interceptors that a container was removed.
     */
    public void removeContainer( Container container )
	throws TomcatException
    {
	BaseInterceptor cI[]=getContextInterceptors(container);
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeContainer( container);
	}
    }

    // -------------------- Interceptors --------------------
    // The interceptors are handled per/container ( thanks to Nacho
    // for this contribution ). We just delegate to the right
    // container ( in future we should remove this, and use the
    // right objects )
    
    public void addRequestInterceptor( BaseInterceptor ri ) {
        defaultContainer.addRequestInterceptor(ri);
    }

    /** Return all the interceptors associated with a request.
	That includes global ( context manager ) interceptors,
	webapp ( Context ) interceptors and possibly interceptors
	associated with containers ( urls inside the web app ).

	For performance reasons we use arrays and cache the result inside
	containers.

	XXX Todo: 
	Dynamic add of interceptors is not supported.
    */
    public BaseInterceptor[] getRequestInterceptors( Request req ) {
        Context ctx=req.getContext();
        // if Bad request (ctx == null) only global interceptors are called
        if( ctx == null )
           return getRequestInterceptors();
        Container ct=ctx.getContainer();
        BaseInterceptor[] ari=ct.getCachedRequestInterceptors();

	return ari;
    }

    public BaseInterceptor[] getRequestInterceptorszz( Request req ,
						       int hook_id)
    {
        Context ctx=req.getContext();
        // if Bad request (ctx == null) only global interceptors are called
        if( ctx == null )
           return defaultContainer.getInterceptors(hook_id);
        Container ct=ctx.getContainer();
        BaseInterceptor[] ari=ct.getInterceptors(hook_id);

	return ari;
    }

    public BaseInterceptor[] getRequestInterceptors() {
	return defaultContainer.getRequestInterceptors();
    }

    public void addContextInterceptor( BaseInterceptor ci) {
        defaultContainer.addContextInterceptor(ci);
    }

    public BaseInterceptor[] getContextInterceptors() {
	return defaultContainer.getContextInterceptors();
    }

    public BaseInterceptor[] getContextInterceptors(Container ct) {
        BaseInterceptor[] aci=ct.getCachedContextInterceptors();
	return aci;
    }

    public BaseInterceptor[] getContextInterceptors(Context ctx) {
        return getContextInterceptors(ctx.getContainer());
    }
    // -------------------- Request processing / subRequest ------------------
    // -------------------- Main request processing methods ------------------

    /** Prepare the req/resp pair for use in tomcat.
     *  Call it after you create the request/response objects
     */
    public void initRequest( Request req, Response resp ) {
	// used to be done in service(), but there is no need to do it
	// every time.
	// We may add other special calls here.
	// XXX Maybe make it a callback?
	resp.setRequest( req );
	req.setResponse( resp );
	req.setContextManager( this );
	resp.init();
    }

    /** This is the entry point in tomcat - the connectors ( or any other
     *  component able to generate Request/Response implementations ) will
     *  call this method to get it processed.
     *  XXX make sure the alghoritm is right, deal with response codes
     */
    public void service( Request req, Response res ) {
	internalService( req, res );
	// clean up
	try {
	    res.finish();
	} catch( Throwable ex ) {
	    handleError( req, res, ex );
	}
	finally {
	    doPostRequest(req, res);
	    req.recycle();
	    res.recycle();
	}
	return;
    }

    // Request processing steps and behavior
    private void internalService( Request req, Response res ) {
	try {
	    /* assert req/res are set up
	       corectly - have cm, and one-one relation
	    */
	    // wront request - parsing error
	    int status=res.getStatus();

	    if( status >= 400 ) {
		if( debug > 0)
		    log( "Error reading request " + req + " " + status);
		handleStatus( req, res, status ); 
		return;
	    }

	    status= processRequest( req );
	    if( status != 0 ) {
		if( debug > 0)
		    log("Error mapping the request " + req + " " + status);
		handleStatus( req, res, status );
		return;
	    }

	    if( req.getWrapper() == null ) {
		status=404;
		if( debug > 0)
		    log("No handler for request " + req + " " + status);
		handleStatus( req, res, status );
		return;
	    }

	    String roles[]=req.getRequiredRoles();
	    if(roles != null )
		status=doAuthorize( req, res, roles );
	    if( status > 200 ) {
		if( debug > 0)
		    log("Authorize error " + req + " " + status);
		handleStatus( req, res, status );
		return;
	    }

	    req.getWrapper().service(req, res);

	} catch (Throwable t) {
	    handleError( req, res, t );
	}
    }

    static int contextMap_ID=Container.getHookId( "contextMap");
    static int requestMap_ID=Container.getHookId( "requestMap");
    static int authorize_ID=Container.getHookId( "authorize");
    static int authenticate_ID=Container.getHookId( "authenticate");


    /** Will find the Handler for a servlet, assuming we already have
     *  the Context. This is also used by Dispatcher and getResource -
     *  where the Context is already known.
     */
    public int processRequest( Request req ) {
	if(debug>9) log("Before processRequest(): "+req.toString());

	int status=0;
        BaseInterceptor ri[];
	ri=defaultContainer.getInterceptors(contextMap_ID);
	
	for( int i=0; i< ri.length; i++ ) {
	    status=ri[i].contextMap( req );
	    if( status!=0 ) return status;
	}

	ri=defaultContainer.getInterceptors( requestMap_ID);
	for( int i=0; i< ri.length; i++ ) {
	    status=ri[i].requestMap( req );
	    if( status!=0 ) return status;
	}

	if(debug>9) log("After processRequest(): "+req.toString());

	return 0;
    }

    /** Call all authentication callbacks. If any of them is able to
	identify the user it will set the principal in req.
     */
    public int doAuthenticate( Request req, Response res ) {
	int status=0;
//	RequestInterceptor reqI[]= getRequestInterceptors(req);
	BaseInterceptor reqI[]= req.getContext().getContainer().getInterceptors(authenticate_ID);
	for( int i=0; i< reqI.length; i++ ) {
	    status=reqI[i].authenticate( req, res );
	    if ( status != 0 ) {
		if( debug>0) log( "Authenticate status " + status );
		return status;
	    }
	}
	return 0;
    }

    /** Call all authorization callbacks. The "require valid user" attributes
	are probably set during the mapping stage ( for efficiency), but it
	can be done here too.
     */
    public int doAuthorize( Request req, Response res, String roles[] ) {
	int status=0;
//	RequestInterceptor reqI[]= getRequestInterceptors(req);
	BaseInterceptor reqI[]= req.getContext().getContainer().getInterceptors(authorize_ID);

	for( int i=0; i< reqI.length; i++ ) {
	    status = reqI[i].authorize( req, res, roles );
	    if ( status != 0 ) {
		if( debug>0) log( "Authorize status " + status );
		return status;
	    }
	}
	return 0;
    }

    /** Call beforeBody callbacks. Before body allows you do do various
	actions before the first byte of the response is sent. After all
	those callbacks are called tomcat may send the status and headers
    */
    int doBeforeBody( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].beforeBody( req, res );
	}
	return 0;
    }

    /** Call beforeCommit callbacks. This allows interceptors to manipulate the
	buffer before it gets sent.
	XXX Add a standard way to access the body. The method was not used too
	much, we need a review and maybe change in parameters.
    */
    int doBeforeCommit( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].beforeCommit( req, res );
	}
	return 0;
    }

    /** Implement the write logic, calling the interceptors
     * and making sure the headers are sent before. This used to
     *	be part of BufferedServletOutputStream, but it's better
     *	to have it here for all output streams.
     */
    public void doWrite(Request req, Response res, byte buf[], int off, int cnt )
	throws IOException
    {
	if (!res.isBufferCommitted()) {
	    res.endHeaders();
	}
	if( cnt>0 ) {
	    doBeforeCommit( req, res );

	    res.doWrite( buf, off, cnt );
	}
    }
    
    int doPreService( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].preService( req, res );
	}
	return 0;
    }

    int doPostService( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].postService( req, res );
	}
	return 0;
    }

    int doPostRequest( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].postRequest( req, res );
	}
	return 0;
    }

    int doNewSessionRequest( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].newSessionRequest( req, res );
	}
	return 0;
    }

    /** Call afterBody callbacks. It is called after the servlet finished
	sending the response ( either closeing the stream or ending ). You
	can deal with connection reuse or do other actions
    */
    int doAfterBody( Request req, Response res ) {
	BaseInterceptor reqI[]= getRequestInterceptors(req);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].afterBody( req, res );
	}
	return 0;
    }

    // -------------------- Sub-Request mechanism --------------------

    /** Create a new sub-request in a given context, set the context "hint"
     *  This is a particular case of sub-request that can't get out of
     *  a context ( and we know the context before - so no need to compute it
     *  again)
     *
     *  Note that session and all stuff will still be computed.
     */
    public Request createRequest( Context ctx, String urlPath ) {
	// assert urlPath!=null

	// deal with paths with parameters in it
	String contextPath=ctx.getPath();
	String origPath=urlPath;

	// append context path
	if( !"".equals(contextPath) && !"/".equals(contextPath)) {
	    if( urlPath.startsWith("/" ) )
		urlPath=contextPath + urlPath;
	    else
		urlPath=contextPath + "/" + urlPath;
	} else {
	    // root context
	    if( !urlPath.startsWith("/" ) )
		urlPath= "/" + urlPath;
	}

	if( debug >4 ) log("createRequest " + origPath + " " + urlPath  );
	Request req= createRequest( urlPath );
	String host=ctx.getHost();
	if( host != null) req.setServerName( host );
	return req;
    }

    /** Create a new sub-request, deal with query string
     */
    public Request createRequest( String urlPath ) {
	String queryString=null;
	int i = urlPath.indexOf("?");
	int len=urlPath.length();
	if (i>-1) {
	    if(i<len)
		queryString =urlPath.substring(i + 1, urlPath.length());
	    urlPath = urlPath.substring(0, i);
	}

	Request lr = new Request();
	lr.setContextManager( this );
	lr.setRequestURI( urlPath );
	lr.setQueryString( queryString );
	return lr;
    }

    // -------------------- Error handling --------------------

    /** Called for error-codes
     */
    public void handleStatus( Request req, Response res, int code ) {
	String errorPath=null;
	Handler errorServlet=null;

	if( code==0 )
	    code=res.getStatus();
	else
	    res.setStatus(code);

	Context ctx = req.getContext();
	if(ctx==null) ctx=getContext("");

	// don't log normal cases ( redirect and need_auth ), they are not
	// error
	// XXX this log was intended to debug the status code generation.
	// it can be removed for all cases.
	if( code != 302 && code != 401 && code!=400  ) // tuneme
	    ctx.log( "Status code:" + code + " request:"  + req + " msg:" +
		     req.getAttribute("javax.servlet.error.message"));
	
	errorPath = ctx.getErrorPage( code );
	if( errorPath != null ) {
	    errorServlet=getHandlerForPath( ctx, errorPath );

	    // Make sure Jsps will work
	    req.setAttribute( "javax.servlet.include.request_uri",
				  ctx.getPath()  + "/" + errorPath );
	    req.setAttribute( "javax.servlet.include.servlet_path", errorPath );
	}

	boolean isDefaultHandler = false;
	if( errorServlet==null ) {
	    errorServlet=ctx.getServletByName( "tomcat.statusHandler");
	    isDefaultHandler = true;
	}

	if (errorServlet == null) {
	    ctx.log( "Handler errorServlet is null! errorPath:" + errorPath);
	    return;
	}

	if (!isDefaultHandler)
	    res.resetBuffer();

	req.setAttribute("javax.servlet.error.status_code",new Integer( code));
	req.setAttribute("tomcat.servlet.error.request", req);

	if( debug>0 )
	    ctx.log( "Handler " + errorServlet + " " + errorPath);

	errorServlet.service( req, res );
    }

    // XXX XXX Security - we should log the message, but nothing
    // should show up  to the user - it gives up information
    // about the internal system !
    // Developers can/should use the logs !!!

    /** General error handling mechanism. It will try to find an error handler
     * or use the default handler.
     */
    void handleError( Request req, Response res , Throwable t  ) {
	Context ctx = req.getContext();
	if(ctx==null) {
	    ctx=getContext("");
	}

	/** The exception must be available to the user.
	    Note that it is _WRONG_ to send the trace back to
	    the client. AFAIK the trace is the _best_ debugger.
	*/
	if( t instanceof IllegalStateException ) {
	    ctx.log("IllegalStateException in " + req, t);
	    // Nothing special in jasper exception treatement, no deps
	    //} else if( t instanceof org.apache.jasper.JasperException ) {
	    // 	    ctx.log("JasperException in " + req, t);
	} else if( t instanceof IOException ) {
            if( "Broken pipe".equals(t.getMessage()))
	    {
		ctx.log("Broken pipe in " + req, t, Logger.DEBUG);  // tuneme
		return;
	    }
	    ctx.log("IOException in " + req, t );
	} else {
	    ctx.log("Exception in " + req , t );
	}

	if(null!=req.getAttribute("tomcat.servlet.error.defaultHandler")){
	    // we are in handleRequest for the "default" error handler
	    log("ERROR: can't find default error handler, or error in default error page", t);
	}

	String errorPath=null;
	Handler errorServlet=null;

	// Scan the exception's inheritance tree looking for a rule
	// that this type of exception should be forwarded
	Class clazz = t.getClass();
	while (errorPath == null && clazz != null) {
	    String name = clazz.getName();
	    errorPath = ctx.getErrorPage(name);
	    clazz = clazz.getSuperclass();
	}

	if( errorPath != null ) {
	    errorServlet=getHandlerForPath( ctx, errorPath );

	    // Make sure Jsps will work
	    req.setAttribute( "javax.servlet.include.request_uri",
				  ctx.getPath()  + "/" + errorPath );
	    req.setAttribute( "javax.servlet.include.servlet_path", errorPath );
	}

	boolean isDefaultHandler = false;
	if( errorLoop( ctx, req ) || errorServlet==null) {
	    errorServlet = ctx.getServletByName("tomcat.exceptionHandler");
	    isDefaultHandler = true;
	}

	if (errorServlet == null) {
	    ctx.log( "Handler errorServlet is null! errorPath:" + errorPath);
	    return;
	}

	if (!isDefaultHandler)
	    res.resetBuffer();

	req.setAttribute("javax.servlet.error.exception_type", t.getClass());
	req.setAttribute("javax.servlet.error.message", t.getMessage());
	req.setAttribute("tomcat.servlet.error.throwable", t);
	req.setAttribute("tomcat.servlet.error.request", req);

	if( debug>0 )
	    ctx.log( "Handler " + errorServlet + " " + errorPath);

	errorServlet.service( req, res );
    }

    public Handler getHandlerForPath( Context ctx, String path ) {
	if( ! path.startsWith( "/" ) ) {
	    return ctx.getServletByName( path );
	}
	Request req1=new Request();
	Response res1=new Response();
	initRequest( req1, res1 );

	req1.setRequestURI( ctx.getPath() + path );
	processRequest( req1 );
	return req1.getWrapper();
    }

    /** Handle the case of error handler generating an error or special status
     */
    private boolean errorLoop( Context ctx, Request req ) {
	if( req.getAttribute("javax.servlet.error.status_code") != null
	    || req.getAttribute("javax.servlet.error.exception_type")!=null) {

	    if( ctx.getDebug() > 0 )
		ctx.log( "Error: exception inside exception servlet " +
			 req.getAttribute("javax.servlet.error.status_code") +
			 " " + req.
			 getAttribute("javax.servlet.error.exception_type"));

	    return true;
	}
	return false;
    }


    // -------------------- Support for notes --------------------

    /** Note id counters. Synchronized access is not necesarily needed
     *  ( the initialization is in one thread ), but anyway we do it
     */
    public static final int NOTE_COUNT=5;
    private  int noteId[]=new int[NOTE_COUNT];

    /** Maximum number of notes supported
     */
    public static final int MAX_NOTES=32;
    public static final int RESERVED=3;

    public static final int SERVER_NOTE=0;
    public static final int CONTAINER_NOTE=1;
    public static final int REQUEST_NOTE=2;
    public static final int HANDLER_NOTE=3;
    
    public static final int REQ_RE_NOTE=0;

    String noteName[][]=new String[NOTE_COUNT][MAX_NOTES];

    /** used to allow interceptors to set specific per/request, per/container
     * and per/CM informations.
     *
     * This will allow us to remove all "specialized" methods in
     * Request and Container/Context, without losing the functionality.
     * Remember - Interceptors are not supposed to have internal state
     * and minimal configuration, all setup is part of the "core", under
     *  central control.
     *  We use indexed notes instead of attributes for performance -
     * this is internal to tomcat and most of the time in critical path
     */

    /** Create a new note id. Interceptors will get an Id at init time for
     *  all notes that it needs.
     *
     *  Throws exception if too many notes are set ( shouldn't happen in
     *  normal use ).
     *  @param noteType The note will be associated with the server,
     *   container or request.
     *  @param name the name of the note.
     */
    public synchronized int getNoteId( int noteType, String name )
	throws TomcatException
    {
	// find if we already have a note with this name
	// ( this is in init(), not critical )
	for( int i=0; i< noteId[noteType] ; i++ ) {
	    if( name.equals( noteName[noteType][i] ) )
		return i;
	}

	if( noteId[noteType] >= MAX_NOTES )
	    throw new TomcatException( "Too many notes ");

	// make sure the note id is > RESERVED
	if( noteId[noteType] < RESERVED ) noteId[noteType]=RESERVED;

	noteName[noteType][ noteId[noteType] ]=name;
	return noteId[noteType]++;
    }

    public String getNoteName( int noteType, int noteId ) {
	return noteName[noteType][noteId];
    }

    // -------------------- Per-server notes --------------------
    Object notes[]=new Object[MAX_NOTES];

    public void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public Object getNote( int pos ) {
	return notes[pos];
    }

    // -------------------- Logging and debug --------------------
    boolean firstLog = true;
    Log loghelper = new Log("tc_log", "ContextManager");

    /**
     * Get the Logger object that the context manager is writing to (necessary?)
     **/
    public Logger getLogger() {
	return loghelper.getLogger();
    }

    /**
     * So other classes can piggyback on the context manager's log
     * stream, using Logger.Helper.setProxy()
     **/
    public Log getLog() {
	return loghelper;
    }
 
    /**
     * Force this object to use the given Logger.
     **/
    public void setLogger( Logger logger ) {
	log("!!!! setLogger: " + logger, Logger.DEBUG);
	loghelper.setLogger(logger);
    }

    public void addLogger(Logger l) {
	if (debug>20)
	    log("addLogger: " + l, new Throwable("trace"), Logger.DEBUG);
	// Will use this later once I feel more sure what I want to do here.
	// -akv
	// firstLog=false;
	//	if("tc_log".equals( logger.getName()) cmLog=logger;
	String path=l.getPath();
	if( path!=null ) {
	    File f=new File( path );
	    if( ! f.isAbsolute() ) {
		// Make it relative to home !
		File wd= getAbsolute( f );
		l.setPath( wd.getAbsolutePath() );
	    }
	    // create the files, ready to log.
	}
	l.open();
    }

    public void setDebug( int level ) {
	if( level != debug )
	    log( "Setting debug level to " + level);
	debug=level;
    }

    public int getDebug() {
	return debug;
    }

    public final void log(String msg) {
	loghelper.log(msg);
    }

    public final void log(String msg, Throwable t) {
	loghelper.log(msg, t);
    }

    public final void log(String msg, int level) {
	loghelper.log(msg, level);
    }

    public final void log(String msg, Throwable t, int level) {
	loghelper.log(msg, t, level);
    }

    // -------------------- Accounting --------------------
    // XXX Can be implemented as note !
    public static final int ACC_INIT_START=0;
    public static final int ACC_INIT_END=0;
    public static final int ACCOUNTS=7;

    Counters cntr=new Counters(ACCOUNTS);

    public Counters getCounters() {
	return cntr;
    }
    
    // -------------------- DEPRECATED --------------------
    // XXX host and port are used only to construct a unique
    // work-dir for contexts, using them and the path
    // Since nobody sets them - I guess we can just drop them
    // anyway.
    // XXX ask and find if there is any other use!
    public static final String DEFAULT_HOSTNAME="localhost";
    public static final int DEFAULT_PORT=8080;
    String hostname;
    int port;

    /**
     * Sets the port number on which this server listens.
     *
     * @param port The new port number
     * @deprecated
     */
    public void setPort(int port) {
	if (debug>0) log("setPort", new Throwable("trace"));
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
	if (debug>0)  log("setHostName", new Throwable("trace"));
	this.hostname=host;
    }

    /**
     * Gets the virtual host name of this server.
     * @deprecated
     */
    public String getHostName() {
	// if (debug>0)  log("getHostName", new Throwable("trace"));
	if(hostname==null)
	    hostname=DEFAULT_HOSTNAME;
	return hostname;
    }
    // -------------------- DEPRECATED --------------------

    /**
     * The set of Contexts associated with this ContextManager,
     * keyed by context paths.
     * @deprecated - the server shouldn't make any assumptions about
     *  the key.
     */
    private Hashtable contexts = new Hashtable();

    /**
     * Get the names of all the contexts in this server.
     * @deprecated Path is not "unique key".
     */
    public Enumeration getContextNames() {
	loghelper.log("getContextNames is deprecated", new Throwable("trace"), Logger.DEBUG);
        return contexts.keys();
    }

    /**
     * Gets a context by it's name, or <code>null</code> if there is
     * no such context.
     *
     * @param name Name of the requested context
     * @deprecated Use an external iterator to find the context that
     *  matches your conditions.
     *
     */
    public Context getContext(String name) {
	//	loghelper.log("getContext(String) is deprecated", new Throwable("trace"), Logger.DEBUG);
	return (Context)contexts.get(name);
    }

    /**
     * Shut down and removes a context from service.
     *
     * @param name Name of the Context to be removed
     * @deprecated Use removeContext( Context ).
     */
    public void removeContext(String name) throws TomcatException {
	loghelper.log("removeContext(String) is deprecated", new Throwable("trace"), Logger.DEBUG);
	Context context = (Context)contexts.get(name);
	removeContext( context);
    }

    public void doPreServletInit(Context ctx, Handler sw)
	throws TomcatException
    {
	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    try {
		cI[i].preServletInit( ctx, sw );
	    } catch( TomcatException ex) {
		log("preServletInit" , ex);
	    }
	}
    }

    public void doPostServletInit(Context ctx, Handler sw)
	throws TomcatException
    {
	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    try {
		cI[i].postServletInit( ctx, sw );
	    } catch( TomcatException ex) {
		log("postServletInit", ex);
	    }
	}
    }

    public void doPreServletDestroy(Context ctx, Handler sw)
	throws TomcatException
    {
	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    try {
		cI[i].preServletDestroy( ctx, sw );
	    } catch( TomcatException ex) {
		log("preServletDestroy", ex);
	    }
	}
    }

    public void doPostServletDestroy(Context ctx, Handler sw)
	throws TomcatException
    {
	BaseInterceptor cI[]=getContextInterceptors(ctx);
	for( int i=0; i< cI.length; i++ ) {
	    try {
		cI[i].postServletDestroy( ctx, sw );
	    } catch( TomcatException ex) {
		log("postServletDestroy", ex);
	    }
	}
    }

    /** @deprecated
     */
    public void setTomcatHome( String s ) {
	log ("setTomcatHome(String) is deprecated", new Throwable("trace"), Logger.DEBUG);
	setInstallDir( s );
    }

    /** @deprecated
     */
    public String getTomcatHome() {
	log ("getTomcatHome() is deprecated", new Throwable("trace"), Logger.DEBUG);
	return getInstallDir();
    }

    /** Convert a relative name to absolute by using the "home" property
     */
    public File getAbsolute(File f) {
        if (!f.isAbsolute()) {
            // evaluate repository path relative to the context's home
	    // directory
	    return new File(getHome(), f.getPath());
        }
        return f;
    }

    public Container getContainer() {
        return defaultContainer;
    }

    public void setContainer(Container newDefaultContainer) {
        defaultContainer = newDefaultContainer;
    }


    // Do we need that ?
    
    //     /** Map the context facade back to the context.
    // 	This call doesn't involve any security checks, and
    // 	can be used by internal components (  they
    // 	already have access to ContextManager ).
    // 	Servlets need to call getAttribute(), and that
    // 	will do the required security checks
    //     */
    //     public Context getRealContext( Object facade ) {
    // 	return null;
    //     }


}
