package junit.tests;

/**
 * Test class used in SuiteTest
 */
import junit.framework.TestCase;

public class NotVoidTestCase extends TestCase {
	public NotVoidTestCase(String name) {
		super(name);
	}
	public int testNotVoid() {
		return 1;
	}
	public void testVoid() {
	}
}