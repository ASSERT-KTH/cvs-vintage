/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

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
		TestSuite suite = new TestSuite("Test for org.columba.mail.parser");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(DateParserTest.class));
		suite.addTest(new TestSuite(MailUrlParserTest.class));
		suite.addTest(new TestSuite(MessageParserUtilsTest.class));
		suite.addTest(new TestSuite(MimeMultipartParserTest.class));
		suite.addTest(new TestSuite(MimeParserTest.class));
		suite.addTest(new TestSuite(Rfc822ParserTest.class));
		//$JUnit-END$
		return suite;
	}
}
