package org.columba.core.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.columba.core.util");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(ListToolsTest.class));
		//$JUnit-END$
		return suite;
	}
}
