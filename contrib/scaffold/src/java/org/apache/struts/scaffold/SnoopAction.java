/**
 * Derived from the Jetty SnoopServlet
 * <http://www.thecortex.net/clover/eg/jetty/report/default-pkg/SnoopServlet.html>
 * and distributed under its open source license
 * <http://jetty.mortbay.org/jetty/LICENSE.html>.
 * The only modifications were to change the method
 * signatures to make the servlet an Action.
 */

package org.apache.struts.scaffold;

import org.apache.struts.action.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */

public final class SnoopAction extends Action {

    public ActionForward perform(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {
        ActionServlet servlet = getServlet();
        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");
        out.println("Snoop Servlet");
        out.println();
        out.println("Servlet init parameters:");
        Enumeration e = servlet.getInitParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = servlet.getInitParameter(key);
            out.println("   " + key + " = " + value);
        }
        out.println();

        out.println("Context init parameters:");
        ServletContext context = servlet.getServletContext();
        e = context.getInitParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object value = context.getInitParameter(key);
            out.println("   " + key + " = " + value);
        }
        out.println();

        out.println("Context attributes:");
        e = context.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object value = context.getAttribute(key);
            out.println("   " + key + " = " + value);
        }
        out.println();

        out.println("Request attributes:");
        e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object value = request.getAttribute(key);
            out.println("   " + key + " = " + value);
        }
        out.println();

        out.println("Servlet Name: " + servlet.getServletName());
        out.println("Protocol: " + request.getProtocol());
        out.println("Scheme: " + request.getScheme());
        out.println("Server Name: " + request.getServerName());
        out.println("Server Port: " + request.getServerPort());
        out.println("Server Info: " + context.getServerInfo());
        out.println("Remote Addr: " + request.getRemoteAddr());
        out.println("Remote Host: " + request.getRemoteHost());
        out.println("Character Encoding: " + request.getCharacterEncoding());
        out.println("Content Length: " + request.getContentLength());
        out.println("Content Type: " + request.getContentType());
        out.println("Locale: " + request.getLocale());
        out.println("Default Response Buffer: " + response.getBufferSize());
        out.println();

        out.println("Parameter names in this request:");
        e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String[] values = request.getParameterValues(key);
            out.print("   " + key + " = ");
            for (int i = 0; i < values.length; i++) {
                out.print(values[i] + " ");
            }
            out.println();
        }
        out.println();

        out.println("Headers in this request:");
        e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = request.getHeader(key);
            out.println("   " + key + ": " + value);
        }
        out.println();

        out.println("Cookies in this request:");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                out.println("   " + cookie.getName() + " = "
                        + cookie.getValue());
            }
        }
        out.println();

        out.println("Request Is Secure: " + request.isSecure());
        out.println("Auth Type: " + request.getAuthType());
        out.println("HTTP Method: " + request.getMethod());
        out.println("Remote User: " + request.getRemoteUser());
        out.println("Request URI: " + request.getRequestURI());
        out.println("Context Path: " + request.getContextPath());
        out.println("Servlet Path: " + request.getServletPath());
        out.println("Path Info: " + request.getPathInfo());
        out.println("Path Trans: " + request.getPathTranslated());
        out.println("Query String: " + request.getQueryString());
        out.println();

        HttpSession session = request.getSession();
        out.println("Requested Session Id: " +
                request.getRequestedSessionId());
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
            out.println("   " + name + " = " + session.getAttribute(name));
        }
        out.println("###");

    return null;

    }
}

