/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.generators;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Locale;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.net.URL;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.qlog.Logger;
import org.apache.tomcat.util.res.StringManager;

/**
 * Handle errors - this is the default handler, you can replace it
 * with customized versions
 *
 * @author Costin Manolache
 */
public final class ErrorHandler extends BaseInterceptor {
    private Context rootContext=null;
    boolean showDebugInfo=true;
    int defaultRedirectStatus=301;
    private String charset = null;
    
    public ErrorHandler() {
    }

    /**
     * Set the charset to use for error page generation.
     */
    public void setUseCharset(String ucs) {
	charset = ucs;
    }

    /**
     * Get the charset to use for error page generation.
     */
    public String getUseCharset() {
	return charset;
    }

    public void setShowDebugInfo( boolean b ) {
	showDebugInfo=b;
    }

    public void setDefaultRedirectStatus( String s ) {
        if( "302".equals(s) )
            defaultRedirectStatus=302;
        else if( "301".equals(s) )
            defaultRedirectStatus=301;
        else
            defaultRedirectStatus=301;
    }

    public void engineInit(ContextManager cm ) {
    }

    /** Check that we are in a stable state.
     */
    public  void engineStart(ContextManager cm )
	throws TomcatException
    {
	/* It is very possible to configure Tomcat without a rootContext.
	   We make certain here that the rootContext is set.  Note that we
	   can't add the context, since we don't have a docRoot.  This one is 
	   only used for error handling.  If somebody subsequently adds a
	   default context, then this one just harmlessly goes to gc 
	   (since it's not part of the app, we don't have to follow Life Cycle)
	*/
	if(rootContext == null){
	    rootContext = cm.createContext();
	    rootContext.setContextManager(cm);
	    rootContext.setPath("");
	    contextInit(rootContext);
	}
    }
    
    /** Add default error handlers
     */
    public void contextInit( Context ctx)
	throws TomcatException
    {
	if( ctx.getHost() == null && ctx.getPath().equals(""))
	    rootContext = ctx;

	ContextManager cm=ctx.getContextManager();
	String dI=cm.getProperty( "showDebugInfo" );
	if( dI!=null && ( dI.equalsIgnoreCase("no") ||
			  dI.equalsIgnoreCase("false"))) {
	    showDebugInfo=false;
	}

	// override with per/context setting
	dI=ctx.getProperty( "showDebugInfo" );
	if( dI!=null && ( dI.equalsIgnoreCase("no") ||
			  dI.equalsIgnoreCase("false"))) {
	    showDebugInfo=false;
	}
	if( dI!=null && ( dI.equalsIgnoreCase("yes") ||
			  dI.equalsIgnoreCase("true"))) {
	    showDebugInfo=true;
	}
	
	ctx.addServlet( new ExceptionHandler(this, showDebugInfo));
	ctx.addServlet( new StatusHandler(this, showDebugInfo));

	// Default status handlers
        // Assume existing error pages are valid.  Don't overwrite with default.
        RedirectHandler rh = new RedirectHandler(this);
        rh.setDefaultRedirectStatus(defaultRedirectStatus);
	ctx.addServlet( rh );
        if (ctx.getErrorPage("302") == null)
            ctx.addErrorPage( "302", "tomcat.redirectHandler");
        if (ctx.getErrorPage("301") == null)
            ctx.addErrorPage( "301", "tomcat.redirectHandler");
	ctx.addServlet( new NotFoundHandler(this, showDebugInfo));
        if (ctx.getErrorPage("404") == null)
            ctx.addErrorPage( "404", "tomcat.notFoundHandler");

	if( debug > 0 ) log( "Init " + ctx + " " + showDebugInfo);
    }

    public int handleError( Request req, Response res, Throwable t ) {
	ContextManager cm=req.getContextManager();
	Context ctx = req.getContext();
	if(ctx==null) {
	    // that happens only if the request can't pass contextMap
	    // hook. The reason for that is a malformed request, or any
	    // other error.
	    ctx=rootContext;
	}

	if( debug > 0 ) log( "In error handler "  +t );
	if( t==null ) {
	    handleStatusImpl( cm, ctx, req, res, res.getStatus() );
	} else {
	    handleErrorImpl( cm, ctx, req, res, t );
	}
	return 200;
    }

    // -------------------- Implementation of handleError
    // Originally in ContextManager.
    
    private final void handleStatusImpl( ContextManager cm, Context ctx,
					 Request req, Response res,
					 int code )
    {
	String errorPath=null;
	Handler errorServlet=null;

	// don't log normal cases ( redirect and need_auth ), they are not
	// error
	// XXX this log was intended to debug the status code generation.
	// it can be removed for all cases.
	if( code > 401 ) {// tuneme
	    if( ctx==null )
		cm.log( "Status code:" + code + " request:"  + req + " msg:" +
		     req.getAttribute("javax.servlet.error.message"));
	    else
		ctx.log( "Status code:" + code + " request:"  + req + " msg:" +
		     req.getAttribute("javax.servlet.error.message"));
	}
	
	errorPath = ctx.getErrorPage( code );
	if( errorPath != null ) {
	    errorServlet=getHandlerForPath( cm, ctx, errorPath );

	    String cpath=ctx.getPath();
	    if( "/".equals(cpath))  cpath="";
	    
	    // Make sure Jsps will work - needed if the error page is a jsp
	    if ( null!=errorPath && errorPath.startsWith("/") ) {
		req.setAttribute( "javax.servlet.include.request_uri",
				  cpath  + errorPath );
	    } else {
		req.setAttribute( "javax.servlet.include.request_uri",
				  cpath  + "/" + errorPath );
	    }
	    req.setAttribute( "javax.servlet.include.servlet_path", errorPath );
	}

	boolean isDefaultHandler = false;
        if ( statusLoop( ctx, req, code ) ){
            log( "Error loop for " + req + " error code " + code);
            return;
        }
	if( errorServlet==null ) {
            if( code == 404 )
                errorServlet=ctx.getServletByName( "tomcat.notFoundHandler");
            else
                errorServlet=ctx.getServletByName( "tomcat.statusHandler");
	    isDefaultHandler = true;
	}

	if (errorServlet == null) {
	    ctx.log( "Handler errorServlet is null! errorPath:" + errorPath);
	    return;
	}

	// XXX The original code didn't reset the buffer if
	// isDefaultHandler :	if (!isDefaultHandler && ...
	// Is there any reason for that ?
	// I also think we should reset the buffer anyway, to get
	// in a stable state - even if the buffer is commited
	if ( !res.isBufferCommitted())
	    res.resetBuffer();

	req.setAttribute("javax.servlet.error.status_code",new Integer( code));
	req.setAttribute("tomcat.servlet.error.request", req);

	if( debug>0 )
	    ctx.log( "Handler " + errorServlet + " " + errorPath);

	// reset error exception
	res.setErrorException( null );
	Exception ex=null;
	try {
	    errorServlet.service( req, res );
	    ex=res.getErrorException();
	} catch (Exception ex1 ) {
	    ex=ex1;
	}
	if( ex!=null && ! (ex instanceof IOException) ) {
	    // we can ignore IOException - probably the user
	    // has clicked "STOP"
	    // we need to log any other error - something may be
	    // broken if the error servlet has errors.
	    ctx.log( "Error in default status handler", ex);
	} 
    }

    // XXX XXX Security - we should log the message, but nothing
    // should show up  to the user - it gives up information
    // about the internal system !
    // Developers can/should use the logs !!!

    /** General error handling mechanism. It will try to find an error handler
     * or use the default handler.
     */
    void handleErrorImpl( ContextManager cm, Context ctx,
			  Request req, Response res , Throwable t  )
    {
	if( debug>0 )
	    log( "Handle error in " + req + " " + t.getMessage() );
	
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
		ctx.log("Broken pipe in " + req, t, Log.DEBUG);  // tuneme
		return;
	    }
            if( "Connection reset by peer".equals(t.getMessage()))
	    {
		ctx.log("Connection reset by peer in " + req, t, Log.DEBUG);  // tuneme
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

	// Bug 3233, ps@psncc.at (Peter Stamfest)
	if (errorPath == null ) {
	    // Use introspection - the error handler is at a lower level,
	    // doesn't depend on servlet api
	    Throwable t2=null;
	    try {
		Method m=t.getClass().getMethod( "getRootCause", new Class[] {} );
		t2 = (Throwable)m.invoke( t, new Object[] {} );
	    } catch(Exception ex) {
	    }

	    if (t2 != null) {
		clazz = t2.getClass();
		while (errorPath == null && clazz != null) {
		    String name = clazz.getName();
		    errorPath = ctx.getErrorPage(name);
		    clazz = clazz.getSuperclass();
		}
	    }
	    if (errorPath != null) t = t2;
	}

	if( errorPath != null ) {
	    errorServlet=getHandlerForPath( cm, ctx, errorPath );

	    String cpath=ctx.getPath();
	    if( "/".equals( cpath ))  cpath="";
	    
	    // Make sure Jsps will work - needed if the error page is a jsp
	    if ( null!=errorPath && errorPath.startsWith("/") ) {
		req.setAttribute( "javax.servlet.include.request_uri",
				  cpath  + errorPath );
	    } else {
		req.setAttribute( "javax.servlet.include.request_uri",
				  cpath  + "/" + errorPath );
	    }
	    req.setAttribute( "javax.servlet.include.servlet_path", errorPath );
	}

	boolean isDefaultHandler = false;
	if ( errorLoop( ctx, req ) ){
	    log( "Error loop for " + req + " error " + t);
	    return;
        }
        if ( errorServlet==null) {
	    errorServlet = ctx.getServletByName("tomcat.exceptionHandler");
	    isDefaultHandler = true;
	    if( debug>0 ) ctx.log( "Using default handler " + errorServlet );
	}

	if (errorServlet == null) {
	    ctx.log( "Handler errorServlet is null! errorPath:" + errorPath);
	    return;
	}


	// XXX The original code didn't reset the buffer if
	// isDefaultHandler :	if (!isDefaultHandler && ...
	// Is there any reason for that ?
	// I also think we should reset the buffer anyway, to get
	// in a stable state - even if the buffer is commited
	if ( !res.isBufferCommitted())
	    res.resetBuffer();

	req.setAttribute("javax.servlet.error.exception_type", t.getClass());
	req.setAttribute("javax.servlet.error.message", t.getMessage());
	req.setAttribute("javax.servlet.jsp.jspException", t);
	req.setAttribute("tomcat.servlet.error.throwable", t);
	req.setAttribute("tomcat.servlet.error.request", req);

	if( debug>0 )
	    ctx.log( "Handler " + errorServlet + " " + errorPath);

	// reset error exception
	res.setErrorException( null );
	Exception ex=null;
	try {
	    errorServlet.service( req, res );
	    ex=res.getErrorException();
	} catch(Exception ex1 ) {
	    ex=ex1;
	}
	if( ex!=null && ! (ex instanceof IOException) ) {
	    // we can ignore IOException - probably the user
	    // has clicked "STOP"
	    // we need to log any other error - something may be
	    // broken if the error servlet has errors.
	    ctx.log( "Error in errorServlet: ", ex);
	} 
    }

    public final Handler getHandlerForPath( ContextManager cm,
					    Context ctx, String path ) {
	if( ! path.startsWith( "/" ) ) {
	    return ctx.getServletByName( path );
	}
	Request req1=cm.createRequest(ctx, path);

	cm.processRequest( req1 );
	return req1.getHandler();
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

    /** Handle the case of status handler generating an error
     */
    private boolean statusLoop( Context ctx, Request req, int newCode ) {
        Integer lastCode = (Integer)req.getAttribute("javax.servlet.error.status_code");
        // If status code repeated, assume recursive loop
        if ( lastCode != null && lastCode.intValue() == newCode) {
            if( ctx.getDebug() > 0 )
                ctx.log( "Error: nested error inside status servlet " + newCode);
            return true;
        }
        return false;
    }
    
}

class NotFoundHandler extends Handler {
    static StringManager sm=StringManager.
	getManager("org.apache.tomcat.resources");
    int sbNote=0;
    boolean showDebugInfo=true;
    private String useCharset;
    
    NotFoundHandler(ErrorHandler bi, boolean showDebugInfo) {
	//	setOrigin( Handler.ORIGIN_INTERNAL );
	name="tomcat.notFoundHandler";
	setModule(bi);
	this.showDebugInfo=showDebugInfo;
	useCharset = bi.getUseCharset();
    }

    public void doService(Request req, Response res)
	throws Exception
    {
	String msg=(String)req.getAttribute("javax.servlet.error.message");

	String charset = useCharset;
	if(charset == null) {
	    charset = req.getCharEncoding();
	}
	if (charset == null) {
	    res.setContentType("text/html");
	} else {
	    res.setContentType("text/html; charset=" + charset);
	    res.setUsingWriter(true);
	}

	// "javax.servlet.include.request_uri" is set to this handler
	String requestURI = req.requestURI().toString();
	
	if( sbNote==0 ) {
	    sbNote=req.getContextManager().getNoteId(ContextManager.REQUEST_NOTE,
						     "NotFoundHandler.buff");
	}

	// we can recycle it because
	// we don't call toString();
	StringBuffer buf=(StringBuffer)req.getNote( sbNote );
	if( buf==null ) {
	    buf = new StringBuffer();
	    req.setNote( sbNote, buf );
	}
	
	boolean needsHead = res.getBuffer().isNew();
	// only include <head>...<body> if reset was successful
	if (needsHead) {
	    buf.append("<head><title>")
		.append(sm.getString("defaulterrorpage.notfound404"))
		.append("</title></head>\r\n<body>");
	}
	buf.append("<h1>")
	    .append(sm.getString("defaulterrorpage.notfound404"))
	    .append("</h1>\r\n");
	buf.append(sm.getString("defaulterrorpage.originalrequest"))
	    .append(" ")
	    .append( HttpMessages.filter( requestURI ) )
	    .append("\r\n");

	if ( null != requestURI && showDebugInfo ) {
	    buf.append("<br><br>\r\n<b>")
		.append(sm.getString("defaulterrorpage.notfoundrequest"))
		.append("</b> ")
		.append( HttpMessages.filter( requestURI ) )
		.append("\r\n");
	}

	if (msg != null){
            buf.append("<br><br>\r\n<b>")
                .append(msg)
                .append("</b><br>\r\n");
        }

	// only add </body> if reset was successful
	if ( needsHead )
	    buf.append("</body>");
	buf.append("\r\n");

	res.setContentLength(buf.length());

	res.getBuffer().write( buf );
	buf.setLength(0);
    }
}

class ExceptionHandler extends Handler {
    static StringManager sm=StringManager.
	getManager("org.apache.tomcat.resources");
    int sbNote=0;
    boolean showDebugInfo=true;
    private String useCharset;
    
    ExceptionHandler(ErrorHandler bi, boolean showDebugInfo) {
	//	setOrigin( Handler.ORIGIN_INTERNAL );
	name="tomcat.exceptionHandler";
	setModule( bi );
	this.showDebugInfo=showDebugInfo;
	useCharset = bi.getUseCharset();
    }

    public void doService(Request req, Response res)
	throws Exception
    {
	String msg=(String)req.getAttribute("javax.servlet.error.message");
	String errorURI = res.getErrorURI();
	
	Throwable e= (Throwable)req.
	    getAttribute("tomcat.servlet.error.throwable");
	if( e==null ) {
	    log("Exception handler called without an exception",
		new Throwable("trace"));
	    return;
	}

	
	if( sbNote==0 ) {
	    sbNote=req.getContextManager().
		getNoteId(ContextManager.REQUEST_NOTE,
			  "ExceptionHandler.buff");
	}

	// we can recycle it because
	// we don't call toString();
	StringBuffer buf=(StringBuffer)req.getNote( sbNote );
	if( buf==null ) {
	    buf = new StringBuffer();
	    req.setNote( sbNote, buf );
	}

	boolean needsHead = res.getBuffer().isNew();

	// only include <head>...<body> if reset was successful
	if ( needsHead ) {
	    String charset = useCharset;
	    if(charset == null) {
		charset = req.getCharEncoding();
	    }
           if (charset == null)
               res.setContentType("text/html");
           else {
               res.setContentType("text/html; charset=" + charset);
               res.setUsingWriter(true);
           }
	    res.setStatus( 500 );
	
	    buf.append("<head><title>");
	    if( null != errorURI && showDebugInfo ) {
		buf.append(sm.getString("defaulterrorpage.includedservlet") )
		    .append(" ");
	    }  else {
		buf.append("Error: ");
	    }
	    buf.append( 500 )
		.append("</title></head>\r\n<body>\r\n");
	}
	buf.append("<h1>");
	if( null != errorURI && showDebugInfo ) {
	    buf.append(sm.getString("defaulterrorpage.includedservlet") ).
		append(" ");
	}  else {
	    buf.append("Error: ");
	}
	
	buf.append( 500 );
	buf.append("</h1>\r\n");

	// More info - where it happended"
	buf.append("<h2>")
	    .append(sm.getString("defaulterrorpage.location"))
	    .append(" ")
	    .append( HttpMessages.filter( req.requestURI().toString() ) )
	    .append("</h2>");

	if ( null != errorURI && showDebugInfo ) {
	    buf.append("\r\n<h2>")
		.append(sm.getString("defaulterrorpage.errorlocation"))
		.append(" ")
		.append( HttpMessages.filter( errorURI ) )
		.append("</h2>");
	}

 	if (showDebugInfo) {
	    buf.append("<b>")
		.append(sm.getString("defaulterrorpage.internalservleterror"));
	    buf.append("</b><br>\r\n<pre>");
	    // prints nested exceptions too, including SQLExceptions, recursively
	    String trace = Logger.throwableToString
		(e, "<b>" + sm.getString("defaulterrorpage.rootcause") + "</b>");
	    buf.append(trace);
	    buf.append("</pre>\r\n");
	} else {
	    buf.append("<b>Error:</b> ")
		.append(e.getMessage())
		.append("<br><br>\r\n");
	}

	// only add </body> if reset was successful
	if (  needsHead )
	    buf.append("</body>");
	buf.append("\r\n");
	
	res.getBuffer().write( buf );
	buf.setLength(0);
    }

    
}

class StatusHandler extends Handler {
    static StringManager sm=StringManager.
	getManager("org.apache.tomcat.resources");
    int sbNote=0;
    boolean showDebugInfo=true;
    private String useCharset;
    
    StatusHandler(ErrorHandler bi, boolean showDebugInfo) {
	//setOrigin( Handler.ORIGIN_INTERNAL );
	name="tomcat.statusHandler";
	setModule( bi );
	this.showDebugInfo=showDebugInfo;
	useCharset = bi.getUseCharset();
    }
    
    // We don't want interceptors called for redirect
    // handler
    public void doService(Request req, Response res)
	throws Exception
    {
	String msg=(String)req.getAttribute("javax.servlet.error.message");
	String errorURI = res.getErrorURI();
	
	// res is reset !!!
	// status is already set
	int sc=res.getStatus();
	
	if( sc == 304 ) {
	    //NotModified must not return a body
	    return;
	} 
	// don't set a content type if we are answering If-Modified-Since.
	// Proxy caches might update their cached content-type with this
	// info (mod_proxy does it). Martin Algesten 15th Oct, 2002.
	String charset = useCharset;
	if(charset == null) {
	    charset = req.getCharEncoding();
	}
	if (charset == null) {
	    res.setContentType("text/html");
	} else {
	    res.setContentType("text/html; charset=" + charset);
	    res.setUsingWriter(true);
	}
	
	if( sbNote==0 ) {
	    sbNote=req.getContextManager().getNoteId(ContextManager.REQUEST_NOTE,
						     "StatusHandler.buff");
	}

	// we can recycle it because
	// we don't call toString();
	StringBuffer buf=(StringBuffer)req.getNote( sbNote );
	if( buf==null ) {
	    buf = new StringBuffer();
	    req.setNote( sbNote, buf );
	}

	boolean needsHead = res.getBuffer().isNew();
	// only include <head>...<body> if reset was successful
	if (needsHead) {
	    buf.append("<head><title>");
	    if( null != errorURI && showDebugInfo ) {
		buf.append(sm.getString("defaulterrorpage.includedservlet") )
		    .append(" ");
	    }  else {
		buf.append("Error: ");
	    }
	    buf.append( sc )
		.append("</title></head>\r\n<body>\r\n");
	}
	buf.append("<h1>");
	if( null != errorURI && showDebugInfo ) {
	    buf.append(sm.getString("defaulterrorpage.includedservlet") )
		.append(" ");
	}  else {
	    buf.append("Error: ");
	}
	
	buf.append( sc );
	buf.append("</h1>\r\n");

	// More info - where it happended"
	buf.append("<h2>")
	    .append(sm.getString("defaulterrorpage.location"))
	    .append(" ")
	    .append( HttpMessages.filter( req.requestURI().toString() ) )
	    .append("</h2>");

	if ( sc >= 400 && errorURI != null && showDebugInfo) {
	    buf.append("\r\n<h2>")
		.append(sm.getString("defaulterrorpage.errorlocation"))
		.append(" ")
		.append( HttpMessages.filter( errorURI ) )
		.append("</h2>");
	}

	if (msg != null){
            buf.append("\r\n<b>")
                .append(msg)
                .append("</b><br>\r\n");
        }

	// add unavailable time if present
	if ( sc == 503) {
	    Integer ut = (Integer)req.getAttribute("tomcat.servlet.error.service.unavailableTime");
	    if ( ut != null) {
		buf.append("<br>");
		// if permanent
		if (ut.intValue() < 0) {
		    buf.append(sm.getString("defaulterrorpage.service.permanently.unavailable"));
		} else {
		    buf.append(sm.getString("defaulterrorpage.service.unavailable",ut));
		}
		buf.append("<br>\r\n");
	    }
	}

	// only add </body> if reset was successful
	if ( needsHead )
	    buf.append("</body>");
	buf.append("\r\n");

	res.setContentLength(buf.length());
	res.getBuffer().write( buf );
	buf.setLength(0);
    }
}
	
class RedirectHandler extends Handler {
    static StringManager sm=StringManager.
	getManager("org.apache.tomcat.resources");
    int sbNote=0;
    int defaultRedirectStatus=301;
    String useCharset;

    RedirectHandler(ErrorHandler bi) {
	//setOrigin( Handler.ORIGIN_INTERNAL );
	name="tomcat.redirectHandler";
	setModule( bi );
	useCharset = bi.getUseCharset();
    }

    public void setDefaultRedirectStatus( int status ) {
        defaultRedirectStatus=status;
    }

    // We don't want interceptors called for redirect
    // handler
    public void doService(Request req, Response res)
	throws Exception
    {
	String location	= (String)
	    req.getAttribute("javax.servlet.error.message");
	Context ctx=req.getContext();

	if( res.getStatus() != 301 &&
	    res.getStatus() != 302 ) {
	    res.setStatus( defaultRedirectStatus );
	}
	
	location = makeAbsolute(req, location);

	if( debug>0) ctx.log("Redirect " + location + " " + req );

	String charset = useCharset;
	if(charset == null) {
	    charset = req.getCharEncoding();
	}
	if (charset == null) {
	    res.setContentType("text/html");
	} else {
	    res.setContentType("text/html; charset=" + charset);
	    res.setUsingWriter(true);
	}

	res.setHeader("Location", location);

	if( sbNote==0 ) {
	    sbNote=req.getContextManager().getNoteId(ContextManager.REQUEST_NOTE,
						     "RedirectHandler.buff");
	}

	// we can recycle it because
	// we don't call toString();
	StringBuffer buf=(StringBuffer)req.getNote( sbNote );
	if( buf==null ) {
	    buf = new StringBuffer();
	    req.setNote( sbNote, buf );
	}
	buf.append("<html><head><title>").
	    append(sm.getString("defaulterrorpage.documentmoved")).
	    append("</title></head>\r\n<body><h1>").
	    append(sm.getString("defaulterrorpage.documentmoved")).
	    append("</h1>\r\n").
	    append(sm.getString("defaulterrorpage.thisdocumenthasmoved")).
	    append(" <a href=\"").
	    append( HttpMessages.filter( location ) ).
	    append("\">here</a>.<p>\r\n</body>\r\n</html>");

	res.setContentLength(buf.length());
	res.getBuffer().write( buf );
        res.getBuffer().close();
	buf.setLength(0);

    }

    // XXX Move it to URLUtil !!!
    private String makeAbsolute(Request req, String location) {
        URL url = null;
        try {
	    // Try making a URL out of the location
	    // Throws an exception if the location is relative
            url = new URL(location);
	} catch (MalformedURLException e) {
	    String requrl = getRequestURL(req);
	    try {
	        url = new URL(new URL(requrl), location);
	    }
	    catch (MalformedURLException ignored) {
	        // Give up
	        return location;
	    }
	}
        return url.toExternalForm();
    }

    static String getRequestURL( Request req )  {
 	StringBuffer url = new StringBuffer ();
	String scheme = req.scheme().toString();
	int port = req.getServerPort ();
	String urlPath = req.requestURI().toString();

	url.append (scheme);		// http, https
	url.append ("://");
	url.append (req.serverName().toString());
	if ((scheme.equals ("http") && port != 80)
		|| (scheme.equals ("https") && port != 443)) {
	    url.append (':');
	    url.append (port);
	}
	url.append(urlPath);
	return url.toString();
    }
}
