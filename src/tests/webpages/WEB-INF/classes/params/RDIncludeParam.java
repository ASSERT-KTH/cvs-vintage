package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */
public class RDIncludeParam extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

	// Don't call getParameters !

	String uri="params.Params/include1?a=b";
	out.println("Calling RD.include for: " + uri);
	RequestDispatcher rd=request.getRequestDispatcher(uri);

	rd.include( request, response );

	ServletUtil.printParamValues( "", " ]",
				      "postInclude1:", " = [ ",
				      "", "",
				      " , ",
				      request, out );

	uri="params.Params/include2?a=c&d=e";
	out.println("Calling RD.include for: " + uri);
	rd=request.getRequestDispatcher(uri);

	rd.include( request, response );
	
	ServletUtil.printParamValues( "", " ]",
				      "postInclude2:", " = [ ",
				      "", "",
				      " , ",
				      request, out );
    }

}



