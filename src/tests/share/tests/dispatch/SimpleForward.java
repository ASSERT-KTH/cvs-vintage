/* $Id: SimpleForward.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */
package tests.dispatch;

import java.io.*;
import java.net.*;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;

public class SimpleForward implements Testable {

    public String getDescription() {
        return "Simple Forwarding Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
	    URL url =
		URLHelper.getURL("/servlet/dispatch.SimpleForwardServlet");
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();
	    String ct = con.getContentType();

	    if (! ct.equals("text/funky")) {
		testResult.setStatus(false);
		testResult.setMessage("Wrong content type. Got: " + ct);

		return testResult; 
	    }

	    InputStream in = con.getInputStream();
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    String s = r.readLine();

	    if (s.equals("TARGET1")) {
		testResult.setStatus(true);

		return testResult;
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("Incorrect content in forward");

		return testResult;
	    }
  	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);

  	    return testResult; 
  	}
    }
}
