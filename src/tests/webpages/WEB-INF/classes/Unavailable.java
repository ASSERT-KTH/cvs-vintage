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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class Unavailable extends HttpServlet {
    static int attempt=0;
    static boolean success=false;
    
    public void init(ServletConfig conf)
        throws ServletException
    {
        attempt++;
        if( attempt < 2 ) 
            throw new UnavailableException("Testing ", 0);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException,ServletException
    {
        res.setContentType("text/plain");
        ServletOutputStream out = res.getOutputStream();

        if (attempt == 1)
            out.println("Error: UnavailableException not handled correctly");
        else if ((attempt % 2) == 0) {
            attempt++;
            out.println("Hello");
        } else {
            attempt++;
            // in case test is re-run
            throw new UnavailableException("Testing ", 0);
        }

        out.close();
    }
}

