/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ServletWrapper.java,v 1.35 2000/03/30 21:46:07 costin Exp $
 * $Revision: 1.35 $
 * $Date: 2000/03/30 21:46:07 $
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
package org.apache.tomcat.core;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Class used to represent a servlet inside a Context.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@dnt.ro
 */
public class ServletWrapper {
    protected StringManager sm = StringManager.getManager("org.apache.tomcat.core");

    protected Context context;
    protected ContextManager contextM;

    // servletName is stored in config!
    protected String servletClassName; // required
    protected ServletConfigImpl config;
    protected Servlet servlet;
    protected Class servletClass;

    // Jsp pages
    private String path = null;

    // optional informations
    protected String description = null;

    boolean initialized=false;
    boolean available=true;

    // Usefull info for class reloading
    protected boolean isReloadable = false;
    // information + make sure destroy is called when no other servlet
    // is running ( this have to be revisited !) 
    protected long lastAccessed;
    protected int serviceCount = 0;

    int loadOnStartup=0;

    Hashtable initArgs=null;
    Hashtable securityRoleRefs=new Hashtable();
    
    public ServletWrapper() {
        config = new ServletConfigImpl();
    }

    ServletWrapper(Context context) {
        config = new ServletConfigImpl();
	setContext( context );
    }

    public void setContext( Context context) {
        this.context = context;
	contextM=context.getContextManager();
	config.setContext( context );
	isReloadable=context.getReloadable();
    }

    protected Context getContext() {
	return context;
    }

    public void setLoadOnStartUp( int level ) {
	loadOnStartup=level;
    }

    public void setLoadOnStartUp( String level ) {
	loadOnStartup=new Integer(level).intValue();
    }

    public int getLoadOnStartUp() {
	return loadOnStartup;
    }
    
    void setReloadable(boolean reloadable) {
	isReloadable = reloadable;
    }

    public String getServletName() {
        return config.getServletName();
    }

    public void setServletName(String servletName) {
        config.setServletName(servletName);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getServletDescription() {
        return this.description;
    }

    public void setServletDescription(String description) {
        this.description = description;
    }

    public String getServletClass() {
        return this.servletClassName;
    }

    public void setServletClass(String servletClassName) {
        this.servletClassName = servletClassName;
	config.setServletClassName(servletClassName);
    }

    /** Security Role Ref represent a mapping between servlet role names and
     *  server roles
     */
    public void addSecurityMapping( String name, String role, String description ) {
	securityRoleRefs.put( name, role );
    }

    public String getSecurityRole( String name ) {
	return (String)securityRoleRefs.get( name );
    }

    public Servlet getServlet() {
	if(servlet==null) {
	    try {
		loadServlet();
	    } 	catch( Exception ex ) {
		ex.printStackTrace();
	    }
	}
	return servlet;
    }

    public void addInitParam( String name, String value ) {
	if( initArgs==null) {
	    initArgs=new Hashtable();
	    config.setInitArgs( initArgs );
	}
	initArgs.put( name, value );
    }
    
    void destroy() {
	initialized=false;
	if (servlet != null) {
	    synchronized (this) {
		// Fancy sync logic is to make sure that no threads are in the
		// handlerequest when this is called and, furthermore, that
		// no threads go through handle request after this method starts!
		// Wait until there are no outstanding service calls,
		// or until 30 seconds have passed (to avoid a hang)
		
		//XXX I don't think it works ( costin )
		while (serviceCount > 0) {
		    try {
			wait(30000);
			
			break;
		    } catch (InterruptedException e) { }
		}

		try {
		    ContextInterceptor cI[]=context.getContextInterceptors();
		    for( int i=0; i<cI.length; i++ ) {
			try {
			    cI[i].preServletDestroy( context, this ); // ignore the error - like in the original code
			} catch( TomcatException ex) {
			    ex.printStackTrace();
			}
			
		    }
		    servlet.destroy();
		    for( int i=cI.length-1; i>=0; i-- ) {
			try {
			    cI[i].postServletDestroy( context, this ); // ignore the error - like in the original code
			} catch( TomcatException ex) {
			    ex.printStackTrace();
			}
			
		    }
		} catch(Exception ex) {
		    ex.printStackTrace();
		    // Should never come here...
		}
	    }
	}
    }

    /** Load and init a the servlet pointed by this wrapper
     */
    public void loadServlet()
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, ServletException
    {
	if (servletClass == null) {
	    if (servletClassName == null) 
		throw new IllegalStateException(sm.getString("wrapper.load.noclassname"));

	    servletClass=context.getServletLoader().loadClass( servletClassName);
	}
	
	servlet = (Servlet)servletClass.newInstance();
	//	System.out.println("Loading " + servletClassName + " " + servlet );
	
	config.setServletClassName(servlet.getClass().getName());
	try {
	    initServlet();
	} catch( UnavailableException ex ) {
	    available=false;
	} catch( ServletException ex ) {
	    available=false;
	}
	//	System.out.println("Init ok " + available);
    }

    
    void initServlet()
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, ServletException
    {
	//	try {
	    final Servlet sinstance = servlet;
	    final ServletConfigImpl servletConfig = config;
	    
	    ContextInterceptor cI[]=context.getContextInterceptors();
	    for( int i=0; i<cI.length; i++ ) {
		try {
		    cI[i].preServletInit( context, this ); // ignore the error - like in the original code
		} catch( TomcatException ex) {
		    ex.printStackTrace();
		}
	    }
	    servlet.init(servletConfig);
	    // if an exception is thrown in init, no end interceptors will be called.
	    // that was in the origianl code
	    
	    for( int i=cI.length-1; i>=0; i-- ) {
		try {
		    cI[i].postServletInit( context, this ); // ignore the error - like in the original code
		} catch( TomcatException ex) {
		    ex.printStackTrace();
		}
		
	    }
	    initialized=true;
	    //	} catch(IOException ioe) {
	    //	    ioe.printStackTrace();
	    // Should never come here...
	    //	}
    }

    // Reloading
    // XXX ugly - should find a better way to deal with invoker
    // The problem is that we are just clearing up invoker, not
    // the class loaded by invoker.
    void handleReload() {
	// That will be reolved after we reset the context - and many
	// other conflicts.
	if( isReloadable && ! "invoker".equals( getServletName())) {
	    ServletLoader loader=context.getServletLoader();
	    if( loader!=null) {
		// XXX no need to check after we remove the old loader
		if( loader.shouldReload() ) {
		    initialized=false;
		    loader.reload();
		    servlet=null;
		    servletClass=null;
// 		    String path=context.getPath();
// 		    String docBase=context.getDocBase();
// 		    // XXX all other properties need to be saved or something else
// 		    ContextManager cm=context.getContextManager();
// 		    cm.removeContext(path);
// 		    Context ctx=new Context();
// 		    ctx.setPath( path );
// 		    ctx.setDocBase( docBase );
// 		    cm.addContext( ctx );
// 		    context=ctx;
//		    // XXX shut down context, remove sessions, etc
		}
	    }
	}
    }
    
    public void handleRequest(Request req, Response res)
    {
	ClassLoader originalCL=null;
	try {
	    // Before we do init() or service(), we need to do some tricks
	    // with the class loader - see bug #116.
	    // some JDK1.2 code will not work without this fix

	    // we save the originalCL because we might be in include
	    // and we need to revert to it when we finish

	    // that will set a new (JDK)context class loader, and return the old one
	    // if we are in JDK1.2
	    originalCL = fixJDKContextClassLoader(context.getServletLoader().getClassLoader());


	    if( path != null ) {
		// XXX call JspServlet directly, did anyone tested it ??
		String requestURI = path + req.getPathInfo();
		RequestDispatcher rd = req.getContext().getRequestDispatcher(requestURI);
		
		if (! res.isStarted())
		    rd.forward(req.getFacade(), res.getFacade());
		else
		    rd.include(req.getFacade(), res.getFacade());

		fixJDKContextClassLoader( originalCL );
		return;
	    }
	    
	    handleReload();
	    //	    System.out.println(" SW " + initialized );
	    if( ! initialized ) {
		try {
		    loadServlet();
		} catch(Exception ex ) {
		    // return not found
		    context.log( "Class Not Found", ex );
		    res.setStatus( 404 );
		    contextM.handleError( req, res, null,  404 );
		    fixJDKContextClassLoader( originalCL );
		    return;
		}
	    }

	    // If servlet was not initialized
	    if( ! available ) {
		// XXX ADD code to handle this case XXX BUG 67
		// 		res.setStatus(404);
		// 		contextM.handleError( req, res, ex, 404 );
	    }
	    // XXX to expensive  per/request, un-load is not so frequent and
	    // the API doesn't require a special state for destroy
	    // synchronized(this) {
	    // 		// logic for un-loading
	    // 		serviceCount++;
	    //
	    
	    RequestInterceptor cI[]=context.getRequestInterceptors();
	    for( int i=0; i<cI.length; i++ ) {
		cI[i].preService( req, res ); // ignore the error - like in the original code
	    }
	    
	    if (servlet instanceof SingleThreadModel) {
		synchronized(servlet) {
		    servlet.service(req.getFacade(), res.getFacade());
		}
	    } else {
		//System.out.print("X");
		servlet.service(req.getFacade(), res.getFacade());
		//System.out.print("Y");
	    }
	    
	    for( int i=cI.length-1; i>=0; i-- ) {
		cI[i].postService( req , res ); // ignore the error - like in the original code
	    }
	    // 	} finally {
	    // 	    synchronized(this) {
	    // 		serviceCount--;
	    // 		notifyAll();
	    // 	    }
	    // 	}
	} catch( Throwable t ) {
	    if( null!=req.getAttribute("tomcat.servlet.error.defaultHandler") ) {
		// we are in handleRequest for the "default
		System.out.println("ERROR: can't find default error handler or error in default error page");
		t.printStackTrace();
	    } else {
		contextM.handleError( req, res, t, 0 );
	    }
	} finally {
	    fixJDKContextClassLoader(originalCL );
	}
    }

    static boolean haveContextClassLoader=true;
    static Class noParams[]=new Class[0];
    static Class clParam[]=new Class[1];
    static Object noObjs[]=new Object[0];
    static { clParam[0]=ClassLoader.class; }

    /** Reflection trick to set the context class loader for JDK1.2, without
	braking JDK1.1.

	This code can be commented out for 3.1 if it creates any problems -
	it should work.

	XXX We need to find a better way to do that - maybe make it part of
	the ServletLoader interface.
     */
    ClassLoader fixJDKContextClassLoader( ClassLoader cl ) {
	if( cl==null ) return null;
	if( ! haveContextClassLoader ) return null;
	
	Thread t=Thread.currentThread();
	try {
	    java.lang.reflect.Method getCCL=t.getClass().getMethod("getContextClassLoader", noParams);
	    java.lang.reflect.Method setCCL=t.getClass().getMethod("setContextClassLoader", clParam) ;
	    if( (getCCL==null) || (setCCL==null) ) {
		haveContextClassLoader=false;
		return null;
	    }
	    ClassLoader old=( ClassLoader)getCCL.invoke( t, noObjs );
	    Object params[]=new Object[1];
	    params[0]=cl;
	    setCCL.invoke( t, params );
	    if( context.getDebug() > 5 ) context.log("Setting system loader " + old + " " + cl );
	    context.log("Setting system loader " + old + " " + cl );
	    
	    return old;
	} catch (NoSuchMethodException ex ) {
	    // we don't have the methods, don't try again
	    haveContextClassLoader=false;
	} catch( Exception ex ) {
	    haveContextClassLoader = false;
	    context.log( "Error setting jdk context class loader", ex );
	}
	return null;
    }

    
    /** @deprecated
     */
    public void handleRequest(final HttpServletRequestFacade request,
			      final HttpServletResponseFacade response)
    {
	Request rrequest=request.getRealRequest();
	Response rresponse=rrequest.getResponse();

	handleRequest( rrequest, rresponse );
    }

    
    public String toString() {
	String toS="Wrapper(" + config.getServletName() + " ";
	if( servlet!=null ) toS=toS+ "S:" + servlet.getClass().getName();
	else  toS= toS + servletClassName;
	return toS + ")";
    }

}
