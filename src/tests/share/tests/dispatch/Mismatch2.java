/* $Id: Mismatch2.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */
package tests.dispatch;

import java.io.*;
import java.net.*;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;

public class Mismatch2 implements Testable {

    public String getDescription() {
        return "Mismatch2 Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
	    URL url =
		URLHelper.getURL("/servlet/dispatch.Mismatch2");
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();
	    InputStream in = con.getInputStream();
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    String s = r.readLine();

	    if (s.equals("SOS OUT")) {
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("Incorrect start of content " + s);
		return testResult;
	    }

	    s = r.readLine();
	    if (s.equals("YES")) {

	    } else {
		testResult.setStatus(false);
		testResult.setMessage("IllegalStateException not thrown ");
		return testResult;
	    }

	    s = r.readLine();	    
	    if (s.equals("FINISH")) {
		testResult.setStatus(true);
		return testResult;
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("Incorrect finish statement " + s);
		return testResult;
	    }
  	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);
  	    return testResult; 
  	}
    }
}
