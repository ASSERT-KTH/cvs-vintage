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

import org.apache.tomcat.core.*;
import org.apache.tomcat.net.*;
import org.apache.tomcat.request.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * A collection class representing the Contexts associated with a particular
 * Server.  The managed Contexts can be accessed directly by name, or
 * indirectly by requesting the Context responsible for processing a
 * particular path.  One particular Context can be distinguished as the
 * default Context, which is selected to process paths for which no other
 * Context is responsible.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */
public class ContextManager {
    /**
     * The string constants for this ContextManager.
     */
    private StringManager sm =StringManager.getManager("org.apache.tomcat.core");

    ContextInterceptor contextInterceptor=new ContextInterceptor( this );
    SessionInterceptor sessionInterceptor=new SessionInterceptor();
    MapperInterceptor mapperInterceptor=new MapperInterceptor();
    
    /**
     * The default Context used to process paths not associated with
     * any other Context.
     */
    private Context defaultContext;

    /**
     * The set of Contexts associated with this ContextManager,
     * keyed by context name.
     */

    private Hashtable contexts = new Hashtable();


    /**
     * The set of Contexts associated with this ContextManager,
     * keyed by the path prefix to be processed by this Context.
     */

    private Hashtable contextMaps = new Hashtable();


    /**
     * The server information string to be returned by the server
     * associated with this ContextManager.
     */

    private String serverInfo = null;


    /**
     * The virtual host name for the Server this ContextManager
     * is associated with.
     * XXX Why is this here instead of in the Server?
     */
    // Used by Contexts
    String hostname;

    /**
     * The port number being listed to by the Server this ContextManager
     * is associated with.
     * XXX Why is this here instead of in the Server?
     */

    int port;


    /**
     * Construct a new ContextManager instance with default values.
     */

    public ContextManager() {

    }
    

    // XXX XXX who calls this ? It should use per context info
    // Wrong because it's not a property of context manager ( maybe
    // defaultServerInfo
    /**
     * Gets the server info string for this server
     */
    
    public String getServerInfo() {
	if( serverInfo==null) 
	    serverInfo=getContext("").getEngineHeader();

        return serverInfo;
    }


    /**
     * Sets the server info string for this server. This string must
     * be of the form <productName>/<productVersion> [(<optionalInfo>)]
     *
     * @param serverInfo The new server information string
     */

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }


    /**
     * Gets the document base of the default context for this server.
     */
    // XXX wrong - ContextManager has no document base !
    public URL getDocumentBase() {
	return getContext("").getDocumentBase();
    }


    /**
     * Sets the document base of the default context for this server.
     *
     * @param docBase The new document base
     */

    public void setDocumentBase(URL docBase) {
	getContext("").setDocumentBase(docBase);
    }

    /**
     * Get the names of all the contexts in this server.
     */
    public Enumeration getContextNames() {
        return contexts.keys();
    }


    /**
     * Gets a context by it's name, or <code>null</code> if there is
     * no such context.
     *
     * @param name Name of the requested context
     */
    
    public Context getContext(String name) {
	return (Context)contexts.get(name);
    }

    public Context getMappedContext( String path ) {
	return  (Context)contextMaps.get(path);
    }

    /**
     * Adds a new Context to the set managed by this ContextManager.
     * XXX Why is there no context name argument?
     * XXX Should this be synchronized?
     *
     * @param path Path prefix to be processed by this Context
     * @param docBase Document base URL for this Context
     */
    public Context addContext(String path, URL docBase) {
	// assert path!=null
        if (path == null) {
	    String msg = sm.getString("server.defaultContext.path.npe");
	    throw new NullPointerException(msg);
	}

        path = path.trim();

	if (path.length() > 0 &&
	    docBase == null) {
	    String msg = sm.getString("server.defaultContext.docBase.npe");

	    throw new NullPointerException(msg);
	}

        if (contexts.get(path) != null) {
	    String msg = sm.getString("server.createctx.existname",
	        path);

	    throw new IllegalStateException(msg);
	}

	if (contextMaps.get(path) != null) {
	    String msg = sm.getString("server.createctx.existmap",
	        path);

	    throw new IllegalStateException(msg);
	}

	if (docBase == null) {
	    throw new RuntimeException("XXX SHOULD NEVER HAPPEN");
	}

	Context context = new Context(this, path, docBase);


	// check to see if defaultContext

	if (path.length() == 0) {
	    contexts.put("",context);
	} else {
	    contexts.put(path, context);
	    contextMaps.put(path, context);
	}

	return context;
    }


    /**
     * Removes a context from service.
     * XXX Should this be synchronized?
     *
     * @param name Name of the Context to be removed
     */
    
    public void removeContext(String name) {
	if (name.equals("")){
	    throw new IllegalArgumentException(name);
	}

	Context context = (Context)contexts.get(name);

	if(context != null) {
	    context.shutdown();
	    contexts.remove(name);
	    contextMaps.remove(name);
	}
    }


    /**
     * Sets the port number on which this server listens.
     *
     * @param port The new port number
     */
    public void setPort(int port) {
	this.port=port;
    }


    /**
     * Gets the port number on which this server listens.
     */

    public int getPort() {
	return port;
    }


    /**
     * Sets the virtual host name of this server.
     *
     * @param host The new virtual host name
     */
    public void setHostName( String host) {
	this.hostname=host;
    }


    /**
     * Gets the virtual host name of this server.
     */

    public String getHostName() {
	return hostname;
    }


    /** Common for all connectors, needs to be shared in order to avoid
	code duplication
    */
    public void service( Request rrequest, Response rresponse ) {
	try {
	    rrequest.setResponse(rresponse);
	    rresponse.setRequest(rrequest);

	    // XXX
	    //    return if an error was detected in processing the
	    //    request line
	    if (rresponse.getStatus() >= 400) {
		rresponse.finish();
		rrequest.recycle();
		rresponse.recycle();
		return;
	    }

	    //    don't do headers if request protocol is http/0.9
	    if (rrequest.getProtocol() == null) {
		rresponse.setOmitHeaders(true);
	    }

	    // XXX Hardcoded - it will be changed in the next step.( costin )

	    // will set the Context
	    contextInterceptor.handleRequest( rrequest );
	    // will set Session 
	    sessionInterceptor.handleRequest( rrequest );
	    
	    // will set all other fields and ServletWrapper
	    mapperInterceptor.handleRequest( rrequest );

	    // do it
	    rrequest.getWrapper().handleRequest(rrequest.getFacade(),
					       rresponse.getFacade());
	    
	    // finish and clean up
	    rresponse.finish();
	    
	    // protocol notification
	    rresponse.endResponse();
	    
	} catch (Exception e) {
	    // XXX
	    // this isn't what we want, we want to log the problem somehow
	    System.out.println("HANDLER THREAD PROBLEM: " + e);
	    e.printStackTrace();
	}
    }

    // XXX need to be changed to use a full sub-request model (costin)
    
    /** Will find the ServletWrapper for a servlet, assuming we already have
     *  the Context. This is used by Dispatcher and getResource - where the Context
     *  is already known.
     */
    int internalRequestParsing( Request req ) {
	return mapperInterceptor.handleRequest( req );
    }
    
    public Context getContextByPath(String path ) {
	// XXX XXX XXX need to create a sub-request !!!!
	// 
	return contextInterceptor.getContextByPath( path );      
    }
    
}
