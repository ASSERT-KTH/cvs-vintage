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

import org.apache.tomcat.server.*;
import org.apache.tomcat.context.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.*;

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//

/**
 * Context represent a Web Application as specified by Servlet Specs.
 * The implementation is a repository for all the properties
 * defined in web.xml and tomcat specific properties, with all the
 * functionality delegated to interceptors.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@dnt.ro
 */
public class Context {
    private static StringManager sm =StringManager.getManager("org.apache.tomcat.core");

    // -------------------- internal properties
    // context "id"
    private String path = "";
    private String docBase;

    // internal state / related objects
    private boolean initialized = false;
    private ContextManager contextM;
    private ServletContextFacade contextFacade;
    private SessionManager sessionManager;
    private ServletWrapper defaultServlet = null;

    private URL documentBase;
    
    // 
    private Hashtable attributes = new Hashtable();

    // work dir
    private File workDir;
    private boolean isWorkDirPersistent = false;

    // tomcat specific properties
    private String engineHeader = null;
    private URL servletBase = null;
    private boolean isInvokerEnabled = false;
    boolean reloadable=true; // XXX change default to false after testing
    
    // for serving WARs directly 
    private File warDir = null;
    private boolean isWARExpanded = false;
    private boolean isWARValidated = false;

    // Class Loading
    private String classPath = ""; // classpath used by the classloader.
    private Vector classPaths = new Vector();
    private Vector libPaths = new Vector();
    // XXX deprecated
    private ServletClassLoader servletLoader;
    private ServletLoader servletL;

    // Interceptors
    private Vector initInterceptors = new Vector();
    private Vector serviceInterceptors = new Vector();
    private Vector destroyInterceptors = new Vector();
    private RequestSecurityProvider rsProvider;

    private Vector contextInterceptors = new Vector();
    
    // Servlets loaded by this context( String->ServletWrapper )
    private Hashtable servlets = new Hashtable();

    // -------------------- from web.xml
    private Hashtable initializationParameters = new Hashtable();
    private Vector welcomeFiles = new Vector();
    private Hashtable errorPages = new Hashtable();
    private String description = null;
    private boolean isDistributable = false;
    private MimeMap mimeTypes = new MimeMap();
    private int sessionTimeOut = -1;

    // taglibs
    Hashtable tagLibs=new Hashtable();
    // Env entries
    Hashtable envEntryTypes=new Hashtable();
    Hashtable envEntryValues=new Hashtable();
    
    // Maps specified in web.xml ( String->ServletWrapper )
    private Hashtable prefixMappedServlets = new Hashtable();
    private Hashtable extensionMappedServlets = new Hashtable();
    private Hashtable pathMappedServlets = new Hashtable();

    int debug=0;
    
    public Context() {
	//	System.out.println("New Context ");
    }
	
    // -------------------- Settable context properties --------------------
    // -------------------- Required properties
    public ContextManager getContextManager() {
	return contextM;
    }

    public void setContextManager(ContextManager cm) {
	contextM=cm;
    }
    
    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	// config believes that the root path is called "/",
	// 
	if( "/".equals(path) )
	    path="";
	this.path = path;
    }

    /* NOTE: if docBase is a URL to a remote location, we should download
       the context and unpack it. It is _very_ inefficient to serve
       files from a remote location ( at least 2x slower )
    */
    
    /** DocBase points to the web application files.
     *
     *  There is no restriction on the syntax and content of DocBase,
     *  it's up to the various modules to interpret this and use it.
     *  For example, to server from a war file you can use war: protocol,
     *  and set up War interceptors.
     * 
     *  "Basic" tomcat treats it is a file ( either absolute or relative to
     *  the CM home ). XXX Make it absolute ??
     *
     */
    public void setDocBase( String docB ) {
	this.docBase=docB;
    }

    public String getDocBase() {
	return docBase;
    }

    // -------------------- Tomcat specific properties

    public void setReloadable( boolean b ) {
	reloadable=b;
    }

    /** Should we reload servlets ?
     */
    public boolean getReloadable() {
	return reloadable;
    }
    
    public String getEngineHeader() {
	return engineHeader;
    }

    public void setEngineHeader(String s) {
        engineHeader=s;
    }

    public boolean isInvokerEnabled() {
        return isInvokerEnabled;
    }

    
    public void setInvokerEnabled(boolean isInvokerEnabled) {
        this.isInvokerEnabled = isInvokerEnabled;
    }

    public File getWorkDir() {
	return workDir;
    }

    public void setWorkDir(File workDir) {
	this.workDir = workDir;
    }

    /** Set work dir using a String property
     */
    public void setWorkDirPath(String workDir) {
	this.workDir=new File(workDir);
    }

    public boolean isWorkDirPersistent() {
        return this.isWorkDirPersistent;
    }

    public void setWorkDirPersistent( boolean b ) {
	isWorkDirPersistent=b;
    }
    
    // -------------------- Internal tomcat attributes 
    public void setRequestSecurityProvider(RequestSecurityProvider rsProvider) {
	this.rsProvider = rsProvider;
    }

    public RequestSecurityProvider getRequestSecurityProvider() {
	return this.rsProvider;
    }

    /** Will return an URL that can be used to read the resource pointed by
     * req, using the context base and the mapped path
     */
    public URL getResourceURL(Request req)
	throws MalformedURLException
    {
	String mappedPath = req.getMappedPath();
	if( debug>0 ) log( "getResourceURL: " + mappedPath + " " + req.getPathInfo());
	if( mappedPath == null ) {
	    mappedPath=req.getPathInfo();
	}
	if(mappedPath == null )
	    mappedPath=req.getLookupPath();
	
        URL docBase = getDocumentBase();

	// again, the special case of serving from wars
	// XXX Need an architecture to deal with other cases, like database-stored files,
	// etc.
	if (docBase.getProtocol().equalsIgnoreCase("war")) {
	    return WARUtil.createURL( this, mappedPath );
	}
	URL url=new URL(docBase.getProtocol(), docBase.getHost(),
		       docBase.getPort(), docBase.getFile() + mappedPath);
	if( debug>0) log( "getResourceURL=" + url + " request=" + req );
	return url;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
	if ( path == null  || ! path.startsWith("/")) {
	    return null; // spec say "return null if we can't return a dispather
	}

	RequestDispatcherImpl rD=new RequestDispatcherImpl( this );
	rD.setPath( path );

	return rD;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        if (name == null)
	    return null;

	// We need to do the checks 
	ServletWrapper wrapper = getServletByName( name );
	if (wrapper == null)
	    return null;
	RequestDispatcherImpl rD=new RequestDispatcherImpl( this );
	rD.setName( name );

	return rD;
    }

    /** Implements getResource() - use a sub-request to let interceptors do the job.
     */
    public URL getResource(String rpath)	throws MalformedURLException {
        URL url = null;

	if ("".equals(rpath)) 
	    return getDocumentBase();
	
	// deal with exceptional cases
        if (rpath == null) 
            throw new MalformedURLException(sm.getString("scfacade.getresource.npe"));
        else if ( ! rpath.startsWith("/")) {
	    // XXX fix - it shouldn't be a special case, MapperInterceptor
	    // should deal with this ( workaround for bug in MapperInterceptor)
	    //	    System.out.println("rpath=" + rpath + " " + path);
	    if( "/".equals(path) ) // default context
		rpath="/" + rpath;
	    else
		throw new MalformedURLException(sm.getString("scfacade.getresource.iae", rpath));
	}

	// Create a Sub-Request, do the request processing stage
	// that will take care of aliasing and set the paths
	Request lr=contextM.createRequest( this, rpath );
	getContextManager().processRequest(lr);

	return getResourceURL( lr );
    }

    
    Context getContext(String path) {
	if (! path.startsWith("/")) {
	    return null; // according to spec, null is returned
	    // if we can't  return a servlet, so it's more probable
	    // servlets will check for null than IllegalArgument
	}
	Request lr=contextM.createRequest( this, path );
	getContextManager().processRequest(lr);
        return lr.getContext();
    }

    public void log(String msg, Throwable t) {
	System.err.println(msg);
	t.printStackTrace(System.err);
    }

    /**
     * 
     */
    String getRealPath( String path) {
	//	Real Path is the same as PathTranslated for a new request
	
	Context base=this; // contextM.getContext("");
	Request req=contextM.createRequest( base , FileUtil.normPath(path) );
	contextM.processRequest(req);
	
	String mappedPath = req.getMappedPath();

	// XXX workaround - need to fix mapper to return mapped path
	if( mappedPath == null ) 
	    mappedPath=req.getPathInfo();
	if(mappedPath == null )
	    mappedPath=req.getLookupPath();
	
	String realPath= this.getDocBase() + mappedPath;

	// Probably not needed - it will be used on the local FS
	realPath = FileUtil.patch(realPath);

	return realPath;
    }

    public File getWARDir() {
        return this.warDir;
    }

    public void setWARDir( File f ) {
	warDir=f;
    }

    public boolean isWARExpanded() {
        return this.isWARExpanded;
    }

    public void setIsWARExpanded(boolean isWARExpanded) {
        this.isWARExpanded = isWARExpanded;
    }

    public boolean isWARValidated() {
        return this.isWARValidated;
    }

    public void setIsWARValidated(boolean isWARValidated) {
        this.isWARValidated = isWARValidated;
    }

    
    /**
     * Adds an interceptor for init() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> init()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */
    public void addInitInterceptor(LifecycleInterceptor interceptor) {
	initInterceptors.addElement(interceptor);
    }

    /**
     * Adds an interceptor for destroy() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> destroy()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */
    public void addDestroyInterceptor(LifecycleInterceptor interceptor) {
	destroyInterceptors.addElement(interceptor);
    }

    /**
     * Adds an interceptor for service() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> service()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */
    public void addServiceInterceptor(ServiceInterceptor interceptor) {
	serviceInterceptors.addElement(interceptor);
    }

    Vector getInitInterceptors() {
	return initInterceptors;
    }

    Vector getDestroyInterceptors() {
	return destroyInterceptors;
    }

    Vector getServiceInterceptors() {
	return serviceInterceptors;
    }
    
    /**
     * Initializes this context to take on requests. This action
     * will cause the context to load it's configuration information
     * from the webapp directory in the docbase.
     *
     * <p>This method may only be called once and must be called
     * before any requests are handled by this context.
     */
    public synchronized void init() {
	if (this.initialized) {
	    String msg = sm.getString("context.init.alreadyinit");
	    throw new IllegalStateException(msg);
	}
	this.initialized = true;

	for( int i=0; i< contextInterceptors.size(); i++ ) {
	    ((ContextInterceptor)contextInterceptors.elementAt(i)).contextInit( this );
	}
    }

    public void addContextInterceptor( ContextInterceptor ci) {
	contextInterceptors.addElement( ci );
    }

    public Enumeration getContextInterceptors() {
	return contextInterceptors.elements();
    }

    public SessionManager getSessionManager() {
	return sessionManager;
    }

    public void setSessionManager( SessionManager manager ) {
	sessionManager= manager;
    }
    
    public void shutdown() {
	// shut down container
	initialized=false;
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper wrapper = (ServletWrapper)servlets.get(key);

	    servlets.remove(key);
	    wrapper.destroy();
	}
	// shut down any sessions

	getSessionManager().removeSessions(this);

	for( int i=0; i< contextInterceptors.size(); i++ ) {
	    ((ContextInterceptor)contextInterceptors.elementAt(i)).contextShutdown( this );
	}

	System.out.println("Context: " + this + " down");
    }
    
    public Enumeration getWelcomeFiles() {
	return welcomeFiles.elements();
    }

    public void removeWelcomeFiles() {
	if( ! this.welcomeFiles.isEmpty() )
	    this.welcomeFiles.removeAllElements();
    }

    public void addWelcomeFile( String s) {
	welcomeFiles.addElement( s );
    }

    /** Add a taglib declaration for this context
     */
    public void addTaglib( String uri, String location ) {
	//	System.out.println("Add taglib " + uri + "  " + location );
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
	System.out.println("Add env-entry " + name + "  " + type + " " + value + " " +description );
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
    
    
    public String getInitParameter(String name) {
        return (String)initializationParameters.get(name);
    }

    /** @deprecated use addInitParameter
     */
    public void setInitParameter( String name, String value ) {
	initializationParameters.put(name, value );
    }

    public void addInitParameter( String name, String value ) {
	initializationParameters.put(name, value );
    }
    
    public Enumeration getInitParameterNames() {
        return initializationParameters.keys();
    }

    public Object getAttribute(String name) {
        if (name.equals("org.apache.tomcat.jsp_classpath"))
	  return getClassPath();
	else if(name.equals("org.apache.tomcat.classloader")) {
	  return this.getLoader();
        }else {
            Object o = attributes.get(name);
            return attributes.get(name);
        }
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    public URL getDocumentBase() {
	if( documentBase == null ) {
	    if( docBase != null)
		try {
		    documentBase=URLUtil.resolve( docBase );
		} catch( MalformedURLException ex ) {
		    ex.printStackTrace();
		}
	}
        return documentBase;
    }

    public void setDocumentBase(URL s) {
	// Used only by startup, will be removed
        this.documentBase=s;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon( String icon ) {

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
    
    public MimeMap getMimeMap() {
        return mimeTypes;
    }

    public void addContentType( String ext, String type) {
	mimeTypes.addContentType( ext, type );
    }

    public String getErrorPage(int errorCode) {
        return getErrorPage(String.valueOf(errorCode));
    }

    public void addErrorPage( String errorType, String value ) {
	this.errorPages.put( errorPages, value );
    }

    public String getErrorPage(String errorCode) {
        return (String)errorPages.get(errorCode);
    }

    ServletContextFacade getFacade() {
        if(contextFacade==null )
	    contextFacade = new ServletContextFacade(contextM, this);
	return contextFacade;
    }

    // --------------------

    
    /** Add a jsp to the "pre-defined" list ( used by web.xml )
     *  @deprecated Create a Wrapper and use add Servlet 
     */
    public void addJSP(String name, String path, String description) {
        // XXX
        // check for duplicates!

	//        JspWrapper wrapper = new JspWrapper(this);
	ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletName(name);
	wrapper.setServletDescription(description);
	wrapper.setPath(path);

	servlets.put(name, wrapper);
    }

    /** True if we have a servlet with className.
     */
    public boolean containsServlet(String className) {
	Enumeration enum = servlets.keys();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);
            if (className.equals(sw.getServletClass()))
	        return true;
	}
	return false;
    }

    /** Check if we have a servlet with the specified name
     */
    public boolean containsServletByName(String name) {
	return (servlets.containsKey(name));
    }

    // XXX use external iterator 
    /** Remove all servlets with a specific class name
     */
    void removeServletByClassName(String className) {
	Enumeration enum = servlets.keys();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);
            if (className.equals(sw.getServletClass()))
		removeServlet( sw );
	}
    }

    /** Remove the servlet with a specific name
     */
    public void removeServletByName(String servletName) {
	ServletWrapper wrapper=(ServletWrapper)servlets.get(servletName);
	if( wrapper != null ) {
	    removeServlet( wrapper );
	}
    }

    /** @deprecated use getServletByPath or getJsp
     */
    public boolean containsJSP(String path) {
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);

	    //	    if( (sw instanceof JspWrapper ) &&
	    if( path.equals( (sw).getPath()))
		return true;
	}
	return false;
    }

    /** Will remove a JSP from the list of "declared" jsps.
     *  Called only by deployment descriptor - to deal with
     *  duplicated mappings -
     *  XXX Find out if we really need that - it can be avoided!
     * @deprecated Use removeServlet and findServletByPath or ByName
     */
    public void removeJSP(String path) {
	Enumeration enum = servlets.keys();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);
	    //	    if( (sw instanceof JspWrapper ) &&
	    if(path.equals( (sw).getPath()))
	        removeServlet( sw );
	}
    }

    /** @deprecated use the method of servletWrapper
     */
    public void setServletInitParams(String name, Hashtable initParams) {
	ServletWrapper wrapper = (ServletWrapper)servlets.get(name);
	if (wrapper != null) {
	    wrapper.setInitArgs(initParams);
	}
    }

    // XXX only one mapping, the mapper should do it's own optimizations
    /**
     * Maps a named servlet to a particular path or extension.
     * If the named servlet is unregistered, it will be added
     * and subsequently mapped.
     *
     * Note that the order of resolution to handle a request is:
     *
     *    exact mapped servlet (eg /catalog)
     *    prefix mapped servlets (eg /foo/bar/*)
     *    extension mapped servlets (eg *jsp)
     *    default servlet
     *
     */
    public void addMapping(String servletName, String path) {
        ServletWrapper sw = (ServletWrapper)servlets.get(servletName);

	if (sw == null) {
	    System.out.println("Servlet not registered " + servletName );
	    // XXX
	    // this might be a bit aggressive

	    if ( servletName.startsWith("/")) {
	        addJSP(servletName, servletName, null);
	    } else {
	        addServlet(servletName, null, servletName);
	    }

	    sw = (ServletWrapper)servlets.get(servletName);
	}

	path = path.trim();

	if (sw != null &&
	    (path.length() > 0)) {
	    if (path.startsWith("/") &&
                path.endsWith("/*")){
	        prefixMappedServlets.put(path, sw);
		//		System.out.println("Map " + path + " -> " + sw );
	    } else if (path.startsWith("*.")) {
	        extensionMappedServlets.put(path, sw);
	    } else if (! path.equals("/")) {
	        pathMappedServlets.put(path, sw);
	    } else {
	        defaultServlet = sw;
	    }
	}
    }

    public ServletWrapper getDefaultServlet() {
	if( defaultServlet==null)
	    defaultServlet=getServletByName(Constants.DEFAULT_SERVLET_NAME );
	// XXX works only if we do load default web.xml first - we should
	// be able to work without that trick ( i.e. define a "default" )
	
	return defaultServlet;
    }
    
    public Hashtable getPathMap() {
	return pathMappedServlets;
    }

    public Hashtable getPrefixMap() {
	return prefixMappedServlets;
    }

    public Hashtable getExtensionMap() {
	return extensionMappedServlets;
    }
    
    public boolean containsMapping(String mapping) {
        mapping = mapping.trim();

        return (prefixMappedServlets.containsKey(mapping) ||
	    extensionMappedServlets.containsKey(mapping) ||
	    pathMappedServlets.containsKey(mapping));
    }

    public void removeMapping(String mapping) {
        mapping = mapping.trim();

	prefixMappedServlets.remove(mapping);
	extensionMappedServlets.remove(mapping);
	pathMappedServlets.remove(mapping);
    }

    // XXX replace with getServlet()
    public ServletWrapper getServletByName(String servletName) {
	return (ServletWrapper)servlets.get(servletName);
    }

    /**
     * Add a servlet with the given name to the container. The
     * servlet will be loaded by the container's class loader
     * and instantiated using the given class name.
     *
     * Called to add a new servlet from web.xml
     * @deprecated use addServlet(ServletWrapper)
     */
    public void addServlet(String name, String className,
			   String description) {
	// assert className!=null

        // check for duplicates
        if (servlets.get(name) != null) {
            removeServletByClassName(name); // XXX XXX why?
            removeServletByName(name);
        }

        ServletWrapper wrapper = new ServletWrapper(this);
	wrapper.setServletName(name);
	wrapper.setServletDescription(description);
	wrapper.setServletClass(className);

	servlets.put(name, wrapper);
    }

    public void addServlet(ServletWrapper wrapper) {
	String name=wrapper.getServletName();
	//	System.out.println("Adding servlet " + name  + " " + wrapper);
        // check for duplicates
        if (servlets.get(name) != null) {
            removeServletByClassName(name); // XXX XXX why?
            removeServletByName(name);
        }
	servlets.put(name, wrapper);
    }

    private void removeServlet(ServletWrapper sw) {
	if (prefixMappedServlets.contains(sw)) {
	    Enumeration enum = prefixMappedServlets.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();
		
		if (prefixMappedServlets.get(key).equals(sw)) {
		    prefixMappedServlets.remove(key);
		}
	    }
	}
	
	if (extensionMappedServlets.contains(sw)) {
	    Enumeration enum = extensionMappedServlets.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();

		if (extensionMappedServlets.get(key).equals(sw)) {
		    extensionMappedServlets.remove(key);
		}
	    }
	}
	
	if (pathMappedServlets.contains(sw)) {
	    Enumeration enum = pathMappedServlets.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();

		if (pathMappedServlets.get(key).equals(sw)) {
		    pathMappedServlets.remove(key);
		}
	    }
	}
	
	servlets.remove(sw.getServletName());
    }
    
    public Enumeration getServletNames() {
	return servlets.keys();
    }

    // -------------------- Class Loading --------------------

    public void setServletLoader(ServletLoader loader ) {
	this.servletL=loader;
    }
    
    public ServletLoader getServletLoader() {
	return servletL;
    }

    /** @deprecated
     */
    public void setLoader(ServletClassLoader loader ) {
	this.servletLoader=loader;
    }

    /** @deprecated
     */
    public ServletClassLoader getLoader() {
	return servletLoader;
    }

    public Enumeration getClassPaths() {
        return this.classPaths.elements();
    }

    public void addClassPath(String path) {
        this.classPaths.addElement(path);
    }

    public Enumeration getLibPaths() {
        return this.libPaths.elements();
    }

    public void addLibPath(String path) {
        this.libPaths.addElement(path);
    }

    // XXX XXX XXX ugly, need rewrite ( servletLoader will call getClassPaths and getLibPaths
    // and will concatenate the "file" part of them ).
    /** Returns the classpath as a string
     */
    public String getClassPath() {
        String cp = this.classPath.trim();
        String servletLoaderClassPath =
            this.getLoader().getClassPath();

        if (servletLoaderClassPath != null &&
            servletLoaderClassPath.trim().length() > 0) {
            cp += ((cp.length() > 0) ? File.pathSeparator : "") +
                servletLoaderClassPath;
        }

        return cp;
    }

    /* -------------------- Utils  -------------------- */
    public void setDebug( int level ) {
	debug=level;
    }

    public void log( String msg ) {
	System.out.println("Context(" + path  + "): " + msg );
    }
    
    public String toString() {
	return "Ctx(" + path + "," + getDocBase() + ")";
	// + " , " + getDocumentBase() + " ) ";
    }
}
