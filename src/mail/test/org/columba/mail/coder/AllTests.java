package org.columba.mail.coder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.columba.mail.coder");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(NullDecoderTest.class));
		//$JUnit-END$
		return suite;
	}
}
