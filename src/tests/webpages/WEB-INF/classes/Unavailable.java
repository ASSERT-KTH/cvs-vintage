import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Unavailable extends HttpServlet {
    static int attempt=0;
    static boolean success=false;
    
    public void init(ServletConfig conf)
        throws ServletException
    {
        attempt++;
        if( attempt < 2 ) 
            throw new UnavailableException("Testing ", 0);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException,ServletException
    {
        res.setContentType("text/plain");
        ServletOutputStream out = res.getOutputStream();

        if (attempt == 1)
            out.println("Error: UnavailableException not handled correctly");
        else if ((attempt % 2) == 0) {
            attempt++;
            out.println("Hello");
        } else {
            attempt++;
            // in case test is re-run
            throw new UnavailableException("Testing ", 0);
        }

        out.close();
    }
}

