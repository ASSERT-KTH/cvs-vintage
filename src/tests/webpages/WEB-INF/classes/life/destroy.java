package life;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class destroy extends HttpServlet {

    public void init() throws ServletException {
	System.out.println("GPDK init");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");
	out.println("<title>Hello, World!</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
 	out.println("<h1>Hello World!</h1>");	
        out.println("<body>");
	out.println("</html>");
    }

    public void destroy() {
	System.out.println("GPDK destroy");
    }

}
