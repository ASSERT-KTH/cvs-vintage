/* $Id: SessionExample.java,v 1.5 2003/09/29 07:39:48 hgomez Exp $
 *
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import util.HTMLFilter;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class SessionExample extends HttpServlet {

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

        String title = rb.getString("sessions.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");

        // img stuff not req'd for source code html showing
	// relative links everywhere!

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue 
	
        out.println("<a href=\"/examples/servlets/sessions.html\">");
        out.println("<img src=\"/examples/images/code.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"/examples/servlets/index.html\">");
        out.println("<img src=\"/examples/images/return.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"return\"></a>");

        out.println("<h3>" + title + "</h3>");

        HttpSession session = request.getSession();
        out.println(rb.getString("sessions.id") + " " + session.getId());
        out.println("<br>");
        out.println(rb.getString("sessions.isnew") + " " + session.isNew() + "<br>");
        out.println(rb.getString("sessions.created") + " ");
        out.println(new Date(session.getCreationTime()) + "<br>");
        out.println(rb.getString("sessions.lastaccessed") + " ");
        out.println(new Date(session.getLastAccessedTime()));
        out.println("<br>");
        out.println(rb.getString("sessions.requestedid") + " " + request.getRequestedSessionId() + "<br>");
        out.println(rb.getString("sessions.requestedidvalid") + " " + request.isRequestedSessionIdValid() + "<br>");
        out.println(rb.getString("sessions.fromcookie") + " " + request.isRequestedSessionIdFromCookie() + "<br>");
        out.println(rb.getString("sessions.fromurl") + " " + request.isRequestedSessionIdFromURL() + "<br>");

        String invalidate = request.getParameter("INVALIDATE");
        if(invalidate != null){
            session.invalidate();
        }else{
            String dataName = request.getParameter("dataname");
            String dataValue = request.getParameter("datavalue");
            if (dataName != null && dataValue != null) {
                session.setAttribute(dataName, dataValue);
            }

            out.println("<P>");
            out.println(rb.getString("sessions.data") + "<br>");
            Enumeration names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement(); 
                String value = session.getAttribute(name).toString();
                out.println(HTMLFilter.filter(name) + " = " 
                            + HTMLFilter.filter(value) + "<br>");
            }
        }

        out.println("<P>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=POST>");
        out.println(rb.getString("sessions.dataname"));
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println(rb.getString("sessions.datavalue"));
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.println("<P>GET based form:<br>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=GET>");
        out.println(rb.getString("sessions.dataname"));
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println(rb.getString("sessions.datavalue"));
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.println("<P>");
        out.println("<P>Invalidate session:<br>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=POST>");
        out.println("<input type=\"hidden\" name=INVALIDATE value=TRUE>");
        out.println("<input type=submit value=\"Invalidate session\">");
        out.println("</form>");

        out.print("<p><a href=\"");
        out.print(response.encodeURL("SessionExample?dataname=foo&datavalue=bar"));
        out.println("\" >URL encoded </a>");
	
        out.println("</body>");
        out.println("</html>");
        
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
