/*
 * $Id: IncludeMismatch2.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class IncludeMismatch2 extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType("text/plain");
	PrintWriter pwo = res.getWriter();
	pwo.println("LINE1");
	String s = "/servlet/dispatch.Target2";
	RequestDispatcher rd = context.getRequestDispatcher(s);
	try {
	    rd.include(req, res);
	} catch (IllegalStateException e) {
	    pwo.println("SUCCESS");
	}
	pwo.println("LINE2");
    }
}

