import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ResponseError extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
	res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong data");
    }
}

