/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**********************************************************************
 *  SERVLET FOR TESTING COOKIE EXPIRATON
 *  It is a slightly modified version of CookieExample.java
 **********************************************************************/

/* $Id: CookieExpire.java,v 1.2 2004/02/27 05:28:09 billbarker Exp $
 *
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class CookieExpire extends HttpServlet {

    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");

        Cookie[] cookies = request.getCookies();
        if (cookies.length > 0) {
            out.println("Cookies:<br>");
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                out.print("Cookie Name: " + cookie.getName() + "<br>");
                out.println("  Cookie Value: " + cookie.getValue() +
			    "  Cookie MaxAge: " + cookie.getMaxAge() +
                            "<br><br>");
            }
        } else {
            out.println("No cookies");
        }

        String cookieName = request.getParameter("cookiename");
        String cookieValue = request.getParameter("cookievalue");
        int cookieMaxAge = -1;
        try {
                cookieMaxAge = Integer.parseInt(request.getParameter("cookiemaxage"));
        } catch (Exception e) {
        }
        if (cookieName != null && cookieValue != null) {
            Cookie cookie = new Cookie(cookieName, cookieValue);
            cookie.setMaxAge(cookieMaxAge);
            response.addCookie(cookie);
            out.println("<P>");
            out.println("Cookie set <br>");
            out.print("Name  " + cookieName +
                      "<br>");
            out.print("Value  " + cookieValue);
	    out.print("Age  " + cookie.getMaxAge());
        }
        
        out.println("<P>");
        out.println("Make cookie <br>");
        out.print("<form action=\"");
        out.println("CookieExpire\" method=POST>");
        out.print("Name  ");
        out.println("<input type=text length=20 name=cookiename><br>");
        out.print("Value  ");
        out.println("<input type=text length=20 name=cookievalue><br>");
        out.print("Seconds until expiration:" + "  ");
        out.println("<input type=text length=20 name=cookiemaxage value=-1><br>");
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
