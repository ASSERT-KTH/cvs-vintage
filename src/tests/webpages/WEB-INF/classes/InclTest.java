import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *  Simplest possible test of problem configuration:
 *  servlet forwards request to JSP, JSP includes another.
 */
public class InclTest
    extends HttpServlet
{
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException
    {
        RequestDispatcher rd =
 	    this.getServletContext().getRequestDispatcher("/Outer.jsp");
        rd.forward(request, response);
    }
}

