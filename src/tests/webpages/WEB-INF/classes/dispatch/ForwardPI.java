/*
 * $Id: ForwardPI.java,v 1.2 2003/10/02 02:52:03 larryi Exp $
 */


package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Test FORWARD with a path info
 *
 */
public class ForwardPI extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	req.setAttribute("originalPI", req.getPathInfo());
	RequestDispatcher rd = getServletContext().getRequestDispatcher("/servlet/RequestDump");
	rd.forward(req, res);
    }

}



