/*
 * $Id: IncludeJspWithTaglib.java,v 1.2 2004/02/27 05:28:09 billbarker Exp $
 *
 *
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
 *
 * Include a jsp that uses a taglib.
 * Make sure XML parser is available to Jasper.
 */

package dispatch;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class IncludeJspWithTaglib extends HttpServlet

{
    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("text/plain");

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/dispatch/msgTag.jsp");
        rd.include(req,res);
    }
}
