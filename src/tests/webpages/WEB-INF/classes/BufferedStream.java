import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class BufferedStream extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/plain");
        ServletOutputStream out = res.getOutputStream();
	for( int i=0; i< 100 ; i++ ) {
	    out.println("====================================================================================================");
	}
    }
}
