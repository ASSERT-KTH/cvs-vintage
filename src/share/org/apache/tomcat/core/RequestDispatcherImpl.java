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

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Alex Cruikshank [alex@epitonic.com]
 */
public class RequestDispatcherImpl implements RequestDispatcher {
    private StringManager sm = StringManager.getManager("org.apache.tomcat.core");
    
    private Context context;
    
    private Request subRequest = null;

    RequestDispatcherImpl(Context context) {
        this.context = context;
    }

    RequestDispatcherImpl(Request  subReq) {
        this.subRequest = subReq;
    }

    public void forward(ServletRequest request, ServletResponse response)
	throws ServletException, IOException
    {
        Request realRequest = ((HttpServletRequestFacade)request).getRealRequest();
        Response realResponse = ((HttpServletResponseFacade)response).getRealResponse();

	String urlPath=realRequest.getLookupPath();
	String queryString=realRequest.getQueryString();
	
	// according to specs
	if (realResponse.isStarted()) {
            String msg = sm.getString("rdi.forward.ise");
	    throw new IllegalStateException(msg);
        }

	// Pre-pend the context name to give appearance of real request
	urlPath = subRequest.getContext().getPath() + urlPath;

	realRequest.setRequestURI( urlPath );

        addQueryString(realRequest, queryString);
        realRequest.setServletPath(this.subRequest.getServletPath());
	realRequest.setPathInfo(this.subRequest.getPathInfo());

	this.subRequest.getWrapper().handleRequest((HttpServletRequestFacade)request,
						   (HttpServletResponseFacade)response);
    }

    public void include(ServletRequest request, ServletResponse response)
	throws ServletException, IOException
    {
	HttpServletRequest req = (HttpServletRequest)request;
        Request realRequest = ((HttpServletRequestFacade)request).getRealRequest();
	Response realResponse = ((HttpServletResponseFacade)response).getRealResponse();

        // add new query string parameters to request
        // if names are duplicates, new values will be prepended to arrays
        //XXX TODO addQueryString( reqFacade.getRealRequest(), this.queryString );

	IncludedResponse iResponse = new IncludedResponse(realResponse);
	// XXX Make sure we "clone" all usefull informations 
	subRequest.setResponse( realResponse );
	subRequest.setServerName( realRequest.getServerName() );
	
	try {
	    subRequest.getWrapper().handleRequest(subRequest.getFacade() , iResponse);
	} catch( Exception ex) {
	    ex.printStackTrace();
	}
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

	String queryString=req.getQueryString();
        // add query string to existing string
        if ((queryString == null) || (queryString.trim().length() <= 0))
            queryString = inQueryString;
        else
            queryString = inQueryString + "&" + queryString;

	req.setQueryString( queryString );
	req.processQueryString();
    }


}
