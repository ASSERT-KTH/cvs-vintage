package CH.ifa.draw.test.framework;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:mtnygard@charter.net">Michael T. Nygard</a>
 * @version $Revision: 1.1 $
 */
public class AllTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllTests.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for CH.ifa.draw.test.framework");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(DrawingChangeEventTest.class));
		suite.addTest(new TestSuite(FigureAttributeConstantTest.class));
		suite.addTest(new TestSuite(FigureChangeEventTest.class));
		//$JUnit-END$
		return suite;
	}
}
