package org.columba.mail.pgp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(AllTests.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.columba.mail.pgp");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(GnuPGUtilTest.class));
		//$JUnit-END$
		return suite;
	}
}
