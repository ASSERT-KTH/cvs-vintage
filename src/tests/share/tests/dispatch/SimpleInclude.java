/* $Id: SimpleInclude.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */
package tests.dispatch;

import java.io.*;
import java.net.*;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;

public class SimpleInclude implements Testable {

    public String getDescription() {
        return "Simple Inclusion Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
	    URL url =
		URLHelper.getURL("/servlet/dispatch.SimpleIncludeServlet");
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();
	    String ct = con.getContentType();

	    if (!ct.equals("text/foobar")) {
		testResult.setStatus(false);
		testResult.setMessage("Wrong content type. Got: " + ct);
		return testResult; 
	    }

	    InputStream in = con.getInputStream();
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    String s = r.readLine();
	    if (!s.equals("LINE1")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read first parent line");
		return testResult;
	    }
	    s = r.readLine();
	    if (!s.equals("TARGET1")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read first included line");
		return testResult;
	    }
	    s = r.readLine();
	    if (!s.equals("LINE2")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read second parent line");
		return testResult;
	    }
	    s = r.readLine();
	    if (!s.equals("TARGET1")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read second included line");
		return testResult;
	    }
	    s = r.readLine();
	    if (!s.equals("LINE3")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read third parent line");
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
