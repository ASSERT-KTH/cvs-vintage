/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Handler.java,v 1.2 2000/06/16 21:03:21 costin Exp $
 * $Revision: 1.2 $
 * $Date: 2000/06/16 21:03:21 $
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
 * The class that will generate the actual response.
 * Each Handler has a "name" that will determine the content that
 * it will handle.
 * 
 * @author costin@dnt.ro
 */
public class Handler {
    protected Context context;
    protected ContextManager contextM;

    protected String name;

    /** True if it can handle requests.
	404 or error if not.
    */
    protected boolean initialized=false;
    
    Hashtable initArgs=null;

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
    protected int origin;

    public Handler() {
    }

    public void setContext( Context context) {
        this.context = context;
	contextM=context.getContextManager();
    }

    public Context getContext() {
	return context;
    }

    public String getName() {
	return name;
    }

    public void setName(String servletName) {
        this.name=servletName;
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
	    return EmptyEnumeration.getEmptyEnumeration();
	}
    }

    /** This method will be called when the handler
	is removed ( by admin or timeout ). Various handlers
	can implement this, but it can't be called from outside.
	( the "guarded" doDestroy is public )
    */
    protected void doDestroy() throws Exception {

    }

    /** Destroy a handler, and notify all the interested interceptors
     */
    public void destroy() throws Exception {
	if ( ! initialized ) return;// already destroyed or not init.
	initialized=false;

	ContextInterceptor cI[]=context.getContextInterceptors();
	for( int i=0; i<cI.length; i++ ) {
	    try {
		cI[i].preServletDestroy( context, null);// this );
		// ignore the error - like in the original code
	    } catch( TomcatException ex) {
		context.log( "Error in preServletDestroy " + cI, ex );
	    }
	}
	
	// XXX post will not be called if any error happens in destroy.
	// That's how tomcat worked before - I think it's a bug !
	doDestroy();
	
	for( int i=cI.length-1; i>=0; i-- ) {
	    try {
		cI[i].postServletDestroy( context, null); //this );
		// ignore the error - like in the original code
	    } catch( TomcatException ex) {
		context.log( "Error in postServletDestroy " + cI, ex );
	    }
	}
    }

    /** Initialize the handler. Handler can override this
	method to initialize themself.
	The method must set initialised=true if successfull.
     */
    protected void doInit() throws Exception
    {

    }

    /** Call the init method, and notify all interested listeners.
     */
    public void init()
	throws Exception
    {
	try {
	    if( initialized ) return;
	    
	    ContextInterceptor cI[]=context.getContextInterceptors();
	    for( int i=0; i<cI.length; i++ ) {
		try {
		    cI[i].preServletInit( context, null);//this );
		    // ignore the error - like in the original code
		} catch( TomcatException ex) {
		    ex.printStackTrace();
		}
	    }

	    doInit();

	    // if an exception is thrown in init, no end interceptors will
	    // be called. that was in the origianl code J2EE used
		
	    for( int i=cI.length-1; i>=0; i-- ) {
		try {
		    cI[i].postServletInit( context, null);//this);
		    // ignore the error - like in the original code
		} catch( TomcatException ex) {
		    ex.printStackTrace();
		}
	    }

	} catch( Exception ex ) {
	    initialized=false;
	}
    }

    /** This is the actual content generator. Can't be called
	from outside.
     */
    protected void doService(Request req, Response res)
	throws Exception
    {

    }

    /** Call the service method, and notify all listeners
     */
    public void service(Request req, Response res) 
    {
	if( ! initialized ) {
	    try {
		init();
	    } catch( Exception ex ) {
		initialized=false;
		context.log("Exception in init  " + ex.getMessage(), ex );
		contextM.handleError( req, res, ex );
		return;
	    }
	}
	
	// We are initialized and fine
	RequestInterceptor cI[]=context.getRequestInterceptors();
	for( int i=0; i<cI.length; i++ ) {
	    cI[i].preService( req, res );
	    // ignore the error - like in the original code
	}

	Throwable t=null;
	try {
	    doService( req, res );
	} catch( Throwable t1 ) {
	    t=t1;
	}
	
	// continue with the postService

	for( int i=cI.length-1; i>=0; i-- ) {
	    cI[i].postService( req , res );
	    // ignore the error - like in the original code
	}

	if( t==null)
	    return;
	
	if( t instanceof IOException ) {
	    if( ((IOException)t).getMessage().equals("Broken pipe"))
		return;
	    System.out.println("XXX XXX " + t.getMessage());
	}
	    
	if(null!=req.getAttribute("tomcat.servlet.error.defaultHandler")){
	    // we are in handleRequest for the "default" error handler
	    System.out.println("ERROR: can't find default error handler "+
			       "or error in default error page");
	    t.printStackTrace();
	} else {
	    String msg=t.getMessage();
	    context.log( "Error in " + getName() +
			 " service() : " + msg, t);
	    // XXX XXX Security - we should log the message, but nothing
	    // should show up  to the user - it gives up information
	    // about the internal system !
	    // Developers can/should use the logs !!!
	    contextM.handleError( req, res, t );
	}
    }
    
    public String toString() {
	return name;
    }

    // -------------------- Origin 
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
