/* $Id: FileLengthTest.java,v 1.2 1999/10/14 23:48:55 akv Exp $
 */

/**
 * Tests whether or not file lengths are being correctly set.
 */

package tests.file;

import java.io.*;
import java.net.*;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;

public class FileLengthTest extends TestableBase {

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
