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
  ContextManager is the entry point and "controler" of the servlet execution.
  It maintains a list of WebApplications and a list of global event
  interceptors that are set up to handle the actual execution.
 
  The ContextManager will direct the request processing flow
  from its arrival from the server/protocl adapter ( in service() ).
  It will do that by calling a number of hooks implemented by Interceptors.
 
  Hooks are provided for request parsing and mapping, auth, autorization,
  pre/post service, actual invocation and logging.
 
  ContextManager will also store properties that are global to the servlet
  container - like root directory, install dir, work dir.
 
  The extension mechanism for tomcat is the Interceptor.
  This class is final - if you need to change certain functionality
  you should add a new hook.
 
  ContextManager is not a singleton - it represent a servlet container
  instance ( with all associated ports and configurations ).
  One application may try to embed multiple ( distinct ) servlet containers -
  this feature hasn't been tested or used
 
 
   Expected startup order:

  1. Create ContextManager

  2. Set settable properties for ContextManager ( home, debug, etc)

  3. Add global Interceptors

  4. You may create, set and add Contexts. NO HOOKS ARE CALLED.
  
  5. Call init(). At this stage engineInit() callback will be
     called for all global interceptors.
     - DefaultCMSetter ( or a replacement ) must be the first in
     the chain and will adjust the paths and set defaults for
     all unset properties.
     - AutoSetup and other interceptors can automatically add/set
     more properties and make other calls.

     During engineInit() a number of Contexts are created and
     added to the server. No addContext() callback is called until
     the last engineInit() returns. ( XXX do we need this restriction ?)

  XXX I'n not sure about contextInit and about anything below.

  x. Server will move to INITIALIZED state. No callback other than engineInit
     can be called before the server enters this state.

  x. addContext() callbacks will be called for each context.
     After init you may add more contexts, and addContext() callback
     will be called (since the server is initialized )

  x. Call start().

  x. All contexts will be initialized ( contextInit()
     callback ).

  x. Server will move to STARTED state. No servlet should be 
     served before this state.

     
     During normal operation, it is possible to add  Contexts.

  1. Create the Context, set properties ( that can be done from servlets
     or by interceptors like ~user)

  2. call CM.addContext(). This will triger the addContext() callback.
     ( if CM is initialized )

  3. call CM.initContext( ctx ). This will triger contextInit() callback.
     After that the context is initialized and can serve requests.
     No request belonging to this context can't be served before this
     method returns.

     XXX Context state
     
     It is also possible to remove Contexts.

  1. Find the Context ( enumerate all existing contexts and find the one
    you need - host and path are most likely keys ).

  2. Call removeContext(). This will call removeContext() callbacks.

  
     To stop the server, you need to:

  1. Call shutdown().

  2. The server will ...

 
  @author James Duncan Davidson [duncan@eng.sun.com]
  @author James Todd [gonzo@eng.sun.com]
  @author Harish Prabandham
  @author costin@eng.sun.com
  @author Hans Bergsten [hans@gefionsoftware.com]
 */
public final class ContextManager implements LogAware{
    /** Official name and version
     */
    public static final String TOMCAT_VERSION = "3.3 dev";
    public static final String TOMCAT_NAME = "Tomcat Web Server";
    
    /** Property used to set the random number generator
     */
    public static final String RANDOM_CLASS_PROPERTY=
	"tomcat.sessionid.randomclass";

    /** Property used to set the base directory ( tomcat home )
     */
    public static final String TOMCAT_HOME=
	"tomcat.home";

    /** Default work dir, relative to home
     */
    public static final String DEFAULT_WORK_DIR="work";

    // Accounting
    /** time when init() started
     */
    public static final int ACC_INIT_START=0;
    /** Time when init() finished()
     */
    public static final int ACC_INIT_END=1;
    public static final int ACCOUNTS=2;

    // State
    /** Server is not initialized
     */
    public static final int STATE_PRE_INIT=0;
    /** Server was initialized, engineInit() was called.
	addContext() can be called.
     */
    public static final int STATE_INIT=1;
    /** Engine is running. All configured contexts are
	initialized ( contextInit()), and requests can be served.
     */
    public static final int STATE_START=2;
    /** Engine has stoped
     */
    public static final int STATE_STOP=3;
    
    // -------------------- local variables --------------------

    private int state=STATE_PRE_INIT;
    
    /** Contexts managed by this server
     */
    private Vector contextsV=new Vector();

    private int debug=0;

    // Global properties for this tomcat instance:

    /** Private workspace for this server
     */
    private String workDir;

    /** The base directory where this instance runs.
     *  It can be different from the install directory to
     *  allow one install per system and multiple users
     */
    private String home;

    /** The directory where tomcat is installed
     */
    private String installDir;

    // "/" on the default host 
    private Context rootContext;
    
    // Server properties ( interceptors, etc ) - it's one level above "/"
    private Container defaultContainer;

    // the application loader. ContextManager is loaded with
    // a class loader containing tomcat-specific classes,
    // use parent loader to avoid polution
    private ClassLoader parentLoader;
    // tomcat classes ( used to load tomcat)
    private URL serverClassPath[];
    
    private Counters cntr=new Counters(ACCOUNTS);

    /**
     * Construct a new ContextManager instance with default values.
     */
    public ContextManager() {
        defaultContainer=new Container();
        defaultContainer.setContext( null );
	defaultContainer.setContextManager( this );
        defaultContainer.setPath( null ); // default container
    }

    // -------------------- Server functions --------------------

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
    public final void init()  throws TomcatException {
	if(debug>0 ) log( "Tomcat classpath = " +
			     System.getProperty( "java.class.path" ));

	cntr.touchCounter( ACC_INIT_START );

	BaseInterceptor cI[]=getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineInit( this );
	}

	state=STATE_INIT;

	Enumeration existingCtxE=contextsV.elements();
	while( existingCtxE.hasMoreElements() ) {
	    Context ctx=(Context)existingCtxE.nextElement();
	    
	    cI=getInterceptors(ctx.getContainer());
	    for( int i=0; i< cI.length; i++ ) {
		cI[i].addContext( this, ctx );
	    }
	}

	cntr.touchCounter( ACC_INIT_END);
    }

    /** Remove all contexts.
     *  Call Intereptor.engineShutdown hooks.
     */
    public final void shutdown() throws TomcatException {
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    removeContext((Context)enum.nextElement());
	}

	BaseInterceptor cI[]=getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineShutdown( this );
	}
    }

    /** Stop the context and release all resources.
     */
    public final void shutdownContext( Context ctx ) throws TomcatException {
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

	BaseInterceptor cI[]=getInterceptors(ctx.getContainer());
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
    public final void start() throws Exception {
    	// init contexts
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    Context ctx = (Context)enum.nextElement();
	    try {
		BaseInterceptor cI[]=getInterceptors(ctx.getContainer());
		for( int i=0; i< cI.length; i++ ) {
		    cI[i].contextInit( ctx );
		}
	    } catch (TomcatException ex ) {
		if( ctx!=null ) {
		    log( "ERROR initializing " + ctx.toString(), ex );
		    removeContext( ctx  );
		}
	    }
	}
	// Note that contextInit() will triger other 
	
	state=STATE_START;
    }

    /** Will stop all connectors
     */
    public final void stop() throws Exception {
	shutdown();
    }

    // -------------------- setable properties --------------------

    /**
     *  The home of the tomcat instance - you can have multiple
     *  users running tomcat, with a shared install directory.
     *  
     *  Home is used as a base for logs, webapps, local config.
     *  Install dir ( if different ) is used to find lib ( jar
     *   files ).
     *
     *  The "tomcat.home" system property is used if no explicit
     *  value is set.
     */
    public final void setHome(String home) {
	this.home=home;
    }

    public final String getHome() {
	return home;
    }

    /**
     *  Get installation directory. This is used to locate
     *  jar files ( lib ). If tomcat instance is shared,
     *  home is used for webapps, logs, config.
     *  If either home or install is not set, the other
     *  is used as default.
     * 
     */
    public final String getInstallDir() {
	return installDir;
    }

    public final void setInstallDir( String tH ) {
	installDir=tH;
    }

    /**
     * WorkDir property - where all working files will be created
     */
    public final void setWorkDir( String wd ) {
	if(debug>0) log("set work dir " + wd);
	this.workDir=wd;
    }

    public final String getWorkDir() {
	return workDir;
    }

    /** Sets the name of the class used for generating random numbers by the
     *  session id generator. By default this is <code>java.security.SecureRandom</code>.
     */
    public final void setRandomClass(String randomClass) {
        System.setProperty(RANDOM_CLASS_PROPERTY, randomClass);
    }

    public final String getRandomClass() {
        String randomClass = System.getProperty(RANDOM_CLASS_PROPERTY);
        return randomClass == null ? "java.security.SecureRandom" : randomClass;
    }

    /** Debug level
     */
    public final void setDebug( int level ) {
	if( level != debug )
	    log( "Setting debug level to " + level);
	debug=level;
    }

    public final int getDebug() {
	return debug;
    }

    // -------------------- Other properties --------------------

    public final int getState() {
	return state;
    }
    
    /**
     *  Parent loader is the "base" class loader of the
     *	application that starts tomcat, and includes no
     *	tomcat classes. All servlet loaders will have it as
     *  a parent loader, as if the webapps would be loaded
     *  by the embeding app ( using parentLoader ).
     *
     *  Tomcat will add servlet.jar and any other extension
     *  it is configured to - for example trusted webapps
     *  may have tomcat internal classes in classpath. 
     */
    public final void setParentLoader( ClassLoader cl ) {
	parentLoader=cl;
    }

    public final ClassLoader getParentLoader() {
	return parentLoader;
    }

    public final URL[] getServerClassPath() {
	if( serverClassPath==null ) return new URL[0];
	return serverClassPath;
    }
    
    public final void setServerClassPath( URL urls[] ) {
	serverClassPath=urls;
    }

    /** Default container
     */
    public final Container getContainer() {
        return defaultContainer;
    }

    public final void setContainer(Container newDefaultContainer) {
        defaultContainer = newDefaultContainer;
    }

    /** Accounting support - various counters associated with the server
     */
    public final Counters getCounters() {
	return cntr;
    }

    // -------------------- Contexts --------------------

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
    public final void initContext( Context ctx ) throws TomcatException {
	if( state!= STATE_PRE_INIT ) {
	    BaseInterceptor cI[]=getInterceptors(ctx.getContainer());
	    for( int i=0; i< cI.length; i++ ) {
		cI[i].contextInit( ctx );
	    }
	}
    }

    /** Return the list of contexts managed by this server
     */
    public final Enumeration getContexts() {
	return contextsV.elements();
    }

    /**
     * Adds a new Context to the set managed by this ContextManager.
     *
     * @param ctx context to be added.
     */
    public final void addContext( Context ctx ) throws TomcatException {
	log("Adding context " +  ctx.toString());
	// Make sure context knows about its manager.
	ctx.setContextManager( this );

	// If the context already exist - the interceptors need
	// to deal with that ( either replace or throw an exception ).

	contextsV.addElement( ctx );

	if( ctx.getHost() == null && ctx.getPath().equals(""))
	    rootContext = ctx;
	
	if( state == STATE_PRE_INIT )
	    return;
	
	BaseInterceptor cI[]=getInterceptors(ctx.getContainer());
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addContext( this, ctx );
	}
    }

    /** Shut down and removes a context from service
     */
    public final void removeContext( Context context ) throws TomcatException {
	if( context==null ) return;

	log( "Removing context " + context.toString());
	
	BaseInterceptor cI[]=getInterceptors(context.getContainer());
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeContext( this, context );
	}

	shutdownContext( context );
	contextsV.removeElement(context);
    }


    // -------------------- Interceptors --------------------
    // The interceptors are handled per/container ( thanks to Nacho
    // for this contribution ).
    
    public final void addInterceptor( BaseInterceptor ri ) {
        defaultContainer.addInterceptor(ri);
    }

    public final BaseInterceptor[] getInterceptors() {
	return defaultContainer.getInterceptors();
    }
    
    public final BaseInterceptor[] getInterceptors( Container ct ) {
	return ct.getInterceptors();
    }
    
    public final BaseInterceptor[] getInterceptors( Request req ,
					      int hook_id)
    {
        Context ctx=req.getContext();
        if( ctx == null )
           return defaultContainer.getInterceptors(hook_id);
        Container ct=ctx.getContainer();
        BaseInterceptor[] ari=ct.getInterceptors(hook_id);

	return ari;
    }

    // -------------------- Request processing / subRequest ------------------
    // -------------------- Main request processing methods ------------------

    /** Prepare the req/resp pair for use in tomcat.
     *  Call it after you create the request/response objects
     */
    public final void initRequest( Request req, Response resp ) {
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
    public final void service( Request req, Response res ) {
	internalService( req, res );
	// clean up
	try {
	    res.finish();
	} catch( Throwable ex ) {
	    handleError( req, res, ex );
	}
	finally {
	    BaseInterceptor reqI[]= getInterceptors(req,
						    Container.H_postRequest);

	    for( int i=0; i< reqI.length; i++ ) {
		reqI[i].postRequest( req, res );
	    }
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
	    if(roles != null ) {
		status=0;
		BaseInterceptor reqI[]= req.getContext().getContainer().
		    getInterceptors(Container.H_authorize);

		// Call all authorization callbacks. 
		for( int i=0; i< reqI.length; i++ ) {
		    status = reqI[i].authorize( req, res, roles );
		    if ( status != 0 ) {
			break;
		    }
		}
	    }
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

    /** Will find the Handler for a servlet, assuming we already have
     *  the Context. This is also used by Dispatcher and getResource -
     *  where the Context is already known.
     */
    public final int processRequest( Request req ) {
	if(debug>9) log("Before processRequest(): "+req.toString());

	int status=0;
        BaseInterceptor ri[];
	ri=defaultContainer.getInterceptors(Container.H_contextMap);
	
	for( int i=0; i< ri.length; i++ ) {
	    status=ri[i].contextMap( req );
	    if( status!=0 ) return status;
	}

	ri=defaultContainer.getInterceptors(Container.H_requestMap);
	for( int i=0; i< ri.length; i++ ) {
	    if( debug > 1 )
		log( "RequestMap " + ri[i] );
	    status=ri[i].requestMap( req );
	    if( status!=0 ) return status;
	}

	if(debug>9) log("After processRequest(): "+req.toString());

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
    public final Request createRequest( Context ctx, String urlPath ) {
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
    public final Request createRequest( String urlPath ) {
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

    /**
     *   Find a context by doing a sub-request and mapping the request
     *   against the active rules ( that means you could use a /~costin
     *   if a UserHomeInterceptor is present )
     *
     *   The new context will be in the same virtual host as base.
     *
     */
    public final  Context getContext(Context base, String path) {
	// XXX Servlet checks should be done in facade
	if (! path.startsWith("/")) {
	    return null; // according to spec, null is returned
	    // if we can't  return a servlet, so it's more probable
	    // servlets will check for null than IllegalArgument
	}
	// absolute path
	Request lr=this.createRequest( path );
	if( base.getHost() != null ) lr.setServerName( base.getHost() );
	this.processRequest(lr);
        return lr.getContext();
    }


    // -------------------- Error handling --------------------
    
    /** Called for error-codes
     */
    public final void handleStatus( Request req, Response res, int code ) {
	String errorPath=null;
	Handler errorServlet=null;

	if( code==0 )
	    code=res.getStatus();
	else
	    res.setStatus(code);

	Context ctx = req.getContext();
	if(ctx==null) ctx=rootContext;

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
	    ctx=rootContext;
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

    public final Handler getHandlerForPath( Context ctx, String path ) {
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

    private String noteName[][]=new String[NOTE_COUNT][MAX_NOTES];
    
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
    public final synchronized int getNoteId( int noteType, String name )
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

    public final String getNoteName( int noteType, int noteId ) {
	return noteName[noteType][noteId];
    }

    // -------------------- Per-server notes --------------------
    private Object notes[]=new Object[MAX_NOTES];
    
    public final void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public final Object getNote( int pos ) {
	return notes[pos];
    }

    // -------------------- Logging and debug --------------------
    private Log loghelper = new Log("tc_log", "ContextManager");

    /**
     * Get the Logger object that the context manager is writing to (necessary?)
     **/
    public final Logger getLogger() {
	return loghelper.getLogger();
    }

    /**
     * So other classes can piggyback on the context manager's log
     * stream, using Logger.Helper.setProxy()
     **/
    public final Log getLog() {
	return loghelper;
    }
 
    /**
     * Force this object to use the given Logger.
     **/
    public final void setLogger( Logger logger ) {
	log("!!!! setLogger: " + logger, Logger.DEBUG);
	loghelper.setLogger(logger);
    }

    public final void addLogger(Logger l) {
	if (debug>20)
	    log("addLogger: " + l, new Throwable("trace"), Logger.DEBUG);

	String path=l.getPath();
	if( path!=null ) {
	    File f=new File( path );
	    if( ! f.isAbsolute() ) {
		File wd= new File(getHome(), f.getPath());
		l.setPath( wd.getAbsolutePath() );
	    }
	    // create the files, ready to log.
	}
	l.open();
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

}
