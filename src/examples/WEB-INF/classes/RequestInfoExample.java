/* $Id: RequestInfoExample.java,v 1.5 2004/02/22 22:57:59 billbarker Exp $
 *
 */
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
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.HTMLFilter;

/**
 * Example servlet showing request information.
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class RequestInfoExample extends HttpServlet {


    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<head>");

        String title = rb.getString("requestinfo.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");

        // img stuff not req'd for source code html showing
	// all links relative!

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue
	
        out.println("<a href=\"/examples/servlets/reqinfo.html\">");
        out.println("<img src=\"/examples/images/code.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"/examples/servlets/index.html\">");
        out.println("<img src=\"/examples/images/return.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"return\"></a>");

        out.println("<h3>" + title + "</h3>");
        out.println("<table border=0><tr><td>");
        out.println(rb.getString("requestinfo.label.method"));
        out.println("</td><td>");
        out.println(request.getMethod());
        out.println("</td></tr><tr><td>");
        out.println(rb.getString("requestinfo.label.requesturi"));
        out.println("</td><td>");        
        out.println(HTMLFilter.filter(request.getRequestURI()));
        out.println("</td></tr><tr><td>");        
        out.println(rb.getString("requestinfo.label.protocol"));
        out.println("</td><td>");        
        out.println(request.getProtocol());
        out.println("</td></tr><tr><td>");
        out.println(rb.getString("requestinfo.label.pathinfo"));
        out.println("</td><td>");        
        out.println(HTMLFilter.filter(request.getPathInfo()));
        out.println("</td></tr><tr><td>");
        out.println(rb.getString("requestinfo.label.remoteaddr"));
        out.println("</td><td>");
 	out.println(request.getRemoteAddr());
 
 	String cipherSuite=
 	    (String)request.getAttribute("javax.servlet.request.cipher_suite");
 
 	if(cipherSuite!=null){
 	    out.println("</td></tr><tr><td>");	
 	    out.println("SSLCipherSuite info");
 	    out.println("</td><td>");
 	    out.println(request.getAttribute("javax.servlet.request.cipher_suite"));
 	}
        out.println("</td><td>");                
        out.println(request.getRemoteAddr());
        out.println("</td></tr></table>");
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }

}

