/* $Id: ContentTypeTest.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
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

public class ContentTypeTest implements Testable {

    public String getDescription() {
        return "Content Type Test";
    }

    public TestResult runTest() {
	TestResult testResult = new TestResult();
  	try {
  	    URL url = URLHelper.getURL("/binaries/image1.gif");
  	    URLConnection con = url.openConnection();
  	    con.connect();
	    String ct = con.getContentType();
	    if (ct.equals("image/gif")) {
		testResult.setStatus(true);
		return testResult;
	    } else {
		testResult.setStatus(false);
	    	testResult.setMessage("Got: " + ct + " Expected: image/gif");
	    	return testResult;
	    }
  	} catch (Exception e) {
  	    testResult.setStatus(false);
  	    testResult.setMessage("Exception: " + e);
  	    return testResult; 
  	}

    }
}
