/* $Id: FileLengthTest.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
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

public class FileLengthTest implements Testable {

    public String getDescription() {
        return "File Length Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
  	    URL url = URLHelper.getURL("/binaries/image1.gif");
  	    URLConnection con = url.openConnection();
  	    con.connect();
  	    long lm = con.getContentLength();
	    File f = new File("webpages" + File.separator +
                "binaries" + File.separator + "image1.gif");

	    if (lm == f.length()) {
		testResult.setStatus(true);
		return testResult;
	    } else {
		testResult.setStatus(false);
		testResult.setMessage("Got: " + lm + " Expected: "
				      + f.length());
		return testResult;
	    }
	    
  	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);
  	    return testResult; 
  	}

    }
}
