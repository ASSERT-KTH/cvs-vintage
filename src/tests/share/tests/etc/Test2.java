
/*
 * $Id: Test2.java,v 1.2 1999/10/14 23:48:53 akv Exp $
 */

/**
 * Sample test module.
 */

package tests.etc;

import org.apache.tools.moo.Testable;
import org.apache.tools.moo.TestResult;

public class Test2 {

    public String getDescription() {
        return "Test2";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();

        testResult.setStatus(false);
        testResult.setMessage("not testable");

        return testResult;
    }
}
