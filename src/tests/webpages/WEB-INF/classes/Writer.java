import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Writer extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/plain");
        PrintWriter  out = res.getWriter();
	for( int i=0; i< 100 ; i++ ) {
	    out.println("====================================================================================================");
	}
    }
}
