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



