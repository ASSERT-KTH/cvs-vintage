
/*
 * $Id: Test4.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */

/**
 * Sample test module.
 */

package tests.etc;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.URLHelper;
import java.net.URL;
import java.net.MalformedURLException;

public class Test4 implements Testable {

    public static void main(String[] args) {
        Test4 test4 = new Test4();

        test4.runTest();
    }

    public String getDescription() {
        return "Test4";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();

        URL url = null;

        try {
            url = URLHelper.getURL("/foo");
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        }

        testResult.setStatus(false);
        testResult.setMessage("failed big time");

        return testResult;
    }
}
