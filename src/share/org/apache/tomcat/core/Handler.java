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

package org.apache.tomcat.core;

import org.apache.tomcat.util.log.Log;

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
    // -------------------- State --------------------

    /** The handler is new, not part of any application.
     *  You must to add the handler to application before doing
     *  anything else.
     *  To ADDED by calling Context.addHandler().
     *  From ADDED by calling Context.removeHandler();
     */
    public static final int STATE_NEW=0;

    /** The handler is added to an application and can be initialized.
     *  To READY by calling init(), if success
     *  TO DISABLED by calling init if it fails ( exception )
     *  From READY by calling destroy()
     */
    public static final int STATE_ADDED=1;

    /** Handler is unable to perform - any attempt to use it should
     *  report an internal error. This is the result of an internal
     *  exception or an error in init()
     *  To ADDED by calling destroy()
     *  From ADDED by calling init() ( if failure )
     */
    public static final int STATE_DISABLED=4;

    // -------------------- Properties --------------------
    // the handler is part of a module
    protected BaseInterceptor module;
    
    protected ContextManager contextM;
    
    protected String name;
    protected int state=STATE_NEW;

    protected Exception errorException=null;
    
    // Debug
    protected int debug=0;
    protected Log logger=null;
    protected Handler next;
    protected Handler prev;


    private Object notes[]=new Object[ContextManager.MAX_NOTES];

    // -------------------- Constructor --------------------

    /** Creates a new handler.
     */
    public Handler() {
    }

    /** A handler is part of a module. The module can create a number
	of handlers. Since the module is configurable, it can 
	also configure the handlers - including debug, etc.

	The handler shares the same log file with the module ( it
	can log to different logs, but the default for debug information
	is shared with the other components of a module ).
     */
    public void setModule(BaseInterceptor module ) {
	this.module=module;
	contextM=module.getContextManager();
	debug=module.getDebug();
	logger=module.getLog();
    }

    public void setContextManager( ContextManager cm ) {
	this.contextM=cm;
	if( logger==null )
	    logger=cm.getLog();
    }

    public BaseInterceptor getModule() {
	return module;
    }
    
    // -------------------- configuration --------------------

    public int getState() {
	return state;
    }

    public void setState( int i ) {
	this.state=i;
    }
    
    public String getName() {
	return name;
    }

    public void setName(String handlerName) {
        this.name=handlerName;
    }

    // -------------------- Common servlet attributes
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

    // -------------------- Methods --------------------

    public void init() throws TomcatException {
    }

    public void destroy() throws TomcatException {
    }
    
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
    public void service(Request req, Response res)
	throws Exception
    {
	BaseInterceptor reqI[]=
	    req.getContainer().getInterceptors(Container.H_preService);
	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].preService( req, res );
	}

	Exception serviceException=null;
	try {
	    invoke( req, res );
	} catch( Exception ex ) {
	    // save error state on request and response
	    serviceException=ex;
	    // if new exception, update info
	    if ( ex != res.getErrorException() ) {
		//log("setErrorException " + ex ); 
		res.setErrorException(ex);
		res.setErrorURI(null);
	    }
	}

	// continue with the postService ( roll back transactions, etc )
	reqI=req.getContainer().getInterceptors(Container.H_postService);
	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].postService( req, res );
	}

	// if error, handle
	if( serviceException != null ) {
	    //log("handle " + serviceException );
	    //serviceException.printStackTrace();
	    handleServiceError( req, res, serviceException );
	    //log("XXX After handleServiceError");
	}
    }

    /** A handler may either directly generate the response or it can
     *	act as a part of a pipeline. Next/Prev allow modules to generically
     *  set the pipeline.
     *
     *  Most handlers will generate the content themself. Few handlers
     *  may prepare the request/response - one example is the Servlet
     *  handler that wraps them and calls a servlet. Modules may
     *  do choose to either hook into the server using the provided
     *  callbacks, or use a pipeline of handler ( valves ). 
     */	
    public void setNext( Handler next ) {
	this.next=next;
    }

    public Handler getNext() {
	return next;
    }

    public Handler getPrevious() {
	return prev;
    }

    public void setPrevious( Handler prev ) {
	this.prev=prev;
    }
    
    // -------------------- methods you can override --------------------
    
    protected void handleServiceError( Request req, Response res, Throwable t )
	throws Exception
    {
	//log("handleServiceError " + t);
	contextM.handleError( req, res, t );
    }
    
    /** Reload notification. This hook is called whenever the
     *  application ( this handler ) is reloaded
     */
    public void reload() {
    }
    
    // Old name, deprecated
    protected void doService(Request req, Response res)
	throws Exception
    {

    }

    /** This is the actual content generator. A handler must generate
     *  the response - either itself or by calling another class, after
     *  setting/changing request properties or altering/wrapping
     *  the request and response.
     *
     *  A Handler should use the "next" property if it is part
     *  of a pipeline ( "valve") 
     */
    protected void invoke(Request req, Response res)
	throws Exception
    {
	// backward compatibility
	doService(req, res);
    }

    // -------------------- Debug --------------------

    public String toString() {
	return name;
    }

    /** Debug level for this handler.
     */
    public void setDebug( int d ) {
	debug=d;
    }

    protected void log( String s ) {
	if ( logger==null ) 
	    contextM.log(s);
	else 
	    logger.log(s);
    }

    protected void log( String s, Throwable t ) {
	if(logger==null )
	    contextM.log(s, t);
	else
	    logger.log(s, t);
    }

    // -------------------- Notes --------------------
    public final void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public final Object getNote( int pos ) {
	return notes[pos];
    }

    public Object getNote( String name ) throws TomcatException {
	int id=contextM.getNoteId( ContextManager.HANDLER_NOTE,
				   name );
	return getNote( id );
    }

    public void setNote( String name, Object value ) throws TomcatException {
	int id=contextM.getNoteId( ContextManager.HANDLER_NOTE,
				   name );
	setNote( id, value );
    }

}
