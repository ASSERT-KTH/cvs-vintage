import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class CloseWriter extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/plain");
        PrintWriter out= res.getWriter();
        out.write( "Closing Writer" );
        /// XXX trying to simulate the closed outputstream bug,
        // this test runs 2 times hoping it will catch a regression
        // on this issue.. 
        out.close();
    }
}
