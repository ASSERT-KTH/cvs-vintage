/*
 *  Copyright 1999-2004 The Apache Software Foundation
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
 *  See the License for the specific language 
 */

package org.apache.tomcat.facade;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.ServerSession;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.compat.Jdk11Compat;

/**
 *   Control class for facades - this is the only "gate" between servlets
 *   and tomcat.
 *
 *   This is an important security component, shouldn't be used for
 *   anything else. Please keep all the code short and clean - and review
 *   everything very often.
 *  
 */
public final class Servlet22Interceptor
    extends BaseInterceptor
{
    public static final String SERVLET_STAMP = " ( JSP 1.1; Servlet 2.2 )";
    private int stmPoolSize = SimplePool.DEFAULT_SIZE;
    private boolean useStmPool = true;
	
    public Servlet22Interceptor() {
    }

    public Servlet22Interceptor(Context ctx) {
    }

    public void setSTMPoolSize(int size) {
	stmPoolSize = size;
    }
    public void setUseSTMPool(boolean use) {
	useStmPool = use;
    }

    // -------------------- implementation
    private void setEngineHeader(Context ctx) {
        String engineHeader=ctx.getEngineHeader();

	// EngineHeader can be set as a Context Property!
	if( engineHeader==null) {
	    StringBuffer sb=new StringBuffer();
	    sb.append(ContextManager.TOMCAT_NAME);
	    sb.append("/");
	    sb.append(ContextManager.TOMCAT_VERSION );
	    sb.append(SERVLET_STAMP);
	    engineHeader=sb.toString();
	}
	ctx.setEngineHeader( engineHeader );
    }

    /** Call servlet.destroy() for all servlets, as required
	by the spec
    */
    public void contextShutdown( Context ctx )
	throws TomcatException
    {
	// shut down and servlets
	Enumeration enum = ctx.getServletNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    Handler wrapper = ctx.getServletByName( key );
	    
	    if( ! (wrapper instanceof ServletHandler) ) 
		continue;

	    try {
		((ServletHandler)wrapper).destroy();
	    } catch(Exception ex ) {
		ctx.log( "Error in destroy ", ex);
	    }
	    // remove the context after it is destroyed.
	    // remove will "un-declare" the servlet
	    // After this the servlet will be in STATE_NEW, and can
	    // be reused.
	    ctx.removeServletByName( key );
	}
    }
    
    public void addContext( ContextManager cm, Context ctx )
	throws TomcatException
    {
	ctx.setFacade(new ServletContextFacade(cm , ctx));
	setEngineHeader( ctx );
    }

    public void addContainer( Container ct )
    	throws TomcatException
    {
	String hN=ct.getHandlerName();
	if( hN == null ) return;
	

	if( ct.getHandler() == null ) {
	    // we have a container with a valid handler name but without
	    // a Handler. Create a ServletWrapper
	    ServletHandler handler=new ServletHandler();
	    handler.setServletClassName( hN );
	    handler.setName( hN );
	    handler.setContext( ct.getContext() );
	    // *.jsp -> jsp is a legacy default mapping  
	    if(debug>0 &&  ! "jsp".equals(hN) ) {
		log( "Create handler " + hN);
	    }
	    handler.setModule( this );
	    ct.setHandler(handler);
	    ct.getContext().addServlet( handler );
	} 
	if(ct.getHandler() instanceof ServletHandler) {
	    ServletHandler handler = (ServletHandler)ct.getHandler();
	    handler.setSTMPoolSize(stmPoolSize);
	    handler.setUseSTMPool(useStmPool);
	}
    }

    static Jdk11Compat jdk11Compat = Jdk11Compat.getJdkCompat();

    /** Call the Servlet22 callbacks when session expires.
     */
    public int sessionState( Request req, ServerSession sess, int newState)
    {
	if( debug > 0 )
	    log("sessionState " + sess.getId() + " " + newState + " " + sess.getState());
	if( newState==ServerSession.STATE_SUSPEND ||
	    newState==ServerSession.STATE_EXPIRED )   {

	    if( debug > 0 )
		log("Unbinding variables ");
	    // generate "unbould" events when the session is suspended or
	    // expired
	    HttpSession httpSess=(HttpSession)sess.getFacade();

	    Vector removed=null; // lazy 
	    Enumeration e = sess.getAttributeNames();
	    ClassLoader clSave = jdk11Compat.getContextClassLoader();
	    ClassLoader cxCL = sess.getContext().getClassLoader();
	    if( clSave != cxCL ) {
		jdk11Compat.setContextClassLoader(cxCL);
	    } else {
		clSave = null;
	    }
	    // announce all values with listener that we'll remove them
	    while( e.hasMoreElements() )   {
		String key = (String) e.nextElement();
		Object value = sess.getAttribute(key);

		if( value instanceof  HttpSessionBindingListener) {
		    if( debug > 0 )
			log("valueUnbound " + sess.getId() + " " + key );
                    try {
                        ((HttpSessionBindingListener) value).valueUnbound
                            (new HttpSessionBindingEvent(httpSess , key));
                    } catch ( Throwable th ) {
                        log("Exception during unbound", th ); 
                    }
		    if( removed==null) removed=new Vector();
		    removed.addElement( key );
		}
	    }
	    if( clSave != null ) {
		jdk11Compat.setContextClassLoader(clSave);
	    }
	    if( removed!=null ) {
		// remove
		e=removed.elements();
		while( e.hasMoreElements() ) {
		    String key = (String) e.nextElement();
		    sess.removeAttribute( key );
		}
	    }
	    if( httpSess != null && newState==ServerSession.STATE_EXPIRED ) {
		((HttpSessionFacade)httpSess).recycle();
	    }
	} 
	return 0;
    }

    
    public int postRequest(Request rreq, Response rres ) {
	//if( rreq.getContext() != ctx ) return; // throw

	//	log( "Recycling " + rreq );
	HttpServletRequest req=(HttpServletRequest)rreq.getFacade();
	if( ! (req instanceof HttpServletRequestFacade))
	    return 0;
	
	((HttpServletRequestFacade)req).recycle();

	// recycle response
	//	Response rres=rreq.getResponse();
	if( rres== null )
	    return 0;
	
	HttpServletResponse res=(HttpServletResponse)rres.getFacade();
	if( res!=null) ((HttpServletResponseFacade)res).recycle();

	// recycle output stream
	// XXX XXX implement it

	return 0;
    }
}
    
