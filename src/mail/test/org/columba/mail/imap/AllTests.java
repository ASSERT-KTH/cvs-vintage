/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.columba.mail.imap");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(IMAPResponseTest.class));
		//$JUnit-END$
		return suite;
	}
}
