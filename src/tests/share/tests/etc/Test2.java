
/*
 * $Id: Test2.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */

/**
 * Sample test module.
 */

package tests.etc;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;

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
