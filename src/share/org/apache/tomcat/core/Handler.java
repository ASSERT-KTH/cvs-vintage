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

import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.collections.EmptyEnumeration;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The class that will generate the actual response or response fragment.
 * Each Handler has a "name" that will determine the content that
 * it will handle.
 *
 * The choice to not use "mime/type" as Apache, NES, IIS
 * is based on the fact that most of the time servlets have "names", and
 * the mime handling is very different in servlet API.
 * It is possible to use mime types as a name, and special interceptors can
 * take advantage of that ( to better integrate with the server ), but
 * this is not a basic feature.
 *
 * Handlers will implement doService, doInit, doDestroy - all methods are
 * protected and can't be called from outside. This ensures the only entry
 * points are service(), init(), destroy() and the state and error handling
 * is consistent.
 *
 * Common properties:
 * <ul>
 *   <li>name
 *   <li>configuration parameters
 *   <li>
 * </ul>
 *
 * @author Costin Manolache
 */
public class Handler {
    /** accounting - various informations we capture about servlet
     *	execution.
     *  // XXX Not implemented
     *  @see org.apache.tomcat.util.Counters
     */
    public static final int ACC_LAST_ACCESSED=0;
    public static final int ACC_INVOCATION_COUNT=1;
    public static final int ACC_SERVICE_TIME=2;
    public static final int ACC_ERRORS=3;
    public static final int ACC_OVERHEAD=4;
    public static final int ACC_IN_INCLUDE=5;
    
    public static final int ACCOUNTS=6;

    // -------------------- Origin --------------------
    /** The handler is declared in a configuration file.
     */
    public static final int ORIGIN_WEB_XML=0;
    /** The handler is automatically added by an "invoker" interceptor,
     *  that is able to add new servlets based on request
     */
    public static final int ORIGIN_INVOKER=1;
    /** The handler is automatically added by an interceptor that
     * implements a templating system.
     */
    public static final int ORIGIN_JSP=2;
    /** any tomcat-specific component that can
	register mappings that are "re-generable",
	i.e. can be recreated - the mapping can
	safely be removed.
    */
    public static final int ORIGIN_DYNAMIC=3;
    /** The handler was added by the admin interface, it should be saved
     *	preferably in web.xml
     */
    public static final int ORIGIN_ADMIN=4;

    /** This handler is created internally by tomcat
     */
    public static final int ORIGIN_INTERNAL=5;

    // -------------------- State --------------------

    public static final int STATE_NEW=0;

    public static final int STATE_ADDED=1;

    public static final int STATE_READY=2;

    public static final int STATE_TEMP_DISABLED=3;

    public static final int STATE_DISABLED=4;
    

    // -------------------- Properties --------------------
    protected Context context;
    protected ContextManager contextM;
    
    protected String name;

    protected int state=STATE_NEW;
    
    /** True if it can handle requests.
	404 or error if not.
    */
    protected boolean initialized=false;
    
    Hashtable initArgs=null;

    // who creates the servlet definition
    protected int origin;

    protected String path;

    protected String servletName;

    protected int loadOnStartup=-1;
    protected boolean loadingOnStartup=false;

    protected Exception errorException=null;
    protected boolean exceptionPermanent=false;
    
    // Debug
    protected int debug=0;
    protected Log logger=null;
    protected String debugHead=null;

    private Counters cntr=new Counters( ACCOUNTS );
    private Object notes[]=new Object[ContextManager.MAX_NOTES];

    // -------------------- Constructor --------------------

    /** Creates a new handler.
     */
    public Handler() {
    }

    /** A handler "belongs" to a single application ( many->one ).
     *  We don't support handlers that spawn multiple Contexts -
     *  the model is simpler because we can set the security constraints,
     *  properties, etc on a application basis.
     */
    public final void setContext( Context context) {
        this.context = context;
	contextM=context.getContextManager();
	logger=context.getLog();
    }

    /** Return the context associated with the handler
     */
    public final Context getContext() {
	return context;
    }

    public int getState() {
	return state;
    }

    public void setState( int i ) {
	this.state=i;
    }
    
    // -------------------- configuration --------------------

    public final String getName() {
	return name;
    }

    public final void setName(String handlerName) {
        this.name=handlerName;
    }

    /** Who created this servlet definition - default is 0, i.e. the
     *	web.xml mapping. It can also be the Invoker, the admin ( by using a
     *  web interface), JSP engine or something else.
     * 
     *  Tomcat can do special actions - for example remove non-used
     *	mappings if the source is the invoker or a similar component
     */
    public final void setOrigin( int origin ) {
	this.origin=origin;
    }
    
    public final int getOrigin() {
	return origin;
    }

    /** Accounting information
     */
    public final Counters getCounters() {
	return cntr;
    }

    // -------------------- Common servlet attributes
    public void setLoadOnStartUp( int level ) {
	loadOnStartup=level;
    }

    public int getLoadOnStartUp() {
	return loadOnStartup;
    }

    public void setLoadingOnStartUp( boolean load ) {
	loadingOnStartup=load;
    }

    public boolean getLoadingOnStartUp() {
	return loadingOnStartup;
    }

    /** Sets an exception that relates to the ability of the
	servlet to execute.  An exception may be set by an
	interceptor if there is an error during the creation
	of the servlet. 
     */
    public void setErrorException(Exception ex) {
	errorException = ex;
    }

    /** Gets the exception that relates to the servlet's
	ability to execute.
     */
    public Exception getErrorException() {
	return errorException;
    }

    public boolean isExceptionPresent() {
	return ( errorException != null );
    }

    public void setExceptionPermanent( boolean permanent ) {
	exceptionPermanent = permanent;
    }

    public boolean isExceptionPermanent() {
	return exceptionPermanent;
    }

    // -------------------- Jsp specific code
    
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
	if( name==null )
	    name=path; // the path will serve as servlet name if not set
    }

    // -------------------- Init params

    /** Add configuration properties associated with this handler.
     *  This is a non-final method, handler may override it with an
     *  improved/specialized version.
     */
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
	    return EmptyEnumeration.getEmptyEnumeration();
	}
    }

    // -------------------- Methods --------------------

    /** Destroy a handler, and notify all the interested interceptors
     */
    public final void destroy() throws Exception {
	if ( ! initialized ) {
	    errorException = null;
	    return;// already destroyed or not init.
	}
	initialized=false;

	// XXX post will not be called if any error happens in destroy.
	// That's how tomcat worked before - I think it's a bug !
	doDestroy();

	errorException=null;
    }


    /** Call the init method, and notify all interested listeners.
     */
    public /* final */ void init()
	throws Exception
    {
	// if initialized, then we were sync blocked when first init() succeeded
	if( initialized ) return;
	// if exception present, then we were sync blocked when first init() failed
	// or an interceptor set an inital exeception
	if (errorException != null) throw errorException;
	try {
	    doInit();
	    initialized=true;
	} catch( Exception ex ) {
	    // save error, assume permanent
	    setErrorException(ex);
	    setExceptionPermanent(true);
	}
    }

    // XXX XXX XXX
    // Must be changed - it's very confusing since it has the same name
    // with the servlet's service() method.
    // The Handler is at a different ( lower ) level, we should
    // use different names ( invoke() ? )

    /** Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
    public final void service(Request req, Response res)
	throws Exception
    {
	if( ! initialized ) {
	    Exception ex=null;
	    synchronized( this ) {
		// we may be initialized when we enter the sync block
		if( ! initialized ) {
		    try {
			init();
		    } catch ( Exception e ) {
			errorException = e;
			exceptionPermanent = true;
		    }
		}
		// get copy of exception, if any, before leaving sync lock
		ex=errorException;
	    }
	    // if error occurred
	    if ( ex != null ) {
		// save error state on request and response
		saveError( req, res, ex );
		log("Exception in init  " + ex.getMessage(), ex );
		// if in included, defer handling to higher level
		if (res.isIncluded()) return;
		// handle init error since at top level
		if( ex instanceof ClassNotFoundException )
		    contextM.handleStatus( req, res, 404 );
		else
		    contextM.handleError( req, res, ex );
		return;
	    }
	}
	
	//	if( ! internal ) {
	// no distinction for internal handlers !
	BaseInterceptor reqI[]=
	    req.getContainer().getInterceptors(Container.H_preService);
	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].preService( req, res );
	}
	//}

	try {
	    doService( req, res );
	} catch( Exception ex ) {
	    // save error state on request and response
	    saveError( req, res, ex );
	}

	// continue with the postService
	//	if( ! internal ) {
	reqI=req.getContainer().getInterceptors(Container.H_postService);
	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].postService( req, res );
	}
	//	}

	// if no error
	if( ! res.isExceptionPresent() ) return;
	// if in included, defer handling to higher level
	if (res.isIncluded()) return;
	// handle original error since at top level
	contextM.handleError( req, res, res.getErrorException() );
    }

    // -------------------- methods you can override --------------------

    /** Reload notification. This hook is called whenever the
     *  application ( this handler ) is reloaded
     */
    public void reload() {
    }
        
    /** This method will be called when the handler
     *	is removed ( by admin or timeout ).
     */
    protected void doDestroy() throws Exception {

    }

    /** Initialize the handler. Handler can override this
     *	method to initialize themself.
     */
    protected void doInit() throws Exception
    {
	
    }

    /** This is the actual content generator. Can't be called
     *  from outside.
     *
     *  This method can't be called directly, you must use service(),
     *  which also tests the initialization and deals with the errors.
     */
    protected void doService(Request req, Response res)
	throws Exception
    {

    }

    // -------------------- Debug --------------------

    public String toString() {
	return name;
    }

    /** Debug level for this handler.
     */
    public final void setDebug( int d ) {
	debug=d;
    }

    protected final void log( String s ) {
	if ( logger==null ) 
	    contextM.log(s);
	else 
	    logger.log(s);
    }

    protected final void log( String s, Throwable t ) {
	if(logger==null )
	    contextM.log(s, t);
	else
	    logger.log(s, t);
    }

    // --------------- Error Handling ----------------

    /** If an error happens during init or service it will be saved in
     *  request and response.
     */
    // XXX error handling in Handler shouldn't be exposed to childs, need
    // simplifications
    protected final void saveError( Request req, Response res, Exception ex ) {
	// save current exception on the request
	req.setErrorException( ex );
	// if the first exception, save info on the response
	if ( ! res.isExceptionPresent() ) {
	    res.setErrorException( ex );
	    res.setErrorURI( (String)req.
			  getAttribute("javax.servlet.include.request_uri"));
	}
    }

    // -------------------- Notes --------------------
    public final void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public final Object getNote( int pos ) {
	return notes[pos];
    }

}
