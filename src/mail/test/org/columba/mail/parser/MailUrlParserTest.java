/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MailUrlParserTest extends TestCase {

	/**
	 * Constructor for MailUrlParserTest.
	 * @param arg0
	 */
	public MailUrlParserTest(String arg0) {
		super(arg0);
	}

	public void testParser() {
		String testData = "mailto:fdietz@users.sourceforge.net";

		MailUrlParser p = new MailUrlParser(testData);

		String adr = (String) p.get("mailto:");

		Assert.assertTrue(adr.equals("fdietz@users.sourceforge.net"));

	}

}
