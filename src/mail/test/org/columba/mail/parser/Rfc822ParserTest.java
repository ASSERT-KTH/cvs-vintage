/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import junit.framework.TestCase;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Message;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Rfc822ParserTest extends TestCase {

	/**
	 * Constructor for Rfc822ParserTest.
	 * @param arg0
	 */
	public Rfc822ParserTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		String messageSource = "testdata";

		Message message = new Rfc822Parser().parse(messageSource, null);
	}

	public void testParseHeader() {
		String messageSource = "testdata";

		ColumbaHeader header = new Rfc822Parser().parseHeader(messageSource);
	}

}
