import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class error402 extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
         res.sendError(HttpServletResponse.SC_PAYMENT_REQUIRED);
    }
}
