/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ServletWrapper.java,v 1.53 2000/06/16 17:58:56 costin Exp $
 * $Revision: 1.53 $
 * $Date: 2000/06/16 17:58:56 $
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

import org.apache.tomcat.facade.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.servlets.TomcatInternalServlet;
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
    protected String servletName;
    protected String servletClassName; // required
    protected Servlet servlet;
    protected Class servletClass;

    protected ServletConfig configF;

    // Jsp pages
    private String path = null;

    // optional informations
    protected String description = null;

    boolean initialized=false;
    // If init() fails, this will keep the reason.
    // init may be called when the servlet starts, but we need to
    // report the error later, to the client
    Exception unavailable=null;
    long unavailableTime=-1;
    
    // Usefull info for class reloading
    protected boolean isReloadable = false;
    // information + make sure destroy is called when no other servlet
    // is running ( this have to be revisited !) 
    protected long lastAccessed;
    protected int serviceCount = 0;

    int loadOnStartup=0;

    Hashtable initArgs=null;
    Hashtable securityRoleRefs=new Hashtable();

    /** The servlet was declared in web.xml
     */
    public static final int ORIGIN_WEB_XML=0;
    public static final int ORIGIN_INVOKER=1;
    public static final int ORIGIN_JSP=2;
    /** any tomcat-specific component that can
	register mappings that are "re-generable",
	i.e. can be recreated - the mapping can
	safely be removed. Jsp and invoker are particular
	cases
    */
    public static final int ORIGIN_DYNAMIC=3;
    /** The servlet was added by the admin, it should be safed
	preferably in web.xml
    */
    public static final int ORIGIN_ADMIN=4;
    
    // who creates the servlet definition
    int origin;

    public ServletWrapper() {
    }

    ServletWrapper(Context context) {
	setContext( context );
    }

    public void setContext( Context context) {
        this.context = context;
	contextM=context.getContextManager();
	isReloadable=context.getReloadable();
        configF = context.getFacadeManager().createServletConfig( this );
    }

    public Context getContext() {
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
	if( servletName != null ) return servletName;
	if( servletClassName != null ) return servletClassName;
	return path;
    }

    public void setServletName(String servletName) {
        this.servletName=servletName;
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

    // XXX Doesn't seem to be used, shouldn't be part of interface -
    // use init and service !!! 
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
	}
	initArgs.put( name, value );
    }

    public String getInitParameter(String name) {
	if (initArgs != null) {
            return (String)initArgs.get(name);
        } else {
            return null;
        }
    }

    public Enumeration getInitParameterNames() {
        if (initArgs != null) {
            return initArgs.keys();
        } else {
            // dirty hack to return an empty enumeration
            Vector v = new Vector();
            return v.elements();
        }
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

		// XXX Move it to an interceptor!!!!
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
			    context.log( "Error in preServletDestroy " + cI, ex );
			}
		    }
		    // XXX post will not be called if any error happens in destroy. That's
		    // how tomcat worked before - I think it's a bug !
		    servlet.destroy();
		    for( int i=cI.length-1; i>=0; i-- ) {
			try {
			    cI[i].postServletDestroy( context, this ); // ignore the error - like in the original code
			} catch( TomcatException ex) {
			    context.log( "Error in postServletDestroy " + cI, ex );
			}
			
		    }
		} catch(Exception ex) {
		    // Should never come here...
		    context.log( "Error in destroy ", ex );
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
	// Jsp case - maybe another Jsp engine is used
	if( servlet==null && path != null ) {
	    handleJspInit();
	}
	// XXX Move this to an interceptor, so it will be configurable.
	// ( and easier to read )
	if (servletClass == null) {
	    if (servletClassName == null) {
		context.log( "Invalid servlet - class and class name are null " + servletName);
		throw new IllegalStateException(sm.getString("wrapper.load.noclassname"));
	    }
	    
	    servletClass=context.getServletLoader().loadClass( servletClassName);
	}
	
	if( servletClass==null ) throw new ServletException("Error loading servlet " + servletClassName );
	servlet = (Servlet)servletClass.newInstance();
	if( servlet==null ) throw new ServletException("Error insantiating servlet "  + servletClassName );
	//	System.out.println("Loading " + servletClassName + " " + servlet );
    }
    
    public void initServlet()
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, ServletException
    {
	if( servlet==null ) loadServlet();

	    checkInternal(servlet, servletClassName);

	    try {
		if( initialized ) return;
		
		final Servlet sinstance = servlet;
		final ServletConfig servletConfig = configF;
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
		// XXX I think it's a bug ( costin )
		
		for( int i=cI.length-1; i>=0; i-- ) {
		    try {
			cI[i].postServletInit( context, this ); // ignore the error - like in the original code
		    } catch( TomcatException ex) {
			    ex.printStackTrace();
		    }
		    
		}
		initialized=true;
		// successfull initialization
		unavailable=null;
	    } catch( UnavailableException ex ) {
		unavailable=ex;
		unavailableTime=System.currentTimeMillis();
		unavailableTime += ex.getUnavailableSeconds() * 1000;
	    } catch( Exception ex ) {
		unavailable=ex;
	    }
    }

    private void checkInternal( Servlet s, String servletClassName ) {
	if( ! servletClassName.startsWith("org.apache.tomcat") ) return;
	if( s instanceof TomcatInternalServlet ) {
	    ((TomcatInternalServlet)s).setFacadeManager( context.getFacadeManager());
	}
    }
    
    // XXX Move it to interceptor - so it can be customized
    // Reloading
    // XXX ugly - should find a better way to deal with invoker
    // The problem is that we are just clearing up invoker, not
    // the class loaded by invoker.
    void handleReload(Request req) throws TomcatException {
		// That will be reolved after we reset the context - and many
		// other conflicts.
		if( isReloadable && ! "invoker".equals( getServletName())) {
			ServletLoader loader=context.getServletLoader();
			if( loader!=null) {
				// XXX no need to check after we remove the old loader
				if( loader.shouldReload() ) {
					// workaround for destroy 
					destroy();
					initialized=false;
					loader.reload();
					
					ContextManager cm=context.getContextManager();
					cm.doReload( req, context );
					
					servlet=null;
					servletClass=null;
					/* Initial attempt to shut down the context and sessions.

					String path=context.getPath();
					String docBase=context.getDocBase();
					// XXX all other properties need to be saved or something else
					ContextManager cm=context.getContextManager();
					cm.removeContext(path);
					Context ctx=new Context();
					ctx.setPath( path );
					ctx.setDocBase( docBase );
					cm.addContext( ctx );
					context=ctx;
					// XXX shut down context, remove sessions, etc
					*/
				}
			}
		}
    }
    
    // <servlet><jsp-file> case - we know it's a jsp
    void handleJspInit() {

	// XXX Jsp Servlet is initialized, the servlet is not generated - we can't hook in!
	// It's jspServet that has to pass the config - but it can't so easily, plus
	// it'll have to hook in.
	// I don't think that ever worked anyway - and I don't think it can work without
	// integrating Jsp handling into tomcat ( using interceptor )
	
	ServletWrapper jspServletW = context.getServletByName("jsp");
	servletClassName = jspServletW.getServletClass();
    }
    
    /** Check if we can try again an init
     */
    private boolean stillUnavailable() {
	// we have a timer - maybe we can try again - how much
	// do we have to wait - (in mSec)
	long moreWaitTime=unavailableTime - System.currentTimeMillis();
	if( unavailableTime > 0 && ( moreWaitTime < 0 )) {
	    // we can try again
	    unavailable=null;
	    unavailableTime=-1;
	    context.log(getServletName() + " unavailable time expired, try again ");
	    return false;
	} else {
	    return true;
	}
    }
    
    /** Send 503. Should be moved in ErrorHandling
     */
    private void handleUnavailable( Request req, Response res ) {
	if( unavailable instanceof UnavailableException ) {
	    int unavailableTime = ((UnavailableException)unavailable).getUnavailableSeconds();
	    if( unavailableTime > 0 ) {
		res.setHeader("Retry-After", Integer.toString(unavailableTime));
	    }
	}

	String msg=unavailable.getMessage();
	long moreWaitTime=unavailableTime - System.currentTimeMillis();
	context.log( "Error in " + getServletName() + "init(), error happened at " +
		     unavailableTime + " wait " + moreWaitTime + " : " + msg, unavailable);
	req.setAttribute("javax.servlet.error.message", msg );
	res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
	contextM.handleStatus( req, res,  HttpServletResponse.SC_SERVICE_UNAVAILABLE );
	return;
    }

    private void handleNotFound( Request req, Response res) {
	context.log( "Can't find servet " + getServletName() + " " + getServletClass() );
	res.setStatus( 404 );
	contextM.handleStatus( req, res,  404 );
    }

    public void handleRequest(Request req, Response res) 
    {
	try {
	    handleReload(req);
	} catch( TomcatException ex ) {
	    ex.printStackTrace();// what to do ?
	}

	// <servlet><jsp-file> case
	if( path!=null ) {
	    req.setAttribute( "javax.servlet.include.request_uri", path );
	}
	
	// Jsp case - if servlet == null init will take care 
	// Special case - we're not ready to run
	if( ! initialized || servlet == null || unavailable!=null  ) {
	    // Don't load if Unavailable timer is in place
	    if(  unavailable != null && stillUnavailable() ) {
		handleUnavailable( req, res );
		return;
	    }
	    
	    // if multiple threads will call the same servlet at once,
	    // we should have only one init 
	    synchronized( this ) {
		// init - only if unavailable was null or unavailable period expired
		try {
		    initServlet();
		} catch(ClassNotFoundException ex ) {
		    handleNotFound( req, res );
		    return;
		} catch( Exception ex ) {
		    context.log("Exception in init servlet " + ex.getMessage(), ex );
		    // any other exception will be set in unavailable - no need to do anything
		}
	    }

	    if( servlet == null ) {
		handleNotFound( req, res );
		return;
	    }

	    // If servlet was not initialized
	    if( unavailable!=null ) {
		handleUnavailable(req, res);
		return;
	    }
	}
	    
	// We are initialized and fine
	try {
	    RequestInterceptor cI[]=context.getRequestInterceptors();
	    for( int i=0; i<cI.length; i++ ) {
		cI[i].preService( req, res ); // ignore the error - like in the original code
	    }

	    if (servlet instanceof SingleThreadModel) {
		synchronized(servlet) {
		    servlet.service(req.getFacade(), res.getFacade());
		}
	    } else {
		servlet.service(req.getFacade(), res.getFacade());
	    }
	    
	    for( int i=cI.length-1; i>=0; i-- ) {
		cI[i].postService( req , res ); // ignore the error - like in the original code
	    }
	}  catch( Throwable t ) {
	    if( t instanceof IOException ) {
		if( ((IOException)t).getMessage().equals("Broken pipe"))
		    return;
		System.out.println("XXX XXX " + t.getMessage());
	    }
	    
	    if( null!=req.getAttribute("tomcat.servlet.error.defaultHandler")) {
		// we are in handleRequest for the "default" error handler
		System.out.println("ERROR: can't find default error handler "+
				   "or error in default error page");
		t.printStackTrace();
	    } else {
		String msg=t.getMessage();
		context.log( "Error in " + getServletName() +
			     " service() : " + msg, t);
		// XXX XXX Security - we should log the message, but nothing
		// should show up  to the user - it gives up information
		// about the internal system !
		// Developers can/should use the logs !!!
		contextM.handleError( req, res, t );
	    }
	} 
    }
    
    public String toString() {
	StringBuffer sb=new StringBuffer();
	sb.append("<servlet n=").append( getServletName());
	if( servlet!=null ) sb.append( " sc=").append(servlet.getClass().getName());
	else  sb.append(" c=").append(servletClassName);
	sb.append(">");
	return sb.toString();
    }

    /** Who created this servlet definition - default is 0, i.e. the
	web.xml mapping. It can also be the Invoker, the admin ( by using a
	web interface), JSP engine or something else.
	
	Tomcat can do special actions - for example remove non-used
	mappings if the source is the invoker or a similar component
    */
    public void setOrigin( int origin ) {
	this.origin=origin;
    }

    public int getOrigin() {
	return origin;
    }

    public void setDescription( String d ) {
	description=d;
    }
    
    // -------------------- Accounting --------------------

    /** ServletWrapper counts. The accounting desing is not
	final, but all this is needed to tune up tomcat
	( and to understand and be able to implement a good
	solution )
    */
    public static final int ACC_LAST_ACCESSED=0;
    public static final int ACC_INVOCATION_COUNT=1;
    public static final int ACC_SERVICE_TIME=2;
    public static final int ACC_ERRORS=3;
    public static final int ACC_OVERHEAD=4;
    public static final int ACC_IN_INCLUDE=5;
    
    public static final int ACCOUNTS=6;
    long accTable[]=new long[ACCOUNTS];

    public void setAccount( int pos, long value ) {
	accTable[pos]=value;
    }

    public long getAccount( int pos ) {
	return accTable[pos];
    }

}
