package headers;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/** Set headers before getting OS
 *  Bug 473.
 */
public class HeaderOS extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType("test/foobar");
	res.setContentLength(5);

	ServletOutputStream sos=res.getOutputStream();
	sos.println("Hello");
    }


}



