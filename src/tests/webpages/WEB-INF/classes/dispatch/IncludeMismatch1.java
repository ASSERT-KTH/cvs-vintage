/*
 * $Id: IncludeMismatch1.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class IncludeMismatch1 extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType("text/plain");
	ServletOutputStream sos = res.getOutputStream();
	sos.println("LINE1");
	String s = "/servlet/dispatch.Target1";
	RequestDispatcher rd = context.getRequestDispatcher(s);
	try {
	    rd.include(req, res);
	} catch (IllegalStateException e) {
	    sos.println("SUCCESS");
	}
	sos.println("LINE2");
    }
}

