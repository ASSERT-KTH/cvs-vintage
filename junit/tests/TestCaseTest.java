package junit.tests;

import java.util.Vector;
import junit.framework.*;

/**
 * A test case testing the testing framework.
 *
 */
public class TestCaseTest extends TestCase {
	
	static class TornDown extends TestCase {
		boolean fTornDown= false;
		
		TornDown(String name) {
			super(name);
		}
		protected void tearDown() {
			fTornDown= true;
		}
		protected void runTest() {
			throw new Error();
		}
	}

	public TestCaseTest(String name) {
		super(name);
	}
	public void testCaseToString() {
		// This test wins the award for twisted snake tail eating while
		// writing self tests. And you thought those weird anonymous
		// inner classes were bad...
		assertEquals("testCaseToString(junit.tests.TestCaseTest)", toString());
	}
	public void testError() {
		TestCase error= new TestCase("error") {
			protected void runTest() {
				throw new Error();
			}
		};
		verifyError(error);
	}
	public void testRunAndTearDownFails() {
		TornDown fails= new TornDown("fails") {
			protected void tearDown() {
				super.tearDown();
				throw new Error();
			}
			protected void runTest() {
				throw new Error();
			}
		};
		verifyError(fails);
		assert(fails.fTornDown);
	}
	public void testSetupFails() {
		TestCase fails= new TestCase("success") {
			protected void setUp() {
				throw new Error();
			}
			protected void runTest() {
			}
		};
		verifyError(fails);
	}
	public void testSuccess() {
		TestCase success= new TestCase("success") {
			protected void runTest() {
			}
		};
		verifySuccess(success);
	}
	public void testFailure() {
		TestCase failure= new TestCase("failure") {
			protected void runTest() {
				fail();
			}
		};
		verifyFailure(failure);
	}

	public void testTearDownAfterError() {
		TornDown fails= new TornDown("fails");
		verifyError(fails);
		assert(fails.fTornDown);
	}
	
	public void testTearDownFails() {
		TestCase fails= new TestCase("success") {
			protected void tearDown() {
				throw new Error();
			}
			protected void runTest() {
			}
		};
		verifyError(fails);
	}
	public void testTearDownSetupFails() {
		TornDown fails= new TornDown("fails") {
			protected void setUp() {
				throw new Error();
			}
		};
		verifyError(fails);
		assert(!fails.fTornDown);
	}
	public void testWasRun() {
		WasRun test= new WasRun("");
		test.run();
		assert(test.fWasRun);
	}
	void verifyError(TestCase test) {
		TestResult result= test.run();
		assert(result.runCount() == 1);
		assert(result.failureCount() == 0);
		assert(result.errorCount() == 1);
	}
	void verifyFailure(TestCase test) {
		TestResult result= test.run();
		assert(result.runCount() == 1);
		assert(result.failureCount() == 1);
		assert(result.errorCount() == 0);
	}
	void verifySuccess(TestCase test) {
		TestResult result= test.run();
		assert(result.runCount() == 1);
		assert(result.failureCount() == 0);
		assert(result.errorCount() == 0);
	}


}