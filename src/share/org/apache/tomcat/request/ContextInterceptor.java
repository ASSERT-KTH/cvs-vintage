/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/request/Attic/ContextInterceptor.java,v 1.3 2000/01/10 22:01:19 costin Exp $
 * $Revision: 1.3 $
 * $Date: 2000/01/10 22:01:19 $
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


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.net.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class ContextInterceptor implements RequestInterceptor {
    ContextManager cm;
    
    public ContextInterceptor(ContextManager cm) {
	this.cm=cm;
    }

    public int handleRequest( Request rrequest ) {
	// resolve the server that we are for
	String path = rrequest.getRequestURI();
	
	Context ctx= this.getContextByPath(path);
	
	// final fix on response & request
	//		rresponse.setServerHeader(server.getServerHeader());
	
	String ctxPath = ctx.getPath();
	String pathInfo =path.substring(ctxPath.length(),
					    path.length());
	rrequest.setContext(ctx);
	rrequest.updatePaths();
	return OK;
    }


    // XXX XXX XXX need to fix this - it is used by getContext(String path) (costin)
    
    /**
     * Gets the context that is responsible for requests for a
     * particular path.  If no specifically assigned Context can be
     * identified, returns the default Context.
     *
     * @param path The path for which a Context is requested
     */
    public Context getContextByPath(String path) {
	String realPath = path;
	Context ctx = null;

	// XXX
	// needs help ... this needs to be optimized out.

        lookup:
	do {
	    ctx = cm.getContext(path);
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

	// no map - root context
	if (ctx == null) {
	    ctx = cm.getContext( "" );
	}

	return ctx;
    }

    
}
