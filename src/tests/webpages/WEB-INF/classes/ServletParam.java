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

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 */
public class ServletParam extends HttpServlet {

    public void doPost (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	doGet( req, res );
    }
    
    public void doGet (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	// Should be text/xml !
	res.setContentType("text/html");

	PrintWriter out = res.getWriter ();
	out.println("<html>");
	out.println("<body>");

	out.println("<h1>" + getServletConfig().getServletName() + "</h1>");
	
	out.println("<dl>");
	Enumeration initParams=getInitParameterNames();
	while( initParams.hasMoreElements()  ) {
	    String name=(String)initParams.nextElement();
	    String value=getInitParameter( name );
	    out.println("<dt>"+name + "</dt><dd>"+ value + "</dd>");
	}
	out.println("</dl>");
	out.println("</body></html>");
	return;
      }
      
}
