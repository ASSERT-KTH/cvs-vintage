/* $Id: WelcomeFileTest.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */

/**
 * Tests whether or not file lengths are being correctly set.
 */

package tests.file;

import java.io.*;
import java.net.*;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;

public class WelcomeFileTest implements Testable {

    public String getDescription() {
        return "Welcome Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
	    URL url = URLHelper.getURL("/welcome");
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();
	    int sc = con.getResponseCode();
	    if (sc != 302) {
		testResult.setStatus(false);
		testResult.setMessage("Server didn't forward to /welcome/");
		return testResult;
	    }
	    String loc = con.getHeaderField("Location");
	    if (loc == null || !loc.endsWith("/welcome/")) {
		testResult.setStatus(false);
		testResult.setMessage("Server didn't forward to /welcome/");
		return testResult;
	    }

	    URL url3 = URLHelper.getURL("/binaries");
	    HttpURLConnection con3 = (HttpURLConnection)url3.openConnection();
	    con.setFollowRedirects(false);
	    con.connect();
	    int sc3 = con3.getResponseCode();
	    if (sc3 == 302) {
		testResult.setStatus(false);
		testResult.setMessage("Server tried to redirect when it is "
				      + "not supposed to");
		return testResult;
	    }
	    
	    // make sure that we have proper file

	    HttpURLConnection con2 = (HttpURLConnection)url.openConnection();
	    con2.setFollowRedirects(true);
	    con2.connect();
	    byte[] conbuf = new byte[con2.getContentLength()];
	    InputStream in = con2.getInputStream();
	    int total = 0;
	    do {
		total += in.read(conbuf, total, conbuf.length - total);
	    } while (total < conbuf.length);
	    in.close();
	    File f = new File("webpages" + File.separator + "welcome" +
                File.separator + "index.html");
	    byte[] filebuf = new byte[con2.getContentLength()];
	    BufferedInputStream fin =
		new BufferedInputStream(new FileInputStream(f));
	    total = 0;
	    do {
		total += fin.read(filebuf, total, filebuf.length - total);
	    } while (total < filebuf.length);
	    fin.close();
	    int badbytes = 0;
	    for (int i = 0; i < conbuf.length; i++) {
		int a = conbuf[i];
		int b = filebuf[i];
		if (a != b) {
		    badbytes++;
		}
	    }
	    if (badbytes == 0) {
		testResult.setStatus(true);
		return testResult;
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("Didn't get correct welcome file");
		return testResult;
	    }
	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);
  	    return testResult; 
  	}
    }
}
