import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Policy extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/plain");
	PrintWriter out=res.getWriter();

	out.println("<h1>Try writing /tmp/foobar</h1>");
	try {
	    FileWriter w=new FileWriter("/tmp/foobar");
	    w.write("Security test - I can write");
	    w.close();   
	} catch( Exception ex ) { 
	    out.println("Exception " + ex + " <pre>");
	    ex.printStackTrace( out );
	    out.println( "</pre>");
	}
    }
}
