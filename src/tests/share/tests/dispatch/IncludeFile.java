/* $Id: IncludeFile.java,v 1.2 1999/10/14 23:48:48 akv Exp $
 */
package tests.dispatch;

import java.io.*;
import java.net.*;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;

public class IncludeFile extends TestableBase {

    public String getDescription() {
        return "File Inclusion Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
	    URL url =
		URLHelper.getURL("/servlet/dispatch.IncludeFileServlet");
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
	    if (!s.equals("FOO")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read included line, read: " + s);
		return testResult;
	    }
	    s = r.readLine();
	    if (!s.equals("LINE2")) {
		testResult.setStatus(false);
		testResult.setMessage("Didn't read second parent line");
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
