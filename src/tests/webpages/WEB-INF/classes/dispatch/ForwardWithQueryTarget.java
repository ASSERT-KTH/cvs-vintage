/*
 * $Id: ForwardWithQueryTarget.java,v 1.2 2004/02/27 05:28:09 billbarker Exp $
 */
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

/**
 * Test FORWARD with a query string
 *
 * @author Arun Jamwal [arunj@eng.sun.com]
 */


package dispatch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class ForwardWithQueryTarget extends HttpServlet {

    public void init() {
	    context = getServletConfig().getServletContext();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
	    res.setContentType(ContentType);
	    PrintWriter pwo = res.getWriter();
	    pwo.println(TargetTag);

	    Hashtable hash = new Hashtable();
	    Enumeration pNames = req.getParameterNames();
	    while (pNames.hasMoreElements()) {
           String name = (String) pNames.nextElement();
	       hash.put(name, req.getParameter(name).trim());
	    }

	    boolean firstPair = true;
        Enumeration e = hash.keys();
	    while (e.hasMoreElements()) {
	      if (firstPair) 
		    firstPair = false;
	      else 
		    pwo.print("&");

       	  String name = (String) e.nextElement();
          pwo.print(name + "=" + hash.get(name));
      	}
    }


    private ServletContext context;
    private static final String ContentType = "text/funky";
    private static final String TargetTag = "FORWARDWITHQUERYTARGET";
}



