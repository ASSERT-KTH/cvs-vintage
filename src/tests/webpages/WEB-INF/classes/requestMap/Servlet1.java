/*
 * $Id: Servlet1.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
 */

package requestMap;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Servlet1 extends HttpServlet {
    private String servletInfo = "Servlet1";

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }

    public String getServletInfo() {
        return this.servletInfo;
    }
    
    public void doGet(HttpServletRequest request,
        HttpServletResponse response)
    throws IOException {
	PrintWriter pw = response.getWriter();

	pw.println("Servlet: " + getServletInfo());
    }
}
