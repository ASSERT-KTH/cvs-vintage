import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class servletToJsp extends HttpServlet {

    public void doGet (HttpServletRequest request,
		       HttpServletResponse response) {

	try {
	    // Set the attribute and Forward to hello.jsp
	    request.setAttribute ("servletName", "servletToJsp");
	    getServletConfig().getServletContext().getRequestDispatcher("/jsp/jsptoserv/hello.jsp").forward(request, response);
	} catch (Exception ex) {
	    ex.printStackTrace ();
	}
    }
}
