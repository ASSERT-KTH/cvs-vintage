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

        out.println("<html>");
        out.println("<head>");

	out.println("<title>Hello</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<body>");

        out.println("<h1>Hello</h1>");
        out.println("</body>");
        out.println("</html>");
    }
}



