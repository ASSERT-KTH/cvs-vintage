/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/JspWrapper.java,v 1.4 2000/02/08 18:50:45 costin Exp $
 * $Revision: 1.4 $
 * $Date: 2000/02/08 18:50:45 $
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
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/* It is used:
   - Context addJsp/removeJsp - to "name" jsps for use in web.xml
   - Context.loadOnStartup 
   - MapperInterceptor - to map between "declared" jsps that are used in mappings

   It is a very strange class - we need to revisit the way we implement
   the "mapping to jsp" - for now it's just out in a separate class to not
   affect the rest of the code.
*/


/**
 * Specialized wrapper for Jsp - used only by Context when a Jsp is declared
 * in web.xml
 * The only use of this class is in load on Startup, and to keep the extra path info.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@dnt.ro
 */
public class JspWrapper extends ServletWrapper {

    private String path = null;

    JspWrapper(Context context) {
        super(context);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    
    public void loadServlet()
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, ServletException
    {
        // Check if this is a declared JSP, they get special treatment
	if( servletClass!=null || servletClassName !=null)
	    super.loadServlet();

	// A Jsp initialized in web.xml -

	// Log ( since I never saw this code called, let me know if it does
	// for you )
	System.out.println("Initializing JSP with JspWrapper");
	
	// Ugly code to trick JSPServlet into loading this.

	// XXX XXX XXX
	// core shouldn't depend on a particular connector!
	// need to find out what this code does!
	
	// XXX XXX find a better way !!!
	//	RequestAdapterImpl reqA=new RequestAdapterImpl();
	//	ResponseAdapterImpl resA=new ResponseAdapterImpl();
	
	RequestImpl request = new RequestImpl();
	ResponseImpl response = new ResponseImpl();
	request.recycle();
	response.recycle();
	
	//	request.setRequestAdapter( reqA );
	// response.setResponseAdapter( resA );
	
	request.setResponse(response);
	response.setRequest(request);
	
	String requestURI = path + "?" +
	    Constants.JSP.Directive.Compile.Name + "=" +
	    Constants.JSP.Directive.Compile.Value;
	
	request.setRequestURI(context.getPath() + path);
	request.setQueryString( Constants.JSP.Directive.Compile.Name + "=" +
			     Constants.JSP.Directive.Compile.Value );
	
	request.setContext(context);
	request.getSession(true);
	
	RequestDispatcher rd =
	    config.getServletContext().getRequestDispatcher(requestURI);
	
	try {
	    rd.forward(request.getFacade(), response.getFacade());
	} catch (ServletException se) {
	} catch (IOException ioe) {
	}
    }

    public void handleRequest(final HttpServletRequestFacade request,
			      final HttpServletResponseFacade response)
	throws IOException
    {
        synchronized (this) {
	    // Check if this is a JSP, they get special treatment
	    if( servletClass!=null || servletClassName !=null)
		super.handleRequest(request, response);

	    // "Special" JSP
	    String requestURI = path + request.getPathInfo();
	    RequestDispatcher rd = request.getRequestDispatcher(requestURI);
	    
	    try {
		if (! response.getRealResponse().isStarted())
		    rd.forward(request, response);
		else
		    rd.include(request, response);
		
	    } catch (ServletException se) {
		se.printStackTrace();
		response.sendError(404);
	    } catch (IOException ioe) {
		ioe.printStackTrace();
		response.sendError(404);
	    }
	    return;
	}
    }
}
