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

package jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ServletToJsp extends HttpServlet {

    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) {
        try {
            /*
            response.setContentType("text/plain");
            PrintWriter pwo = response.getWriter();
            pwo.println("from ServletToJsp servlet");
            */
            // Uncommenting the above and trying to forward below
            // will result in [expetcted] error..
            
            // Set the attribute and Forward to hello.jsp
            request.setAttribute ("servletName", "ServletToJsp");
            getServletConfig().getServletContext().
                getRequestDispatcher("/jsp/jsptoserv/hello.jsp").
                    forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }
}
