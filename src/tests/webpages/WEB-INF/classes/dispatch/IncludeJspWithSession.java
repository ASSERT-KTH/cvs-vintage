/*
 * $Id: IncludeJspWithSession.java,v 1.1 2001/02/23 21:45:47 larryi Exp $
 *
 * Get session and include a jsp.
 * Make sure session cookie isn't lost.
 */

package dispatch;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class IncludeJspWithSession extends HttpServlet

{
    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("text/plain");

        HttpSession mySession = req.getSession(true);

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/Inner.jsp");
        rd.include(req,res);
    }
}
