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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 * @author Jason Hunter <jch@eng.sun.com>
 */
public class RequestDump extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
	doGet( request, response );
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
	dumpRequest(this, request, response );
    }
    
    public static void dumpRequest(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
	Enumeration enum, names, e;

	ServletContext context=servlet.getServletContext();
        out.println();
        out.println("<h2>Request info</h2>");

	out.println("<table border='1' id='req.info'>");

        out.println("<tr><td>Servlet Name</td><td>" + servlet.getServletName() + "</td></tr>");
        out.println("<tr><td>Protocol</td><td>" + request.getProtocol() + "</td></tr>");
        out.println("<tr><td>Scheme</td><td>" + request.getScheme() + "</td></tr>");
        out.println("<tr><td>Server Name</td><td>" + request.getServerName() + "</td></tr>");
        out.println("<tr><td>Server Port</td><td>" + request.getServerPort() + "</td></tr>");
        out.println("<tr><td>Server Info</td><td>" + context.getServerInfo() + "</td></tr>");
        out.println("<tr><td>Remote Addr</td><td>" + request.getRemoteAddr() + "</td></tr>");
        out.println("<tr><td>Remote Host</td><td>" + request.getRemoteHost() + "</td></tr>");
        out.println("<tr><td>Character Encoding</td><td>" + request.getCharacterEncoding() + "</td></tr>");
        out.println("<tr><td>Content Length</td><td>" + request.getContentLength() + "</td></tr>");
        out.println("<tr><td>Content Type</td><td>"+ request.getContentType() + "</td></tr>");
        out.println("<tr><td>Locale</td><td>"+ request.getLocale() + "</td></tr>");
        out.println("<tr><td>Default Response Buffer</td><td>"+ response.getBufferSize() + "</td></tr>");
        out.println("<tr><td>Request Is Secure</td><td>" + request.isSecure() + "</td></tr>");
        out.println("<tr><td>Auth Type</td><td>" + request.getAuthType() + "</td></tr>");
        out.println("<tr><td>HTTP Method</td><td>" + request.getMethod() + "</td></tr>");
        out.println("<tr><td>Remote User</td><td>" + request.getRemoteUser() + "</td></tr>");
        out.println("<tr><td>Request URI</td><td>" + request.getRequestURI() + "</td></tr>");
        out.println("<tr><td>Context Path</td><td>" + request.getContextPath() + "</td></tr>");
        out.println("<tr><td>Servlet Path</td><td>" + request.getServletPath() + "</td></tr>");
        out.println("<tr><td>Path Info</td><td>" + request.getPathInfo() + "</td></tr>");
	out.println("<tr><td>Path Trans</td><td>" + request.getPathTranslated() + "</td></tr>");
        out.println("<tr><td>Query String</td><td>" + request.getQueryString() + "</td></tr>");

	HttpSession session = request.getSession(false);
	if( session!=null ) {
	    out.println("<tr><td>Requested Session Id</td><td>" +
                    request.getRequestedSessionId() + "</td></tr>");
	    out.println("<tr><td>Current Session Id</td><td>" + session.getId() + "</td></tr>");
	    out.println("<tr><td>Session Created Time</td><td>" + session.getCreationTime() + "</td></tr>");
	    out.println("<tr><td>Session Last Accessed Time</td><td>" +
			session.getLastAccessedTime() + "</td></tr>");
	    out.println("<tr><td>Session Max Inactive Interval Seconds</td><td>" +
			session.getMaxInactiveInterval() + "</td></tr>");
	}

	out.println("</table>");
	
        out.println("<h2>Parameters</h2>");

	out.println("<table border='1' id='req.params'>");
        e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String[] values = request.getParameterValues(key);
            out.print("<tr><td>" + key + "</td><td>");
            for(int i = 0; i < values.length; i++) {
                out.print("<span>" + values[i] + "</span> ");
            }
            out.println("</td></tr>");
        }
        out.println("</table>");

        out.println("<h2>Headers</h2>");
	out.println("<table border='1' id='req.headers'>");
	e = request.getHeaderNames();
	while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = request.getHeader(key);
	    out.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>" );
        }
	out.println("</table>");

        out.println("<h2>Cookies</h2>");
	out.println("<table border='1' id='req.cookies'>");
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
	    out.println("<tr><td>" + cookie.getName() + "</td><td>" + cookie.getValue() + "</td></tr>" );
        }
	out.println("</table>");

	if( session != null ) {
	    out.println("<h2>Session</h2>");
	    out.println("<table border='1' id='req.cookies'>");
	    names = session.getAttributeNames();
	    while (names.hasMoreElements()) {
		String name = (String) names.nextElement();
		out.println("<tr><td>" + name + "</td><td>" +
			    session.getAttribute(name) + "</td></tr>" );
	    }
	    out.println("</table>");
	}
	
        out.println("<h2>Request attributes</h2>");
	out.println("<table border='1' id='req.attributes'>");
	e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            Object value = request.getAttribute(key);
	    out.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>" ); 
        }
	out.println("</table>");

	out.println("<h2>Context attributes:</h2>");
	out.println("<table border='1' id='ctx.attributes'>");
	enum = context.getAttributeNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
            Object value = context.getAttribute(key);
	    out.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>" ); 
	}
	out.println("</table>");
	

	out.println("<h2>Servlet init parameters</h2>");
	out.println("<table border='1' id='servlet.init.params'>");
	e = servlet.getInitParameterNames();
	while (e.hasMoreElements()) {
	    String key = (String)e.nextElement();
	    String value = servlet.getInitParameter(key);
	    out.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>" ); 
	}
	out.println("</table>");

	out.println("<h2>Context init parameters</h2>");
	out.println("<table border='1' id='ctx.init.params'>");

	enum = context.getInitParameterNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
            Object value = context.getInitParameter(key);
	    out.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>" ); 
	}
	out.println("</table>");


    }
}

