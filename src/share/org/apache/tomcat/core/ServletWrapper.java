/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ServletWrapper.java,v 1.21 2000/02/10 23:49:57 costin Exp $
 * $Revision: 1.21 $
 * $Date: 2000/02/10 23:49:57 $
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

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
public class ServletWrapper {
    protected StringManager sm = StringManager.getManager("org.apache.tomcat.core");

    protected Context context;

    // servletName is stored in config!
    protected String servletClassName; // required
    protected ServletConfigImpl config;
    protected Servlet servlet;
    protected Class servletClass;

    // Jsp pages
    private String path = null;

    // optional informations
    protected String description = null;

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

    String getServletDescription() {
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
    
    /** @deprecated
     */
    void setInitArgs(Hashtable initArgs) {
        config.setInitArgs(initArgs);
    }

    void destroy() {
	if (servlet != null) {
	    synchronized (this) {
		waitForDestroy();
		try {
		    handleDestroy( context, servlet );
		} catch(IOException ioe) {
		    ioe.printStackTrace();
		    // Should never come here...
		} catch(ServletException se) {
		    se.printStackTrace();
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

	config.setServletClassName(servlet.getClass().getName());

	try {
	    final Servlet sinstance = servlet;
	    final ServletConfigImpl servletConfig = config;
	    
	    handleInit(context, servlet, servletConfig);
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	    // Should never come here...
	}
    }

    // XXX XXX need to go directly to Jsp API 
    public void handleJspRequest(final HttpServletRequestFacade request,
			      final HttpServletResponseFacade response)
	throws IOException
    {
	// "Special" JSP
	String requestURI = path + request.getPathInfo();
	RequestDispatcher rd = request.getRequestDispatcher(requestURI);
	
	try {
	    if (! response.getRealResponse().isStarted())
		rd.forward(request, response);
	    else
		rd.include(request, response);
		
	} catch (ServletException se) {
	    se.printStackTrace();
	    response.sendError(404);
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	    response.sendError(404);
	}
	return;
    }
    
    public void handleRequest(final HttpServletRequestFacade request,
			      final HttpServletResponseFacade response)
	throws IOException
    {
	if( path != null ) handleJspRequest( request, response );

	Context context = getContext();

	// Reloading
	// XXX ugly - should find a better way to deal with invoker
	// The problem is that we are just clearing up invoker, not
	// the class loaded by invoker.

	// That will be reolved after we reset the context - and many
	// other conflicts.
	if( isReloadable && ! "invoker".equals( getServletName())) {
	    ServletLoader loader=context.getServletLoader();
	    if( loader!=null) {
		// XXX no need to check after we remove the old loader
		if( loader.shouldReload() ) {
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
	
	
	if (servlet == null) {
	    synchronized (this) {
		try {
		    loadServlet();
		} catch (ClassNotFoundException e) {
		    response.sendError(404, "Class not found " + servletClassName);
		    return;
		} catch (Exception e) {
		    // Make sure the servlet will never
		    // service a request
		    servlet = null;
		    sendInternalServletError(e, request, response);
		    return;
		}
	    }
	}

        try {
	    synchronized(this) {
		// logic for un-loading
		serviceCount++;
	    }

	    
	    handleInvocation( context, servlet, request, response );

	} catch (ServletException e) {
	    // XXX
	    // if it's an unvailable exception, we probably want
	    // to paint a different screen
            handleException(request, response, e);
        } catch (SocketException e) {
	    // replace with Log:
	    System.out.println("Socket Exception : " + e.getMessage());
        } catch (Throwable e) {
	    // XXX
	    // decide which exceptions we should not eat at this point
            handleException(request, response, e);
	} finally {
	    synchronized(this) {
		serviceCount--;
		notifyAll();
	    }
	}
    }

    public void handleException(HttpServletRequestFacade request,
				HttpServletResponseFacade response,
				Throwable t)
    {
        Context context = request.getRealRequest().getContext();
        ServletContextFacade contextFacade = context.getFacade();

        // Scan the exception's inheritance tree looking for a rule
        // that this type of exception should be forwarded

        String path = null;
        Class clazz = t.getClass();

        while (path == null && clazz != null) {
            String name = clazz.getName();
            path = context.getErrorPage(name);
            clazz = clazz.getSuperclass();
        }
	
        // If path is non-null, we should do a forward
        // Don't do a forward if exception_type is already defined though to
        // avoid an infinite loop.

        if (path != null &&
	    request.getAttribute(
                Constants.Attribute.ERROR_EXCEPTION_TYPE) == null) {
            RequestDispatcher rd = contextFacade.getRequestDispatcher(path);

            // XXX 
            // The spec should really be changed to allow us to include
            // the full exception object.  Oh well.

            request.setAttribute(Constants.Attribute.ERROR_EXCEPTION_TYPE,
	        t.getClass().getName());
            request.setAttribute(Constants.Attribute.ERROR_MESSAGE,
                t.getMessage());

            try {
		// A forward would be ideal, so reset and try it

		response.getRealResponse().reset();

		if (response.getRealResponse().isStarted()) 
		    rd.include(request, response);
		else
		    rd.forward(request, response);
            } catch (IOException e) {
		e.printStackTrace();
                // Shouldn't get here
            } catch (ServletException e) {
		e.printStackTrace();
                // Shouldn't get here
            }
        } else {
            try {
		sendInternalServletError( t, request, response);
	    } catch (IOException e) {
                e.printStackTrace();
		// ???
            }
        }
    }

    void sendInternalServletError( Throwable t, HttpServletRequestFacade request,
				   HttpServletResponseFacade response )
	throws IOException
    {
	// Used to communicate with Default Error Page
		request.setAttribute("tomcat.error.throwable", t);
		// XXX need to make this configurable, any servlet
		// can act as the default error handler

		// Need to do a normal servlet invocation!
		try {
		    Servlet errorP=new org.apache.tomcat.servlets.DefaultErrorPage();
		    errorP.service(request,response);
		} catch(Exception ex) {
		    System.out.println("FATAL: error in error handler");
		    ex.printStackTrace();
		}
    }
    
    /** Call the init method and all init interceptors
     */
    protected void handleInit(Context context, Servlet servlet, ServletConfig servletConfig )
	throws ServletException, IOException
    {
	//	Vector v=context.getInitInterceptors();
	// 	for( int i=0; i<v.size(); i++ ) {
	// 	    try { 
	// 		((LifecycleInterceptor)v.elementAt(i)).preInvoke( context, servlet );
	// 	    } catch(InterceptorException ex ) {
	// 		ex.printStackTrace();
	// 	    }
	// 	}
	ContextInterceptor cI[]=context.getContextInterceptors();
	for( int i=0; i<cI.length; i++ ) {
	    cI[i].preServletInit( context, this ); // ignore the error - like in the original code
	}
	servlet.init(servletConfig);
	// if an exception is thrown in init, no end interceptors will be called.
	// that was in the origianl code

	for( int i=cI.length-1; i>=0; i-- ) {
	    cI[i].postServletInit( context, this ); // ignore the error - like in the original code
	}
	// 	for( int i=v.size()-1; i>=0 ; i-- ) {
	// 	    try { 
	// 		((LifecycleInterceptor)v.elementAt(i)).postInvoke( context, servlet );
	// 	    } catch(InterceptorException ex ) {
	// 		ex.printStackTrace();
	// 	    }
	// 	}
    }

    /** Call destroy(), with all interceptors before and after in the
	right order;
    */
    protected void handleDestroy(Context context, Servlet servlet )
	throws ServletException, IOException
    {
	// 	Vector v=context.getDestroyInterceptors();
	// 	for( int i=0; i<v.size(); i++ ) {
	// 	    try { 
	// 		((LifecycleInterceptor)v.elementAt(i)).preInvoke( context, servlet );
	// 	    } catch(InterceptorException ex ) {
	// 		ex.printStackTrace();
	// 	    }
	// 	}
	ContextInterceptor cI[]=context.getContextInterceptors();
	for( int i=0; i<cI.length; i++ ) {
	    cI[i].preServletDestroy( context, this ); // ignore the error - like in the original code
	}
	servlet.destroy();
	for( int i=cI.length-1; i>=0; i-- ) {
	    cI[i].postServletDestroy( context, this ); // ignore the error - like in the original code
	}
	// if an exception is thrown in init, no end interceptors will be called.
	// that was in the origianl code

	// 	for( int i=v.size()-1; i>=0 ; i-- ) {
	// 	    try { 
	// 		((LifecycleInterceptor)v.elementAt(i)).postInvoke( context, servlet );
	// 	    } catch(InterceptorException ex ) {
	// 		ex.printStackTrace();
	// 	    }
	// 	}
    }
    

    /** Call service(), with all interceptors before and after in the
	right order;
    */
    protected void handleInvocation(Context ctx, Servlet servlet,
				  HttpServletRequestFacade request, HttpServletResponseFacade response )
	throws ServletException, IOException
    {
	// XXX pass Request and Response as paramters - all tomcat code use this pattern,
	// we need to use Facade only when we call the Servlet methods
	
	// 	Vector v = context.getServiceInterceptors();
	// 	for( int i=0; i<v.size(); i++ ) {
	// 	    try { 
	// 		((ServiceInterceptor)v.elementAt(i)).preInvoke(context, servlet,
	// 								  request, response);
	// 	    } catch(InterceptorException ex ) {
	// 		ex.printStackTrace();
	// 	    }
	// 	}
	RequestInterceptor cI[]=context.getRequestInterceptors();
	for( int i=0; i<cI.length; i++ ) {
	    cI[i].preService( request.getRealRequest(), response.getRealResponse() ); // ignore the error - like in the original code
	}
	
	if (servlet instanceof SingleThreadModel) {
	    synchronized(servlet) {
		servlet.service(request, response);
	    }
	} else {
	    servlet.service(request, response);
	}

	for( int i=cI.length-1; i>=0; i-- ) {
	    cI[i].postService( request.getRealRequest() , response.getRealResponse() ); // ignore the error - like in the original code
	}
	// 	for( int i=v.size()-1; i>=0 ; i-- ) {
	// 	    try { 	
	// 		((ServiceInterceptor)v.elementAt(i)).postInvoke(context, servlet,
	// 								  request, response);
	// 	    } catch(InterceptorException ex ) {
	// 		ex.printStackTrace();
	// 	    }
	// 	}
    }

    // Fancy sync logic is to make sure that no threads are in the
    // handlerequest when this is called and, furthermore, that
    // no threads go through handle request after this method starts!
    protected void waitForDestroy() {
	// Wait until there are no outstanding service calls,
	// or until 30 seconds have passed (to avoid a hang)
	
	// XXX wrong logic !
	while (serviceCount > 0) {
	    try {
		wait(30000);
		
		break;
	    } catch (InterruptedException e) { }
	}
    }

    public String toString() {
	String toS="Wrapper(" + config.getServletName() + " ";
	if( servlet!=null ) toS=toS+ "S:" + servlet.getClass().getName();
	else  toS= toS + servletClassName;
	return toS + ")";
    }

}
