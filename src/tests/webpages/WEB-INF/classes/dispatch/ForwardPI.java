/*
 * $Id: ForwardPI.java,v 1.1 2001/08/26 01:45:39 costin Exp $
 */


package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import RequestDump;

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



