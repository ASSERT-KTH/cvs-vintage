
/*
 * $Id: Test4.java,v 1.2 1999/10/14 23:48:53 akv Exp $
 */

/**
 * Sample test module.
 */

package tests.etc;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;
import java.net.URL;
import java.net.MalformedURLException;

public class Test4 extends TestableBase {

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
