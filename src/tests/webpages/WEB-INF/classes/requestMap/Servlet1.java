/*
 * $Id: Servlet1.java,v 1.2 2004/02/27 05:28:10 billbarker Exp $
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

package requestMap;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Servlet1 extends HttpServlet {
    private String servletInfo = "Servlet1";

    private ServletContext context;
    
    public void init() {
	context = getServletConfig().getServletContext();
    }

    public String getServletInfo() {
        return this.servletInfo;
    }
    
    public void doGet(HttpServletRequest request,
        HttpServletResponse response)
    throws IOException {
	PrintWriter pw = response.getWriter();

	pw.println("Servlet: " + getServletInfo());
    }
}
