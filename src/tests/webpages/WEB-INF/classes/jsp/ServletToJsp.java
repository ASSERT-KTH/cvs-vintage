
package jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ServletToJsp extends HttpServlet {

    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) {
        try {
            /*
            response.setContentType("text/plain");
            PrintWriter pwo = response.getWriter();
            pwo.println("from ServletToJsp servlet");
            */
            // Uncommenting the above and trying to forward below
            // will result in [expetcted] error..
            
            // Set the attribute and Forward to hello.jsp
            request.setAttribute ("servletName", "ServletToJsp");
            getServletConfig().getServletContext().
                getRequestDispatcher("/jsp/jsptoserv/hello.jsp").
                    forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }
}
