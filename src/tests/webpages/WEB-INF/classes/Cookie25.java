import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Cookie25 extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        res.setContentType("text/plain");
	Cookie c=new Cookie("foo", "bar");
	c.setMaxAge( 60 * 60 * 24 * 26 );
	res.addCookie( c );

        ServletOutputStream out = res.getOutputStream();

	out.println("Set cookie " + c.toString());

        out.close();
    }
}

