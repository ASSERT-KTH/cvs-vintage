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
    private URL docBase;

    // internal state / related objects
    private boolean initialized = false;
    private ContextManager server;
    private ServletContextFacade contextFacade;
    private SessionManager sessionManager;
    private ServletWrapper defaultServlet = null;

    // 
    private Hashtable attributes = new Hashtable();

    // work dir
    private File workDir;
    private boolean isWorkDirPersistent = false;

    // tomcat specific properties
    private String engineHeader = null;
    private URL servletBase = null;
    private boolean isInvokerEnabled = false;

    // for serving WARs directly 
    private File warDir = null;
    private boolean isWARExpanded = false;
    private boolean isWARValidated = false;

    // Class Loading 
    private String classPath = ""; // classpath used by the classloader.
    private Vector classPaths = new Vector();
    private Vector libPaths = new Vector();
    private ServletClassLoader servletLoader;
    private ClassLoader classLoader = null;
    
    // Interceptors
    private Vector initInterceptors = new Vector();
    private Vector serviceInterceptors = new Vector();
    private Vector destroyInterceptors = new Vector();
    private RequestSecurityProvider rsProvider;
    
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

    // Maps specified in web.xml ( String->ServletWrapper )
    private Hashtable prefixMappedServlets = new Hashtable();
    private Hashtable extensionMappedServlets = new Hashtable();
    private Hashtable pathMappedServlets = new Hashtable();
    // servlets loaded on startup( String->ServletWrapper )
    private Hashtable loadableServlets = new Hashtable();

    // -------------------- Accessors --------------------
    public Context() {
    }
	
    public Context(ContextManager server, String path, URL docBase) {
        this.server = server;
	this.path = path;
	setDocumentBase( docBase );
        contextFacade = new ServletContextFacade(server, this);
    }

    public String getEngineHeader() {
        if( engineHeader==null) {
	    /*
	     * Whoever modifies this needs to check this modification is
	     * ok with the code in com.jsp.runtime.ServletEngine or talk
	     * to akv before you check it in. 
	     */
	    // Default value for engine header
	    // no longer use core.properties - the configuration comes from
	    // server.xml or web.xml - no more properties.
	    StringBuffer sb=new StringBuffer();
	    sb.append(Constants.TOMCAT_NAME).append("/").append(Constants.TOMCAT_VERSION);
	    sb.append(" (").append(Constants.JSP_NAME).append(" ").append(Constants.JSP_VERSION);
	    sb.append("; ").append(Constants.SERVLET_NAME).append(" ");
	    sb.append(Constants.SERVLET_MAJOR).append(".").append(Constants.SERVLET_MINOR);
	    sb.append( "; Java " );
	    sb.append(System.getProperty("java.version")).append("; ");
	    sb.append(System.getProperty("os.name") + " ");
	    sb.append(System.getProperty("os.version") + " ");
	    sb.append(System.getProperty("os.arch") + "; java.vendor=");
	    sb.append(System.getProperty("java.vendor")).append(")");
	    engineHeader=sb.toString();
	}
	return engineHeader;
    }

    public void setEngineHeader(String s) {
        engineHeader=s;
    }

    public ContextManager getContextManager() {
	return server;
    }
    
    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public boolean isInvokerEnabled() {
        return isInvokerEnabled;
    }

    public void setInvokerEnabled(boolean isInvokerEnabled) {
        this.isInvokerEnabled = isInvokerEnabled;
    }

    public void setRequestSecurityProvider(
	RequestSecurityProvider rsProvider) {
	this.rsProvider = rsProvider;
    }

    public RequestSecurityProvider getRequestSecurityProvider() {
	if ( rsProvider==null)
	    rsProvider=DefaultRequestSecurityProvider.getInstance();
	
	return this.rsProvider;
    }

    public File getWorkDir() {
	if( workDir==null)
	    workDir=new File(System.getProperty("user.dir", ".") +
			     System.getProperty("file.separator") + Constants.WORK_DIR);
	return workDir;
    }

    public void setWorkDir(String workDir, boolean isWorkDirPersistent) {
        File f = null;

        try {
	    f = new File(workDir);
	} catch (Throwable e) {
	}

	setWorkDir(f, isWorkDirPersistent);
    }

    public void setWorkDir(File workDir, boolean isWorkDirPersistent) {
	this.workDir = workDir;
        this.isWorkDirPersistent = isWorkDirPersistent;
	// assert workDir!=null - or no reason to set it

	// workDir will be cleaned at init() and shutdown()
    }

    public boolean isWorkDirPersistent() {
        return this.isWorkDirPersistent;
    }

    File getWARDir() {
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

	// Set defaults if not already there
	new DefaultContextSetter().handleContextInit( this );
	
	// set up work dir ( attribute + creation )
	new WorkDirInterceptor().handleContextInit( this );

	// XXX who uses servletBase ???
	URL servletBase = this.docBase;
        this.setServletBase(servletBase);

	// expand WAR
	new WarInterceptor().handleContextInit( this );

	// Read context's web.xml
	new WebXmlInterceptor().handleContextInit( this );

	if (! this.isInvokerEnabled) {
	    // Put in a special "no invoker" that handles
	    // /servlet requests and explains why no servlet
	    // is being invoked
	    this.addServlet(Constants.Servlet.NoInvoker.Name,
			    Constants.Servlet.NoInvoker.Class, null);
	    this.addMapping(Constants.Servlet.NoInvoker.Name,
			    Constants.Servlet.NoInvoker.Map);
	}

	// load initial servlets
	new LoadOnStartupInterceptor().handleContextInit( this );
    }

    public SessionManager getSessionManager() {
	if( sessionManager==null ) {
	    // default - will change when a better one exists
	    //	    sessionManager = org.apache.tomcat.session.ServerSessionManager.getManager();
	    sessionManager =
		new org.apache.tomcat.session.StandardSessionManager();
	}
	return sessionManager;
    }

    public void setSessionManager( SessionManager manager ) {
	sessionManager= manager;
    }
    
    public void shutdown() {
	// shut down container
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper wrapper = (ServletWrapper)servlets.get(key);

	    servlets.remove(key);
	    wrapper.destroy();
	}
	// shut down any sessions

	getSessionManager().removeSessions(this);

	new WorkDirInterceptor().handleContextShutdown(this);
	
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
    
    public String getInitParameter(String name) {
        return (String)initializationParameters.get(name);
    }

    public void setInitParameter( String name, String value ) {
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
        return docBase;
    }

    public void setDocumentBase(URL docBase) {
	String file = docBase.getFile();

	if (! file.endsWith("/")) {
	    try {
		docBase = new URL(docBase.getProtocol(),
                    docBase.getHost(), docBase.getPort(), file + "/");
	    } catch (MalformedURLException mue) {
		System.out.println("SHOULD NEVER HAPPEN: " + mue);
	    }
	}

	this.docBase = docBase;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return contextFacade;
    }


    public Enumeration getInitLevels() {
	return loadableServlets.keys();
    }

    public Enumeration getLoadableServlets( Integer level ) {
	return ((Vector)loadableServlets.get( level )).elements();
    }

    public void setLoadableServlets( Integer level, Vector servlets ) {
	loadableServlets.put( level, servlets );
    }

    public void addLoadableServlet( Integer level,String name ) {
	Vector v;
	if( loadableServlets.get(level) != null ) 
	    v=(Vector)loadableServlets.get(level);
	else
	    v=new Vector();
	
	v.addElement(name);
	loadableServlets.put(level, v);
    }
    

    // -------------------- From Container

    public URL getServletBase() {
        return this.servletBase;
    }

    public void setServletBase(URL servletBase) {
        this.servletBase = servletBase;
    }


    // -------------------- 
    public void addJSP(String name, String path) {
        addJSP(name, null, path);
    }

    public void addJSP(String name, String path, String description) {
        // XXX
        // check for duplicates!

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

    public boolean containsJSP(String path) {
        ServletWrapper[] sw = getServletsByPath(path);

        return (sw != null &&
	    sw.length > 0);
    }

    public void removeJSP(String path) {
	Enumeration enum = servlets.keys();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);
	    if (path.equals(sw.getPath()))
	        removeServlet( sw );
	}
    }

    public void setServletInitParams(String name, Hashtable initParams) {
	ServletWrapper wrapper = (ServletWrapper)servlets.get(name);
	if (wrapper != null) {
	    wrapper.setInitArgs(initParams);
	}
    }
    
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
	    // XXX
	    // this might be a bit aggressive

	    if (! servletName.startsWith("/")) {
	        addServlet(servletName, null, servletName);
	    } else {
	        addJSP(servletName, servletName);
	    }

	    sw = (ServletWrapper)servlets.get(servletName);
	}

	path = path.trim();

	if (sw != null &&
	    (path.length() > 0)) {
	    if (path.startsWith("/") &&
                path.endsWith("/*")){
	        prefixMappedServlets.put(path, sw);
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

    Request lookupServletByName(String servletName) {
        Request lookupResult = null;

	ServletWrapper wrapper = (ServletWrapper)servlets.get(servletName);

	if (wrapper != null) {
	    lookupResult = new Request();
	    lookupResult.setWrapper( wrapper );
	    lookupResult.setPathInfo("");
	}

        return lookupResult;
    }

    public ServletWrapper getServletByName(String servletName) {
	return (ServletWrapper)servlets.get(servletName);
    }

    ServletWrapper getServletAndLoadByName(String servletName) {
	// XXX
	// make sure that we aren't tramping over ourselves!
	ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletClass(servletName);

	servlets.put(servletName, wrapper);

	return wrapper;
    }

    ServletWrapper loadServlet(String servletClassName) {
        // XXX
        // check for duplicates!

        // XXX
        // maybe dispatch to addServlet?
        
        ServletWrapper wrapper = new ServletWrapper(this);

        wrapper.setServletClass(servletClassName);

        servlets.put(servletClassName, wrapper);

        return wrapper;
    }

    /**
     * Add a servlet with the given name to the container. The
     * servlet will be loaded by the container's class loader
     * and instantiated using the given class name.
     *
     * Called to add a new servlet from web.xml
     *
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
    
    private void removeServlets(ServletWrapper[] sw) {
	if (sw != null) {
	    for (int i = 0; i < sw.length; i++) {
		removeServlet( sw[i] );
	    }
	}
    }

    /** Return servlets with a specified class name
     */
    private ServletWrapper[] getServletsByClassName(String name) {
        Vector servletWrappers = new Vector();
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);


            if (sw.getServletClass() != null &&
                sw.getServletClass().equals(name)) {
	        servletWrappers.addElement(sw);
	    }
	}

	ServletWrapper[] wrappers =
	    new ServletWrapper[servletWrappers.size()];

	servletWrappers.copyInto((ServletWrapper[])wrappers);

        return wrappers;
    }

    // XXX
    // made package protected so that RequestMapper can have access

    public ServletWrapper[] getServletsByPath(String path) {
        Vector servletWrappers = new Vector();
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);

	    if (sw.getPath() != null &&
	        sw.getPath().equals(path)) {
	        servletWrappers.addElement(sw);
	    }
	}

	ServletWrapper[] wrappers =
	    new ServletWrapper[servletWrappers.size()];

	servletWrappers.copyInto((ServletWrapper[])wrappers);

        return wrappers;
    }


    // -------------------- Class Loading --------------------
    public ClassLoader getClassLoader() {
      return this.classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    void setLoader(ServletClassLoader loader ) {
	this.servletLoader=loader;
    }
    
    ServletClassLoader getLoader() {
	if(servletLoader == null) {
	    servletLoader = new ServletClassLoaderImpl(this);
	}
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
}
