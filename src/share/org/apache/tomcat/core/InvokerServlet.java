/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/InvokerServlet.java,v 1.3 1999/11/06 00:19:52 costin Exp $
 * $Revision: 1.3 $
 * $Date: 1999/11/06 00:19:52 $
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

import org.apache.tomcat.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

public class InvokerServlet extends HttpServlet {
    
    private StringManager sm =
	StringManager.getManager(Constants.Package);
    private Context context;
    private Container container;
    
    public void init() throws ServletException {
	ServletContextFacade facade =
	    (ServletContextFacade)getServletContext();
        context = facade.getRealContext();
        container = context.getContainer();
    }
    
    public void service(HttpServletRequest request,
        HttpServletResponse response)
    throws ServletException, IOException {
        String requestPath = request.getRequestURI();
	String pathInfo = (String)request.getAttribute(
            Constants.Attribute.PathInfo);

	if (pathInfo == null) {
	    pathInfo = request.getPathInfo();
	}

	String includedRequestURI = (String)request.getAttribute(
	    Constants.Attribute.RequestURI);
	boolean inInclude = false;

	if (includedRequestURI != null) {
	    inInclude = true;
	} else {
	    inInclude = false;
	}

        String servletName = "";
        String newServletPath = "";
        String newPathInfo = "";

        // XXX
        // yet another example of substring overkill -- we can do
        // this better....

        if (pathInfo != null &&
            pathInfo.startsWith("/") &&
	    pathInfo.length() > 2) {
            servletName = pathInfo.substring(1, pathInfo.length());

            if (servletName.indexOf("/") > -1) {
                servletName =
		    servletName.substring(0, servletName.indexOf("/"));
            }

	    if (! inInclude) {
		newServletPath = request.getServletPath() +
		    "/" + servletName;
	    } else {
		newServletPath = (String)request.getAttribute
		    (Constants.Attribute.ServletPath)  + "/" +
                    servletName;
	    }
	    
            // XXX
            // oh, very sloppy here just catching the exception... Do
            // this for real...

            try {
		if (inInclude) {
		    newPathInfo = includedRequestURI.substring(
			newServletPath.length(),
			includedRequestURI.length());
		} else {
		    newPathInfo = requestPath.substring(
			context.getPath().length() +
			    newServletPath.length(),
			requestPath.length());
		}
		
		int i = newPathInfo.indexOf("?");

		if (i > -1) {
		    newPathInfo = newPathInfo.substring(0, i);
		}

		if (newPathInfo.length() < 1) {
		    newPathInfo = null;
		}
            } catch (Exception e) {
                newPathInfo = null;
            }
        } else {
            // theres not enough information here to invoke a servlet
            doError(response);

            return;
        }

        // try the easy one -- lookup by name
        
        ServletWrapper wrapper = container.getServletByName(servletName);

        if (wrapper == null) {
            // try the more forceful approach

            wrapper = container.getServletAndLoadByName(servletName);
        }

        if (wrapper == null) {
            // we are out of luck

            doError(response);

            return;
        }

        HttpServletRequestFacade requestfacade =
	    (HttpServletRequestFacade)request;
        HttpServletResponseFacade responsefacade =
	    (HttpServletResponseFacade)response;
	Request realRequest = requestfacade.getRealRequest();
	Response realResponse = responsefacade.getRealResponse();

	// The saved servlet path, path info are for cases in which a
	// request dispatcher forwards through the invoker. This is
	// some seriously sick code here that needs to be done
	// better, but this will do the trick for now.
	
	String savedServletPath = (String)realRequest.getAttribute(
            Constants.Attribute.ServletPath);
	String savedPathInfo = (String)realRequest.getAttribute(
            Constants.Attribute.PathInfo);

	if (! inInclude) {
	    realRequest.setServletPath(newServletPath);
	    realRequest.setPathInfo(newPathInfo);
	} else {
	    if (newServletPath != null) {
		realRequest.setAttribute(
                    Constants.Attribute.ServletPath, newServletPath);
	    }

	    if (newPathInfo != null) {
		realRequest.setAttribute(
                    Constants.Attribute.PathInfo, newPathInfo);
	    }

	    if (newPathInfo == null) {
		// Can't store a null, so remove for same effect

		realRequest.removeAttribute(
                    Constants.Attribute.PathInfo);
	    }
	}

        wrapper.handleRequest(requestfacade, responsefacade);

	if (inInclude) {
	    if (savedServletPath != null) {
		realRequest.setAttribute(
                    Constants.Attribute.ServletPath, savedServletPath);
	    } else {
		realRequest.removeAttribute(
                    Constants.Attribute.ServletPath);
	    }

	    if (savedPathInfo != null) {
		realRequest.setAttribute(
                    Constants.Attribute.PathInfo, savedPathInfo);
	    } else {
		realRequest.removeAttribute(
                    Constants.Attribute.PathInfo);
	    }
	}
    }

    public void doError(HttpServletResponse response)
    throws ServletException, IOException {
        response.sendError(404);
    }    
}
