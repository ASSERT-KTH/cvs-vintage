/*
 * Created on Jul 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pgp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author waffel
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.columba.mail.pgp");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(PGPControllerTest.class));
		//$JUnit-END$
		return suite;
	}
}
