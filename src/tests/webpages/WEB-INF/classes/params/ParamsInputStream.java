package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */
public class ParamsInputStream extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
        throws IOException, ServletException
    {
	PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

	ServletUtil.printParams( request, out );
	out.flush();

	ServletUtil.printBody( request, out );
    }

}



