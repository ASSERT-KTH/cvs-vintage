/* $Id: IncludeMismatch2.java,v 1.2 1999/10/14 23:48:49 akv Exp $
 */
package tests.dispatch;

import java.io.*;
import java.net.*;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;

public class IncludeMismatch2 extends TestableBase {

    public String getDescription() {
        return "IncludeMismatch2 Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
	    URL url =
		URLHelper.getURL("/servlet/dispatch.IncludeMismatch2");
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();
	    InputStream in = con.getInputStream();
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    String s = r.readLine();

	    if (s.equals("LINE1")) {
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("Incorrect start of content " + s);
		return testResult;
	    }

            s = r.readLine();
            if (s.indexOf("error: 500") > -1) {
                String line = null;

                while ((line = r.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        break;
                    }
                }
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("IllegalStateException not thrown ");
		return testResult;
	    }

	    s = r.readLine();	    
	    if (s.equals("LINE2")) {
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
