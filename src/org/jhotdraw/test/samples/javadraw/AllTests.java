package CH.ifa.draw.test.samples.javadraw;

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
		TestSuite suite = new TestSuite("Test for CH.ifa.draw.test.samples.javadraw");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(AnimationDecoratorTest.class));
		suite.addTest(new TestSuite(AnimatorTest.class));
		suite.addTest(new TestSuite(BouncingDrawingTest.class));
		suite.addTest(new TestSuite(JavaDrawAppTest.class));
		suite.addTest(new TestSuite(JavaDrawAppletTest.class));
		suite.addTest(new TestSuite(JavaDrawViewerTest.class));
		suite.addTest(new TestSuite(MySelectionToolTest.class));
		suite.addTest(new TestSuite(PatternPainterTest.class));
		suite.addTest(new TestSuite(URLToolTest.class));
		//$JUnit-END$
		return suite;
	}
}
