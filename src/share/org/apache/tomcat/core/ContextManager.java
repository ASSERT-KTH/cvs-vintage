/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/ContextManager.java,v 1.1 1999/10/09 00:30:02 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:30:02 $
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

import org.apache.tomcat.core.*;
import org.apache.tomcat.net.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */

public class ContextManager  implements Server {

    private StringManager sm =
        StringManager.getManager(Constants.Package);

    private Context defaultContext;
    private Hashtable contexts = new Hashtable();
    private Hashtable contextMaps = new Hashtable();
    private String serverInfo = null;

    // Used by Contexts
    String hostname;
    int port;

    public ContextManager() {

    }
    
    /**
     * Gets the server info string for this server
     */

    public String getServerInfo() {
        return serverInfo;
	// that is the behavior in the current tomcat
	// return defaultContext.getEngineHeader();

    }

    /**
     * Sets the server info string for this server. This string must
     * be of the form <productName>/<productVersion> [(<optionalInfo>)]
     */

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    public URL getDocumentBase() {
	return defaultContext.getDocumentBase();
    }

    /**
     * Sets the document base of the default context for this server.
     */

    public void setDocumentBase(URL docBase) {
	defaultContext.setDocumentBase(docBase);
    }
    
    /**
     * Gets the default Context for this server
     */
    public Context getDefaultContext() {
        return defaultContext;
    }

    /**
     * Gets the default Context for this server
     */
    public void setDefaultContext(Context ctx) {
        defaultContext=ctx;
    }

    /**
     * Get the names of all the contexts in the system.
     */
    
    public Enumeration getContextNames() {
        return contexts.keys();
    }

    /**
     * Get a context by it's name
     */
    
    public Context getContext(String name) {
	return (Context)contexts.get(name);
    }

    /**
     * Gets the context that is responsible for requests for a
     * particular path.
     */
    
    public Context getContextByPath(String path) {
	String realPath = path;
	Context ctx = null;

	// XXX
	// needs help ... this needs to be optimized out.

        lookup:
	do {
	    ctx = (Context)contextMaps.get(path);
	    if (ctx == null) {
	        int i = path.lastIndexOf('/');
		if (i > -1 && path.length() > 1) {
		    path = path.substring(0, i);
		    if (path.length() == 0) {
		        path = "/";
		    }
		} else {
		    // path too short
		    break lookup;
		}
	    } else {
	    }
	} while (ctx == null);

	if (ctx == null) {
	    ctx = defaultContext;
	}

	return ctx;
    }

    public Context addContext(String path, URL docBase) {
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

	Context context = new Context(this, path);

	if (docBase != null) {
	    context.setDocumentBase(docBase);
	}

	// check to see if defaultContext

	if (path.length() == 0) {
	    contexts.put(
                org.apache.tomcat.core.Constants.Context.Default.Name,
	        context);
	} else {
	    contexts.put(path, context);
	    contextMaps.put(path, context);
	}

	return context;
    }

    /**
     * Removes a context from service
     */
    
    public void removeContext(String name) {
	if (name.equals(
	    org.apache.tomcat.core.Constants.Context.Default.Name)){
	    throw new IllegalArgumentException(name);
	}

	Context context = (Context)contexts.get(name);

	if(context != null) {
	    context.shutdown();
	    contexts.remove(name);
	    contextMaps.remove(name);
	}
    }

    public void setPort(int port) {
	this.port=port;
    }

    public int getPort() {
	return port;
    }

    public void setHostName( String host) {
	this.hostname=host;
    }

    public String getHostName() {
	return hostname;
    }

    /** Common for all connectors, needs to be shared in order to avoid
	code duplication
    */
    public void service( RequestImpl rrequest, ResponseImpl rresponse ) {
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

	    // resolve the server that we are for
	    String path = rrequest.getRequestURI();
	    
	    Context ctx= this.getContextByPath(path);
	    
	    // final fix on response & request
	    //		rresponse.setServerHeader(server.getServerHeader());
	    
	    String ctxPath = ctx.getPath();
	    String pathInfo =path.substring(ctxPath.length(),
					    path.length());
	    //    don't do headers if request protocol is http/0.9
	    if (rrequest.getProtocol() == null) {
		rresponse.setOmitHeaders(true);
	    }
	    
	    // do it
	    //		System.out.println( request + " " + rresponse );
	    ctx.handleRequest(rrequest, rresponse);
	    
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

    
}
