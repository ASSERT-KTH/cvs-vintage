/*
 * $Id: IncludeFileServlet.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class IncludeFileServlet extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType("text/foobar");
	PrintWriter pwo = res.getWriter();
	pwo.println("LINE1");
	String s = "/dispatch/foo.html";
	RequestDispatcher rd = context.getRequestDispatcher(s);
	rd.include(req, res);
	pwo.println("LINE2");
    }
}

