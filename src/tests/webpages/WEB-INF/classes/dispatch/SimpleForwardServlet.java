/*
 * $Id: SimpleForwardServlet.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SimpleForwardServlet extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType("text/foobar");
	String s = "/servlet/dispatch.Target1";
	RequestDispatcher rd = context.getRequestDispatcher(s);
	rd.forward(req, res);
    }
}



