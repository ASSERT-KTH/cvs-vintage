/*
 * $Id: SimpleIncludeServlet.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SimpleIncludeServlet extends HttpServlet {

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
	String s = "/servlet/dispatch.Target1";
	RequestDispatcher rd = context.getRequestDispatcher(s);
	rd.include(req, res);
	pwo.println("LINE2");
	rd.include(req, res);
	pwo.println("LINE3");
    }
}



