/* $Id: SimpleRedirect.java,v 1.3 1999/11/02 00:46:51 costin Exp $
 */

package tests.dispatch;

import java.io.*;
import java.net.*;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;

// XXX XXX Add more info about what fails - and what is tested!!!
public class SimpleRedirect extends TestableBase {

    public String getDescription() {
        return "Simple Redirect Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
            String sLoc = "/servlet/dispatch.SimpleRedirectServlet";
            String loc = "/index.html";

	    URL url = URLHelper.getURL(sLoc + "?" + loc);
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();

            String l = con.getHeaderField("Location");

            if (l == null || ! l.endsWith(loc)) {
                testResult.setStatus(false);
		testResult.setMessage("Incorrect Location header");
                return testResult;
            }

            loc = "index.html";

	    url = URLHelper.getURL(sLoc + "?" + loc);
	    con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();

            l = con.getHeaderField("Location");

            if (l == null || ! l.endsWith(loc)) {
                testResult.setStatus(false);
		testResult.setMessage("Incorrect Location header");
                return testResult;
            }

	    // XXX Wrong test - sendRedirect(null) is not specified 
	    // 	    url = URLHelper.getURL(sLoc);
	    // 	    con = (HttpURLConnection)url.openConnection();
	    // 	    con.setFollowRedirects(false);
	    // 	    con.connect();
	    
	    //             l = con.getHeaderField("Location");
	    
	    //             if (l != null) {
	    //                 testResult.setStatus(false);
	    // 		testResult.setMessage("Incorrect Location header");
	    //                 return testResult;
	    //             }

            testResult.setStatus(true);
            return testResult;
  	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);
  	    return testResult; 
  	}
    }
}
