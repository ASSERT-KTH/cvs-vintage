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

public class Cookie25 extends HttpServlet {

    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        res.setContentType("text/plain");
	Cookie c=new Cookie("foo", "bar");
	c.setVersion(1);
	c.setMaxAge( 60 * 60 * 24 * 100 );
	res.addCookie( c );

        ServletOutputStream out = res.getOutputStream();

	out.println("Set cookie " + c.toString());

        out.close();
    }
}

