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

package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *  
 */
public class RDIncludeISParams extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

	// No parameter is read 

	String uri="params.InputStreamParams/include1?a=b";
	out.println("Calling RD.include for: " + uri);
	// The POST body should be available in the included
	// servlet - it should not be read before the first
	// getParameter.
	RequestDispatcher rd=request.getRequestDispatcher(uri);

	rd.include( request, response );

	ServletUtil.printParamValues( "", " ]",
				      "postInclude1:", " = [ ",
				      "", "",
				      " , ",
				      request, out );
    }

}



