/* $Id: SnoopServlet.java,v 1.3 2003/02/16 23:13:59 larryi Exp $
 *
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;
import util.HTMLFilter;

/**
 *
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 * @author Jason Hunter <jch@eng.sun.com>
 */

public class SnoopServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

        out.println("Snoop Servlet");
	out.println();
	out.println("Servlet init parameters:");
	Enumeration e = getInitParameterNames();
	while (e.hasMoreElements()) {
	    String key = (String)e.nextElement();
	    String value = getInitParameter(key);
	    out.println("   " + key + " = " + value); 
	}
	out.println();

	out.println("Context init parameters:");
	ServletContext context = getServletContext();
	Enumeration enum = context.getInitParameterNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
            Object value = context.getInitParameter(key);
            out.println("   " + key + " = " + value);
	}
	out.println();

	out.println("Context attributes:");
	enum = context.getAttributeNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
            Object value = context.getAttribute(key);
            out.println("   " + key + " = " + value);
	}
	out.println();
	
        out.println("Request attributes:");
        e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            Object value = request.getAttribute(key);
            out.println("   " + HTMLFilter.filter(key) + " = " + value);
        }
        out.println();
        out.println("Servlet Name: " + getServletName());
        out.println("Protocol: " + request.getProtocol());
        out.println("Scheme: " + request.getScheme());
        out.println("Server Name: " + HTMLFilter.filter(request.getServerName()));
        out.println("Server Port: " + request.getServerPort());
        out.println("Server Info: " + context.getServerInfo());
        out.println("Remote Addr: " + request.getRemoteAddr());
        out.println("Remote Host: " + request.getRemoteHost());
        out.println("Character Encoding: " + HTMLFilter.filter(request.getCharacterEncoding()));
        out.println("Content Length: " + request.getContentLength());
        out.println("Content Type: "+ HTMLFilter.filter(request.getContentType()));
        out.println("Locale: "+ HTMLFilter.filter(request.getLocale().toString()));
        out.println("Default Response Buffer: "+ response.getBufferSize());
        out.println();
        out.println("Parameter names in this request:");
        e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String[] values = request.getParameterValues(key);
            out.print("   " + HTMLFilter.filter(key) + " = ");
            for(int i = 0; i < values.length; i++) {
                out.print(HTMLFilter.filter(values[i]) + " ");
            }
            out.println();
        }
        out.println();
        out.println("Headers in this request:");
        e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = request.getHeader(key);
            out.println(HTMLFilter.filter("   " + key + ": " + value));
        }
        out.println();  
        out.println("Cookies in this request:");
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            out.println(HTMLFilter.filter("   " + cookie.getName() + " = " + cookie.getValue()));
        }
        out.println();

        out.println("Request Is Secure: " + request.isSecure());
        out.println("Auth Type: " + request.getAuthType());
        out.println("HTTP Method: " + request.getMethod());
        out.println("Remote User: " + request.getRemoteUser());
        out.println("Request URI: " + request.getRequestURI());
        out.println("Context Path: " + request.getContextPath());
        out.println("Servlet Path: " + request.getServletPath());
        out.println("Path Info: " + HTMLFilter.filter(request.getPathInfo()));
	out.println("Path Trans: " + request.getPathTranslated());
        out.println("Query String: " + HTMLFilter.filter(request.getQueryString()));

        out.println();
        HttpSession session = request.getSession();
        out.println("Requested Session Id: " +
                    HTMLFilter.filter(request.getRequestedSessionId()));
        out.println("Current Session Id: " + session.getId());
	out.println("Session Created Time: " + session.getCreationTime());
        out.println("Session Last Accessed Time: " +
                    session.getLastAccessedTime());
        out.println("Session Max Inactive Interval Seconds: " +
                    session.getMaxInactiveInterval());
        out.println();
        out.println("Session values: ");
        Enumeration names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.println(HTMLFilter.filter("   " + name + " = " + session.getAttribute(name)));
        }
    }
}

