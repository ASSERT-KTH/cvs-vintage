import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Post extends HttpServlet {
    public void init( ServletConfig conf ) throws ServletException {
	super.init( conf );
    }
    
    public void service( HttpServletRequest req, HttpServletResponse res )
	throws ServletException
    {
	try {
	    BufferedReader input;
	    PrintWriter output;
	    String line;
	    String requestStream;
	    
	    input = req.getReader();
	    requestStream="";
                
	    while ((line=input.readLine()) != null ) {
		System.out.println(line);
		requestStream = requestStream + line + "\n";
	    }
	    System.out.println("END reading " + requestStream );
	    
	    ServletOutputStream out = res.getOutputStream();
	    System.out.println("GET OS");
	    res.setContentType("text/html"); // Required for HTTP
	    System.out.println("SCT");
	    out.println("<h1>Hello</h1>");
	    out.println("<h1>Hello</h1>");
	    out.println("<h1>Hello</h1>");
	    System.out.println("DONE");
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void destroy() {
	super.destroy();
    }
}
