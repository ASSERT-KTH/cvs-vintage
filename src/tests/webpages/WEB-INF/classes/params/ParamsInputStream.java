package request;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */
public class ParamsInputStream extends HttpServlet {

    public void service(HttpServletRequest request,
			HttpServletResponse response)
        throws IOException, ServletException
    {
	PrintWriter out = response.getWriter();
        response.setContentType("text/plain");
	Enumeration paramN=request.getParameterNames();

	while( paramN.hasMoreElements() ) {
	    String name=(String)paramN.nextElement();
	    String all[]=request.getParameterValues(name);

	    out.print(name);
	    out.print(" = [ " );
	    for( int i=0; i<all.length; i++ ) {
		if( i>0 ) out.print( " , ");
		out.print( all[i] );
	    }
	    out.println( " ]");
	    
	}
	
        out.println("QueryString= " + request.getQueryString());


	InputStream in=request.getInputStream();

	out.println("START BODY: " );
	int c;
	while( (c=in.read()) >= 0 ) {
	    out.write( (char)c );
	}
	out.println();
	out.println("END BODY");

    }

}



