import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * The simplest possible servlet.
 *
 * @author James Duncan Davidson
 */
public class HelloW extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

	out.println("<h1>Hello World</h1><h1>Hello World</h1><h1>Hello World</h1><h1>Hello World</h1><h1>Hello World</h1>");
    }
}



