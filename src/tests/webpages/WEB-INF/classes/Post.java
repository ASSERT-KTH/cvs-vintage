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
import javax.servlet.*;
import javax.servlet.http.*;

public class Post extends HttpServlet {
    public void init( ServletConfig conf ) throws ServletException {
	super.init( conf );
    }
    
    public void service( HttpServletRequest req, HttpServletResponse res )
	throws ServletException
    {
	try {
	    BufferedReader input;
	    PrintWriter output;
	    String line;
	    String requestStream;
	    
	    input = req.getReader();
	    requestStream="";
                
	    while ((line=input.readLine()) != null ) {
		System.out.println(line);
		requestStream = requestStream + line + "\n";
	    }
	    System.out.println("END reading " + requestStream );
	    
	    ServletOutputStream out = res.getOutputStream();
	    System.out.println("GET OS");
	    res.setContentType("text/html"); // Required for HTTP
	    System.out.println("SCT");
	    out.println("<h1>Hello</h1>");
	    out.println("<h1>Hello</h1>");
	    out.println("<h1>Hello</h1>");
	    System.out.println("DONE");
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void destroy() {
	super.destroy();
    }
}
