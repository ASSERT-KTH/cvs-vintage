import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 */
public class ServletParam extends HttpServlet {

    public void doPost (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	doGet( req, res );
    }
    
    public void doGet (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	// Should be text/xml !
	res.setContentType("text/html");

	PrintWriter out = res.getWriter ();
	out.println("<html>");
	out.println("<body>");

	out.println("<h1>" + getServletConfig().getServletName() + "</h1>");
	
	out.println("<dl>");
	Enumeration initParams=getInitParameterNames();
	while( initParams.hasMoreElements()  ) {
	    String name=(String)initParams.nextElement();
	    String value=getInitParameter( name );
	    out.println("<dt>"+name + "</dt><dd>"+ value + "</dd>");
	}
	out.println("</dl>");
	out.println("</body></html>");
	return;
      }
      
}
