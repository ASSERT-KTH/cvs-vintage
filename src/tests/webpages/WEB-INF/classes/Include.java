import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Include extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
	
	out.println("Before include");
	RequestDispatcher rd=getServletContext().getRequestDispatcher("/realPath.jsp");
	rd.include( req, res );
	out.println("After include ");
        out.close();
    }
}

