package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */
public class ServletUtil {


    public static void printParams( HttpServletRequest request,
				   PrintWriter out )
        throws IOException, ServletException
    {
	Enumeration paramN=request.getParameterNames();
	out.print( "Params = [");
	
	while( paramN.hasMoreElements() ) {
	    String name=(String)paramN.nextElement();
	    String all[]=request.getParameterValues(name);
	    out.print(name);
	    if( paramN.hasMoreElements()) out.print(" , ");
	}
	
	out.println( " ]");

	paramN=request.getParameterNames();

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
	
        out.println("Query = " + request.getQueryString());
    }

    public static void printBody( HttpServletRequest request,
				  PrintWriter out )
        throws IOException, ServletException
    {
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



