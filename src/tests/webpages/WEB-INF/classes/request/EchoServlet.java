/* $Id: EchoServlet.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 *
 */

package request;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * The simplest possible servlet.
 *
 * @author 
 */

public class EchoServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println(request.getQueryString());
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
System.out.println("EchoServletr: inside doPost()");
        doGet(request, response);
    }
    
}



