
package pageextends;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ExtendServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
            throws ServletException, IOException {

    final PrintWriter out = res.getWriter();

    out.println("<HTML><BODY>");
    out.println("<H1>page extends directive test</H1>");
    out.println("</BODY></HTML>");

  }

  public String getName() {
      return this.getClass().getName();
  }
}
