/* $Id: CookieExample1.java,v 1.3 2003/02/16 23:13:59 larryi Exp $
 *
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import util.HTMLFilter;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class CookieExample1 extends HttpServlet {

    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body bgcolor=\"white\">");
        out.println("<head>");

        String title = rb.getString("cookies.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");

	// relative links

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue 
	
        out.println("<a href=\"/examples/servlets/cookies.html\">");
        out.println("<img src=\"/examples/images/code.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"/examples/servlets/index.html\">");
        out.println("<img src=\"/examples/images/return.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"return\"></a>");

        out.println("<h3>" + title + "</h3>");

        Cookie[] cookies = request.getCookies();
        if ((cookies != null) && (cookies.length > 0)) {
            out.println(rb.getString("cookies.cookies") + "<br>");
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                out.print("Cookie Name: " + HTMLFilter.filter(cookie.getName())
                          + "<br>");
                out.println("  Cookie Value: " 
                            + HTMLFilter.filter(cookie.getValue())
                            + "<br><br>");
                out.println("Cookie Version: " + cookie.getVersion()
                            + "<br>");
                out.println("Cookie Domain: "
                            + HTMLFilter.filter(cookie.getDomain())
                            + "<br>");
                out.println("Cookie Path: "
                            + HTMLFilter.filter(cookie.getPath())
                            + "<br>");
                out.println("<br>");
            }
        } else {
            out.println(rb.getString("cookies.no-cookies"));
        }

        String cookieName = request.getParameter("cookiename");
        String cookieValue = request.getParameter("cookievalue");
	String path= request.getParameter( "cookiepath" );
	String domain= request.getParameter( "cookiedomain" );
	String secure= request.getParameter( "cookiesecure" );
	String version= request.getParameter( "cookieversion" );
	String comment= request.getParameter( "cookiecomment" );
	String maxage= request.getParameter( "cookiemaxage" );
        if (cookieName != null && !"".equals( cookieName) ) {
	    // cookie without value is valid !
	    Cookie cookie = new Cookie(cookieName, cookieValue);
	    if( ! "".equals( path ))
		cookie.setPath( path );
	    if( ! "".equals( domain ))
		cookie.setDomain( domain );
	    if( ! "".equals( secure ))
		cookie.setSecure( true );
	    if( "1".equals( version )) 
		cookie.setVersion(1);
	    if( ! "".equals( comment ))
		cookie.setComment( comment );
	    if( ! "".equals( maxage )) {
		try {
		    Integer max=new Integer( maxage );
		    cookie.setMaxAge( max.intValue() );
		} catch(Exception ex ) {
		}
	    }

            response.addCookie(cookie);
            out.println("<P>");
            out.println(rb.getString("cookies.set") + "<br>");
            out.print(rb.getString("cookies.name") + "  " 
                      + HTMLFilter.filter(cookieName) + "<br>");
            out.print(rb.getString("cookies.value") + "  " 
                      + HTMLFilter.filter(cookieValue));
        }
        
        out.println("<P>");
        out.println(rb.getString("cookies.make-cookie") + "<br>");
        out.print("<form action=\"");
        out.println("CookieExample1\" method=POST>");
        out.print(rb.getString("cookies.name") + "  ");
        out.println("<input type=text length=20 name=cookiename><br>");
        out.print(rb.getString("cookies.value") + "  ");
        out.println("<input type=text length=20 name=cookievalue><br>");
	out.print("Path  ");
	out.println("<input type=text length=20 name=cookiepath><br>");
	out.print("Domain ");
	out.println("<input type=text length=20 name=cookiedomain><br>");
	out.print("Secure ");
	out.println("<input type=text length=20 name=cookiesecure><br>");
	out.print("Version ");
	out.println("<input type=text length=20 name=cookieversion><br>");
	out.print("Comment ");
	out.println("<input type=text length=20 name=cookiecomment><br>");
	out.print("MaxAge ");
	out.println("<input type=text length=20 name=cookiemaxage><br>");
        out.println("<input type=submit></form>");
            
            
        out.println("</body>");
        out.println("</html>");
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }

}


