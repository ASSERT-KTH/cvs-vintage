package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *  Double include
 */
public class IncludeIncludeISParams extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

	// No parameter is read 

	String uri="params.RDIncludeISParams?a=x";
	out.println("Calling RD.include for: " + uri);
	RequestDispatcher rd=request.getRequestDispatcher(uri);

	rd.include( request, response );

	ServletUtil.printParamValues( "", " ]",
				      "postInclude0:", " = [ ",
				      "", "",
				      " , ",
				      request, out );
    }

}



