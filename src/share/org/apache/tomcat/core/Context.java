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

import org.apache.tomcat.util.http.MimeMap;
import org.apache.tomcat.util.log.Log;

import java.io.File;
import java.net.FileNameMap;
import java.net.URL;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;


/**
 * Context represent a Web Application as specified by Servlet Specs.
 * The implementation is a repository for all the properties
 * defined in web.xml and tomcat specific properties.
 * 
 * This object has many properties, but doesn't do anything special
 * except simple cashing.
 *
 * You need to set at least "path" and "base" before adding a
 * context to a server. You can also set any other properties.
 *
 * At addContext() stage log and paths will be "fixed" based on
 * context manager settings.
 *
 * At initContext() stage, web.xml will be read and all other
 * properties will be set. WebXmlReader must be the first
 * module in initContext() chain. 
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@dnt.ro
 * @author Gal Shachor shachor@il.ibm.com
 */
public class Context {
    // -------------------- Constants --------------------
    
    // Proprietary attribute names for contexts - defined
    // here so we can document them ( will show in javadoc )

    /** Private tomcat attribute names
     */
    public static final String ATTRIB_PREFIX="org.apache.tomcat";

    /** Protection domain to be used to create new classes in this context.
	This is used only by JspServlet, and should be avoided -
	the preferred mechanism is to use the default policy file
	and URLClassLoader.
    */
    public static final String ATTRIB_PROTECTION_DOMAIN=
	"org.apache.tomcat.protection_domain";
    
    /** This attribute will return the real context (
     *  org.apache.tomcat.core.Context).
     *  Only "trusted" applications will get the value. Null if the application
     * 	is not trusted.
     */
    public static final String ATTRIB_REAL_CONTEXT="org.apache.tomcat.context";

    /** Context is new, possibly not even added to server.
	ContextManager is not set, and most of the paths are not fixed
    */
    public static final int STATE_NEW=0;

    /** Context was added to the server, but contextInit() is not
	called. Paths are not set yet, the only valid information is
	the contextURI.
     */
    public static final int STATE_ADDED=1;
    
    /**
       Relative paths are fixed, based
       on server base, and CM is set.
       If a request arives for this context, an error message should be
       displayed ( "application is temporary disabled" )
     */
    public static final int STATE_DISABLED=2;

    /** Context is initialized and ready to serve. We have all mappings
	and configs from web.xml.
    */
    public static final int STATE_READY=3;
    
    // -------------------- internal properties
    private String name;
    
    // context "id"
    private String path = "";

    // directory where the context files are located.
    private String docBase;

    // Absolute path to docBase if file-system based
    private String absPath;
    private Hashtable properties=new Hashtable();
    
    private int state=STATE_NEW;
    
    // internal state / related objects
    private ContextManager contextM;
    private Object contextFacade;
    // print debugging information
    private int debug=0;

    // enable reloading
    private boolean reloadable=true; 

    // XXX Use a better repository
    private Hashtable attributes = new Hashtable();

    // directory with write-permissions for servlets
    private File workDir;

    // Servlets loaded by this context( String->ServletWrapper )
    private Hashtable servlets = new Hashtable();

    // Initial properties for the context
    private Hashtable initializationParameters = new Hashtable();

    // WelcomeFiles
    private Vector welcomeFilesV=new Vector();
    // cached for faster access
    private String welcomeFiles[] = null;

    // Defined error pages. 
    private Hashtable errorPages = new Hashtable();

    // mime mappings
    private MimeMap mimeTypes = new MimeMap();

    // Default session time out
    private int sessionTimeOut = -1;

    private boolean isDistributable = false;

    // Maps specified in web.xml ( String url -> Handler  )
    private Hashtable mappings = new Hashtable();

    // Security constraints ( String url -> Container )
    private Hashtable constraints=new Hashtable();

    // All url patterns ( url_pattern -> properties )
    private Hashtable containers=new Hashtable();

    // Container used if no match is found
    // Also contains the special properties for
    // this context. 
    private Container defaultContainer = null;

    // Authentication properties
    private String authMethod;
    private String realmName;
    private String formLoginPage;
    private String formErrorPage;

    // Servlet-Engine header ( default set by Servlet facade)
    private String engineHeader = null;

    // Virtual host name ( null if default )
    private String vhost=null;
    // Virtual host ip address (if vhost isn't an address)
    private String vhostip=null;
    // vhost aliases 
    private Vector vhostAliases=new Vector();

    // are servlets allowed to access internal objects? 
    private boolean trusted=false;

    private static Log defaultContextLog=Log.getLog("org/apache/tomcat/core", "Context");
    // log channels for context and servlets 
    private Log loghelper = defaultContextLog;
    private Log loghelperServlet;

    // servlet API implemented by this Context
    private String apiLevel="2.2";

    // class loader for this context
    private ClassLoader classLoader;
    // Vector<URL>, using URLClassLoader conventions
    private Vector classPath=new Vector();

    // true if a change was detected and this context
    // needs reload
    private boolean reload;

    // -------------------- from web.xml --------------------
    // Those properties are not directly used in context
    // operation, we just store them.
    private String description = null;
    private String icon=null;
    // taglibs
    private Hashtable tagLibs=new Hashtable();
    // Env entries
    private Hashtable envEntryTypes=new Hashtable();
    private Hashtable envEntryValues=new Hashtable();

    private int attributeInfo;
    // -------------------- Constructor --------------------
    
    public Context() {
	defaultContainer=new Container();
	defaultContainer.setContext( this );
	defaultContainer.setPath( null ); // default container
    }


    // -------------------- Active methods --------------------

    // The main role of Context is to store the many properties
    // that a web application have.

    // There are only few methods here that actually do something
    // ( and we try to keep the object "passive" - it is already
    // full of properties, no need to make it to complicated.

    /** Add a new container. Container define special properties for
	a set of urls.
    */
    public void addContainer( Container ct )
	throws TomcatException
    {
	// Notify interceptors that a new container is added
	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addContainer( ct );
	}
    }
    
    
    /**
     * Maps a named servlet to a particular path or extension.
     *
     * If the named servlet is unregistered, it will be added
     * and subsequently mapped. The servlet can be set by intereceptors
     * during addContainer() hook.
     *
     * If the mapping already exists it will be replaced by the new
     * mapping.
     * @deprecated Use addContainer
     */
    public void addServletMapping(String path, String servletName)
	throws TomcatException
    {
	if( mappings.get( path )!= null) {
	    log( "Removing duplicate " + path + " -> " + mappings.get(path) );
	    mappings.remove( path );
	    Container ct=(Container)containers.get( path );
	    removeContainer( ct );
	}

	// sw may be null - in wich case interceptors may
	// set it 
        Handler sw = getServletByName(servletName);
	
	Container map=contextM.createContainer();
	map.setContext( this );
	map.setHandlerName( servletName );
	map.setHandler( sw );
	map.setPath( path );

	// Notify interceptors that a new container is added
	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addContainer( map );
	}

	sw = getServletByName(servletName);
	
	
	if (sw == null) {
	    // web.xml validation - a mapping with no servlet rollback
	    removeContainer( map );
 	    throw new TomcatException( "Mapping with invalid servlet  " +
				       path + " " + servletName );
	}

	containers.put( path, map );
	mappings.put( path, sw );
	if( debug > 4 )
	    log( "Map " + path + " -> " + mappings.get(path));
    }

    /** Will add a new security constraint:
	For all paths:
	if( match(path) && match(method) && match( transport ) )
	then require("roles")

	This is equivalent with adding a Container with the path,
	method and transport. If the container will be matched,
	the request will have to pass the security constraints.
	@deprecated Use addContainer
    */
    public void addSecurityConstraint( String path[], String methods[],
				       String roles[], String transport)
	throws TomcatException
    {
	for( int i=0; i< path.length; i++ ) {
	    Container ct=contextM.createContainer();
	    ct.setContext( this );
	    ct.setTransport( transport );
	    ct.setRoles( roles );
	    ct.setPath( path[i] );
	    ct.setMethods( methods );

	    // XXX check if exists, merge if true.
	    constraints.put( path[i], ct );
	    //contextM.addSecurityConstraint( this, path[i], ct);

	    // Notify interceptors that a new container is added
	    BaseInterceptor cI[]=ct.getInterceptors();
	    for( int j=0; j< cI.length; j++ ) {
		cI[j].addContainer( ct );
	    }
	}
    }

    /** Check if "special" attributes can be used by
     *   user application. Only trusted apps can get 
     *   access to the implementation object.
     */
    public boolean allowAttribute( String name ) {
	// check if we can access this attribute.
	if( isTrusted() ) return true;
	log( "Attempt to access internal attribute in untrusted app",
	     null, Log.ERROR);
	return false;
    }

    // -------------------- Passive properties --------------------
    // Everything bellow is just get/set
    // for web application properties 
    // --------------------
    
    // -------------------- Facade --------------------
    
    /** Every context is associated with a facade. We don't know the exact
	type of the facade, as a Context can be associated with a 2.2 ...
	ServletContext.
     */
    public Object getFacade() {
	return contextFacade;
    }

    public void setFacade(Object obj) {
        if(contextFacade!=null ) {
	    log( "Changing facade " + contextFacade + " " +obj);
	}
	contextFacade=obj;
    }


    // -------------------- Settable context properties --------------------

    /** Returned the main server ( servlet container )
     */
    public final  ContextManager getContextManager() {
	return contextM;
    }

    /** This method is called when the Context is added
	to a server. Some of the Context properties
	depend on the ContextManager, and will be adjusted
	by interceptors ( DefaultCMSetter )
    */
    public void setContextManager(ContextManager cm) {
	if( contextM != null ) return;
	contextM=cm;
	if( defaultContainer==null ) {
	    defaultContainer=contextM.createContainer();
	    defaultContainer.setContext( this );
	    defaultContainer.setPath( null ); // default container
	}
	try {
	    attributeInfo=cm.getNoteId(ContextManager.REQUEST_NOTE,
				       "req.attribute");
	} catch( TomcatException ex ) {
	    ex.printStackTrace();
	}
    }

    protected void setContextManager1(ContextManager cm) {
	contextM=cm;
    }
	    
    
    /** Default container for this context.
     */
    public Container getContainer() {
	return defaultContainer;
    }

    public final int getState() {
	return state;
    }

    /** Move the context in a different state.
	Can be called only from tomcat.core.ContextManager.
	( package access )
    */
    public void setState( int state )
	throws TomcatException
    {
	// call state callback
	BaseInterceptor csI[]=getContainer().getInterceptors();
	for( int i=0; i< csI.length; i++ ) {
	    csI[i].contextState( this, state ); 
	}

	// transition from NEW to ADDED. The system is stable, we
	// can init our own local modules
	if(this.state==STATE_NEW && state==STATE_ADDED ) {
	    // we are just beeing added
	    BaseInterceptor cI[]=getContainer().getInterceptors();
	    for( int i=0; i< cI.length; i++ ) {
		if( cI[i].getContext() != this )
		    continue; // not ours, don't have to initialize it.
		try {
		    cI[i].addInterceptor( contextM, this , cI[i] ); 
		    BaseInterceptor existingI[]=defaultContainer.
			getInterceptors();
		    for( int j=0; j<existingI.length; j++ ) {
			if( existingI[j] != cI[i] )
			    existingI[j].addInterceptor( contextM,
							 this, cI[i] );
		    }
		    
		    // set all local interceptors 
		    cI[i].setContextManager( contextM );
		    cI[i].engineInit( contextM );
		    cI[i].addContext( contextM, this );
		} catch( TomcatException ex ) {
		    log("Error adding module " + cI[i] + " to " + this , ex);
		}
	    }
	}
	this.state=state;
    }

    protected void setState1( int state ) {
	this.state=state;
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
     *
     * After this call, the context will be in READY state and will
     * be able to server requests.
     * 
     * @exception if any interceptor throws an exception the error
     *   will prevent the context from becoming READY
     */
    public void init() throws TomcatException {
	if( state==STATE_READY ) {
	    log( "Already initialized " );
	    return;
	}

	// make sure we see all interceptors added so far
	getContainer().resetInterceptorCache(Container.H_engineInit);

	// no action if ContextManager is not initialized
	if( contextM==null ||
	    contextM.getState() == ContextManager.STATE_NEW ) {
	    log( "ERROR: ContextManager is not yet initialized ");
	    return;
	}

	BaseInterceptor cI[]=getContainer().getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].contextInit( this );
	}
	
	// Only if all init methods succeed an no ex is thrown
	setState( Context.STATE_READY );
    }


    /** Stop the context. After the call the context will be disabled,
	( DISABLED state ) and it'll not be able to serve requests.
	The context will still be available and can be enabled later
	by calling initContext(). Requests mapped to this context
	should report a "temporary unavailable" message.
	
	
	All servlets will be destroyed, and resources held by the
	context will be freed.

	The contextShutdown callbacks can wait until the running serlvets
	are completed - there is no way to force the shutdown.
     */
    public void shutdown() throws TomcatException {
	setState( Context.STATE_DISABLED ); // called before
	// the hook, no more request should be allowed in unstable state

	BaseInterceptor cI[]= getContainer().getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].contextShutdown( this );
	}
    }


    
    // -------------------- Basic properties --------------------
    /** Return a name ( id ) for this context. Currently it's composed
	from the virtual host and path, or set explicitely by the app.
    */
    public String getName() {
	if(name!=null ) return name;
	name=(vhost==null ? "DEFAULT:" : vhost + ":" )  +
	    ("".equals(path)  ? "/ROOT" :  path);
	return name;
    }

    public void setName( String s ) {
	name=s;
    }
    
    /** Base URL for this context
     */
    public String getPath() {
	return path;
    }

    /** Base URL for this context
     */
    public void setPath(String path) {
	// config believes that the root path is called "/",
	//
	if( "/".equals(path) )
	    path="";
	this.path = path;
	loghelper=Log.getLog("org/apache/tomcat/core",
			     "Ctx("+ getId() +") ");
	name=null;
    }

    /**
     *  Make this context visible as part of a virtual host.
     *  The host is the "default" name, it may also have aliases.
     */
    public void setHost( String h ) {
	vhost=h;
	name=null;
    }

    /**
     * Return the virtual host name, or null if we are in the
     * default context
     */
    public String getHost() {
	return vhost;
    }

    /**
     * Set the virtual host ip address.
     */
    public final void setHostAddress( String ip ) {
        vhostip=ip;
    }

    /**
     * Return the virtual host ip address.
     */
    public final String getHostAddress() {
        return vhostip;
    }
    
    /** DocBase points to the web application files.
     *
     *  There is no restriction on the syntax and content of DocBase,
     *  it's up to the various modules to interpret this and use it.
     *  For example, to serve from a war file you can use war: protocol,
     *  and set up War interceptors.
     *
     *  "Basic" tomcat treats it as a file ( either absolute or relative to
     *  the CM home ).
     */
    public void setDocBase( String docB ) {
	this.docBase=docB;
    }

    public String getDocBase() {
	return docBase;
    }

    /** Return the absolute path for the docBase, if we are file-system
     *  based, null otherwise.
    */
    public String getAbsolutePath() {
	return absPath;
    }

    /** Set the absolute path to this context.
     * 	If not set explicitely, it'll be docBase ( if absolute )
     *  or relative to "home" ( cm.getHome() ).
     *  DefaultCMSetter will "fix" the path.
     */
    public void setAbsolutePath(String s) {
	absPath=s;
    }

    public String getProperty( String n ) {
	return (String)properties.get( n );
    }

    public void setProperty( String n, String v ) {
	properties.put( n, v );
    }
    
    // -------------------- Tomcat specific properties --------------------
    
    public void setReloadable( boolean b ) {
	reloadable=b;
    }

    /** Should we reload servlets ?
     */
    public boolean getReloadable() {
	return reloadable;
    }

    // -------------------- API level --------------------
    
    /** The servlet API variant that will be used for requests in this
     *  context
     */ 
    public void setServletAPI( String s ) {
	if( s==null ) return;
	if( s.endsWith("23") || s.endsWith("2.3")) {
	    apiLevel="2.3";
	} else if( ( s.endsWith("22") || s.endsWith("2.2")) ) {
	    apiLevel="2.2";
	} else {
	    log( "Unknown API " + s );
	}
    }

    public String getServletAPI() {
	return apiLevel;
    }
    
    // -------------------- Welcome files --------------------

    /** Return welcome files defined in web.xml or the
     *  defaults, if user doesn't define any
     */
    public String[] getWelcomeFiles() {
	if( welcomeFiles==null ) {
	    welcomeFiles=new String[ welcomeFilesV.size() ];
	    for( int i=0; i< welcomeFiles.length; i++ ) {
		welcomeFiles[i]=(String)welcomeFilesV.elementAt( i );
	    }
	}
	return welcomeFiles;
    }

    /** Add an welcome file. 
     */
    public void addWelcomeFile( String s) {
	if (s == null ) return;
	s=s.trim();
	if(s.length() == 0)
	    return;
	welcomeFiles=null; // invalidate the cache
	welcomeFilesV.addElement( s );
    }

    // -------------------- Init parameters --------------------
    
    public String getInitParameter(String name) {
        return (String)initializationParameters.get(name);
    }

    public void addInitParameter( String name, String value ) {
	initializationParameters.put(name, value );
    }

    public Enumeration getInitParameterNames() {
        return initializationParameters.keys();
    }


    // --------------------  Attributes --------------------

    /** Return an attribute value.
     *  "Special" attributes ( defined org.apache.tomcat )
     *  are computed
     * 
     */
    public Object getAttribute(String name) {
	Object o=attributes.get( name );
	if( o!=null ) return o;
	if(name.equals(ATTRIB_REAL_CONTEXT)) {
	    if( ! allowAttribute(name) ) {
		return null;
	    }
	    return this;
	}
	// interceptors may return special attributes
	BaseInterceptor reqI[]= this.getContainer().
	    getInterceptors(Container.H_getInfo);
	for( int i=0; i< reqI.length; i++ ) {
	    o=reqI[i].getInfo( this, null, attributeInfo, name );
	    if ( o != null ) {
		break;
	    }
	}

	return o;    
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    /**
     *  XXX Use callbacks !!
     */
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }


    // -------------------- Web.xml properties --------------------

    /** Add a taglib declaration for this context
     */
    public void addTaglib( String uri, String location ) {
	tagLibs.put( uri, location );
    }

    public String getTaglibLocation( String uri ) {
	return (String)tagLibs.get(uri );
    }

    public Enumeration getTaglibs() {
	return tagLibs.keys();
    }

    /** Add Env-entry to this context
     */
    public void addEnvEntry( String name,String type, String value, String description ) {
	log("Add env-entry " + name + "  " + type + " " + value + " " +description );
	if( name==null || type==null) throw new IllegalArgumentException();
	envEntryTypes.put( name, type );
	if( value!=null)
	    envEntryValues.put( name, value );
    }

    public String getEnvEntryType(String name) {
	return (String)envEntryTypes.get(name);
    }

    public String getEnvEntryValue(String name) {
	return (String)envEntryValues.get(name);
    }

    public Enumeration getEnvEntries() {
	return envEntryTypes.keys();
    }

    
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon( String icon ) {
	this.icon=icon;
    }

    public boolean isDistributable() {
        return this.isDistributable;
    }

    public void setDistributable(boolean isDistributable) {
        this.isDistributable = isDistributable;
    }

    public int getSessionTimeOut() {
        return this.sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    // -------------------- Mime types --------------------

    public FileNameMap getMimeMap() {
        return mimeTypes;
    }

    public void addContentType( String ext, String type) {
	mimeTypes.addContentType( ext, type );
    }
    
    // -------------------- Error pages --------------------

    public String getErrorPage(int errorCode) {
        return getErrorPage(String.valueOf(errorCode));
    }

    public void addErrorPage( String errorType, String value ) {
	this.errorPages.put( errorType, value );
    }

    public String getErrorPage(String errorCode) {
        return (String)errorPages.get(errorCode);
    }


    // -------------------- Auth --------------------
    
    /** Authentication method, if any specified
     */
    public String getAuthMethod() {
	return authMethod;
    }

    /** Realm to be used
     */
    public String getRealmName() {
	return realmName;
    }

    public String getFormLoginPage() {
	return formLoginPage;
    }

    public String getFormErrorPage() {
	return formErrorPage;
    }

    public void setFormLoginPage( String page ) {
	formLoginPage=page;
    }
    
    public void setFormErrorPage( String page ) {
	formErrorPage=page;
    }

    public void setLoginConfig( String authMethod, String realmName,
				String formLoginPage, String formErrorPage)
    {
	this.authMethod=authMethod;
	this.realmName=realmName;
	this.formLoginPage=formLoginPage;
	this.formErrorPage=formErrorPage;
    }

    // -------------------- Mappings --------------------

    public Enumeration getContainers() {
	return containers.elements();
    }

    /** Return an enumeration of Strings, representing
     *  all URLs ( relative to this context ) having
     *	associated properties ( handlers, security, etc)
     */
    public Enumeration getContainerLocations() {
	return containers.keys();
    }

    /** Return the container ( properties ) associated
     *  with a path ( relative to this context )
     */
    public Container getContainer( String path ) {
	return (Container)containers.get(path);
    }

    /** Remove a container
     */
    public void removeContainer( Container ct )
	throws TomcatException
    {
	containers.remove(ct.getPath());

	// notify modules that a container was removed
	BaseInterceptor cI[]=ct.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeContainer( ct );
	}
    }

    // -------------------- Servlets management --------------------
    /**
     * Add a servlet. Servlets are mapped by name.
     * This method is used to maintain the list of declared
     * servlets, that can be used for mappings.
     * @deprecated. Use addHandler() 
     */
    public void addServlet(Handler wrapper)
    	throws TomcatException
    {
	addHandler( wrapper );
    }

    /**
     * Add a servlet. Servlets are mapped by name.
     * This method is used to maintain the list of declared
     * servlets, that can be used for mappings.
     */
    public void addHandler(Handler wrapper)
    	throws TomcatException
    {
	//	wrapper.setContext( this );
	wrapper.setState( Handler.STATE_ADDED );
	String name=wrapper.getName();
	wrapper.setContextManager( contextM );

        // check for duplicates
        if (getServletByName(name) != null) {
	    log("Removing duplicate servlet " + name  + " " + wrapper);
            removeServletByName(name);
        }
	if( debug>5 ) log( "Adding servlet=" + name + "-> "
			   + wrapper);
	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].addHandler( wrapper );
	}
	
	servlets.put(name, wrapper);
    }

    public void removeHandler( Handler handler )
	throws TomcatException
    {
	if( handler==null ) return;
	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeHandler( handler );
	}

	servlets.remove( handler.getName());
	handler.setState( Handler.STATE_NEW );
    }

    /** Remove the servlet with a specific name
     */
    public void removeServletByName(String servletName)
	throws TomcatException
    {
	Handler h=getServletByName( servletName );
	removeHandler( h );
    }

    /**
     *  
     */
    public Handler getServletByName(String servletName) {
	return (Handler)servlets.get(servletName);
    }


    
    /** Return all servlets registered with this Context
     *  The elements will be of type Handler ( or sub-types ) 
     */
    public Enumeration getServletNames() {
	return servlets.keys();
    }

    // -------------------- Loading and sessions --------------------

    /** The current class loader. This value may change if reload
     *  is used, you shouldn't cache the result
     */
    public ClassLoader getClassLoader() {
	return classLoader;
    }

    public void setClassLoader(ClassLoader cl ) {
	classLoader=cl;
    }

    // -------------------- ClassPath --------------------
    
    public void addClassPath( URL url ) {
	classPath.addElement( url);
    }

    /** Returns the full classpath - concatenation
	of ContextManager classpath and locally specified
	class path
    */
    public URL[] getClassPath() {
	if( classPath==null ) return new URL[0];
	URL serverCP[]=new URL[0]; //contextM.getServerClassPath();
	URL urls[]=new URL[classPath.size() + serverCP.length];
	int pos=0;
	for( int i=0; i<serverCP.length; i++ ) {
	    urls[pos++]=serverCP[i];
	}
	for( int i=0; i<classPath.size(); i++ ) {
	    urls[pos++]=(URL)classPath.elementAt( i );
	}
	return urls;
    }

    /* -------------------- Utils  -------------------- */
    public void setDebug( int level ) {
	if (level!=debug)
	    log( "Setting debug to " + level );
	debug=level;
    }

    public int getDebug( ) {
	return debug;
    }

    public String toString() {
	return getName();
    }

    // ------------------- Logging ---------------

    public String getId() {
	return ((vhost==null) ? "" : vhost + ":" )  +  path;
    }
    
    /** Internal log method
     */
    public void log(String msg) {
	loghelper.log(msg);
    }

    /** Internal log method
     */
    public void log(String msg, Throwable t) {
	loghelper.log(msg, t);
    }

    /** Internal log method
     */
    public void log(String msg, Throwable t, int level) {
	loghelper.log(msg, t, level);
    }

    /** User-level log method ( called from a servlet).
     *  Context supports 2 log streams - one is used by the
     *  tomcat core ( internals ) and one is used by 
     *  servlets
     */
    public void logServlet( String msg , Throwable t ) {
	if (loghelperServlet == null) {
	    loghelperServlet = loghelper;
	}
	if (t == null)
	    loghelperServlet.log(msg);	// uses level INFORMATION
	else
	    loghelperServlet.log(msg, t); // uses level ERROR
    }

    public void setLog(Log logger) {
	loghelper=logger;
    }

    public void setServletLog(Log logger) {
	loghelperServlet=logger;
    }

    public Log getLog() {
	return loghelper;
    }

    public Log getServletLog() {
	return loghelperServlet;
    }

    // -------------------- Path methods  --------------------

    /**  What is reported in the "Servlet-Engine" header
     *   for this context. It is set automatically by
     *   a facade interceptor.
     *   XXX Do we want to allow user to customize it ?
     */
    public void setEngineHeader(String s) {
        engineHeader=s;
    }

    /**  
     */
    public String getEngineHeader() {
	return engineHeader;
    }

    // -------------------- Work dir --------------------
    
    /**
     *  Work dir is a place where servlets are allowed
     *  to write
     */
    public void setWorkDir(String workDir) {
	this.workDir = new File(workDir);
    }

    /**  
     */
    public File getWorkDir() {
	return workDir;
    }

    /**  
     */
    public void setWorkDir(File workDir) {
	this.workDir = workDir;
    }

    // -------------------- Virtual host support --------------------
    
    /** Virtual host support - this context will be part of 
     *  a virtual host with the specified name. You should
     *  set all the aliases. XXX Not implemented
     */
    public void addHostAlias( String alias ) {
	vhostAliases.addElement( alias );
    }

    public Enumeration getHostAliases() {
	return vhostAliases.elements();
    }
    // -------------------- Security - trusted code -------------------- 

    /** Mark the webapplication as trusted, i.e. it can
     *  access internal objects and manipulate tomcat core
     */
    public void setTrusted( boolean t ) {
	trusted=t;
    }

    public boolean isTrusted() {
	return trusted;
    }

    // -------------------- Per-context interceptors ----------

    /** Add a per-context interceptor. The hooks defined will
     *  be used only for requests that are matched in this context.
     *  contextMap hook is not called ( since the context is not
     *	known at that time.
     *  
     *  This method will only store the interceptor. No action
     *  takes place before the context is added ( since contextM
     *  may be unknown ).
     */
    public void addInterceptor( BaseInterceptor ri )
	throws TomcatException
    {
	ri.setContext( this );
	defaultContainer.addInterceptor(ri);

	if( getState() == STATE_NEW ) return;

	// This shouldn't happen in most cases - the "normal"
	// case is to construct a Context, add the local modules
	// and then add it to a server. Later, when the server
	// is initialized it'll init the contexts and that will
	// init local modules.

	// The following code is not tested - it deals with the
	// case that a module is added at runtime. Even if this
	// is not a 'normal' case we should handle it. 
	
	// we are at least ADDED - that means the CM is initialized
	// if we can find the global modules and announce our presence
	ri.addInterceptor( contextM, this , ri ); 
	BaseInterceptor existingI[]=defaultContainer.getInterceptors();
	for( int i=0; i<existingI.length; i++ ) {
	    if( existingI[i] != ri )
		existingI[i].addInterceptor( contextM, this, ri );
	    // contextM  can be null
	}
	
	ri.setContextManager( contextM );

	ri.engineInit( contextM );
	ri.addContext( contextM, this ); // it'll not know about other
	// contexts - in future we may add a mechanism to let it know
	// about the hierarchy.

	if( getState() == STATE_ADDED ) return;

	// we are initialized
	ri.contextInit( this );

	if( contextM.getState() == ContextManager.STATE_START ) {
	    ri.engineStart(contextM );
	}
    }

}
