/*
 * $Id: PrintWriterTest1Servlet.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class PrintWriterTest1Servlet extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	ServletOutputStream sos = res.getOutputStream();

        sos.println("PreInclude");

        String sLoc = "/servlet/dispatch.PrintWriterTest2Servlet";
        RequestDispatcher rd = context.getRequestDispatcher(sLoc);
        rd.include(req, res);

        sos.println("PostInclude");
    }
}
