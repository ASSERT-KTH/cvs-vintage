/*
 * $Id: Mismatch1.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Mismatch1 extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
	res.setContentType("text/plain");
	PrintWriter pwo = res.getWriter();
	pwo.println("PWO OUT");
	try {
	    ServletOutputStream sos = res.getOutputStream();
	    sos.println("NO");
	} catch (IllegalStateException ise) {
	    pwo.println("YES");
	}
	pwo.println("FINISH");
    }
}
