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

import org.apache.tomcat.util.StringManager;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/*
  We do a new sub-request for each include() or forward().
  Even if today we take all decisions based only on path, that may
  change ( i.e. a request can take different paths based on authentication,
  headers, etc - other Interceptors may affect it).

  I think this is the correct action - instead of doing a lookup when
  we construct the dispatcher. ( costin )
 */

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Alex Cruikshank [alex@epitonic.com]
 * @author costin@dnt.ro
 */
public class RequestDispatcherImpl implements RequestDispatcher {
    private StringManager sm = StringManager.getManager("org.apache.tomcat.core");
    
    Context context;
    // path dispatchers
    String path;
    String queryString;

    // name dispatchers
    String name;

    /** Used for Context.getRD( path )
     */
    RequestDispatcherImpl(Context context) {
        this.context = context;
    }

    void setPath( String urlPath ) {
	// separate the query string
	int i = urlPath.indexOf("?");
	if( i<0 )
	    this.path=urlPath;
	else {
	    this.path=urlPath.substring( 0,i );
	    int len=urlPath.length();
	    if( i< len )
		this.queryString =urlPath.substring(i + 1);
        }
    }

    void setName( String name ) {
	this.name=name;
    }
    
    public void forward(ServletRequest request, ServletResponse response)
	throws ServletException, IOException
    {
	Request realRequest = ((HttpServletRequestFacade)request).getRealRequest();
        Response realResponse = ((HttpServletResponseFacade)response).getRealResponse();
	// according to specs
	if (realResponse.isStarted()) 
	    throw new IllegalStateException(sm.getString("rdi.forward.ise"));

	// the strange case in a separate method.
	if( name!=null) forwardNamed( request, response );
	
	// from strange spec reasons, forward and include are very different in
	// the way they process the request - if you don't understand the code
	// try to understand the spec.
	
	// in forward case, the Path parametrs of the request are what you would
	// expect, so we just do a new processRequest on the modified request

	// set the context - no need to fire context parsing again
	realRequest.setContext( context );

	// Note that Mapper interceptor uses lookup path. 
	realRequest.setLookupPath( path );
	realRequest.setRequestURI( context.getPath() + path );

	// merge query string as specified in specs - before, it may affect
	// the way the request is handled by special interceptors
	if( queryString != null )
	    addQueryString( realRequest, queryString );
	
	// run the new request through the context manager
	// not that this is a very particular case of forwarding
	context.getContextManager().processRequest(realRequest);

	// CM should have set the wrapper - call it
	// LOG	System.out.println("Forward " + realRequest.getServletPath());
	realRequest.getWrapper().handleRequest((HttpServletRequestFacade)request,
						   (HttpServletResponseFacade)response);
    }

    public void include(ServletRequest request, ServletResponse response)
	throws ServletException, IOException
    {
        Request realRequest = ((HttpServletRequestFacade)request).getRealRequest();
	Response realResponse = ((HttpServletResponseFacade)response).getRealResponse();

	// the strange case in a separate method
	if( name!=null) includeNamed( request, response );
	
	// Implement the spec that "no changes in response, only write"
	// can also be done by setting the response to 0.9 mode ( as Apache does!)
	IncludedResponse iResponse = new IncludedResponse(realResponse);

	// Here the spec is very special, pay attention

	// We need to pass the original request, with all the paths - and the new paths
	// in special attributes.

	// We still need to find out where do we want to go ( today )
	// That means we create a subRequest with the new paths ( since
	// the mapping and aliasing is done on Requests), and run it
	// through prepare.

	// That also means that some special cases ( like the invoker !! )
	// will have to pay attention to the attributes, or we'll get a loop

	Request subRequest=context.getContextManager().createRequest( context, path );

	// I hope no interceptor (or code) in processRequest use any
	// of the original request info ( like Auth headers )
	//
	// XXX We need to clone the request, so that processRequest can
	// make an informed mapping ( Auth, Authorization, etc)
	//
	// This will never work corectly unless we do a full clone - but
	// for simple cases ( no auth, etc) it does

	// note that we also need a dummy response - SessionInterceptors may
	// change something !
	subRequest.setResponse( realResponse );
	
	context.getContextManager().processRequest(subRequest);
	// Now subRequest containse the processed and aliased paths, plus
	// the wrapper that will handle the request.

	// We will use the stack a bit - save all path attributes, set the
	// new values, and after return from wrapper revert to the original

	Object old_request_uri=realRequest.getAttribute("javax.servlet.include.request_uri");
	realRequest.setAttribute("javax.servlet.include.request_uri",
				 path);
	// context.getPath() + path );

	Object old_context_path=realRequest.getAttribute("javax.servlet.include.context_path");
	realRequest.setAttribute("javax.servlet.include.context_path",
				 context.getPath()); // never change anyway - RD can't get out

	Object old_servlet_path=realRequest.getAttribute("javax.servlet.include.servlet_path");
	realRequest.setAttribute("javax.servlet.include.servlet_path",
				 subRequest.getServletPath());
	
	Object old_path_info=realRequest.getAttribute("javax.servlet.include.path_info");
	realRequest.setAttribute("javax.servlet.include.path_info",
				 subRequest.getPathInfo());

	Object old_query_string=realRequest.getAttribute("javax.servlet.include.query_string");
	realRequest.setAttribute("javax.servlet.include.query_string", queryString);

	// Not explicitely stated, but we need to save the old parameters before
	// adding the new ones
	Hashtable old_parameters=realRequest.getParametersCopy();
	// NOTE: it has a side effect of _reading_ the form data - which
	// is against the specs ( you can't read the post until asked for
	// parameters). I see no way of dealing with that -
	// if we don't do it and the included request need a parameter,
	// the form will be read and we'll have no way to know that.

	// IMHO the spec should do something about that - or smarter
	// people should implement the spec. ( costin )

	addQueryString( realRequest, queryString );

// 	System.out.println("Lookup : " + subRequest );
// 	System.out.println();
// 	System.out.println("Req: " + realRequest);
 	// now it's really strange: we call the wrapper on the subrequest
	// for the realRequest ( since the real request will still have the
	// original handler/wrapper )
	subRequest.getWrapper().handleRequest(realRequest.getFacade() , iResponse);

	// After request, we want to restore the include attributes - for
	// chained includes.
	realRequest.setParameters( old_parameters);

	replaceAttribute( realRequest, "javax.servlet.include.request_uri",
				 old_request_uri);
	replaceAttribute( realRequest, "javax.servlet.include.context_path",
				 old_context_path); 
	replaceAttribute( realRequest, "javax.servlet.include.servlet_path",
				 old_servlet_path);
	replaceAttribute( realRequest, "javax.servlet.include.path_info",
				 old_path_info);
	replaceAttribute( realRequest, "javax.servlet.include.query_string",
				 old_query_string);
    }

	

    /** Named dispatcher include
     *  Separate from normal include - which is still too messy
     */
    public void includeNamed(ServletRequest request, ServletResponse response)
	throws ServletException, IOException
    {
	// Use the original request - as in specification !

	// We got here if name!=null, so assert it
	ServletWrapper wrapper = context.getServletByName( name );

	wrapper.handleRequest( (HttpServletRequestFacade)request,
			       (HttpServletResponseFacade)response);

    }

    /** Named forward
     */
    public void forwardNamed(ServletRequest request, ServletResponse response)
	throws ServletException, IOException
    {
	ServletWrapper wrapper = context.getServletByName( name );
	wrapper.handleRequest( (HttpServletRequestFacade)request,
			       (HttpServletResponseFacade)response);
    }    

    /**
     * Adds a query string to the existing set of parameters.
     * The additional parameters represented by the query string will be
     * merged with the existing parameters.
     * Used by the RequestDispatcherImpl to add query string parameters
     * to the request.
     *
     * @param inQueryString URLEncoded parameters to add
     */
    void addQueryString(Request req, String inQueryString) {
        // if query string is null, do nothing
        if ((inQueryString == null) || (inQueryString.trim().length() <= 0))
            return;

	Hashtable newParams = HttpUtils.parseQueryString(queryString);
	Hashtable parameters= req.getParameters();

	// add new to old ( it alters the original hashtable in request)
	Enumeration e=newParams.keys();
	while(e.hasMoreElements() ) {
	    String key=(String)e.nextElement();
	    parameters.put( key, newParams.get(key));
	}
    }

    /** Restore attribute - if value is null, remove the attribute.
     *  X Maybe it should be the befavior of setAttribute() - it is not
     *  specified what to do with null.
     *  ( or it is - null means no value in getAttribute, so setting to
     *    null should mean setting to no value. ?)
     */
    private void replaceAttribute( Request realRequest, String name, Object value) {
	if( value == null )
	    realRequest.removeAttribute( name );
	else
	    realRequest.setAttribute( name, value );
    }

}
