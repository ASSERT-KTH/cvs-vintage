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

public class ja extends HttpServlet
{
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  {
    try
    {
      if ((req.getParameter("lang") != null) && req.getParameter("lang").equals("ja"))
      {
        resp.setContentType("text/html; charset=euc-jp");
        PrintWriter out = resp.getWriter();
        out.println("<html><head><title>Japanese</title></head>");
        out.println("<body>");
        out.println("<p>Japanese</p>");
        out.println("<p>Encoding is " + resp.getCharacterEncoding());

        out.println("<p>Location = \u6240\u5728\u5730\uff1a</p>");
        out.println("<p>Latest research updates = \u76f4\u8fd1\u306e\u30ea\u30b5\u30fc\u30c1\u53ca\u3073\u66f4\u65b0</p>");
        out.println("</body>");
        out.println("</html>");
      }
      else
      {
        resp.setContentType("text/html; charset=iso-8859-1");
        PrintWriter out = resp.getWriter();
        out.println("<html><head><title>English</title></head>");
        out.println("<body>");
        out.println("<p>English</p>");
	out.println("<p>Encoding is " + resp.getCharacterEncoding());
        out.println("</body>");
        out.println("</html>");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
