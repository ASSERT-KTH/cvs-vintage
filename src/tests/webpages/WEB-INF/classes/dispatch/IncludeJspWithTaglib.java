/*
 * $Id: IncludeJspWithTaglib.java,v 1.1 2001/10/09 02:31:45 larryi Exp $
 *
 * Include a jsp that uses a taglib.
 * Make sure XML parser is available to Jasper.
 */

package dispatch;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class IncludeJspWithTaglib extends HttpServlet

{
    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("text/plain");

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/dispatch/msgTag.jsp");
        rd.include(req,res);
    }
}
