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

public class SessionShare extends HttpServlet {
    public void init( ServletConfig conf ) throws ServletException {
	super.init( conf );
    }
    
    public void service( HttpServletRequest req, HttpServletResponse res )
	throws ServletException
    {
	try {
	    ServletOutputStream out = res.getOutputStream();
	    res.setContentType("text/html"); // Required for HTTP
	    out.println( "<HTML>" );
	    out.println( "<HEAD>" );
	    out.println( "<META HTTP-EQUIV=\"Refresh\"CONTENT=\"10;URL=http://localhost:8080/test/sessionShare.jsp\">" );
	    out.println( "<TITLE>JSP TEST</TITLE>" );
	    out.println( "</HEAD>" );
	    out.println( "<BODY>" );
	    
	    String App = new String( "This is a string" );

	    HttpSession session = req.getSession( true );
	    if ( session != null ){
		session.putValue( "APP", App );
	    }
	    out.println( "<p>Value of session id is: " + session.getId() + "</p>" );
	    out.println( "<p>Value of string in session is: " + session.getValue( "APP") + "</p>" );
	    out.println( "</BODY>" );
	    
	} catch ( Exception e ) {}
    }

    public void destroy() {
	super.destroy();
    }
}
