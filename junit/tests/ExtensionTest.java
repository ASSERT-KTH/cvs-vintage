package junit.tests;

import junit.framework.*;
import junit.extensions.*;

/**
 * A test case testing the extensions to the testing framework.
 *
 */
public class ExtensionTest extends TestCase {
	static class TornDown extends TestSetup {
		boolean fTornDown= false;
		
		TornDown(Test test) {
			super(test);
		}
		protected void tearDown() {
			fTornDown= true;
		}
	}
	public ExtensionTest(String name) {
		super(name);
	}
	public void testRunningErrorInTestSetup() {
		TestCase test= new TestCase("failure") {
			public void runTest() {
				fail();
			}
		};

		TestSetup wrapper= new TestSetup(test);

		TestResult result= new TestResult();
		wrapper.run(result);
		assert(!result.wasSuccessful());
	}
	public void testRunningErrorsInTestSetup() {
		TestCase failure= new TestCase("failure") {
			public void runTest() {
				fail();
			}
		};

		TestCase error= new TestCase("error") {
			public void runTest() {
				throw new Error();
			}
		};

		TestSuite suite= new TestSuite();
		suite.addTest(failure);
		suite.addTest(error);
		
		TestSetup wrapper= new TestSetup(suite);

		TestResult result= new TestResult();
		wrapper.run(result);

		assertEquals(1, result.failureCount());
		assertEquals(1, result.errorCount());
	}
	public void testSetupErrorDontTearDown() {
		WasRun test= new WasRun("");

		TornDown wrapper= new TornDown(test) {
			public void setUp() {
				fail();
			}
		};

		TestResult result= new TestResult();
		wrapper.run(result);

		assert(!wrapper.fTornDown);
	}
	public void testSetupErrorInTestSetup() {
		WasRun test= new WasRun("");

		TestSetup wrapper= new TestSetup(test) {
			public void setUp() {
				fail();
			}
		};

		TestResult result= new TestResult();
		wrapper.run(result);

		assert(!test.fWasRun);
		assert(!result.wasSuccessful());
	}
}