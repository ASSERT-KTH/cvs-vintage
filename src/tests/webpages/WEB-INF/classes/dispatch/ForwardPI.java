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
/*
 * $Id: ForwardPI.java,v 1.3 2004/02/27 05:28:09 billbarker Exp $
 */


package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Test FORWARD with a path info
 *
 */
public class ForwardPI extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
	req.setAttribute("originalPI", req.getPathInfo());
	RequestDispatcher rd = getServletContext().getRequestDispatcher("/servlet/RequestDump");
	rd.forward(req, res);
    }

}



