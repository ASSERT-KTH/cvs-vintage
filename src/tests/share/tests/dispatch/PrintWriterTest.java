/* $Id: PrintWriterTest.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */

package tests.dispatch;

import java.io.*;
import java.net.*;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;

public class PrintWriterTest implements Testable {

    public String getDescription() {
        return "PrintWriterTest Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
            String sLoc = "/servlet/dispatch.PrintWriterTest1Servlet";

	    URL url = URLHelper.getURL(sLoc);
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();

            InputStream in = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String s = null;

            s = br.readLine();

            if (! s.equals("PreInclude")) {
  	        testResult.setStatus(false);
  	        testResult.setMessage("didn't read expected PreInclude line");
  	        return testResult; 
            }

            s = br.readLine();
            if (s.indexOf("error: 500") > -1) {
                String line = null;

                while ((line = br.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        break;
                    }
                }
            } else {
                testResult.setStatus(false);
                testResult.setMessage("IllegalStateException not thrown ");
                return testResult;
            }

            s = br.readLine();
            if (! s.equals("PostInclude")) {
  	        testResult.setStatus(false);
  	        testResult.setMessage("didn't read expected PostInclude line");
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
