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
package org.apache.tomcat.facade;

import org.apache.tomcat.core.*;
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
public class ServletWrapper extends Handler {

    protected Class servletClass;

    protected Servlet servlet;

    // facade
    protected ServletConfig configF;

    //     // Jsp pages
    //     private String path = null;

    // optional informations
    protected String description = null;

    // If init() fails, Handler.errorException will hold the reason.
    // In the case of an UnavailableException, this field will hold
    // the expiration time if UnavailableException is not permanent.
    long unavailableTime=-1;
    
    // Usefull info for class reloading
    protected boolean isReloadable = false;
    // information + make sure destroy is called when no other servlet
    // is running ( this have to be revisited !) 
    protected long lastAccessed;
    protected int serviceCount = 0;
    
    //    int loadOnStartup=0;

    Hashtable securityRoleRefs=new Hashtable();

    public ServletWrapper() {
    }

    public void setContext( Context context) {
	super.setContext( context );
	isReloadable=context.getReloadable();
        configF = new ServletConfigImpl(this);
    }

    public String toString() {
	return name + "(" + servletClassName + "/" + path + ")";
    }
    
    // -------------------- Servlet specific properties 
    public void setLoadOnStartUp( int level ) {
	loadOnStartup=level;
	// here setting a level implies loading
	loadingOnStartup=true;
    }

    public void setLoadOnStartUp( String level ) {
	if (level.length() > 0)
	    loadOnStartup=new Integer(level).intValue();
	else
	    loadOnStartup=-1;
	// here setting a level implies loading
	loadingOnStartup=true;
    }

    void setReloadable(boolean reloadable) {
	isReloadable = reloadable;
    }

    public String getName() {
	return getServletName();
    }
    
    public String getServletName() {
	if(name!=null) return name;
	return path;
    }

    public void setServletName(String servletName) {
        this.servletName=servletName;
	name=servletName;
    }

    public String getServletDescription() {
        return this.description;
    }

    public void setDescription( String d ) {
	description=d;
    }
    
    public void setServletDescription(String description) {
        this.description = description;
    }

    public String getServletClass() {
        return this.servletClassName;
    }

    public String getServletClassName() {
        return this.servletClassName;
    }

    public void setServletClass(String servletClassName) {
	super.setServletClass( servletClassName );
	servlet=null; // reset the servlet, if it was set
	servletClass=null;
    }

    public Exception getErrorException() {
	Exception ex = super.getErrorException();
	if ( ex != null ) {
	    if ( ex instanceof UnavailableException &&
		    ! ((UnavailableException)ex).isPermanent()) {
		// make updated UnavailableException, reporting a minimum of 1 second
		int secs=1;
		long moreWaitTime=unavailableTime - System.currentTimeMillis();
		if( moreWaitTime > 0 )
		    secs = (int)((moreWaitTime + 999) / 1000);
		ex = new UnavailableException(ex.getMessage(), secs);
	    }
	}
	return ex;
    }


    public void reload() {
	if( initialized ) {
	    try {
		destroy();
	    } catch(Exception ex ) {
		log( "Error in destroy ", ex );
	    }
	}

	if( servletClassName != null ) {
	    // I can survive reload
	    servlet=null;
	    servletClass=null;
	}
	initialized=false;
    }
    
    /** Security Role Ref represent a mapping between servlet role names and
     *  server roles
     */
    public void addSecurityMapping( String name, String role,
				    String description ) {
	securityRoleRefs.put( name, role );
    }

    public String getSecurityRole( String name ) {
	return (String)securityRoleRefs.get( name );
    }

    // -------------------- Jsp specific code
    
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // -------------------- 

    public Servlet getServlet() {
	if(servlet==null) {
	    try {
		loadServlet();
	    } 	catch( Exception ex ) {
		log("in loadServlet()", ex);
	    }
	}
	return servlet;
    }

    protected void doDestroy() throws TomcatException {
	synchronized (this) {
	    // Fancy sync logic is to make sure that no threads are in the
	    // handlerequest when this is called and, furthermore, that
	    // no threads go through handle request after this method starts!
	    // Wait until there are no outstanding service calls,
	    // or until 30 seconds have passed (to avoid a hang)
		
	    //XXX I don't think it works ( costin )
	    
	    // XXX Move it to an interceptor!!!!
	    while (serviceCount > 0) {
		try {
		    wait(30000);
		    break;
		} catch (InterruptedException e) { }
	    }

	    try {
		if( servlet!=null) {
		    BaseInterceptor cI[]=context.
			getContainer().getInterceptors();
		    for( int i=0; i< cI.length; i++ ) {
			try {
			    cI[i].preServletDestroy( context, this );
			} catch( TomcatException ex) {
			    log("preServletDestroy", ex);
			}
		    }
		    servlet.destroy();

		    for( int i=0; i< cI.length; i++ ) {
			try {
			    cI[i].postServletDestroy( context, this );
			} catch( TomcatException ex) {
			    log("postServletDestroy", ex);
			}
		    }
		}
	    } catch(Exception ex) {
		// Should never come here...
		log( "Error in destroy ", ex );
	    }
	}
    }

    /** Load and init a the servlet pointed by this wrapper
     */
    private void loadServlet()
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException
    {
	// XXX Move this to an interceptor, so it will be configurable.
	// ( and easier to read )
	// 	log("LoadServlet " + servletClass + " "
	// 			   + servletClassName);
	if (servletClass == null) {
	    if (servletClassName == null) {
		throw new IllegalStateException("Can't happen - classname "
						+ "is null, who added this ?");
	    }
	    servletClass=context.getClassLoader().loadClass(servletClassName);
	}
	
	servlet = (Servlet)servletClass.newInstance();

	// hack for internal servlets
	if( ! servletClassName.startsWith("org.apache.tomcat") ) return;
    }

    /** Override Handler's init - load the servlet before calling
	and interceptor
    */
    public void init()
    	throws Exception
   {
	// if initialized, then we were sync blocked when first init() succeeded
	if( initialized ) return;
	// if exception present, then we were sync blocked when first init() failed
	// or an interceptor set an inital exeception
	// in the latter case, preServletInit() and postServletInit() interceptors
	// don't get called
	if ( isExceptionPresent() ) return;

       // make sure the servlet is loaded before calling preInit
	// Jsp case - maybe another Jsp engine is used
	if( servlet==null && path != null &&  servletClassName == null) {
	    log("Calling handleJspInit " + servletClassName);
	    handleJspInit();
	}

	if( servlet==null ) {
	    // try to load servlet, save exception if one occurs
	    try {
		loadServlet();
	    } catch ( Exception ex ) {
		// save init exception
		setErrorException( ex );
		setExceptionPermanent( true );
		return;
	    }
	}

	// Call pre, doInit and post
	BaseInterceptor cI[]=context.getContainer().getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    try {
		cI[i].preServletInit( context, this );
	    } catch( TomcatException ex) {
		log("preServletInit" , ex);
	    }
	}

	doInit();

	// doInit() catches exceptions, so post interceptors are always called	
	for( int i=0; i< cI.length; i++ ) {
	    try {
		cI[i].postServletInit( context, this );
	    } catch( TomcatException ex) {
		log("postServletInit" , ex);
	    }
	}
    }

    protected void doInit()
	throws Exception
    {
	// ASSERT synchronized at higher level, initialized must be false
	try {
	    final Servlet sinstance = servlet;
	    final ServletConfig servletConfig = configF;
	    servlet.init(servletConfig);
	    initialized=true;
	} catch( UnavailableException ex ) {
	    setServletUnavailable( ex );
	    servlet=null;
	} catch( Exception ex ) {
	    // other init exceptions are treated as permanent
	    // XXX need a context setting for unavailable time
	    setErrorException(ex);
	    setExceptionPermanent(true);
	    servlet=null;
	}
    }

    /** Override service to hook reloading - it can be done in a clean
	interceptor. It also hooks jsp - we should have a separate
	JspHandler
    */
    public void service(Request req, Response res) 
	throws Exception
    {
	// <servlet><jsp-file> case
	if( path!=null ) {
	    if( path.startsWith("/"))
		req.setAttribute( "javax.servlet.include.request_uri",
				  req.getContext().getPath()  + path );
	    else
		req.setAttribute( "javax.servlet.include.request_uri",
				  req.getContext().getPath()  + "/" + path );
	    req.setAttribute( "javax.servlet.include.servlet_path", path );
	}

	// if servlet is not available
	if ( isExceptionPresent() ) {
	    // get if error has expired
	    checkErrorExpired( req, res );
	    // if we have an error on this request
	    if ( req.isExceptionPresent()) {
		// if in included, defer handling to higher level
		if ( res.isIncluded() )
		    return;
		// otherwise handle error
		contextM.handleError( req, res, getErrorException());
	    }
	    context.log(getServletName() +
			" unavailable time expired, trying again ");
	}

	// we reach here of there is no error or the exception has expired
	// will do an init
	super.service( req, res );
    }

    protected void doService(Request req, Response res)
	throws Exception
    {
	// Get facades - each req have one facade per context
	// the facade itself is very light.

	// For big servers ( with >100s of webapps ) we can
	// use a pool or other technique. Right now there
	// are many other ( much more expensive ) resources
	// associated with contexts ( like the session thread)

	// XXX
	HttpServletRequest reqF= (HttpServletRequest)req.getFacade();
	HttpServletResponse resF= (HttpServletResponse)res.getFacade();
	if( reqF == null || resF == null ||
	    ! (reqF instanceof HttpServletRequestFacade) ) {
	    reqF=new HttpServletRequestFacade(req);
	    resF=new HttpServletResponseFacade(res);
	    req.setFacade( reqF );
	    res.setFacade( resF );
	}
	
	try {
	    doService( reqF, resF );
	} catch ( Exception ex ) {
	    if ( ex instanceof UnavailableException ) {
		// if error not set
		if ( ! isExceptionPresent() ) {
		    synchronized(this) {
			if ( ! isExceptionPresent() ) {
			    setServletUnavailable( (UnavailableException)ex );
			    // XXX if the UnavailableException is permanent we are supposed
			    // to destroy the servlet.  Synchronization of this destruction
			    // needs review before adding this.
			}
		    }
		}
	    }
	    // save error state on request and response
	    saveError( req, res, ex );
	}
    }

    protected void doService(HttpServletRequest req, HttpServletResponse res)
	throws Exception
    {
	// We are initialized and fine
	if (servlet instanceof SingleThreadModel) {
	    synchronized(servlet) {
		servlet.service(req, res);
	    }
	} else {
	    servlet.service(req, res);
	}
    }

    // -------------------- Jsp hooks
    // <servlet><jsp-file> case - we know it's a jsp
    void handleJspInit() {
	// XXX Jsp Servlet is initialized, the servlet is not generated -
	// we can't hook in! It's jspServet that has to pass the config -
	// but it can't so easily, plus  it'll have to hook in.
	// I don't think that ever worked anyway - and I don't think
	// it can work without integrating Jsp handling into tomcat
	// ( using interceptor )
	ServletWrapper jspServletW = (ServletWrapper)context.getServletByName("jsp");
	servletClassName = jspServletW.getServletClass();
    }

    // -------------------- Utility methods --------------------

    private void setServletUnavailable( UnavailableException ex ) {
	// servlet exception state
	setErrorException( ex );
	if ( ex.isPermanent() ) {
	    setExceptionPermanent( true );
	} else {
	    setExceptionPermanent( false );
	    unavailableTime=System.currentTimeMillis();
	    unavailableTime += ex.getUnavailableSeconds() * 1000;
	}
    }

    /** Check if error exception is present and if so, has the error
	expired.  Sets error on request and response if un-expired
	error found.
     */

    private void checkErrorExpired( Request req, Response res ) {
	// synchronize so another request can't expire the error
	synchronized(this) {
	    // if error still present
	    if ( isExceptionPresent() ) {
		// if permanent exception or timer not expired
		if ( isExceptionPermanent() ||
			(unavailableTime - System.currentTimeMillis()) > 0) {
		    // save error state on request and response
		    saveError( req, res, getErrorException() );
		} else {
		    // we can try again
		    setErrorException(null);
		    setExceptionPermanent(false);
		    unavailableTime=-1;
		}
	    }
	}
    }

}
