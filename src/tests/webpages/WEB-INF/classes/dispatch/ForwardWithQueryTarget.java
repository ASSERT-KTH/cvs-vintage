/*
 * $Id: ForwardWithQueryTarget.java,v 1.1 1999/10/09 00:20:58 duncan Exp $
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



