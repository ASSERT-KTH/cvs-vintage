
/*
 * $Id: Test3.java,v 1.2 1999/10/14 23:48:53 akv Exp $
 */

/**
 * Sample test module.
 */

package tests.etc;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;

public class Test3 extends TestableBase {

    public String getDescription() {
        return "Test3";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();

        testResult.setStatus(false);
        testResult.setMessage("failed big time");

        return testResult;
    }
}
