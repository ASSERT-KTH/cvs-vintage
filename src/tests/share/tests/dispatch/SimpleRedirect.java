/* $Id: SimpleRedirect.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */

package tests.dispatch;

import java.io.*;
import java.net.*;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;

public class SimpleRedirect implements Testable {

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

	    url = URLHelper.getURL(sLoc);
	    con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();

            l = con.getHeaderField("Location");

            if (l != null) {
                testResult.setStatus(false);
		testResult.setMessage("Incorrect Location header");
                return testResult;
            }

            testResult.setStatus(true);
            return testResult;
  	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);
  	    return testResult; 
  	}
    }
}
