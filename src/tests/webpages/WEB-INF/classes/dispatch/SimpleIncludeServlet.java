/*
 * $Id: SimpleIncludeServlet.java,v 1.2 2004/02/27 05:28:09 billbarker Exp $
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
 */

package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SimpleIncludeServlet extends HttpServlet {

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	res.setContentType("text/foobar");
	PrintWriter pwo = res.getWriter();
	pwo.println("LINE1");
	String s = "/servlet/dispatch.Target1";
	RequestDispatcher rd = context.getRequestDispatcher(s);
	rd.include(req, res);
	pwo.println("LINE2");
	rd.include(req, res);
	pwo.println("LINE3");
    }
}



