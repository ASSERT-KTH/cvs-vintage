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
package org.apache.tomcat.servlets;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/*
  Right now it is not well-integrated, and it's not configurable.
  If someone needs to customize this it should be easy to
  take the steps to do Class.forName() and all rest.

  This code was originally hardcoded in ServletWrapper ( where
  service() is called.
 */

/**
 *  Report an error - if no other error page was set up or
 *  if an error happens in an error page.
 */
public class DefaultErrorPage extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException
    {
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);

	Throwable e= (Throwable)request.getAttribute("tomcat.error.throwable");
	
	pw.println("<b>Internal Servlet Error:</b><br>");
        pw.println("<pre>");
	if( e != null ) 
	    e.printStackTrace(pw);
	pw.println("</pre>");

        if (e instanceof ServletException) {
	    printRootCause((ServletException) e, pw);
	}
	
	response.sendError(500, sw.toString());
    }

    /** A bit of recursion - print all traces in the stack
     */
    void printRootCause(ServletException e, PrintWriter out) {
        Throwable cause = e.getRootCause();

	if (cause != null) {
	    out.println("<b>Root cause:</b>");
	    out.println("<pre>");
	    cause.printStackTrace(out);
	    out.println("</pre>");

	    if (cause instanceof ServletException) {
		printRootCause((ServletException)cause, out);  // recurse
	    }
	}
    }
}
