package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */
public class RDInclude extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

	out.println("RequestDispatcher view: ");
	ServletUtil.printParams( request, out );

	String uri="Params?a=b";
	out.println("Calling RD.include for: " + uri);
	RequestDispatcher rd=request.getRequestDispatcher(uri);

	rd.include( request, response );

	out.println("After include ");
	ServletUtil.printParams( request, out );

	uri="Params?a=c&d=e";
	out.println("Calling RD.include for: " + uri);
	rd=request.getRequestDispatcher(uri);

	rd.include( request, response );
	
	out.println("After include ");
	ServletUtil.printParams( request, out );
    }

}



