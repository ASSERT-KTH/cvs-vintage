package org.apache.tomcat.startup;

import java.net.*;
import java.io.*;

import org.apache.tomcat.core.*;
import org.apache.tomcat.request.*;
import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.modules.session.*;
import org.apache.tomcat.context.*;
import org.apache.tomcat.util.log.*;
import java.security.*;
import java.util.*;

// XXX XXX This started as a hack to integrate with J2EE,
// need a major rewrite

/**
 *  Use this class to embed tomcat in your application.
 *  The order is important:
 *  1. set properties like workDir and debug
 *  2. add all interceptors including your application-specific
 *  3. add the endpoints 
 *  4. add at least the root context ( you can add more if you want )
 *  5. call start(). The web service will be operational.
 *  6. You can add/remove contexts
 *  7. stop().
 *  
 *  You can add more contexts after start, but interceptors and  
 *  endpoints must be set before the first context and root must be
 *  set before start().
 *
 *  All file paths _must_ be absolute. ( right now if the path is relative it
 *  will be made absolute using tomcat.home as base. This behavior is very
 *  "expensive" as code complexity and will be deprecated ).
 * 
 * @author costin@eng.sun.com
 */
public class EmbededTomcat { 
    ContextManager contextM = new ContextManager();
    Object application;

    // null == not set up
    Vector requestInt=null;
    Vector connectors=new Vector();

    String workDir;

    Log loghelper = new Log("tc_log", this);
    
    // configurable properties
    int debug=0;
    
    public EmbededTomcat() {
    }

    // -------------------- Properties - set before start

    public ContextManager getContextManager() {
	return contextM;
    }
    
    /** Set debugging - must be called before anything else
     */
    public void setDebug( int debug ) {
	this.debug=debug;
    }

    /** This is an adapter object that provides callbacks into the
     *  application.
     *  For tomcat, it will be a BaseInterceptor.
     * 	See the top level documentation
     */
    public void addApplicationAdapter( Object adapter )
	throws TomcatException
    {
	if(requestInt==null)  initDefaultInterceptors();

	// In our case the adapter must be BaseInterceptor.
	if ( adapter instanceof BaseInterceptor ) {
	    addInterceptor( (BaseInterceptor)adapter);
	}
    }

    public void setApplication( Object app ) {
	application=app;
    }

    /** Keep a reference to the application in which we are embeded
     */
    public Object getApplication() {
	return application;
    }
    
    public void setWorkDir( String dir ) {
	workDir=dir;
    }
    
    // -------------------- Endpoints --------------------
    
    /** Add a HTTP listener.
     *  You must add all the endpoints before calling start().
     */
    public void addEndpoint( int port, InetAddress addr , String hostname)
	throws TomcatException
    {
	if(debug>0) log( "addConnector " + port + " " + addr +
			 " " + hostname );

	Http10Interceptor sc=new Http10Interceptor();
	sc.setServer( contextM );
	sc.setDebug( debug );
	sc.setPort( port ) ;
	if( addr != null ) sc.setAddress( addr );
	if( hostname != null ) sc.setHostName( hostname );
	
	contextM.addInterceptor(  sc );
    }

    /** Add a secure HTTP listener.
     */
    public void addSecureEndpoint( int port, InetAddress addr, String hostname,
				    String keyFile, String keyPass )
	throws TomcatException
    {
	if(debug>0) log( "addSecureConnector " + port + " " + addr + " " +
			 hostname );
	
	Http10Interceptor sc=new Http10Interceptor();
	sc.setServer( contextM );
	sc.setPort( port ) ;
	if( addr != null ) sc.setAddress(  addr );
	if( hostname != null ) sc.setHostName( hostname );
	
	sc.setSocketFactory("org.apache.tomcat.util.net.SSLSocketFactory");
	sc.setSecure(true);

	contextM.addInterceptor(  sc );
    }

    // -------------------- Context add/remove --------------------

    boolean initialized=false;
    
    /** Add and init a context
     */
    public Context addContext( String ctxPath, URL docRoot )
	throws TomcatException
    {
	if(debug>0) log( "add context \"" + ctxPath + "\" " + docRoot );
	if( ! initialized ) {
	    initContextManager();
	}
	
	// tomcat supports only file-based contexts
	if( ! "file".equals( docRoot.getProtocol()) ) {
	    log( "addContext() invalid docRoot: " + docRoot );
	    throw new RuntimeException("Invalid docRoot " + docRoot );
	}

	try {
	    Context ctx=new Context();
	    ctx.setDebug( debug );
	    ctx.setContextManager( contextM );
	    ctx.setPath( ctxPath );
	    // XXX if virtual host set it.
	    ctx.setDocBase( docRoot.getFile());
	    contextM.addContext( ctx );
	    // 	    if( facadeM == null ) facadeM=ctx.getFacadeManager();
	    // 	    return ctx.getFacade();
	    return ctx;
	} catch( Exception ex ) {
	    log("exception adding context " + ctxPath + "/" + docRoot, ex);
	}
	return null;
    }

    /** Find the context mounted at /cpath.
	Right now virtual hosts are not supported in
	embeded tomcat.
    */
    public Object getServletContext( String host,
				     String cpath )
    {
	// We don't support virtual hosts in embeded tomcat
	// ( it's not difficult, but can be done later )
	Enumeration ctxE=contextM.getContexts();
	while( ctxE.hasMoreElements() ) {
	    Context ctx=(Context)ctxE.nextElement();
	    // XXX check host too !
	    if( ctx.getPath().equals( cpath ))
		return ctx.getFacade();
	}
	return null;
    }

    // -------------------- Private methods
    public void addInterceptor( BaseInterceptor ri ) {
	if( requestInt == null ) requestInt=new Vector();
	requestInt.addElement( ri );
	if( ri instanceof BaseInterceptor )
	    ((BaseInterceptor)ri).setDebug( debug );
    }

    private void initContextManager()
	throws TomcatException 
    {
	if(requestInt==null)  initDefaultInterceptors();
	contextM.setDebug( debug );
	
	for( int i=0; i< requestInt.size() ; i++ ) {
	    contextM.addInterceptor( (BaseInterceptor)
				     requestInt.elementAt( i ) );
	}

	contextM.setWorkDir( workDir );

	try {
	    contextM.init();
	} catch( Exception ex ) {
	    log("exception initializing ContextManager", ex);
	}
	if(debug>0) log( "ContextManager initialized" );
	initialized=true;
    }
    
    private void initDefaultInterceptors() {
	// Explicitely set up all the interceptors we need.
	// The order is important ( like in apache hooks, it's a chain !)
	
	// no AutoSetup !
	
	// set workdir, engine header, auth Servlet, error servlet, loader

	// XXX So far Embeded tomcat is specific to Servlet 2.2.
	// It need a major refactoring to support multiple
	// interfaces ( I'm not sure it'll be possible to support
	// multiple APIs at the same time in embeded mode )

	//	addInterceptor( new LogEvents() );
	
	DefaultCMSetter defaultCMI=new DefaultCMSetter();
	addInterceptor( defaultCMI );

	BaseInterceptor webXmlI=
	    (BaseInterceptor)newObject("org.apache.tomcat.facade.WebXmlReader");
	addInterceptor( webXmlI );

	PolicyInterceptor polI=new PolicyInterceptor();
	addInterceptor( polI );
	polI.setDebug(0);
        
	LoaderInterceptor12 loadI=new LoaderInterceptor12();
	addInterceptor( loadI );

	ErrorHandler errH=new ErrorHandler();
	addInterceptor( errH );

	WorkDirInterceptor wdI=new WorkDirInterceptor();
	addInterceptor( wdI );

	// Debug
	// 	LogEvents logEventsI=new LogEvents();
	// 	addRequestInterceptor( logEventsI );

	SessionId sessI=new SessionId();
	addInterceptor( sessI );

	SimpleMapper1 mapI=new SimpleMapper1();
	addInterceptor( mapI );

	InvokerInterceptor invI=new InvokerInterceptor();
	addInterceptor( invI );
	
	BaseInterceptor jspI=(BaseInterceptor)newObject("org.apache.tomcat.facade.JspInterceptor");
	addInterceptor( jspI );

	StaticInterceptor staticI=new StaticInterceptor();
	addInterceptor( staticI );

	addInterceptor( new SimpleSessionStore());
	
	BaseInterceptor loadOnSI= (BaseInterceptor)newObject("org.apache.tomcat.facade.LoadOnStartupInterceptor");
	addInterceptor( loadOnSI );

	BaseInterceptor s22=(BaseInterceptor)newObject("org.apache.tomcat.facade.Servlet22Interceptor");
	addInterceptor( s22 );

	// access control ( find if a resource have constraints )
	AccessInterceptor accessI=new AccessInterceptor();
	addInterceptor( accessI );
	accessI.setDebug(0);

	// set context class loader
	Jdk12Interceptor jdk12I=new Jdk12Interceptor();
	addInterceptor( jdk12I );

    }
    

    // -------------------- Utils --------------------
    public void log( String s ) {
	loghelper.log( s );
    }
    public void log( String s, Throwable t ) {
	loghelper.log( s, t );
    }
    public void log( String s, int level ) {
	loghelper.log( s, level );
    }
    public void log( String s, Throwable t, int level ) {
	loghelper.log( s, t, level );
    }

    /** Sample - you can use it to tomcat
     */
    public static void main( String args[] ) {
	try {
	    File pwdF=new File(".");
	    String pwd=pwdF.getCanonicalPath();

	    EmbededTomcat tc=new EmbededTomcat();
	    tc.setWorkDir( pwd + "/work"); // relative to pwd

	    Context sctx=tc.addContext("", new URL
				       ( "file", null, pwd + "/webapps/ROOT"));
	    sctx.init();

	    sctx=tc.addContext("/examples", new URL
		("file", null, pwd + "/webapps/examples"));
	    sctx.init();

	    tc.addEndpoint( 8080, null, null);
	    tc.getContextManager().start();
	} catch (Throwable t ) {
	    // this stack trace is ok, i guess, since it's just a
	    // sample main
	    t.printStackTrace();
	}
    }

    private Object newObject( String classN ) {
	try {
	    Class c=Class.forName( classN );
	    return c.newInstance();
	} catch( Exception ex ) {
	    ex.printStackTrace();
	    return null;
	}
    }
	

}
